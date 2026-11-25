package app.fnfmodmanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import app.fnfmodmanager.MainViewModel
import app.fnfmodmanager.data.InstallUi
import app.fnfmodmanager.ui.components.AppBackground
import app.fnfmodmanager.ui.components.BottomNavBar
import app.fnfmodmanager.ui.components.FnfToastHost
import app.fnfmodmanager.ui.components.ModActions
import app.fnfmodmanager.ui.components.Tab
import app.fnfmodmanager.ui.screens.ActivitySheet
import app.fnfmodmanager.ui.screens.DeleteConfirm
import app.fnfmodmanager.ui.screens.DiscoverScreen
import app.fnfmodmanager.ui.screens.HomeActions
import app.fnfmodmanager.ui.screens.HomeScreen
import app.fnfmodmanager.ui.screens.InstallSheet
import app.fnfmodmanager.ui.screens.LibraryScreen
import app.fnfmodmanager.ui.screens.ModDetailSheet
import app.fnfmodmanager.ui.screens.SettingsScreen
import app.fnfmodmanager.ui.screens.VersionEditor

class Pickers(val zip: () -> Unit, val sourceFolder: () -> Unit, val modsFolder: () -> Unit)

@Composable
fun FnfApp(vm: MainViewModel, pickers: Pickers) {
    val state by vm.ui.collectAsState()
    val player by vm.playerState.collectAsState()
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var chooser by remember { mutableStateOf(false) }

    // A transfer that is interrupted when the screen turns off can leave 0 B files: keep it on while copying.
    val busy = state.install is InstallUi.Running || state.install is InstallUi.Preparing
    val view = LocalView.current
    DisposableEffect(busy) {
        view.keepScreenOn = busy
        onDispose { view.keepScreenOn = false }
    }

    val modActions = ModActions(vm::openDetail, vm::toggleFavorite, vm::preview)
    val home = HomeActions(
        onAddMod = { chooser = true },
        onPickZip = pickers.zip,
        onChooseFolder = pickers.modsFolder,
        onEditVersion = { vm.editVersion(true) },
        onLaunch = vm::launchGame,
        onBell = { vm.showActivity(true) },
        onSettings = { tab = Tab.SETTINGS },
        onSeeAll = { tab = Tab.LIBRARY },
        onTogglePlay = vm::togglePlay,
        onSeek = vm::seek,
        onMute = vm::toggleMute,
    )

    AppBackground {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    Tab.HOME -> HomeScreen(state, player, home, modActions)
                    Tab.DISCOVER -> DiscoverScreen()
                    Tab.LIBRARY -> LibraryScreen(state, vm::setQuery, vm::setFilter, vm::setSort, vm::rescan, modActions)
                    Tab.SETTINGS -> SettingsScreen(state, pickers.modsFolder) { vm.editVersion(true) }
                }
            }
            BottomNavBar(tab, { tab = it })
        }

        InstallSheet(
            ui = state.install,
            showChooser = chooser,
            gameVersion = state.gameVersion,
            onPickZip = { chooser = false; pickers.zip() },
            onPickFolder = { chooser = false; pickers.sourceFolder() },
            onConfirm = vm::confirmInstall,
            onBack = vm::backToReady,
            onClose = { chooser = false; vm.dismissInstall() },
        )

        state.detail?.let { m ->
            ModDetailSheet(
                m, state.gameVersion,
                onClose = vm::closeDetail,
                onFavorite = { vm.toggleFavorite(m) },
                onPreview = { vm.preview(m) },
                onRepatch = { vm.repatch(m) },
                onDelete = { vm.askDelete(m) },
            )
        }
        state.confirmDelete?.let { DeleteConfirm(it, vm::deleteConfirmed, vm::cancelDelete) }
        if (state.editVersion) VersionEditor(state.gameVersion, vm::setGameVersion) { vm.editVersion(false) }
        if (state.showActivity) ActivitySheet(state.activity) { vm.showActivity(false) }

        FnfToastHost(state.toast, vm::clearToast)
    }
}
