package com.example.reminderacteautoandroid.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.reminderacteautoandroid.config.RetrofitClient
import com.example.reminderacteautoandroid.config.TokenManager
import com.example.reminderacteautoandroid.service.AuthApiService
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    navController: NavController,
    onContinueOffline: () -> Unit
){
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if(errorMessage != null){
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
        }

        Text("Bine ai venit!", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {newValue ->
                if (!newValue.contains("\n") && !newValue.contains("\t") && !newValue.contains(" ")) {
                    email = newValue
                }
            },
            label = {Text("Email")},
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { newValue ->
                if (!newValue.contains("\n") && !newValue.contains("\t") && !newValue.contains(" ")) {
                    password = newValue
                }
            },
            label = { Text("Parola") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    defaultKeyboardAction(ImeAction.Done)
                }
            )
        )

        Button(
            onClick = {
                scope.launch {
                    try {
                        val response = RetrofitClient.authService.login(AuthApiService.UserRequestDTO(email, password))
                        if(response.isSuccessful){
                            response.body()?.token?.let { token ->
                                tokenManager.saveToken(token)
                                onLoginSuccess()
                            }
                        } else{
                            errorMessage = "Eroare: Email sau parola incorecte!"
                        }
                    } catch (e: Exception){
                        errorMessage = "Eroare la conexiune la server! ${e.message}"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            Text("Conectare")
        }

        Row {
            TextButton(onClick = onNavigateToRegister) {
                Text("Nu ai cont? Inregistreaza-te")
            }

            TextButton(onClick = onNavigateToForgotPassword) {
                Text("Ai uitat parola?")
            }
        }

        TextButton(onClick = onContinueOffline, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Continua offline")
        }
    }
}