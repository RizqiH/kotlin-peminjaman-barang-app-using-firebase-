package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.data.repository.LabItemRepository
import com.mylab.qrscanner.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductState {
    object Idle : ProductState()
    object Loading : ProductState()
    data class Success(val items: List<LabItem>) : ProductState()
    data class Error(val message: String) : ProductState()
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val repository: LabItemRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<ProductState>(ProductState.Idle)
    val state: StateFlow<ProductState> = _state.asStateFlow()
    
    private val _selectedItem = MutableStateFlow<LabItem?>(null)
    val selectedItem: StateFlow<LabItem?> = _selectedItem.asStateFlow()
    
    fun loadProducts(category: String? = null, condition: String? = null) {
        viewModelScope.launch {
            _state.value = ProductState.Loading
            
            when (val result = repository.getAllItems(category = category, condition = condition)) {
                is Result.Success -> {
                    _state.value = ProductState.Success(result.data)
                }
                is Result.Error -> {
                    _state.value = ProductState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = ProductState.Loading
                }
            }
        }
    }
    
    fun selectItem(item: LabItem) {
        _selectedItem.value = item
    }
    
    fun clearSelection() {
        _selectedItem.value = null
    }
    
    fun loadProductById(productId: String) {
        viewModelScope.launch {
            when (val result = repository.getItemById(productId)) {
                is Result.Success -> {
                    val item = result.data
                    // Update state with single item
                    _state.value = ProductState.Success(listOf(item))
                }
                is Result.Error -> {
                    _state.value = ProductState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = ProductState.Loading
                }
            }
        }
    }
    
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            when (val result = repository.deleteItem(productId)) {
                is Result.Success -> {
                    loadProducts() // Reload list
                }
                is Result.Error -> {
                    _state.value = ProductState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = ProductState.Loading
                }
            }
        }
    }
    
    fun deleteItem(itemId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.deleteItem(itemId)) {
                is Result.Success -> {
                    loadProducts() // Reload list
                    onSuccess()
                }
                is Result.Error -> {
                    onError(result.message)
                }
                is Result.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }
}


