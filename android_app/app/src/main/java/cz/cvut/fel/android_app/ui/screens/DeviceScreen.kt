package cz.cvut.fel.android_app.ui.screens

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import cz.cvut.fel.android_app.domain.model.BleConnectionState
import cz.cvut.fel.android_app.domain.model.Device
import cz.cvut.fel.android_app.ui.components.base.AppNotificationHost
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.ui.components.base.showError
import cz.cvut.fel.android_app.ui.components.domain.DeviceConnectionStatus
import cz.cvut.fel.android_app.ui.components.domain.DeviceItem
import cz.cvut.fel.android_app.viewmodel.DeviceViewModel

private fun requiredBlePermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

@Composable
fun DeviceScreen(
    viewModel: DeviceViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showError(it)
            viewModel.clearError()
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopScan() }
    }

    val permissions = remember { requiredBlePermissions() }
    var hasPermissions by remember {
        mutableStateOf(
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasPermissions = results.values.all { it }
        if (hasPermissions) viewModel.refreshBluetoothState()
    }

    val btEnableLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshBluetoothState() }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        topBar = {
            AppTopBar(title = "Connect Device", onNavigateBack = onNavigateBack)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            when {
                !hasPermissions -> item {
                    PermissionCard(
                        onGrant = { permissionLauncher.launch(permissions) }
                    )
                }

                !uiState.isBluetoothEnabled -> item {
                    BluetoothDisabledCard(
                        onEnable = {
                            btEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                        }
                    )
                }

                else -> {
                    val connectedAddress = (uiState.connectionState as? BleConnectionState.Connected)?.deviceAddress
                    if (connectedAddress != null) {
                        item {
                            ConnectedBanner(
                                address = connectedAddress,
                                deviceName = uiState.knownDevices.find { it.macAddress == connectedAddress }?.name
                                    ?: uiState.scannedDevices.find { it.macAddress == connectedAddress }?.name
                            )
                        }
                    }
                    
                    val scannedAddresses = uiState.scannedDevices.map { it.macAddress }.toSet()
                    val knownOnly = uiState.knownDevices
                        .filter { it.macAddress !in scannedAddresses }
                        .sortedByDescending { it.lastConnected }
                    if (knownOnly.isNotEmpty()) {
                        item {
                            Text(
                                "Previously Connected",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        items(knownOnly, key = { "known_${it.macAddress}" }) { device ->
                            DeviceItem(
                                name = device.name,
                                address = device.macAddress,
                                connectionStatus = connectionStatus(device, uiState),
                                onClick = { viewModel.toggleConnection(device) }
                            )
                        }
                        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Scan for Devices", style = MaterialTheme.typography.titleMedium)
                            if (uiState.isScanning) {
                                OutlinedButton(onClick = viewModel::stopScan) { Text("Stop") }
                            } else {
                                Button(onClick = viewModel::startScan) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Scan")
                                }
                            }
                        }
                    }

                    if (uiState.isScanning && uiState.scannedDevices.isEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    "Searching for devices...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (!uiState.isScanning && uiState.hasScanned && uiState.scannedDevices.isEmpty()) {
                        item {
                            Text(
                                "No devices found. Make sure the flow meter is powered on and within range.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    items(uiState.scannedDevices, key = { it.macAddress }) { device ->
                        DeviceItem(
                            name = device.name,
                            address = device.macAddress,
                            connectionStatus = connectionStatus(device, uiState),
                            onClick = { viewModel.toggleConnection(device) }
                        )
                    }
                }
            }

        }
    }

    AppNotificationHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.TopCenter)
    )
    } 
}

@Composable
private fun PermissionCard(onGrant: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Bluetooth permission required",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "This app needs Bluetooth access to scan for and connect to the flow meter.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Button(
                onClick = onGrant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Grant Permission")
            }
        }
    }
}

@Composable
private fun BluetoothDisabledCard(onEnable: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    "Bluetooth is disabled",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                "Enable Bluetooth to scan for and connect to the flow meter.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Button(
                onClick = onEnable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enable Bluetooth")
            }
        }
    }
}

@Composable
private fun ConnectedBanner(address: String, deviceName: String?) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Connected",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    deviceName ?: address,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (deviceName != null) {
                    Text(
                        address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun connectionStatus(device: Device, uiState: cz.cvut.fel.android_app.viewmodel.DeviceUiState): DeviceConnectionStatus =
    when {
        uiState.connectionState is BleConnectionState.Connected &&
                uiState.connectionState.deviceAddress == device.macAddress -> DeviceConnectionStatus.Connected
        uiState.connectingAddress == device.macAddress -> DeviceConnectionStatus.Connecting
        else -> DeviceConnectionStatus.Idle
    }