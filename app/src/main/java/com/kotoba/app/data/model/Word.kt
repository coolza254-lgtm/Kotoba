package com.kotoba.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FuriganaToken(
    val base: String,
    val reading: String?
)

@Entity(tableName = "words")
@TypeConverters(WordConverters::class)
data class WordEntity(
    @PrimaryKey val id: Int,
    val kanji: String,
    val kana: String,
    val rubyJson: String, // Stored as JSON: [["橋", "はし"]]
    val meanTh: String,
    val meanEn: String,
    val pos: String,
    val lvl: String, // N5, N4, or N3
    val moraeJson: String, // e.g. ["は", "し"]
    val dropMora: Int,
    val pitchPattern: String,
    val exampleJp: String,
    val exampleTh: String,
    val exampleEn: String,
    val note: String? = null,

    // FSRS-5 State
    var state: String = "new", // "new", "learning", "review", "relearning"
    var stability: Double = 0.0,
    var difficulty: Double = 0.0,
    var elapsedDays: Int = 0,
    var scheduledDays: Int = 0,
    var reps: Int = 0,
    var lapses: Int = 0,
    var lastReview: Long = 0L,
    var nextReview: Long = 0L,
    var isSuspended: Boolean = false,
    var firstStudiedAt: Long = 0L
) {
    fun getRubyTokens(): List<FuriganaToken> {
        return try {
            val type = object : TypeToken<List<List<String>>>() {}.type
            val raw: List<List<String>> = Gson().fromJson(rubyJson, type)
            raw.map { FuriganaToken(it[0], it.getOrNull(1)?.takeIf { r -> r.isNotEmpty() }) }
        } catch (e: Exception) {
            listOf(FuriganaToken(kanji, kana))
        }
    }

    fun getMorae(): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(moraeJson, type)
        } catch (e: Exception) {
            listOf(kana)
        }
    }
}

class WordConverters {
    @TypeConverter
    fun fromStringList(value: List<String>?): String = Gson().toJson(value ?: emptyList<String>())

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }
}
