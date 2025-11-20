package com.mylab.qrscanner.presentation.screen

import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.mylab.qrscanner.presentation.viewmodel.BorrowingViewModel
import com.mylab.qrscanner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowingHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: BorrowingViewModel = hiltViewModel(),
    authViewModel: com.mylab.qrscanner.presentation.viewmodel.AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()
    
    val currentUser = when (val state = authState) {
        is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success -> state.user
        else -> null
    }
    
    LaunchedEffect(currentUser?.uid) {
        // Load borrowings berdasarkan userId untuk mahasiswa
        currentUser?.uid?.let { userId ->
            viewModel.loadBorrowings(userId)
        } ?: run {
            // Jika tidak ada userId (petugas), load semua
            viewModel.loadBorrowings()
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
                title = { Text("Riwayat Peminjaman", color = White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = White)
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
                    // Tampilkan semua status: pending, dipinjam, dikembalikan
                    val borrowings = currentState.borrowings.sortedByDescending { 
                        it.createdAt ?: ""
                    }
                    
                    if (borrowings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada riwayat peminjaman", color = GrayLight)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(borrowings) { borrowing ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = BlackCard
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
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
                                                    text = "Oleh: ${borrowing.userName}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = GrayLight
                                                )
                                            }
                                            // Status badge dengan warna berbeda
                                            val (statusColor, statusIcon) = when {
                                                borrowing.status == "dikembalikan" -> 
                                                    GreenSuccess to Icons.Default.CheckCircle
                                                borrowing.approvalStatus == "pending" -> 
                                                    Yellow to Icons.Default.Schedule
                                                borrowing.approvalStatus == "rejected" -> 
                                                    RedError to Icons.Default.Cancel
                                                else -> 
                                                    OrangeMain to Icons.Default.Inventory
                                            }
                                            
                                            Icon(
                                                imageVector = statusIcon,
                                                contentDescription = borrowing.status,
                                                tint = statusColor,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(
                                                    text = "Pinjam",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = GrayLight
                                                )
                                                Text(
                                                    text = borrowing.tglPinjam,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = White
                                                )
                                            }
                                            if (borrowing.tglKembali.isNotEmpty()) {
                                                Column {
                                                    Text(
                                                        text = "Kembali",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = GrayLight
                                                    )
                                                    Text(
                                                        text = borrowing.tglKembali,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = White
                                                    )
                                                }
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        // Status badge dengan semua status
                                        val (badgeColor, badgeText) = when {
                                            borrowing.status == "dikembalikan" -> 
                                                GreenSuccess to "DIKEMBALIKAN"
                                            borrowing.approvalStatus == "pending" -> 
                                                Yellow to "PENDING"
                                            borrowing.approvalStatus == "rejected" -> 
                                                RedError to "DITOLAK"
                                            borrowing.status == "dipinjam" && borrowing.approvalStatus == "approved" -> 
                                                OrangeMain to "DIPINJAM"
                                            else -> 
                                                GrayMedium to borrowing.status.uppercase()
                                        }
                                        
                                        Surface(
                                            color = badgeColor.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = badgeText,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                color = badgeColor,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        
                                        // Return button dihapus - return sekarang dilakukan di ReturnScreen
                                    }
                                }
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

