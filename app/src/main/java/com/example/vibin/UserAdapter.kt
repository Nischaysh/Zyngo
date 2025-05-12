package com.example.vibin

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.databinding.ItemUserBinding

class UserAdapter(private val users: List<User>) : 
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(private val binding: ItemUserBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.userName.text = user.name
            
            // Log the image URL we're trying to load
            Log.d("UserAdapter", "Loading image for user ${user.name}: ${user.profileImageUrl}")
            
            // Load profile image using Glide
            Glide.with(binding.root.context)
                .load(user.profileImageUrl)
                .placeholder(R.drawable.man)
                .error(R.drawable.man)
                .circleCrop()
                .into(binding.userProfileImage)

            // Set click listener
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, UserProfileActivity::class.java).apply {
                    putExtra("userId", user.uid)
                }
                context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size
} 