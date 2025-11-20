package com.mylab.qrscanner.data.repository

import com.mylab.qrscanner.data.model.LabItem
import kotlinx.coroutines.delay
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockLabItemRepository @Inject constructor() {
    
    // Mock data storage
    private val mockItems = mutableListOf(
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-001",
            itemName = "Mikroskop Digital",
            category = "Electronics",
            condition = "Good",
            location = "Lab Room 101",
            description = "Mikroskop digital dengan kamera built-in"
        ),
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-002",
            itemName = "Bunsen Burner",
            category = "Chemistry",
            condition = "Good",
            location = "Lab Room 102",
            description = "Pembakar bunsen untuk praktikum kimia"
        ),
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-003",
            itemName = "Centrifuge",
            category = "Biology",
            condition = "Maintenance",
            location = "Lab Room 103",
            description = "Alat sentrifugal untuk pemisahan sampel"
        ),
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-004",
            itemName = "Oscilloscope",
            category = "Electronics",
            condition = "Good",
            location = "Lab Room 104",
            description = "Oscilloscope digital 2 channel"
        ),
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-005",
            itemName = "pH Meter Digital",
            category = "Chemistry",
            condition = "Good",
            location = "Lab Room 102",
            description = "pH meter digital untuk pengukuran keasaman"
        ),
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-006",
            itemName = "Autoclave",
            category = "Biology",
            condition = "Broken",
            location = "Lab Room 103",
            description = "Sterilisator untuk alat-alat laboratorium"
        ),
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-007",
            itemName = "Multimeter",
            category = "Electronics",
            condition = "Good",
            location = "Lab Room 104",
            description = "Multimeter digital untuk pengukuran listrik"
        ),
        LabItem(
            id = UUID.randomUUID().toString(),
            itemCode = "LAB-008",
            itemName = "Beaker Set",
            category = "Chemistry",
            condition = "Good",
            location = "Lab Room 102",
            description = "Set gelas kimia berbagai ukuran"
        )
    )
    
    suspend fun verifyItemByQRCode(qrCode: String): Result<LabItem> {
        delay(500) // Simulate network delay
        
        // Try to find by item code
        val item = mockItems.find { it.itemCode == qrCode }
        
        return if (item != null) {
            Result.Success(item)
        } else {
            // Create dummy item for unknown QR codes
            Result.Success(
                LabItem(
                    id = UUID.randomUUID().toString(),
                    itemCode = qrCode,
                    itemName = "Unknown Item",
                    category = "General",
                    condition = "Good",
                    location = "Unknown",
                    description = "Item scanned with code: $qrCode"
                )
            )
        }
    }
    
    suspend fun getItemById(itemId: String): Result<LabItem> {
        delay(300)
        
        val item = mockItems.find { it.id == itemId }
        
        return if (item != null) {
            Result.Success(item)
        } else {
            Result.Error("Item tidak ditemukan")
        }
    }
    
    suspend fun getAllItems(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
        condition: String? = null
    ): Result<List<LabItem>> {
        delay(500)
        
        var filteredItems = mockItems.toList()
        
        // Filter by category if provided
        if (!category.isNullOrEmpty()) {
            filteredItems = filteredItems.filter { it.category == category }
        }
        
        // Filter by condition if provided
        if (!condition.isNullOrEmpty()) {
            filteredItems = filteredItems.filter { it.condition == condition }
        }
        
        return Result.Success(filteredItems)
    }
    
    suspend fun createItem(item: LabItem): Result<LabItem> {
        delay(500)
        
        val newItem = item.copy(id = UUID.randomUUID().toString())
        mockItems.add(newItem)
        
        return Result.Success(newItem)
    }
    
    suspend fun updateItem(itemId: String, item: LabItem): Result<LabItem> {
        delay(500)
        
        val index = mockItems.indexOfFirst { it.id == itemId }
        
        return if (index != -1) {
            mockItems[index] = item.copy(id = itemId)
            Result.Success(mockItems[index])
        } else {
            Result.Error("Item tidak ditemukan")
        }
    }
    
    suspend fun deleteItem(itemId: String): Result<Unit> {
        delay(300)
        
        val removed = mockItems.removeIf { it.id == itemId }
        
        return if (removed) {
            Result.Success(Unit)
        } else {
            Result.Error("Item tidak ditemukan")
        }
    }
    
    fun getStats(): Map<String, Int> {
        return mapOf(
            "total" to mockItems.size,
            "good" to mockItems.count { it.condition.equals("Good", ignoreCase = true) },
            "maintenance" to mockItems.count { it.condition.equals("Maintenance", ignoreCase = true) },
            "broken" to mockItems.count { it.condition.equals("Broken", ignoreCase = true) }
        )
    }
}


