package com.signsathi.presentation.signUp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.signsathi.components.CustomButton
import com.signsathi.components.CustomTextField
import com.signsathi.navigationGraph.Screens
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.DarkGrey
import com.signsathi.ui.theme.Orange
import com.signsathi.ui.theme.nunito
import com.signsathi.utils.UiState


@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val authState = viewModel.authState.value
    val showOtpScreen = viewModel.showOtpScreen.value
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(showOtpScreen) {
        if (showOtpScreen) {
            navController.navigate(Screens.OtpVerification.route)
            viewModel.resetOtpNavigation()  // ← reset flag after navigating
        }
    }
    // When signUp() succeeds → showOtpScreen becomes true → navigate
    LaunchedEffect(showOtpScreen) {
        if (showOtpScreen) {
            navController.navigate(Screens.OtpVerification.route)
        }
    }

    LaunchedEffect(authState) {
        if (authState is UiState.Error) {
            snackbarHostState.showSnackbar(authState.message)
            viewModel.resetState()
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
                .navigationBarsPadding()
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "close")
                }
                Text(
                    text = "Create Account",
                    modifier = Modifier.align(Alignment.Center),
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGrey
                    )
                )
            }

            CustomTextField(
                input = viewModel.emailText.value,
                label = "Email",
                onValueChange = {
                    viewModel.setEmailText(it) },
                modifier = Modifier.padding(16.dp)
            )
            CustomTextField(
                input = viewModel.passwordText.value,
                label = "Password",
                onValueChange = { viewModel.setPasswordText(it) },
                keyboardType = KeyboardType.Password,
                isPassword = true,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )
            CustomTextField(
                input = viewModel.confirmPasswordText.value,
                label = "Confirm Password",
                onValueChange = { viewModel.setConfirmPassword(it) },
                keyboardType = KeyboardType.Password,
                isPassword = true,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
            )

            if (authState is UiState.Loading) {
                CircularProgressIndicator(
                    color = Orange,
                    modifier = Modifier.padding(8.dp)
                )
            }

            CustomButton(
                text = "SIGN UP",
                onClick = {
                    if (authState !is UiState.Loading) viewModel.signUp()
                }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
