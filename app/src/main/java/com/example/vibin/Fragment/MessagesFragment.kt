package com.example.vibin.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vibin.Adapter.UserListAdapter
import com.example.vibin.Adapter.UserPresenceAdapter
import com.example.vibin.BottomSheet.ChatUserBottomSHeet
import com.example.vibin.databinding.FragmentMessagesBinding
import com.example.vibin.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class MessagesFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: UserPresenceAdapter
    private val userList = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabNewChat.setOnClickListener {
            val bottomsheet = ChatUserBottomSHeet()
            bottomsheet.show(parentFragmentManager,bottomsheet.tag)
        }

        adapter = UserPresenceAdapter(userList)
        binding.userPresenceRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.userPresenceRecycler.adapter = adapter


        loadFollowingUsers()
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