package app.fnfmodmanager.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class ModFormatException(message: String) : Exception(message)

/** One file of a mod, path is relative to the mod root (the folder that holds _polymod_meta.json). */
class SrcFile(val path: String, val size: Long, val handle: Any?)

interface ModSource : Closeable {
    val label: String
    val suggestedFolder: String
    val files: List<SrcFile>
    fun open(file: SrcFile): InputStream
}

internal const val META_NAME = "_polymod_meta.json"
internal const val ICON_NAME = "_polymod_icon.png"

internal fun sanitizeFolder(s: String): String {
    val t = s.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_', '.', ' ')
    return t.ifEmpty { "mod" }
}

/** Shallowest _polymod_meta.json decides the mod root. Returns the path prefix ("" or "Folder/"). */
internal fun detectRoot(paths: List<String>): String? =
    paths.filter { it.substringAfterLast('/') == META_NAME }
        .minByOrNull { p -> p.count { it == '/' } }
        ?.removeSuffix(META_NAME)

internal fun notVsliceMessage(paths: List<String>): String =
    if (paths.any { it.substringAfterLast('/') == "pack.json" })
        "ده مود Psych Engine مش V-Slice. اللعبة الأصلية مش هتشغله."
    else
        "مفيش ملف _polymod_meta.json جوه المود، يعني مش مود V-Slice."

internal fun isSafe(path: String): Boolean =
    !path.startsWith("/") && !path.contains('\\') && path.split('/').none { it == ".." }

internal fun displayName(context: Context, uri: Uri): String? = try {
    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
        if (c.moveToFirst()) c.getString(0) else null
    }
} catch (e: Exception) {
    null
}

private fun junk(name: String) =
    name.startsWith("__MACOSX/") || name.contains("/__MACOSX/") || name.endsWith(".DS_Store") || name.endsWith("Thumbs.db")

class ZipModSource private constructor(
    override val label: String,
    private val zip: ZipFile,
    private val temp: File?,
    override val files: List<SrcFile>,
    override val suggestedFolder: String,
) : ModSource {

    override fun open(file: SrcFile): InputStream = zip.getInputStream(file.handle as ZipEntry)

    override fun close() {
        runCatching { zip.close() }
        temp?.delete()
    }

    companion object {
        fun open(context: Context, uri: Uri): ZipModSource {
            val label = displayName(context, uri) ?: "mod.zip"
            var temp: File? = null
            val zip: ZipFile = try {
                try {
                    // Random access straight from the picked file: no extra copy of a 300 MB zip.
                    context.contentResolver.openFileDescriptor(uri, "r")!!.use { pfd ->
                        ZipFile("/proc/self/fd/${pfd.fd}")
                    }
                } catch (e: Exception) {
                    val f = File(context.cacheDir, "import_${System.nanoTime()}.zip")
                    context.contentResolver.openInputStream(uri)!!.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
                    temp = f
                    ZipFile(f)
                }
            } catch (e: Exception) {
                temp?.delete()
                throw ModFormatException("الملف ده مش ZIP سليم. جرّب تنزله تاني.")
            }

            val all = zip.entries().asSequence().filter { !it.isDirectory && !junk(it.name) }.toList()
            val names = all.map { it.name }
            val prefix = detectRoot(names) ?: run {
                zip.close(); temp?.delete()
                throw ModFormatException(notVsliceMessage(names))
            }
            val files = all.filter { it.name.startsWith(prefix) }.map { SrcFile(it.name.removePrefix(prefix), it.size, it) }
            if (files.any { !isSafe(it.path) }) {
                zip.close(); temp?.delete()
                throw ModFormatException("الملف فيه مسارات مش آمنة، مش هركّبه.")
            }
            val base = prefix.trimEnd('/').substringAfterLast('/').ifEmpty { label.substringBeforeLast('.') }
            return ZipModSource(label, zip, temp, files, sanitizeFolder(base))
        }
    }
}

class TreeModSource private constructor(
    private val fs: SafFs,
    override val label: String,
    override val files: List<SrcFile>,
    override val suggestedFolder: String,
) : ModSource {

    override fun open(file: SrcFile): InputStream = fs.openIn(file.handle as Uri)

    override fun close() {}

    companion object {
        fun open(context: Context, tree: Uri): TreeModSource {
            val fs = SafFs(context.contentResolver, tree)
            val found = ArrayList<Pair<String, SafFs.Child>>()
            val queue = ArrayDeque<Pair<String, Uri>>()
            queue.add("" to fs.rootDoc())
            while (queue.isNotEmpty()) {
                val (rel, dir) = queue.removeFirst()
                for (c in fs.children(dir)) {
                    val p = rel + c.name
                    if (c.isDir) queue.add("$p/" to c.uri) else if (!junk(p)) found += p to c
                }
            }
            val names = found.map { it.first }
            val prefix = detectRoot(names) ?: throw ModFormatException(notVsliceMessage(names))
            val files = found.filter { it.first.startsWith(prefix) }.map { SrcFile(it.first.removePrefix(prefix), it.second.size, it.second.uri) }
            val rootName = fs.rootName().ifEmpty { "mod" }
            val base = prefix.trimEnd('/').substringAfterLast('/').ifEmpty { rootName }
            return TreeModSource(fs, rootName, files, sanitizeFolder(base))
        }
    }
}
