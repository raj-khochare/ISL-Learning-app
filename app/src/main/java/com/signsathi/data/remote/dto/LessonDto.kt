package com.signsathi.data.remote.dto

import com.google.gson.annotations.SerializedName

// ─── /lessons response ───────────────────────────────────────────────────────

data class LessonsResponse(
    @SerializedName("units") val units: List<UnitDto>
)

data class UnitDto(
    @SerializedName("unitId")          val unitId: String,
    @SerializedName("unitTitle")       val unitTitle: String,
    @SerializedName("unitDescription") val unitDescription: String,
    @SerializedName("unitOrder")       val unitOrder: Int,
    @SerializedName("lessons")         val lessons: List<LessonDto>
)

data class LessonDto(
    @SerializedName("lessonId")      val lessonId: String,
    @SerializedName("lessonTitle")   val lessonTitle: String,
    @SerializedName("lessonOrder")   val lessonOrder: Int,
    @SerializedName("xpReward")      val xpReward: Int,
    @SerializedName("videoUrl")      val videoUrl: String,
    @SerializedName("thumbnailUrl")  val thumbnailUrl: String
)

// ─── /progress response ──────────────────────────────────────────────────────

data class UserProgressResponse(
    @SerializedName("xp")              val xp: Int,
    @SerializedName("streakDays")      val streakDays: Int,
    @SerializedName("heartsLeft")      val heartsLeft: Int,
    /** Map of lessonId -> status ("completed" | "active") */
    @SerializedName("lessonProgress")  val lessonProgress: Map<String, String>
)