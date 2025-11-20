package com.mylab.qrscanner.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mylab.qrscanner.data.model.VerificationLog
import com.mylab.qrscanner.data.repository.LabItemRepository
import com.mylab.qrscanner.data.repository.Result
import com.mylab.qrscanner.data.repository.VerificationLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VerificationLogViewModel @Inject constructor(
    private val verificationLogRepository: VerificationLogRepository,
    private val labItemRepository: LabItemRepository
) : ViewModel() {
    
    suspend fun logVerification(
        barcode: String,
        userId: String? = null,
        userName: String? = null
    ): Result<VerificationLog> {
        return try {
            // Verify if barcode exists in database
            val itemResult = labItemRepository.verifyItemByQRCode(barcode)
            
            val log = when (itemResult) {
                is Result.Success -> {
                    val item = itemResult.data
                    VerificationLog(
                        barcode = barcode,
                        itemId = item.id,
                        itemCode = item.itemCode,
                        itemName = item.itemName,
                        status = "valid",
                        waktu = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                        userId = userId,
                        userName = userName
                    )
                }
                is Result.Error -> {
                    VerificationLog(
                        barcode = barcode,
                        status = "invalid",
                        waktu = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                        userId = userId,
                        userName = userName
                    )
                }
                else -> {
                    VerificationLog(
                        barcode = barcode,
                        status = "invalid",
                        waktu = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                        userId = userId,
                        userName = userName
                    )
                }
            }
            
            when (val result = verificationLogRepository.createLog(log)) {
                is Result.Success -> Result.Success(result.data)
                is Result.Error -> Result.Error(result.message)
                is Result.Loading -> Result.Loading
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to log verification")
        }
    }
}

