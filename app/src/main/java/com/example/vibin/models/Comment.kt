data class Comment(
    val userId: String = "",
    val userName: String = "",
    val commentText: String = "",
    val timestamp: com.google.firebase.Timestamp? = null
)
