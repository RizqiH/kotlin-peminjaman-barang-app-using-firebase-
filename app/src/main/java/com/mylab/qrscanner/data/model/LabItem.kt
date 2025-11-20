package com.mylab.qrscanner.data.model

import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.gson.annotations.SerializedName

@IgnoreExtraProperties
data class LabItem(
    @SerializedName("id")
    var id: String = "",
    
    @SerializedName("item_code")
    var itemCode: String = "",
    
    @SerializedName("item_name")
    var itemName: String = "",
    
    @SerializedName("category")
    var category: String = "",
    
    @SerializedName("condition")
    var condition: String = "",
    
    @SerializedName("location")
    var location: String = "",
    
    @SerializedName("stok")
    var stok: Int = 0,
    
    @SerializedName("description")
    var description: String? = null,
    
    @SerializedName("created_at")
    var createdAt: String? = null,
    
    @SerializedName("updated_at")
    var updatedAt: String? = null
) {
    // Empty constructor for Firestore
    constructor() : this(
        id = "",
        itemCode = "",
        itemName = "",
        category = "",
        condition = "",
        location = "",
        stok = 0,
        description = null,
        createdAt = null,
        updatedAt = null
    )
}

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("data")
    val data: T?
)





