package com.signsathi.presentation.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signsathi.data.repository.AuthRepository
import com.signsathi.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _emailText = mutableStateOf("")
    val emailText: State<String> = _emailText

    private val _passwordText = mutableStateOf("")
    val passwordText: State<String> = _passwordText

    private val _authState = mutableStateOf<UiState>(UiState.Idle)
    val authState: State<UiState> = _authState

    fun setEmailText(email: String) {
        _emailText.value = email
    }

    fun setPasswordText(password: String) {
        _passwordText.value = password
    }

    fun signIn() {
        // FIX: added input validation before hitting the network
        val email = _emailText.value.trim()
        val password = _passwordText.value

        if (email.isBlank() || password.isBlank()) {
            _authState.value = UiState.Error("Email and password cannot be empty")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = UiState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                authRepository.signIn(email = email, password = password)
                    .onSuccess { _authState.value = UiState.Success }
                    .onFailure {
                        Log.e("LoginViewModel", "Sign in failed: ${it.message}", it)
                        _authState.value = UiState.Error(it.message ?: "Sign in failed")
                    }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Unexpected error: ${e.message}", e)
                _authState.value = UiState.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun forgotPassword() {
        val email = _emailText.value.trim()
        if (email.isBlank()) {
            _authState.value = UiState.Error("Enter your email address first")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = UiState.Error("Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            _authState.value = UiState.Loading
            authRepository.forgotPassword(email)
                .onSuccess { _authState.value = UiState.Success }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Failed to send reset email") }
        }
    }

    fun resetState() {
        _authState.value = UiState.Idle
    }
}