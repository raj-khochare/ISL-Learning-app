package com.signsathi.data.local.entity

import androidx.room.Entity

/**
 * Stores per-lesson completion state and top-level stats locally.
 *
 * Two types of rows live in this table:
 *
 * 1. Stats row — lessonId = "STATS"
 *    Holds xp, streakDays, heartsLeft, lastActivityDate
 *
 * 2. Per-lesson row — lessonId = actual lesson id (e.g. "lesson_1_1")
 *    Holds status: "locked" | "active" | "completed"
 *
 * Composite primary key = (userId + lessonId), matching DynamoDB exactly.
 */
@Entity(
    tableName = "user_progress",
    primaryKeys = ["userId", "lessonId"]
)
data class UserProgressEntity(
    val userId: String,
    val lessonId: String,           // "STATS" for top-level row, lesson id otherwise
    val status: String = "locked",  // "locked" | "active" | "completed"
    val xp: Int = 0,                // only meaningful on the STATS row
    val streakDays: Int = 0,        // only meaningful on the STATS row
    val heartsLeft: Int = 5,        // only meaningful on the STATS row
    val lastActivityDate: String = "",
    /** True when this row has been modified locally but not yet synced to DynamoDB.
     *  WorkManager will pick up rows where isSynced = false and push them. */
    val isSynced: Boolean = true
)