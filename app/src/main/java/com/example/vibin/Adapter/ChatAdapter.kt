import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.vibin.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT = 1
    private val TYPE_RECEIVED = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].senderId == currentUserId) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chat_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is SentMessageViewHolder) {
            holder.bind(message)
        } else if (holder is ReceivedMessageViewHolder) {
            holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("ResourceAsColor")
        fun bind(message: ChatMessage) {
            val messageText = itemView.findViewById<TextView>(R.id.tvtextMessage)
            val timeText = itemView.findViewById<TextView>(R.id.tvtextTime)
            val statusView = itemView.findViewById<ImageView>(R.id.status)
            val messageContainer = itemView.findViewById<LinearLayout>(R.id.messageContainer)
            val color  = ContextCompat.getColor(messageText.context, R.color.white)

            timeText.text = formatTimestamp(message.timestamp)

            if (message.message == "#deleted") {
                messageText.setTextColor(R.color.blue)
                messageText.text = "This message was deleted!!"
                messageText.setTypeface(null, Typeface.ITALIC)
            }else{
                messageText.text = message.message
                messageText.setTextColor(color)
                messageText.setTypeface(null, Typeface.NORMAL)
            }

            // Change status color
            if (message.status == "seen") {
                statusView.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.blue),
                    PorterDuff.Mode.SRC_IN
                )
            } else {
                statusView.setColorFilter(
                    ContextCompat.getColor(itemView.context, R.color.white),
                    PorterDuff.Mode.SRC_IN
                )
            }

            // Delete logic
            messageContainer.setOnLongClickListener {
                if (message.senderId == currentUserId && message.message != "#deleted") {
                    showCustomDialog(itemView.context, "Want to delete this message?") {
                        deleteMessageText(message.messageId, message.receiverId)
                    }
                }
                true
            }
        }
    }

    inner class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(message: ChatMessage) {
            val messageText = itemView.findViewById<TextView>(R.id.tvtextMessage)
            val timeText = itemView.findViewById<TextView>(R.id.tvtextTime)
            timeText.text = formatTimestamp(message.timestamp)
            val color  = ContextCompat.getColor(messageText.context, R.color.text_primary)
            if (message.message == "#deleted") {
                messageText.setTextColor(Color.GRAY)
                messageText.text = "This message was deleted!!"
                messageText.setTypeface(null, Typeface.ITALIC)
            }else{
                messageText.text = message.message
                messageText.setTextColor(color)
                messageText.setTypeface(null, Typeface.NORMAL)
            }
        }
    }

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }

    private fun deleteMessageText(messageId: String, receiverId: String) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val chatId = if (currentUserId < receiverId)
            "$currentUserId-$receiverId"
        else
            "$receiverId-$currentUserId"

        val updates = mapOf(
            "message" to "#deleted"
        )

        FirebaseFirestore.getInstance()
            .collection("chats")
            .document(chatId)
            .collection("messages")
            .document(messageId)
            .update(updates)
    }

    private fun showCustomDialog(context: Context, message: String, onYes: () -> Unit) {
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
}
