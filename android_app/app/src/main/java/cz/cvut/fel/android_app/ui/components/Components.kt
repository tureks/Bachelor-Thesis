package cz.cvut.fel.android_app.ui.components

import androidx.compose.runtime.Composable
import cz.cvut.fel.android_app.domain.model.StreamMeasurement

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit
) {
    // TODO: Implement SearchBar
}

@Composable
fun MeasurementItem(
    measurement: StreamMeasurement,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    // TODO: Implement MeasurementItem
}
