package com.example.reminderacteautoandroid.screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


sealed class DashboardRoutes(val route: String){
    object MainScreen: DashboardRoutes("vehicles/list")
    object EditVehicleScreen: DashboardRoutes("vehicles/{userId}/{vehicleId}"){
        fun editVehicleRoute(userId: Long, vehicleId: Long) = "vehicles/$userId/$vehicleId"
    }
    object AddVehicleScreen: DashboardRoutes("vehicles/add/{userId}"){
        fun addVehicleRoute(userId: Long) = "vehicles/add/$userId"
    }

}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen() {
    val dashboardController = rememberNavController()

    val onEditVehicle: (Long, Long) -> Unit = { userId, vehicleId ->
        dashboardController.navigate(DashboardRoutes.EditVehicleScreen.editVehicleRoute(userId,vehicleId))
    }
    val onAddVehicle: (Long) -> Unit = { userId ->
        dashboardController.navigate(DashboardRoutes.AddVehicleScreen.addVehicleRoute(userId))
    }
    Scaffold { paddingValues ->
        NavHost(
            navController = dashboardController,
            startDestination = DashboardRoutes.MainScreen.route,
            modifier = Modifier.padding(paddingValues)
        ){
            composable(DashboardRoutes.MainScreen.route){
                MainScreen(
                    onEditVehicle = onEditVehicle,
                    onAddVehicle = onAddVehicle
                )
            }

            composable(
                DashboardRoutes.EditVehicleScreen.route,
                arguments = listOf(navArgument("vehicleId"){ type =
                    NavType.LongType
                },
                    navArgument("userId"){ type =
                        NavType.LongType
                    }

                )
            ){ backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getLong("vehicleId")
                val userId = backStackEntry.arguments?.getLong("userId")
                if(vehicleId != null && userId != null){
                    EditVehicleScreen(
                        vehicleId = vehicleId,
                        userId = userId,
                        onEditSuccessful = {dashboardController.navigate(DashboardRoutes.MainScreen.route)}
                    )
                } else {
                    Text("Eroare, vehiculul $vehicleId nu exista!")
                }
            }

            composable(DashboardRoutes.AddVehicleScreen.route,
                arguments = listOf(navArgument("userId"){
                    type = NavType.LongType
                })){ backStackEntry ->
                val userId = backStackEntry.arguments?.getLong("userId")
                if(userId != null){
                    AddVehicleScreen(
                        userId,
                        onAddSuccess = { dashboardController.navigate(DashboardRoutes.MainScreen.route) }
                    )
                } else{
                    Text("Eroare, userul $userId este invalid!")
                }

            }
        }
    }
}