package com.signsathi.presentation.lesson

// ─── Question types ───────────────────────────────────────────────────────────

enum class QuestionType {
    /** Show a video → user picks the correct sign name from text choices */
    VIDEO_TO_NAME,
    /** Show a word → user picks the correct video from video choices */
    WORD_TO_VIDEO
}

// ─── A single answer choice ───────────────────────────────────────────────────

data class QuizChoice(
    val id       : String,
    val label    : String,
    val videoUrl : String = ""
)

// ─── A single quiz question ───────────────────────────────────────────────────

data class QuizQuestion(
    val type            : QuestionType,
    val signTitle       : String,
    val videoUrl        : String,
    val choices         : List<QuizChoice>,
    val correctChoiceId : String
)

// ─── Lesson phases ────────────────────────────────────────────────────────────

sealed class LessonPhase {
    object Loading : LessonPhase()
    data class Error(val message: String) : LessonPhase()

    /** User is watching / reading the sign */
    object Learn : LessonPhase()

    /** User is answering a quiz question */
    object Quiz : LessonPhase()

    /** Answer has been checked — showing correct/wrong state */
    object Result : LessonPhase()

    /** All questions answered — show completion screen */
    data class Completed(val totalXpEarned: Int) : LessonPhase()
}

// ─── Full UI state ────────────────────────────────────────────────────────────

data class LessonUiState(
    val phase            : LessonPhase     = LessonPhase.Loading,
    val lessonTitle      : String          = "",
    val description      : String          = "",
    val videoUrl         : String          = "",
    val xpReward         : Int             = 0,

    // Quiz state
    val questions        : List<QuizQuestion> = emptyList(),
    val currentIndex     : Int             = 0,
    val selectedChoiceId : String?         = null,
    val isAnswerChecked  : Boolean         = false,
    val isCorrect        : Boolean         = false,
    val xpEarnedSoFar    : Int             = 0,
) {
    /** 0f → 1f progress through the quiz questions */
    val progress: Float
        get() = if (questions.isEmpty()) 0f
        else currentIndex.toFloat() / questions.size.toFloat()

    val currentQuestion: QuizQuestion?
        get() = questions.getOrNull(currentIndex)
}