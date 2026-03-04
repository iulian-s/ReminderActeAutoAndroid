package com.example.reminderacteautoandroid.screen

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.reminderacteautoandroid.config.RetrofitClient
import com.example.reminderacteautoandroid.service.DocumentApiService
import com.example.reminderacteautoandroid.service.VehicleApiService
import kotlinx.coroutines.launch
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleScreen(
    userId: Long,
    onAddSuccess: () -> Unit
) {
    var brand by remember{ mutableStateOf("") }
    var model by remember{ mutableStateOf("") }
    val documents = remember { mutableStateListOf(DocumentApiService.DocumentInput()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var savingProgress by remember { mutableStateOf<String?>(null) } // null means not saving


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adauga vehicul") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ){ paddingValues ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            OutlinedTextField(
                value = brand,
                onValueChange = { newValue ->
                    if (!newValue.contains("\n") && !newValue.contains("\t")) {
                        brand = newValue
                    }
                },
                label = { Text("Brand") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = model,
                onValueChange = { newValue ->
                    if (!newValue.contains("\n") && !newValue.contains("\t")) {
                        model = newValue
                    }
                },
                label = { Text("Model") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))

            Text("Documente")
            Spacer(modifier = Modifier.height(8.dp))
            documents.forEachIndexed { index, document ->
                DocumentEntryItem(
                    document = document,
                    onTypeChange = { newType ->
                        documents[index] = documents[index].copy(type = newType)
                    },
                    onDateChange = { newDate ->
                        documents[index] = documents[index].copy(expiryDate = newDate)
                    },
                    onRemove = {
                        val documentToRemove = documents[index]
                        documents.removeAt(index) // Always remove from UI first

                        // Only call the API if the document actually exists on the server
                        if (documentToRemove.id != null) {
                            scope.launch {
                                try {
                                    val response = RetrofitClient.documentService.deleteDocument(documentToRemove.id)
                                    if (response.isSuccessful) {
                                        Log.d("DEBUG_DELETE", "Document deleted from DB")
                                    } else {
                                        Log.e("DEBUG_DELETE", "Failed to delete: ${response.code()}")
                                    }
                                } catch (e: Exception) {
                                    Log.e("DEBUG_DELETE", "Network error", e)
                                }
                            }
                        }

                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(onClick = { documents.add(DocumentApiService.DocumentInput()) }) {
                Text("Adauga Document Nou")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        savingProgress = "Se salvează vehiculul..."
                        if (brand.isBlank() || model.isBlank()) {
                            snackbarHostState.showSnackbar("Completeaza toate campurile vehiculului")
                            return@launch
                        }

                        if (documents.any { it.type.isBlank() || it.expiryDate == null }) {
                            snackbarHostState.showSnackbar("Completeaza toate documentele")
                            return@launch
                        }

                        try {
                            val vehicleRequest = VehicleApiService.VehicleRequestDTO(userId, brand, model)
                            val vehicleResponse = RetrofitClient.vehicleService.addVehicle(vehicleRequest)

                            if (!vehicleResponse.isSuccessful) {
                                val errorMsg = "Eroare Vehicul: ${vehicleResponse.code()} ${vehicleResponse.errorBody()?.string()}"
                                Log.e("DEBUG_SAVE", errorMsg)
                                snackbarHostState.showSnackbar(errorMsg)
                                return@launch
                            }

                            if (vehicleResponse.isSuccessful) {
                                val vehicleId = vehicleResponse.body()!!.id
                                var errorOccurred = false
                                for ((index, doc) in documents.withIndex()) {
                                    savingProgress =
                                        "Se salveaza documentul ${index + 1} din ${documents.size}..."
                                    val documentRequest = VehicleApiService.DocumentRequestDTO(
                                        vehicleId = vehicleId,
                                        type = doc.type,
                                        expiryDate = doc.expiryDate!!.toString()
                                    )
                                    try {
                                        val docResponse = RetrofitClient.documentService.addDocument(vehicleId, documentRequest)
                                        if (docResponse.isSuccessful) {
                                            savingProgress = null
                                            Log.d("DEBUG_SAVE", "Documentul $index (${doc.type}) salvat.")
                                        } else {
                                            savingProgress = null
                                            errorOccurred = true
                                            val docError = "Eroare Doc $index: ${docResponse.code()} - ${docResponse.message()}"
                                            Log.e("DEBUG_SAVE", docError)
                                            snackbarHostState.showSnackbar(docError)

                                        }
                                    } catch (e: Exception){
                                        println(e.message)
                                        errorOccurred = true
                                    }

                                }
                                if (!errorOccurred) {
                                    savingProgress = null
                                    onAddSuccess()
                                } else {
                                    savingProgress = null
                                    snackbarHostState.showSnackbar("Unele documente nu s-au putut salva.")
                                }
                            }
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Eroare: ${e.message}")
                        }
                    }
                }
            ) {
                Text("Adauga")
            }
        }
        if (savingProgress != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Card {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(savingProgress!!)
                    }
                }
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentEntryItem(
    document: DocumentApiService.DocumentInput,
    onTypeChange: (String) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onRemove: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = document.type,
                onValueChange = onTypeChange,
                label = { Text("Tip Document (ex: RCA, ITP)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Date Picker Trigger
            OutlinedTextField(
                value = document.expiryDate?.toString() ?: "Selectează data",
                onValueChange = {},
                label = { Text("Data Expirare") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                trailingIcon = {
                    androidx.compose.material3.IconButton(onClick = { showDatePicker = true }) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendar"
                        )
                    }
                }
            )

            Button(
                onClick = onRemove,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Șterge")
            }
        }
    }

    if (showDatePicker) {
        androidx.compose.material3.DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        onDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) {
            androidx.compose.material3.DatePicker(state = datePickerState)
        }
    }
}