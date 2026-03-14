package com.signsathi.presentation.signUp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.signsathi.data.repository.AuthRepository
import com.signsathi.utils.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.State
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _emailText = mutableStateOf("")
    val emailText: State<String> = _emailText

    private val _passwordText = mutableStateOf("")
    val passwordText: State<String> = _passwordText

    private val _confirmPasswordText = mutableStateOf("")
    val confirmPasswordText: State<String> = _confirmPasswordText

    private val _otpCode = mutableStateOf("")
    val otpCode: State<String> = _otpCode

    // controls whether to show OTP screen
    private val _showOtpScreen = mutableStateOf(false)
    val showOtpScreen: State<Boolean> = _showOtpScreen

    private val _authState = mutableStateOf<UiState>(UiState.Idle)
    val authState: State<UiState> = _authState

    fun setEmailText(email: String) { _emailText.value = email }
    fun setPasswordText(password: String) { _passwordText.value = password }
    fun setConfirmPassword(password: String) { _confirmPasswordText.value = password }
    fun setOtpCode(code: String) { _otpCode.value = code }
    fun resetState() { _authState.value = UiState.Idle }

    fun resetOtpNavigation() { _showOtpScreen.value = false }

    fun signUp() {
        if (_passwordText.value != _confirmPasswordText.value) {
            _authState.value = UiState.Error("Passwords do not match")
            return
        }
        if (_emailText.value.isBlank() || _passwordText.value.isBlank()) {
            _authState.value = UiState.Error("Fields cannot be empty")
            return
        }
        viewModelScope.launch {
            _authState.value = UiState.Loading
            authRepository.signUp(
                email = _emailText.value.trim(),
                password = _passwordText.value
            )
                .onSuccess {
                    _authState.value = UiState.Idle
                    _showOtpScreen.value = true   // navigate to OTP screen
                }
                .onFailure {
                    _authState.value = UiState.Error(it.message ?: "Sign up failed")
                }
        }
    }

    fun confirmSignUp() {
        viewModelScope.launch {
            _authState.value = UiState.Loading
            authRepository.confirmSignUp(
                email = _emailText.value.trim(),
                code = _otpCode.value.trim()
            )
                .onSuccess { _authState.value = UiState.Success }
                .onFailure { _authState.value = UiState.Error(it.message ?: "Verification failed") }
        }
    }

    fun resendCode() {
        viewModelScope.launch {
            authRepository.resendConfirmationCode(_emailText.value.trim())
        }
    }
}