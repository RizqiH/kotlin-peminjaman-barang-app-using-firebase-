package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.local.HistoryManager
import com.mylab.qrscanner.data.model.ScanHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class HistoryState {
    object Loading : HistoryState()
    data class Success(val historyList: List<ScanHistory>) : HistoryState()
    data class Error(val message: String) : HistoryState()
}

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyManager: HistoryManager
) : ViewModel() {
    
    private val _state = MutableStateFlow<HistoryState>(HistoryState.Loading)
    val state: StateFlow<HistoryState> = _state.asStateFlow()
    
    init {
        loadHistory()
    }
    
    fun loadHistory() {
        viewModelScope.launch {
            try {
                val historyList = historyManager.getHistory()
                _state.value = HistoryState.Success(historyList)
            } catch (e: Exception) {
                _state.value = HistoryState.Error(e.message ?: "Failed to load history")
            }
        }
    }
    
    fun deleteHistoryItem(id: String) {
        viewModelScope.launch {
            try {
                historyManager.deleteHistoryItem(id)
                loadHistory() // Reload after delete
            } catch (e: Exception) {
                _state.value = HistoryState.Error(e.message ?: "Failed to delete item")
            }
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            try {
                historyManager.clearHistory()
                _state.value = HistoryState.Success(emptyList())
            } catch (e: Exception) {
                _state.value = HistoryState.Error(e.message ?: "Failed to clear history")
            }
        }
    }
    
    fun saveHistory(scanHistory: ScanHistory) {
        viewModelScope.launch {
            try {
                historyManager.saveHistory(scanHistory)
                loadHistory() // Reload after save
            } catch (e: Exception) {
                // Silent fail for save, don't update state
            }
        }
    }
}

