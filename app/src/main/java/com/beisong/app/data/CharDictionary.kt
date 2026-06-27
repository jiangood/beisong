package com.beisong.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

data class CharInfo(
    val character: String,
    val pinyin: String,
    val meaning: String
)

class CharDictionary(private val context: Context) {
    private var dict: JSONObject? = null

    suspend fun load() = withContext(Dispatchers.IO) {
        if (dict == null) {
            val json = context.assets.open("chinese_dict.json")
                .bufferedReader().use { it.readText() }
            dict = JSONObject(json)
        }
    }

    fun lookup(char: String): CharInfo? {
        if (char.length != 1) return null
        val obj = dict?.optJSONObject(char) ?: return null
        return CharInfo(
            character = char,
            pinyin = obj.optString("p", ""),
            meaning = obj.optString("m", "")
        )
    }
}
