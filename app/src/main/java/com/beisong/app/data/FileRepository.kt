package com.beisong.app.data

import android.content.Context

class FileRepository(private val context: Context) {

    fun listTextFiles(): List<String> {
        return context.assets.list("")?.filter { it.endsWith(".txt") }?.sorted() ?: emptyList()
    }

    fun loadSegments(fileName: String): List<String> {
        val text = context.assets.open(fileName).bufferedReader().use { it.readText() }
        return text.split(Regex("\\n\\s*\\n"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun displayName(fileName: String): String {
        return fileName.removeSuffix(".txt")
    }
}
