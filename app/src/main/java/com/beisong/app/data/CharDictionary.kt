package com.beisong.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class CharDetail(
    val text: String,
    val book: String
)

data class CharDefinition(
    val meaning: String,
    val details: List<CharDetail>
)

data class CharReading(
    val pinyin: String,
    val definitions: List<CharDefinition>
)

data class CharInfo(
    val character: String,
    val readings: List<CharReading>
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
        val readingsArray = obj.optJSONArray("r") ?: return null

        val readings = mutableListOf<CharReading>()
        for (i in 0 until readingsArray.length()) {
            val readingObj = readingsArray.optJSONObject(i) ?: continue
            val pinyin = readingObj.optString("p", "")
            val defsArray = readingObj.optJSONArray("defs") ?: continue

            val defs = mutableListOf<CharDefinition>()
            for (j in 0 until defsArray.length()) {
                val defObj = defsArray.optJSONObject(j) ?: continue
                val meaning = defObj.optString("m", "")
                val detailsArray = defObj.optJSONArray("d")

                val details = mutableListOf<CharDetail>()
                if (detailsArray != null) {
                    for (k in 0 until detailsArray.length()) {
                        val detailObj = detailsArray.optJSONObject(k) ?: continue
                        details.add(CharDetail(
                            text = detailObj.optString("t", ""),
                            book = detailObj.optString("b", "")
                        ))
                    }
                }
                defs.add(CharDefinition(meaning = meaning, details = details))
            }
            if (defs.isNotEmpty()) {
                readings.add(CharReading(pinyin = pinyin, definitions = defs))
            }
        }
        return if (readings.isEmpty()) null else CharInfo(char, readings)
    }
}
