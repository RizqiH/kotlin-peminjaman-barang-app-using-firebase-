package com.mylab.qrscanner.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Borrowing(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var itemId: String = "",
    var itemCode: String = "",
    var itemName: String = "",
    var tglPinjam: String = "",
    var tglKembali: String = "",
    var status: String = "dipinjam", // "dipinjam", "dikembalikan"
    var approvalStatus: String = "pending", // "pending", "approved", "rejected"
    var returnApprovalStatus: String? = null, // "pending", "approved", "rejected" (untuk pengembalian)
    var notes: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) {
    constructor() : this(
        id = "",
        userId = "",
        userName = "",
        itemId = "",
        itemCode = "",
        itemName = "",
        tglPinjam = "",
        tglKembali = "",
        status = "dipinjam",
        approvalStatus = "pending",
        returnApprovalStatus = null,
        notes = null,
        createdAt = null,
        updatedAt = null
    )
}

