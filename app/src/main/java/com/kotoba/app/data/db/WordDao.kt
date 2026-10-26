package com.kotoba.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kotoba.app.data.model.WordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM words ORDER BY id ASC")
    fun getAllWords(): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE lvl = :level ORDER BY id ASC")
    fun getWordsByLevel(level: String): Flow<List<WordEntity>>

    @Query("SELECT * FROM words WHERE id = :id LIMIT 1")
    suspend fun getWordById(id: Int): WordEntity?

    @Query("SELECT * FROM words WHERE state != 'new' AND nextReview <= :now AND isSuspended = 0 ORDER BY nextReview ASC")
    suspend fun getDueReviewWords(now: Long = System.currentTimeMillis()): List<WordEntity>

    @Query("SELECT * FROM words WHERE state = 'new' AND isSuspended = 0 LIMIT :limit")
    suspend fun getNewWords(limit: Int): List<WordEntity>

    @Query("SELECT COUNT(*) FROM words")
    suspend fun getWordCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<WordEntity>)

    @Update
    suspend fun updateWord(word: WordEntity)
}
