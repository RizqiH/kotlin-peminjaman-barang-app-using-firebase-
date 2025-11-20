package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.presentation.viewmodel.ChatViewModel
import com.mylab.qrscanner.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var messageText by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsState()
    val listState = rememberLazyListState()
    
    // TODO: Get current user from AuthViewModel
    val currentUserId = "current_user_id" // Placeholder
    val currentUserName = "Current User" // Placeholder
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            TopAppBar(
                title = { Text("Chat Accounting", color = White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlackCard
                )
            )
            
            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val isCurrentUser = message.sender == currentUserId
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            modifier = Modifier.widthIn(max = 280.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentUser) OrangeMain else BlackCard
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                if (!isCurrentUser) {
                                    Text(
                                        text = message.senderName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrangeMain,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Text(
                                    text = message.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isCurrentUser) Black else White
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        .format(Date(message.timestamp)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCurrentUser) Black.copy(alpha = 0.6f) else GrayLight
                                )
                            }
                        }
                    }
                }
            }
            
            // Message Input
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = BlackCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tulis pesan...", color = GrayLight) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            focusedBorderColor = OrangeMain,
                            unfocusedBorderColor = GrayDark,
                            unfocusedPlaceholderColor = GrayLight
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                    
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(currentUserId, currentUserName, messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = OrangeMain,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Black
                        )
                    }
                }
            }
        }
    }
}

