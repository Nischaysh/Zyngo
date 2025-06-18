package com.example.vibin.models

data class User(
    val uid: String = "",
    val firstname: String = "",
    val username: String = "",
    val status: String = "",
    var email: String? = null,
    val lastSeen: Long? = null,
    val profileImageUrl: String = ""
)