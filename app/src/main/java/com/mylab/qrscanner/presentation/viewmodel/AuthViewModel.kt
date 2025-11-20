package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.model.User
import com.mylab.qrscanner.data.repository.AuthRepository
import com.mylab.qrscanner.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow<AuthState>(AuthState.Idle)
    val state: StateFlow<AuthState> = _state.asStateFlow()
    
    val currentUser: User?
        get() = authRepository.currentUser?.let {
            viewModelScope.launch {
                authRepository.getUserFromFirestore(it.uid)
            }
            null // Will be updated via state
        }
    
    init {
        checkAuthState()
    }
    
    private fun checkAuthState() {
        viewModelScope.launch {
            if (authRepository.isLoggedIn()) {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _state.value = AuthState.Success(user)
                }
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            when (val result = authRepository.loginWithEmail(email, password)) {
                is Result.Success -> {
                    _state.value = AuthState.Success(result.data)
                }
                is Result.Error -> {
                    _state.value = AuthState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = AuthState.Loading
                }
            }
        }
    }
    
    fun register(email: String, password: String, nama: String, role: String = "mahasiswa") {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            when (val result = authRepository.registerWithEmail(email, password, nama, role)) {
                is Result.Success -> {
                    _state.value = AuthState.Success(result.data)
                }
                is Result.Error -> {
                    _state.value = AuthState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = AuthState.Loading
                }
            }
        }
    }
    
    fun resetPassword(email: String) {
        viewModelScope.launch {
            _state.value = AuthState.Loading
            when (val result = authRepository.resetPassword(email)) {
                is Result.Success -> {
                    _state.value = AuthState.Success(User()) // Temporary success state
                }
                is Result.Error -> {
                    _state.value = AuthState.Error(result.message)
                }
                is Result.Loading -> {
                    _state.value = AuthState.Loading
                }
            }
        }
    }
    
    fun logout() {
        authRepository.logout()
        _state.value = AuthState.Idle
    }
    
    fun resetState() {
        _state.value = AuthState.Idle
    }
}

