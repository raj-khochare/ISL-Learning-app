package com.signsathi.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.kotlin.core.Amplify
import com.signsathi.data.local.dao.UserProgressDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

data class ProfileUiState(
    val isLoading        : Boolean = true,
    val username         : String  = "",
    val email            : String  = "",
    val xp               : Int     = 0,
    val streakDays       : Int     = 0,
    val heartsLeft       : Int     = 5,
    val lessonsCompleted : Int     = 0,
    val error            : String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val progressDao: UserProgressDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    private fun loadProfile() {
        viewModelScope.launch {

            // 1. Get Amplify identity
            val userId = try {
                Amplify.Auth.getCurrentUser().userId
            } catch (e: Exception) {
                _uiState.value = ProfileUiState(isLoading = false, error = "Not signed in")
                return@launch
            }

            // 2. Fetch user attributes (username / email) — one-shot
            val attributes = try {
                Amplify.Auth.fetchUserAttributes()
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel: fetchUserAttributes failed")
                emptyList()
            }
            val email    = attributes.find { it.key.keyString == "email" }?.value ?: ""
            val username = attributes
                .find { it.key.keyString == "preferred_username" }?.value
                ?: email.substringBefore("@")

            // 3. Observe Room — emits whenever XP / progress changes
            combine(
                progressDao.observeStats(userId),
                progressDao.observeProgress(userId)
            ) { stats, allRows ->
                val completed = allRows.count {
                    it.lessonId != "STATS" && it.status == "completed"
                }
                ProfileUiState(
                    isLoading        = false,
                    username         = username,
                    email            = email,
                    xp               = stats?.xp         ?: 0,
                    streakDays       = stats?.streakDays ?: 0,
                    heartsLeft       = stats?.heartsLeft ?: 5,
                    lessonsCompleted = completed
                )
            }.collect { _uiState.value = it }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                Amplify.Auth.signOut()
            } catch (e: Exception) {
                Timber.e(e, "ProfileViewModel: signOut failed")
            }
        }
    }
}