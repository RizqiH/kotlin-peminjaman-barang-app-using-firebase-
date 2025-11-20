package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Bitmap
import android.widget.ImageView
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.util.QRCodeGenerator
import com.mylab.qrscanner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeDisplayScreen(
    item: LabItem,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit
) {
    // Generate QR code from itemCode (this is what will be scanned)
    val qrCodeBitmap = remember(item.itemCode) {
        QRCodeGenerator.generateQRCodeWithColors(
            text = item.itemCode,  // QR code contains the itemCode for scanning
            width = 512,
            height = 512,
            foregroundColor = android.graphics.Color.BLACK,
            backgroundColor = android.graphics.Color.WHITE
        )
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
                        text = "QR Code Generated",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = White
                        )
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Item Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BlackCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = item.itemName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Code: ${item.itemCode}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = GrayLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Category: ${item.category}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayMedium
                        )
                    }
                }
                
                // QR Code Display
                Card(
                    modifier = Modifier
                        .size(300.dp)
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = White
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrCodeBitmap != null) {
                            AndroidView(
                                factory = { context ->
                                    ImageView(context).apply {
                                        setImageBitmap(qrCodeBitmap)
                                        scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            CircularProgressIndicator(color = OrangeMain)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Scan this QR code to view item details",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayLight
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GrayDark,
                            contentColor = White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Close")
                    }
                    
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeMain,
                            contentColor = Black
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Done",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

