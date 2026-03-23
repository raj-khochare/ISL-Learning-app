package com.signsathi.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.signsathi.data.local.entity.LessonNodeEntity
import com.signsathi.data.local.entity.LessonUnitEntity

/**
 * Room one-to-many relation.
 * Lets us query a unit and all its lessons in a single DB call
 * using @Transaction in LessonDao.
 */
data class UnitWithLessons(
    @Embedded
    val unit: LessonUnitEntity,

    @Relation(
        parentColumn = "unitId",
        entityColumn = "unitId"
    )
    val lessons: List<LessonNodeEntity>
)

























