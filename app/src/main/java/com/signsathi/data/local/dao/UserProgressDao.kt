package com.signsathi.data.local.dao


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.signsathi.data.local.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(progress: UserProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(progressList: List<UserProgressEntity>)

    /** Observe all progress rows for a user — emits whenever anything changes. */
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    fun observeProgress(userId: String): Flow<List<UserProgressEntity>>

    /** Get all progress rows for a user (one-shot, not a Flow). */
    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    suspend fun getProgress(userId: String): List<UserProgressEntity>

    /** Get the STATS row specifically. */
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND lessonId = 'STATS' LIMIT 1")
    suspend fun getStatsRow(userId: String): UserProgressEntity?

    /** Get the status of a specific lesson for a user. */
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND lessonId = :lessonId LIMIT 1")
    suspend fun getLessonProgress(userId: String, lessonId: String): UserProgressEntity?

    /** Get all rows that have been modified locally but not yet synced to DynamoDB. */
    @Query("SELECT * FROM user_progress WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedRows(userId: String): List<UserProgressEntity>

    /** Mark a row as synced after WorkManager successfully pushes it. */
    @Query("UPDATE user_progress SET isSynced = 1 WHERE userId = :userId AND lessonId = :lessonId")
    suspend fun markAsSynced(userId: String, lessonId: String)

    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)

    @Query("SELECT * FROM user_progress WHERE userId = :userId AND lessonId = 'STATS' LIMIT 1")
    fun observeStats(userId: String): Flow<UserProgressEntity?>
}