data class ChatMessage(
    val messageId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val status: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
