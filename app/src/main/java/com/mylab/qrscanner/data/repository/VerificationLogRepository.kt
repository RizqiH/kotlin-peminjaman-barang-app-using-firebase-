package com.mylab.qrscanner.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.mylab.qrscanner.data.model.VerificationLog
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VerificationLogRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val logsCollection = firestore.collection("log_verifikasi")
    
    suspend fun createLog(log: VerificationLog): Result<VerificationLog> {
        return try {
            val logData = hashMapOf(
                "barcode" to log.barcode,
                "itemId" to (log.itemId ?: ""),
                "itemCode" to (log.itemCode ?: ""),
                "itemName" to (log.itemName ?: ""),
                "status" to log.status,
                "waktu" to (log.waktu.ifEmpty { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) }),
                "userId" to (log.userId ?: ""),
                "userName" to (log.userName ?: ""),
                "notes" to (log.notes ?: "")
            )
            
            val docRef = logsCollection.add(logData).await()
            
            val createdLog = log.copy(
                id = docRef.id,
                waktu = log.waktu.ifEmpty { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) }
            )
            
            Result.Success(createdLog)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create verification log")
        }
    }
    
    suspend fun getLogsByUser(userId: String): Result<List<VerificationLog>> {
        return try {
            val snapshot = logsCollection
                .whereEqualTo("userId", userId)
                .orderBy("waktu", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val logs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(VerificationLog::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(logs)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch logs")
        }
    }
    
    suspend fun getAllLogs(): Result<List<VerificationLog>> {
        return try {
            val snapshot = logsCollection
                .orderBy("waktu", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .await()
            
            val logs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(VerificationLog::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(logs)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch logs")
        }
    }
    
    suspend fun getLogsByBarcode(barcode: String): Result<List<VerificationLog>> {
        return try {
            val snapshot = logsCollection
                .whereEqualTo("barcode", barcode)
                .orderBy("waktu", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val logs = snapshot.documents.mapNotNull { doc ->
                doc.toObject(VerificationLog::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(logs)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch logs")
        }
    }
}

