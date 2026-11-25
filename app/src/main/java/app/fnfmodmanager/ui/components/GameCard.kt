package app.fnfmodmanager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.ui.theme.FnfColors

/** Main game card of the home screen: name, version, real folder path, detection state and the launch button. */
@Composable
fun GameCard(
    gameVersion: String,
    installed: Boolean,
    installedVersion: String?,
    hasFolder: Boolean,
    path: String,
    onEditVersion: () -> Unit,
    onChoosePath: () -> Unit,
    onLaunch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    GlassCard(modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FnfIcon(Ic.Gamepad, size = 28.dp, red = true)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f).fnfClickable(onClick = onEditVersion)) {
                Text(stringResource(R.string.home_game_title), style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
                Text(stringResource(R.string.home_game_version_fmt, gameVersion), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted)
            }
            IconTile(Ic.Play, onLaunch, stringResource(R.string.home_launch), size = 44.dp)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().fnfClickable(onClick = onChoosePath),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FnfIcon(Ic.Folder, size = 20.dp, red = true)
            Text(
                path.ifBlank { stringResource(R.string.home_path_none) },
                style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, maxLines = 2, overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        StatusRow(installed, if (installed) stringResource(R.string.home_detected_fmt, installedVersion ?: gameVersion) else stringResource(R.string.home_not_detected))
        Spacer(Modifier.height(4.dp))
        StatusRow(hasFolder, stringResource(if (hasFolder) R.string.home_perm_ok else R.string.home_perm_missing))
    }
}

@Composable
private fun StatusRow(ok: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FnfIcon(if (ok) Ic.Check else Ic.Alert, size = 20.dp, tint = if (ok) FnfColors.Success else FnfColors.Warning)
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (ok) FnfColors.Success else FnfColors.Warning)
    }
}
