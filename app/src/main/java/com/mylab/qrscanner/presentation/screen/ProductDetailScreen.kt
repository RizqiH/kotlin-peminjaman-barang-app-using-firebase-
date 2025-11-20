package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.presentation.viewmodel.ProductViewModel
import com.mylab.qrscanner.presentation.viewmodel.BorrowingViewModel
import com.mylab.qrscanner.util.QRCodeGenerator
import com.mylab.qrscanner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    productId: String,
    productViewModel: ProductViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    borrowingViewModel: BorrowingViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val productState by productViewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()
    val borrowingActionState by borrowingViewModel.actionState.collectAsState()
    
    val currentUser = when (val state = authState) {
        is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success -> state.user
        else -> null
    }
    val isPetugas = currentUser?.isPetugas() == true
    
    LaunchedEffect(borrowingActionState) {
        when (val state = borrowingActionState) {
            is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Success -> {
                borrowingViewModel.resetActionState()
                onNavigateBack() // Navigate back after successful borrow request
            }
            else -> {}
        }
    }
    
    LaunchedEffect(productId) {
        productViewModel.loadProductById(productId)
    }
    
    val product = when (val state = productState) {
        is com.mylab.qrscanner.presentation.viewmodel.ProductState.Success -> {
            state.items.firstOrNull { it.id == productId }
        }
        else -> null
    }
    
    // Generate QR code from item ID (for verification)
    val qrCodeBitmap = remember(product?.id) {
        product?.id?.let { itemId ->
            QRCodeGenerator.generateQRCodeWithColors(
                text = itemId,  // QR code contains the item ID for verification
                width = 512,
                height = 512,
                foregroundColor = android.graphics.Color.BLACK,
                backgroundColor = android.graphics.Color.WHITE
            )
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        when {
            product == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeMain)
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar
                    TopAppBar(
                        title = {
                            Text(
                                text = "Product Detail",
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
                            if (isPetugas) {
                                IconButton(onClick = { onNavigateToEdit(product.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        tint = OrangeMain
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        productViewModel.deleteProduct(product.id)
                                        onNavigateBack()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = RedError
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = BlackCard
                        )
                    )
                    
                    // Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // QR Code Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BlackCard
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "QR Code untuk Verifikasi",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Scan QR ini untuk verifikasi barang",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayLight
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                
                                if (qrCodeBitmap != null) {
                                    AndroidView(
                                        factory = { context ->
                                            ImageView(context).apply {
                                                setImageBitmap(qrCodeBitmap)
                                                scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                                            }
                                        },
                                        modifier = Modifier
                                            .size(300.dp)
                                            .background(White, RoundedCornerShape(16.dp))
                                            .padding(16.dp)
                                    )
                                } else {
                                    CircularProgressIndicator(color = OrangeMain)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "ID: ${product.id}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayMedium,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                        
                        // Product Details
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = BlackCard
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Detail Barang",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = White
                                )
                                
                                DetailRow("Nama Barang", product.itemName)
                                DetailRow("Kode Barang", product.itemCode)
                                DetailRow("Kategori", product.category)
                                DetailRow("Kondisi", product.condition)
                                DetailRow("Lokasi", product.location)
                                DetailRow("Stock", product.stok.toString(), 
                                    color = if (product.stok > 0) GreenSuccess else RedError)
                                
                                product.description?.let {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Deskripsi",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GrayLight
                                    )
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = White
                                    )
                                }
                            }
                        }
                        
                        // Pinjam Button for Mahasiswa
                        if (!isPetugas && product.stok > 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    currentUser?.let { user ->
                                        borrowingViewModel.borrowItem(
                                            userId = user.uid,
                                            userName = user.nama,
                                            itemId = product.id,
                                            itemCode = product.itemCode,
                                            itemName = product.itemName
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = borrowingActionState !is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Loading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = OrangeMain,
                                    contentColor = Black
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                if (borrowingActionState is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Loading) {
                                    CircularProgressIndicator(
                                        color = Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AddShoppingCart,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pinjam Barang",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            if (borrowingActionState is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Error) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = (borrowingActionState as com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Error).message,
                                    color = RedError,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Permintaan pinjaman akan menunggu persetujuan petugas",
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayLight
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color = White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = GrayLight
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

