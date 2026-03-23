package com.signsathi.data.repository

import com.signsathi.data.model.LessonUnit
import com.signsathi.data.model.UserProgress
import kotlinx.coroutines.flow.Flow

interface LessonRepository {
    /**
     * Emits the full list of units with their lessons whenever
     * the local Room cache changes. The repository handles
     * fetching from the network in the background.
     */
    fun observeUnits(userId: String): Flow<List<LessonUnit>>

    /**
     * Emits the user's current XP, streak and hearts whenever
     * the local Room cache changes.
     */
    fun observeUserProgress(userId: String): Flow<UserProgress>

    /**
     * Force a fresh fetch from the API and update the local cache.
     * Call this on pull-to-refresh or on first app open.
     */
    suspend fun refresh(userId: String)
}
