package cz.cvut.fel.android_app.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import cz.cvut.fel.android_app.ui.screens.FinalizeMeasurementScreen
import cz.cvut.fel.android_app.ui.screens.HistoryScreen
import cz.cvut.fel.android_app.ui.screens.MainScreen
import cz.cvut.fel.android_app.ui.screens.MeasurementScreen
import cz.cvut.fel.android_app.ui.screens.SettingsScreen
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    measurementViewModel: StreamMeasurementViewModel,
    historyViewModel: HistoryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MainScreen.route
    ) {
        composable(Screen.MainScreen.route) {
            MainScreen(
                viewModel = measurementViewModel,
                onNavigateToMeasurement = { navController.navigate(Screen.Measurement.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Measurement.route) {
            MeasurementScreen(
                viewModel = measurementViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFinalize = { navController.navigate(Screen.FinalizeMeasurement.route) }
            )
        }
        composable(Screen.FinalizeMeasurement.route) {
            FinalizeMeasurementScreen(
                viewModel = measurementViewModel,
                onNavigateBack = { navController.popBackStack() },
                onComplete = {
                    navController.popBackStack(Screen.MainScreen.route, inclusive = false)
                }
            )
        }
        composable(Screen.History.route) {
            HistoryScreen(
                viewModel = historyViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMeasurement = { id -> 
                    // Handle navigation to specific measurement if needed
                }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        // Add Device screen composable when ready
    }
}
