package com.signsathi.presentation.practice

// ─── Practice modes ───────────────────────────────────────────────────────────

enum class PracticeMode {
    /** Camera is live — recognizes whatever sign the user performs */
    FREE_RECOGNITION,
    /** App shows a target sign — user performs it — app confirms */
    GUIDED_PRACTICE,
    /** App shows sign name — user attempts — self-rates got it / not yet */
    FLASHCARD
}

// ─── A single sign entry shown in the practice session ────────────────────────

data class PracticeSign(
    val label    : String,
    val category : String,
    val videoUrl : String = ""   // empty until videos are uploaded to S3
)

// ─── Flashcard state ──────────────────────────────────────────────────────────

enum class FlashcardRating { GOT_IT, NOT_YET, UNANSWERED }

data class FlashcardState(
    val sign       : PracticeSign,
    val isRevealed : Boolean       = false,
    val rating     : FlashcardRating = FlashcardRating.UNANSWERED
)

// ─── Guided practice result for a single attempt ─────────────────────────────

enum class AttemptResult { CORRECT, WRONG, WAITING }

// ─── Full practice UI state ───────────────────────────────────────────────────

data class PracticeUiState(
    val mode                : PracticeMode         = PracticeMode.FREE_RECOGNITION,
    val isInitializing      : Boolean              = true,
    val initError           : String?              = null,

    // Recognition state (FREE + GUIDED modes)
    val recognizedLabel     : String?              = null,
    val recognizedCategory  : String?              = null,
    val confidence          : Float                = 0f,
    val isHandDetected      : Boolean              = false,

    // Guided practice state
    val targetSign          : PracticeSign?        = null,
    val attemptResult       : AttemptResult        = AttemptResult.WAITING,
    val guidedScore         : Int                  = 0,
    val guidedTotal         : Int                  = 0,

    // Flashcard state
    val flashcards          : List<FlashcardState> = emptyList(),
    val currentFlashcardIndex: Int                 = 0,
    val flashcardSessionDone : Boolean             = false,

    // Sign list (for mode selection)
    val availableSigns      : List<PracticeSign>   = emptyList(),
    val selectedCategory    : String               = "all",
) {
    val currentFlashcard: FlashcardState?
        get() = flashcards.getOrNull(currentFlashcardIndex)

    val flashcardProgress: Float
        get() = if (flashcards.isEmpty()) 0f
        else currentFlashcardIndex.toFloat() / flashcards.size.toFloat()

    val gotItCount: Int
        get() = flashcards.count { it.rating == FlashcardRating.GOT_IT }

    val notYetCount: Int
        get() = flashcards.count { it.rating == FlashcardRating.NOT_YET }
}