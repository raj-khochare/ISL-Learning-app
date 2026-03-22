package com.signsathi.presentation.signUp

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.signsathi.utils.components.CustomButton
import com.signsathi.utils.components.CustomTextField
import com.signsathi.navigationGraph.Screens
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.DarkGrey
import com.signsathi.ui.theme.Orange
import com.signsathi.ui.theme.nunito
import com.signsathi.utils.UiState

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun OtpVerificationScreen(
    navController: NavController
) {

    val signUpBackStackEntry = remember(navController) {
        navController.getBackStackEntry(Screens.SignUp.route)
    }
    val viewModel: SignUpViewModel = hiltViewModel(signUpBackStackEntry)

    val authState = viewModel.authState.value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState) {
        when (authState) {
            is UiState.Success -> {
                navController.navigate(Screens.Login.route) {
                    popUpTo(Screens.GetStarted.route) {
                        inclusive = false   // keeps GetStarted so user can go back if needed
                    }
                }
            }
            is UiState.Error -> {
                snackbarHostState.showSnackbar(authState.message)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = "Verify Your Email",
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGrey
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the 6-digit code sent to ${viewModel.emailText.value}",
                style = TextStyle(fontFamily = nunito, fontSize = 14.sp, color = DarkGrey),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            CustomTextField(
                input = viewModel.otpCode.value,
                label = "6-digit code",
                onValueChange = { viewModel.setOtpCode(it) },
                keyboardType = KeyboardType.Number,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (authState is UiState.Loading) {
                CircularProgressIndicator(color = Orange)
            }

            CustomButton(
                text = "VERIFY",
                onClick = {
                    if (authState !is UiState.Loading) viewModel.confirmSignUp()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Resend Code",
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize = 16.sp,
                    color = DarkGrey,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.resendCode() }
            )
        }
    }
}