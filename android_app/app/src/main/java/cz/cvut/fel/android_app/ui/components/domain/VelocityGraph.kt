package cz.cvut.fel.android_app.ui.components.domain

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import cz.cvut.fel.android_app.viewmodel.VelocityReading
import java.util.Locale

@Composable
fun VelocityGraph(
    readings: List<VelocityReading>,
    windowSeconds: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(color = onSurfaceColor)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures { onTap() }
        }
    ) {
        val leftPadding = 32.dp.toPx()
        val rightPadding = 8.dp.toPx()
        val topPadding = 8.dp.toPx()
        val bottomPadding = 20.dp.toPx()
        
        val graphWidth = size.width - leftPadding - rightPadding
        val graphHeight = size.height - bottomPadding - topPadding

        val now = System.currentTimeMillis()
        val windowMillis = windowSeconds * 1000L

        val startTime = now - windowMillis

        drawRect(
            color = outlineColor.copy(alpha = 0.05f),
            topLeft = Offset(leftPadding, topPadding),
            size = Size(graphWidth, graphHeight)
        )
        drawLine(color = outlineColor, start = Offset(leftPadding, topPadding), end = Offset(leftPadding, graphHeight + topPadding), strokeWidth = 0.5.dp.toPx())
        drawLine(color = outlineColor, start = Offset(leftPadding, graphHeight + topPadding), end = Offset(size.width - rightPadding, graphHeight + topPadding), strokeWidth = 0.5.dp.toPx())

        val visibleReadings = readings.filter { it.timestamp >= startTime }
        val maxV = (visibleReadings.maxOfOrNull { it.velocity } ?: 1.0).coerceAtLeast(0.1)
        val minV = (visibleReadings.minOfOrNull { it.velocity } ?: 0.0).coerceAtLeast(0.0)
        val yRange = (maxV - minV).coerceAtLeast(0.3)
        val yPadding = yRange * 0.2
        val yMax = maxV + yPadding
        val yMin = (minV - yPadding).coerceAtLeast(0.0)

        val topLabel = textMeasurer.measure(String.format(Locale.US, "%.1f", yMax), style = textStyle)
        drawText(topLabel, topLeft = Offset(leftPadding - topLabel.size.width - 6f, topPadding))

        val unitLayout = textMeasurer.measure("m/s", style = textStyle)
        drawText(unitLayout, topLeft = Offset(leftPadding - unitLayout.size.width - 6f, topPadding + graphHeight / 2 - unitLayout.size.height / 2))

        val bottomLabel = textMeasurer.measure(String.format(Locale.US, "%.1f", yMin), style = textStyle)
        drawText(bottomLabel, topLeft = Offset(leftPadding - bottomLabel.size.width - 6f, topPadding + graphHeight - bottomLabel.size.height))

        val startLabel = textMeasurer.measure("0s", style = textStyle)
        drawText(startLabel, topLeft = Offset(leftPadding, topPadding + graphHeight + 4.dp.toPx()))

        val endLabel = textMeasurer.measure("${windowSeconds}s", style = textStyle)
        drawText(endLabel, topLeft = Offset(size.width - rightPadding - endLabel.size.width, topPadding + graphHeight + 4.dp.toPx()))

        if (visibleReadings.size < 2) return@Canvas

        clipRect(left = leftPadding, top = topPadding, right = size.width - rightPadding, bottom = graphHeight + topPadding) {
            val path = Path()
            val points = visibleReadings.map { reading ->
                Offset(
                    x = leftPadding + (reading.timestamp - startTime).toFloat() / windowMillis.toFloat() * graphWidth,
                    y = topPadding + graphHeight - ((reading.velocity.toFloat() - yMin.toFloat()) / (yMax - yMin).toFloat() * graphHeight)
                )
            }
            path.moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val p0 = points[i - 1]
                val p1 = points[i]
                val controlX = (p0.x + p1.x) / 2
                path.quadraticTo(p0.x, p0.y, controlX, (p0.y + p1.y) / 2)
            }
            path.lineTo(points.last().x, points.last().y)
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}