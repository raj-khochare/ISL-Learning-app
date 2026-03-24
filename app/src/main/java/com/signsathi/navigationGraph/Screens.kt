package com.signsathi.navigationGraph

sealed class Screens(val route: String) {
    object Splash : Screens("splash")
    object GetStarted : Screens("getStarted")
    object SignUp : Screens("signUp")
    object Login : Screens("login")
    object Home : Screens("home")
    object OtpVerification : Screens("otpVerification")
    object Practice   : Screens("practice")
    object Signs      : Screens("signs")
    object Profile    : Screens("profile")
    object Lesson : Screens("lesson/{lessonId}") {
        fun createRoute(lessonId: String) = "lesson/$lessonId"
    }
}