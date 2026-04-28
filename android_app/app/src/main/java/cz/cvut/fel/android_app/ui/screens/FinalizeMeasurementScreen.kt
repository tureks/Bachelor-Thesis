package cz.cvut.fel.android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.ui.components.base.AppButton
import cz.cvut.fel.android_app.ui.components.base.AppTextField
import cz.cvut.fel.android_app.ui.components.base.AppTopBar
import cz.cvut.fel.android_app.viewmodel.StreamMeasurementViewModel

@Composable
fun FinalizeMeasurementScreen(
    viewModel: StreamMeasurementViewModel,
    onNavigateBack: () -> Unit,
    onComplete: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Finalize Measurement",
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Location Name",
                placeholder = "e.g. Vltava - Prague"
            )

            AppTextField(
                value = note,
                onValueChange = { note = it },
                label = "Notes",
                placeholder = "Optional notes about the measurement"
            )

            Spacer(modifier = Modifier.weight(1f))

            AppButton(
                text = "Save and Complete",
                onClick = {
                    viewModel.finalizeMeasurement(name, note)
                    onComplete()
                }
            )
        }
    }
}
