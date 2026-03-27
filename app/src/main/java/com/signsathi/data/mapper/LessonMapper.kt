package com.signsathi.data.mapper


import com.signsathi.data.local.entity.LessonNodeEntity
import com.signsathi.data.local.entity.LessonUnitEntity
import com.signsathi.data.local.entity.UserProgressEntity
import com.signsathi.data.local.relation.UnitWithLessons
import com.signsathi.data.model.LessonNode
import com.signsathi.data.model.LessonUnit
import com.signsathi.data.model.NodeState
import com.signsathi.data.model.UserProgress
import com.signsathi.data.remote.dto.LessonDto
import com.signsathi.data.remote.dto.UnitDto
import com.signsathi.data.remote.dto.UserProgressResponse
import kotlin.collections.iterator

// ─── DTO → Entity ─────────────────────────────────────────────────────────────

fun UnitDto.toEntity(fetchedAt: Long): LessonUnitEntity =
    LessonUnitEntity(
        unitId          = unitId,
        unitTitle       = unitTitle,
        unitDescription = unitDescription,
        unitOrder       = unitOrder,
        lastFetchedAt   = fetchedAt
    )

fun LessonDto.toEntity(unitId: String): LessonNodeEntity =
    LessonNodeEntity(
        unitId       = unitId,
        lessonId     = lessonId,
        lessonTitle  = lessonTitle,
        description  = description,
        lessonOrder  = lessonOrder,
        xpReward     = xpReward,
        videoUrl     = videoUrl,
        thumbnailUrl = thumbnailUrl
    )

// ─── DTO → Progress Entities ──────────────────────────────────────────────────

fun UserProgressResponse.toEntities(userId: String): List<UserProgressEntity> {
    val entities = mutableListOf<UserProgressEntity>()

    // STATS row
    entities.add(
        UserProgressEntity(
            userId           = userId,
            lessonId         = "STATS",
            xp               = xp,
            streakDays       = streakDays,
            heartsLeft       = heartsLeft,
            isSynced         = true
        )
    )

    // Per-lesson rows
    for ((lessonId, status) in lessonProgress) {
        entities.add(
            UserProgressEntity(
                userId   = userId,
                lessonId = lessonId,
                status   = status,
                isSynced = true
            )
        )
    }

    return entities
}

// ─── Entity → Domain ──────────────────────────────────────────────────────────

/**
 * Converts Room entities into domain models.
 * Merges lesson content (from lesson_nodes) with progress state
 * (from user_progress) to compute the correct NodeState for each lesson.
 *
 * @param progressMap  Map of lessonId -> status string from user_progress table.
 *                     If a lesson has no entry, it defaults to "locked".
 */
fun UnitWithLessons.toDomain(progressMap: Map<String, String>): LessonUnit {
    val sortedLessons = lessons.sortedBy { it.lessonOrder }

    // Determine which lesson is the ActiveContinue node:
    // It's the first lesson that is NOT completed — i.e. the user's current position.
    val firstNonCompletedId = sortedLessons
        .firstOrNull { progressMap[it.lessonId] != "completed" }
        ?.lessonId

    val domainLessons = sortedLessons.map { entity ->
        val status = progressMap[entity.lessonId] ?: "locked"
        val nodeState = when {
            status == "completed"                        -> NodeState.Completed
            entity.lessonId == firstNonCompletedId       -> NodeState.ActiveContinue
            status == "active"                           -> NodeState.Active
            else                                         -> NodeState.Locked
        }
        LessonNode(
            id        = entity.lessonId,
            title     = entity.lessonTitle,
            description = entity.description,
            videoUrl = entity.videoUrl,
            state     = nodeState,
            xpReward  = entity.xpReward
        )
    }

    return LessonUnit(
        id          = unit.unitId,
        title       = unit.unitTitle,
        description = unit.unitDescription,
        nodes       = domainLessons
    )
}

fun UserProgressEntity.toUserProgress(): UserProgress =
    UserProgress(
        xp          = xp,
        streakDays  = streakDays,
        heartsLeft  = heartsLeft
    )