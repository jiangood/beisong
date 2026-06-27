package com.beisong.app.data

import android.content.Context
import androidx.core.content.edit

class ReadingHistory(private val context: Context) {
    private val prefs = context.getSharedPreferences("reading_history", Context.MODE_PRIVATE)

    fun recordOpen(fileName: String) {
        prefs.edit {
            putLong("ts_$fileName", System.currentTimeMillis())
        }
    }

    fun recordProgress(fileName: String, segmentIndex: Int) {
        prefs.edit {
            putInt("seg_$fileName", segmentIndex)
        }
    }

    fun getLastOpened(fileName: String): Long {
        return prefs.getLong("ts_$fileName", 0L)
    }

    fun getLastSegment(fileName: String): Int {
        return prefs.getInt("seg_$fileName", 0)
    }
}
