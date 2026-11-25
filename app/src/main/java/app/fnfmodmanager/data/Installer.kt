package app.fnfmodmanager.data

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.IOException
import kotlin.coroutines.coroutineContext

/** Where the mods live: the folder named "mods" inside the game's data folder. */
object ModsLocator {
    fun find(fs: SafFs): Uri? {
        val root = fs.rootDoc()
        if (fs.rootName() == "mods") return root
        return fs.children(root).firstOrNull { it.isDir && it.name == "mods" }?.uri
    }

    fun ensure(fs: SafFs): Uri {
        find(fs)?.let { return it }
        return fs.mkdir(fs.rootDoc(), "mods") ?: throw IOException("مقدرتش أعمل فولدر mods")
    }
}

class Installer(private val context: Context) {
    private val cr = context.contentResolver

    suspend fun prepare(src: ModSource, isZip: Boolean, modsTree: Uri?): PreparedMod = withContext(Dispatchers.IO) {
        val metaFile = src.files.first { it.path == META_NAME }
        var title = ""
        var author = ""
        var api = ""
        var readable = true
        try {
            val raw = src.open(metaFile).use { it.readBytes() }
            if (raw.isEmpty()) {
                readable = false
            } else {
                val o = JSONObject(String(raw, Charsets.UTF_8).removePrefix("﻿"))
                title = o.optString("title")
                author = o.optString("author")
                api = o.optString("api_version")
            }
        } catch (e: Exception) {
            readable = false
        }
        val cover = src.files.firstOrNull { it.path == ICON_NAME }
            ?.let { f -> runCatching { src.open(f).use { it.readBytes() } }.getOrNull() }
        var exists = false
        if (modsTree != null) {
            exists = runCatching {
                val fs = SafFs(cr, modsTree)
                val root = ModsLocator.find(fs)
                root != null && fs.children(root).any { it.name == src.suggestedFolder }
            }.getOrDefault(false)
        }
        PreparedMod(
            source = src,
            sourceLabel = src.label,
            isZip = isZip,
            title = title.ifBlank { src.suggestedFolder },
            author = author,
            modApiVersion = api,
            fileCount = src.files.size,
            totalBytes = src.files.sumOf { maxOf(it.size, 0L) },
            folderName = src.suggestedFolder,
            exists = exists,
            coverBytes = cover,
            metaReadable = readable,
        )
    }

