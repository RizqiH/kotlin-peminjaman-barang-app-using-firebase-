package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.model.Borrowing
import com.mylab.qrscanner.data.repository.BorrowingRepository
import com.mylab.qrscanner.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

sealed class BorrowingState {
    object Idle : BorrowingState()
    object Loading : BorrowingState()
    data class Success(val borrowings: List<Borrowing>) : BorrowingState()
    data class Error(val message: String) : BorrowingState()
}

sealed class BorrowingActionState {
    object Idle : BorrowingActionState()
    object Loading : BorrowingActionState()
    data class Success(val borrowing: Borrowing) : BorrowingActionState()
    data class Error(val message: String) : BorrowingActionState()
}

@HiltViewModel
class BorrowingViewModel @Inject constructor(
    private val borrowingRepository: BorrowingRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<BorrowingState>(BorrowingState.Idle)
    val state: StateFlow<BorrowingState> = _state.asStateFlow()
    
    private val _actionState = MutableStateFlow<BorrowingActionState>(BorrowingActionState.Idle)
    val actionState: StateFlow<BorrowingActionState> = _actionState.asStateFlow()
    
    // Untuk menyimpan pending return verification
    private val _pendingReturnVerification = MutableStateFlow<Pair<String, String>?>(null)
    val pendingReturnVerification: StateFlow<Pair<String, String>?> = _pendingReturnVerification.asStateFlow()
    
    fun setPendingReturnVerification(borrowingId: String, expectedItemId: String) {
        _pendingReturnVerification.value = Pair(borrowingId, expectedItemId)
    }
    
    fun clearPendingReturnVerification() {
        _pendingReturnVerification.value = null
    }
    
    fun borrowItem(
        userId: String,
        userName: String,
        itemId: String,
        itemCode: String,
        itemName: String,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _actionState.value = BorrowingActionState.Loading
            
            val borrowing = Borrowing(
                userId = userId,
                userName = userName,
                itemId = itemId,
                itemCode = itemCode,
                itemName = itemName,
                tglPinjam = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                tglKembali = "",
                status = "dipinjam",
                notes = notes
            )
            
            when (val result = borrowingRepository.createBorrowing(borrowing)) {
                is Result.Success -> {
                    _actionState.value = BorrowingActionState.Success(result.data)
                }
                is Result.Error -> {
                    _actionState.value = BorrowingActionState.Error(result.message)
                }
                is Result.Loading -> {
                    _actionState.value = BorrowingActionState.Loading
                }
            }
        }
    }
    
    fun returnItem(borrowingId: String, verifiedItemId: String? = null) {
        viewModelScope.launch {
            _actionState.value = BorrowingActionState.Loading
            when (val result = borrowingRepository.returnBorrowing(borrowingId, verifiedItemId)) {
                is Result.Success -> {
                    _actionState.value = BorrowingActionState.Success(result.data)
                    loadBorrowings() // Refresh list
                }
                is Result.Error -> {
                    _actionState.value = BorrowingActionState.Error(result.message)
                }
                is Result.Loading -> {
                    _actionState.value = BorrowingActionState.Loading
                }
            }
        }
    }
    
    fun loadBorrowings(userId: String? = null) {
        viewModelScope.launch {
            _state.value = BorrowingState.Loading
            val result = if (userId != null) {
                borrowingRepository.getBorrowingsByUser(userId)
            } else {
                borrowingRepository.getAllBorrowings()
            }
            
            when (result) {
                is Result.Success -> {
                    _state.value = BorrowingState.Success(result.data)
                }
                is Result.Error -> {
                    _state.value = BorrowingState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = BorrowingState.Loading
                }
            }
        }
    }
    
    fun loadActiveBorrowings() {
        viewModelScope.launch {
            _state.value = BorrowingState.Loading
            when (val result = borrowingRepository.getActiveBorrowings()) {
                is Result.Success -> {
                    _state.value = BorrowingState.Success(result.data)
                }
                is Result.Error -> {
                    _state.value = BorrowingState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = BorrowingState.Loading
                }
            }
        }
    }
    
    fun approveBorrowing(borrowingId: String) {
        viewModelScope.launch {
            _actionState.value = BorrowingActionState.Loading
            when (val result = borrowingRepository.approveBorrowing(borrowingId)) {
                is Result.Success -> {
                    _actionState.value = BorrowingActionState.Success(result.data)
                    loadBorrowings() // Refresh list
                }
                is Result.Error -> {
                    _actionState.value = BorrowingActionState.Error(result.message)
                }
                is Result.Loading -> {
                    _actionState.value = BorrowingActionState.Loading
                }
            }
        }
    }
    
    fun rejectBorrowing(borrowingId: String) {
        viewModelScope.launch {
            _actionState.value = BorrowingActionState.Loading
            when (val result = borrowingRepository.rejectBorrowing(borrowingId)) {
                is Result.Success -> {
                    _actionState.value = BorrowingActionState.Success(result.data)
                    loadBorrowings() // Refresh list
                }
                is Result.Error -> {
                    _actionState.value = BorrowingActionState.Error(result.message)
                }
                is Result.Loading -> {
                    _actionState.value = BorrowingActionState.Loading
                }
            }
        }
    }
    
    suspend fun verifyReturnQRCode(borrowingId: String, scannedQRCode: String): Result<com.mylab.qrscanner.data.model.LabItem> {
        return borrowingRepository.verifyReturnQRCode(borrowingId, scannedQRCode)
    }
    
    fun approveReturn(borrowingId: String, verifiedItemId: String) {
        viewModelScope.launch {
            _actionState.value = BorrowingActionState.Loading
            when (val result = borrowingRepository.approveReturn(borrowingId, verifiedItemId)) {
                is Result.Success -> {
                    _actionState.value = BorrowingActionState.Success(result.data)
                    loadBorrowings() // Refresh list
                }
                is Result.Error -> {
                    _actionState.value = BorrowingActionState.Error(result.message)
                }
                is Result.Loading -> {
                    _actionState.value = BorrowingActionState.Loading
                }
            }
        }
    }
    
    fun rejectReturn(borrowingId: String) {
        viewModelScope.launch {
            _actionState.value = BorrowingActionState.Loading
            when (val result = borrowingRepository.rejectReturn(borrowingId)) {
                is Result.Success -> {
                    _actionState.value = BorrowingActionState.Success(result.data)
                    loadBorrowings() // Refresh list
                }
                is Result.Error -> {
                    _actionState.value = BorrowingActionState.Error(result.message)
                }
                is Result.Loading -> {
                    _actionState.value = BorrowingActionState.Loading
                }
            }
        }
    }
    
    fun requestReturn(borrowingId: String) {
        viewModelScope.launch {
            _actionState.value = BorrowingActionState.Loading
            // Set returnApprovalStatus to pending
            when (val result = borrowingRepository.updateReturnStatus(borrowingId, "pending")) {
                is Result.Success -> {
                    _actionState.value = BorrowingActionState.Success(result.data)
                    loadBorrowings() // Refresh list
                }
                is Result.Error -> {
                    _actionState.value = BorrowingActionState.Error(result.message)
                }
                is Result.Loading -> {
                    _actionState.value = BorrowingActionState.Loading
                }
            }
        }
    }
    
    fun resetActionState() {
        _actionState.value = BorrowingActionState.Idle
    }
}

