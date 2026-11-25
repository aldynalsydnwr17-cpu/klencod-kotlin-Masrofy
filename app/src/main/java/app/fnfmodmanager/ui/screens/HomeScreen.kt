package app.fnfmodmanager.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import app.fnfmodmanager.data.ModStatus
import app.fnfmodmanager.data.PreviewPlayer
import app.fnfmodmanager.data.UiState
import app.fnfmodmanager.ui.components.EmptyState
import app.fnfmodmanager.ui.components.FnfLogo
import app.fnfmodmanager.ui.components.IconTile
import app.fnfmodmanager.ui.components.Ic
import app.fnfmodmanager.ui.components.InfoCard
import app.fnfmodmanager.ui.components.MiniPlayer
import app.fnfmodmanager.ui.components.ModActions
import app.fnfmodmanager.ui.components.ModCard
import app.fnfmodmanager.ui.components.PrimaryButton
import app.fnfmodmanager.ui.components.SecondaryButton
import app.fnfmodmanager.ui.components.SectionHeader
import app.fnfmodmanager.ui.components.StatCard
import app.fnfmodmanager.ui.theme.FnfColors

class HomeActions(
    val onAddMod: () -> Unit,
    val onPickZip: () -> Unit,
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
                FnfLogo(Modifier.weight(1f).height(52.dp))
                IconTile(Ic.Bell, actions.onBell, stringResource(R.string.cd_activity), badge = state.unread)
                Spacer(Modifier.width(10.dp))
                IconTile(Ic.Settings, actions.onSettings, stringResource(R.string.tab_settings))
            }
        }
        item {
            InfoCard(
                icon = Ic.Gamepad,
                title = stringResource(R.string.home_game_title),
                subtitle = stringResource(R.string.home_game_version_fmt, state.gameVersion),
                onClick = actions.onEditVersion,
                trailing = { IconTile(Ic.Play, actions.onLaunch, stringResource(R.string.home_launch), size = 44.dp) },
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(state.mods.size.toString(), stringResource(R.string.stat_total), Ic.Layers, Modifier.weight(1f))
                StatCard(state.count(ModStatus.INSTALLED).toString(), stringResource(R.string.status_installed), Ic.Check, Modifier.weight(1f), tint = FnfColors.Success)
                StatCard(state.count(ModStatus.INCOMPATIBLE).toString(), stringResource(R.string.status_incompatible), Ic.Alert, Modifier.weight(1f), tint = FnfColors.Warning)
            }
        }
        if (state.modsTree == null) {
            item {
                EmptyState(
                    Ic.FolderPlus, stringResource(R.string.home_no_folder_title), stringResource(R.string.home_no_folder_body),
                    actionText = stringResource(R.string.home_choose_folder), onAction = actions.onChooseFolder,
                )
            }
        } else {
            item { PrimaryButton(stringResource(R.string.home_add_mod), actions.onAddMod, Modifier.fillMaxWidth(), icon = Ic.FolderPlus) }
            item { SecondaryButton(stringResource(R.string.home_pick_zip), actions.onPickZip, Modifier.fillMaxWidth(), icon = Ic.Download) }
        }
        if (player.loaded) {
            item { MiniPlayer(player, actions.onTogglePlay, actions.onSeek, actions.onMute) }
        }
        if (state.mods.isNotEmpty()) {
            item { SectionHeader(stringResource(R.string.home_recent), action = stringResource(R.string.home_see_all), onAction = actions.onSeeAll) }
            item {
                val recent = state.mods.sortedByDescending { it.lastModified }.take(8)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recent, key = { it.folderName }) { m -> ModCard(m, modActions, Modifier.width(160.dp)) }
                }
            }
        } else if (state.modsTree != null && !state.loading) {
            item { EmptyState(Ic.Layers, stringResource(R.string.library_empty_title), stringResource(R.string.library_empty_body)) }
        }
    }
}
