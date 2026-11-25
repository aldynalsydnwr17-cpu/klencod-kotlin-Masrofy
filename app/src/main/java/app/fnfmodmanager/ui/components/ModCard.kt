package app.fnfmodmanager.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.ModInfo
import app.fnfmodmanager.data.ModStatus
import app.fnfmodmanager.ui.theme.FnfColors

/** Everything a mod card (and its More menu) can do. Each entry is wired to a real action in the ViewModel. */
data class ModActions(
    val onOpen: (ModInfo) -> Unit,
    val onFavorite: (ModInfo) -> Unit,
    val onPreview: (ModInfo) -> Unit,
    val onLaunch: () -> Unit,
    val onChangeCover: (ModInfo) -> Unit,
    val onResetCover: (ModInfo) -> Unit,
    val onRepatch: (ModInfo) -> Unit,
    val onVerify: (ModInfo) -> Unit,
    val onDelete: (ModInfo) -> Unit,
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
            MoreMenu(mod, actions, Modifier.align(Alignment.TopEnd).padding(6.dp))
        }
        Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(mod.title, style = MaterialTheme.typography.titleSmall, color = FnfColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(
                    mod.modVersion.takeIf { it.isNotBlank() }?.let { "v$it" },
                    mod.apiVersion.takeIf { it.isNotBlank() }?.let { "FNF v$it" },
                ).joinToString(" • ").ifBlank { " " },
                style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
            Row { StatusChip(mod.status, compact = true) }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                PrimaryButton(stringResource(R.string.card_play), actions.onLaunch, Modifier.weight(1f), icon = Ic.Play, height = 40.dp)
                IconTile(Ic.Info, { actions.onOpen(mod) }, stringResource(R.string.card_info), size = 40.dp)
            }
        }
    }
}

@Composable
private fun MoreMenu(mod: ModInfo, actions: ModActions, modifier: Modifier = Modifier) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        Box(
            Modifier.size(34.dp).fnfClickable { open = true }.clip(CircleShape).background(FnfColors.Background.copy(alpha = 0.65f)),
            contentAlignment = Alignment.Center,
        ) { FnfIcon(Ic.More, size = 20.dp, red = true, contentDescription = stringResource(R.string.card_more)) }
        DropdownMenu(open, { open = false }, containerColor = FnfColors.Card) {
            @Composable
            fun item(label: Int, icon: IconPair, onClick: () -> Unit) {
                DropdownMenuItem(
                    text = { Text(stringResource(label), style = MaterialTheme.typography.bodyMedium, color = FnfColors.Text) },
                    leadingIcon = { FnfIcon(icon, size = 20.dp, red = true) },
                    onClick = { open = false; onClick() },
                )
            }
            item(R.string.more_details, Ic.Info) { actions.onOpen(mod) }
            item(R.string.more_preview, Ic.Music) { actions.onPreview(mod) }
            item(if (mod.favorite) R.string.detail_unfavorite else R.string.detail_favorite, if (mod.favorite) Ic.StarFilled else Ic.Star) { actions.onFavorite(mod) }
            item(R.string.more_change_cover, Ic.FolderPlus) { actions.onChangeCover(mod) }
            if (mod.customCover) item(R.string.more_reset_cover, Ic.Refresh) { actions.onResetCover(mod) }
            if (mod.status == ModStatus.INCOMPATIBLE) item(R.string.more_repatch, Ic.Refresh) { actions.onRepatch(mod) }
            item(R.string.more_verify, Ic.Check) { actions.onVerify(mod) }
            item(R.string.more_uninstall, Ic.Trash) { actions.onDelete(mod) }
        }
    }
}
