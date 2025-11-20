package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mylab.qrscanner.presentation.viewmodel.DashboardViewModel
import com.mylab.qrscanner.presentation.viewmodel.DashboardState
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.data.model.Borrowing
import com.mylab.qrscanner.ui.theme.*
import com.mylab.qrscanner.ui.theme.RedError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToScanner: () -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToActiveBorrowings: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()
    
    val currentUser = when (val authStateValue = authState) {
        is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success -> authStateValue.user
        else -> null
    }
    val isPetugas = currentUser?.isPetugas() == true
    
    val dashboardData = when (val currentState = state) {
        is DashboardState.Success -> {
            currentState
        }
        else -> null
    }
    val totalScans = dashboardData?.totalScans ?: 0
    val recentScans = dashboardData?.recentScans ?: emptyList()
    val stats = dashboardData?.stats ?: emptyMap()
    val activeBorrowings = dashboardData?.activeBorrowings ?: emptyList()
    
    // Load borrowings untuk petugas
    LaunchedEffect(isPetugas) {
        if (isPetugas) {
            viewModel.refreshWithBorrowings()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        when (val currentState = state) {
            is DashboardState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeMain)
                }
            }
            is DashboardState.Error -> {
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
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeMain
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
            is DashboardState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Top Bar
                    TopAppBar(
                        title = {
                            Text(
                                text = "Dashboard",
                                color = White,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = BlackCard
                        )
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
            item {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lab Item Management System",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayLight
                    )
                }
            }
            
            item {
                // Quick Actions
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        QuickActionCard(
                            title = "Scan QR",
                            icon = Icons.Default.QrCodeScanner,
                            color = OrangeMain,
                            onClick = onNavigateToScanner
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "Products",
                            icon = Icons.Default.Inventory,
                            color = PurpleAccent,
                            onClick = onNavigateToProducts
                        )
                    }
                    item {
                        QuickActionCard(
                            title = "History",
                            icon = Icons.Default.History,
                            color = GreenSuccess,
                            onClick = {
                                // Navigate ke scan history screen
                                onNavigateToHistory()
                            }
                        )
                    }
                    if (isPetugas) {
                        item {
                            QuickActionCard(
                                title = "Add Item",
                                icon = Icons.Default.Add,
                                color = BlueInfo,
                                onClick = onNavigateToAddProduct
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                // Statistics Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Scans",
                        value = totalScans.toString(),
                        icon = Icons.Default.QrCode2,
                        color = OrangeMain
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Items",
                        value = stats["total"]?.toString() ?: "0",
                        icon = Icons.Default.Inventory2,
                        color = PurpleAccent
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Good",
                        value = stats["good"]?.toString() ?: "0",
                        icon = Icons.Default.CheckCircle,
                        color = GreenSuccess
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Maintenance",
                        value = stats["maintenance"]?.toString() ?: "0",
                        icon = Icons.Default.Warning,
                        color = OrangeMain
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            
            // Recent Scans Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Scans",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                    
                    if (recentScans.isNotEmpty()) {
                        TextButton(onClick = {
                            // Navigate ke scan history screen
                            onNavigateToHistory()
                        }) {
                            Text(
                                text = "See All",
                                color = OrangeMain
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            if (recentScans.isEmpty()) {
                item {
                    EmptyRecentScans(onNavigateToScanner)
                }
            } else {
                items(recentScans) { scan ->
                    RecentScanItem(
                        scan = scan,
                        onClick = {
                            // Navigate ke scan history screen
                            onNavigateToHistory()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Active Borrowings Section (hanya untuk petugas)
            if (isPetugas && activeBorrowings.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Barang Sedang Dipinjam",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        
                        TextButton(onClick = {
                            // Navigate ke active borrowings screen
                            onNavigateToActiveBorrowings()
                        }) {
                            Text(
                                text = "See All",
                                color = OrangeMain
                            )
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                items(activeBorrowings.take(5)) { borrowing ->
                    ActiveBorrowingItem(
                        borrowing = borrowing,
                        onClick = {
                            // Navigate ke active borrowings screen
                            onNavigateToActiveBorrowings()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(110.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlackCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = White
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlackCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayLight
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = White
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentScanItem(
    scan: com.mylab.qrscanner.data.model.ScanHistory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
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
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scan.itemName ?: "QR Code",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = scan.qrCode,
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Text(
                text = scan.getFormattedTime(),
                style = MaterialTheme.typography.bodySmall,
                color = GrayMedium
            )
        }
    }
}

@Composable
fun ActiveBorrowingItem(
    borrowing: com.mylab.qrscanner.data.model.Borrowing,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlueInfo.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = null,
                    tint = BlueInfo,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = borrowing.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Oleh: ${borrowing.userName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = GrayLight,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Surface(
                color = OrangeMain.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "DIPINJAM",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = OrangeMain,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyRecentScans(onNavigateToScanner: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = BlackCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = GrayMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No Scans Yet",
                style = MaterialTheme.typography.titleMedium,
                color = White,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Start scanning QR codes to see them here",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayLight
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onNavigateToScanner,
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeMain,
                    contentColor = Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scan Now",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

