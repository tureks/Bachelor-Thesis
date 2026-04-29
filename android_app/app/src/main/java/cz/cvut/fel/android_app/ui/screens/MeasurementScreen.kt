package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
    onNavigateToCompleteSegment: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPoint by remember { mutableStateOf<ManualVelocityPoint?>(null) }
    var showTimeWindowDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Segment Capture",
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(onClick = { showCancelDialog = true }) {
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.addManualPoint() },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(Locale.US, "%.2f m/s", uiState.windowAverage),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Live Average",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder(enabled = true).copy(width = 0.5.dp)
            ) {
                VelocityGraph(
                    readings = uiState.recentReadings,
                    windowSeconds = uiState.timeWindow,
                    onTap = { viewModel.addManualPoint() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Velocity Points",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.manualPoints.isEmpty() && uiState.segments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tap the velocity reading or graph\nto capture a point",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Surface(
                    onClick = { showTimeWindowDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Time Window Settings",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
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
                    onNavigateBack()
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
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