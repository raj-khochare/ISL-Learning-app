package com.signsathi.presentation.homeScreen

import androidx.lifecycle.ViewModel
import com.signsathi.data.model.LessonUnit
import com.signsathi.data.model.UserProgress
import com.signsathi.data.repository.LessonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(
        val units: List<LessonUnit>,
        val progress: UserProgress
    ) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val lessonRepository: LessonRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
}