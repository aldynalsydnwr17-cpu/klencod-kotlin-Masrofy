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
    private val manifests = ManifestStore(context)

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
        var installedNames: Set<String> = emptySet()
        if (modsTree != null) {
            runCatching {
                val fs = SafFs(cr, modsTree)
                val root = ModsLocator.find(fs)
                if (root != null) {
                    installedNames = fs.children(root).filter { it.isDir }.map { it.name }.toSet()
                    exists = src.suggestedFolder in installedNames
                }
            }
        }
        val others = runCatching { manifests.all().associate { m -> m.folder to m.files.map { it.path }.toSet() } }.getOrDefault(emptyMap())
        val findings = runCatching { ModScan.analyze(context, src, installedNames, others, src.suggestedFolder) }.getOrDefault(emptyList())
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
            findings = findings,
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
        mode: ConflictMode,
        onProgress: (InstallPhase, Int, Long, String) -> Unit,
    ): InstallResult = withContext(Dispatchers.IO) {
        val fs = SafFs(cr, modsTree)
        onProgress(InstallPhase.PREPARE, 0, 0L, "")
        val modsRoot = ModsLocator.ensure(fs)
        val existing = fs.children(modsRoot)
        val folder = if (mode == ConflictMode.RENAME) {
            var n = 2
            while (existing.any { it.name == "${p.folderName}_$n" }) n++
            "${p.folderName}_$n"
        } else p.folderName
        existing.firstOrNull { it.name == folder }?.let { old ->
            // Never overwrite silently: the caller must have asked the user and passed REPLACE.
            if (mode != ConflictMode.REPLACE) throw IOException("في مود بنفس الاسم، ولم يتم اختيار الاستبدال.")
            backup(fs, old, folder)
            if (!fs.delete(old.uri)) throw IOException("تعذّر حذف المود القديم بعد أخذ النسخة الاحتياطية.")
        }
        val modDir = fs.mkdir(modsRoot, folder) ?: throw IOException("تعذّر إنشاء مجلد المود.")
        val entries = ArrayList<ManifestStore.Entry>()
        var finished = false
        try {
            val warnings = ArrayList<String>()
            val dirs = HashMap<String, Uri>().apply { put("", modDir) }
            fun dirFor(rel: String): Uri = dirs.getOrPut(rel) {
                val parent = dirFor(rel.substringBeforeLast('/', ""))
                fs.mkdir(parent, rel.substringAfterLast('/')) ?: throw IOException("مقدرتش أعمل فولدر $rel")
            }

            val files = p.source.files
            val skipPaths = p.findings.flatMap { it.skipPaths }.toSet()
            val toCopy = files.filter { it.path != META_NAME && it.path !in skipPaths }
            p.findings.filter { it.skipPaths.isNotEmpty() }.forEach { warnings += it.text }
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
                        entries += ManifestStore.Entry(f.path, expected)
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

            // --- files the mod doctor adds (empty scripts that switch off a base-game character script)
            for (path in p.findings.flatMap { it.inject }) {
                val name = path.substringAfterLast('/')
                writeVerified(fs, dirFor(path.substringBeforeLast('/', "")), name, ByteArray(0))
                entries += ManifestStore.Entry(path, 0L)
            }
            p.findings.filter { it.inject.isNotEmpty() }.forEach { warnings += "🔧 " + it.text }

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
            entries += ManifestStore.Entry(META_NAME, bytes.size.toLong())

            // --- final check
            onProgress(InstallPhase.VERIFY, files.size, totalBytes, "")
            val top = fs.children(modDir)
            val meta = top.firstOrNull { it.name == META_NAME }
            if (meta == null || meta.size == 0L) throw IOException("$META_NAME طلع فاضي بعد الكتابة.")

            p.coverBytes?.let { b ->
                val dir = File(context.filesDir, "covers").apply { mkdirs() }
                File(dir, "$folder.png").writeBytes(b)
            }
            manifests.save(ManifestStore.Manifest(folder, p.title, System.currentTimeMillis(), entries))
            finished = true
            InstallResult(folder, p.title, toCopy.size + 1 - warnings.count { it.startsWith("اتخطّيت الملف") }, totalBytes, oldApi, newApi, warnings)
        } finally {
            if (!finished) withContext(NonCancellable) { fs.delete(modDir) }
        }
    }

    /** Copies the folder that is about to be replaced into the app's private storage (latest backup per mod). */
    private fun backup(fs: SafFs, old: SafFs.Child, folder: String) {
        val target = File(context.filesDir, "backups/" + java.net.URLEncoder.encode(folder, "UTF-8"))
        target.deleteRecursively()
        fun copy(dir: Uri, to: File) {
            to.mkdirs()
            for (c in fs.children(dir)) {
                if (c.isDir) copy(c.uri, File(to, c.name))
                else fs.openIn(c.uri).use { input -> File(to, c.name).outputStream().use { input.copyTo(it) } }
            }
        }
        try { copy(old.uri, target) } catch (e: Exception) {
            target.deleteRecursively()
            throw IOException("تعذّر أخذ نسخة احتياطية من المود القديم، لذلك لم يُستبدل.")
        }
    }

    private fun walk(fs: SafFs, root: Uri, rel: String): Uri? {
        var cur = root
        for (part in rel.split('/').filter { it.isNotEmpty() }) {
            cur = fs.children(cur).firstOrNull { it.isDir && it.name == part }?.uri ?: return null
        }
        return cur
    }

    /**
     * Removes ONLY the files recorded in this app's manifest for the mod, then folders that became empty.
     * A mod the app did not install has no manifest and is refused: nothing is deleted.
     */
    suspend fun uninstall(tree: Uri, mod: ModInfo): UninstallResult = withContext(Dispatchers.IO) {
        val m = manifests.load(mod.folderName) ?: throw NotManagedException()
        val fs = SafFs(cr, tree)
        var deleted = 0
        val dirs = LinkedHashSet<String>()
        val listCache = HashMap<String, List<SafFs.Child>>()
        for (e in m.files) {
            val dirRel = e.path.substringBeforeLast('/', "")
            val dirUri = walk(fs, mod.docUri, dirRel) ?: continue
            val kids = listCache.getOrPut(dirRel) { fs.children(dirUri) }
            val f = kids.firstOrNull { !it.isDir && it.name == e.path.substringAfterLast('/') } ?: continue
            if (fs.delete(f.uri)) deleted++
            var d = dirRel
            while (d.isNotEmpty()) { dirs += d; d = d.substringBeforeLast('/', "") }
        }
        for (d in dirs.sortedByDescending { dd -> dd.count { it == '/' } }) {
            val u = walk(fs, mod.docUri, d) ?: continue
            if (fs.children(u).isEmpty()) fs.delete(u)
        }
        val left = fs.children(mod.docUri).size
        var removed = false
        if (left == 0 && fs.delete(mod.docUri)) {
            removed = true
            manifests.delete(mod.folderName)
        }
        UninstallResult(deleted, left, removed)
    }

    /** Checks the installed files against the manifest (exists, not 0 B, same size) and the metadata file. */
    suspend fun verify(tree: Uri, mod: ModInfo, gameVersion: String): VerifyReport = withContext(Dispatchers.IO) {
        val fs = SafFs(cr, tree)
        val issues = ArrayList<String>()
        val notes = ArrayList<String>()
        val m = manifests.load(mod.folderName)
        var checked = 0
        if (m != null) {
            val listCache = HashMap<String, List<SafFs.Child>>()
            for (e in m.files) {
                val dirRel = e.path.substringBeforeLast('/', "")
                val dirUri = walk(fs, mod.docUri, dirRel)
                val kids = dirUri?.let { u -> listCache.getOrPut(dirRel) { fs.children(u) } }
                val f = kids?.firstOrNull { !it.isDir && it.name == e.path.substringAfterLast('/') }
                checked++
                when {
                    f == null -> issues += "ملف ناقص: ${e.path}"
                    f.size == 0L && e.size > 0L -> issues += "ملف فارغ (0 B): ${e.path}"
                    f.size >= 0 && f.size != e.size -> issues += "حجم مختلف عن وقت التثبيت: ${e.path}"
                }
            }
        } else {
            notes += "هذا المود لم يُثبَّت بواسطة التطبيق، لذلك الفحص يشمل ملف البيانات فقط."
            val kids = fs.children(mod.docUri)
            checked = kids.size
            kids.filter { !it.isDir && it.size == 0L }.forEach { issues += "ملف فارغ (0 B): ${it.name}" }
        }
        when (mod.status) {
            ModStatus.ERROR -> issues += mod.statusDetail ?: "ملف _polymod_meta.json غير سليم."
            ModStatus.INCOMPATIBLE -> notes += mod.statusDetail ?: "api_version لا يطابق إصدار اللعبة ($gameVersion)."
            else -> {}
        }
        if (issues.isEmpty()) notes += "الملفات سليمة. هذا لا يضمن أن المود يعمل في إصدارك، لكنه يؤكد أن لا شيء ناقص أو فارغ."
        VerifyReport(mod.title, m != null, checked, issues, notes)
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
