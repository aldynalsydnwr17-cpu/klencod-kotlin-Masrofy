package app.fnfmodmanager.data

import android.content.Context
import org.json.JSONObject

enum class Severity { CRASH, WARN }

/** One thing the pre-install check found. [skipPaths] are files the installer leaves out so the game does not crash. */
data class Finding(val severity: Severity, val text: String, val skipPaths: List<String> = emptyList())

/**
 * Pre-install check ("mod doctor"). Rules:
 *  1. character JSON that is broken, or has an unknown renderType            -> CRASH, file skipped
 *  2. character JSON replacing a base character with another renderType      -> CRASH, file skipped
 *     (base renderTypes are read from the installed game; small built-in table as a fallback)
 *  3. other data/*.json that is empty or not valid JSON                      -> WARN
 *  4. "dependencies" in _polymod_meta.json naming a mod that is not installed-> WARN
 *  5. Lua scripts (Psych Engine), which V-Slice does not run                 -> WARN
 *  6. files that another installed mod already provides                      -> WARN
 */
object ModScan {
    private val FALLBACK_BASE = mapOf("bf" to "multianimateatlas", "bf-christmas" to "multisparrow")
    private val KNOWN_TYPES = setOf("sparrow", "multisparrow", "animateatlas", "multianimateatlas", "packer", "custom")
    private val CHAR = Regex("^data/characters/([^/]+)\\.json$")

    private fun parse(src: ModSource, f: SrcFile): JSONObject? = runCatching {
        val raw = src.open(f).use { it.readBytes() }
        JSONObject(String(raw, Charsets.UTF_8).removePrefix("﻿"))
    }.getOrNull()

    fun analyze(
        context: Context,
        src: ModSource,
        installedFolders: Set<String>,
        others: Map<String, Set<String>>,
        ownFolder: String,
    ): List<Finding> {
        val out = ArrayList<Finding>()
        val base = BaseGame.characterRenderTypes(context).ifEmpty { FALLBACK_BASE }

        // 1 + 2: characters
        for (f in src.files) {
            val m = CHAR.find(f.path) ?: continue
            val id = m.groupValues[1]
            val o = parse(src, f)
            if (o == null) {
                out += Finding(Severity.CRASH, "ملف الشخصية $id.json بايظ أو فاضي، فاتخطّيته عشان اللعبة متقفلش.", listOf(f.path))
                continue
            }
            val got = o.optString("renderType")
            if (got.isNotBlank() && got.lowercase() !in KNOWN_TYPES) {
                out += Finding(Severity.CRASH, "الشخصية $id نوع رسمها ($got) مش معروف للعبة، فاتخطّيت ملفها.", listOf(f.path))
                continue
            }
            val expected = base[id]
            if (expected != null && got.isNotBlank() && !got.equals(expected, ignoreCase = true)) {
                out += Finding(
                    Severity.CRASH,
                    "المود بيستبدل الشخصية الأساسية $id بنوع رسم ($got) واللعبة محتاجة ($expected)، وده بيعمل Crash. اتخطّيت ملفها وباقي المود هيتثبّت، يعني السكن مش هيظهر على الشخصية دي.",
                    listOf(f.path),
                )
            }
        }

        // 3: other data json
        var checked = 0
        for (f in src.files) {
            if (checked >= 300) break
            if (!f.path.startsWith("data/") || !f.path.endsWith(".json") || CHAR.matches(f.path)) continue
            if (f.size > 3_000_000) continue
            checked++
            if (f.size == 0L || parse(src, f) == null) out += Finding(Severity.WARN, "الملف ${f.path} فاضي أو مش JSON سليم، ممكن اللعبة تقف لما تحمّله.")
        }

        // 4: dependencies
        src.files.firstOrNull { it.path == META_NAME }?.let { meta ->
            val deps = parse(src, meta)?.optJSONObject("dependencies")
            deps?.keys()?.forEach { dep ->
                if (dep !in installedFolders) out += Finding(Severity.WARN, "المود محتاج مود تاني اسمه «$dep» وهو مش متثبّت. ثبّته الأول وإلا اللعبة هتديك خطأ تبعية.")
            }
        }

        // 5: lua
        if (src.files.any { it.path.endsWith(".lua", ignoreCase = true) }) {
            out += Finding(Severity.WARN, "فيه سكربتات Lua (Psych Engine). لعبة V-Slice مبتشغّلهاش، فجزء من المود مش هيشتغل.")
        }

        // 6: overlap with other installed mods
        val mine = src.files.map { it.path }.filter { it != META_NAME && it != ICON_NAME }.toSet()
        for ((folder, paths) in others) {
            if (folder == ownFolder) continue
            val shared = mine.intersect(paths)
            if (shared.isNotEmpty()) {
                out += Finding(Severity.WARN, "هيتعارض مع المود «$folder» في ${shared.size} ملف (مثلاً ${shared.first()}). اللي بيتحمّل آخر هو اللي بيكسب.")
            }
        }
        return out
    }
}
