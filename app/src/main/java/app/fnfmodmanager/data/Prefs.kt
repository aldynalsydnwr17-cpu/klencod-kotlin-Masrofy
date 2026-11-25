package app.fnfmodmanager.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Tiny SharedPreferences wrapper. Nothing here is secret. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("fnf_mod_manager", Context.MODE_PRIVATE)

    var gameVersion: String
        get() = sp.getString("game_version", DEFAULT_VERSION) ?: DEFAULT_VERSION
        set(v) = sp.edit().putString("game_version", v).apply()

    var modsTree: String?
        get() = sp.getString("mods_tree", null)
        set(v) = sp.edit().putString("mods_tree", v).apply()

    var amoled: Boolean
        get() = sp.getBoolean("amoled", false)
        set(v) = sp.edit().putBoolean("amoled", v).apply()

    var installedByApp: Set<String>
        get() = HashSet(sp.getStringSet("installed", emptySet()) ?: emptySet())
        set(v) = sp.edit().putStringSet("installed", HashSet(v)).apply()

    var favorites: Set<String>
        get() = HashSet(sp.getStringSet("favorites", emptySet()) ?: emptySet())
        set(v) = sp.edit().putStringSet("favorites", HashSet(v)).apply()

    var activity: List<ActivityItem>
        get() {
            val raw = sp.getString("activity", null) ?: return emptyList()
            return runCatching {
                val arr = JSONArray(raw)
                (0 until arr.length()).map {
                    val o = arr.getJSONObject(it)
                    ActivityItem(o.getLong("t"), o.getString("x"), o.getBoolean("ok"))
                }
            }.getOrDefault(emptyList())
        }
        set(v) {
            val arr = JSONArray()
            v.take(30).forEach { arr.put(JSONObject().put("t", it.time).put("x", it.text).put("ok", it.ok)) }
            sp.edit().putString("activity", arr.toString()).apply()
        }

    companion object {
        const val DEFAULT_VERSION = "0.8.8"
        const val GAME_PACKAGE = "me.funkin.fnf"
    }
}
