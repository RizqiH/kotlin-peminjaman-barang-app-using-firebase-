package com.mylab.qrscanner.data.repository

import com.mylab.qrscanner.data.api.ApiService
import com.mylab.qrscanner.data.model.LabItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

@Singleton
class LabItemRepository @Inject constructor(
    private val apiService: ApiService,
    private val mockRepository: MockLabItemRepository,
    private val firestoreRepository: FirestoreRepository
) {
    
    // Firebase is now the primary data source
    private val useFirebase = true
    private val useMockData = false  // Disabled - using Firebase only
    
    suspend fun verifyItemByQRCode(qrCode: String): Result<LabItem> {
        return when {
            useFirebase -> {
                firestoreRepository.getItemByQRCode(qrCode)
            }
            useMockData -> {
                mockRepository.verifyItemByQRCode(qrCode)
            }
            else -> {
                withContext(Dispatchers.IO) {
                    try {
                        val response = apiService.verifyItemByQRCode(qrCode)
                        
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true && apiResponse.data != null) {
                                Result.Success(apiResponse.data)
                            } else {
                                Result.Error(apiResponse?.message ?: "Item tidak ditemukan")
                            }
                        } else {
                            Result.Error("Error: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        Result.Error(e.message ?: "Terjadi kesalahan koneksi")
                    }
                }
            }
        }
    }
    
    suspend fun getItemById(itemId: String): Result<LabItem> {
        return when {
            useFirebase -> {
                firestoreRepository.getItemById(itemId)
            }
            useMockData -> {
                mockRepository.getItemById(itemId)
            }
            else -> {
                withContext(Dispatchers.IO) {
                    try {
                        val response = apiService.getItemById(itemId)
                        
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true && apiResponse.data != null) {
                                Result.Success(apiResponse.data)
                            } else {
                                Result.Error(apiResponse?.message ?: "Item tidak ditemukan")
                            }
                        } else {
                            Result.Error("Error: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        Result.Error(e.message ?: "Terjadi kesalahan koneksi")
                    }
                }
            }
        }
    }

    suspend fun getAllItems(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
        condition: String? = null
    ): Result<List<LabItem>> {
        return when {
            useFirebase -> {
                firestoreRepository.getAllItems(category, condition)
            }
            useMockData -> {
                mockRepository.getAllItems(page, limit, category, condition)
            }
            else -> {
                withContext(Dispatchers.IO) {
                    try {
                        val response = apiService.getAllItems(page, limit, category, condition)
                        
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true && apiResponse.data != null) {
                                Result.Success(apiResponse.data)
                            } else {
                                Result.Error(apiResponse?.message ?: "Gagal memuat data")
                            }
                        } else {
                            Result.Error("Error: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        Result.Error(e.message ?: "Terjadi kesalahan koneksi")
                    }
                }
            }
        }
    }
    
    suspend fun createItem(item: LabItem): Result<LabItem> {
        return when {
            useFirebase -> {
                firestoreRepository.createItem(item)
            }
            useMockData -> {
                mockRepository.createItem(item)
            }
            else -> {
                withContext(Dispatchers.IO) {
                    try {
                        val response = apiService.createItem(item)
                        
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true && apiResponse.data != null) {
                                Result.Success(apiResponse.data)
                            } else {
                                Result.Error(apiResponse?.message ?: "Gagal menambah item")
                            }
                        } else {
                            Result.Error("Error: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        Result.Error(e.message ?: "Terjadi kesalahan koneksi")
                    }
                }
            }
        }
    }
    
    suspend fun updateItem(itemId: String, item: LabItem): Result<LabItem> {
        return when {
            useFirebase -> {
                firestoreRepository.updateItem(itemId, item)
            }
            useMockData -> {
                mockRepository.updateItem(itemId, item)
            }
            else -> {
                withContext(Dispatchers.IO) {
                    try {
                        val response = apiService.updateItem(itemId, item)
                        
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true && apiResponse.data != null) {
                                Result.Success(apiResponse.data)
                            } else {
                                Result.Error(apiResponse?.message ?: "Gagal mengupdate item")
                            }
                        } else {
                            Result.Error("Error: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        Result.Error(e.message ?: "Terjadi kesalahan koneksi")
                    }
                }
            }
        }
    }
    
    suspend fun deleteItem(itemId: String): Result<Unit> {
        return when {
            useFirebase -> {
                firestoreRepository.deleteItem(itemId)
            }
            useMockData -> {
                mockRepository.deleteItem(itemId)
            }
            else -> {
                withContext(Dispatchers.IO) {
                    try {
                        val response = apiService.deleteItem(itemId)
                        
                        if (response.isSuccessful) {
                            val apiResponse = response.body()
                            if (apiResponse?.success == true) {
                                Result.Success(Unit)
                            } else {
                                Result.Error(apiResponse?.message ?: "Gagal menghapus item")
                            }
                        } else {
                            Result.Error("Error: ${response.code()} - ${response.message()}")
                        }
                    } catch (e: Exception) {
                        Result.Error(e.message ?: "Terjadi kesalahan koneksi")
                    }
                }
            }
        }
    }
    
    suspend fun getStats(): Map<String, Int> {
        return when {
            useFirebase -> {
                firestoreRepository.getStats()
            }
            useMockData -> {
                mockRepository.getStats()
            }
            else -> {
                mapOf(
                    "total" to 0,
                    "good" to 0,
                    "maintenance" to 0,
                    "broken" to 0
                )
            }
        }
    }
}




