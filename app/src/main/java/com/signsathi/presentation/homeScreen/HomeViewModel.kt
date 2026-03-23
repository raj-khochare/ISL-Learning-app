package com.signsathi.presentation.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.kotlin.core.Amplify
import com.signsathi.data.model.LessonNode
import com.signsathi.data.model.LessonUnit
import com.signsathi.data.model.UserProgress
import com.signsathi.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

// ─── UI state ─────────────────────────────────────────────────────────────────

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val units: List<LessonUnit>,
        val progress: UserProgress,
        val isRefreshing: Boolean = false
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}

// ─── ViewModel ────────────────────────────────────────────────────────────────

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lessonRepository: LessonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHome()
    }

    private fun loadHome() {
        viewModelScope.launch {
            val userId = getCurrentUserId() ?: run {
                _uiState.value = HomeUiState.Error("Could not get user session")
                return@launch
            }

            // Kick off a background refresh — updates Room if cache is stale.
            // The combine flow below emits automatically when Room updates.
            launch { refresh(userId) }

            // Observe Room — emits immediately with cached data, then again
            // after refresh() writes fresh data.
            combine(
                lessonRepository.observeUnits(userId),
                lessonRepository.observeUserProgress(userId)
            ) { units, progress ->
                HomeUiState.Success(units = units, progress = progress)
            }
                .catch { e ->
                    Timber.e(e, "HomeViewModel: error observing data")
                    _uiState.value = HomeUiState.Error(e.message ?: "Something went wrong")
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    /** Called on pull-to-refresh — forces a network fetch regardless of cache age. */
    fun forceRefresh() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is HomeUiState.Success) {
                _uiState.value = currentState.copy(isRefreshing = true)
            }
            val userId = getCurrentUserId() ?: return@launch
            refresh(userId)
        }
    }

    private suspend fun refresh(userId: String) {
        try {
            lessonRepository.refresh(userId)
        } catch (e: Exception) {
            Timber.e(e, "HomeViewModel: refresh failed — serving cached data")
        }
    }

    private suspend fun getCurrentUserId(): String? {
        return try {
            Amplify.Auth.getCurrentUser().userId
        } catch (e: Exception) {
            Timber.e(e, "HomeViewModel: failed to get current user")
            null
        }
    }

    fun onLessonClick(node: LessonNode) {
        // TODO: navigate to lesson screen in Phase 3
        Timber.d("HomeViewModel: lesson tapped — ${node.id} (${node.title})")
    }
}