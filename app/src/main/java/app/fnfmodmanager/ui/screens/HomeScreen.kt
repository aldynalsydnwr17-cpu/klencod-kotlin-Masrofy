package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.fnfmodmanager.R
import app.fnfmodmanager.data.PreviewPlayer
import app.fnfmodmanager.data.UiState
import app.fnfmodmanager.ui.components.EmptyState
import app.fnfmodmanager.ui.components.FnfLogo
import app.fnfmodmanager.ui.components.FnfMascot
import app.fnfmodmanager.ui.components.GameCard
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.IconTile
import app.fnfmodmanager.ui.components.ImportPanel
import app.fnfmodmanager.ui.components.MiniPlayer
import app.fnfmodmanager.ui.components.ModActions
import app.fnfmodmanager.ui.components.ModCard
import app.fnfmodmanager.ui.components.SectionHeader

class HomeActions(
    val onAddMod: () -> Unit,
    val onChooseFolder: () -> Unit,
    val onEditVersion: () -> Unit,
    val onLaunch: () -> Unit,
    val onBell: () -> Unit,
    val onSettings: () -> Unit,
    val onSeeAll: () -> Unit,
    val onTogglePlay: () -> Unit,
    val onSeek: (Float) -> Unit,
    val onMute: () -> Unit,
)

@Composable
fun HomeScreen(state: UiState, player: PreviewPlayer.State, actions: HomeActions, modActions: ModActions, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier.statusBarsPadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                FnfMascot(Modifier.height(140.dp))
                FnfLogo(Modifier.weight(1f).height(100.dp))
                IconTile(Ic.Bell, actions.onBell, stringResource(R.string.cd_activity), badge = state.unread)
                Spacer(Modifier.width(10.dp))
                IconTile(Ic.Settings, actions.onSettings, stringResource(R.string.tab_settings))
            }
        }
        item {
            GameCard(
                gameVersion = state.gameVersion,
                installed = state.gameInstalled,
                installedVersion = state.gameInstalledVersion,
                hasFolder = state.modsTree != null,
                path = state.gamePath,
                onEditVersion = actions.onEditVersion,
                onChoosePath = actions.onChooseFolder,
                onLaunch = actions.onLaunch,
            )
        }
        item { ImportPanel(actions.onAddMod) }
        item { SectionHeader(stringResource(R.string.home_installed), icon = Ic.Library, action = stringResource(R.string.home_see_all), onAction = actions.onSeeAll) }
        if (state.mods.isNotEmpty()) {
            item {
                val recent = state.mods.sortedByDescending { it.lastModified }.take(8)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recent, key = { it.folderName }) { m -> ModCard(m, modActions, Modifier.width(190.dp)) }
                }
            }
        } else {
            item {
                EmptyState(
                    Ic.Library, stringResource(R.string.library_empty_title), stringResource(R.string.library_empty_body),
                    actionText = stringResource(R.string.import_cta), onAction = actions.onAddMod,
                )
            }
        }
    }
}
