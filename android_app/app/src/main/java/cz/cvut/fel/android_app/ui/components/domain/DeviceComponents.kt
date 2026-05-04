package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class DeviceConnectionStatus { Idle, Connecting, Connected }

@Composable
fun DeviceItem(
    name: String,
    address: String,
    connectionStatus: DeviceConnectionStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (connectionStatus == DeviceConnectionStatus.Connected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when (connectionStatus) {
                        DeviceConnectionStatus.Connected -> Icons.Default.BluetoothConnected
                        DeviceConnectionStatus.Connecting -> Icons.Default.BluetoothSearching
                        DeviceConnectionStatus.Idle -> Icons.Default.Bluetooth
                    },
                    contentDescription = null,
                    tint = when (connectionStatus) {
                        DeviceConnectionStatus.Connected -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = name.ifBlank { "Unknown Device" },
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            when (connectionStatus) {
                DeviceConnectionStatus.Connected -> OutlinedButton(onClick = onClick) {
                    Text("Disconnect")
                }
                DeviceConnectionStatus.Connecting -> CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                DeviceConnectionStatus.Idle -> Button(onClick = onClick) {
                    Text("Connect")
                }
            }
        }
    }
}