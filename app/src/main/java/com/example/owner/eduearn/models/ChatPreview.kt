package com.example.owner.eduearn.models
data class ChatPreview(
    val chatId: String,
    val otherUid: String,
    val otherName: String,
    val lastMessage: String,
    val lastTimestamp: Long
)