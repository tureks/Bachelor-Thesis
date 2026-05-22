package cz.cvut.fel.android_app

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import cz.cvut.fel.android_app.ui.AppNavigation
import cz.cvut.fel.android_app.ui.theme.Android_appTheme
import cz.cvut.fel.android_app.viewmodel.BleViewModel
import cz.cvut.fel.android_app.viewmodel.CaptureViewModel
import cz.cvut.fel.android_app.viewmodel.DeviceViewModel
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import cz.cvut.fel.android_app.viewmodel.MeasurementViewModel
import cz.cvut.fel.android_app.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    private val bleViewModel: BleViewModel by viewModels { BleViewModel.Factory }
    private val captureViewModel: CaptureViewModel by viewModels { CaptureViewModel.Factory }
    private val measurementViewModel: MeasurementViewModel by viewModels { MeasurementViewModel.Factory }
    private val deviceViewModel: DeviceViewModel by viewModels { DeviceViewModel.Factory }
    private val userViewModel: UserViewModel by viewModels { UserViewModel.Factory }
    private val historyViewModel: HistoryViewModel by viewModels { HistoryViewModel.Factory }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) deviceViewModel.refreshBluetoothState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        )

        enableEdgeToEdge()
        setContent {
            Android_appTheme {
                val navController = rememberNavController()
                AppNavigation(
                    navController = navController,
                    bleViewModel = bleViewModel,
                    captureViewModel = captureViewModel,
                    measurementViewModel = measurementViewModel,
                    deviceViewModel = deviceViewModel,
                    historyViewModel = historyViewModel,
                    userViewModel = userViewModel
                )
            }
        }
    }
}