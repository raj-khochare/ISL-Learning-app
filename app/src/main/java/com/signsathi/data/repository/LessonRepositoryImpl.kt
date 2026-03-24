package com.signsathi.data.repository

import com.signsathi.data.local.dao.LessonDao
import com.signsathi.data.local.dao.UserProgressDao
import com.signsathi.data.local.entity.UserProgressEntity
import com.signsathi.data.mapper.toDomain
import com.signsathi.data.mapper.toEntities
import com.signsathi.data.mapper.toEntity
import com.signsathi.data.mapper.toUserProgress
import com.signsathi.data.model.LessonUnit
import com.signsathi.data.model.UserProgress
import com.signsathi.data.remote.LessonApiService
import com.signsathi.data.remote.dto.CompleteLessonRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

/**
 * Offline-first implementation of LessonRepository.
 *
 * Pattern: stale-while-revalidate
 * ─────────────────────────────────
 * 1. Room is the single source of truth — the UI always reads from Room.
 * 2. On [refresh], we fetch from the API and write to Room.
 * 3. Room's Flow emits automatically when the write lands,
 *    so the UI updates without any extra wiring.
 * 4. If the device is offline, Room serves the last cached data silently.
 *
 * Cache staleness: we refresh if the cached data is older than CACHE_TTL_MS.
 * The ViewModel calls [refresh] on init and the repository decides
 * whether a network call is actually needed.
 */
class LessonRepositoryImpl @Inject constructor(
    private val lessonDao: LessonDao,
    private val userProgressDao: UserProgressDao,
    private val api: LessonApiService
) : LessonRepository {

    companion object {
        /** Refresh from network if cache is older than 1 hour */
        private const val CACHE_TTL_MS = 60 * 60 * 1000L
    }

    // ─── Observe ──────────────────────────────────────────────────────────────

    /**
     * Combines the units+lessons flow with the progress flow so that
     * NodeState is always computed from the latest of both.
     */
    override fun observeUnits(userId: String): Flow<List<LessonUnit>> =
        combine(
            lessonDao.observeUnitsWithLessons(),
            userProgressDao.observeProgress(userId)
        ) { unitsWithLessons, progressList ->
            val progressMap = progressList
                .filter { it.lessonId != "STATS" }
                .associate { it.lessonId to it.status }

            unitsWithLessons.map { it.toDomain(progressMap) }
        }

    override fun observeUserProgress(userId: String): Flow<UserProgress> =
        userProgressDao.observeProgress(userId).map { rows ->
            val statsRow = rows.find { it.lessonId == "STATS" }
            statsRow?.toUserProgress() ?: UserProgress()
        }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    override suspend fun refresh(userId: String) {
        if (!isCacheStale()) {
            Timber.d("LessonRepository: cache is fresh, skipping network fetch")
            return
        }
        refreshLessons()
        refreshProgress(userId)
    }

    private suspend fun isCacheStale(): Boolean {
        val oldestFetch = lessonDao.getOldestFetchTime() ?: return true
        return System.currentTimeMillis() - oldestFetch > CACHE_TTL_MS
    }

    private suspend fun refreshLessons() {
        try {
            val response = api.getLessons()
            val now = System.currentTimeMillis()

            val unitEntities = response.units.map { it.toEntity(now) }
            val lessonEntities = response.units.flatMap { unit ->
                unit.lessons.map { it.toEntity(unit.unitId) }
            }

            // Replace cache atomically — cascade deletes lesson_nodes too
            lessonDao.deleteAllUnits()
            lessonDao.insertUnits(unitEntities)
            lessonDao.insertLessons(lessonEntities)

            Timber.d("LessonRepository: refreshed ${unitEntities.size} units, ${lessonEntities.size} lessons")
        } catch (e: Exception) {
            Timber.e(e, "LessonRepository: failed to refresh lessons")
        }
    }

    private suspend fun refreshProgress(userId: String) {
        try {
            val response = api.getUserProgress()
            val entities = response.toEntities(userId)
            userProgressDao.insertAll(entities)
            Timber.d("LessonRepository: refreshed progress for $userId")
        } catch (e: Exception) {
            Timber.e(e, "LessonRepository: failed to refresh progress")
        }
    }

    override suspend fun completeLesson(
        userId   : String,
        lessonId : String,
        xpEarned : Int
    ): Result<Int> {
        return try {
            val response = api.completeLesson(
                CompleteLessonRequest(lessonId = lessonId, xpEarned = xpEarned)
            )
            // Update local Room immediately
            userProgressDao.insert(
                UserProgressEntity(
                    userId = userId,
                    lessonId = lessonId,
                    status = "completed",
                    isSynced = true
                )
            )
            val currentStats = userProgressDao.getStatsRow(userId)
            userProgressDao.insert(
                UserProgressEntity(
                    userId     = userId,
                    lessonId   = "STATS",
                    xp         = response.totalXp,
                    streakDays = currentStats?.streakDays ?: 0,
                    heartsLeft = currentStats?.heartsLeft ?: 5,
                    isSynced   = true
                )
            )
            Result.success(response.totalXp)
        } catch (e: Exception) {
            Timber.e(e, "LessonRepository: completeLesson failed")
            Result.failure(e)
        }
    }
}