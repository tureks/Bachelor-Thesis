package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.viewmodel.VelocityReading
import java.util.Locale

@Composable
fun VelocityGraph(
    readings: List<VelocityReading>,
    windowSeconds: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceColor)

    Canvas(modifier = modifier) {
        val labelPadding = 48.dp.toPx()
        val bottomPadding = 24.dp.toPx()

        val width = size.width - labelPadding
        val height = size.height - bottomPadding
        val now = System.currentTimeMillis()
        val windowMillis = windowSeconds * 1000L
        val startTime = now - windowMillis

        drawRect(
            color = outlineColor.copy(alpha = 0.05f),
            topLeft = Offset(labelPadding, 0f),
            size = Size(width, height)
        )

        drawLine(color = outlineColor, start = Offset(labelPadding, 0f), end = Offset(labelPadding, height), strokeWidth = 1f)
        drawLine(color = outlineColor, start = Offset(labelPadding, height), end = Offset(size.width, height), strokeWidth = 1f)

        val maxVelocity = (readings.maxOfOrNull { it.velocity } ?: 1.0).coerceAtLeast(0.5)

        val maxLayout = textMeasurer.measure(String.format(Locale.US, "%.1f", maxVelocity), style = textStyle)
        drawText(maxLayout, topLeft = Offset(labelPadding - maxLayout.size.width - 8f, 0f))

        val unitLayout = textMeasurer.measure("m/s", style = textStyle)
        drawText(unitLayout, topLeft = Offset(labelPadding - unitLayout.size.width - 8f, height / 2 - unitLayout.size.height / 2))

        val zeroLayout = textMeasurer.measure("0.0", style = textStyle)
        drawText(zeroLayout, topLeft = Offset(labelPadding - zeroLayout.size.width - 8f, height - zeroLayout.size.height))

        val startLayout = textMeasurer.measure("-${windowSeconds}s", style = textStyle)
        drawText(startLayout, topLeft = Offset(labelPadding, height + 4.dp.toPx()))

        val nowLayout = textMeasurer.measure("now", style = textStyle)
        drawText(nowLayout, topLeft = Offset(size.width - nowLayout.size.width, height + 4.dp.toPx()))

        if (readings.size < 2) return@Canvas

        clipRect(left = labelPadding, top = 0f, right = size.width, bottom = height) {
            val path = Path()
            val points = readings.map { reading ->
                Offset(
                    x = labelPadding + ((reading.timestamp - startTime).toFloat() / windowMillis) * width,
                    y = height - (reading.velocity.toFloat() / maxVelocity.toFloat() * height)
                )
            }

            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val controlPointX = (p0.x + p1.x) / 2
                path.quadraticTo(p0.x, p0.y, controlPointX, (p0.y + p1.y) / 2)
            }
            path.lineTo(points.last().x, points.last().y)

            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}