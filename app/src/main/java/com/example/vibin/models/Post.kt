package com.example.vibin.models


import com.google.firebase.Timestamp

data class Post(
    val userId: String = "",
    val userName: String = "",
    val text: String = "",
    val imageUrl: String? = null,
    val timestamp: Timestamp = Timestamp.now()
)
