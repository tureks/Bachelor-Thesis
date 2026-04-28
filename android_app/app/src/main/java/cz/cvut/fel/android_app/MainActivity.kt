package cz.cvut.fel.android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import cz.cvut.fel.android_app.ui.AppNavigation
import cz.cvut.fel.android_app.ui.theme.Android_appTheme
import cz.cvut.fel.android_app.viewmodel.DeviceViewModel
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import cz.cvut.fel.android_app.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    private val measurementViewModel: StreamMeasurementViewModel by viewModels { StreamMeasurementViewModel.Factory }
    private val deviceViewModel: DeviceViewModel by viewModels { DeviceViewModel.Factory }
    private val userViewModel: UserViewModel by viewModels { UserViewModel.Factory }
    private val historyViewModel: HistoryViewModel by viewModels { HistoryViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_appTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    measurementViewModel = measurementViewModel,
                    historyViewModel = historyViewModel
                )
            }
        }
    }
}