package com.signsathi.presentation.splash

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.signsathi.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoggedIn = mutableStateOf<Boolean?>(null) // null = still checking
    val isLoggedIn: State<Boolean?> = _isLoggedIn

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            delay(300)
            _isLoggedIn.value = authRepository.isSignedIn()
        }
    }
}