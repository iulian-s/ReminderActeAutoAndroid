package com.example.reminderacteautoandroid.screen

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.reminderacteautoandroid.config.RetrofitClient
import com.example.reminderacteautoandroid.service.VehicleApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class VehicleListState{
    object Loading: VehicleListState()
    data class Success(val user: VehicleApiService.UserResponseDTO): VehicleListState()
    data class Error(val message: String): VehicleListState()
}
@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onEditVehicle: (Long, Long) -> Unit,
    onAddVehicle: (Long) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    var vehiclesListState by remember {
        mutableStateOf<VehicleListState>(VehicleListState.Loading)
    }
    val scope = rememberCoroutineScope ()
    var isRefreshing by remember { mutableStateOf(false) }
    var showDeleteWarning by remember { mutableStateOf(false) }
    var vehicleToDelete by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) }

    suspend fun fetchDetails(){
        try{
            val results = RetrofitClient.vehicleService.getVehiclesAndDocuments()
            vehiclesListState = VehicleListState.Success(results)
        } catch (e: Exception){
            println(e.message)
            vehiclesListState = VehicleListState.Error("Eroare ${e.message}")
        }
    }

    LaunchedEffect(Unit) {
        fetchDetails()
    }

    if (showDeleteWarning)
        AlertDialog(
            onDismissRequest = { showDeleteWarning = false },
            title = { Text(text = "Atenție!") },
            text = {
                Text(text = "Sigur vrei sa stergi autovehiculul?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteWarning = false
                        vehicleToDelete?.let { id ->
                            scope.launch {
                                try{
                                    val response = RetrofitClient.vehicleService.deleteVehicle(id)
                                    if(response.isSuccessful){
                                        fetchDetails()
                                    }

                                } catch (e: Exception){
                                    println(e.message)
                                    vehiclesListState = VehicleListState.Error("Eroare ${e.message}")
                                }
                            }
                        }
                        vehicleToDelete = null
                    }
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteWarning = false }) {
                    Text("Anuleaza")
                }
            }
        )

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                fetchDetails()
                isRefreshing = false
            }
        }
    ) {
        when(val state = vehiclesListState){
            is VehicleListState.Loading -> {
                if(!isRefreshing){
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is VehicleListState.Error -> {
                var showError by remember { mutableStateOf(false) }
                LaunchedEffect(state) {
                    showError = false
                    delay(3000)
                    showError = true
                }
                if(showError){
                    Box(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            is VehicleListState.Success -> {
                val userVehicles = remember(state.user.vehicles){
                    state.user.vehicles.toList()
                }
                if(userVehicles.isEmpty()){
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column (modifier = Modifier.padding(16.dp)){
                            Text("Nicun vehicul inregistrat!")
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onAddVehicle(state.user.id) }) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Adauga autovehicul")
                            }
                        }

                    }
                }
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    Row(verticalAlignment = Alignment.CenterVertically){
                        Text(text = "")
                        Spacer(modifier = Modifier.weight(0.33f))
                        Text(
                            text = "Autovehiculele mele",
                            style = MaterialTheme.typography.titleLarge,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.weight(0.33f))
                        Box{
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.AccountCircle, "Options")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {Text("Profil")},
                                    onClick = {
                                        expanded = false
                                        onProfileClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = {Text("Logout")},
                                    onClick = {
                                        expanded = false
                                        onLogout()
                                    }
                                )
                            }
                        }

                    }
                    if(!userVehicles.isEmpty()){

                        LazyColumn(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(userVehicles){ vehicle ->
                                VehicleCard(
                                    vehicle = vehicle,
                                    onEditClick = {
                                        onEditVehicle(vehicle.userId,vehicle.id)
                                    },
                                    onDeleteClick = {
                                        vehicleToDelete = vehicle.id
                                        showDeleteWarning = true
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ){
                            Button(onClick = { onAddVehicle(state.user.id) }) {
                                Icon(Icons.Default.Add, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Adauga autovehicul")
                            }
                        }
                    }

                }



            }
        }
    }
}