package com.example.vibin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
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
        profileImage = view.findViewById(R.id.profileicon)

        // Set default profile image
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
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
                    }
                }
                .addOnFailureListener { e ->
                    showMessage("Error loading profile data: ${e.message}")
                }
        }
        profileImage.setImageResource(R.drawable.man)
        fetchUserData()

    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val profileImageUrl = document.getString("profileImageUrl")
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

}