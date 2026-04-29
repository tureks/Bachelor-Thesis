package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var width by remember { mutableStateOf("") }
    var depth by remember { mutableStateOf("") }
    var selectedPointIds by remember { mutableStateOf(uiState.manualPoints.map { it.id }.toSet()) }
    var editingPoint by remember { mutableStateOf<ManualVelocityPoint?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Complete Segment",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            val w = width.toDoubleOrNull() ?: 0.0
                            val d = depth.toDoubleOrNull() ?: 0.0
                            viewModel.completeSegment(w, d, selectedPointIds)
                            onNavigateBack()
                        },
                        enabled = width.isNotEmpty() && depth.isNotEmpty() && selectedPointIds.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done")
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
            Text("Segment Dimensions", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = width,
                    onValueChange = { width = it },
                    label = "Width (m)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = DecimalKeyboard
                )
                AppTextField(
                    value = depth,
                    onValueChange = { depth = it },
                    label = if (uiState.preferredUnit == MeasurementUnit.HYDROMETRIC) "Depth (cm)" else "Depth (m)",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = DecimalKeyboard
                )
            }

            HorizontalDivider()

            Text("Velocity Points", style = MaterialTheme.typography.titleMedium)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
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
        }
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