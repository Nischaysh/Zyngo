package com.example.vibin.Adapter



import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.R
import com.example.vibin.models.Post
import com.google.firebase.firestore.FirebaseFirestore

class PostAdapter(private val postList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
    private lateinit var db: FirebaseFirestore

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUserName: TextView = view.findViewById(R.id.tvUserName)
        val tvPostText: TextView = view.findViewById(R.id.tvPostText)
        val ivPostImage: ImageView = view.findViewById(R.id.ivPostImage)
        val userProfileImage: ImageView = view.findViewById(R.id.userProfileImage)
        val ivLike: ImageView = view.findViewById(R.id.ivLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        db = FirebaseFirestore.getInstance()
        holder.tvUserName.text = post.userName
        holder.tvPostText.text = post.text

        if (!post.imageUrl.isNullOrEmpty()) {
            holder.ivPostImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.ivPostImage)
        } else {
            holder.ivPostImage.visibility = View.GONE
        }

        db.collection("users")
            .document(post.userId)
            .get()
            .addOnSuccessListener { document ->
                val profileImageUrl = document.getString("profileImageUrl")
                if (!profileImageUrl.isNullOrEmpty()) {
                    Glide.with(holder.itemView.context)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.ic_default_user)
                        .circleCrop()
                        .into(holder.userProfileImage)
                } else {
                    holder.userProfileImage.setImageResource(R.drawable.ic_default_user)
                }
            }
            .addOnFailureListener {
                holder.userProfileImage.setImageResource(R.drawable.ic_default_user)
            }

        var isLiked = false

        holder.ivLike.setOnClickListener {
            isLiked = !isLiked
            holder.ivLike.setImageResource(
                if (isLiked) R.drawable.heart_liked else R.drawable.heart
            )
        }
    }

    override fun getItemCount(): Int = postList.size
}
