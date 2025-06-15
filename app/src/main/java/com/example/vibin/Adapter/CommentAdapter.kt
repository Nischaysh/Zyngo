package com.example.vibin.Adapter

import Comment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.R
import com.example.vibin.databinding.ItemCommentBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.binding.tvCommentUser.text = comment.userName
        holder.binding.tvCommentText.text = comment.commentText

        // Format and display timestamp
        comment.timestamp?.let {
            holder.binding.tvTimeStamp.text = getTimeAgo(it.toDate())
        } ?: run {
            holder.binding.tvTimeStamp.text = ""
        }

        // Load user profile image from Firestore
        FirebaseFirestore.getInstance().collection("users")
            .document(comment.userId)
            .get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl")
                Glide.with(holder.itemView.context)
                    .load(profileImageUrl ?: R.drawable.ic_default_user)
                    .placeholder(R.drawable.ic_default_user)
                    .circleCrop()
                    .into(holder.binding.userProfileImage)
            }
            .addOnFailureListener {
                holder.binding.userProfileImage.setImageResource(R.drawable.ic_default_user)
            }
    }

    override fun getItemCount(): Int = comments.size

    private fun getTimeAgo(time: Date): String {
        val now = System.currentTimeMillis()
        val diff = now - time.time

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hr ago"
            days < 7 -> "$days d ago"
            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(time)
        }
    }
}
