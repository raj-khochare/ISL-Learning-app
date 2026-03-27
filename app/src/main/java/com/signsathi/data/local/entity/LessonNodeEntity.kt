package com.signsathi.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Cached representation of a single lesson node.
 * Foreign key links each lesson to its parent unit.
 * Index on unitId speeds up queries like "get all lessons for unit_1".
 */
@Entity(
    tableName = "lesson_nodes",
    foreignKeys = [
        ForeignKey(
            entity = LessonUnitEntity::class,
            parentColumns = ["unitId"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE   // deleting a unit deletes all its lessons
        )
    ],
    indices = [Index(value = ["unitId"])]
)
data class LessonNodeEntity(
    val unitId: String,
    val lessonId: String,
    val lessonTitle: String,
    val description  : String = "",
    val lessonOrder: Int,
    val xpReward: Int,
    val videoUrl: String,
    val thumbnailUrl: String
) {
    // Room requires a single @PrimaryKey — lessonId is globally unique
    @androidx.room.PrimaryKey
    var id: String = lessonId
}
