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

sealed class AddProductState {
    object Idle : AddProductState()
    object Loading : AddProductState()
    data class Success(val createdItem: LabItem) : AddProductState()
    data class Error(val message: String) : AddProductState()
}

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val repository: LabItemRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<AddProductState>(AddProductState.Idle)
    val state: StateFlow<AddProductState> = _state.asStateFlow()
    
    fun addProduct(item: LabItem) {
        viewModelScope.launch {
            _state.value = AddProductState.Loading
            
            when (val result = repository.createItem(item)) {
                is Result.Success -> {
                    _state.value = AddProductState.Success(result.data)
                }
                is Result.Error -> {
                    _state.value = AddProductState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = AddProductState.Loading
                }
            }
        }
    }
    
    fun resetState() {
        _state.value = AddProductState.Idle
    }
}


