package com.example.vibin.Adapter

import Post
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.BottomSheet.CommentBottomSheet
import com.example.vibin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvPostText: TextView = view.findViewById(R.id.tvPostText)
        val ivPostImage: ImageView = view.findViewById(R.id.ivPostImage)
        val userProfileImage: ImageView = view.findViewById(R.id.userProfileImage)
        val ivLike: ImageView = view.findViewById(R.id.ivLike)
        val ivComment: ImageView = view.findViewById(R.id.ivComment)
        val ivMenu: ImageView = view.findViewById(R.id.ivMenu)
        val tvLikeCount: TextView = view.findViewById(R.id.tvLikeCount)
        val tvCommentCount: TextView = view.findViewById(R.id.tvCommentCount)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        holder.tvUserName.text = post.userName
        holder.tvPostText.text = post.text
        holder.tvLikeCount.text = "${post.likes}"

        db.collection("posts").document(post.postId)
            .collection("comments")
            .get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.size() == 0 ){
                    val count = ""
                    holder.tvCommentCount.text = "$count"
                }
                else{
                    val count  = snapshot.size()
                    holder.tvCommentCount.text = "$count"
                }


            }
            .addOnFailureListener {
                holder.tvCommentCount.text = ""
            }


        holder.ivComment.setOnClickListener {
            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            val commentSheet = CommentBottomSheet(post.postId)
            commentSheet.show(fragmentManager, commentSheet.tag)
        }
        holder.ivMenu.setOnClickListener {
            if(currentUserId == post.userId){
                showCustomDialog(holder.itemView.context, " Want to delete this post?") {
                    val  postId = post.postId

                    db.collection("posts").document(postId)
                        .delete()
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Deleted", Toast.LENGTH_SHORT).show()
                            postList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, postList.size)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(holder.itemView.context, "Some error", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            else {
                Toast.makeText(holder.itemView.context, "Cannot edit someone post", Toast.LENGTH_SHORT).show()
            }

        }


        // Set post image
        if (!post.imageUrl.isNullOrEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(post.imageUrl).into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }

        // Load profile image
        db.collection("users").document(post.userId).get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl")
                Glide.with(holder.itemView.context)
                    .load(profileImageUrl ?: R.drawable.ic_default_user)
                    .placeholder(R.drawable.ic_default_user)
                    .circleCrop()
                    .into(holder.userProfileImage)
            }
            .addOnFailureListener {
                holder.userProfileImage.setImageResource(R.drawable.ic_default_user)
            }

        // Set heart icon
        val isLiked = post.likedBy.contains(currentUserId)
        holder.ivLike.setImageResource(if (isLiked) R.drawable.heart_liked else R.drawable.heart)

        // Like button click
        holder.ivLike.setOnClickListener {
            val postRef = db.collection("posts").document(post.postId)
            holder.ivLike.setImageResource(R.drawable.heart_liked)

            db.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likes") ?: 0
                val likedByList = snapshot.get("likedBy") as? MutableList<String> ?: mutableListOf()

                if (likedByList.contains(currentUserId)) {
                    likedByList.remove(currentUserId)
                    transaction.update(postRef, "likes", currentLikes - 1)
                } else {
                    likedByList.add(currentUserId)
                    transaction.update(postRef, "likes", currentLikes + 1)
                }
                transaction.update(postRef, "likedBy", likedByList)
            }.addOnSuccessListener {
                // Update local list and refresh UI
                val updatedPost = postList[position]
                val userLiked = updatedPost.likedBy.contains(currentUserId)
                if (userLiked) {
                    updatedPost.likedBy = updatedPost.likedBy - currentUserId
                    updatedPost.likes -= 1
                } else {
                    updatedPost.likedBy = updatedPost.likedBy + currentUserId
                    updatedPost.likes += 1
                }
                notifyItemChanged(position)
            }
        }

        // Timestamp
        post.timestamp?.let {
            holder.tvTimestamp.text = getTimeAgo(it)
        } ?: run {
            holder.tvTimestamp.text = ""
        }
    }

    fun showCustomDialog(context: Context, message: String, onYes: () -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val messageText = dialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnYes = dialogView.findViewById<Button>(R.id.ButtonYes)
        val btnNo = dialogView.findViewById<Button>(R.id.ButtonNo)

        messageText.text = message

        btnYes.setOnClickListener {
            onYes()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    override fun getItemCount(): Int = postList.size



    private fun getTimeAgo(timestamp: com.google.firebase.Timestamp): String {
        val postTime = timestamp.toDate().time
        val now = System.currentTimeMillis()
        val diff = now - postTime

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "$minutes minute${if (minutes == 1L) "" else "s"} ago"
            hours < 24 -> "$hours hour${if (hours == 1L) "" else "s"} ago"
            days < 7 -> "$days day${if (days == 1L) "" else "s"} ago"
            else -> {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                sdf.format(postTime)
            }
        }
    }
}
