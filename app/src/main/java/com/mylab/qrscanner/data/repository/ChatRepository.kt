package com.mylab.qrscanner.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mylab.qrscanner.data.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val chatRef: DatabaseReference = database.getReference("chat/room_1/messages")
    
    fun sendMessage(message: ChatMessage) {
        val messageRef = chatRef.push()
        val messageId = messageRef.key ?: ""
        messageRef.setValue(
            hashMapOf(
                "id" to messageId,
                "sender" to message.sender,
                "senderName" to message.senderName,
                "message" to message.message,
                "timestamp" to message.timestamp,
                "roomId" to message.roomId
            )
        )
    }
    
    fun observeMessages(): Flow<List<ChatMessage>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { child ->
                    val map = child.value as? Map<*, *>
                    map?.let {
                        ChatMessage(
                            id = it["id"] as? String ?: child.key ?: "",
                            sender = it["sender"] as? String ?: "",
                            senderName = it["senderName"] as? String ?: "",
                            message = it["message"] as? String ?: "",
                            timestamp = (it["timestamp"] as? Long) ?: 0L,
                            roomId = it["roomId"] as? String ?: "room_1"
                        )
                    }
                }.sortedBy { it.timestamp }
                
                trySend(messages)
            }
            
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        
        chatRef.addValueEventListener(listener)
        
        awaitClose {
            chatRef.removeEventListener(listener)
        }
    }
}

