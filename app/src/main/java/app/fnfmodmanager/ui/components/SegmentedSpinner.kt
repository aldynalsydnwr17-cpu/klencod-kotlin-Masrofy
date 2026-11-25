package app.fnfmodmanager.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

/** Ring made of 12 separate arcs; the red ones sweep round while installing. */
@Composable
fun SegmentedSpinner(modifier: Modifier = Modifier, size: Dp = 96.dp, progress: Float? = null) {
    val t = rememberInfiniteTransition(label = "spin")
    val angle by t.animateFloat(0f, 360f, infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart), label = "a")
    Canvas(modifier.size(size)) {
        val stroke = 7.dp.toPx()
        val d = this.size.minDimension - stroke
        val segs = 12
        val sweep = 360f / segs - 8f
        val lit = if (progress != null) (progress.coerceIn(0f, 1f) * segs).toInt() else 4
        for (i in 0 until segs) {
            val start = -90f + i * (360f / segs)
            val on = if (progress != null) i < lit else true
            val color = if (on) FnfColors.Accent else FnfColors.Card
            val alpha = if (progress != null) 1f else 0.25f + 0.75f * (((i * 30f - angle + 720f) % 360f) / 360f)
            rotate(0f) {
                drawArc(
                    color = color.copy(alpha = if (on) alpha else 1f),
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(stroke / 2, stroke / 2),
                    size = Size(d, d),
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
    }
}
