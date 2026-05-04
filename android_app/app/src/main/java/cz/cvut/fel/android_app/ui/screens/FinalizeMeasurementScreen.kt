package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.ui.components.base.AppTextField
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import java.util.Locale

@Composable
fun FinalizeMeasurementScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var name by remember(uiState.measurement?.id) { mutableStateOf(uiState.measurement?.name ?: "") }
    var note by remember(uiState.measurement?.id) { mutableStateOf(uiState.measurement?.note ?: "") }
    var showCancelDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val isNameValid = name.isNotBlank()
    val location = uiState.currentLocation
    val isHydrometric = uiState.preferredUnit == MeasurementUnit.HYDROMETRIC
    val totals = uiState.totals

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        topBar = {
            AppTopBar(
                title = "Save Measurement",
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(onClick = { showCancelDialog = true }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Totals summary - Only show here
            if (totals != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Flow", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = String.format(Locale.US, "%.3f %s",
                                    if (isHydrometric) totals.totalFlow * 1000.0 else totals.totalFlow,
                                    if (isHydrometric) "l/s" else "m³/s"
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Width", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = String.format(Locale.US, "%.2f %s",
                                    if (isHydrometric) totals.totalWidth * 100.0 else totals.totalWidth,
                                    if (isHydrometric) "cm" else "m"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Max Depth", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = String.format(Locale.US, "%.2f %s",
                                    if (isHydrometric) totals.maxDepth * 100.0 else totals.maxDepth,
                                    if (isHydrometric) "cm" else "m"
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // GPS location card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (location != null)
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (location != null)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                    if (location != null) {
                        Column {
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%.5f°,  %.5f°",
                                    location.latitude,
                                    location.longitude
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = String.format(Locale.US, "±%.0f m accuracy", location.accuracy),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                        Text(
                            text = "Location unavailable",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Location Name",
                placeholder = "e.g. Vltava – Prague",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            AppTextField(
                value = note,
                onValueChange = { note = it },
                label = "Notes",
                placeholder = "Optional notes about the measurement",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Surface(
                onClick = {
                    if (isNameValid) {
                        viewModel.finalizeMeasurement(name, note)
                        onComplete()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                color = if (isNameValid) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 4.dp,
                border = BorderStroke(0.3.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isNameValid
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Save Measurement",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isNameValid) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Measurement") },
            text = { Text("Are you sure you want to cancel this measurement? All captured data will be lost.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cancelMeasurement()
                    onNavigateToMain()
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
    }
}