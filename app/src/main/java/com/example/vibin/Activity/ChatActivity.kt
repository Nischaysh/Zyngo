package com.example.vibin.Activity

import ChatAdapter
import ChatMessage
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
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
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class ChatActivity : AppCompatActivity() {

    private val messages = mutableListOf<ChatMessage>()
    private lateinit var adapter: ChatAdapter
    private lateinit var db: FirebaseFirestore
    private var messageListener: ListenerRegistration? = null

    private val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var otherUserId: String

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
        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val sendMessage = findViewById<ImageView>(R.id.sendMessage)
        val editTextText2 = findViewById<EditText>(R.id.editTextText2)

        otherUserId = intent.getStringExtra("otherUserId") ?: ""
        loadUserData(otherUserId)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = false
        chatRecyclerView.layoutManager = layoutManager
        adapter = ChatAdapter(messages, currentUserId)
        chatRecyclerView.adapter = adapter

        sendMessage.setOnClickListener {
            sendMessage(editTextText2.text.toString())
        }

        findViewById<ShapeableImageView>(R.id.profileImage).setOnClickListener {
            startActivity(Intent(this, UserProfileActivity::class.java).apply {
                putExtra("userId", otherUserId)
            })
        }
         val back  = findViewById<ImageButton>(R.id.menuButton)
        back.setOnClickListener {
            finish()
        }

    }

    private fun loadUserData(otherUserId: String) {
        val profileImage = findViewById<ShapeableImageView>(R.id.profileImage)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvPresence = findViewById<TextView>(R.id.tvPresence)

        db.collection("users")
            .document(otherUserId)
            .addSnapshotListener { document, error ->
                if (error != null || document == null || !document.exists()) return@addSnapshotListener
                if (isFinishing || isDestroyed) return@addSnapshotListener

                val firstName = document.getString("firstName") ?: ""
                val profileImageUrl = document.getString("profileImageUrl") ?: ""
                val status = document.getString("status") ?: ""
                val lastSeen = document.getLong("lastSeen")

                try {
                    Glide.with(this)
                        .load(profileImageUrl)
                        .circleCrop()
                        .into(profileImage)
                } catch (_: IllegalArgumentException) {}

                tvUsername.text = firstName
                if (status == "online") {
                    tvPresence.text = "Active now"
                    tvPresence.setTextColor(ContextCompat.getColor(this, R.color.green))
                } else {
                    tvPresence.text = lastSeen?.let { getShortTimeAgo(it) } ?: "Offline"
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
            minutes < 60 -> "$minutes min ago"
            hours < 24 -> "$hours hours ago"
            days < 7 -> "$days day ago"
            else -> {
                val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
                sdf.format(java.util.Date(time))
            }
        }
    }

    private fun sendMessage(text: String) {
        val input = findViewById<EditText>(R.id.editTextText2)
        if (text.isEmpty()) return

        val chatId = if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"
        val messageId = db.collection("chats").document(chatId).collection("messages").document().id

        val message = ChatMessage(
            messageId = messageId,
            senderId = currentUserId,
            receiverId = otherUserId,
            message = text,
            status = "sent"
        )

        db.collection("chats").document(chatId)
            .collection("messages")
            .document(messageId)
            .set(message)

        val recentChat = mapOf(
            "userId" to otherUserId,
            "lastMessage" to text,
            "timestamp" to message.timestamp
        )
        val reverseRecentChat = mapOf(
            "userId" to currentUserId,
            "lastMessage" to text,
            "timestamp" to message.timestamp
        )

        db.collection("users").document(currentUserId)
            .collection("recentChats")
            .document(otherUserId)
            .set(recentChat)

        db.collection("users").document(otherUserId)
            .collection("recentChats")
            .document(currentUserId)
            .set(reverseRecentChat)


        input.setText("")
    }

    private fun startListeningForMessages() {
        val chatRecyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        val chatId = if (currentUserId < otherUserId) "$currentUserId-$otherUserId" else "$otherUserId-$currentUserId"

        messageListener = db.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                messages.clear()
                for (doc in snapshots.documents) {
                    val msg = doc.toObject(ChatMessage::class.java) ?: continue
                    messages.add(msg)

                    // Mark as seen only if sent by other user
                    if (msg.senderId != currentUserId && msg.status != "seen") {
                        db.collection("chats").document(chatId)
                            .collection("messages")
                            .document(msg.messageId)
                            .update("status", "seen")
                    }
                }

                adapter.notifyDataSetChanged()
                chatRecyclerView.scrollToPosition(messages.size - 1)
            }
    }

    override fun onStart() {
        super.onStart()
        startListeningForMessages()
    }

    override fun onStop() {
        super.onStop()
        messageListener?.remove()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
