package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.ui.components.base.AppButton
import cz.cvut.fel.android_app.ui.components.base.AppTextField
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel

@Composable
fun MeasurementScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(
                title = "New Measurement",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.finalizeMeasurement("", "") }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Stream Information",
                style = MaterialTheme.typography.titleLarge
            )

            AppTextField(
                value = "", // Bind to state
                onValueChange = {},
                label = "Location Name",
                placeholder = "e.g. Vltava - Prague"
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = "", // Bind to state
                    onValueChange = {},
                    label = "Width (m)",
                    modifier = Modifier.weight(1f)
                )
                AppTextField(
                    value = "", // Bind to state
                    onValueChange = {},
                    label = "Depth (m)",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            AppButton(
                text = "Start Velocity Capture",
                icon = Icons.Default.PlayArrow,
                onClick = { viewModel.startCapture() }
            )
        }
    }
}
