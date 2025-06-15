package com.example.vibin.BottomSheet

import Comment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vibin.Adapter.CommentAdapter
import com.example.vibin.R
import com.example.vibin.databinding.CommentBottomSheetBinding
import com.example.vibin.databinding.NotificationBottomSheetBinding
import com.example.vibin.databinding.UpdateProfileBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class CommentBottomSheet(private val postId: String) : BottomSheetDialogFragment() {

    private lateinit var binding: CommentBottomSheetBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val comments = mutableListOf<Comment>()
    private lateinit var commentAdapter: CommentAdapter
    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CommentBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)

            // Set full height of dialog
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            // 40% of screen height
            val screenHeight = resources.displayMetrics.heightPixels
            behavior.peekHeight = (screenHeight * 0.5).toInt()

            // Allow dragging to full
            behavior.isFitToContents = false
            behavior.expandedOffset = 0
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        commentAdapter = CommentAdapter(comments)
        binding.recyclerComments.layoutManager = LinearLayoutManager(context)
        binding.recyclerComments.adapter = commentAdapter

        loadComments()

        binding.btnSendComment.setOnClickListener {
            val commentText = binding.etComment.text.toString().trim()
            if (commentText.isNotEmpty()) {
                addComment(commentText)
            }
        }
    }

    private fun loadComments() {
        db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                comments.clear()
                for (doc in snapshots!!) {
                    val comment = doc.toObject(Comment::class.java)
                    comments.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }
    }

    private fun addComment(commentText: String) {
        val currentUser = auth.currentUser ?: return
        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { userDoc ->
            val userName = userDoc.getString("username") ?: "User"
            val comment = Comment(
                userId = currentUser.uid,
                userName = userName,
                commentText = commentText,
                timestamp = Timestamp.now()
            )
            db.collection("posts").document(postId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener {
                    binding.etComment.text?.clear()
                }
        }
    }
}
