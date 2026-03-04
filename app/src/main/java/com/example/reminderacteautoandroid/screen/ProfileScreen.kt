package com.example.reminderacteautoandroid.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.reminderacteautoandroid.config.RetrofitClient
import com.example.reminderacteautoandroid.service.UserApiService
import com.example.reminderacteautoandroid.service.VehicleApiService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onAccountDeleted: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var wantsToChangePassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope ()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var deletePasswordInput by remember { mutableStateOf("") }



    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.userService.getCurrentUser()
            if(response.isSuccessful){
                email = response.body()?.email ?: "Eroare"
            }
        } catch (e: Exception){e.printStackTrace()}
        isLoading = false
    }
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
    } else {
        Column(modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())){
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                Text(
                    text = "Profil",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(modifier = Modifier
                .padding(vertical = 8.dp)
                .padding(horizontal = 16.dp)) {
                Text("Email", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Text(email, style = MaterialTheme.typography.bodyLarge)
                HorizontalDivider(modifier = Modifier.padding(top = 4.dp), thickness = 0.5.dp)
            }

            OutlinedButton(
                onClick = { wantsToChangePassword = !wantsToChangePassword },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (wantsToChangePassword) "Renunta" else "Schimba parola")
            }
            if (wantsToChangePassword) {
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Parola actuala") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Parola noua") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Button(onClick = {
                    scope.launch{
                        try{
                            val passwordRequest = UserApiService.ChangePasswordRequestDTO(
                                oldPassword = oldPassword,
                                newPassword = newPassword
                            )
                            val response = RetrofitClient.userService.changePassword(passwordRequest)
                            if(response.isSuccessful){
                                Toast.makeText(context, "Parola modificata cu succes!", Toast.LENGTH_SHORT).show()
                                wantsToChangePassword = false
                            }
                        } catch (e: Exception){e.printStackTrace()}
                    }
                }, enabled = oldPassword.isNotBlank() && newPassword.isNotBlank())
                {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center){
                        Text("Schimba parola")}
                    }

            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                showDeleteDialog = true
            }, modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red))
            {
                Text("Sterge contul!")
            }
            if(showDeleteDialog){
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        deletePasswordInput = ""
                    },
                    title = { Text("Esti sigur?") },
                    text = {
                        Column {
                            Text("Actiune ireversibila, introduceti parola pentru confirmare.")
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = deletePasswordInput,
                                onValueChange = { deletePasswordInput = it },
                                label = { Text("Parola") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if(deletePasswordInput.isNotBlank()){
                                scope.launch {
                                    try {
                                        val response = RetrofitClient.userService.deleteAccount(deletePasswordInput)
                                        if(response.code() == 200 || response.isSuccessful){
                                            Toast.makeText(context, "Cont sters cu succes!", Toast.LENGTH_SHORT).show()
                                            onAccountDeleted()
                                        } else{
                                            Toast.makeText(context, "Parola incorecta!", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception){
                                        Toast.makeText(context, "Eroare la stergere, va rugam sa incercati mai tarziu!", Toast.LENGTH_SHORT).show()
                                        e.printStackTrace()
                                    } finally {
                                        showDeleteDialog = false
                                        deletePasswordInput = ""
                                    }
                                }
                            }
                        },
                            colors = ButtonDefaults.buttonColors(Color.Red)
                        ) {
                            Text("Sterge definitiv", color = Color.White)
                        }
                    }, dismissButton = {
                        TextButton(onClick = {showDeleteDialog = false}) { Text("Anuleaza") }
                    }

                )
            }
        }
    }

}