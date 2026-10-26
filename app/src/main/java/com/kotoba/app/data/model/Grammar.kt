package com.kotoba.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FormulaRule(
    val tag: String,
    val rule: String
)

data class ExampleSentence(
    val jp: String,
    val th: String,
    val en: String
)

@Entity(tableName = "grammar")
@TypeConverters(GrammarConverters::class)
data class GrammarEntity(
    @PrimaryKey val id: String,
    val title: String,
    val lvl: String, // "base", "N5", "N4"
    val groupName: String,
    val meanTh: String,
    val meanEn: String,
    val formulaJson: String,
    val nuanceTh: String,
    val nuanceEn: String,
    val examplesJson: String,
    val quizQuestion: String? = null,
    val quizChoicesJson: String? = null,
    val quizAnswer: Int = 0,
    var isMastered: Boolean = false
) {
    fun getFormulas(): List<FormulaRule> {
        return try {
            val type = object : TypeToken<List<FormulaRule>>() {}.type
            Gson().fromJson(formulaJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getExamples(): List<ExampleSentence> {
        return try {
            val type = object : TypeToken<List<ExampleSentence>>() {}.type
            Gson().fromJson(examplesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getQuizChoices(): List<String> {
        return try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(quizChoicesJson ?: "[]", type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}

class GrammarConverters {
    @TypeConverter
    fun fromRuleList(value: List<FormulaRule>?): String = Gson().toJson(value ?: emptyList<FormulaRule>())
    @TypeConverter
    fun toRuleList(value: String): List<FormulaRule> {
        val type = object : TypeToken<List<FormulaRule>>() {}.type
        return Gson().fromJson(value, type) ?: emptyList()
    }
}
