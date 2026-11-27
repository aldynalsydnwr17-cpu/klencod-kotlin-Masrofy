package app.fnfmodmanager.data

import android.net.Uri
import java.io.File

/** What the app knows about one installed mod, taken from its _polymod_meta.json. */
enum class ModStatus { INSTALLED, COMPATIBLE, INCOMPATIBLE, ERROR }

enum class StatusFilter { ALL, INSTALLED, COMPATIBLE, INCOMPATIBLE, ERROR, FAVORITES }

enum class SortMode { NAME, RECENT, FAVORITES }

enum class ToastKind { SUCCESS, ERROR, INFO }

data class ToastMsg(
    val text: String,
    val kind: ToastKind = ToastKind.SUCCESS,
    val id: Long = System.nanoTime(),
)

data class ModInfo(
    val folderName: String,
    val docUri: Uri,
    val title: String,
    val author: String,
    val description: String,
    val modVersion: String,
    val apiVersion: String,
    val status: ModStatus,
    /** Human readable reason when status is ERROR or INCOMPATIBLE, otherwise null. */
    val statusDetail: String?,
    /** Cover extracted from the mod's own _polymod_icon.png (byte-for-byte copy), or null. */
    val cover: File?,
    val lastModified: Long,
    val favorite: Boolean,
    /** True when the cover is one the user picked by hand instead of the mod's own icon. */
    val customCover: Boolean = false,
    /** True when this app installed the mod and holds an install manifest for it (only those can be removed by the app). */
    val managed: Boolean = false,
)

data class ActivityItem(val time: Long, val text: String, val ok: Boolean)

/** A mod that was read from a ZIP / folder and is ready to be installed. */
class PreparedMod(
    val source: ModSource,
    val sourceLabel: String,
    val isZip: Boolean,
    val title: String,
    val author: String,
    val modApiVersion: String,
    val fileCount: Int,
    val totalBytes: Long,
    val folderName: String,
    val exists: Boolean,
    val coverBytes: ByteArray?,
    val metaReadable: Boolean,
    val findings: List<Finding> = emptyList(),
)

data class InstallResult(
    val folderName: String,
    val title: String,
    val fileCount: Int,
    val totalBytes: Long,
    val oldApi: String,
    val newApi: String,
    val warnings: List<String>,
)

enum class ConflictMode { NONE, REPLACE, RENAME }

class NotManagedException : Exception()

data class UninstallResult(val deleted: Int, val left: Int, val removedFolder: Boolean)

data class VerifyReport(
    val title: String,
    val managed: Boolean,
    val checked: Int,
    val issues: List<String>,
    val notes: List<String>,
)

enum class InstallPhase { PREPARE, COPY, PATCH, VERIFY }

sealed interface InstallUi {
    data object Preparing : InstallUi
    data class Ready(val prepared: PreparedMod) : InstallUi
    data class Running(
        val prepared: PreparedMod,
        val phase: InstallPhase,
        val filesDone: Int,
        val filesTotal: Int,
        val bytesDone: Long,
        val bytesTotal: Long,
        val current: String,
    ) : InstallUi

    data class Done(val result: InstallResult) : InstallUi
    data class Failed(val message: String, val details: List<String> = emptyList()) : InstallUi
}

data class UiState(
    val gameVersion: String,
    val modsTree: Uri? = null,
    val modsPathLabel: String = "",
    val mods: List<ModInfo> = emptyList(),
    val loading: Boolean = false,
    val install: InstallUi? = null,
    val toast: ToastMsg? = null,
    val activity: List<ActivityItem> = emptyList(),
    val unread: Boolean = false,
    val query: String = "",
    val filter: StatusFilter = StatusFilter.ALL,
    val sort: SortMode = SortMode.NAME,
    val detailName: String? = null,
    val showActivity: Boolean = false,
    val editVersion: Boolean = false,
    val confirmDelete: String? = null,
    val gameInstalled: Boolean = false,
    val gameInstalledVersion: String? = null,
    val gamePath: String = "",
    val amoled: Boolean = false,
    val verify: VerifyReport? = null,
    val verifying: Boolean = false,
) {
    val detail: ModInfo? get() = detailName?.let { n -> mods.firstOrNull { it.folderName == n } }

    fun count(status: ModStatus) = mods.count { it.status == status }

    fun visibleMods(): List<ModInfo> {
        val q = query.trim()
        val filtered = mods.filter { m ->
            (q.isEmpty() || m.title.contains(q, true) || m.folderName.contains(q, true) || m.author.contains(q, true)) &&
                when (filter) {
                    StatusFilter.ALL -> true
                    StatusFilter.INSTALLED -> m.status == ModStatus.INSTALLED
                    StatusFilter.COMPATIBLE -> m.status == ModStatus.COMPATIBLE
                    StatusFilter.INCOMPATIBLE -> m.status == ModStatus.INCOMPATIBLE
                    StatusFilter.ERROR -> m.status == ModStatus.ERROR
                    StatusFilter.FAVORITES -> m.favorite
                }
        }
        return when (sort) {
            SortMode.NAME -> filtered.sortedBy { it.title.lowercase() }
            SortMode.RECENT -> filtered.sortedByDescending { it.lastModified }
            SortMode.FAVORITES -> filtered.sortedWith(compareByDescending<ModInfo> { it.favorite }.thenBy { it.title.lowercase() })
        }
    }
}
