package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.ModInfo
import app.fnfmodmanager.data.ModStatus
import app.fnfmodmanager.ui.components.AngularButton
import app.fnfmodmanager.ui.components.FnfSheet
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.ModCover
import app.fnfmodmanager.ui.components.PrimaryButton
import app.fnfmodmanager.ui.components.SecondaryButton
import app.fnfmodmanager.ui.components.StatusChip
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun ModDetailSheet(
    mod: ModInfo,
    gameVersion: String,
    onClose: () -> Unit,
    onRepatch: () -> Unit,
    onDelete: () -> Unit,
    onChangeCover: () -> Unit,
    onResetCover: () -> Unit,
) {
    FnfSheet(onDismiss = onClose) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ModCover(mod.cover, Modifier.size(150.dp), corner = 22.dp, maxSide = 1024)
            Text(mod.title, style = MaterialTheme.typography.titleMedium, color = FnfColors.Text, textAlign = TextAlign.Center)
            if (mod.author.isNotBlank()) Text(mod.author, style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted)
            StatusChip(mod.status)
            mod.statusDetail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = FnfColors.Warning, textAlign = TextAlign.Center) }
            if (mod.description.isNotBlank()) Text(mod.description, style = MaterialTheme.typography.bodyMedium, color = FnfColors.Text, textAlign = TextAlign.Center)
            Text(
                stringResource(R.string.detail_versions_fmt, mod.modVersion.ifBlank { "—" }, mod.apiVersion.ifBlank { "—" }, gameVersion),
                style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, textAlign = TextAlign.Center,
            )
            Text(mod.folderName, style = MaterialTheme.typography.labelSmall, color = FnfColors.Muted)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(stringResource(R.string.more_change_cover), onChangeCover, Modifier.weight(1f), icon = Ic.FolderPlus, height = 48.dp)
                if (mod.customCover) SecondaryButton(stringResource(R.string.more_reset_cover), onResetCover, Modifier.weight(1f), icon = Ic.Refresh, height = 48.dp)
            }
            if (mod.status == ModStatus.INCOMPATIBLE) {
                PrimaryButton(stringResource(R.string.detail_repatch_fmt, gameVersion), onRepatch, Modifier.fillMaxWidth(), icon = Ic.Refresh, height = 50.dp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AngularButton(stringResource(R.string.more_uninstall), onDelete, Modifier.weight(1f), primary = false)
                AngularButton(stringResource(R.string.close), onClose, Modifier.weight(1f))
            }
        }
    }
}
