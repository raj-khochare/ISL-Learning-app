package com.signsathi.navigationGraph

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.signsathi.presentation.GetStartedScreen
import com.signsathi.presentation.homeScreen.HomeScreen
import com.signsathi.presentation.lesson.LessonScreen
import com.signsathi.presentation.login.LoginScreen
import com.signsathi.presentation.practice.PracticeScreen
import com.signsathi.presentation.profile.ProfileScreen
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

        composable(
            route     = Screens.Lesson.route,
            arguments = listOf(
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) {
            LessonScreen(navController = navController)
        }

// Placeholder screens for bottom nav tabs
        composable(Screens.Practice.route) {
            PracticeScreen()
        }
        composable(Screens.Signs.route) {
            // TODO: wire up existing Signs screen
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Signs — coming soon")
            }
        }
        composable(Screens.Profile.route) {
            ProfileScreen(navController = navController)
        }

    }
}