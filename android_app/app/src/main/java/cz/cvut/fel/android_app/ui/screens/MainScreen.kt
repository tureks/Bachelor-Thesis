package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.ui.components.base.AppButton
import cz.cvut.fel.android_app.ui.components.base.AppLogo
import cz.cvut.fel.android_app.ui.components.base.AppOutlinedButton
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel

private const val BATTERY_LOW_THRESHOLD = 20

@Composable
fun MainScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateToMeasurement: () -> Unit,
    onNavigateToDevice: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConnected = uiState.connectionState is BleConnectionState.Connected
    var showOverwriteDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        topBar = {
            AppTopBar(
                title = "Stream Measurement",
                titleContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppLogo(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Water Flow",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onNavigateToDevice,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isConnected) "Battery: ${uiState.batteryLevel}%" else "Connect",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                !isConnected -> MaterialTheme.colorScheme.primary
                                uiState.batteryLevel < BATTERY_LOW_THRESHOLD -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppButton(
                    text = "New Measurement",
                    icon = Icons.Default.Add,
                    onClick = {
                        if (uiState.measurement != null) {
                            showOverwriteDialog = true
                        } else {
                            viewModel.startNewMeasurement()
                            onNavigateToMeasurement()
                        }
                    }
                )

                if (uiState.measurement != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AppButton(
                        text = "Continue Measurement",
                        icon = Icons.Default.PlayArrow,
                        onClick = { onNavigateToMeasurement() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                if (!isConnected) {
                    AppOutlinedButton(
                        text = "Connect Device",
                        icon = Icons.Default.Bluetooth,
                        onClick = onNavigateToDevice
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AppOutlinedButton(
                    text = "Measurement History",
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = onNavigateToHistory
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppOutlinedButton(
                    text = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = onNavigateToSettings
                )
            }

        }
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text("New Measurement") },
            text = { Text("Starting a new measurement will discard the current unsaved data. Do you want to continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.startNewMeasurement()
                        onNavigateToMeasurement()
                        showOverwriteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Discard & Start New") }
            },
            dismissButton = {
                TextButton(onClick = { showOverwriteDialog = false }) { Text("Keep") }
            }
        )
    }
}