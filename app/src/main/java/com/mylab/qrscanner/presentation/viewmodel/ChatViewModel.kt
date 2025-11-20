package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.model.ChatMessage
import com.mylab.qrscanner.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        observeMessages()
    }
    
    private fun observeMessages() {
        viewModelScope.launch {
            chatRepository.observeMessages().collect { messageList ->
                _messages.value = messageList
                _isLoading.value = false
            }
        }
    }
    
    fun sendMessage(sender: String, senderName: String, message: String) {
        if (message.isNotBlank()) {
            val chatMessage = ChatMessage(
                sender = sender,
                senderName = senderName,
                message = message.trim(),
                timestamp = System.currentTimeMillis()
            )
            chatRepository.sendMessage(chatMessage)
        }
    }
}

