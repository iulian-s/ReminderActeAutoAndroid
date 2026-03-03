package com.example.reminderacteautoandroid.screen

import android.os.Build
import androidx.annotation.RequiresApi
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

    object RecoverPassword: AuthScreenRoutes("recoverPassword")
    object Dashboard: AuthScreenRoutes("Dashboard")
}

@RequiresApi(Build.VERSION_CODES.O)
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
            startDestination = if(!isLoggedIn) AuthScreenRoutes.Login.route else AuthScreenRoutes.Dashboard.route,
            //startDestination = AuthScreenRoutes.Login.route,
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
                RegisterScreen(
                    onBack = { authController.popBackStack() },
                    onRegisterSuccess = {
                        authController.navigate(AuthScreenRoutes.Login.route)
                    }
                )
            }

            composable(AuthScreenRoutes.Dashboard.route){
                LaunchedEffect(isLoggedIn) {
                    if(!isLoggedIn){
                        authController.navigate(AuthScreenRoutes.Login.route){
                            popUpTo(AuthScreenRoutes.Dashboard.route) { inclusive = true }
                        }
                    }
                }
                if (isLoggedIn) {
                    DashboardScreen()
                }
            }

            composable(AuthScreenRoutes.ForgotPassword.route){
                ForgotPasswordScreen(
                    onBack = { authController.popBackStack() },
                    onSend = {authController.navigate(AuthScreenRoutes.RecoverPassword.route)}
                )
            }

            composable(AuthScreenRoutes.RecoverPassword.route){
                RecoverPasswordScreen(
                    onBack = { authController.popBackStack() },
                    onSuccess = { authController.navigate(AuthScreenRoutes.Login.route)}
                )
            }
        }
    }
}