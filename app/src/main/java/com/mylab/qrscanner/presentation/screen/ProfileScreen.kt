package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.state.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    
    val currentUser = when (val state = authState) {
        is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success -> state.user
        else -> null
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
                        text = "Profile",
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
            
            // Profile Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Profile Header
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
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(OrangeMain.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = OrangeMain,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        
                        Text(
                            text = currentUser?.nama ?: "User",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = White
                        )
                        
                        Text(
                            text = currentUser?.email ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayLight
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (currentUser?.isPetugas() == true) OrangeMain.copy(alpha = 0.2f) else BlueInfo.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (currentUser?.isPetugas() == true) "Petugas" else "Mahasiswa",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (currentUser?.isPetugas() == true) OrangeMain else BlueInfo,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Menu Items
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = BlackCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        ProfileMenuItem(
                            icon = Icons.Default.Settings,
                            title = "Settings",
                            onClick = { /* TODO: Navigate to settings */ }
                        )
                        Divider(color = GrayDark, thickness = 1.dp)
                        ProfileMenuItem(
                            icon = Icons.Default.Info,
                            title = "About",
                            onClick = { /* TODO: Navigate to about */ }
                        )
                        Divider(color = GrayDark, thickness = 1.dp)
                        ProfileMenuItem(
                            icon = Icons.Default.ExitToApp,
                            title = "Logout",
                            onClick = { showLogoutDialog = true },
                            iconColor = RedError,
                            textColor = RedError
                        )
                    }
                }
            }
        }
    }
    
    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Text(
                    text = "Logout",
                    color = White
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin logout?",
                    color = GrayLight
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedError,
                        contentColor = White
                    )
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false }
                ) {
                    Text("Batal", color = GrayLight)
                }
            },
            containerColor = BlackCard,
            titleContentColor = White,
            textContentColor = GrayLight
        )
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    iconColor: androidx.compose.ui.graphics.Color = White,
    textColor: androidx.compose.ui.graphics.Color = White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = GrayMedium,
            modifier = Modifier.size(20.dp)
        )
    }
}