    /**
     * Copies every file of the mod into mods/<folder>, then writes the patched _polymod_meta.json LAST.
     * Every file is checked after writing (name and size) and written again once if the check fails, which is
     * what protects against the "files show 0 B" problem. If anything essential still fails the half-written
     * folder is deleted, because a broken _polymod_meta.json makes the game show "Mod Metadata Parse Error".
     */
    suspend fun install(
        p: PreparedMod,
        modsTree: Uri,
        gameVersion: String,
        overwrite: Boolean,
        onProgress: (InstallPhase, Int, Long, String) -> Unit,
    ): InstallResult = withContext(Dispatchers.IO) {
        val fs = SafFs(cr, modsTree)
        onProgress(InstallPhase.PREPARE, 0, 0L, "")
        val modsRoot = ModsLocator.ensure(fs)
        fs.children(modsRoot).firstOrNull { it.name == p.folderName }?.let { old ->
            if (!overwrite) throw IOException("في مود بنفس الاسم.")
            if (!fs.delete(old.uri)) throw IOException("مقدرتش أمسح المود القديم.")
        }
        val modDir = fs.mkdir(modsRoot, p.folderName) ?: throw IOException("مقدرتش أعمل فولدر المود.")
        var finished = false
        try {
            val warnings = ArrayList<String>()
            val dirs = HashMap<String, Uri>().apply { put("", modDir) }
            fun dirFor(rel: String): Uri = dirs.getOrPut(rel) {
                val parent = dirFor(rel.substringBeforeLast('/', ""))
                fs.mkdir(parent, rel.substringAfterLast('/')) ?: throw IOException("مقدرتش أعمل فولدر $rel")
            }

            val files = p.source.files
            val toCopy = files.filter { it.path != META_NAME }
            val totalBytes = toCopy.sumOf { maxOf(it.size, 0L) }
            var bytesDone = 0L
            var lastEmit = 0L
            val buf = ByteArray(64 * 1024)

            for ((index, f) in toCopy.withIndex()) {
                coroutineContext.ensureActive()
                val name = f.path.substringAfterLast('/')
                val parent = dirFor(f.path.substringBeforeLast('/', ""))
                var ok = false
                var written = 0L
                for (attempt in 0..1) {
                    val uri = fs.createFile(parent, name, mimeFor(name)) ?: throw IOException("مقدرتش أعمل الملف $name")
                    written = 0L
                    try {
                        p.source.open(f).use { input ->
                            fs.openOut(uri).use { out ->
                                while (true) {
                                    val n = input.read(buf)
                                    if (n < 0) break
                                    out.write(buf, 0, n)
                                    written += n
                                    val now = System.currentTimeMillis()
                                    if (now - lastEmit > 150) {
                                        lastEmit = now
                                        onProgress(InstallPhase.COPY, index, bytesDone + written, f.path)
                                    }
                                }
                                out.flush()
                            }
                        }
                    } catch (e: IOException) {
                        fs.delete(uri)
                        continue
                    }
                    val st = fs.stat(uri)
                    val expected = if (f.size >= 0) f.size else written
                    if (st != null && st.name == name && st.size == expected) {
                        ok = true
                        break
                    }
                    fs.delete(uri)
                }
                bytesDone += maxOf(written, 0L)
                if (!ok) {
                    if (isOptional(name)) warnings += "اتخطّيت الملف ${f.path}" else throw IOException("الملف ${f.path} ما اتكتبش سليم.")
                }
                onProgress(InstallPhase.COPY, index + 1, bytesDone, f.path)
            }

            // --- patch _polymod_meta.json (written last)
            onProgress(InstallPhase.PATCH, toCopy.size, totalBytes, META_NAME)
            val metaFile = files.first { it.path == META_NAME }
            val raw = p.source.open(metaFile).use { it.readBytes() }
            val obj = JSONObject(String(raw, Charsets.UTF_8).removePrefix("﻿"))
            val oldApi = obj.optString("api_version")
            val newApi = gameVersion.ifBlank { oldApi }
            if (newApi != oldApi) obj.put("api_version", newApi)
            val bytes = obj.toString(2).replace("\\/", "/").toByteArray(Charsets.UTF_8)
            writeVerified(fs, modDir, META_NAME, bytes)

            // --- final check
            onProgress(InstallPhase.VERIFY, files.size, totalBytes, "")
            val top = fs.children(modDir)
            val meta = top.firstOrNull { it.name == META_NAME }
            if (meta == null || meta.size == 0L) throw IOException("$META_NAME طلع فاضي بعد الكتابة.")

            p.coverBytes?.let { b ->
                val dir = File(context.filesDir, "covers").apply { mkdirs() }
                File(dir, "${p.folderName}.png").writeBytes(b)
            }
            finished = true
            InstallResult(p.folderName, p.title, files.size - warnings.size, totalBytes, oldApi, newApi, warnings)
        } finally {
            if (!finished) withContext(NonCancellable) { fs.delete(modDir) }
        }
    }

    /** Rewrites api_version inside an already installed mod. */
    suspend fun repatch(modsTree: Uri, metaDirUri: Uri, version: String): Unit = withContext(Dispatchers.IO) {
        val fs = SafFs(cr, modsTree)
        val meta = fs.children(metaDirUri).firstOrNull { it.name == META_NAME } ?: throw IOException("مفيش $META_NAME")
        val obj = JSONObject(String(fs.readBytes(meta.uri), Charsets.UTF_8).removePrefix("﻿"))
        obj.put("api_version", version)
        val bytes = obj.toString(2).replace("\\/", "/").toByteArray(Charsets.UTF_8)
        fs.openOut(meta.uri, "wt").use { it.write(bytes); it.flush() }
        val st = fs.stat(meta.uri)
        if (st == null || st.size != bytes.size.toLong()) throw IOException("الكتابة ما اتأكدتش.")
    }

    private fun writeVerified(fs: SafFs, parent: Uri, name: String, bytes: ByteArray) {
        for (attempt in 0..1) {
            val uri = fs.createFile(parent, name, mimeFor(name)) ?: throw IOException("مقدرتش أعمل $name")
            fs.openOut(uri).use { it.write(bytes); it.flush() }
            val st = fs.stat(uri)
            if (st != null && st.name == name && st.size == bytes.size.toLong()) return
            fs.delete(uri)
        }
        throw IOException("$name ما اتكتبش سليم.")
    }

    private fun isOptional(name: String) =
        name.endsWith(".md", true) || name.endsWith(".txt", true) || name.endsWith(".url", true)

    /** The MIME type must match the extension, otherwise some providers append a second extension. */
    private fun mimeFor(name: String): String {
        val ext = name.substringAfterLast('.', "").lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "application/octet-stream"
    }
}
