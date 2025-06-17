package com.example.vibin.Fragment

import Post
import android.R.attr.visibility
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.vibin.Activity.CreatePostActivity
import com.example.vibin.Adapter.PostAdapter
import com.example.vibin.Adapter.ShimmerAdapter
import com.example.vibin.R
import com.example.vibin.models.User
import com.example.vibin.Adapter.UserAdapter
import com.example.vibin.BottomSheet.NotificationBottomSheet
import com.example.vibin.databinding.FragmentHomeBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileImage: ShapeableImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter
    private lateinit var postAdapter: PostAdapter
    private lateinit var shimmerAdapter: ShimmerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupRecyclerView()
        fetchUserData()
        fetchAllUsers()
        binding.postRecycleview.layoutManager = LinearLayoutManager(requireContext()) // or requireContext() if in Fragment
        startShimmer()
        fetchAllPosts()
        setupSwipeToRefresh()


        binding.btnpicimage.setOnClickListener {
                startActivity(Intent(requireContext(), CreatePostActivity::class.java))
        }

        binding.HideText.setOnClickListener {
            if (binding.HideText.text == "Show"){
            binding.userRecycleview.visibility = View.VISIBLE
            binding.HideText.text = "Hide"
            }else{
                binding.userRecycleview.visibility = View.GONE
                binding.HideText.text = "Show"
            }
        }
        binding.btnPost.setOnClickListener {
            val text = binding.etCaption.text.toString().trim()
            val textLayout = binding.usernameLayout

            if(text.isEmpty()){
                textLayout.error = "Please Write Something"
            }
            else{
                textLayout.error = null
                savePost()
            }

        }




        binding.NotificationButton.setOnClickListener {
            val bottomsheet = NotificationBottomSheet()
            bottomsheet.show(parentFragmentManager,bottomsheet.tag)
        }
    }

    private fun startShimmer() {
        binding.shimmerRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            shimmerAdapter = ShimmerAdapter(5)
            adapter = shimmerAdapter
            visibility = View.VISIBLE
        }
        binding.postRecycleview.visibility = View.GONE
    }
    private fun stopShimmer() {
        binding.shimmerRecyclerView.visibility = View.GONE
        binding.postRecycleview.visibility = View.VISIBLE
    }


    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchAllPosts()
            startShimmer()
        }
        binding.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary_variant)
        binding.swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE)
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.primary,)
    }


    private fun setupViews() {
        profileImage = binding.profileicon
    }

    private fun setupRecyclerView() {
        binding.userRecycleview.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            userAdapter = UserAdapter(emptyList())
            adapter = userAdapter
        }
    }

    private fun fetchAllUsers() {
        showMessage("Fetching users...")
        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
//                showMessage("Got ${documents.size()} documents")
                val userList = documents.mapNotNull { document ->
                    val username = document.getString("username")
                    val firstname = document.getString("firstName")
                    val profileImageUrl = document.getString("profileImageUrl")

                    if (profileImageUrl.isNullOrEmpty()) {
                        showMessage("Warning: Empty profile image URL for user $username")
                    }

                    User(
                        uid = document.id,
                        username = username ?: "",
                        firstname = firstname ?: "",
                        profileImageUrl = profileImageUrl ?: ""
                    )
                }
                if (userList.isEmpty()) {
                    showMessage("No users found in the list")
                } else {

                    userAdapter = UserAdapter(userList)
                    binding.userRecycleview.adapter = userAdapter
                }
            }
            .addOnFailureListener { e ->
                showMessage("Error fetching users: ${e.message}")
            }
    }
    private fun fetchAllPosts() {
        db.collection("posts")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val postList = documents.mapNotNull { document ->
                    val userName = document.getString("userName")
                    val text = document.getString("text")
                    val userId = document.getString("userId")
                    val imageUrl = document.getString("imageUrl")
                    val timestamp = document.getTimestamp("timestamp")
                    val likes = document.getLong("likes")?.toInt() ?: 0
                    val likedBy = document.get("likedBy") as? List<String> ?: emptyList()

                    Post(
                        postId = document.id,
                        userId = userId ?: "",
                        text = text ?: "",
                        userName = userName ?: "",
                        imageUrl = imageUrl ?: "",
                        timestamp = timestamp,
                        likes = likes,
                        likedBy = likedBy
                    )
                }

                if (postList.isEmpty()) {
                    showMessage("No post found in the list")
                } else {
                    postAdapter = PostAdapter(postList.toMutableList())
                    binding.postRecycleview.adapter = postAdapter
                }
                stopShimmer()
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                showMessage("Error fetching post: ${e.message}")
                binding.swipeRefreshLayout.isRefreshing = false
                stopShimmer()
            }
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val gender = document.getString("gender")
                        if(gender == "Male"){
                            binding.profileicon.setImageResource(R.drawable.man)
                        }else{
                            binding.profileicon.setImageResource(R.drawable.woman)
                        }

                        val profileImageUrl = document.getString("profileImageUrl")
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

    private fun loadProfileImage(imageUrl: String) {
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_default_user)
            .error(R.drawable.ic_default_user)
            .circleCrop()
            .into(profileImage)
    }

    private fun savePost() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        val userDocRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)

        userDocRef.get()
            .addOnSuccessListener { document ->
                val userName = document.getString("username") ?: "Unknown"

                val post = Post(
                    userId = uid,
                    userName = userName,
                    text = binding.etCaption.text.toString(),
                    imageUrl = null,
                    timestamp = Timestamp.now()
                )

                FirebaseFirestore.getInstance().collection("posts")
                    .add(post)
                    .addOnSuccessListener {
                        showMessage("Posted")
                        binding.etCaption.text?.clear()
                    }
            }
            .addOnFailureListener {
                showMessage("Failed to fetch user data.")
            }
    }

    private fun showMessage(message: String) {
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}