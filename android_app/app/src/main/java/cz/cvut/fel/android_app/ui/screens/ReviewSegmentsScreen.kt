package cz.cvut.fel.android_app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import cz.cvut.fel.android_app.ui.components.domain.CancelMeasurementDialog
import cz.cvut.fel.android_app.ui.components.domain.EditSegmentDialog
import cz.cvut.fel.android_app.ui.components.domain.SegmentSummaryItem
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel

@Composable
fun ReviewSegmentsScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMetadata: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    val unit = uiState.preferredUnit

    // System back button follows the same logic as the top-bar back button
    BackHandler { onNavigateBack() }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Review Segments",
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
                onClick = onNavigateToMetadata,
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                text = { Text("Save Measurement") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = UnitConverter.formatFlow(uiState.totals?.totalFlow ?: 0.0, unit),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total Stream Flow",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Segments  —  tap to edit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
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

    if (showCancelDialog) {
        CancelMeasurementDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                viewModel.cancelMeasurement()
                onNavigateToMain()
            }
        )
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
}