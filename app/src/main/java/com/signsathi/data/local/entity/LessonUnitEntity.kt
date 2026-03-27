package com.signsathi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached representation of a lesson unit.
 * Maps 1:1 to the unit-level fields returned by the /lessons API.
 */
@Entity(tableName = "lesson_units")
data class LessonUnitEntity(
    @PrimaryKey
    val unitId: String,
    val unitTitle: String,
    val unitDescription: String,
    val unitOrder: Int,
    /** Timestamp of when this row was last fetched from the API (epoch millis).
     *  Used to decide when to refresh — we refresh if data is older than 1 hour. */
    val lastFetchedAt: Long = 0L
)