package cz.cvut.fel.android_app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.ui.components.base.AppButton
import cz.cvut.fel.android_app.ui.components.base.AppOutlinedButton
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import kotlinx.coroutines.delay

private const val BATTERY_LOW_THRESHOLD = 20
private const val HINT_TIMEOUT_MS = 5_000L

@Composable
fun MainScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateToMeasurement: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isConnected = true // TODO: restore to `uiState.connectionState is BleConnectionState.Connected`
    var showConnectionHint by remember { mutableStateOf(false) }
    var showOverwriteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(showConnectionHint) {
        if (showConnectionHint) {
            delay(HINT_TIMEOUT_MS)
            showConnectionHint = false
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Stream Measurement",
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = if (isConnected) "Battery: ${uiState.batteryLevel}%" else "Not Connected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when {
                                !isConnected -> MaterialTheme.colorScheme.onSurfaceVariant
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
                    enabled = true, // TODO: restore to `enabled = isConnected` after device testing
                    onClick = {
                        if (!isConnected) {
                            showConnectionHint = true
                        } else if (uiState.measurement != null) {
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
                        enabled = true, // TODO: restore to `enabled = isConnected` after device testing
                        onClick = {
                            if (!isConnected) {
                                showConnectionHint = true
                            } else {
                                onNavigateToMeasurement()
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                AppOutlinedButton(
                    text = "Measurement History",
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = {
                        showConnectionHint = false
                        onNavigateToHistory()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppOutlinedButton(
                    text = "Settings",
                    icon = Icons.Default.Settings,
                    onClick = {
                        showConnectionHint = false
                        onNavigateToSettings()
                    }
                )
            }

            AnimatedVisibility(
                visible = showConnectionHint,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable { showConnectionHint = false },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Connect to a measuring device to start the measurement",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }

    if (showOverwriteDialog) {
        AlertDialog(
            onDismissRequest = { showOverwriteDialog = false },
            title = { Text("New Measurement") },
            text = { Text("Starting a new measurement will delete the previous one. Do you want to continue?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.startNewMeasurement()
                    onNavigateToMeasurement()
                    showOverwriteDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showOverwriteDialog = false }) { Text("No") }
            }
        )
    }
}