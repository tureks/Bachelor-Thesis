package cz.cvut.fel.android_app.ui.components.base

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val TreeColor = Color(0xFF1E2D40)
private val TreeDeepColor = Color(0xFF111827)
private val WaterColor = Color(0xFF38BDF8)
private val WaterDeepColor = Color(0xFF0284C7)

@Composable
fun AppLogo(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(40.dp)
            .aspectRatio(1f)
    ) {
        val scale = size.width / 200f

        drawWaterLine(this, 145f + 25f - 8f, 4f, 5.5f, WaterColor.copy(alpha = 0.55f), scale)
        drawWaterLine(this, 145f + 25f, 5f, 10f, WaterColor, scale)
        drawWaterLine(this, 145f + 25f + 8f, 4f, 5.5f, WaterDeepColor.copy(alpha = 0.65f), scale)

        val groundY = 145f
        val centerTop = groundY - 110f
        val sideTop = groundY - 82f
        val backTop = groundY - 65f

        drawPine(this, 75f, backTop, groundY, 40f, 3, TreeColor.copy(alpha = 0.45f), scale)
        drawPine(this, 125f, backTop, groundY, 40f, 3, TreeColor.copy(alpha = 0.45f), scale)
        drawPine(this, 50f, sideTop, groundY, 48f, 3, TreeColor, scale)
        drawPine(this, 100f, centerTop, groundY, 56f, 3, TreeDeepColor, scale)
        drawPine(this, 150f, sideTop, groundY, 48f, 3, TreeColor, scale)
    }
}

private fun drawPine(
    drawScope: DrawScope,
    cx: Float,
    top: Float,
    bottom: Float,
    w: Float,
    tiers: Int,
    color: Color,
    scale: Float
) {
    val h = bottom - top
    val tierH = h / tiers
    val overlap = 0.30f
    val ov = tierH * overlap
    val taper = 0.5f

    val path = Path()

    for (i in 0 until tiers) {
        val apexY = top + tierH * i
        val baseY = top + tierH * (i + 1) + ov
        val halfW = (w / 2f) * (taper + (1f - taper) * (i / Math.max(1, tiers - 1).toFloat()))
        
        if (i == 0) {
            path.moveTo(cx * scale, apexY * scale)
        } else {
            path.lineTo((cx + halfW * 0.18f) * scale, (apexY + tierH * 0.05f) * scale)
        }
        path.lineTo((cx + halfW) * scale, baseY * scale)
    }

    for (i in (tiers - 1) downTo 0) {
        val apexY = top + tierH * i
        val baseY = top + tierH * (i + 1) + ov
        val halfW = (w / 2f) * (taper + (1f - taper) * (i / Math.max(1, tiers - 1).toFloat()))
        
        path.lineTo((cx - halfW) * scale, baseY * scale)
        if (i > 0) {
            path.lineTo((cx - halfW * 0.18f) * scale, (apexY + tierH * 0.05f) * scale)
        }
    }
    path.close()

    drawScope.drawPath(
        path = path,
        color = color,
        style = Fill
    )
    drawScope.drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.4f * scale,
            join = StrokeJoin.Round,
            cap = StrokeCap.Round
        )
    )
}

private fun drawWaterLine(
    drawScope: DrawScope,
    y: Float,
    amp: Float,
    strokeWidth: Float,
    color: Color,
    scale: Float
) {
    val path = Path()
    path.moveTo(-20f * scale, y * scale)
    path.cubicTo(
        20f * scale, (y - amp) * scale,
        50f * scale, (y - amp) * scale,
        70f * scale, y * scale
    )
    path.cubicTo(
        90f * scale, (y + amp) * scale,
        110f * scale, (y + amp) * scale,
        130f * scale, y * scale
    )
    path.cubicTo(
        160f * scale, (y - amp * 0.6f) * scale,
        180f * scale, (y - amp * 0.6f) * scale,
        220f * scale, y * scale
    )

    drawScope.drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth * scale,
            cap = StrokeCap.Round
        )
    )
}
