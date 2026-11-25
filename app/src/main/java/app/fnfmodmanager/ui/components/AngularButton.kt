package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

private fun angularPath(size: Size, cut: Float) = Path().apply {
    moveTo(cut, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - cut)
    lineTo(size.width - cut, size.height)
    lineTo(0f, size.height)
    lineTo(0f, cut)
    close()
}

private class AngularShape(private val cut: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Generic(angularPath(size, cut))
}

/** The bevelled-corner frame at the bottom of the reference sheet, used for dialog actions. */
@Composable
fun AngularButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
) {
    val cut = with(androidx.compose.ui.platform.LocalDensity.current) { 12.dp.toPx() }
    Box(
        modifier
            .height(48.dp)
            .fnfClickable(enabled = enabled, onClick = onClick)
            .clip(AngularShape(cut))
            .drawBehind {
                val p = angularPath(size, cut)
                if (primary) {
                    drawPath(p, Brush.linearGradient(listOf(FnfColors.AccentHigh, FnfColors.AccentLow)))
                    drawPath(p, FnfColors.AccentEdge, style = Stroke(1.5.dp.toPx()))
                } else {
                    drawPath(p, Brush.verticalGradient(listOf(Color(0xFF26232C), Color(0xFF17151D))))
                    drawPath(p, FnfColors.Accent.copy(alpha = 0.75f), style = Stroke(1.5.dp.toPx()))
                }
            }
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge,
            color = if (primary) Color.White else FnfColors.Text,
            maxLines = 1,
        )
    }
}
