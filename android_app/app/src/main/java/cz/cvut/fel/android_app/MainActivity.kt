package cz.cvut.fel.android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import cz.cvut.fel.android_app.ui.theme.Android_appTheme
import cz.cvut.fel.android_app.viewmodel.DeviceViewModel
import cz.cvut.fel.android_app.viewmodel.MeasurementViewModel
import cz.cvut.fel.android_app.viewmodel.UserViewModel

class MainActivity : ComponentActivity() {

    private val measurementViewModel: MeasurementViewModel by viewModels { MeasurementViewModel.Factory }
    private val deviceViewModel: DeviceViewModel by viewModels { DeviceViewModel.Factory }
    private val userViewModel: UserViewModel by viewModels { UserViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Android_appTheme {

            }
        }
    }
}