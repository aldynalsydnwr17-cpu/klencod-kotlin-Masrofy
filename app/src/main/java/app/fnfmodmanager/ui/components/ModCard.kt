package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.data.ModInfo
import app.fnfmodmanager.ui.theme.FnfColors

data class ModActions(
    val onOpen: (ModInfo) -> Unit,
    val onFavorite: (ModInfo) -> Unit,
    val onPreview: (ModInfo) -> Unit,
)

@Composable
fun ModCard(mod: ModInfo, actions: ModActions, modifier: Modifier = Modifier) {
    GlassCard(modifier.fillMaxWidth(), corner = 20.dp, onClick = { actions.onOpen(mod) }, contentPadding = PaddingValues(10.dp)) {
        Box {
            ModCover(mod.cover, Modifier.fillMaxWidth().aspectRatio(1f), corner = 14.dp)
            Box(
                Modifier.align(Alignment.TopStart).padding(6.dp).size(34.dp).fnfClickable { actions.onFavorite(mod) }
                    .clip(CircleShape).background(FnfColors.Background.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) { FnfIcon(if (mod.favorite) Ic.StarFilled else Ic.Star, size = 20.dp, red = true) }
            Box(
                Modifier.align(Alignment.BottomStart).padding(6.dp).size(34.dp).fnfClickable { actions.onPreview(mod) }
                    .clip(CircleShape).background(FnfColors.Accent),
                contentAlignment = Alignment.Center,
            ) { FnfIcon(Ic.Play, size = 18.dp) }
        }
        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(mod.title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (mod.author.isNotBlank()) Text(mod.author, style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row { StatusChip(mod.status, compact = true) }
        }
    }
}
