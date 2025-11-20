package com.mylab.qrscanner.data.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class VerificationLog(
    var id: String = "",
    var barcode: String = "",
    var itemId: String? = null,
    var itemCode: String? = null,
    var itemName: String? = null,
    var status: String = "invalid", // "valid" or "invalid"
    var waktu: String = "",
    var userId: String? = null,
    var userName: String? = null,
    var notes: String? = null
) {
    constructor() : this(
        id = "",
        barcode = "",
        itemId = null,
        itemCode = null,
        itemName = null,
        status = "invalid",
        waktu = "",
        userId = null,
        userName = null,
        notes = null
    )
}

