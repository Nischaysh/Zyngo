package com.example.vibin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.vibin.databinding.FragmentHomeBinding
import com.example.vibin.databinding.FragmentProfileBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileImage: ShapeableImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userAdapter: UserAdapter

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

        binding.HideText.setOnClickListener {
            if (binding.HideText.text == "Show"){
            binding.userRecycleview.visibility = View.VISIBLE
            binding.HideText.text = "Hide"
            }else{
                binding.userRecycleview.visibility = View.GONE
                binding.HideText.text = "Show"
            }
        }
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
                showMessage("Got ${documents.size()} documents")
                val userList = documents.mapNotNull { document ->
                    val username = document.getString("firstName")
                    val profileImageUrl = document.getString("profileImageUrl")
                    showMessage("User: $username, Image URL: $profileImageUrl")
                    
                    if (profileImageUrl.isNullOrEmpty()) {
                        showMessage("Warning: Empty profile image URL for user $username")
                    }
                    
                    User(
                        uid = document.id,
                        name = username ?: "",
                        profileImageUrl = profileImageUrl ?: ""
                    )
                }
                if (userList.isEmpty()) {
                    showMessage("No users found in the list")
                } else {
                    showMessage("Found ${userList.size} users")
                    userAdapter = UserAdapter(userList)
                    binding.userRecycleview.adapter = userAdapter
                }
            }
            .addOnFailureListener { e ->
                showMessage("Error fetching users: ${e.message}")
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