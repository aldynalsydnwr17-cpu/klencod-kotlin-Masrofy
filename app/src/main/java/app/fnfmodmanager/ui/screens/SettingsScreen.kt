package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.UiState
import app.fnfmodmanager.ui.components.GlassCard
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.InfoCard
import app.fnfmodmanager.ui.components.SecondaryButton
import app.fnfmodmanager.ui.components.StatusChip
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun SettingsScreen(
    state: UiState,
    onChooseFolder: () -> Unit,
    onEditVersion: () -> Unit,
    onRevoke: () -> Unit,
    onAmoled: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier.statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.titleLarge, color = FnfColors.Text) }
        item {
            InfoCard(
                Ic.Folder, stringResource(R.string.settings_folder_title),
                if (state.modsTree == null) stringResource(R.string.settings_folder_none) else state.gamePath.takeIf { it.isNotBlank() }?.let { "\u2066$it\u2069" } ?: stringResource(R.string.settings_folder_set),
                onClick = onChooseFolder,
            )
        }
        item {
            val detected = state.gameInstalledVersion
            InfoCard(
                Ic.Gamepad, stringResource(R.string.settings_version_title),
                state.gameVersion + if (detected != null) "  ·  " + stringResource(R.string.home_detected_fmt, detected) else "",
                onClick = onEditVersion,
            )
        }
        // theme
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_theme_title), style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
                Text(stringResource(R.string.settings_theme_body), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(stringResource(R.string.theme_default), { onAmoled(false) }, Modifier.weight(1f), icon = if (!state.amoled) Ic.Check else null, height = 46.dp)
                    SecondaryButton(stringResource(R.string.theme_amoled), { onAmoled(true) }, Modifier.weight(1f), icon = if (state.amoled) Ic.Check else null, height = 46.dp)
                }
            }
        }
        // file permission
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_perm_title), style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
                    StatusChip(if (state.modsTree != null) app.fnfmodmanager.data.ModStatus.INSTALLED else app.fnfmodmanager.data.ModStatus.ERROR, compact = true)
                }
                Text(
                    stringResource(if (state.modsTree != null) R.string.settings_perm_granted else R.string.settings_perm_missing),
                    style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, modifier = Modifier.padding(top = 2.dp, bottom = 10.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton(stringResource(R.string.settings_perm_regrant), onChooseFolder, Modifier.weight(1f), icon = Ic.Folder, height = 46.dp)
                    if (state.modsTree != null) SecondaryButton(stringResource(R.string.settings_perm_revoke), onRevoke, Modifier.weight(1f), icon = Ic.Trash, height = 46.dp)
                }
            }
        }
        item { InfoCard(Ic.Info, stringResource(R.string.settings_help_title), stringResource(R.string.settings_help_body)) }
        item { Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted) }
    }
}
