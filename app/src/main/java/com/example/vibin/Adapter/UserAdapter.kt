package com.example.vibin.Adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.Activity.UserProfileActivity
import com.example.vibin.R
import com.example.vibin.Adapter.User
import com.example.vibin.databinding.ItemUserBinding

class UserAdapter(private val users: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.userName.text = "@"+user.username
            binding.firstName.text = user.firstname

            // Log the image URL we're trying to load
            Log.d("UserAdapter", "Loading image for user ${user.username}: ${user.profileImageUrl}")

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