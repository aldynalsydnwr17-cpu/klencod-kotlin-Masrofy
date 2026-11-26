package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

/** glass-card-background.svg rebuilt as a real container: dark fill, soft stroke, short red accent line on top. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(corner)
    val base = if (onClick != null) modifier.fnfClickable(onClick = onClick) else modifier
    Column(
        base
            .clip(shape)
            .drawBehind {
                val c = corner.toPx()
                val w = 1.5.dp.toPx()
                drawRoundRect(FnfColors.Surface.copy(alpha = 0.85f), cornerRadius = CornerRadius(c))
                drawRoundRect(
                    FnfColors.GlassStroke.copy(alpha = 0.65f),
                    topLeft = Offset(w / 2, w / 2),
                    size = Size(size.width - w, size.height - w),
                    cornerRadius = CornerRadius(c - w / 2),
                    style = Stroke(w),
                )
                drawLine(
                    FnfColors.Accent.copy(alpha = 0.9f),
                    Offset(16.dp.toPx(), w / 2),
                    Offset(80.dp.toPx(), w / 2),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            .padding(contentPadding),
        content = content,
    )
}
