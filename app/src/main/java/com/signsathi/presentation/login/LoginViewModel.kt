package com.signsathi.presentation.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.signsathi.data.repository.AuthRepository
import androidx.lifecycle.viewModelScope
import com.signsathi.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _emailText = mutableStateOf("")
    val emailText : State<String> = _emailText

    private val _passwordText = mutableStateOf("")
    val passwordText : State<String> = _passwordText

    private val _authState = mutableStateOf<UiState>(UiState.Idle)
    val authState: State<UiState> = _authState

    fun setEmailText(email : String){
        _emailText.value = email
    }
    fun setPasswordText(password : String){
        _passwordText.value = password
    }

    fun signIn() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            try {
                authRepository.signIn(
                    email = _emailText.value.trim(),
                    password = _passwordText.value
                )
                    .onSuccess { _authState.value = UiState.Success }
                    .onFailure {
                        Log.e("LoginViewModel", "Sign in failed: ${it.message}", it) // ← add this
                        _authState.value = UiState.Error(it.message ?: "Sign in failed")
                    }
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Unexpected error: ${e.message}", e) // ← and this
                _authState.value = UiState.Error(e.message ?: "Unexpected error")
            }
        }
    }

    fun forgotPassword() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            authRepository.forgotPassword(_emailText.value.trim())
                .onSuccess { _authState.value = UiState.Success }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Failed") }
        }
    }

    fun resetState() { _authState.value = UiState.Idle }
}