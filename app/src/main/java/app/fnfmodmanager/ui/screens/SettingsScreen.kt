package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
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
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.InfoCard
import app.fnfmodmanager.ui.theme.FnfColors

@Composable
fun SettingsScreen(state: UiState, onChooseFolder: () -> Unit, onEditVersion: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text(stringResource(R.string.tab_settings), style = MaterialTheme.typography.titleLarge, color = FnfColors.Text) }
        item {
            InfoCard(
                Ic.Folder, stringResource(R.string.settings_folder_title),
                if (state.modsTree == null) stringResource(R.string.settings_folder_none) else state.modsPathLabel.ifBlank { stringResource(R.string.settings_folder_set) },
                onClick = onChooseFolder,
            )
        }
        item { InfoCard(Ic.Gamepad, stringResource(R.string.settings_version_title), state.gameVersion, onClick = onEditVersion) }
        item { InfoCard(Ic.Info, stringResource(R.string.settings_help_title), stringResource(R.string.settings_help_body)) }
        item { Text(stringResource(R.string.settings_about), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted) }
    }
}
