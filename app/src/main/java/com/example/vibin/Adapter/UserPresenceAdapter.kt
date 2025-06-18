package com.example.vibin.Adapter

import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.databinding.ItemPresenceBinding
import com.example.vibin.models.User
import com.google.firebase.firestore.FirebaseFirestore

class UserPresenceAdapter(private val users: List<User>) :
    RecyclerView.Adapter<UserPresenceAdapter.UserViewHolder>() {

    class UserViewHolder(val binding: ItemPresenceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemPresenceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        val context = holder.itemView.context

        // Load profile image
        Glide.with(context)
            .load(user.profileImageUrl)
            .circleCrop()
            .into(holder.binding.ivUserProfile)

                val status = user.status
                if (status ==  "online"){
                    holder.binding.greendot.visibility = View.VISIBLE
                }else{
                        holder.binding.greendot.visibility = View.GONE
                }

    }

    fun getShortTimeAgo(time: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            days < 7 -> "${days}d"
            else -> {
                val date = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                date.format(java.util.Date(time))
            }
        }
    }


    override fun getItemCount(): Int = users.size
}
