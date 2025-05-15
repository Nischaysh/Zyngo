package com.example.vibin.Activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.vibin.R
import com.example.vibin.databinding.ActivityUserProfileBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()


        // Get the user ID passed from the previous activity
        val userId = intent.getStringExtra("userId")
        if (userId != null) {
            loadUserData(userId)
        }

        // Set up back button
        binding.backButton.setOnClickListener { finish() }
        binding.messageTopButton.setOnClickListener { /* TODO: Message action */ }
        binding.followButton.setOnClickListener { /* TODO: Follow action */ }
        binding.messageButton.setOnClickListener { /* TODO: Message action */ }
    }

    private fun loadUserData(userId: String) {
        db.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val username = document.getString("username") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val followers = document.getLong("followers") ?: 0L
                    val following = document.getLong("following") ?: 0L

                    // Set profile image
                    Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.man)
                        .error(R.drawable.man)
                        .circleCrop()
                        .into(binding.profileImage)

                    // Set handle
                    binding.userfullname.text = firstName+" "+lastName
                    binding.userHandle.text = "@$username"
                    // Set bio
                    binding.userBio.text = bio
                    // Set stats
                    binding.followersCount.text = followers.toString()
                    binding.followingCount.text = following.toString()

                }
            }
    }
}