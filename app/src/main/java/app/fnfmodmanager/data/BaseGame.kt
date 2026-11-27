package app.fnfmodmanager.data

import android.content.Context
import android.content.pm.PackageManager
import org.json.JSONObject
import java.util.zip.ZipFile

/**
 * Reads facts straight from the installed game (its APK is readable by other apps), so the checks follow the REAL
 * game version instead of a list that goes out of date. Everything here fails soft: if the APK layout is not what we expect,
 * the caller falls back to the small built-in table.
 */
object BaseGame {
    /** character id -> renderType used by the game's own data/characters/<id>.json */
    class Data(val renderTypes: Map<String, String>, val scripts: Set<String>)
    @Volatile private var cache: Pair<Long, Data>? = null

    private val CHARACTER = Regex("(^|/)data/characters/([^/]+)\\.json$")
    private val SCRIPT = Regex("(^|/)scripts/characters/([^/]+)\\.hxc$")

    fun characters(context: Context): Data {
        val info = runCatching {
            context.packageManager.getPackageInfo(Prefs.GAME_PACKAGE, 0)
        }.getOrNull() ?: return Data(emptyMap(), emptySet())
        val stamp = info.lastUpdateTime
        cache?.let { if (it.first == stamp) return it.second }
        val ai = info.applicationInfo ?: return Data(emptyMap(), emptySet())
        val apks = listOfNotNull(ai.sourceDir) + (ai.splitSourceDirs?.toList() ?: emptyList())
        val out = HashMap<String, String>()
        val scripts = HashSet<String>()
        for (apk in apks) runCatching {
            ZipFile(apk).use { z ->
                val e = z.entries()
                while (e.hasMoreElements()) {
                    val entry = e.nextElement()
                    if (entry.isDirectory || entry.size > 2_000_000) continue
                    SCRIPT.find(entry.name)?.let { scripts += it.groupValues[2] }
                    val m = CHARACTER.find(entry.name) ?: continue
                    val id = m.groupValues[2]
                    val rt = runCatching {
                        JSONObject(String(z.getInputStream(entry).readBytes(), Charsets.UTF_8).removePrefix("﻿")).optString("renderType")
                    }.getOrNull().orEmpty()
                    if (rt.isNotBlank()) out[id] = rt
                }
            }
        }
        val d = Data(out, scripts)
        cache = stamp to d
        return d
    }
}
