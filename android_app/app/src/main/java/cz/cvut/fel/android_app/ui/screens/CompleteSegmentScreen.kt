package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.ui.components.base.AppTextField
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.domain.*
import cz.cvut.fel.android_app.viewmodel.ManualVelocityPoint
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel

private val DecimalKeyboard = KeyboardOptions(keyboardType = KeyboardType.Decimal)

@Composable
fun CompleteSegmentScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToMeasurement: () -> Unit,
    onNavigateToFinalize: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPointIds by remember { mutableStateOf(uiState.manualPoints.map { it.id }.toSet()) }

    // Keep selection in sync when navigating back or when points change
    LaunchedEffect(uiState.manualPoints) {
        selectedPointIds = uiState.manualPoints.map { it.id }.toSet()
    }

    var editingPoint by remember { mutableStateOf<ManualVelocityPoint?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val width = uiState.currentWidth
    val depth = uiState.currentDepth

    val isValid = width.isNotEmpty() && depth.isNotEmpty() && (selectedPointIds.isNotEmpty() || uiState.editingSegment != null)
    val isHydrometric = uiState.preferredUnit == MeasurementUnit.HYDROMETRIC
    
    val segmentNumber = uiState.editingSegment?.segmentNumber ?: (uiState.segments.size + 1)

    val calculatedFlow = remember(width, depth, selectedPointIds, uiState.manualPoints, isHydrometric) {
        val wRaw = width.toDoubleOrNull() ?: 0.0
        val dRaw = depth.toDoubleOrNull() ?: 0.0
        
        val w = if (isHydrometric) wRaw / 100.0 else wRaw
        val d = if (isHydrometric) dRaw / 100.0 else dRaw
        
        val selectedPoints = uiState.manualPoints.filter { selectedPointIds.contains(it.id) }
        val avgV = if (selectedPoints.isEmpty()) {
            uiState.editingSegment?.averageVelocity ?: 0.0
        } else {
            selectedPoints.map { it.velocity }.average()
        }
        val flowM3 = avgV * w * d
        
        if (isHydrometric) flowM3 * 1000.0 else flowM3
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Segment",
                titleContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Segment")
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = androidx.compose.ui.graphics.Color.Transparent,
                            border = BorderStroke(0.3.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = segmentNumber.toString(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                },
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
                onClick = {
                    if (isValid) {
                        val w = width.toDoubleOrNull() ?: 0.0
                        val d = depth.toDoubleOrNull() ?: 0.0
                        viewModel.completeSegment(w, d, selectedPointIds)
                        onNavigateToMeasurement()
                    }
                },
                icon = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) },
                text = { Text("Next Segment") },
                containerColor = if (isValid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isValid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
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
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.2f %s", calculatedFlow, if (isHydrometric) "l/s" else "m³/s"),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Estimated Segment Flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Segment Dimensions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = width,
                    onValueChange = { viewModel.setCurrentWidth(it) },
                    label = if (isHydrometric) "Width (cm)" else "Width (m)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = DecimalKeyboard
                )
                AppTextField(
                    value = depth,
                    onValueChange = { viewModel.setCurrentDepth(it) },
                    label = if (isHydrometric) "Depth (cm)" else "Depth (m)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = DecimalKeyboard
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Velocity Points",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.manualPoints, key = { it.id }) { point ->
                    SegmentPointItem(
                        point = point,
                        unit = uiState.preferredUnit,
                        isSelected = selectedPointIds.contains(point.id),
                        onToggle = {
                            selectedPointIds = if (selectedPointIds.contains(point.id)) {
                                selectedPointIds - point.id
                            } else {
                                selectedPointIds + point.id
                            }
                        },
                        onEdit = { editingPoint = point },
                        onDelete = { viewModel.deleteManualPoint(point.id) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Surface(
                    onClick = {
                        if (isValid) {
                            val w = width.toDoubleOrNull() ?: 0.0
                            val d = depth.toDoubleOrNull() ?: 0.0
                            viewModel.completeSegment(w, d, selectedPointIds)
                            onNavigateToFinalize()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 6.dp,
                    border = BorderStroke(0.3.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .height(56.dp)
                        .align(Alignment.CenterStart),
                    enabled = isValid
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .fillMaxHeight()
                    ) {
                        Text(
                            "Complete Measurement",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isValid) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
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
                    onNavigateToMain()
                    showCancelDialog = false
                }) { Text("Yes") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
    }

    editingPoint?.let { point ->
        EditPointDialog(
            point = point,
            unit = uiState.preferredUnit,
            onDismiss = { editingPoint = null },
            onUpdateHeight = { newHeight ->
                viewModel.updateManualPointHeight(point.id, newHeight)
                editingPoint = null
            },
            onDelete = {
                viewModel.deleteManualPoint(point.id)
                editingPoint = null
            }
        )
    }
}
