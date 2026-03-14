package com.signsathi.navigationGraph

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.signsathi.presentation.GetStartedScreen
import com.signsathi.presentation.homeScreen.HomeScreen
import com.signsathi.presentation.login.LoginScreen
import com.signsathi.presentation.signUp.OtpVerificationScreen
import com.signsathi.presentation.signUp.SignUpScreen
import com.signsathi.presentation.splash.SplashScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.Splash.route
    ){
        composable(Screens.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screens.GetStarted.route) {
            GetStartedScreen(navController = navController)
        }
        composable (Screens.SignUp.route){
            SignUpScreen(navController = navController)
        }

        composable (Screens.Login.route){
            LoginScreen(navController = navController)
        }

        composable (Screens.Home.route){
            HomeScreen(navController = navController)
        }

        composable (Screens.OtpVerification.route){
            OtpVerificationScreen(navController = navController)
        }

    }
}