package com.example.vibin.Activity

import ChatAdapter
import ChatMessage
import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.vibin.R
import com.example.vibin.databinding.ActivityChatBinding
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions

class ChatActivity : AppCompatActivity() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var db: FirebaseFirestore

    private val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var otherUserId: String // You must pass this via intent

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = FirebaseFirestore.getInstance()
        val chatRecyclerView: RecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val sendMessage: ImageView = findViewById<ImageView>(R.id.sendMessage)
        val editTextText2: EditText = findViewById<EditText>(R.id.editTextText2)

        // Get otherUserId from intent
        otherUserId = intent.getStringExtra("otherUserId") ?: ""
        loadUserData(otherUserId)

        adapter = ChatAdapter(messages, currentUserId)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = adapter

        sendMessage.setOnClickListener {
            sendMessage(editTextText2.text.toString())
        }

        val profileImage: ShapeableImageView = findViewById(R.id.profileImage)
        profileImage.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java).apply {
                putExtra("userId", otherUserId)
            }
            startActivity(intent)
        }
        listenForMessages()
    }

    private fun setUserPresence(status: String) {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val presenceRef = FirebaseFirestore.getInstance().collection("users").document(uid)

        val data = hashMapOf(
            "status" to status,
            "lastSeen" to System.currentTimeMillis()
        )

        presenceRef.set(data, SetOptions.merge())
    }
    override fun onStart() {
        super.onStart()
        setUserPresence("online")
    }

    override fun onStop() {
        super.onStop()
        setUserPresence("offline")
    }

    private fun loadUserData(otherUserId: String) {
        val profileImage: ShapeableImageView = findViewById(R.id.profileImage)
        val tvUsername: TextView = findViewById(R.id.tvUsername)
        val tvPresence: TextView = findViewById(R.id.tvPresence)

        db.collection("users")
            .document(otherUserId)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) return@addSnapshotListener

                // Prevent crash if activity is destroyed
                if (isFinishing || isDestroyed) return@addSnapshotListener

                val firstName = document.getString("firstName") ?: ""
                val profileImageUrl = document.getString("profileImageUrl") ?: ""
                val status = document.getString("status") ?: ""
                val lastSeen = document.getLong("lastSeen")

                try {
                    Glide.with(this@ChatActivity)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(profileImage)
                } catch (e: IllegalArgumentException) {
                    // Optional: Log or ignore
                }

                tvUsername.text = firstName

                if (status == "online") {
                    tvPresence.text = "Active now"
                    tvPresence.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    tvPresence.text = if (lastSeen != null) getShortTimeAgo(lastSeen) else "Offline"
                    tvPresence.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                }
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
            seconds < 60 -> "Offline just now"
            minutes < 60 -> "${minutes} min ago"
            hours < 24 -> "${hours} hours ago"
            days < 7 -> "${days} day ago"
            else -> {
                val date = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                date.format(java.util.Date(time))
            }
        }
    }


    private fun sendMessage(text: String) {
        val editTextText2: EditText = findViewById<EditText>(R.id.editTextText2)
        if (text.isEmpty()) return

        val messageId = db.collection("chats").document().id
        val chatId =
            if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"

        val message = ChatMessage(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = otherUserId,
            message = text
        )

        db.collection("chats").document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)

        editTextText2.setText("")
    }

    private fun listenForMessages() {
        val chatRecyclerView: RecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)

        val chatId =
            if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"

        db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) return@addSnapshotListener

                messages.clear()
                snapshots?.forEach { doc ->
                    val msg = doc.toObject(ChatMessage::class.java)
                    messages.add(msg)
                }
                adapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messages.size - 1)
            }
    }
}
