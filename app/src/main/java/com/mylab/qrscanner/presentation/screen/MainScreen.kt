package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.ui.theme.*

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isCenter: Boolean = false
) {
    // Petugas menu
    object Home : BottomNavItem("home", "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Products : BottomNavItem("products", "Products", Icons.Filled.Inventory, Icons.Outlined.Inventory)
    object ScanQR : BottomNavItem("scanner", "Scan QR", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner, isCenter = true)
    object Approval : BottomNavItem("approval", "Approval", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle)
    object Profile : BottomNavItem("profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person)
    
    // Mahasiswa menu
    object Return : BottomNavItem("return", "Return", Icons.Filled.Undo, Icons.Outlined.Undo)
    object History : BottomNavItem("history", "History", Icons.Filled.History, Icons.Outlined.History)
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateToScanner: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToApproval: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReturn: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToActiveBorrowings: () -> Unit,
    onLogout: () -> Unit
) {
    val authState by authViewModel.state.collectAsState()
    val currentUser = when (val state = authState) {
        is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success -> state.user
        else -> null
    }
    val isPetugas = currentUser?.isPetugas() == true
    
    // Role-based menu items
    val petugasItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Products,
        BottomNavItem.ScanQR,
        BottomNavItem.Approval,
        BottomNavItem.Profile
    )
    
    val mahasiswaItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Products,
        BottomNavItem.Return,
        BottomNavItem.History,
        BottomNavItem.Profile
    )
    
    val items = if (isPetugas) petugasItems else mahasiswaItems
    var selectedItem by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            CustomBottomNavigationBar(
                items = items,
                selectedIndex = selectedItem,
                onItemSelected = { index ->
                    val clickedItem = items[index]
                    when (clickedItem) {
                        is BottomNavItem.ScanQR -> {
                            // ScanQR navigasi ke screen terpisah, tidak ubah selectedItem
                            onNavigateToScanner()
                        }
                        is BottomNavItem.Approval -> {
                            // Approval navigasi ke screen terpisah, langsung navigate tanpa ubah selectedItem
                            // Karena akan navigate ke screen terpisah, tidak perlu update selectedItem
                            onNavigateToApproval()
                        }
                        is BottomNavItem.Profile -> {
                            // Profile navigasi ke screen terpisah, langsung navigate tanpa ubah selectedItem
                            // Karena akan navigate ke screen terpisah, tidak perlu update selectedItem
                            onNavigateToProfile()
                        }
                        is BottomNavItem.Return -> {
                            // Return hanya ubah selectedItem (screen di MainScreen)
                            selectedItem = index
                        }
                        is BottomNavItem.History -> {
                            // History navigasi ke screen terpisah, langsung navigate tanpa ubah selectedItem
                            onNavigateToHistory()
                        }
                        else -> {
                            // Home, Products hanya ubah selectedItem (screen di MainScreen)
                            selectedItem = index
                        }
                    }
                },
                isPetugas = isPetugas
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when {
                isPetugas -> {
                    // Petugas: Home(0), Products(1), ScanQR(2-center), Approval(3), Profile(4)
                    when (selectedItem) {
                        0 -> DashboardScreen(
                            onNavigateToScanner = onNavigateToScanner,
                            onNavigateToProducts = { selectedItem = 1 },
                            onNavigateToHistory = onNavigateToHistory,
                            onNavigateToAddProduct = onNavigateToAddProduct,
                            onNavigateToActiveBorrowings = onNavigateToActiveBorrowings,
                            onLogout = onLogout
                        )
                        1 -> ProductListScreen(
                            onNavigateBack = { selectedItem = 0 },
                            onNavigateToAddProduct = onNavigateToAddProduct,
                            onProductClick = { item ->
                                onNavigateToProductDetail(item.id)
                            }
                        )
                        2 -> {
                            // ScanQR - tidak ada screen di sini, navigasi ke screen terpisah
                            // Tapi tetap perlu screen default jika selectedItem = 2
                            DashboardScreen(
                                onNavigateToScanner = onNavigateToScanner,
                                onNavigateToProducts = { selectedItem = 1 },
                                onNavigateToHistory = onNavigateToHistory,
                                onNavigateToAddProduct = onNavigateToAddProduct,
                                onNavigateToActiveBorrowings = onNavigateToActiveBorrowings,
                                onLogout = onLogout
                            )
                        }
                        3 -> ApprovalScreen(
                            onNavigateBack = { selectedItem = 0 },
                            onNavigateToReturnVerification = { borrowingId, expectedItemId ->
                                // Navigate ke scanner untuk verifikasi return
                                onNavigateToScanner()
                            },
                            onNavigateToScanner = {
                                onNavigateToScanner()
                            }
                        )
                        4 -> ProfileScreen(
                            onNavigateBack = { selectedItem = 0 },
                            onLogout = onLogout
                        )
                        else -> DashboardScreen(
                            onNavigateToScanner = onNavigateToScanner,
                            onNavigateToProducts = { selectedItem = 1 },
                            onNavigateToHistory = onNavigateToHistory,
                            onNavigateToAddProduct = onNavigateToAddProduct,
                            onNavigateToActiveBorrowings = onNavigateToActiveBorrowings,
                            onLogout = onLogout
                        )
                    }
                }
                else -> {
                    // Mahasiswa: Home(0), Products(1), Return(2), History(3), Profile(4)
                    when (selectedItem) {
                        0 -> DashboardScreen(
                            onNavigateToScanner = {},
                            onNavigateToProducts = { selectedItem = 1 },
                            onNavigateToHistory = onNavigateToHistory,
                            onNavigateToAddProduct = {},
                            onNavigateToActiveBorrowings = {},
                            onLogout = onLogout
                        )
                        1 -> ProductListScreen(
                            onNavigateBack = { selectedItem = 0 },
                            onNavigateToAddProduct = {},
                            onProductClick = { item ->
                                onNavigateToProductDetail(item.id)
                            }
                        )
                        2 -> ReturnScreen(
                            onNavigateBack = { selectedItem = 0 },
                            onNavigateToReturnVerification = { borrowingId, expectedItemId ->
                                // Navigate ke scanner untuk scan QR
                                onNavigateToScanner()
                            }
                        )
                        3 -> BorrowingHistoryScreen(
                            onNavigateBack = { selectedItem = 0 }
                        )
                        4 -> ProfileScreen(
                            onNavigateBack = { selectedItem = 0 },
                            onLogout = onLogout
                        )
                        else -> DashboardScreen(
                            onNavigateToScanner = {},
                            onNavigateToProducts = { selectedItem = 1 },
                            onNavigateToHistory = onNavigateToHistory,
                            onNavigateToAddProduct = {},
                            onNavigateToActiveBorrowings = {},
                            onLogout = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CustomBottomNavigationBar(
    items: List<BottomNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    isPetugas: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        // Bottom bar dengan cutout di tengah
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter),
            color = BlackCard,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            shadowElevation = 0.dp,
            tonalElevation = 0.dp
        ) {
            // Empty - content akan di overlay
        }
        
        // Content overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .align(Alignment.BottomCenter)
        ) {
            // Regular buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedIndex == index
                    val isCenter = item.isCenter && isPetugas
                    val isReturnCenter = item is BottomNavItem.Return && !isPetugas
                    
                    if (!isCenter && !isReturnCenter) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onItemSelected(index) }
                                .padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title,
                                tint = if (isSelected) OrangeMain else GrayMedium,
                                modifier = Modifier.size(26.dp)
                            )
                            
                            // Selection dot indicator
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 5.dp else 0.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) OrangeMain else androidx.compose.ui.graphics.Color.Transparent)
                            )
                        }
                    } else {
                        // Spacer untuk center button
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        // Center floating button
        items.forEachIndexed { index, item ->
            val isCenter = item.isCenter && isPetugas
            val isReturnCenter = item is BottomNavItem.Return && !isPetugas
            
            if (isCenter || isReturnCenter) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 10.dp)
                        .clickable { onItemSelected(index) }
                ) {
                    Surface(
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        color = OrangeMain,
                        shadowElevation = 12.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.selectedIcon,
                                contentDescription = item.title,
                                tint = White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    
                    // Dot indicator untuk center button
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(OrangeMain)
                    )
                }
            }
        }
    }
}
