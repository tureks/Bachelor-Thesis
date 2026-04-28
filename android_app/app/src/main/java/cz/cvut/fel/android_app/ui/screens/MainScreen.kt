package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.ui.components.base.AppButton
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel

@Composable
fun MainScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateToMeasurement: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Stream Gauging",
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Text(
                            text = "Battery: ${uiState.batteryLevel}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (uiState.batteryLevel < 20) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppButton(
                text = "New Measurement",
                icon = Icons.Default.Add,
                onClick = {
                    viewModel.startNewMeasurement()
                    onNavigateToMeasurement()
                }
            )

            if (uiState.measurement != null) {
                Spacer(modifier = Modifier.height(16.dp))
                AppButton(
                    text = "Continue Measurement",
                    icon = Icons.Default.PlayArrow,
                    onClick = onNavigateToMeasurement
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            AppButton(
                text = "Measurement History",
                icon = Icons.Default.List,
                onClick = onNavigateToHistory,
                isPrimary = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            AppButton(
                text = "Settings",
                icon = Icons.Default.Settings,
                onClick = onNavigateToSettings,
                isPrimary = false
            )
        }
    }
}
