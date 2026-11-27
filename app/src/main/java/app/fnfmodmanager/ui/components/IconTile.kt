package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.ui.theme.FnfColors

/** Dark rounded-square icon button (the tiles in the top-right of the reference sheet). */
@Composable
fun IconTile(
    icon: IconPair,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    red: Boolean = true,
    badge: Boolean = false,
    size: Dp = 48.dp,
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier
            .size(size)
            .fnfClickable(onClick = onClick)
            .clip(shape)
            .background(FnfColors.TileDark)
            .border(1.dp, FnfColors.GlassStroke.copy(alpha = 0.55f), shape)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        FnfIcon(icon, size = 24.dp, red = red)
        if (badge) {
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-9).dp, y = 9.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(FnfColors.Accent),
            )
        }
    }
}
