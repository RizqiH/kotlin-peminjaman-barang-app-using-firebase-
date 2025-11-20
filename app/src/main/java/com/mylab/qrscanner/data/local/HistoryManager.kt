package com.mylab.qrscanner.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mylab.qrscanner.data.model.ScanHistory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "qr_scan_history",
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    private val historyKey = "scan_history_list"
    
    fun saveHistory(scanHistory: ScanHistory) {
        val currentHistory = getHistory().toMutableList()
        // Add to beginning of list
        currentHistory.add(0, scanHistory)
        // Keep only last 100 scans
        if (currentHistory.size > 100) {
            currentHistory.removeAt(currentHistory.size - 1)
        }
        
        val json = gson.toJson(currentHistory)
        prefs.edit().putString(historyKey, json).apply()
    }
    
    fun getHistory(): List<ScanHistory> {
        val json = prefs.getString(historyKey, null) ?: return emptyList()
        val type = object : TypeToken<List<ScanHistory>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun clearHistory() {
        prefs.edit().remove(historyKey).apply()
    }
    
    fun deleteHistoryItem(id: String) {
        val currentHistory = getHistory().toMutableList()
        currentHistory.removeIf { history -> history.id == id }
        val json = gson.toJson(currentHistory)
        prefs.edit().putString(historyKey, json).apply()
    }
}
