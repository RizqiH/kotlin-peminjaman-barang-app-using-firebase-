package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mylab.qrscanner.data.model.ScanHistory
import com.mylab.qrscanner.presentation.viewmodel.HistoryViewModel
import com.mylab.qrscanner.presentation.viewmodel.HistoryState
import com.mylab.qrscanner.ui.theme.Black
import com.mylab.qrscanner.ui.theme.BlackCard
import com.mylab.qrscanner.ui.theme.GrayLight
import com.mylab.qrscanner.ui.theme.GrayMedium
import com.mylab.qrscanner.ui.theme.OrangeMain
import com.mylab.qrscanner.ui.theme.RedError
import com.mylab.qrscanner.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onHistoryItemClick: (String) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<ScanHistory?>(null) }
    
    // Refresh data saat screen muncul
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }
    
    val historyList = when (val currentState = state) {
        is HistoryState.Success -> currentState.historyList
        else -> emptyList()
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
                title = {
                    Text(
                        text = "Scan History",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                actions = {
                    if (historyList.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                viewModel.clearHistory()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All",
                                tint = White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlackCard
                )
            )
            
            // Content
            when (val currentState = state) {
                is HistoryState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangeMain)
                    }
                }
                is HistoryState.Success -> {
                    if (currentState.historyList.isEmpty()) {
                        EmptyHistoryContent()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp, bottom = 80.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(currentState.historyList) { history ->
                                HistoryItem(
                                    history = history,
                                    onClick = { onHistoryItemClick(history.qrCode) },
                                    onDelete = {
                                        itemToDelete = history
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
                is HistoryState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = currentState.message,
                                color = RedError
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadHistory() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangeMain
                                )
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = "Delete History",
                    color = White
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this scan history?",
                    color = GrayLight
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        itemToDelete?.let {
                            viewModel.deleteHistoryItem(it.id)
                        }
                        showDeleteDialog = false
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = OrangeMain)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = White)
                }
            },
            containerColor = BlackCard
        )
    }
}

@Composable
fun HistoryItem(
    history: ScanHistory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlackCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(OrangeMain.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode2,
                    contentDescription = null,
                    tint = OrangeMain,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = history.itemName ?: "QR Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = history.qrCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = GrayMedium,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = history.getFormattedTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayMedium
                    )
                }
            }
            
            // Delete Button
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = GrayLight
                )
            }
        }
    }
}

@Composable
fun EmptyHistoryContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GrayMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Scan History",
            style = MaterialTheme.typography.headlineSmall,
            color = White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your QR code scan history will appear here",
            style = MaterialTheme.typography.bodyLarge,
            color = GrayLight
        )
    }
}

