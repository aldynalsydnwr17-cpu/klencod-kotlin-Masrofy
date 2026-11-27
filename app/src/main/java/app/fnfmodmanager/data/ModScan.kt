package app.fnfmodmanager.data

import org.json.JSONObject

/** A file of the mod that would make FNF crash on start: it replaces a base-game character with a different render type. */
data class ModConflict(val path: String, val id: String, val expected: String, val got: String)

/**
 * Pre-install check. Version 1 knows the character render types that the game's own scripts hard-require
 * (taken from real "Render type mismatch" crashes on FNF Mobile 0.8.8). Extend BASE_RENDER_TYPES when a new one shows up.
 */
object ModScan {
    private val BASE_RENDER_TYPES = mapOf(
        "bf" to "multianimateatlas",
        "bf-christmas" to "multisparrow",
    )

    fun conflicts(src: ModSource): List<ModConflict> {
        val out = ArrayList<ModConflict>()
        for (f in src.files) {
            val p = f.path
            if (!p.startsWith("data/characters/") || !p.endsWith(".json")) continue
            val id = p.removePrefix("data/characters/").removeSuffix(".json")
            val expected = BASE_RENDER_TYPES[id] ?: continue
            val got = runCatching {
                val raw = src.open(f).use { it.readBytes() }
                JSONObject(String(raw, Charsets.UTF_8).removePrefix("﻿")).optString("renderType")
            }.getOrNull() ?: continue
            if (!got.equals(expected, ignoreCase = true)) out += ModConflict(p, id, expected, got)
        }
        return out
    }
}
