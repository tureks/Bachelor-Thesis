package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import cz.cvut.fel.android_app.R
import cz.cvut.fel.android_app.ui.components.ExportShareConfig
import cz.cvut.fel.android_app.ui.components.ExportShareEffect
import cz.cvut.fel.android_app.ui.components.SaveToDeviceEffect
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.domain.DeleteConfirmationDialog
import cz.cvut.fel.android_app.ui.components.domain.EditMeasurementMetadataDialog
import cz.cvut.fel.android_app.ui.components.domain.EditSegmentDialog
import cz.cvut.fel.android_app.ui.components.domain.SegmentSummaryItem
import cz.cvut.fel.android_app.ui.theme.Dimens
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import cz.cvut.fel.android_app.viewmodel.MeasurementDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MeasurementDetailsScreen(
    measurementId: Int,
    viewModel: MeasurementDetailViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val measurement = uiState.measurement
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US) }

    LaunchedEffect(measurementId) {
        viewModel.loadMeasurement(measurementId)
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val exportConfig = uiState.exportContent?.let { content ->
        ExportShareConfig(
            content = content,
            measurementNames = uiState.exportedMeasurementNames,
            userEmail = uiState.userEmail,
            operatorName = uiState.operatorName
        )
    }
    ExportShareEffect(
        config = exportConfig,
        snackbarHostState = snackbarHostState,
        onDone = { viewModel.clearExportContent() }
    )

    val downloadConfig = uiState.downloadContent?.let { content ->
        ExportShareConfig(
            content = content,
            measurementNames = uiState.exportedMeasurementNames,
            userEmail = uiState.userEmail,
            operatorName = uiState.operatorName
        )
    }
    SaveToDeviceEffect(
        config = downloadConfig,
        snackbarHostState = snackbarHostState,
        onDone = { viewModel.clearDownloadContent() }
    )

    var showEditMetadataDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = stringResource(R.string.screen_measurement_details),
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { viewModel.downloadMeasurement() }) {
                        Icon(Icons.Default.FileDownload, contentDescription = stringResource(R.string.export_save_to_device))
                    }
                    IconButton(onClick = { viewModel.exportMeasurement() }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.action_export))
                    }
                    IconButton(onClick = { showEditMetadataDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.action_edit))
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading || measurement == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(Dimens.paddingM)
                    .fillMaxSize()
            ) {
                Text(
                    text = measurement.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = dateFormat.format(Date(measurement.measureTimestamp)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.groupSpacing))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryStat(
                        label = stringResource(R.string.label_total_flow),
                        value = UnitConverter.formatFlow(measurement.totalFlow ?: 0.0, uiState.preferredUnit, decimals = 3)
                    )
                    SummaryStat(
                        label = stringResource(R.string.label_total_width),
                        value = UnitConverter.formatLength(measurement.totalWidth ?: 0.0, uiState.preferredUnit)
                    )
                }

                if (measurement.gpsLat != null && measurement.gpsLong != null) {
                    Spacer(modifier = Modifier.height(Dimens.paddingM))
                    Text(
                        text = stringResource(R.string.label_location, measurement.gpsLat, measurement.gpsLong),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!measurement.note.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(Dimens.paddingM))
                    Text(text = stringResource(R.string.label_note), style = MaterialTheme.typography.titleSmall)
                    Text(text = measurement.note, style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(Dimens.groupSpacing))

                Text(
                    text = stringResource(R.string.label_segments),
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Dimens.itemSpacing),
                    contentPadding = PaddingValues(top = Dimens.paddingS, bottom = Dimens.paddingM)
                ) {
                    items(uiState.segments, key = { it.id }) { segment ->
                        SegmentSummaryItem(
                            segment = segment,
                            unit = uiState.preferredUnit,
                            onClick = { viewModel.startEditingSegment(segment) }
                        )
                    }
                }
            }
        }
    }

    uiState.editingSegment?.let { segment ->
        EditSegmentDialog(
            segment = segment,
            points = uiState.editingPoints,
            unit = uiState.preferredUnit,
            onDismiss = { viewModel.stopEditingSegment() },
            onSave = { updatedSegment, points ->
                viewModel.updateSegmentDimensions(updatedSegment, points)
                viewModel.stopEditingSegment()
            }
        )
    }

    if (showEditMetadataDialog && measurement != null) {
        EditMeasurementMetadataDialog(
            measurement = measurement,
            onDismiss = { showEditMetadataDialog = false },
            onSave = { name, note ->
                viewModel.updateMetadata(name, note)
                showEditMetadataDialog = false
            }
        )
    }

    if (showDeleteDialog && measurement != null) {
        DeleteConfirmationDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message, measurement.name),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteMeasurement { onNavigateBack() }
            }
        )
    }
}

@Composable
private fun SummaryStat(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
    }
}