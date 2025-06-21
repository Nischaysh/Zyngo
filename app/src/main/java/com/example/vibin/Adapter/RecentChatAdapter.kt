package com.example.vibin.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.databinding.ItemRecentChatBinding
import com.google.firebase.firestore.FirebaseFirestore
import ChatTile
import android.content.Intent
import android.text.format.DateUtils
import android.view.View
import com.example.vibin.Activity.ChatActivity

class RecentChatAdapter(
    private var chatList: List<ChatTile>
) : RecyclerView.Adapter<RecentChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(val binding: ItemRecentChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemRecentChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chatList[position]
        val context = holder.itemView.context

        FirebaseFirestore.getInstance().collection("users")
            .document(chat.otherUserId)
            .get()
            .addOnSuccessListener { userDoc ->
                val username = userDoc.getString("firstName") ?: "User"
                val profileImage = userDoc.getString("profileImageUrl")

                holder.binding.usernameTextView.text = username
                holder.binding.lastMessageTextView.text = chat.lastMessage

                Glide.with(context)
                    .load(profileImage)
                    .circleCrop()
                    .into(holder.binding.profileImageView)

                // Format time
                val relativeTime = DateUtils.getRelativeTimeSpanString(
                    chat.timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                holder.binding.timeTextView.text = relativeTime
                holder.binding.timeTextView.visibility = View.VISIBLE
                holder.itemView.setOnClickListener {
                    val intent = Intent(holder.itemView.context, ChatActivity::class.java)
                    intent.putExtra("otherUserId", chat.otherUserId)
                    holder.itemView.context.startActivity(intent)
                }
            }
    }

    override fun getItemCount(): Int = chatList.size

    fun updateData(newList: List<ChatTile>) {
        chatList = newList
        notifyDataSetChanged()
    }
}
