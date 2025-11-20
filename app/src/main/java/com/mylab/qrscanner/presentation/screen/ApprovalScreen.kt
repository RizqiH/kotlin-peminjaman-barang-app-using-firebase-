package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.data.model.Borrowing
import com.mylab.qrscanner.presentation.viewmodel.BorrowingViewModel
import com.mylab.qrscanner.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ApprovalScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReturnVerification: (String, String) -> Unit,
    onNavigateToScanner: () -> Unit,
    viewModel: BorrowingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { 2 }, initialPage = 0) // 0: Peminjaman, 1: Pengembalian
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.loadBorrowings() // Load all borrowings for approval
    }
    
    // Sync pager state dengan selectedTab
    LaunchedEffect(pagerState.currentPage) {
        // Pager state sudah ter-update
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
                        text = "Approval",
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
            
            // Tabs
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = BlackCard,
                contentColor = White
            ) {
                Tab(
                    selected = pagerState.currentPage == 0,
                    onClick = { 
                        scope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    text = { Text("Peminjaman") }
                )
                Tab(
                    selected = pagerState.currentPage == 1,
                    onClick = { 
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    text = { Text("Pengembalian") }
                )
            }
            
            // Horizontal Pager Content
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
                    val borrowings = currentState.borrowings
                    
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val filteredBorrowings = if (page == 0) {
                            // Peminjaman: pending approval
                            borrowings.filter { it.approvalStatus == "pending" && it.status == "dipinjam" }
                        } else {
                            // Pengembalian: pending return approval
                            borrowings.filter { 
                                it.returnApprovalStatus == "pending" && 
                                it.status == "dipinjam" && 
                                it.approvalStatus == "approved"
                            }
                        }
                        
                        if (filteredBorrowings.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (page == 0) "Tidak ada permintaan peminjaman" else "Tidak ada permintaan pengembalian",
                                    color = GrayLight
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredBorrowings) { borrowing ->
                                    ApprovalItemCard(
                                        borrowing = borrowing,
                                        isReturnApproval = page == 1,
                                        onApprove = {
                                            if (page == 0) {
                                                viewModel.approveBorrowing(borrowing.id)
                                            } else {
                                                // For return, navigate ke return verification screen dengan parameter
                                                // Pastikan itemId tidak kosong
                                                if (borrowing.itemId.isNotEmpty()) {
                                                    // Navigate ke return verification screen dengan borrowingId dan expectedItemId
                                                    onNavigateToReturnVerification(borrowing.id, borrowing.itemId)
                                                } else {
                                                    // Error jika itemId kosong
                                                    // TODO: Show error message
                                                }
                                            }
                                        },
                                        onReject = {
                                            if (page == 0) {
                                                viewModel.rejectBorrowing(borrowing.id)
                                            } else {
                                                viewModel.rejectReturn(borrowing.id)
                                            }
                                        }
                                    )
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

@Composable
fun ApprovalItemCard(
    borrowing: Borrowing,
    isReturnApproval: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
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
                        text = "Oleh: ${borrowing.userName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayLight
                    )
                    Text(
                        text = "Tanggal: ${borrowing.tglPinjam}",
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayLight
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isReturnApproval) OrangeMain.copy(alpha = 0.2f) else BlueInfo.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = if (isReturnApproval) "Pengembalian" else "Peminjaman",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = if (isReturnApproval) OrangeMain else BlueInfo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedError,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tolak")
                }
                
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenSuccess,
                        contentColor = White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isReturnApproval) "Verifikasi" else "Setujui")
                }
            }
        }
    }
}

