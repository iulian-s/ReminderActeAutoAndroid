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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.reminderacteautoandroid.config.RetrofitClient
import com.example.reminderacteautoandroid.screen.AuthScreen
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
                    AuthScreen()
                }
            }
        }
    }
}

