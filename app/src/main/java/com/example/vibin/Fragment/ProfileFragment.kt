package com.example.vibin.Fragment

import Post
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.vibin.Activity.SigninActivity
import com.example.vibin.Adapter.PostAdapter
import com.example.vibin.Adapter.ShimmerAdapter
import com.example.vibin.BottomSheet.UpdateProfileBottomSheet
import com.example.vibin.R
import com.example.vibin.databinding.FragmentProfileBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yalantis.ucrop.UCrop
import java.io.File

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileImage: ShapeableImageView
    private lateinit var editProfileImageButton: View
    private lateinit var menuButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var postAdapter: PostAdapter
    private val userPosts = mutableListOf<Post>()
    private lateinit var storage: FirebaseStorage
    private lateinit var shimmerAdapter: ShimmerAdapter
    private lateinit var storageRef: StorageReference

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let { uri ->
                launchCropper(uri) // 🔍 Launch cropper instead of direct upload
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        profileImage = view.findViewById(R.id.profileImage)
        editProfileImageButton = view.findViewById(R.id.editProfileImageButton)
        menuButton = view.findViewById(R.id.menuButton)

        fetchUserData()
        loadPostCount()
        startShimmer()
        loadUserPosts()

        editProfileImageButton.setOnClickListener {
            showCustomDialog("Wanna upload profile pic?") {
                openGallery()
            }
        }

        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        binding.editProfileButton.setOnClickListener {
            val bottomSheet = UpdateProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }

        postAdapter = PostAdapter(userPosts)
        binding.recyclerUserPosts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUserPosts.adapter = postAdapter


    }

    private fun startShimmer() {
        binding.shimmerRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            shimmerAdapter = ShimmerAdapter(5)
            adapter = shimmerAdapter
            visibility = View.VISIBLE
        }
        binding.recyclerUserPosts.visibility = View.GONE
    }
    private fun stopShimmer() {
        binding.shimmerRecyclerView.visibility = View.GONE
        binding.recyclerUserPosts.visibility = View.VISIBLE
    }


    private fun loadUserPosts() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("posts")
            .whereEqualTo("userId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // handle error
                    return@addSnapshotListener
                }

                userPosts.clear()
                for (doc in snapshots!!) {
                    val post = doc.toObject(Post::class.java).copy(postId = doc.id) // make sure postId is set
                    userPosts.add(post)
                }
                postAdapter.notifyDataSetChanged()
                stopShimmer()
            }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        getContent.launch(intent)
    }

    // 🔍 Launch uCrop for circular cropping
    private fun launchCropper(sourceUri: Uri) {
        val destinationUri = Uri.fromFile(
            File(requireContext().cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
        )

        val options = UCrop.Options().apply {
            setCompressionQuality(80)
            setCircleDimmedLayer(true)
            setShowCropFrame(false)
            setShowCropGrid(false)
            setHideBottomControls(true)
            setFreeStyleCropEnabled(false)
            requireActivity().setTheme(R.style.UcropFixTheme)
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        UCrop.of(sourceUri, destinationUri)
            .withAspectRatio(1f, 1f)
            .withOptions(options)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(data!!)
            resultUri?.let { uploadImageToFirebase(it) }

        } else if (resultCode == UCrop.RESULT_ERROR) {
            val cropError = UCrop.getError(data!!)
            showMessage("Crop error: ${cropError?.message}")
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            showMessage("Uploading image...")

            val imageRef = storageRef.child("profile_images/${user.uid}.jpg")

            imageRef.putFile(imageUri)
                .addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        db.collection("users")
                            .document(user.uid)
                            .update("profileImageUrl", uri.toString())
                            .addOnSuccessListener {
                                loadProfileImage(uri.toString())
                                showMessage("Profile image updated successfully")
                            }
                            .addOnFailureListener { e ->
                                showMessage("Error updating profile: ${e.message}")
                            }
                    }
                }
                .addOnFailureListener { e ->
                    showMessage("Error uploading image: ${e.message}")
                }
        }
    }

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_default_user)
            .error(R.drawable.ic_default_user)
            .circleCrop()
            .into(profileImage)
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username")
                        val firstName = document.getString("firstName")
                        val bio = document.getString("bio")
                        val followingCount = document.getLong("followingCount") ?: 0L
                        val followerCount = document.getLong("followerCount") ?: 0L
                        val profileImageUrl = document.getString("profileImageUrl")

                        binding.toolbarUsername.text = username ?: "Username not set"
                        binding.nameText.text = firstName ?: "Name not set"
                        binding.followingCount.text = followingCount.toString()
                        binding.followersCount.text = followerCount.toString()
                        binding.bioText.text = bio

                        profileImageUrl?.let { url -> loadProfileImage(url) }
                    } else {
                        showMessage("User data not found")
                    }
                }
                .addOnFailureListener { e ->
                    showMessage("Error fetching user data: ${e.message}")
                }
        }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_account -> {
                    val bottomSheet = UpdateProfileBottomSheet()
                    bottomSheet.show(childFragmentManager, "UpdateProfileBottomSheet")
                    true
                }
                R.id.menu_about -> {
                    showMessage("About")
                    true
                }
                R.id.menu_logout -> {
                    auth.signOut()
                    startActivity(Intent(requireContext(), SigninActivity::class.java))
                    activity?.finish()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showCustomDialog(message: String, onYes: () -> Unit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val messageText = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnYes = dialogView.findViewById<Button>(R.id.ButtonYes)
        val btnNo = dialogView.findViewById<Button>(R.id.ButtonNo)

        messageText.text = message

        btnYes.setOnClickListener {
            onYes()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showMessage(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadPostCount() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("posts")
            .whereEqualTo("userId", currentUserId)
            .get()
            .addOnSuccessListener { snapshot ->
                val postCount = snapshot.size()
                binding.postsCount.text = postCount.toString()
            }
            .addOnFailureListener { e ->
                binding.postsCount.text = "0"
            }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
