package com.signsathi.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.signsathi.data.local.entity.LessonNodeEntity
import com.signsathi.data.local.entity.LessonUnitEntity
import com.signsathi.data.local.relation.UnitWithLessons
import kotlinx.coroutines.flow.Flow

@Dao
interface LessonDao {

    // ── Units ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<LessonUnitEntity>)

    @Query("SELECT * FROM lesson_units ORDER BY unitOrder ASC")
    fun observeUnits(): Flow<List<LessonUnitEntity>>

    @Query("SELECT * FROM lesson_units ORDER BY unitOrder ASC")
    suspend fun getUnits(): List<LessonUnitEntity>

    @Query("DELETE FROM lesson_units")
    suspend fun deleteAllUnits()

    // ── Nodes ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonNodeEntity>)

    @Query("DELETE FROM lesson_nodes")
    suspend fun deleteAllLessons()

    // ── Units with their lessons (one-to-many relation) ──────────────────────

    @Transaction
    @Query("SELECT * FROM lesson_units ORDER BY unitOrder ASC")
    fun observeUnitsWithLessons(): Flow<List<UnitWithLessons>>

    @Transaction
    @Query("SELECT * FROM lesson_units ORDER BY unitOrder ASC")
    suspend fun getUnitsWithLessons(): List<UnitWithLessons>

    // ── Cache freshness ───────────────────────────────────────────────────────

    /** Returns the oldest lastFetchedAt across all units.
     *  If null, the cache is empty. */
    @Query("SELECT MIN(lastFetchedAt) FROM lesson_units")
    suspend fun getOldestFetchTime(): Long?
}