package com.signsathi.presentation.lesson

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signsathi.data.local.dao.LessonDao
import com.signsathi.data.model.LessonNode
import com.signsathi.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LessonViewModel @Inject constructor(
    private val lessonDao       : LessonDao,
    private val lessonRepository : LessonRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(LessonUiState())
    val uiState: StateFlow<LessonUiState> = _uiState.asStateFlow()

    // lessonId is passed as a nav argument
    private val lessonId: String = checkNotNull(savedStateHandle["lessonId"])

    init {
        loadLesson()
    }

    // ─── Load ──────────────────────────────────────────────────────────────────

    fun retryLoad() = loadLesson()

    private fun loadLesson() {
        viewModelScope.launch {
            _uiState.value = LessonUiState(phase = LessonPhase.Loading)
            try {
                // Get the target lesson entity
                val unitsWithLessons = lessonDao.getUnitsWithLessons()
                val allLessons = unitsWithLessons.flatMap { it.lessons }

                val target = allLessons.find { it.lessonId == lessonId }
                    ?: error("Lesson $lessonId not found in cache")

                // Get sibling lessons from the same unit (used as quiz distractors)
                val siblings = allLessons
                    .filter { it.unitId == target.unitId && it.lessonId != lessonId }

                // Build quiz questions from the target + siblings
                val questions = buildQuestions(
                    target   = LessonNode(
                        id          = target.lessonId,
                        title       = target.lessonTitle,
                        videoUrl = target.videoUrl,
                        description = target.description,
                        state       = com.signsathi.data.model.NodeState.Active,
                        xpReward    = target.xpReward
                    ),
                    siblings = siblings.map { s ->
                        LessonNode(
                            id          = s.lessonId,
                            title       = s.lessonTitle,
                            description = s.description,
                            videoUrl = target.videoUrl,
                            state       = com.signsathi.data.model.NodeState.Active,
                            xpReward    = s.xpReward
                        )
                    }
                )

                _uiState.value = LessonUiState(
                    phase       = LessonPhase.Learn,
                    lessonTitle = target.lessonTitle,
                    description = target.description,
                    videoUrl    = target.videoUrl,
                    xpReward    = target.xpReward,
                    questions   = questions
                )

            } catch (e: Exception) {
                Timber.e(e, "LessonViewModel: failed to load lesson $lessonId")
                _uiState.value = LessonUiState(
                    phase = LessonPhase.Error(e.message ?: "Failed to load lesson")
                )
            }
        }
    }

    // ─── Phase transitions ────────────────────────────────────────────────────

    fun onContinueToQuiz() {
        val state = _uiState.value
        if (state.questions.isEmpty()) {
            // No siblings to make questions from — go straight to completed
            _uiState.value = state.copy(
                phase = LessonPhase.Completed(state.xpReward)
            )
            return
        }
        _uiState.value = state.copy(
            phase        = LessonPhase.Quiz,
            currentIndex = 0,
            selectedChoiceId = null,
            isAnswerChecked  = false
        )
    }

    fun onChoiceSelected(choiceId: String) {
        val state = _uiState.value
        if (state.isAnswerChecked) return   // lock after checking
        _uiState.value = state.copy(selectedChoiceId = choiceId)
    }

    fun onCheckAnswer() {
        val state    = _uiState.value
        val question = state.currentQuestion ?: return
        val selected = state.selectedChoiceId ?: return

        val isCorrect = selected == question.correctChoiceId
        val xpGained  = if (isCorrect) (state.xpReward / state.questions.size).coerceAtLeast(1) else 0

        _uiState.value = state.copy(
            phase           = LessonPhase.Result,
            isAnswerChecked = true,
            isCorrect       = isCorrect,
            xpEarnedSoFar   = state.xpEarnedSoFar + xpGained
        )
    }

    fun onNext() {
        val state     = _uiState.value
        val nextIndex = state.currentIndex + 1

        if (nextIndex >= state.questions.size) {
            // All questions done — post to backend
            finishLesson(state.xpEarnedSoFar)
        } else {
            _uiState.value = state.copy(
                phase            = LessonPhase.Quiz,
                currentIndex     = nextIndex,
                selectedChoiceId = null,
                isAnswerChecked  = false,
                isCorrect        = false
            )
        }
    }

    private fun finishLesson(totalXp: Int) {
        // Show completion immediately (optimistic)
        _uiState.value = _uiState.value.copy(
            phase = LessonPhase.Completed(totalXp)
        )

        // Post to backend in background
        viewModelScope.launch {
            try {
                val userId = com.amplifyframework.kotlin.core.Amplify
                    .Auth.getCurrentUser().userId
                lessonRepository.completeLesson(
                    userId   = userId,
                    lessonId = lessonId,
                    xpEarned = totalXp
                ).onFailure { e ->
                    Timber.e(e, "LessonViewModel: completion sync failed")
                    // Don't show error — local cache already updated
                }
            } catch (e: Exception) {
                Timber.e(e, "LessonViewModel: failed to get userId")
            }
        }
    }

    // ─── Quiz generation ──────────────────────────────────────────────────────

    /**
     * Builds quiz questions dynamically based on available siblings.
     *
     * Rules:
     * - If 0 siblings → no questions (go straight to completed)
     * - If 1–2 siblings → 1 question (VIDEO_TO_NAME)
     * - If 3+ siblings → 2 questions alternating VIDEO_TO_NAME / WORD_TO_VIDEO
     *
     * Each question uses up to 3 choices: 1 correct + up to 2 distractors.
     */
    private fun buildQuestions(
        target   : LessonNode,
        siblings : List<LessonNode>
    ): List<QuizQuestion> {
        if (siblings.isEmpty()) return emptyList()

        val questions = mutableListOf<QuizQuestion>()
        val shuffledSiblings = siblings.shuffled()

        // Question 1 — VIDEO_TO_NAME: show target video, pick the name
        val distractors1 = shuffledSiblings.take(2)
        val choices1 = buildNameChoices(target, distractors1)
        questions.add(
            QuizQuestion(
                type            = QuestionType.VIDEO_TO_NAME,
                signTitle       = target.title,
                videoUrl        = target.videoUrl,
                choices         = choices1,
                correctChoiceId = target.id
            )
        )

        // Question 2 — WORD_TO_VIDEO (only if 3+ siblings available)
        if (siblings.size >= 3) {
            val distractors2 = shuffledSiblings.take(2)
            val choices2 = buildVideoChoices(target, distractors2)
            questions.add(
                QuizQuestion(
                    type            = QuestionType.WORD_TO_VIDEO,
                    signTitle       = target.title,
                    videoUrl        = target.videoUrl,
                    choices         = choices2,
                    correctChoiceId = target.id
                )
            )
        }

        return questions
    }

    private fun buildNameChoices(
        target      : LessonNode,
        distractors : List<LessonNode>
    ): List<QuizChoice> {
        val choices = mutableListOf(
            QuizChoice(id = target.id, label = target.title, videoUrl = target.videoUrl)
        )
        distractors.forEach { d ->
            choices.add(QuizChoice(id = d.id, label = d.title, videoUrl = d.videoUrl))
        }
        return choices.shuffled()
    }

    private fun buildVideoChoices(
        target      : LessonNode,
        distractors : List<LessonNode>
    ): List<QuizChoice> {
        val choices = mutableListOf(
            QuizChoice(id = target.id, label = target.title, videoUrl = target.videoUrl)
        )
        distractors.forEach { d ->
            choices.add(QuizChoice(id = d.id, label = d.title, videoUrl = d.videoUrl))
        }
        return choices.shuffled()
    }
}