package com.kotoba.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kotoba.app.data.model.ExampleSentence
import com.kotoba.app.data.model.FormulaRule
import com.kotoba.app.data.model.GrammarConverters
import com.kotoba.app.data.model.GrammarEntity
import com.kotoba.app.data.model.WordConverters
import com.kotoba.app.data.model.WordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

@Database(entities = [WordEntity::class, GrammarEntity::class], version = 1, exportSchema = false)
@TypeConverters(WordConverters::class, GrammarConverters::class)
abstract class KotobaDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao
    abstract fun grammarDao(): GrammarDao

    companion object {
        @Volatile
        private var INSTANCE: KotobaDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): KotobaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KotobaDatabase::class.java,
                    "kotoba_database"
                )
                    .addCallback(DatabaseCallback(context, scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(context, database)
                    }
                }
            }
        }

        suspend fun populateInitialData(context: Context, database: KotobaDatabase) {
            try {
                val gson = Gson()

                // 1. Populate Vocabulary N5 & N4
                context.assets.open("data/vocab_n5_n4.json").use { stream ->
                    val reader = InputStreamReader(stream)
                    val rawListType = object : TypeToken<List<RawWordJson>>() {}.type
                    val rawWords: List<RawWordJson> = gson.fromJson(reader, rawListType)
                    val words = rawWords.map { raw ->
                        WordEntity(
                            id = raw.id,
                            kanji = raw.kanji,
                            kana = raw.kana,
                            rubyJson = gson.toJson(raw.ruby),
                            meanTh = raw.meanTh,
                            meanEn = raw.meanEn,
                            pos = raw.pos,
                            lvl = raw.lvl,
                            moraeJson = gson.toJson(raw.morae),
                            dropMora = raw.dropMora,
                            pitchPattern = raw.pitchPattern,
                            exampleJp = raw.exampleJp,
                            exampleTh = raw.exampleTh,
                            exampleEn = raw.exampleEn,
                            note = raw.note
                        )
                    }
                    database.wordDao().insertAll(words)
                }

                // 2. Populate Grammar Foundations, N5 & N4
                context.assets.open("data/grammar_n5_n4.json").use { stream ->
                    val reader = InputStreamReader(stream)
                    val rawListType = object : TypeToken<List<RawGrammarJson>>() {}.type
                    val rawGrammar: List<RawGrammarJson> = gson.fromJson(reader, rawListType)
                    val grammarList = rawGrammar.map { raw ->
                        GrammarEntity(
                            id = raw.id,
                            title = raw.title,
                            lvl = raw.lvl,
                            groupName = raw.group,
                            meanTh = raw.meanTh,
                            meanEn = raw.meanEn,
                            formulaJson = gson.toJson(raw.formula),
                            nuanceTh = raw.nuanceTh,
                            nuanceEn = raw.nuanceEn,
                            examplesJson = gson.toJson(raw.examples),
                            quizQuestion = raw.quizQuestion,
                            quizChoicesJson = gson.toJson(raw.quizChoices),
                            quizAnswer = raw.quizAnswer
                        )
                    }
                    database.grammarDao().insertAll(grammarList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

private data class RawWordJson(
    val id: Int,
    val kanji: String,
    val kana: String,
    val ruby: List<List<String>>,
    val meanTh: String,
    val meanEn: String,
    val pos: String,
    val lvl: String,
    val morae: List<String>,
    val dropMora: Int,
    val pitchPattern: String,
    val exampleJp: String,
    val exampleTh: String,
    val exampleEn: String,
    val note: String? = null
)

private data class RawGrammarJson(
    val id: String,
    val title: String,
    val lvl: String,
    val group: String,
    val meanTh: String,
    val meanEn: String,
    val formula: List<FormulaRule>,
    val nuanceTh: String,
    val nuanceEn: String,
    val examples: List<ExampleSentence>,
    val quizQuestion: String? = null,
    val quizChoices: List<String>? = null,
    val quizAnswer: Int = 0
)
