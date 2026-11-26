package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.data.PreviewPlayer
import app.fnfmodmanager.ui.theme.FnfColors

/** Slider that always runs left-to-right like a media timeline, whatever the layout direction. */
@Composable
fun FnfSlider(progress: Float, onSeek: (Float) -> Unit, modifier: Modifier = Modifier) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier.height(24.dp).pointerInput(Unit) { detectTapGestures { onSeek((it.x / size.width).coerceIn(0f, 1f)) } }
                .pointerInput(Unit) { detectHorizontalDragGestures { change, _ -> onSeek((change.position.x / size.width).coerceIn(0f, 1f)) } },
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(50)).background(FnfColors.Card))
            Box(Modifier.fillMaxWidth(progress.coerceIn(0f, 1f)).height(5.dp).clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(FnfColors.AccentLow, FnfColors.AccentHigh))))
        }
    }
}

private fun fmt(ms: Int): String {
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

@Composable
fun MiniPlayer(state: PreviewPlayer.State, onToggle: () -> Unit, onSeek: (Float) -> Unit, onMute: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(modifier.fillMaxWidth(), contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(46.dp).fnfClickable(onClick = onToggle).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(FnfColors.AccentHigh, FnfColors.AccentLow))),
                contentAlignment = Alignment.Center,
            ) { FnfIcon(if (state.playing) Ic.Pause else Ic.Play, size = 24.dp) }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(state.title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                FnfSlider(state.progress, onSeek)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Text(fmt(state.position), style = MaterialTheme.typography.labelSmall, color = FnfColors.Muted)
                        Text(fmt(state.duration), style = MaterialTheme.typography.labelSmall, color = FnfColors.Muted)
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.fnfClickable(onClick = onMute)) { FnfIcon(if (state.muted) Ic.VolumeOff else Ic.Volume, size = 24.dp, red = true) }
        }
    }
}
