package com.kotoba.app.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kotoba.app.data.model.WordConverters
import com.kotoba.app.data.model.WordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.InputStreamReader

@Database(entities = [WordEntity::class], version = 4, exportSchema = false)
@TypeConverters(WordConverters::class)
abstract class KotobaDatabase : RoomDatabase() {

    abstract fun wordDao(): WordDao

    fun checkAndSeedData(context: Context, scope: CoroutineScope, force: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            try {
                populateInitialData(context, this@KotobaDatabase, force = force)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to check or seed data", e)
            }
        }
    }

    companion object {
        private const val TAG = "KotobaDB"
        private const val VOCAB_ASSET_VERSION = 2 // Full ~3,000-word JLPT N5-N3 edition
        private const val KEY_SYNCED_VOCAB_VERSION = "synced_vocab_asset_version"
        private val syncMutex = Mutex()

        @Volatile
        private var INSTANCE: KotobaDatabase? = null

        /**
         * Non-destructive migration from v3 -> v4:
         * Drops the removed grammar table and adds firstStudiedAt to words while preserving 100% of FSRS progress.
         */
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS grammar")
                db.execSQL("ALTER TABLE words ADD COLUMN firstStudiedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE words SET firstStudiedAt = lastReview WHERE state != 'new' AND lastReview > 0")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): KotobaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KotobaDatabase::class.java,
                    "kotoba_database"
                )
                    .addMigrations(MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context, scope))
                    .build()
                INSTANCE = instance

                // Smart sync: only parses JSON when curriculum version bumps or DB is incomplete
                instance.checkAndSeedData(context, scope, force = false)

                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                scope.launch(Dispatchers.IO) {
                    INSTANCE?.let { database ->
                        populateInitialData(context, database, force = true)
                    }
                }
            }
        }

        /**
         * Smart Non-Destructive Sync:
         * Loads all ~3,000 JLPT N5, N4, and N3 vocabulary items from assets when needed,
         * preserving existing FSRS study state and firstStudiedAt timestamps.
         */
        suspend fun populateInitialData(context: Context, database: KotobaDatabase, force: Boolean = false) {
            syncMutex.withLock {
                try {
                    val prefs = context.applicationContext.getSharedPreferences("kotoba_prefs", Context.MODE_PRIVATE)
                    val syncedVersion = prefs.getInt(KEY_SYNCED_VOCAB_VERSION, 0)
                    val currentCount = database.wordDao().getWordCount()

                    if (!force && syncedVersion == VOCAB_ASSET_VERSION && currentCount >= 2900) {
                        return
                    }

                    val gson = Gson()
                    val existingWordsMap = database.wordDao().getAllWordsOnce().associateBy { it.id }

                    context.assets.open("data/vocab_n5_n3.json").use { stream ->
                        val reader = InputStreamReader(stream, Charsets.UTF_8)
                        val rawListType = object : TypeToken<List<RawWordJson>>() {}.type
                        val rawWords: List<RawWordJson> = gson.fromJson(reader, rawListType)
                        val mergedWords = rawWords.map { raw ->
                            val existing = existingWordsMap[raw.id]
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
                                note = raw.note,
                                state = existing?.state ?: "new",
                                stability = existing?.stability ?: 0.0,
                                difficulty = existing?.difficulty ?: 0.0,
                                elapsedDays = existing?.elapsedDays ?: 0,
                                scheduledDays = existing?.scheduledDays ?: 0,
                                reps = existing?.reps ?: 0,
                                lapses = existing?.lapses ?: 0,
                                lastReview = existing?.lastReview ?: 0L,
                                nextReview = existing?.nextReview ?: 0L,
                                isSuspended = existing?.isSuspended ?: false,
                                firstStudiedAt = existing?.firstStudiedAt ?: 0L
                            )
                        }
                        database.wordDao().insertAll(mergedWords)
                        prefs.edit().putInt(KEY_SYNCED_VOCAB_VERSION, VOCAB_ASSET_VERSION).apply()
                        Log.d(TAG, "Synced ${mergedWords.size} vocabulary items (N5-N3)")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error seeding Kotoba database", e)
                }
            }
        }
    }
}

data class RawWordJson(
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
    val note: String?
)
