package com.example.vibin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.widget.PopupMenu
import com.example.vibin.databinding.FragmentProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileImage: ShapeableImageView
    private lateinit var editProfileImageButton: View
    private lateinit var menuButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    private val getContent = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = result.data?.data
            selectedImageUri?.let { uri ->
                uploadImageToFirebase(uri)
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

        // Set default profile image
        profileImage.setImageResource(R.drawable.ic_default_user)

        // Fetch user data including profile image
        fetchUserData()

        // Set up click listener for edit profile image button
        editProfileImageButton.setOnClickListener {
            openGallery()
        }

        // Set up menu button click listener
        menuButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        binding.editProfileButton.setOnClickListener {
            val bottomSheet = UpdateProfileBottomSheet()
            bottomSheet.show(parentFragmentManager, bottomSheet.tag)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        getContent.launch(intent)
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // Show loading state
            showMessage("Uploading image...")

            // Create a reference to the image in Firebase Storage
            val imageRef = storageRef.child("profile_images/${user.uid}.jpg")

            // Upload the image
            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Update Firestore with the image URL
                        db.collection("users")
                            .document(user.uid)
                            .update("profileImageUrl", uri.toString())
                            .addOnSuccessListener {
                                // Update the profile image in the UI
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

                        // Update UI with the retrieved data
                        binding.toolbarUsername.text = username ?: "Username not set"
                        binding.nameText.text = firstName ?: "Name not set"
                        binding.followingCount.text = followingCount.toString()
                        binding.followersCount.text = followerCount.toString()
                        binding.bioText.text = bio

                        // Load profile image if URL exists
                        profileImageUrl?.let { url ->
                            loadProfileImage(url)
                        }
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
                    // Show update profile bottom sheet
                    val bottomSheet = UpdateProfileBottomSheet()
                    bottomSheet.show(childFragmentManager, "UpdateProfileBottomSheet")
                    true
                }
                R.id.menu_about -> {
                    // Handle about click
                    showMessage("About")
                    true
                }
                R.id.menu_logout -> {
                    // Handle logout click
                    auth.signOut()
                    // Navigate to SigninActivity
                    startActivity(Intent(requireContext(), SigninActivity::class.java))
                    activity?.finish()
                    true
                }
                else -> false
            }
        }

        popupMenu.show()
    }

    private fun showMessage(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // prevent memory leaks
    }
}