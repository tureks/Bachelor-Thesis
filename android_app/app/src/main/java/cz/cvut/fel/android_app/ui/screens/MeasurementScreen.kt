package cz.cvut.fel.android_app.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.ui.components.base.AppNotificationHost
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.base.showError
import cz.cvut.fel.android_app.ui.components.domain.*
import cz.cvut.fel.android_app.viewmodel.BleViewModel
import cz.cvut.fel.android_app.viewmodel.CaptureViewModel
import cz.cvut.fel.android_app.viewmodel.ManualVelocityPoint
import cz.cvut.fel.android_app.viewmodel.MeasurementViewModel
import cz.cvut.fel.android_app.viewmodel.UserViewModel
import java.util.Locale

@Composable
fun MeasurementScreen(
    bleViewModel: BleViewModel,
    captureViewModel: CaptureViewModel,
    measurementViewModel: MeasurementViewModel,
    userViewModel: UserViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCompleteSegment: () -> Unit
) {
    val bleState by bleViewModel.uiState.collectAsState()
    val captureState by captureViewModel.uiState.collectAsState()
    val measureState by measurementViewModel.uiState.collectAsState()
    val userState by userViewModel.uiState.collectAsState()
    val devMode = userState.profile?.developerMode ?: false

    val isConnected = devMode || bleState.connectionState is BleConnectionState.Connected
    var selectedPoint by remember { mutableStateOf<ManualVelocityPoint?>(null) }
    var showTimeWindowDialog by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }

    BackHandler { onNavigateBack() }

    // When resuming an editing session, pre-populate capture state from the loaded points.
    LaunchedEffect(measureState.editingSegment, measureState.editingPoints) {
        val segment = measureState.editingSegment
        if (segment != null && measureState.editingPoints.isNotEmpty()) {
            captureViewModel.setForEditing(
                measureState.editingPoints,
                segment.segmentWidth,
                segment.depth,
                measureState.preferredUnit
            )
        }
    }

    LaunchedEffect(measureState.error) {
        measureState.error?.let {
            snackbarHostState.showError(it)
            measurementViewModel.clearError()
        }
    }

    val segmentNumber = measureState.editingSegment?.segmentNumber ?: (measureState.segments.size + 1)

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { focusManager.clearFocus() })
        },
        topBar = {
            AppTopBar(
                title = "Segment Capture",
                titleContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Segment Capture")
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
            if (!isConnected) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.BluetoothDisabled, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(
                            text = "Device not connected — connect a device to capture velocity",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isConnected) Modifier.clickable { captureViewModel.addManualPoint(bleState.windowAverage) } else Modifier),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format(Locale.US, "%.2f m/s", bleState.windowAverage),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Live Average",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (bleState.velocityOverLimit) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Above 5.00 m/s — outside measurement range",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.2f m/s", bleState.windowMin),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Min", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = String.format(Locale.US, "%.2f m/s", bleState.windowMax),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("Max", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                border = CardDefaults.outlinedCardBorder(enabled = true)
            ) {
                VelocityGraph(
                    readings = bleState.recentReadings,
                    windowSeconds = bleState.timeWindow,
                    onTap = { if (isConnected) captureViewModel.addManualPoint(bleState.windowAverage) },
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

            if (captureState.manualPoints.isEmpty()) {
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
                    contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
                ) {
                    items(captureState.manualPoints, key = { it.id }) { point ->
                        ManualPointItem(
                            point = point,
                            unit = measureState.preferredUnit,
                            onEdit = { selectedPoint = point }
                        )
                    }
                }
            }

            OutlinedIconButton(
                onClick = { showTimeWindowDialog = true },
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.Start)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Time Window Settings")
            }
        }
    }

    AppNotificationHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.TopCenter)
    )
    } 

    if (showCancelDialog) {
        CancelMeasurementDialog(
            onDismiss = { showCancelDialog = false },
            onConfirm = {
                measurementViewModel.cancelMeasurement()
                captureViewModel.reset()
                onNavigateBack()
            }
        )
    }

    if (showTimeWindowDialog) {
        TimeWindowDialog(
            currentWindow = bleState.timeWindow,
            onDismiss = { showTimeWindowDialog = false },
            onConfirm = {
                bleViewModel.setTimeWindow(it)
                showTimeWindowDialog = false
            }
        )
    }

    selectedPoint?.let { point ->
        EditPointDialog(
            point = point,
            unit = measureState.preferredUnit,
            onDismiss = { selectedPoint = null },
            onUpdateHeight = { height ->
                captureViewModel.updateManualPointHeight(point.id, height)
                selectedPoint = null
            },
            onDelete = {
                captureViewModel.deleteManualPoint(point.id)
                selectedPoint = null
            }
        )
    }
}