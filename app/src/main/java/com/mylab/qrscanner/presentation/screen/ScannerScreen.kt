package com.mylab.qrscanner.presentation.screen

import android.Manifest
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.data.model.ScanHistory
import com.mylab.qrscanner.presentation.viewmodel.HistoryViewModel
import com.mylab.qrscanner.presentation.viewmodel.VerificationLogViewModel
import com.mylab.qrscanner.ui.theme.*
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(),
    verificationLogViewModel: VerificationLogViewModel = hiltViewModel(),
    borrowingViewModel: com.mylab.qrscanner.presentation.viewmodel.BorrowingViewModel = hiltViewModel(),
    onQRCodeScanned: (String) -> Unit = {},
    onNavigateBack: () -> Unit,
    onNavigateToHistory: () -> Unit,
    borrowingId: String? = null, // Untuk return verification
    expectedItemId: String? = null // Untuk return verification
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var isFlashEnabled by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var shouldTriggerScan by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val actionState by borrowingViewModel.actionState.collectAsState()
    val pendingReturnVerification by borrowingViewModel.pendingReturnVerification.collectAsState()
    
    // Check if there's pending return verification from ApprovalScreen
    val returnBorrowingId = borrowingId ?: pendingReturnVerification?.first
    val returnExpectedItemId = (expectedItemId ?: pendingReturnVerification?.second)?.trim()
    val isReturnVerificationMode = returnBorrowingId != null && returnExpectedItemId != null && returnExpectedItemId.isNotEmpty()
    
    // TODO: Get current user from AuthViewModel
    val currentUserId = "current_user_id" // Placeholder
    val currentUserName = "Current User" // Placeholder
    
    // Gallery picker launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
                uri?.let {
            scanQRCodeFromImage(context, it) { qrCode ->
                if (isReturnVerificationMode && returnBorrowingId != null) {
                    // Mode return verification - langsung approve jika cocok
                    val scannedId = qrCode.trim()
                    scope.launch {
                        when (val result = borrowingViewModel.verifyReturnQRCode(returnBorrowingId, scannedId)) {
                            is com.mylab.qrscanner.data.repository.Result.Success -> {
                                // QR code cocok, langsung approve
                                borrowingViewModel.approveReturn(returnBorrowingId, scannedId)
                            }
                            is com.mylab.qrscanner.data.repository.Result.Error -> {
                                // QR code tidak cocok
                                showError = result.message
                                shouldTriggerScan = true
                            }
                            else -> {}
                        }
                    }
                } else {
                    // Normal scan mode
                    // Save to history via ViewModel
                    historyViewModel.saveHistory(ScanHistory(qrCode = qrCode, itemName = null))
                    // Log verification to Firestore
                    scope.launch {
                        verificationLogViewModel.logVerification(
                            barcode = qrCode,
                            userId = currentUserId,
                            userName = currentUserName
                        )
                    }
                    onQRCodeScanned(qrCode)
                }
            }
        }
    }
    
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    // Handle return verification success/error
    LaunchedEffect(actionState) {
        when (val state = actionState) {
            is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Success -> {
                if (isReturnVerificationMode) {
                    // Tampilkan success message
                    showSuccessMessage = "Barang cocok, pengembalian berhasil"
                    // Clear pending verification setelah delay
                    scope.launch {
                        kotlinx.coroutines.delay(2000)
                        borrowingViewModel.clearPendingReturnVerification()
                        onNavigateBack()
                    }
                }
            }
            is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Error -> {
                if (isReturnVerificationMode) {
                    showError = state.message
                    shouldTriggerScan = true
                }
            }
            else -> {}
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreview(
                onQRCodeScanned = { qrCode ->
                    if (isReturnVerificationMode && returnBorrowingId != null) {
                        // Mode return verification - langsung approve jika cocok
                        val scannedId = qrCode.trim()
                        scope.launch {
                            when (val result = borrowingViewModel.verifyReturnQRCode(returnBorrowingId, scannedId)) {
                                is com.mylab.qrscanner.data.repository.Result.Success -> {
                                    // QR code cocok, langsung approve
                                    borrowingViewModel.approveReturn(returnBorrowingId, scannedId)
                                }
                                is com.mylab.qrscanner.data.repository.Result.Error -> {
                                    // QR code tidak cocok
                                    showError = result.message
                                    shouldTriggerScan = true
                                }
                                else -> {}
                            }
                        }
                    } else {
                        // Normal scan mode
                        // Save to history via ViewModel
                        historyViewModel.saveHistory(ScanHistory(qrCode = qrCode, itemName = null))
                        // Log verification to Firestore
                        scope.launch {
                            verificationLogViewModel.logVerification(
                                barcode = qrCode,
                                userId = currentUserId,
                                userName = currentUserName
                            )
                        }
                        onQRCodeScanned(qrCode)
                    }
                },
                isFlashEnabled = isFlashEnabled,
                shouldTriggerScan = shouldTriggerScan,
                onCameraControlReady = { control ->
                    cameraControl = control
                },
                onScanTriggered = {
                    shouldTriggerScan = false
                }
            )
        } else {
            PermissionDeniedContent(
                permissionState = cameraPermissionState
            )
        }
        
        // Top Bar with icons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = BlackCard.copy(alpha = 0.7f),
                    contentColor = White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { 
                        // Open gallery
                        galleryLauncher.launch("image/*")
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = BlackCard.copy(alpha = 0.7f),
                        contentColor = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Gallery"
                    )
                }
                
                IconButton(
                    onClick = { 
                        // Toggle flash
                        isFlashEnabled = !isFlashEnabled
                        cameraControl?.enableTorch(isFlashEnabled)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (isFlashEnabled) OrangeMain.copy(alpha = 0.7f) else BlackCard.copy(alpha = 0.7f),
                        contentColor = if (isFlashEnabled) Black else White
                    )
                ) {
                    Icon(
                        imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Flash"
                    )
                }
                
                IconButton(
                    onClick = {
                        // Toggle camera (future feature)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = BlackCard.copy(alpha = 0.7f),
                        contentColor = White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera"
                    )
                }
            }
        }
        
        // QR Scanning Frame Overlay
        QRScannerOverlay()
        
        // Success Message (untuk return verification)
        if (showSuccessMessage != null && isReturnVerificationMode) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = GreenSuccess.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = showSuccessMessage ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
            }
        }
        
        // Error Message (untuk return verification)
        if (showError != null && isReturnVerificationMode && showSuccessMessage == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = RedError.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = showError ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                }
            }
        }
        
        // Loading State (untuk return verification)
        if (actionState is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Loading && isReturnVerificationMode && showSuccessMessage == null) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 80.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlackCard.copy(alpha = 0.9f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = OrangeMain,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Memverifikasi dan mengembalikan barang...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = White
                    )
                }
            }
        }
        
        // Instructions untuk return verification
        if (isReturnVerificationMode && showSuccessMessage == null && showError == null && actionState !is com.mylab.qrscanner.presentation.viewmodel.BorrowingActionState.Loading) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 80.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlackCard.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = OrangeMain,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Verifikasi Pengembalian",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                    }
                    Text(
                        text = "Scan QR Code pada barang yang akan dikembalikan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayLight
                    )
                }
            }
        }
        
        // Bottom Actions - hanya tampil jika bukan return verification mode
        if (!isReturnVerificationMode) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                    onClick = { 
                        // Open gallery (alternative)
                        galleryLauncher.launch("image/*")
                    },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = White
                )
            ) {
                Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Image",
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Center Scan Button
            FloatingActionButton(
                    onClick = { 
                        // Manual trigger scan
                        shouldTriggerScan = true
                    },
                containerColor = OrangeMain,
                contentColor = Black,
                shape = CircleShape,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan",
                    modifier = Modifier.size(36.dp)
                )
            }
            
            IconButton(
                    onClick = { 
                        // Open history
                        onNavigateToHistory()
                    },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "History",
                    modifier = Modifier.size(28.dp)
                )
                }
            }
        }
    }
}

