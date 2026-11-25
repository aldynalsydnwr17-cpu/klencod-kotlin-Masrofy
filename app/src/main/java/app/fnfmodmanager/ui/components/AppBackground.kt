package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import app.fnfmodmanager.ui.theme.FnfColors

/** #101014 with the soft red glows of the reference design. Drawn, not an image. */
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .fillMaxSize()
            .background(FnfColors.Background)
            .drawBehind {
                drawRect(
                    Brush.radialGradient(
                        listOf(FnfColors.Accent.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.1f, 0f),
                        radius = size.width * 1.1f,
                    ),
                )
                drawRect(
                    Brush.radialGradient(
                        listOf(FnfColors.Accent.copy(alpha = 0.10f), Color.Transparent),
                        center = Offset(size.width * 0.95f, size.height),
                        radius = size.width,
                    ),
                )
            },
        content = content,
    )
}
