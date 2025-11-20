package com.mylab.qrscanner.presentation.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.presentation.viewmodel.QRScannerState
import com.mylab.qrscanner.presentation.viewmodel.QRScannerViewModel
import com.mylab.qrscanner.ui.theme.*

@Composable
fun ResultScreen(
    qrCode: String,
    viewModel: QRScannerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onScanAgain: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(qrCode) {
        if (state is QRScannerState.Idle) {
            viewModel.verifyQRCode(qrCode)
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                
                Text(
                    text = "QR Code",
                    style = MaterialTheme.typography.titleLarge,
                    color = White,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(
                    onClick = { /* More options */ },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More"
                    )
                }
            }
            
            when (val currentState = state) {
                is QRScannerState.Loading -> {
                    LoadingContent()
                }
                is QRScannerState.Success -> {
                    SuccessContent(
                        item = currentState.item,
                        qrCode = qrCode,
                        onScanAgain = onScanAgain
                    )
                }
                is QRScannerState.Error -> {
                    ErrorContent(
                        message = currentState.message,
                        onRetry = { viewModel.verifyQRCode(qrCode) },
                        onScanAgain = onScanAgain
                    )
                }
                is QRScannerState.Idle -> {
                    LoadingContent()
                }
            }
        }
    }
}

@Composable
fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                color = OrangeMain,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Memverifikasi barang...",
                style = MaterialTheme.typography.bodyLarge,
                color = GrayLight
            )
        }
    }
}

@Composable
fun SuccessContent(
    item: LabItem,
    qrCode: String,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // QR Code Display
        val qrBitmap = remember(qrCode) { generateQRCode(qrCode) }
        if (qrBitmap != null) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(White)
                    .border(4.dp, OrangeMain, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Data Section
        Text(
            text = "Data",
            style = MaterialTheme.typography.titleMedium,
            color = GrayLight,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = qrCode,
            style = MaterialTheme.typography.bodyMedium,
            color = White,
            modifier = Modifier.align(Alignment.Start)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Item Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = BlackCard
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailRow(
                    label = "Nama Barang",
                    value = item.itemName,
                    icon = Icons.Default.Inventory
                )
                
                DetailRow(
                    label = "Kode Barang",
                    value = item.itemCode,
                    icon = Icons.Default.QrCode2
                )
                
                DetailRow(
                    label = "Kategori",
                    value = item.category,
                    icon = Icons.Default.Category
                )
                
                DetailRow(
                    label = "Kondisi",
                    value = item.condition,
                    icon = Icons.Default.CheckCircle,
                    valueColor = getConditionColor(item.condition)
                )
                
                DetailRow(
                    label = "Lokasi",
                    value = item.location,
                    icon = Icons.Default.LocationOn
                )
                
                item.description?.let { description ->
                    if (description.isNotEmpty()) {
                        DetailRow(
                            label = "Deskripsi",
                            value = description,
                            icon = Icons.Default.Description
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { /* Save */ },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangeMain
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(OrangeMain)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Simpan")
            }
            
            OutlinedButton(
                onClick = { /* Copy */ },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangeMain
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(OrangeMain)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Salin")
            }
            
            OutlinedButton(
                onClick = { /* Share */ },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangeMain
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(OrangeMain)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bagikan")
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
    
    // Bottom Floating Action Buttons
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = { /* History */ },
                containerColor = BlackCard,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    modifier = Modifier.size(24.dp)
                )
            }
            
            FloatingActionButton(
                onClick = onScanAgain,
                containerColor = OrangeMain,
                contentColor = Black,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan Again",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            FloatingActionButton(
                onClick = { /* Gallery */ },
                containerColor = BlackCard,
                contentColor = White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = "Gallery",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onScanAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Red
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Barang Tidak Ditemukan",
            style = MaterialTheme.typography.headlineSmall,
            color = White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = GrayLight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeMain,
                contentColor = Black
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "Coba Lagi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedButton(
            onClick = onScanAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = OrangeMain
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(OrangeMain)
            )
        ) {
            Text(
                text = "Scan Ulang",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueColor: Color = White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangeMain,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = GrayLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = valueColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun getConditionColor(condition: String): Color {
    return when (condition.lowercase()) {
        "baik", "good", "excellent" -> Green
        "sedang", "fair", "moderate" -> Yellow
        "rusak", "buruk", "poor", "damaged" -> Red
        else -> White
    }
}

fun generateQRCode(content: String, size: Int = 512): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}





