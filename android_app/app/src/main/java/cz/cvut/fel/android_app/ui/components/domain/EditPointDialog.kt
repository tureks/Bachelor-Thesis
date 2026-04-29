package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.viewmodel.ManualVelocityPoint
import java.util.Locale

@Composable
fun EditPointDialog(
    point: ManualVelocityPoint,
    unit: MeasurementUnit,
    onDismiss: () -> Unit,
    onUpdateHeight: (Double) -> Unit,
    onDelete: () -> Unit
) {
    val isHydrometric = unit == MeasurementUnit.HYDROMETRIC
    val initialDepth = if (isHydrometric) point.height * 100.0 else point.height
    var heightInput by remember {
        mutableStateOf(String.format(Locale.US, "%.1f", initialDepth))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Velocity Point") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Velocity: ${String.format(Locale.US, "%.3f", point.velocity)} m/s")
                OutlinedTextField(
                    value = heightInput,
                    onValueChange = { heightInput = it },
                    label = { Text(if (isHydrometric) "Depth (cm)" else "Depth (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                heightInput.toDoubleOrNull()?.let {
                    val finalHeight = if (isHydrometric) it / 100.0 else it
                    onUpdateHeight(finalHeight)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Delete")
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}