package com.mylab.qrscanner.presentation.screen

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.presentation.viewmodel.BorrowingViewModel
import com.mylab.qrscanner.presentation.viewmodel.ProductViewModel
import com.mylab.qrscanner.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BorrowingScreen(
    onNavigateBack: () -> Unit,
    borrowingViewModel: BorrowingViewModel = hiltViewModel(),
    productViewModel: ProductViewModel = hiltViewModel()
) {
    var selectedItem by remember { mutableStateOf<LabItem?>(null) }
    var showItemList by remember { mutableStateOf(false) }
    
    val productsState by productViewModel.state.collectAsState()
    val borrowingState by borrowingViewModel.actionState.collectAsState()
    
    LaunchedEffect(Unit) {
        productViewModel.loadProducts()
    }
    
    LaunchedEffect(borrowingState) {
        if (borrowingState is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Success) {
            selectedItem = null
            showItemList = false
            borrowingViewModel.resetActionState()
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
                title = { Text("Peminjaman Barang", color = White) },
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
            if (showItemList) {
                // Item Selection List
                when (val state = productsState) {
                    is com.mylab.qrscanner.presentation.viewmodel.ProductState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.items) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = BlackCard
                                    ),
                                    onClick = {
                                        selectedItem = item
                                        showItemList = false
                                    }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Text(
                                            text = item.itemName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = White
                                        )
                                        Text(
                                            text = "Kode: ${item.itemCode}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GrayLight
                                        )
                                        Text(
                                            text = "Stok: ${item.stok}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (item.stok > 0) GreenSuccess else RedError
                                        )
                                    }
                                }
                            }
                        }
                    }
                    is com.mylab.qrscanner.presentation.viewmodel.ProductState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = OrangeMain)
                        }
                    }
                    else -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Tidak ada barang tersedia", color = GrayLight)
                        }
                    }
                }
            } else if (selectedItem != null) {
                // Borrowing Form
                val item = selectedItem!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = BlackCard
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Detail Barang",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = OrangeMain
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nama: ${item.itemName}", color = White)
                            Text("Kode: ${item.itemCode}", color = GrayLight)
                            Text("Kategori: ${item.category}", color = GrayLight)
                            Text("Kondisi: ${item.condition}", color = GrayLight)
                            Text("Stok: ${item.stok}", color = if (item.stok > 0) GreenSuccess else RedError)
                        }
                    }
                    
                    // TODO: Get current user from AuthViewModel
                    // For now, using placeholder
                    Button(
                        onClick = {
                            borrowingViewModel.borrowItem(
                                userId = "current_user_id", // TODO: Get from auth
                                userName = "Current User", // TODO: Get from auth
                                itemId = item.id,
                                itemCode = item.itemCode,
                                itemName = item.itemName
                            )
                        },
                        enabled = item.stok > 0 &&
                                  borrowingState !is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeMain,
                            contentColor = Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (borrowingState is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Loading) {
                            CircularProgressIndicator(color = Black, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "Pinjam Barang",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    if (borrowingState is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Error) {
                        Text(
                            text = (borrowingState as com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Error).message,
                            color = RedError
                        )
                    }
                }
            } else {
                // Initial Screen - Select Item
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { showItemList = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeMain,
                            contentColor = Black
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pilih Barang untuk Dipinjam",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

