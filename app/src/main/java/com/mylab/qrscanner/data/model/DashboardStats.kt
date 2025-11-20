package com.mylab.qrscanner.data.model

data class DashboardStats(
    val totalItems: Int = 0,
    val totalScans: Int = 0,
    val goodCondition: Int = 0,
    val needMaintenance: Int = 0,
    val recentScans: List<ScanHistory> = emptyList()
)


