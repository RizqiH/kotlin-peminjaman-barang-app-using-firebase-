package com.mylab.qrscanner.data.model

import java.text.SimpleDateFormat
import java.util.*

data class ScanHistory(
    val id: String = UUID.randomUUID().toString(),
    val qrCode: String,
    val itemName: String?,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}