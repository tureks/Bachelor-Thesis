package cz.cvut.fel.android_app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.cvut.fel.android_app.ui.screens.CompleteSegmentScreen
import cz.cvut.fel.android_app.ui.screens.DeviceScreen
import cz.cvut.fel.android_app.ui.screens.FinalizeMeasurementScreen
import cz.cvut.fel.android_app.ui.screens.HistoryScreen
import cz.cvut.fel.android_app.ui.screens.MainScreen
import cz.cvut.fel.android_app.ui.screens.MeasurementDetailsScreen
import cz.cvut.fel.android_app.ui.screens.MeasurementScreen
import cz.cvut.fel.android_app.ui.screens.ReviewSegmentsScreen
import cz.cvut.fel.android_app.ui.screens.SettingsScreen
import cz.cvut.fel.android_app.viewmodel.BleViewModel
import cz.cvut.fel.android_app.viewmodel.CaptureViewModel
import cz.cvut.fel.android_app.viewmodel.DeviceViewModel
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import cz.cvut.fel.android_app.viewmodel.MeasurementDetailViewModel
import cz.cvut.fel.android_app.viewmodel.MeasurementViewModel
import cz.cvut.fel.android_app.viewmodel.UserViewModel

private fun NavHostController.safePopBackStack() {
    if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED && previousBackStackEntry != null) {
        popBackStack()
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    bleViewModel: BleViewModel,
    captureViewModel: CaptureViewModel,
    measurementViewModel: MeasurementViewModel,
    deviceViewModel: DeviceViewModel,
    historyViewModel: HistoryViewModel,
    userViewModel: UserViewModel
) {
    val navigateToMain: () -> Unit = {
        navController.navigate(Screen.Main.route) {
            popUpTo(Screen.Main.route) { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            enterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { it }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { -it / 3 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally(animationSpec = tween(300)) { -it }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(animationSpec = tween(300)) { it / 3 }
            }
        ) {
            composable(Screen.Main.route) {
                MainScreen(
                    bleViewModel = bleViewModel,
                    captureViewModel = captureViewModel,
                    measurementViewModel = measurementViewModel,
                    userViewModel = userViewModel,
                    onNavigateToMeasurement = { navController.navigate(Screen.Measurement.route) },
                    onNavigateToDevice = { navController.navigate(Screen.Device.route) { launchSingleTop = true } },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Measurement.route) {
                MeasurementScreen(
                    bleViewModel = bleViewModel,
                    captureViewModel = captureViewModel,
                    measurementViewModel = measurementViewModel,
                    userViewModel = userViewModel,
                    onNavigateBack = {
                        val captureState = captureViewModel.uiState.value
                        val measureState = measurementViewModel.uiState.value
                        if (captureState.manualPoints.isEmpty() && measureState.editingSegment == null && measureState.segments.isNotEmpty()) {
                            measurementViewModel.startEditingSegment(measureState.segments.last())
                        }
                        navController.safePopBackStack()
                    },
                    onNavigateToCompleteSegment = {
                        navController.navigate(Screen.CompleteSegment.route) { launchSingleTop = true }
                    }
                )
            }
            composable(Screen.CompleteSegment.route) {
                CompleteSegmentScreen(
                    captureViewModel = captureViewModel,
                    measurementViewModel = measurementViewModel,
                    onNavigateBack = { navController.safePopBackStack() },
                    onNavigateToMeasurement = {
                        navController.navigate(Screen.Measurement.route) { launchSingleTop = true }
                    },
                    onNavigateToFinalize = {
                        navController.navigate(Screen.ReviewSegments.route) { launchSingleTop = true }
                    },
                    onNavigateToMain = navigateToMain
                )
            }
            composable(Screen.ReviewSegments.route) {
                ReviewSegmentsScreen(
                    viewModel = measurementViewModel,
                    onNavigateBack = {
                        val state = measurementViewModel.uiState.value
                        if (state.editingSegment == null && state.segments.isNotEmpty()) {
                            measurementViewModel.startEditingSegment(state.segments.last())
                        }
                        navController.safePopBackStack()
                    },
                    onNavigateToMetadata = {
                        navController.navigate(Screen.FinalizeMeasurement.route) { launchSingleTop = true }
                    },
                    onNavigateToMain = navigateToMain
                )
            }
            composable(Screen.FinalizeMeasurement.route) {
                FinalizeMeasurementScreen(
                    viewModel = measurementViewModel,
                    onNavigateBack = { navController.safePopBackStack() },
                    onComplete = navigateToMain
                )
            }
            composable(Screen.Device.route) {
                DeviceScreen(
                    viewModel = deviceViewModel,
                    onNavigateBack = { navController.safePopBackStack() }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = historyViewModel,
                    onNavigateBack = { navController.safePopBackStack() },
                    onNavigateToDetails = { id ->
                        navController.navigate(Screen.MeasurementDetails.createRoute(id))
                    }
                )
            }
            composable(
                route = Screen.MeasurementDetails.route,
                arguments = listOf(navArgument("measurementId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("measurementId")
                    ?.takeIf { it > 0 } ?: return@composable
                val detailViewModel: MeasurementDetailViewModel = viewModel(factory = MeasurementDetailViewModel.Factory)
                MeasurementDetailsScreen(
                    measurementId = id,
                    viewModel = detailViewModel,
                    onNavigateBack = { navController.safePopBackStack() }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = userViewModel,
                    onNavigateBack = { navController.safePopBackStack() }
                )
            }
        }
    }
}