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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.presentation.viewmodel.AddProductViewModel
import com.mylab.qrscanner.presentation.viewmodel.AddProductState
import com.mylab.qrscanner.ui.theme.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: AddProductViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onSuccess: (LabItem) -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Electronics") }
    var condition by remember { mutableStateOf("Good") }
    var location by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    val state by viewModel.state.collectAsState()
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showConditionMenu by remember { mutableStateOf(false) }
    
    val categories = listOf("Electronics", "Chemistry", "Biology", "Physics", "General")
    val conditions = listOf("Good", "Maintenance", "Broken")
    
    LaunchedEffect(state) {
        when (val currentState = state) {
            is AddProductState.Success -> {
                // Auto-generate QR code and navigate to display screen
                onSuccess(currentState.createdItem)
            }
            else -> {}
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
                title = {
                    Text(
                        text = "Add Lab Item",
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
            
            // Form Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name", color = GrayLight) },
                    placeholder = { Text("e.g., Microscope", color = GrayMedium) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BlackCard,
                        unfocusedContainerColor = BlackCard,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = OrangeMain,
                        focusedIndicatorColor = OrangeMain,
                        unfocusedIndicatorColor = GrayDark,
                        focusedLabelColor = OrangeMain,
                        unfocusedLabelColor = GrayLight
                    )
                )
                
                // Category Dropdown
                ExposedDropdownMenuBox(
                    expanded = showCategoryMenu,
                    onExpandedChange = { showCategoryMenu = !showCategoryMenu }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category", color = GrayLight) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BlackCard,
                            unfocusedContainerColor = BlackCard,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            cursorColor = OrangeMain,
                            focusedIndicatorColor = OrangeMain,
                            unfocusedIndicatorColor = GrayDark,
                            focusedLabelColor = OrangeMain,
                            unfocusedLabelColor = GrayLight
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false },
                        modifier = Modifier.background(BlackCard)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = White) },
                                onClick = {
                                    category = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Condition Dropdown
                ExposedDropdownMenuBox(
                    expanded = showConditionMenu,
                    onExpandedChange = { showConditionMenu = !showConditionMenu }
                ) {
                    OutlinedTextField(
                        value = condition,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Condition", color = GrayLight) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showConditionMenu)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = BlackCard,
                            unfocusedContainerColor = BlackCard,
                            focusedTextColor = White,
                            unfocusedTextColor = White,
                            cursorColor = OrangeMain,
                            focusedIndicatorColor = OrangeMain,
                            unfocusedIndicatorColor = GrayDark,
                            focusedLabelColor = OrangeMain,
                            unfocusedLabelColor = GrayLight
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showConditionMenu,
                        onDismissRequest = { showConditionMenu = false },
                        modifier = Modifier.background(BlackCard)
                    ) {
                        conditions.forEach { cond ->
                            DropdownMenuItem(
                                text = { Text(cond, color = White) },
                                onClick = {
                                    condition = cond
                                    showConditionMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location", color = GrayLight) },
                    placeholder = { Text("e.g., Lab Room 101", color = GrayMedium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = GrayLight
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BlackCard,
                        unfocusedContainerColor = BlackCard,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = OrangeMain,
                        focusedIndicatorColor = OrangeMain,
                        unfocusedIndicatorColor = GrayDark,
                        focusedLabelColor = OrangeMain,
                        unfocusedLabelColor = GrayLight
                    )
                )
                
                // Stock
                OutlinedTextField(
                    value = stok,
                    onValueChange = { newValue ->
                        // Only allow numeric input
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            stok = newValue
                        }
                    },
                    label = { Text("Stock", color = GrayLight) },
                    placeholder = { Text("e.g., 10", color = GrayMedium) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = null,
                            tint = GrayLight
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BlackCard,
                        unfocusedContainerColor = BlackCard,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = OrangeMain,
                        focusedIndicatorColor = OrangeMain,
                        unfocusedIndicatorColor = GrayDark,
                        focusedLabelColor = OrangeMain,
                        unfocusedLabelColor = GrayLight
                    )
                )
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)", color = GrayLight) },
                    placeholder = { Text("Additional details...", color = GrayMedium) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = BlackCard,
                        unfocusedContainerColor = BlackCard,
                        focusedTextColor = White,
                        unfocusedTextColor = White,
                        cursorColor = OrangeMain,
                        focusedIndicatorColor = OrangeMain,
                        unfocusedIndicatorColor = GrayDark,
                        focusedLabelColor = OrangeMain,
                        unfocusedLabelColor = GrayLight
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Error Message
                if (state is AddProductState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = RedError.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = RedError
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (state as AddProductState.Error).message,
                                color = RedError,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Submit Button
                Button(
                    onClick = {
                        if (itemName.isNotEmpty() && location.isNotEmpty() && stok.isNotEmpty()) {
                            // Auto-generate itemCode (using timestamp + random)
                            val finalItemCode = "LAB-${System.currentTimeMillis().toString().takeLast(8)}"
                            val stockValue = stok.toIntOrNull() ?: 0
                            
                            val newItem = LabItem(
                                id = "", // Will be auto-generated by Firestore
                                itemCode = finalItemCode,
                                itemName = itemName,
                                category = category,
                                condition = condition,
                                location = location,
                                stok = stockValue,
                                description = description.ifEmpty { null }
                            )
                            viewModel.addProduct(newItem)
                        }
                    },
                    enabled = itemName.isNotEmpty() && 
                              location.isNotEmpty() &&
                              stok.isNotEmpty() &&
                              stok.toIntOrNull() != null &&
                              state !is AddProductState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeMain,
                        contentColor = Black,
                        disabledContainerColor = GrayDark,
                        disabledContentColor = GrayMedium
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (state is AddProductState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Black
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Item",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}


