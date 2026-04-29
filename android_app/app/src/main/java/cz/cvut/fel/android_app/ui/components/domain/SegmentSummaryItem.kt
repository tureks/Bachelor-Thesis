package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.domain.model.MeasurementUnit
import cz.cvut.fel.android_app.domain.model.StreamSegment
import java.util.Locale

@Composable
fun SegmentSummaryItem(
    segment: StreamSegment,
    unit: MeasurementUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val isHydrometric = unit == MeasurementUnit.HYDROMETRIC

    val widthLabel = if (isHydrometric) {
        String.format(Locale.US, "%.0f cm", segment.segmentWidth * 100)
    } else {
        String.format(Locale.US, "%.2f m", segment.segmentWidth)
    }

    val depthLabel = if (isHydrometric) {
        String.format(Locale.US, "%.0f cm", segment.depth * 100)
    } else {
        String.format(Locale.US, "%.2f m", segment.depth)
    }

    val flowLabel = if (isHydrometric) {
        String.format(Locale.US, "%.2f l/s", segment.segmentFlow * 1000)
    } else {
        String.format(Locale.US, "%.3f m³/s", segment.segmentFlow)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(0.3.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "#${segment.segmentNumber}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = "W: $widthLabel  D: $depthLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format(Locale.US, "%.2f m/s  →  $flowLabel", segment.averageVelocity),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}