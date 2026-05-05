package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.ui.components.base.AppTextField
import cz.cvut.fel.android_app.ui.utils.UnitConverter
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.base.SegmentNumberBadge
import cz.cvut.fel.android_app.ui.components.domain.*
import cz.cvut.fel.android_app.viewmodel.ManualVelocityPoint
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel
import java.util.Locale

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
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedPointIds by remember { mutableStateOf(uiState.manualPoints.map { it.id }.toSet()) }

    LaunchedEffect(uiState.manualPoints) {
        selectedPointIds = uiState.manualPoints.map { it.id }.toSet()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    var editingPoint by remember { mutableStateOf<ManualVelocityPoint?>(null) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val width = uiState.currentWidth
    val depth = uiState.currentDepth

    val isValid = width.isNotEmpty() && depth.isNotEmpty() && (selectedPointIds.isNotEmpty() || uiState.editingSegment != null)
    val segmentNumber = uiState.editingSegment?.segmentNumber ?: (uiState.segments.size + 1)

    val unit = uiState.preferredUnit
    val calculatedFlow = remember(width, depth, selectedPointIds, uiState.manualPoints, unit) {
        val w = UnitConverter.displayToMeters(width.toDoubleOrNull() ?: 0.0, unit)
        val d = UnitConverter.displayToMeters(depth.toDoubleOrNull() ?: 0.0, unit)
        val selectedPoints = uiState.manualPoints.filter { selectedPointIds.contains(it.id) }
        val avgV = if (selectedPoints.isEmpty()) {
            uiState.editingSegment?.averageVelocity ?: 0.0
        } else {
            selectedPoints.map { it.velocity }.average()
        }
        UnitConverter.m3sToDisplay(avgV * w * d, unit)
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                title = "Segment",
                titleContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Segment")
                        Spacer(modifier = Modifier.width(8.dp))
                        SegmentNumberBadge(segmentNumber)
                    }
                },
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(onClick = { showCancelDialog = true }) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(Locale.US, "%.2f %s", calculatedFlow, UnitConverter.flowLabel(unit)),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Estimated Segment Flow",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Segment Dimensions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppTextField(
                    value = width,
                    onValueChange = { viewModel.setCurrentWidth(it) },
                    label = "Width (${UnitConverter.lengthLabel(unit)})",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = DecimalKeyboard
                )
                AppTextField(
                    value = depth,
                    onValueChange = { viewModel.setCurrentDepth(it) },
                    label = "Depth (${UnitConverter.lengthLabel(unit)})",
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
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
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
                        onEdit = { editingPoint = point }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        viewModel.completeSegment(
                            width.toDoubleOrNull() ?: 0.0,
                            depth.toDoubleOrNull() ?: 0.0,
                            selectedPointIds
                        )
                        onNavigateToFinalize()
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Complete\nMeasurement",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }

                Button(
                    onClick = {
                        if (isValid) {
                            viewModel.completeSegment(
                                width.toDoubleOrNull() ?: 0.0,
                                depth.toDoubleOrNull() ?: 0.0,
                                selectedPointIds
                            )
                            onNavigateToMeasurement()
                        }
                    },
                    enabled = isValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Next\nSegment",
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
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
                showCancelDialog = false
                onNavigateToMain()
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
