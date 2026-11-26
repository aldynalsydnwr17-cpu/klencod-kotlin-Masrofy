package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.UiState
import app.fnfmodmanager.ui.components.FnfIcon
import app.fnfmodmanager.ui.components.GlassCard
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.ImportPanel
import app.fnfmodmanager.ui.components.SecondaryButton
import app.fnfmodmanager.ui.theme.FnfColors

/** Local import: explains the steps and offers the file picker. There is no online catalogue. */
@Composable
fun ImportScreen(state: UiState, onImport: () -> Unit, onChooseFolder: () -> Unit, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { Text(stringResource(R.string.tab_import), style = MaterialTheme.typography.titleLarge, color = FnfColors.Text) }
        item { Text(stringResource(R.string.import_intro), style = MaterialTheme.typography.bodyMedium, color = FnfColors.Muted) }
        item { ImportPanel(onImport) }
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.import_steps_title), style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
                Step(1, stringResource(R.string.import_step1), done = state.modsTree != null)
                Step(2, stringResource(R.string.import_step2), done = state.gameVersion.isNotBlank())
                Step(3, stringResource(R.string.import_step3), done = false)
                Step(4, stringResource(R.string.import_step4), done = false)
            }
        }
        item {
            SecondaryButton(
                stringResource(if (state.modsTree == null) R.string.home_choose_folder else R.string.settings_perm_regrant),
                onChooseFolder, Modifier.fillMaxWidth(), icon = Ic.Folder,
            )
        }
        item {
            GlassCard(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.import_limits_title), style = MaterialTheme.typography.titleSmall, color = FnfColors.Text)
                Text(stringResource(R.string.import_limits_body), style = MaterialTheme.typography.bodySmall, color = FnfColors.Muted, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun Step(n: Int, text: String, done: Boolean) {
    Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.Top) {
        FnfIcon(if (done) Ic.Check else Ic.Info, size = 20.dp, tint = if (done) FnfColors.Success else FnfColors.Muted)
        Spacer(Modifier.width(10.dp))
        Text("$n. $text", style = MaterialTheme.typography.bodyMedium, color = FnfColors.Text)
    }
}
