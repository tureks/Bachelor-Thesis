package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.base.SegmentNumberBadge
import cz.cvut.fel.android_app.ui.components.domain.EditSegmentDialog
import cz.cvut.fel.android_app.ui.components.domain.SegmentSummaryItem
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeasurementDetailsScreen(
    measurementId: Int,
    historyViewModel: HistoryViewModel,
    measurementViewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onEditMeasurement: () -> Unit
) {
    val historyState by historyViewModel.uiState.collectAsState()
    val measurementState by measurementViewModel.uiState.collectAsState()
    
    val measurement = remember(measurementId, historyState.measurements, measurementState.measurement) {
        if (measurementState.measurement?.id == measurementId) {
            measurementState.measurement
        } else {
            historyState.measurements.find { it.id == measurementId }
        }
    }

    val segments = remember(measurementId, measurementState.measurement, measurementState.segments) {
        if (measurementState.measurement?.id == measurementId) {
            measurementState.segments
        } else {
            emptyList() // We need to load them if they are not the current ones
        }
    }

    LaunchedEffect(measurementId) {
        if (measurementState.measurement?.id != measurementId) {
            measurementViewModel.loadMeasurementForEditing(measurementId)
        }
    }

    val isHydrometric = measurementState.preferredUnit == MeasurementUnit.HYDROMETRIC
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Measurement Details",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { measurement?.let { historyViewModel.exportMeasurement(it) } }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(onClick = onEditMeasurement) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    var showDeleteConfirmation by remember { mutableStateOf(false) }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }

                    if (showDeleteConfirmation) {
                        cz.cvut.fel.android_app.ui.components.domain.DeleteConfirmationDialog(
                            onDismiss = { showDeleteConfirmation = false },
                            onConfirm = {
                                measurement?.let {
                                    historyViewModel.deleteMeasurement(it)
                                    onNavigateBack()
                                }
                                showDeleteConfirmation = false
                            },
                            title = "Delete Measurement",
                            message = "Are you sure you want to delete measurement \"${measurement?.name}\"?"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (measurement == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = measurement.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dateFormat.format(Date(measurement.measureTimestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStat(
                        label = "Total Flow",
                        value = String.format(Locale.US, "%.3f %s",
                            if (isHydrometric) (measurement.totalFlow ?: 0.0) * 1000.0 else (measurement.totalFlow ?: 0.0),
                            if (isHydrometric) "l/s" else "m³/s"
                        )
                    )
                    SummaryStat(
                        label = "Total Width",
                        value = String.format(Locale.US, "%.2f %s",
                            if (isHydrometric) (measurement.totalWidth ?: 0.0) * 100.0 else (measurement.totalWidth ?: 0.0),
                            if (isHydrometric) "cm" else "m"
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (measurement.gpsLat != null && measurement.gpsLong != null) {
                    Text(
                        text = "Location: ${String.format(Locale.US, "%.5f, %.5f", measurement.gpsLat, measurement.gpsLong)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                if (!measurement.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Note", style = MaterialTheme.typography.titleSmall)
                    Text(text = measurement.note, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Segments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                ) {
                    items(segments, key = { it.id }) { segment ->
                        SegmentSummaryItem(
                            segment = segment,
                            unit = measurementState.preferredUnit,
                            onClick = { measurementViewModel.startEditingSegment(segment) }
                        )
                    }
                }
            }
        }
    }

    measurementState.editingSegment?.let { segment ->
        EditSegmentDialog(
            segment = segment,
            points = measurementState.editingPoints,
            unit = measurementState.preferredUnit,
            onDismiss = { measurementViewModel.stopEditingSegment() },
            onSave = { updatedSegment, points ->
                measurementViewModel.updateSegmentDimensions(updatedSegment, points)
                measurementViewModel.stopEditingSegment()
            }
        )
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}
