package app.fnfmodmanager.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URLEncoder

/**
 * The app-owned install manifest: for every mod this app installs it records exactly which files it wrote.
 * Uninstall and verify work from this list only, so nothing the app did not put there is ever deleted.
 */
class ManifestStore(context: Context) {
    data class Entry(val path: String, val size: Long)
    data class Manifest(val folder: String, val title: String, val time: Long, val files: List<Entry>)

    private val dir = File(context.filesDir, "manifests").apply { mkdirs() }
    private fun file(folder: String) = File(dir, URLEncoder.encode(folder, "UTF-8") + ".json")

    fun exists(folder: String) = file(folder).exists()

    fun save(m: Manifest) {
        val arr = JSONArray()
        m.files.forEach { arr.put(JSONObject().put("p", it.path).put("s", it.size)) }
        file(m.folder).writeText(JSONObject().put("folder", m.folder).put("title", m.title).put("time", m.time).put("files", arr).toString())
    }

    fun load(folder: String): Manifest? = runCatching {
        val o = JSONObject(file(folder).readText())
        val arr = o.getJSONArray("files")
        Manifest(
            o.getString("folder"), o.optString("title"), o.optLong("time"),
            (0 until arr.length()).map { arr.getJSONObject(it).let { e -> Entry(e.getString("p"), e.getLong("s")) } },
        )
    }.getOrNull()

    /** Every manifest this app has written (used to find files that two mods both provide). */
    fun all(): List<Manifest> = dir.listFiles()?.mapNotNull { f ->
        runCatching { load(java.net.URLDecoder.decode(f.nameWithoutExtension, "UTF-8")) }.getOrNull()
    } ?: emptyList()

    fun delete(folder: String) { file(folder).delete() }
}
