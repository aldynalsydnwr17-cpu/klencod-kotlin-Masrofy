package app.fnfmodmanager.data

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.io.File

class ModsRepository(private val context: Context, private val prefs: Prefs) {
    private val cr = context.contentResolver
    private val coversDir = File(context.filesDir, "covers").apply { mkdirs() }

    /** Reads every folder inside mods/ and works out its status. Runs blocking SAF queries: call from IO. */
    fun scan(tree: Uri, gameVersion: String): List<ModInfo> {
        val fs = SafFs(cr, tree)
        val root = ModsLocator.find(fs) ?: return emptyList()
        val installed = prefs.installedByApp
        val favorites = prefs.favorites
        return fs.children(root).filter { it.isDir }.map { dir ->
            runCatching { info(fs, dir, gameVersion, installed, favorites) }.getOrElse {
                ModInfo(dir.name, dir.uri, dir.name, "", "", "", "", ModStatus.ERROR, "ما قدرتش أقرا الفولدر", null, dir.modified, dir.name in favorites)
            }
        }
    }

    private fun info(fs: SafFs, dir: SafFs.Child, game: String, installed: Set<String>, favorites: Set<String>): ModInfo {
        val kids = fs.children(dir.uri)
        val meta = kids.firstOrNull { it.name == META_NAME }
        val icon = kids.firstOrNull { it.name == ICON_NAME }
        val cover = icon?.takeIf { it.size != 0L }?.let { cacheCover(fs, dir.name, it) }

        var title = dir.name
        var author = ""
        var desc = ""
        var modVersion = ""
        var api = ""
        var status = ModStatus.COMPATIBLE
        var detail: String? = null
        when {
            meta == null -> { status = ModStatus.ERROR; detail = "مفيش ملف $META_NAME" }
            meta.size == 0L -> { status = ModStatus.ERROR; detail = "$META_NAME فاضي (0B). امسح المود وركّبه تاني." }
            else -> {
                try {
                    val o = JSONObject(String(fs.readBytes(meta.uri), Charsets.UTF_8).removePrefix("﻿"))
                    title = o.optString("title").ifBlank { dir.name }
                    author = o.optString("author")
                    desc = o.optString("description")
                    modVersion = o.optString("mod_version")
                    api = o.optString("api_version")
                    status = when {
                        !apiMatches(api, game) -> ModStatus.INCOMPATIBLE
                        dir.name in installed -> ModStatus.INSTALLED
                        else -> ModStatus.COMPATIBLE
                    }
                    if (status == ModStatus.INCOMPATIBLE) detail = "api_version في المود = ${api.ifBlank { "مش مكتوب" }} ومش نفس نسخة اللعبة ($game)."
                } catch (e: Exception) {
                    status = ModStatus.ERROR
                    detail = "$META_NAME مش JSON سليم."
                }
            }
        }
        return ModInfo(dir.name, dir.uri, title, author, desc, modVersion, api, status, detail, cover, dir.modified, dir.name in favorites)
    }

    /** Byte-for-byte copy of the mod's own icon, re-copied only when the size changes. Never re-encoded. */
    private fun cacheCover(fs: SafFs, folder: String, icon: SafFs.Child): File? = runCatching {
        val f = File(coversDir, "$folder.png")
        if (!f.exists() || (icon.size > 0 && f.length() != icon.size)) f.writeBytes(fs.readBytes(icon.uri))
        f
    }.getOrNull()

    fun apiMatches(api: String, game: String): Boolean {
        if (api.isBlank() || game.isBlank()) return false
        return api.split('.').take(2) == game.split('.').take(2)
    }

    fun delete(tree: Uri, mod: ModInfo): Boolean {
        val ok = SafFs(cr, tree).delete(mod.docUri)
        if (ok) {
            File(coversDir, "${mod.folderName}.png").delete()
            prefs.installedByApp = prefs.installedByApp - mod.folderName
            prefs.favorites = prefs.favorites - mod.folderName
        }
        return ok
    }

    /** First instrumental found in songs/<song>/Inst*.ogg, as (song folder, file). */
    fun findPreview(tree: Uri, mod: ModInfo): Pair<String, Uri>? {
        val fs = SafFs(cr, tree)
        val songs = fs.children(mod.docUri).firstOrNull { it.isDir && it.name == "songs" } ?: return null
        for (song in fs.children(songs.uri).filter { it.isDir }) {
            val files = fs.children(song.uri)
            val inst = files.firstOrNull { it.name == "Inst.ogg" }
                ?: files.firstOrNull { it.name.startsWith("Inst") && it.name.endsWith(".ogg") }
            if (inst != null) return song.name to inst.uri
        }
        return null
    }
}
