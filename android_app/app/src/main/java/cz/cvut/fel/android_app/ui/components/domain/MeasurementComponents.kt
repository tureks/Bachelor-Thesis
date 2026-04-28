package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cz.cvut.fel.android_app.domain.model.StreamMeasurement

@Composable
fun MeasurementItem(
    measurement: StreamMeasurement,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // TODO: Implement domain-specific measurement item
}
