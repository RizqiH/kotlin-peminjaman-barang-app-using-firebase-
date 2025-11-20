package com.mylab.qrscanner.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mylab.qrscanner.data.model.Borrowing
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BorrowingRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val borrowingsCollection = firestore.collection("peminjaman")
    private val itemsCollection = firestore.collection("lab_items")
    
    suspend fun createBorrowing(borrowing: Borrowing): Result<Borrowing> {
        return try {
            // Check stock availability (but don't decrease yet - wait for approval)
            val itemDoc = itemsCollection.document(borrowing.itemId).get().await()
            if (!itemDoc.exists()) {
                return Result.Error("Item not found")
            }
            
            val currentStock = itemDoc.getLong("stok")?.toInt() ?: 0
            if (currentStock <= 0) {
                return Result.Error("Stock tidak tersedia")
            }
            
            // Don't decrease stock yet - wait for approval
            val borrowingData = hashMapOf(
                "userId" to borrowing.userId,
                "userName" to borrowing.userName,
                "itemId" to borrowing.itemId,
                "itemCode" to borrowing.itemCode,
                "itemName" to borrowing.itemName,
                "tglPinjam" to borrowing.tglPinjam,
                "tglKembali" to borrowing.tglKembali,
                "status" to borrowing.status,
                "approvalStatus" to borrowing.approvalStatus,
                "returnApprovalStatus" to (borrowing.returnApprovalStatus ?: ""),
                "notes" to (borrowing.notes ?: ""),
                "createdAt" to (borrowing.createdAt ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())),
                "updatedAt" to (borrowing.updatedAt ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            )
            
            val docRef = borrowingsCollection.add(borrowingData).await()
            
            val createdBorrowing = borrowing.copy(
                id = docRef.id,
                createdAt = borrowing.createdAt ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                updatedAt = borrowing.updatedAt ?: SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            
            Result.Success(createdBorrowing)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create borrowing")
        }
    }
    
    suspend fun returnBorrowing(borrowingId: String, verifiedItemId: String? = null): Result<Borrowing> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (doc.exists()) {
                val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
                if (borrowing != null) {
                    // Verify item ID if provided
                    if (verifiedItemId != null && borrowing.itemId != verifiedItemId) {
                        return Result.Error("QR Code tidak cocok dengan barang yang dipinjam")
                    }
                    
                    // Increase stock
                    val itemDoc = itemsCollection.document(borrowing.itemId).get().await()
                    if (itemDoc.exists()) {
                        val currentStock = itemDoc.getLong("stok")?.toInt() ?: 0
                        itemsCollection.document(borrowing.itemId).update("stok", currentStock + 1).await()
                    }
                    
                    val updatedBorrowing = borrowing.copy(
                        status = "dikembalikan",
                        tglKembali = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                        updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )
                    borrowingsCollection.document(borrowingId).set(updatedBorrowing).await()
                    Result.Success(updatedBorrowing)
                } else {
                    Result.Error("Borrowing not found")
                }
            } else {
                Result.Error("Borrowing not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to return borrowing")
        }
    }
    
    suspend fun approveBorrowing(borrowingId: String): Result<Borrowing> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (doc.exists()) {
                val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
                if (borrowing != null) {
                    // Decrease stock when approved
                    val itemDoc = itemsCollection.document(borrowing.itemId).get().await()
                    if (itemDoc.exists()) {
                        val currentStock = itemDoc.getLong("stok")?.toInt() ?: 0
                        if (currentStock <= 0) {
                            return Result.Error("Stock tidak tersedia")
                        }
                        itemsCollection.document(borrowing.itemId).update("stok", currentStock - 1).await()
                    }
                    
                    val updatedBorrowing = borrowing.copy(
                        approvalStatus = "approved",
                        status = "dipinjam", // Pastikan status tetap "dipinjam" setelah approve
                        updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )
                    // Gunakan update() untuk hanya update field yang berubah, bukan replace seluruh document
                    borrowingsCollection.document(borrowingId).update(
                        "approvalStatus", "approved",
                        "status", "dipinjam",
                        "updatedAt", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    ).await()
                    Result.Success(updatedBorrowing)
                } else {
                    Result.Error("Borrowing not found")
                }
            } else {
                Result.Error("Borrowing not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to approve borrowing")
        }
    }
    
    suspend fun rejectBorrowing(borrowingId: String): Result<Borrowing> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (doc.exists()) {
                val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
                if (borrowing != null) {
                    val updatedBorrowing = borrowing.copy(
                        approvalStatus = "rejected",
                        updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )
                    borrowingsCollection.document(borrowingId).set(updatedBorrowing).await()
                    Result.Success(updatedBorrowing)
                } else {
                    Result.Error("Borrowing not found")
                }
            } else {
                Result.Error("Borrowing not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to reject borrowing")
        }
    }
    
    suspend fun verifyReturnQRCode(borrowingId: String, scannedQRCode: String): Result<com.mylab.qrscanner.data.model.LabItem> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (!doc.exists()) {
                return Result.Error("Data peminjaman tidak ditemukan")
            }
            
            val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
            if (borrowing == null) {
                return Result.Error("Data peminjaman tidak valid")
            }
            
            // Verify QR code - bisa berisi itemId atau itemCode
            var isValid = false
            var actualItemId = borrowing.itemId
            val trimmedQRCode = scannedQRCode.trim()
            
            // Debug: Log untuk troubleshooting
            android.util.Log.d("ReturnVerification", "Borrowing ItemId: ${borrowing.itemId}, Scanned QR: $trimmedQRCode")
            
            // Cek apakah QR code adalah itemId (Firestore document ID)
            if (borrowing.itemId == trimmedQRCode) {
                isValid = true
                android.util.Log.d("ReturnVerification", "QR Code cocok sebagai itemId")
            } else {
                // Jika tidak cocok, cek apakah QR code adalah itemCode
                android.util.Log.d("ReturnVerification", "Mencari item berdasarkan itemCode: $trimmedQRCode")
                val itemByCodeSnapshot = itemsCollection
                    .whereEqualTo("itemCode", trimmedQRCode)
                    .limit(1)
                    .get()
                    .await()
                
                if (!itemByCodeSnapshot.isEmpty) {
                    val foundItemId = itemByCodeSnapshot.documents.first().id
                    android.util.Log.d("ReturnVerification", "Item ditemukan dengan itemCode, ItemId: $foundItemId, Borrowing ItemId: ${borrowing.itemId}")
                    // Bandingkan dengan itemId di borrowing
                    if (foundItemId == borrowing.itemId) {
                        isValid = true
                        actualItemId = foundItemId
                        android.util.Log.d("ReturnVerification", "QR Code cocok sebagai itemCode")
                    } else {
                        android.util.Log.e("ReturnVerification", "ItemId tidak cocok: Found=$foundItemId, Expected=${borrowing.itemId}")
                        return Result.Error("QR Code tidak cocok dengan barang yang dipinjam. Item code sama tapi item berbeda.")
                    }
                } else {
                    android.util.Log.e("ReturnVerification", "Item tidak ditemukan dengan itemCode: $trimmedQRCode")
                    return Result.Error("QR Code tidak cocok dengan barang yang dipinjam. Expected Item ID: ${borrowing.itemId}, Expected Item Code: ${borrowing.itemCode}, Scanned: $trimmedQRCode")
                }
            }
            
            if (!isValid) {
                return Result.Error("QR Code tidak cocok dengan barang yang dipinjam")
            }
            
            // Get item details
            val itemDoc = itemsCollection.document(actualItemId).get().await()
            if (!itemDoc.exists()) {
                return Result.Error("Barang tidak ditemukan di database")
            }
            
            val item = itemDoc.toObject(com.mylab.qrscanner.data.model.LabItem::class.java)?.copy(id = itemDoc.id)
            if (item == null) {
                return Result.Error("Data barang tidak valid")
            }
            
            Result.Success(item)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to verify QR code: ${e.localizedMessage}")
        }
    }
    
    suspend fun approveReturn(borrowingId: String, verifiedItemId: String): Result<Borrowing> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (!doc.exists()) {
                return Result.Error("Data peminjaman tidak ditemukan")
            }
            
            val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
            if (borrowing == null) {
                return Result.Error("Data peminjaman tidak valid")
            }
            
            // Verify QR code - bisa berisi itemId atau itemCode
            var isValid = false
            var actualItemId = borrowing.itemId
            
            // Cek apakah QR code adalah itemId (Firestore document ID)
            if (borrowing.itemId == verifiedItemId.trim()) {
                isValid = true
            } else {
                // Jika tidak cocok, cek apakah QR code adalah itemCode
                // Cari item berdasarkan itemCode
                val itemByCodeSnapshot = itemsCollection
                    .whereEqualTo("itemCode", verifiedItemId.trim())
                    .limit(1)
                    .get()
                    .await()
                
                if (!itemByCodeSnapshot.isEmpty) {
                    val foundItemId = itemByCodeSnapshot.documents.first().id
                    // Bandingkan dengan itemId di borrowing
                    if (foundItemId == borrowing.itemId) {
                        isValid = true
                        actualItemId = foundItemId
                    } else {
                        // ItemCode cocok tapi itemId berbeda
                        return Result.Error("QR Code tidak cocok dengan barang yang dipinjam. Item code sama tapi item berbeda.")
                    }
                } else {
                    // QR code tidak cocok dengan itemId maupun itemCode
                    return Result.Error("QR Code tidak cocok dengan barang yang dipinjam. Expected Item ID: ${borrowing.itemId}, Expected Item Code: ${borrowing.itemCode}, Scanned: ${verifiedItemId.trim()}")
                }
            }
            
            if (!isValid) {
                return Result.Error("QR Code tidak cocok dengan barang yang dipinjam")
            }
            
            // Check if item exists in Firestore
            val itemDoc = itemsCollection.document(actualItemId).get().await()
            if (!itemDoc.exists()) {
                return Result.Error("Barang tidak ditemukan di database. Item ID: $actualItemId")
            }
            
            // Increase stock
            val currentStock = itemDoc.getLong("stok")?.toInt() ?: 0
            itemsCollection.document(actualItemId).update("stok", currentStock + 1).await()
            
            // Update borrowing status
            val updatedBorrowing = borrowing.copy(
                status = "dikembalikan",
                returnApprovalStatus = "approved",
                tglKembali = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )
            borrowingsCollection.document(borrowingId).set(updatedBorrowing).await()
            Result.Success(updatedBorrowing)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to approve return: ${e.localizedMessage}")
        }
    }
    
    suspend fun rejectReturn(borrowingId: String): Result<Borrowing> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (doc.exists()) {
                val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
                if (borrowing != null) {
                    val updatedBorrowing = borrowing.copy(
                        returnApprovalStatus = "rejected",
                        updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )
                    borrowingsCollection.document(borrowingId).set(updatedBorrowing).await()
                    Result.Success(updatedBorrowing)
                } else {
                    Result.Error("Borrowing not found")
                }
            } else {
                Result.Error("Borrowing not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to reject return")
        }
    }
    
    suspend fun updateReturnStatus(borrowingId: String, status: String): Result<Borrowing> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (doc.exists()) {
                val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
                if (borrowing != null) {
                    val updatedBorrowing = borrowing.copy(
                        returnApprovalStatus = status,
                        updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )
                    borrowingsCollection.document(borrowingId).set(updatedBorrowing).await()
                    Result.Success(updatedBorrowing)
                } else {
                    Result.Error("Borrowing not found")
                }
            } else {
                Result.Error("Borrowing not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update return status")
        }
    }
    
    suspend fun getBorrowingById(borrowingId: String): Result<Borrowing> {
        return try {
            val doc = borrowingsCollection.document(borrowingId).get().await()
            if (doc.exists()) {
                val borrowing = doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
                if (borrowing != null) {
                    Result.Success(borrowing)
                } else {
                    Result.Error("Borrowing not found")
                }
            } else {
                Result.Error("Borrowing not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch borrowing")
        }
    }
    
    suspend fun getBorrowingsByUser(userId: String): Result<List<Borrowing>> {
        return try {
            val snapshot = borrowingsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val borrowings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(borrowings)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch borrowings")
        }
    }
    
    suspend fun getAllBorrowings(): Result<List<Borrowing>> {
        return try {
            val snapshot = borrowingsCollection
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val borrowings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(borrowings)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch borrowings")
        }
    }
    
    suspend fun getActiveBorrowings(): Result<List<Borrowing>> {
        return try {
            val snapshot = borrowingsCollection
                .whereEqualTo("status", "dipinjam")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val borrowings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Borrowing::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(borrowings)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch active borrowings")
        }
    }
}

