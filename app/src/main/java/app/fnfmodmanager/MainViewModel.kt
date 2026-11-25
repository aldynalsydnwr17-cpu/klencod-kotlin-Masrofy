package app.fnfmodmanager

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.fnfmodmanager.data.ActivityItem
import app.fnfmodmanager.data.InstallPhase
import app.fnfmodmanager.data.InstallUi
import app.fnfmodmanager.data.Installer
import app.fnfmodmanager.data.ModFormatException
import app.fnfmodmanager.data.ModInfo
import app.fnfmodmanager.data.ModSource
import app.fnfmodmanager.data.ModsRepository
import app.fnfmodmanager.data.PreparedMod
import app.fnfmodmanager.data.Prefs
import app.fnfmodmanager.data.PreviewPlayer
import app.fnfmodmanager.data.SafFs
import app.fnfmodmanager.data.SortMode
import app.fnfmodmanager.data.StatusFilter
import app.fnfmodmanager.data.ToastKind
import app.fnfmodmanager.data.ToastMsg
import app.fnfmodmanager.data.TreeModSource
import app.fnfmodmanager.data.UiState
import app.fnfmodmanager.data.ZipModSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx: Context = app.applicationContext
    private val prefs = Prefs(ctx)
    private val repo = ModsRepository(ctx, prefs)
    private val installer = Installer(ctx)
    private val player = PreviewPlayer(ctx)

    val playerState: StateFlow<PreviewPlayer.State> = player.state

    private val _ui = MutableStateFlow(
        UiState(gameVersion = prefs.gameVersion, modsTree = restoreTree(), activity = prefs.activity),
    )
    val ui: StateFlow<UiState> = _ui.asStateFlow()

    private var pending: ModSource? = null
    private var lastPrepared: PreparedMod? = null

    init {
        rescan()
    }

    // ------------------------------------------------------------------ setup
    private fun restoreTree(): Uri? {
        val uri = prefs.modsTree?.let { Uri.parse(it) } ?: return null
        val granted = ctx.contentResolver.persistedUriPermissions.any { it.uri == uri && it.isWritePermission }
        return if (granted) uri else null
    }

    fun setModsTree(uri: Uri) {
        runCatching {
            ctx.contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION,
            )
        }
        prefs.modsTree = uri.toString()
        _ui.update { it.copy(modsTree = uri) }
        toast(ctx.getString(R.string.toast_folder_saved))
        log(ctx.getString(R.string.log_folder_saved), true)
        rescan()
    }

    fun setGameVersion(version: String) {
        val v = version.trim()
        if (v.isEmpty()) return
        prefs.gameVersion = v
        _ui.update { it.copy(gameVersion = v, editVersion = false) }
        toast(ctx.getString(R.string.toast_version_saved))
        rescan()
    }

    fun rescan() {
        val tree = _ui.value.modsTree ?: return
        viewModelScope.launch {
            _ui.update { it.copy(loading = true) }
            val version = _ui.value.gameVersion
            val (mods, label) = withContext(Dispatchers.IO) {
                val list = runCatching { repo.scan(tree, version) }.getOrDefault(emptyList())
                val name = runCatching { SafFs(ctx.contentResolver, tree).rootName() }.getOrDefault("")
                list to (if (name == "mods" || name.isEmpty()) "mods" else "$name / mods")
            }
            _ui.update { it.copy(mods = mods, modsPathLabel = label, loading = false) }
        }
    }

    // ------------------------------------------------------------------ install flow
    fun pickZip(uri: Uri) = prepareSource(isZip = true) { ZipModSource.open(ctx, uri) }

    fun pickSourceFolder(uri: Uri) = prepareSource(isZip = false) { TreeModSource.open(ctx, uri) }

    private fun prepareSource(isZip: Boolean, open: () -> ModSource) {
        viewModelScope.launch {
            closePending()
            _ui.update { it.copy(install = InstallUi.Preparing) }
            try {
                val src = withContext(Dispatchers.IO) { open() }
                pending = src
                val prepared = installer.prepare(src, isZip, _ui.value.modsTree)
                lastPrepared = prepared
                _ui.update { it.copy(install = InstallUi.Ready(prepared)) }
            } catch (e: ModFormatException) {
                closePending()
                _ui.update { it.copy(install = InstallUi.Failed(e.message ?: "")) }
            } catch (e: Exception) {
                closePending()
                _ui.update {
                    it.copy(install = InstallUi.Failed(ctx.getString(R.string.install_read_failed), listOfNotNull(e.message)))
                }
            }
        }
    }

    fun confirmInstall(version: String) {
        val p = (_ui.value.install as? InstallUi.Ready)?.prepared ?: return
        val tree = _ui.value.modsTree
        if (tree == null) {
            _ui.update { it.copy(install = InstallUi.Failed(ctx.getString(R.string.toast_choose_folder))) }
            return
        }
        if (!p.metaReadable) {
            _ui.update { it.copy(install = InstallUi.Failed(ctx.getString(R.string.install_meta_unreadable))) }
            return
        }
        val v = version.trim().ifEmpty { _ui.value.gameVersion }
        if (v != _ui.value.gameVersion) {
            prefs.gameVersion = v
            _ui.update { it.copy(gameVersion = v) }
        }
        viewModelScope.launch {
            fun running(phase: InstallPhase, done: Int, bytes: Long, current: String) =
                InstallUi.Running(p, phase, done, p.fileCount, bytes, p.totalBytes, current)

            _ui.update { it.copy(install = running(InstallPhase.PREPARE, 0, 0L, "")) }
            try {
                val result = installer.install(p, tree, v, p.exists) { phase, done, bytes, current ->
                    _ui.update { s -> if (s.install is InstallUi.Running) s.copy(install = running(phase, done, bytes, current)) else s }
                }
                prefs.installedByApp = prefs.installedByApp + p.folderName
                log(ctx.getString(R.string.log_installed_fmt, result.title), true)
                closePending()
                _ui.update { it.copy(install = InstallUi.Done(result)) }
                rescan()
            } catch (e: Exception) {
                log(ctx.getString(R.string.log_install_failed_fmt, p.title), false)
                _ui.update {
                    it.copy(install = InstallUi.Failed(ctx.getString(R.string.install_failed_body), listOfNotNull(e.message)))
                }
            }
        }
    }

    /** From the failure screen back to the summary of the same mod. */
    fun backToReady() {
        val p = lastPrepared
        _ui.update { it.copy(install = if (p != null && pending != null) InstallUi.Ready(p) else null) }
    }

    fun dismissInstall() {
        closePending()
        _ui.update { it.copy(install = null) }
    }

    private fun closePending() {
        runCatching { pending?.close() }
        pending = null
    }

    // ------------------------------------------------------------------ mods
    fun askDelete(mod: ModInfo) = _ui.update { it.copy(confirmDelete = mod.folderName) }

    fun cancelDelete() = _ui.update { it.copy(confirmDelete = null) }

    fun deleteConfirmed() {
        val name = _ui.value.confirmDelete ?: return
        val mod = _ui.value.mods.firstOrNull { it.folderName == name }
        val tree = _ui.value.modsTree
        _ui.update { it.copy(confirmDelete = null, detailName = null) }
        if (mod == null || tree == null) return
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) { runCatching { repo.delete(tree, mod) }.getOrDefault(false) }
            if (ok) {
                toast(ctx.getString(R.string.toast_deleted))
                log(ctx.getString(R.string.log_deleted_fmt, mod.title), true)
            } else {
                toast(ctx.getString(R.string.toast_error_generic), ToastKind.ERROR)
            }
            rescan()
        }
    }

    fun repatch(mod: ModInfo) {
        val tree = _ui.value.modsTree ?: return
        val version = _ui.value.gameVersion
        viewModelScope.launch {
            try {
                installer.repatch(tree, mod.docUri, version)
                toast(ctx.getString(R.string.toast_repatched))
                log(ctx.getString(R.string.log_repatched_fmt, mod.title, version), true)
            } catch (e: Exception) {
                toast(ctx.getString(R.string.toast_error_generic), ToastKind.ERROR)
            }
            rescan()
        }
    }

    fun toggleFavorite(mod: ModInfo) {
        val now = prefs.favorites
        prefs.favorites = if (mod.folderName in now) now - mod.folderName else now + mod.folderName
        _ui.update { s ->
            s.copy(mods = s.mods.map { if (it.folderName == mod.folderName) it.copy(favorite = !it.favorite) else it })
        }
    }

    // ------------------------------------------------------------------ preview player
    fun preview(mod: ModInfo) {
        val tree = _ui.value.modsTree ?: return
        viewModelScope.launch {
            val found = withContext(Dispatchers.IO) { runCatching { repo.findPreview(tree, mod) }.getOrNull() }
            if (found == null) {
                toast(ctx.getString(R.string.toast_preview_none), ToastKind.INFO)
                return@launch
            }
            if (!player.load(viewModelScope, found.second, "${mod.title} · ${found.first}")) {
                toast(ctx.getString(R.string.toast_preview_fail), ToastKind.ERROR)
            }
        }
    }

    fun togglePlay() = player.toggle(viewModelScope)

    fun seek(fraction: Float) = player.seek(fraction)

    fun toggleMute() = player.toggleMute()

    // ------------------------------------------------------------------ game
    fun launchGame() {
        val intent = ctx.packageManager.getLaunchIntentForPackage(Prefs.GAME_PACKAGE)
        if (intent == null) {
            toast(ctx.getString(R.string.toast_game_missing), ToastKind.ERROR)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(intent)
        }
    }

    // ------------------------------------------------------------------ ui state setters
    fun setQuery(q: String) = _ui.update { it.copy(query = q) }
    fun setFilter(f: StatusFilter) = _ui.update { it.copy(filter = f) }
    fun setSort(s: SortMode) = _ui.update { it.copy(sort = s) }
    fun openDetail(mod: ModInfo) = _ui.update { it.copy(detailName = mod.folderName) }
    fun closeDetail() = _ui.update { it.copy(detailName = null) }
    fun showActivity(show: Boolean) = _ui.update { it.copy(showActivity = show, unread = if (show) false else it.unread) }
    fun editVersion(show: Boolean) = _ui.update { it.copy(editVersion = show) }
    fun clearToast() = _ui.update { it.copy(toast = null) }

    fun toast(text: String, kind: ToastKind = ToastKind.SUCCESS) = _ui.update { it.copy(toast = ToastMsg(text, kind)) }

    private fun log(text: String, ok: Boolean) {
        val list = (listOf(ActivityItem(System.currentTimeMillis(), text, ok)) + prefs.activity).take(30)
        prefs.activity = list
        _ui.update { it.copy(activity = list, unread = true) }
    }

    override fun onCleared() {
        player.release()
        closePending()
    }
}
