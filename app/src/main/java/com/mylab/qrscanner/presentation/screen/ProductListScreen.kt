package com.mylab.qrscanner.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.presentation.viewmodel.ProductViewModel
import com.mylab.qrscanner.presentation.viewmodel.ProductState
import com.mylab.qrscanner.presentation.viewmodel.AuthViewModel
import com.mylab.qrscanner.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onProductClick: (LabItem) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val authState by authViewModel.state.collectAsState()
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val currentUser = when (val authStateValue = authState) {
        is com.mylab.qrscanner.presentation.viewmodel.AuthState.Success -> authStateValue.user
        else -> null
    }
    val isPetugas = currentUser?.isPetugas() == true
    
    LaunchedEffect(Unit) {
        viewModel.loadProducts()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Lab Items",
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
                    IconButton(onClick = { /* Filter */ }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlackCard
                )
            )
            
            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = BlackCard
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = GrayLight
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search items...",
                                color = GrayMedium
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            cursorColor = OrangeMain,
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Category Filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedCategory == "Electronics",
                    onClick = { selectedCategory = "Electronics" },
                    label = { Text("Electronics") }
                )
                FilterChip(
                    selected = selectedCategory == "Chemistry",
                    onClick = { selectedCategory = "Chemistry" },
                    label = { Text("Chemistry") }
                )
                FilterChip(
                    selected = selectedCategory == "Biology",
                    onClick = { selectedCategory = "Biology" },
                    label = { Text("Biology") }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Content
            when (val currentState = state) {
                is ProductState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = OrangeMain)
                    }
                }
                
                is ProductState.Success -> {
                    val filteredItems = currentState.items.filter { item ->
                        (selectedCategory == null || item.category == selectedCategory) &&
                        (searchQuery.isEmpty() || 
                         item.itemName.contains(searchQuery, ignoreCase = true) ||
                         item.itemCode.contains(searchQuery, ignoreCase = true))
                    }
                    
                    if (filteredItems.isEmpty()) {
                        EmptyProductList(onNavigateToAddProduct)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 12.dp,
                                end = 12.dp,
                                top = 6.dp,
                                bottom = if (isPetugas) 80.dp else 12.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredItems) { item ->
                                ProductItemCard(
                                    item = item,
                                    isPetugas = isPetugas,
                                    onClick = { onProductClick(item) },
                                    onEdit = if (isPetugas) { { onProductClick(item) } } else null,
                                    onDelete = if (isPetugas) { 
                                        { viewModel.deleteProduct(item.id) }
                                    } else null
                                )
                            }
                        }
                    }
                }
                
                is ProductState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = RedError
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = currentState.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = GrayLight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadProducts() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangeMain,
                                contentColor = Black
                            )
                        ) {
                            Text("Retry")
                        }
                    }
                }
                
                ProductState.Idle -> {
                    // Initial state
                }
            }
        }
        
        // FAB - Only show for petugas
        if (isPetugas) {
            FloatingActionButton(
                onClick = onNavigateToAddProduct,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = OrangeMain,
                contentColor = Black
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Product"
                )
            }
        }
    }
}

@Composable
fun ProductItemCard(
    item: LabItem,
    isPetugas: Boolean = false,
    onClick: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
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
            // Icon based on category
            val (icon, iconColor) = when (item.category) {
                "Electronics" -> Icons.Default.ElectricalServices to BlueInfo
                "Chemistry" -> Icons.Default.Science to PurpleAccent
                "Biology" -> Icons.Default.Biotech to GreenSuccess
                else -> Icons.Default.Inventory to OrangeMain
            }
            
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.itemCode,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayLight
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = GrayMedium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = GrayMedium
                    )
                }
            }
            
            // Condition Badge
            val (conditionColor, conditionIcon) = when (item.condition.lowercase()) {
                "good" -> GreenSuccess to Icons.Default.CheckCircle
                "maintenance" -> OrangeMain to Icons.Default.Build
                "broken" -> RedError to Icons.Default.Cancel
                else -> GrayMedium to Icons.Default.Info
            }
            
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = conditionColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = conditionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = conditionColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.condition,
                            style = MaterialTheme.typography.bodySmall,
                            color = conditionColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // Stock Badge
                Text(
                    text = "Stock: ${item.stok}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (item.stok > 0) GreenSuccess else RedError,
                    fontWeight = FontWeight.Medium
                )
                
                // Action buttons for petugas
                if (isPetugas) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        onEdit?.let {
                            IconButton(
                                onClick = { it() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = OrangeMain,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        onDelete?.let {
                            IconButton(
                                onClick = { it() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = RedError,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyProductList(onNavigateToAddProduct: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inventory,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = GrayMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "No Items Found",
            style = MaterialTheme.typography.headlineSmall,
            color = White,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Start by adding your first lab item",
            style = MaterialTheme.typography.bodyLarge,
            color = GrayLight
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onNavigateToAddProduct,
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangeMain,
                contentColor = Black
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Item",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

