import com.google.firebase.Timestamp

data class Post(
    val postId: String = "", // <-- Add this
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    var likes: Int = 0,
    var likedBy: List<String> = emptyList(),
    val imageUrl: String? = null,
    val timestamp: Timestamp? = null
)
