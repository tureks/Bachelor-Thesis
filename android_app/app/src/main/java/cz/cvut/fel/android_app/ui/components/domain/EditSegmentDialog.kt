package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamSegment
import cz.cvut.fel.android_app.domain.model.VelocityPoint
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import java.util.Locale

@Composable
fun EditSegmentDialog(
    segment: StreamSegment,
    points: List<VelocityPoint>,
    unit: MeasurementUnit,
    onDismiss: () -> Unit,
    onSave: (StreamSegment, List<VelocityPoint>) -> Unit
) {

    var widthInput by remember {
        mutableStateOf(UnitConverter.metersToInput(segment.segmentWidth, unit))
    }
    var depthInput by remember {
        mutableStateOf(UnitConverter.metersToInput(segment.depth, unit))
    }

    var localPoints by remember(points) { mutableStateOf(points) }
    var pointToDelete by remember { mutableStateOf<VelocityPoint?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Edit Segment #${segment.segmentNumber}",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = widthInput,
                        onValueChange = { widthInput = it },
                        label = { Text("Width (${UnitConverter.lengthLabel(unit)})") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = depthInput,
                        onValueChange = { depthInput = it },
                        label = { Text("Depth (${UnitConverter.lengthLabel(unit)})") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Velocity Points",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(localPoints, key = { it.id }) { point ->
                        PointEditRow(
                            point = point,
                            unit = unit,
                            onUpdate = { updatedPoint ->
                                localPoints = localPoints.map { if (it.id == updatedPoint.id) updatedPoint else it }
                            },
                            onDelete = {
                                pointToDelete = point
                            }
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(top = 16.dp, bottom = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val w = UnitConverter.displayToMeters(widthInput.toDoubleOrNull() ?: return@Button, unit)
                            val d = UnitConverter.displayToMeters(depthInput.toDoubleOrNull() ?: return@Button, unit)
                            onSave(segment.copy(segmentWidth = w, depth = d), localPoints)
                        },
                        enabled = widthInput.toDoubleOrNull() != null && depthInput.toDoubleOrNull() != null
                    ) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }

    pointToDelete?.let { point ->
        DeleteConfirmationDialog(
            onDismiss = { pointToDelete = null },
            onConfirm = {
                localPoints = localPoints.filter { it.id != point.id }
                pointToDelete = null
            },
            title = "Delete Point",
            message = "Are you sure you want to delete this velocity point?"
        )
    }
}

@Composable
private fun PointEditRow(
    point: VelocityPoint,
    unit: MeasurementUnit,
    onUpdate: (VelocityPoint) -> Unit,
    onDelete: () -> Unit
) {
    var velocityInput by remember { mutableStateOf(String.format(Locale.US, "%.2f", point.velocity)) }
    var heightInput by remember {
        mutableStateOf(
            String.format(Locale.US, "%.0f", point.measureHeight ?: 0.0)
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = velocityInput,
                onValueChange = {
                    velocityInput = it
                    it.toDoubleOrNull()?.let { v -> onUpdate(point.copy(velocity = v)) }
                },
                label = { Text("v (m/s)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = heightInput,
                onValueChange = {
                    heightInput = it
                    it.toDoubleOrNull()?.let { h ->
                        onUpdate(point.copy(measureHeight = h))
                    }
                },
                label = { Text("h (%)") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete point",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
