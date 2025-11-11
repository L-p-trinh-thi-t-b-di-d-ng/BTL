package com.dex.lingbook.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val currentLevel: String = ""
)