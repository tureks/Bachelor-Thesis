package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.ui.components.base.AppButton
import cz.cvut.fel.android_app.ui.components.base.AppLogo
import cz.cvut.fel.android_app.ui.components.base.AppOutlinedButton
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.BleViewModel
import cz.cvut.fel.android_app.viewmodel.CaptureViewModel
import cz.cvut.fel.android_app.viewmodel.MeasurementViewModel
import cz.cvut.fel.android_app.viewmodel.UserViewModel

private const val BATTERY_LOW_THRESHOLD = 20

@Composable
fun MainScreen(
    bleViewModel: BleViewModel,
    captureViewModel: CaptureViewModel,
    measurementViewModel: MeasurementViewModel,
    userViewModel: UserViewModel,
    onNavigateToMeasurement: () -> Unit,
    onNavigateToDevice: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val bleState by bleViewModel.uiState.collectAsState()
    val measureState by measurementViewModel.uiState.collectAsState()
    val userState by userViewModel.uiState.collectAsState()
    val devMode = userState.profile?.developerMode ?: false
    val isConnected = devMode || bleState.connectionState is BleConnectionState.Connected
    val isProbeConnected = devMode || bleState.probeConnected
    var showOverwriteDialog by remember { mutableStateOf(false) }

    Scaffold(
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
                    if (isConnected) {
                        Icon(
                            imageVector = if (isProbeConnected) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = if (isProbeConnected) "Probe connected" else "Probe not connected",
                            tint = if (isProbeConnected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
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
                            text = if (isConnected) "Battery: ${bleState.batteryLevel}%" else "Connect",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                !isConnected -> MaterialTheme.colorScheme.primary
                                bleState.batteryLevel < BATTERY_LOW_THRESHOLD -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isConnected) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(
                            "Connect a device to start measuring",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            } else if (!isProbeConnected) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(
                            "Connect probe to the measuring device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AppButton(
                        text = "New Measurement",
                        icon = Icons.Default.Add,
                        enabled = isConnected,
                        onClick = {
                            if (measureState.measurement != null) {
                                showOverwriteDialog = true
                            } else {
                                measurementViewModel.startNewMeasurement()
                                onNavigateToMeasurement()
                            }
                        }
                    )

                    if (measureState.measurement != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        AppButton(
                            text = "Continue Measurement",
                            icon = Icons.Default.PlayArrow,
                            enabled = isConnected,
                            onClick = { onNavigateToMeasurement() }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

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
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text("New Measurement") },
            text = { Text("Starting a new measurement will discard the current unsaved data. Do you want to continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        measurementViewModel.startNewMeasurement()
                        captureViewModel.reset()
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