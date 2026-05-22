package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun CancelMeasurementDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cancel Measurement") },
        text = { Text("Are you sure you want to cancel this measurement? All captured data will be lost.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) { Text("Discard") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Keep Going") }
        }
    )
}