package com.example.reminderacteautoandroid.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.reminderacteautoandroid.config.RetrofitClient
import com.example.reminderacteautoandroid.config.TokenManager
import com.example.reminderacteautoandroid.service.AuthApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var email by remember{ mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showBackupWarning by remember { mutableStateOf(true) }


    if (showBackupWarning)
        AlertDialog(
            onDismissRequest = { showBackupWarning = false },
            title = { Text(text = "Atenție!") },
            text = {
                Text(text = "Aceasta aplicatie este un proiect personal facut in timpul facultatii, nu recomand introducerea de date personale, insa este necesara introducerea unui email real, pentru a putea primi notificari pe mail.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackupWarning = false
                    }
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBackupWarning = false }) {
                    Text("Anuleaza")
                }
            }
        )


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Creaza cont") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Inapoi")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Adauga un email valid pentru a putea primi mail-uri in legatura cu starea documentelor!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { newValue ->
                    if (!newValue.contains("\n") && !newValue.contains("\t") && !newValue.contains(" ")) {
                        email = newValue
                    }
                },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { newValue ->
                    if (!newValue.contains("\n") && !newValue.contains("\t")&& !newValue.contains(" ")) {
                        password = newValue
                    }
                },
                label = { Text("Parolă") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it
                },
                label = { Text("Confirmă Parola") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = password != confirmPassword && confirmPassword.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Button(
                onClick = {
                    scope.launch {
                        val request = AuthApiService.UserRequestDTO(
                            email = email,
                            password = password
                        )

                        try {
                            val response = RetrofitClient.authService.register(request)

                            if(response.isSuccessful){
                                onRegisterSuccess()
                                //snackbarHostState.showSnackbar("Bine ai venit, $email!")
                            }
                            else{
                                if (response.code() == 409 || response.code() == 400 || response.code() == 403) {
                                    errorMessage = "Acest email este deja asociat unui cont."
                                } else {
                                    errorMessage = "Eroare la inregistrare, te rugam sa incerci mai tarziu!"
                                }
                            }
                        } catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = email.isNotBlank() && password == confirmPassword && email.contains("@") && password.isNotBlank()
            ) {
                Text("Inregistrare")
            }

        }

    }
}