package com.example.reminderacteautoandroid

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.example.reminderacteautoandroid.config.RetrofitClient
import com.example.reminderacteautoandroid.config.TokenManager
import com.example.reminderacteautoandroid.screen.AuthScreen
import com.example.reminderacteautoandroid.screen.AuthScreenRoutes
import com.example.reminderacteautoandroid.screen.DashboardScreen
import com.example.reminderacteautoandroid.ui.theme.ReminderActeAutoAndroidTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AuthEvents{
    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvent = _logoutEvent.asSharedFlow()

    suspend fun triggerLogout(){
        _logoutEvent.emit(Unit)
    }
}
class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        RetrofitClient.init(applicationContext)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReminderActeAutoAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    var globalNavHostController by remember { mutableStateOf<NavHostController?>(null) }
                    val context = LocalContext.current
                    val tokenManager = remember { TokenManager(context) }
                    val token = tokenManager.getToken()
                    if (token == null){
//                        LaunchedEffect(Unit) {globalNavHostController?.navigate(AuthScreenRoutes.Login.route) }
                        AuthScreen()
                    } else{
//                        tokenManager.deleteToken()
                        DashboardScreen()
                    }

                }
            }
        }
    }
}

