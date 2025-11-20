package com.mylab.qrscanner.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mylab.qrscanner.data.model.LabItem
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    private val itemsCollection = firestore.collection("lab_items")
    
    suspend fun getAllItems(
        category: String? = null,
        condition: String? = null
    ): Result<List<LabItem>> {
        return try {
            var query: Query = itemsCollection
            
            if (!category.isNullOrEmpty()) {
                query = query.whereEqualTo("category", category)
            }
            
            if (!condition.isNullOrEmpty()) {
                query = query.whereEqualTo("condition", condition)
            }
            
            val snapshot = query.get().await()
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(LabItem::class.java)?.copy(id = doc.id)
            }
            
            Result.Success(items)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch items from Firestore")
        }
    }
    
    suspend fun getItemById(itemId: String): Result<LabItem> {
        return try {
            val doc = itemsCollection.document(itemId).get().await()
            if (doc.exists()) {
                val item = doc.toObject(LabItem::class.java)?.copy(id = doc.id)
                if (item != null) {
                    Result.Success(item)
                } else {
                    Result.Error("Item not found")
                }
            } else {
                Result.Error("Item not found")
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch item from Firestore")
        }
    }
    
    suspend fun getItemByQRCode(qrCode: String): Result<LabItem> {
        return try {
            // Coba cari berdasarkan itemCode dulu
            val snapshot = itemsCollection
                .whereEqualTo("itemCode", qrCode.trim())
                .limit(1)
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                val item = doc.toObject(LabItem::class.java)?.copy(id = doc.id)
                if (item != null) {
                    return Result.Success(item)
                }
            }
            
            // Jika tidak ditemukan berdasarkan itemCode, coba cari berdasarkan document ID
            try {
                val doc = itemsCollection.document(qrCode.trim()).get().await()
                if (doc.exists()) {
                    val item = doc.toObject(LabItem::class.java)?.copy(id = doc.id)
                    if (item != null) {
                        return Result.Success(item)
                    }
                }
            } catch (e: Exception) {
                // Document ID tidak valid, lanjut ke error
            }
            
            Result.Error("Item not found")
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to fetch item from Firestore")
        }
    }
    
    suspend fun createItem(item: LabItem): Result<LabItem> {
        return try {
            // Create item without ID - Firestore will auto-generate document ID
            val itemData = hashMapOf(
                "itemCode" to item.itemCode,
                "itemName" to item.itemName,
                "category" to item.category,
                "condition" to item.condition,
                "location" to item.location,
                "stok" to item.stok,
                "description" to (item.description ?: ""),
                "createdAt" to (item.createdAt ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())),
                "updatedAt" to (item.updatedAt ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()))
            )
            
            // Firestore auto-generates document ID
            val docRef = itemsCollection.add(itemData).await()
            
            // Return created item with auto-generated ID
            val createdItem = LabItem(
                id = docRef.id,  // Auto-generated ID from Firestore
                itemCode = item.itemCode,
                itemName = item.itemName,
                category = item.category,
                condition = item.condition,
                location = item.location,
                description = item.description,
                createdAt = item.createdAt ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date()),
                updatedAt = item.updatedAt ?: java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
            )
            
            Result.Success(createdItem)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to create item in Firestore")
        }
    }
    
    suspend fun updateItem(itemId: String, item: LabItem): Result<LabItem> {
        return try {
            val itemData = item.copy(id = itemId)
            itemsCollection.document(itemId).set(itemData).await()
            Result.Success(itemData)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to update item in Firestore")
        }
    }
    
    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            itemsCollection.document(itemId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to delete item from Firestore")
        }
    }
    
    suspend fun getStats(): Map<String, Int> {
        return try {
            val snapshot = itemsCollection.get().await()
            val items = snapshot.documents.mapNotNull { 
                it.toObject(LabItem::class.java) 
            }
            
            mapOf(
                "total" to items.size,
                "good" to items.count { it.condition.equals("Good", ignoreCase = true) },
                "maintenance" to items.count { it.condition.equals("Maintenance", ignoreCase = true) },
                "broken" to items.count { it.condition.equals("Broken", ignoreCase = true) }
            )
        } catch (e: Exception) {
            mapOf(
                "total" to 0,
                "good" to 0,
                "maintenance" to 0,
                "broken" to 0
            )
        }
    }
}

