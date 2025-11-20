package com.mylab.qrscanner.data.model

data class ChatMessage(
    val id: String = "",
    val sender: String = "", // userId
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val roomId: String = "room_1" // Default room for accounting chat
)

