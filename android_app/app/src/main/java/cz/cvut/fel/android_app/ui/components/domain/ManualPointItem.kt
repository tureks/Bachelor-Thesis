package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.viewmodel.ManualVelocityPoint
import java.util.Locale

@Composable
fun ManualPointItem(
    point: ManualVelocityPoint,
    unit: MeasurementUnit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = String.format(Locale.US, "%.3f m/s", point.velocity),
                    style = MaterialTheme.typography.bodyLarge
                )
                val depthLabel = if (unit == MeasurementUnit.HYDROMETRIC) {
                    String.format(Locale.US, "Depth: %.1f cm", point.height * 100)
                } else {
                    String.format(Locale.US, "Depth: %.2f m", point.height)
                }
                Text(text = depthLabel, style = MaterialTheme.typography.bodySmall)
            }
            Text("Edit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}