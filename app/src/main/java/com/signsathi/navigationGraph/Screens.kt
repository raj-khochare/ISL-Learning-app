package com.signsathi.navigationGraph

sealed class Screens (val route: String){
    object GetStarted : Screens("getStarted")
    object SignUp : Screens("signUp")
    object Login : Screens("login")

}