@Composable
fun QRScannerOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Semi-transparent background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scanAreaSize = size.width * 0.7f
            val left = (size.width - scanAreaSize) / 2
            val top = (size.height - scanAreaSize) / 2
            
            // Draw semi-transparent overlay
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = size
            )
            
            // Cut out the scanning area
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(scanAreaSize, scanAreaSize),
                cornerRadius = CornerRadius(32f),
                blendMode = BlendMode.Clear
            )
        }
        
        // Orange frame corners
        Box(
            modifier = Modifier
                .size(280.dp)
                .padding(16.dp)
        ) {
            val cornerLength = 60.dp
            val cornerWidth = 6.dp
            
            // Top-left corner
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .size(cornerLength)
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, cornerLength.toPx())
                    lineTo(0f, 0f)
                    lineTo(cornerLength.toPx(), 0f)
                }
                drawPath(
                    path = path,
                    color = OrangeMain,
                    style = Stroke(width = cornerWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
            
            // Top-right corner
            Canvas(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(cornerLength)
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(cornerLength.toPx(), 0f)
                    lineTo(cornerLength.toPx(), cornerLength.toPx())
                }
                drawPath(
                    path = path,
                    color = OrangeMain,
                    style = Stroke(width = cornerWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
            
            // Bottom-left corner
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .size(cornerLength)
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, 0f)
                    lineTo(0f, cornerLength.toPx())
                    lineTo(cornerLength.toPx(), cornerLength.toPx())
                }
                drawPath(
                    path = path,
                    color = OrangeMain,
                    style = Stroke(width = cornerWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
            
            // Bottom-right corner
            Canvas(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(cornerLength)
            ) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cornerLength.toPx(), 0f)
                    lineTo(cornerLength.toPx(), cornerLength.toPx())
                    lineTo(0f, cornerLength.toPx())
                }
                drawPath(
                    path = path,
                    color = OrangeMain,
                    style = Stroke(width = cornerWidth.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    onQRCodeScanned: (String) -> Unit,
    isFlashEnabled: Boolean,
    shouldTriggerScan: Boolean,
    onCameraControlReady: (CameraControl) -> Unit,
    onScanTriggered: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    var hasScanned by remember { mutableStateOf(false) }
    var lastScanTime by remember { mutableStateOf(0L) }
    val scanCooldownMs = 2000L // 2 detik cooldown setelah scan
    
    LaunchedEffect(shouldTriggerScan) {
        if (shouldTriggerScan) {
            hasScanned = false
            lastScanTime = 0L
            onScanTriggered()
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            val currentTime = System.currentTimeMillis()
                            // Skip processing jika sudah scan atau masih dalam cooldown
                            if (hasScanned || (currentTime - lastScanTime) < scanCooldownMs) {
                                imageProxy.close()
                                return@setAnalyzer
                            }
                            
                            processImageProxy(imageProxy) { qrCode ->
                                hasScanned = true
                                lastScanTime = currentTime
                                onQRCodeScanned(qrCode)
                            }
                        }
                    }
                
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    
                    // Provide camera control
                    onCameraControlReady(camera.cameraControl)
                    
                    // Enable torch if needed
                    if (isFlashEnabled) {
                        camera.cameraControl.enableTorch(true)
                    }
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    imageProxy: ImageProxy,
    onQRCodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        
        val scanner = BarcodeScanning.getClient(options)
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { qrCode ->
                        onQRCodeDetected(qrCode)
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("QRScanner", "Barcode scanning failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

private fun scanQRCodeFromImage(
    context: Context,
    imageUri: Uri,
    onQRCodeDetected: (String) -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        
        val image = InputImage.fromBitmap(bitmap, 0)
        
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        
        val scanner = BarcodeScanning.getClient(options)
        
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    barcodes[0].rawValue?.let { qrCode ->
                        onQRCodeDetected(qrCode)
                    }
                } else {
                    Log.e("QRScanner", "No QR code found in image")
                }
            }
            .addOnFailureListener { e ->
                Log.e("QRScanner", "Barcode scanning from image failed", e)
            }
    } catch (e: Exception) {
        Log.e("QRScanner", "Failed to load image", e)
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDeniedContent(
    permissionState: PermissionState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = OrangeMain
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Camera Permission Required",
            style = MaterialTheme.typography.headlineSmall,
            color = White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Camera permission is required to scan QR codes",
            style = MaterialTheme.typography.bodyLarge,
            color = GrayLight
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { permissionState.launchPermissionRequest() },
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
                text = "Grant Permission",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
