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
import java.util.Locale

@Composable
fun EditSegmentDialog(
    segment: StreamSegment,
    points: List<VelocityPoint>,
    unit: MeasurementUnit,
    onDismiss: () -> Unit,
    onSave: (StreamSegment, List<VelocityPoint>) -> Unit
) {
    val isHydrometric = unit == MeasurementUnit.HYDROMETRIC

    var widthInput by remember {
        mutableStateOf(
            if (isHydrometric) (segment.segmentWidth * 100).toString()
            else segment.segmentWidth.toString()
        )
    }
    var depthInput by remember {
        mutableStateOf(
            if (isHydrometric) (segment.depth * 100).toString()
            else segment.depth.toString()
        )
    }

    var localPoints by remember { mutableStateOf(points) }
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
                        label = { Text(if (isHydrometric) "Width (cm)" else "Width (m)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = depthInput,
                        onValueChange = { depthInput = it },
                        label = { Text(if (isHydrometric) "Depth (cm)" else "Depth (m)") },
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
                            val w = widthInput.toDoubleOrNull()
                                ?: if (isHydrometric) segment.segmentWidth * 100.0 else segment.segmentWidth
                            val d = depthInput.toDoubleOrNull()
                                ?: if (isHydrometric) segment.depth * 100.0 else segment.depth
                            onSave(segment.copy(segmentWidth = w, depth = d), localPoints)
                        }
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
    val isHydrometric = unit == MeasurementUnit.HYDROMETRIC

    var velocityInput by remember { mutableStateOf(point.velocity.toString()) }
    var heightInput by remember {
        mutableStateOf(
            String.format(Locale.US, "%.2f", (point.measureHeight ?: 0.0) / 100.0)
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
                        onUpdate(point.copy(measureHeight = h * 100.0))
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
