package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

/** primary-button-background.svg rebuilt as a real button: red gradient, light edge, glow and top highlight. */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: IconPair? = null,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    compact: Boolean = false,
) {
    val corner = 12.dp
    val shape = RoundedCornerShape(corner)
    Row(
        modifier
            .height(height)
            .fnfClickable(enabled = enabled, onClick = onClick)
            .shadow(12.dp, shape, ambientColor = FnfColors.Accent.copy(alpha = 0.5f), spotColor = FnfColors.Accent.copy(alpha = 0.5f))
            .clip(shape)
            .drawBehind {
                drawRect(
                    Brush.linearGradient(
                        listOf(FnfColors.Accent, Color(0xFFE0183A)),
                        start = Offset.Zero,
                        end = Offset(0f, size.height),
                    ),
                )
                drawLine(
                    Color.White.copy(alpha = 0.22f),
                    Offset(size.width * 0.05f, size.height * 0.1f),
                    Offset(size.width * 0.34f, size.height * 0.1f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                val w = 1.5.dp.toPx()
                drawRoundRect(
                    FnfColors.AccentEdge,
                    topLeft = Offset(w / 2, w / 2),
                    size = Size(size.width - w, size.height - w),
                    cornerRadius = CornerRadius(corner.toPx() - w / 2),
                    style = Stroke(w),
                )
            }
            .padding(horizontal = if (compact) 6.dp else 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            FnfIcon(icon, size = if (compact) 16.dp else 22.dp)
            Spacer(Modifier.width(if (compact) 4.dp else 8.dp))
        }
        Text(
            text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
