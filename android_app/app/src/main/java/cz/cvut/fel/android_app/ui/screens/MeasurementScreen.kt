package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.domain.*
import cz.cvut.fel.android_app.viewmodel.ManualVelocityPoint
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import java.util.Locale

@Composable
fun MeasurementScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCompleteSegment: () -> Unit,
    onNavigateToFinalize: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPoint by remember { mutableStateOf<ManualVelocityPoint?>(null) }
    var showTimeWindowDialog by remember { mutableStateOf(false) }
    val onCancel = { viewModel.cancelMeasurement(); onNavigateBack() }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Segment Capture",
                onNavigateBack = onCancel,
                actions = {
                    IconButton(onClick = onNavigateToFinalize) {
                        Icon(Icons.Default.Check, contentDescription = "Finalize")
                    }
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCompleteSegment,
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                text = { Text("Complete Segment") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clickable { viewModel.addManualPoint() }
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = String.format(Locale.US, "%.3f m/s", uiState.windowAverage),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Live Average",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(onClick = { showTimeWindowDialog = true }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Settings, contentDescription = "Time Window")
                        Text("${uiState.timeWindow}s", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            VelocityGraph(
                readings = uiState.recentReadings,
                windowSeconds = uiState.timeWindow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manual Velocity Points",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = { viewModel.addManualPoint() }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Capture Current")
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.manualPoints, key = { it.id }) { point ->
                    ManualPointItem(
                        point = point,
                        unit = uiState.preferredUnit,
                        onClick = { selectedPoint = point }
                    )
                }

                if (uiState.segments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Completed Segments",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(uiState.segments, key = { it.id }) { segment ->
                        SegmentSummaryItem(segment = segment, unit = uiState.preferredUnit)
                    }
                }
            }
        }
    }

    if (showTimeWindowDialog) {
        TimeWindowDialog(
            currentWindow = uiState.timeWindow,
            onDismiss = { showTimeWindowDialog = false },
            onConfirm = {
                viewModel.setTimeWindow(it)
                showTimeWindowDialog = false
            }
        )
    }

    selectedPoint?.let { point ->
        EditPointDialog(
            point = point,
            unit = uiState.preferredUnit,
            onDismiss = { selectedPoint = null },
            onUpdateHeight = { height ->
                viewModel.updateManualPointHeight(point.id, height)
                selectedPoint = null
            },
            onDelete = {
                viewModel.deleteManualPoint(point.id)
                selectedPoint = null
            }
        )
    }
}