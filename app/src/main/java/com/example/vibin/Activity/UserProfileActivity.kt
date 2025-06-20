package com.example.vibin.Activity

import Post
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.vibin.Adapter.PostAdapter
import com.example.vibin.R
import com.example.vibin.databinding.ActivityUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlin.toString

class UserProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserProfileBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var postAdapter: PostAdapter
    private val userPosts = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val currentUserId = auth.currentUser?.uid

        // Get the user ID passed from the previous activity
        val targetUserId = intent.getStringExtra("userId")
        loadUserPosts(targetUserId.toString())
        loadUserData(targetUserId.toString())
        checkFollow(currentUserId.toString(),targetUserId.toString())

        postAdapter = PostAdapter(userPosts)
        binding.recyclerUserPosts.layoutManager = LinearLayoutManager(this)
        binding.recyclerUserPosts.adapter = postAdapter

        binding.followButton.setOnClickListener {
            if(binding.followButton.text == "FOLLOW"){
                followUser(currentUserId.toString(),targetUserId.toString())
                binding.followButton.text = "UNFOLLOW"
            }
            else{
                unfollowUser(currentUserId.toString(),targetUserId.toString())
                binding.followButton.text = "FOLLOW"
            }
        }



        // Set up back button
        binding.backButton.setOnClickListener { finish() }
        binding.messageButton.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("otherUserId", targetUserId)
            startActivity(intent)
        }
    }

    private fun checkFollow(currentUserId : String , targetUserId : String){
        val followingDoc = db.collection("users").document(currentUserId.toString())
            .collection("following").document(targetUserId.toString())

        followingDoc.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // Already following → Show "Unfollow"
                binding.followButton.text = "UNFOLLOW"
            } else {
                // Not following → Show "Follow"
                binding.followButton.text = "FOLLOW"
            }
        }

    }
    private fun followUser(currentUserId : String , targetUserId : String){
        // Add to following
        db.collection("users").document(currentUserId!!)
            .collection("following").document(targetUserId)
            .set(mapOf("followedAt" to FieldValue.serverTimestamp()))
        // Add to followers
        db.collection("users").document(targetUserId)
            .collection("followers").document(currentUserId)
            .set(mapOf("followedAt" to FieldValue.serverTimestamp()))

        updateFollowCount(currentUserId.toString(),targetUserId.toString(),"follow")
    }

    private fun unfollowUser(currentUserId : String , targetUserId : String){
        // Remove from following
        db.collection("users").document(currentUserId!!)
            .collection("following").document(targetUserId)
            .delete()
        // Remove from followers
        db.collection("users").document(targetUserId)
            .collection("followers").document(currentUserId)
            .delete()

        updateFollowCount(currentUserId.toString(),targetUserId.toString(),"unfollow")
    }

    private fun updateFollowCount(currentUserId: String, targetUserId: String , Operation: String){
        if (Operation == "follow"){
            db.collection("users").document(targetUserId)
                .update("followerCount", FieldValue.increment(1))
            db.collection("users").document(currentUserId)
                .update("followingCount", FieldValue.increment(1))
        }else{
            db.collection("users").document(targetUserId)
                .update("followerCount", FieldValue.increment(-1))
            db.collection("users").document(currentUserId)
                .update("followingCount", FieldValue.increment(-1))
        }
    }


    private fun loadUserData(userId: String) {
        db.collection("users")
            .document(userId)
            .addSnapshotListener { document, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: ""
                    val lastName = document.getString("lastName") ?: ""
                    val username = document.getString("username") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl") ?: ""
                    val bio = document.getString("bio") ?: ""
                    val followers = document.getLong("followerCount") ?: 0L
                    val following = document.getLong("followingCount") ?: 0L

                    // Set profile image
                    Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.man)
                        .error(R.drawable.man)
                        .circleCrop()
                        .into(binding.profileImage)

                    // Set user data
                    binding.userfullname.text = "$firstName $lastName"
                    binding.userHandle.text = "@$username"
                    binding.userBio.text = bio
                    binding.followersCount.text = followers.toString()
                    binding.followingCount.text = following.toString()
                }
            }
    }

    private fun loadUserPosts(targetuserid : String) {
        val currentUserId = targetuserid

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
            }
    }

}