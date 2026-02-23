package com.example.reminderacteautoandroid.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reminderacteautoandroid.config.TokenManager

sealed class AuthScreenRoutes(val route: String){
    object Login: AuthScreenRoutes("login")
    object Register: AuthScreenRoutes("register")
    
    object ForgotPassword: AuthScreenRoutes("forgotPassword")
    object Dashboard: AuthScreenRoutes("Dashboard")
}

@Composable
fun AuthScreen(){
    val authController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var isLoggedIn by remember {
        mutableStateOf(tokenManager.getToken() != null)
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        NavHost(
            navController = authController,
            startDestination = AuthScreenRoutes.Login.route,
            modifier = Modifier.padding(paddingValues)
        ){
            composable(AuthScreenRoutes.Login.route){
                LoginScreen(
                    navController = authController,
                    onNavigateToRegister = {authController.navigate(AuthScreenRoutes.Register.route)},
                    onNavigateToForgotPassword = {authController.navigate(AuthScreenRoutes.ForgotPassword.route)},
                    onLoginSuccess = {
                        isLoggedIn = true
                        authController.navigate(AuthScreenRoutes.Dashboard.route){
                            popUpTo(AuthScreenRoutes.Login.route) {inclusive = true}
                        }
                    }

                )
            }

            composable(AuthScreenRoutes.Register.route){
                //RegisterScreen()
            }

            composable(AuthScreenRoutes.Dashboard.route){
                LaunchedEffect(isLoggedIn) {
                    if(!isLoggedIn){
                        authController.navigate(AuthScreenRoutes.Login.route)
                    }
                }
                DashboardScreen()
            }
        }
    }
}