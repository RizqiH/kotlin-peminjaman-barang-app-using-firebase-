package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.data.model.Borrowing
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.presentation.viewmodel.BorrowingViewModel
import com.mylab.qrscanner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReturnScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReturnVerification: (String, String) -> Unit,
    borrowingViewModel: BorrowingViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state by borrowingViewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()
    
    val currentUser = when (val state = authState) {
        is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success -> state.user
        else -> null
    }
    
    // Load borrowings saat user ID tersedia atau saat screen di-compose
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            borrowingViewModel.loadBorrowings(userId)
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
                title = {
                    Text(
                        text = "Pengembalian",
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlackCard
                )
            )
            
            // Content
            when (val currentState = state) {
                is com.mylab.qrscanner.presentation.viewmodel.BorrowingState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangeMain)
                    }
                }
                is com.mylab.qrscanner.presentation.viewmodel.BorrowingState.Success -> {
                    // Filter: hanya yang sudah approved dan masih dipinjam
                    val activeBorrowings = currentState.borrowings.filter { borrowing ->
                        val isApproved = borrowing.approvalStatus == "approved"
                        val isDipinjam = borrowing.status == "dipinjam" || borrowing.status.isEmpty()
                        val returnStatus = borrowing.returnApprovalStatus ?: ""
                        val canReturn = returnStatus.isEmpty() || returnStatus == "rejected"
                        isApproved && isDipinjam && canReturn
                    }
                    
                    if (activeBorrowings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = GrayMedium
                                )
                                Text(
                                    text = "Tidak ada barang yang dipinjam",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GrayLight
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(activeBorrowings) { borrowing ->
                                ReturnItemCard(
                                    borrowing = borrowing,
                                    onReturn = {},
                                    onNavigateToVerification = {
                                        onNavigateToReturnVerification(borrowing.id, borrowing.itemId)
                                    }
                                )
                            }
                        }
                    }
                }
                is com.mylab.qrscanner.presentation.viewmodel.BorrowingState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentState.message,
                            color = RedError
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ReturnItemCard(
    borrowing: Borrowing,
    onReturn: () -> Unit,
    onNavigateToVerification: () -> Unit,
    borrowingViewModel: BorrowingViewModel = hiltViewModel()
) {
    val actionState by borrowingViewModel.actionState.collectAsState()
    
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Success -> {
                borrowingViewModel.resetActionState()
            }
            else -> {}
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlackCard
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = borrowing.itemName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    Text(
                        text = "Kode: ${borrowing.itemCode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayLight
                    )
                    Text(
                        text = "Tanggal Pinjam: ${borrowing.tglPinjam}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayLight
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GreenSuccess.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Dipinjam",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = GreenSuccess,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Button(
                onClick = {
                    // Navigate ke scanner untuk scan QR code
                    onNavigateToVerification()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeMain,
                    contentColor = Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan QR untuk Kembalikan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "Scan QR code barang untuk memulai proses pengembalian",
                style = MaterialTheme.typography.bodySmall,
                color = GrayLight
            )
        }
    }
}

