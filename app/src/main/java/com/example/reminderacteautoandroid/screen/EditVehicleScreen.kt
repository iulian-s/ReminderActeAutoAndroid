package com.example.reminderacteautoandroid.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun VehicleScreen(
    vehicleId: Long,
    onBack: () -> Unit
) {
    Text("Vehiculul $vehicleId")
}