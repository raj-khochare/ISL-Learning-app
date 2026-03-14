package com.signsathi.presentation.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.signsathi.navigationGraph.Screens
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.Orange

@Composable
fun SplashScreen(
    navController: NavController,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val isLoggedIn = viewModel.isLoggedIn.value

    // wait until check is done then navigate
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == null) return@LaunchedEffect  // still checking

        if (isLoggedIn) {
            //  already signed in → go directly to Home
            navController.navigate(Screens.Home.route) {
                popUpTo(Screens.Splash.route) { inclusive = true }
            }
        } else {
            //  not signed in → go to GetStarted
            navController.navigate(Screens.GetStarted.route) {
                popUpTo(Screens.Splash.route) { inclusive = true }
            }
        }
    }

    // simple loading UI while checking
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = Orange)
    }
}
