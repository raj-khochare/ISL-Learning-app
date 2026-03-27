package com.signsathi.presentation.practice

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signsathi.data.recognition.ModelManager
import com.signsathi.data.recognition.SignRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    private val signRecognizer : SignRecognizer,
    private val modelManager   : ModelManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    // Throttle recognition to avoid flooding the UI with rapid label changes
    private var lastRecognitionTime = 0L
    private val RECOGNITION_INTERVAL_MS = 150L

    // Timer job for guided practice feedback reset
    private var feedbackResetJob: Job? = null

    init {
        initialize()
    }

    // ─── Initialization ───────────────────────────────────────────────────────

    private fun initialize() {
        viewModelScope.launch {
            _uiState.update { it.copy(isInitializing = true, initError = null) }
            try {
                // Check for OTA model update in background
                launch { modelManager.checkForUpdates() }

                // Initialize recognizer on IO thread
                withContext(Dispatchers.IO) {
                    signRecognizer.initialize()
                }

                // Build sign list from label map
                val labelMap     = modelManager.getLabelMap()
                val labelsArray  = labelMap.getJSONArray("labels")
                val categoryMap  = labelMap.getJSONObject("category_map")

                val signs = (0 until labelsArray.length()).map { i ->
                    val label    = labelsArray.getString(i)
                    val category = if (categoryMap.has(label))
                        categoryMap.getString(label) else "unknown"
                    PracticeSign(label = label, category = category)
                }

                _uiState.update {
                    it.copy(
                        isInitializing = false,
                        availableSigns = signs
                    )
                }

                Timber.d("PracticeViewModel: initialized with ${signs.size} signs")

            } catch (e: Exception) {
                Timber.e(e, "PracticeViewModel: initialization failed")
                _uiState.update {
                    it.copy(
                        isInitializing = false,
                        initError      = "Failed to load recognition model: ${e.message}"
                    )
                }
            }
        }
    }

    // ─── Mode selection ───────────────────────────────────────────────────────

    fun selectMode(mode: PracticeMode) {
        val state = _uiState.value
        when (mode) {
            PracticeMode.FREE_RECOGNITION -> {
                _uiState.update { it.copy(
                    mode            = mode,
                    recognizedLabel = null,
                    confidence      = 0f
                )}
            }
            PracticeMode.GUIDED_PRACTICE -> {
                val signs = filteredSigns(state).shuffled()
                _uiState.update { it.copy(
                    mode          = mode,
                    targetSign    = signs.firstOrNull(),
                    attemptResult = AttemptResult.WAITING,
                    guidedScore   = 0,
                    guidedTotal   = 0
                )}
            }
            PracticeMode.FLASHCARD -> {
                val flashcards = filteredSigns(state)
                    .shuffled()
                    .map { FlashcardState(sign = it) }
                _uiState.update { it.copy(
                    mode                  = mode,
                    flashcards            = flashcards,
                    currentFlashcardIndex = 0,
                    flashcardSessionDone  = false
                )}
            }
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    private fun filteredSigns(state: PracticeUiState): List<PracticeSign> =
        if (state.selectedCategory == "all") state.availableSigns
        else state.availableSigns.filter { it.category == state.selectedCategory }

    // ─── Camera frame processing ──────────────────────────────────────────────

    /**
     * Called for every camera frame. Throttled to RECOGNITION_INTERVAL_MS.
     * Runs recognition on IO thread, updates UI state on main thread.
     */
    fun onCameraFrame(bitmap: Bitmap) {
        val now = System.currentTimeMillis()
        if (now - lastRecognitionTime < RECOGNITION_INTERVAL_MS) return
        lastRecognitionTime = now

        viewModelScope.launch(Dispatchers.IO) {
            val result = signRecognizer.recognize(bitmap)

            withContext(Dispatchers.Main) {
                val state = _uiState.value

                if (result == null) {
                    _uiState.update { it.copy(
                        isHandDetected  = false,
                        recognizedLabel = null,
                        confidence      = 0f
                    )}
                    return@withContext
                }

                _uiState.update { it.copy(
                    isHandDetected     = true,
                    recognizedLabel    = result.label,
                    recognizedCategory = result.category,
                    confidence         = result.confidence
                )}

                // In guided mode — check if the recognized sign matches the target
                if (state.mode == PracticeMode.GUIDED_PRACTICE) {
                    val target = state.targetSign
                    if (target != null && result.label == target.label
                        && result.confidence >= 0.85f) {
                        onGuidedCorrect()
                    }
                }
            }
        }
    }

    // ─── Guided practice ──────────────────────────────────────────────────────

    private fun onGuidedCorrect() {
        val state = _uiState.value
        if (state.attemptResult == AttemptResult.CORRECT) return  // already registering

        _uiState.update { it.copy(
            attemptResult = AttemptResult.CORRECT,
            guidedScore   = it.guidedScore + 1,
            guidedTotal   = it.guidedTotal + 1
        )}

        feedbackResetJob?.cancel()
        feedbackResetJob = viewModelScope.launch {
            delay(1500)   // show correct feedback for 1.5 seconds
            nextGuidedSign()
        }
    }

    fun onGuidedSkip() {
        _uiState.update { it.copy(
            attemptResult = AttemptResult.WRONG,
            guidedTotal   = it.guidedTotal + 1
        )}
        feedbackResetJob?.cancel()
        feedbackResetJob = viewModelScope.launch {
            delay(800)
            nextGuidedSign()
        }
    }

    private fun nextGuidedSign() {
        val state = _uiState.value
        val signs = filteredSigns(state).shuffled()
        // Pick a different sign than the current one
        val next  = signs.firstOrNull { it.label != state.targetSign?.label }
            ?: signs.firstOrNull()
        _uiState.update { it.copy(
            targetSign    = next,
            attemptResult = AttemptResult.WAITING
        )}
    }

    // ─── Flashcard mode ───────────────────────────────────────────────────────

    fun onFlashcardReveal() {
        _uiState.update { state ->
            val cards = state.flashcards.toMutableList()
            val index = state.currentFlashcardIndex
            if (index < cards.size) {
                cards[index] = cards[index].copy(isRevealed = true)
            }
            state.copy(flashcards = cards)
        }
    }

    fun onFlashcardRate(rating: FlashcardRating) {
        _uiState.update { state ->
            val cards = state.flashcards.toMutableList()
            val index = state.currentFlashcardIndex
            if (index < cards.size) {
                cards[index] = cards[index].copy(rating = rating)
            }
            val nextIndex = index + 1
            val isDone    = nextIndex >= cards.size
            state.copy(
                flashcards             = cards,
                currentFlashcardIndex  = if (isDone) index else nextIndex,
                flashcardSessionDone   = isDone
            )
        }
    }

    fun restartFlashcards() {
        val state     = _uiState.value
        val flashcards = filteredSigns(state)
            .shuffled()
            .map { FlashcardState(sign = it) }
        _uiState.update { it.copy(
            flashcards             = flashcards,
            currentFlashcardIndex  = 0,
            flashcardSessionDone   = false
        )}
    }

    // ─── Cleanup ──────────────────────────────────────────────────────────────

    override fun onCleared() {
        super.onCleared()
        feedbackResetJob?.cancel()
        signRecognizer.close()
    }
}