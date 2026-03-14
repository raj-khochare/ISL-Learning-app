package com.signsathi.navigationGraph

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.signsathi.presentation.GetStartedScreen
import com.signsathi.presentation.login.LoginScreen
import com.signsathi.presentation.signUp.SignUpScreen

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.GetStarted.route
    ){
        composable(Screens.GetStarted.route) {
            GetStartedScreen(navController = navController)
        }
        composable (Screens.SignUp.route){
            SignUpScreen(navController = navController)
        }

        composable (Screens.Login.route){
            LoginScreen(navController = navController)
        }
    }
}