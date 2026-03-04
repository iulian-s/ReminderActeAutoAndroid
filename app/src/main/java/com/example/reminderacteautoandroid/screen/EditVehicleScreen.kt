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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditVehicleScreen(
    vehicleId: Long,
    userId: Long,
    onEditSuccessful: () -> Unit
) {
    var brand by remember{ mutableStateOf("") }
    var model by remember{ mutableStateOf("") }
    val documents = remember { mutableStateListOf(DocumentApiService.DocumentInput()) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(true) }
    var savingProgress by remember { mutableStateOf<String?>(null) } // null means not saving

    LaunchedEffect(vehicleId) {
        try {
            val vehicleResponse = RetrofitClient.vehicleService.getVehicleById(vehicleId)
            if (vehicleResponse.isSuccessful) {
                val vehicle = vehicleResponse.body()
                brand = vehicle?.brand ?: ""
                model = vehicle?.model ?: ""
            }

            val docsResponse = RetrofitClient.documentService.getDocumentsByVehicleId(vehicleId)
            if (docsResponse.isSuccessful) {
                documents.clear()
                docsResponse.body()?.forEach { doc ->
                    val parsedDate = try {
                        java.time.LocalDate.parse(doc.expiryDate)
                    } catch (e: Exception) {
                        Log.e("EDIT_DEBUG", "Failed to parse date: ${doc.expiryDate}")
                        null // Handle cases where date might be malformed
                    }
                    documents.add(
                        DocumentApiService.DocumentInput(
                            id = doc.id,
                            type = doc.type,
                            expiryDate = parsedDate
                        )
                    )
                }
            }
        } catch (e: Exception){
            Log.e("EDIT_BUG", "Failed to load data", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adauga vehicul") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ){ paddingValues ->
        if(isLoading){
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                CircularProgressIndicator()
            }
        } else {
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
                Button(onClick = { documents.add(DocumentApiService.DocumentInput(id = null, type = "", expiryDate = null)) }) {
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
                                val vehicleResponse = RetrofitClient.vehicleService.updateVehicle(vehicleId, vehicleRequest)
                                if (vehicleResponse.isSuccessful) {
                                    val vId = vehicleResponse.body()!!.id
                                    var errorOccurred = false

                                    // Process documents one by one
                                    for ((index, doc) in documents.withIndex()) {
                                        savingProgress = "Se salveaza documentul ${index + 1} din ${documents.size}..."
                                        val documentRequest = VehicleApiService.DocumentRequestDTO(
                                            vehicleId = vId,
                                            type = doc.type,
                                            expiryDate = doc.expiryDate!!.toString()
                                        )

                                        try {
                                            Log.d("DEBUG_SAVE", "Processing doc $index: ${doc.type} (ID: ${doc.id ?: "NEW"})")

                                            val docResponse = if (doc.id == null) {
                                                RetrofitClient.documentService.addDocument(vId, documentRequest)
                                            } else {
                                                RetrofitClient.documentService.updateDocument(doc.id, documentRequest)
                                            }

                                            if (!docResponse.isSuccessful) {
                                                val errorBody = docResponse.errorBody()?.string()
                                                Log.e("DEBUG_SAVE", "Doc $index FAILED: ${docResponse.code()} - $errorBody")
                                                errorOccurred = true
                                            }
                                        } catch (e: Exception) {
                                            Log.e("DEBUG_SAVE", "Network exception on doc $index", e)
                                            errorOccurred = true
                                        }
                                    }

                                    if (!errorOccurred) {
                                        savingProgress = null
                                        onEditSuccessful()
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
                    Text("Salveaza")
                }

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