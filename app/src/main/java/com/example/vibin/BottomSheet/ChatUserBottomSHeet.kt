package com.example.vibin.BottomSheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vibin.Adapter.UserListAdapter
import com.example.vibin.R
import com.example.vibin.databinding.ChatUserBottomSheetBinding
import com.example.vibin.databinding.NotificationBottomSheetBinding
import com.example.vibin.databinding.UpdateProfileBottomSheetBinding
import com.example.vibin.models.User
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class ChatUserBottomSHeet : BottomSheetDialogFragment() {

    private var _binding: ChatUserBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserListAdapter
    private val userList = mutableListOf<User>()
    override fun getTheme(): Int = R.style.TransparentBottomSheetDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

    }
    override fun onStart() {
        super.onStart()
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)

            // Set full height of dialog
            it.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT

            // 45% of screen height
            val screenHeight = resources.displayMetrics.heightPixels
            behavior.peekHeight = (screenHeight * 0.45).toInt()

            // Allow dragging to full
            behavior.isFitToContents = false
            behavior.expandedOffset = 0
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = UserListAdapter(userList)
        binding.recyclerFollowing.layoutManager = LinearLayoutManager(context)
        binding.recyclerFollowing.adapter = adapter

        loadFollowingUsers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ChatUserBottomSheetBinding.inflate(inflater, container, false)

        return binding.root
    }

    private fun loadFollowingUsers() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .collection("following")
            .get()
            .addOnSuccessListener { followingDocs ->
                val followedUserIds = followingDocs.map { it.id }

                if (followedUserIds.isEmpty()) return@addOnSuccessListener

                // Fetch all followed user details
                db.collection("users")
                    .whereIn(FieldPath.documentId(), followedUserIds)
                    .get()
                    .addOnSuccessListener { userDocs ->
                        userList.clear()
                        for (doc in userDocs) {
                            val user = doc.toObject(User::class.java)
                            userList.add(user)
                        }
                        adapter.notifyDataSetChanged()
                    }
            }
    }

}