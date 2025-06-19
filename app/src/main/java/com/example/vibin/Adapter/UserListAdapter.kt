package com.example.vibin.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.Activity.ChatActivity
import com.example.vibin.databinding.ItemUserBinding
import com.example.vibin.databinding.ItemUserFollowingBinding
import com.example.vibin.models.User
import kotlin.jvm.java

class UserListAdapter(private val context: Context,private val users: List<User>) :
    RecyclerView.Adapter<UserListAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemUserFollowingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserFollowingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.binding.tvUserName.text = user.username
        Log.d("UserPresenceAdapter", "User object: $user ${user.uid}")
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("otherUserId", user.uid)
            holder.itemView.context.startActivity(intent)
        }
        Glide.with(holder.itemView.context)
            .load(user.profileImageUrl)
            .circleCrop()
            .into(holder.binding.ivUserProfile)
    }

    override fun getItemCount(): Int = users.size
}
