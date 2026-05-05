package cz.cvut.fel.android_app.ui.screens

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import cz.cvut.fel.android_app.ui.components.domain.EditMeasurementMetadataDialog
import cz.cvut.fel.android_app.ui.components.domain.EditSegmentDialog
import cz.cvut.fel.android_app.ui.components.domain.SegmentSummaryItem
import cz.cvut.fel.android_app.viewmodel.HistoryViewModel
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import java.io.File
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
    val context = LocalContext.current

    LaunchedEffect(historyState.exportContent) {
        historyState.exportContent?.let { content ->
            val names = historyState.exportedMeasurementNames
            val safeBase = names.firstOrNull()
                ?.replace(Regex("[^a-zA-Z0-9_-]"), "_")
                ?.take(40) ?: "measurement"
            val file = File(context.cacheDir, "${safeBase}_export.csv")
            file.writeText(content)
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

            val subject = "Stream Measurement Report: ${names.firstOrNull() ?: ""}"
            val body = buildString {
                append("Please find attached the stream gauging measurement report")
                names.firstOrNull()?.let { append(" for \"$it\"") }
                append(".")
                if (historyState.operatorName.isNotEmpty()) append("\n\nOperator: ${historyState.operatorName}")
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                if (historyState.userEmail.isNotEmpty()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(historyState.userEmail))
                }
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                context.startActivity(Intent.createChooser(intent, "Send Report via Email"))
            } catch (_: android.content.ActivityNotFoundException) { }
            historyViewModel.clearExportContent()
        }
    }

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

    val unit = measurementState.preferredUnit
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US)

    var showEditMetadataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Measurement Details",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { measurement?.let { historyViewModel.exportMeasurement(it) } }) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                    IconButton(onClick = { showEditMetadataDialog = true }) {
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
                        value = UnitConverter.formatFlow(measurement.totalFlow ?: 0.0, unit, decimals = 3)
                    )
                    SummaryStat(
                        label = "Total Width",
                        value = UnitConverter.formatLength(measurement.totalWidth ?: 0.0, unit)
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

    if (showEditMetadataDialog && measurement != null) {
        EditMeasurementMetadataDialog(
            measurement = measurement,
            onDismiss = { showEditMetadataDialog = false },
            onSave = { name, note ->
                measurementViewModel.updateMeasurementMetadata(name, note)
                showEditMetadataDialog = false
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
