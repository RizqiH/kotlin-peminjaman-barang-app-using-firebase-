package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.local.HistoryManager
import com.mylab.qrscanner.data.model.ScanHistory
import com.mylab.qrscanner.data.model.Borrowing
import com.mylab.qrscanner.data.repository.LabItemRepository
import com.mylab.qrscanner.data.repository.BorrowingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardState {
    object Loading : DashboardState()
    data class Success(
        val totalScans: Int,
        val recentScans: List<ScanHistory>,
        val stats: Map<String, Int>,
        val activeBorrowings: List<Borrowing> = emptyList()
    ) : DashboardState()
    data class Error(val message: String) : DashboardState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val historyManager: HistoryManager,
    private val repository: LabItemRepository,
    private val borrowingRepository: BorrowingRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val state: StateFlow<DashboardState> = _state.asStateFlow()
    
    init {
        loadDashboardData()
    }
    
    fun loadDashboardData(loadBorrowings: Boolean = false) {
        viewModelScope.launch {
            try {
                // Get scan history
                val scanHistory = historyManager.getHistory()
                val totalScans = scanHistory.size
                val recentScans = scanHistory.take(5)
                
                // Get stats from repository (now suspend function)
                val stats = repository.getStats()
                
                // Load active borrowings if requested (for petugas)
                val activeBorrowings = if (loadBorrowings) {
                    when (val result = borrowingRepository.getActiveBorrowings()) {
                        is com.mylab.qrscanner.data.repository.Result.Success -> result.data
                        else -> emptyList()
                    }
                } else {
                    emptyList()
                }
                
                _state.value = DashboardState.Success(
                    totalScans = totalScans,
                    recentScans = recentScans,
                    stats = stats,
                    activeBorrowings = activeBorrowings
                )
            } catch (e: Exception) {
                _state.value = DashboardState.Error(e.message ?: "Failed to load dashboard data")
            }
        }
    }
    
    fun refresh() {
        loadDashboardData()
    }
    
    fun refreshWithBorrowings() {
        loadDashboardData(loadBorrowings = true)
    }
}

