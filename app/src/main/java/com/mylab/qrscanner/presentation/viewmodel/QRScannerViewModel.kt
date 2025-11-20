package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.local.HistoryManager
import com.mylab.qrscanner.data.model.LabItem
import com.mylab.qrscanner.data.repository.LabItemRepository
import com.mylab.qrscanner.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QRScannerState {
    object Idle : QRScannerState()
    object Loading : QRScannerState()
    data class Success(val item: LabItem) : QRScannerState()
    data class Error(val message: String) : QRScannerState()
}

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val repository: LabItemRepository,
    val historyManager: HistoryManager
) : ViewModel() {
    
    private val _state = MutableStateFlow<QRScannerState>(QRScannerState.Idle)
    val state: StateFlow<QRScannerState> = _state.asStateFlow()
    
    private val _scannedQRCode = MutableStateFlow<String?>(null)
    val scannedQRCode: StateFlow<String?> = _scannedQRCode.asStateFlow()
    
    fun verifyQRCode(qrCode: String) {
        viewModelScope.launch {
            _state.value = QRScannerState.Loading
            _scannedQRCode.value = qrCode
            
            when (val result = repository.verifyItemByQRCode(qrCode)) {
                is Result.Success -> {
                    _state.value = QRScannerState.Success(result.data)
                }
                is Result.Error -> {
                    _state.value = QRScannerState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = QRScannerState.Loading
                }
            }
        }
    }
    
    fun resetState() {
        _state.value = QRScannerState.Idle
        _scannedQRCode.value = null
    }
}




