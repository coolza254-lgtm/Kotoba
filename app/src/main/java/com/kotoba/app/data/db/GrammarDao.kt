package com.kotoba.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kotoba.app.data.model.GrammarEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GrammarDao {
    @Query("SELECT * FROM grammar ORDER BY id ASC")
    fun getAllGrammar(): Flow<List<GrammarEntity>>

    @Query("SELECT * FROM grammar ORDER BY id ASC")
    suspend fun getAllGrammarOnce(): List<GrammarEntity>

    @Query("SELECT * FROM grammar WHERE lvl = :level ORDER BY id ASC")
    fun getGrammarByLevel(level: String): Flow<List<GrammarEntity>>

    @Query("SELECT * FROM grammar WHERE id = :id LIMIT 1")
    suspend fun getGrammarById(id: String): GrammarEntity?

    @Query("SELECT COUNT(*) FROM grammar")
    suspend fun getGrammarCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(grammar: List<GrammarEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewGrammar(grammar: List<GrammarEntity>)

    @Update
    suspend fun updateGrammar(grammar: GrammarEntity)
}
