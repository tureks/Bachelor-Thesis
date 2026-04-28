package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.cvut.fel.android_app.ui.components.base.AppButton
import cz.cvut.fel.android_app.ui.components.base.AppTextField
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.ManualVelocityPoint
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import cz.cvut.fel.android_app.viewmodel.VelocityReading
import java.util.*

@Composable
fun MeasurementScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFinalize: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedPoint by remember { mutableStateOf<ManualVelocityPoint?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Segment Capture",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = onNavigateToFinalize) {
                        Icon(Icons.Default.Check, contentDescription = "Finalize")
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Center Average Velocity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable { viewModel.addManualPoint() }
                    .padding(8.dp)
            ) {
                Text(
                    text = String.format(Locale.US, "%.3f m/s", uiState.windowAverage),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Average (click to save point)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Window Config
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Window:")
                OutlinedTextField(
                    value = uiState.timeWindow.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { seconds -> viewModel.setTimeWindow(seconds) }
                    },
                    modifier = Modifier.width(100.dp),
                    suffix = { Text("s") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Velocity Graph
            VelocityGraph(
                readings = uiState.recentReadings,
                windowSeconds = uiState.timeWindow,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Velocity Points",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.manualPoints) { point ->
                    ManualPointItem(
                        point = point,
                        onClick = { selectedPoint = point }
                    )
                }
            }
        }
    }

    selectedPoint?.let { point ->
        EditPointDialog(
            point = point,
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

@Composable
fun VelocityGraph(
    readings: List<VelocityReading>,
    windowSeconds: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val now = System.currentTimeMillis()
        val windowMillis = windowSeconds * 1000L
        val startTime = now - windowMillis

        // Draw background/grid
        drawRect(color = outlineColor.copy(alpha = 0.1f))
        drawLine(
            color = outlineColor,
            start = Offset(0f, height),
            end = Offset(width, height),
            strokeWidth = 2f
        )

        if (readings.size < 2) return@Canvas

        val maxVelocity = (readings.maxOfOrNull { it.velocity } ?: 1.0).coerceAtLeast(0.5)
        
        val path = Path()
        readings.forEachIndexed { index, reading ->
            val x = ((reading.timestamp - startTime).toFloat() / windowMillis) * width
            val y = height - (reading.velocity.toFloat() / maxVelocity.toFloat() * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4f)
        )
    }
}

@Composable
fun ManualPointItem(
    point: ManualVelocityPoint,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = String.format(Locale.US, "%.3f m/s", point.velocity),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Depth: ${point.height} m",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.Default.Edit, contentDescription = "Edit")
        }
    }
}

@Composable
fun EditPointDialog(
    point: ManualVelocityPoint,
    onDismiss: () -> Unit,
    onUpdateHeight: (Double) -> Unit,
    onDelete: () -> Unit
) {
    var heightText by remember { mutableStateOf(point.height.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Velocity Point") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Velocity: ${String.format(Locale.US, "%.3f", point.velocity)} m/s")
                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Point Height/Depth (m)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                heightText.toDoubleOrNull()?.let { onUpdateHeight(it) }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}
