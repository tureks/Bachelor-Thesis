package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.StreamMeasurement
import cz.cvut.fel.android_app.ui.components.base.AppTextField

@Composable
fun EditMeasurementMetadataDialog(
    measurement: StreamMeasurement,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(measurement.name) }
    var note by remember { mutableStateOf(measurement.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Measurement") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Location Name",
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "Notes",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, note) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
