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
    object VehicleScreen: DashboardRoutes("vehicles/{vehicleId}"){
        fun editVehicleRoute(vehicleId: Long) = "vehicles/$vehicleId"
    }
    object AddVehicleScreen: DashboardRoutes("vehicles/add")

}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen() {
    val dashboardController = rememberNavController()

    val onEditVehicle: (Long) -> Unit = { vehicleId ->
        dashboardController.navigate(DashboardRoutes.VehicleScreen.editVehicleRoute(vehicleId))
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
                    onAddVehicle = {dashboardController.navigate(DashboardRoutes.AddVehicleScreen.route)}
                )
            }

            composable(
                DashboardRoutes.VehicleScreen.route,
                arguments = listOf(navArgument("vehicleId")
                    { type =
                    NavType.LongType
                    }
                )
            ){ backStackEntry ->
                val vehicleId = backStackEntry.arguments?.getLong("vehicleId")
                if(vehicleId != null){
                    VehicleScreen(
                        vehicleId = vehicleId,
                        onBack = {dashboardController.popBackStack()}
                    )
                } else {
                    Text("Eroare, vehiculul $vehicleId nu exista!")
                }
            }

            composable(DashboardRoutes.AddVehicleScreen.route){
                AddVehicleScreen()
            }
        }
    }
}