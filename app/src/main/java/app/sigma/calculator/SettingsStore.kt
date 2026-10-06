package app.sigma.calculator

import android.content.Context
import app.sigma.calculator.engine.AngleMode
import org.json.JSONArray
import org.json.JSONObject

/** One past calculation, shown in the History panel. */
data class HistoryEntry(val expression: String, val result: String, val angleMode: AngleMode)

/** Follow the phone's setting, or always light, or always dark. */
enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Saves history and settings on the phone, so they survive closing the app.
 * Uses SharedPreferences: a small key–value store built into Android.
 */
class SettingsStore(context: Context) {

    private val prefs = context.getSharedPreferences("sigma", Context.MODE_PRIVATE)

    var angleMode: AngleMode
        get() = enumOrDefault(prefs.getString("angle", null), AngleMode.DEG)
        set(value) = prefs.edit().putString("angle", value.name).apply()

    var themeMode: ThemeMode
        get() = enumOrDefault(prefs.getString("theme", null), ThemeMode.SYSTEM)
        set(value) = prefs.edit().putString("theme", value.name).apply()

    /** The last answer, for the Ans key. Stored as raw bits so no precision is lost. */
    var ans: Double
        get() = Double.fromBits(prefs.getLong("ans", 0.0.toRawBits()))
        set(value) = prefs.edit().putLong("ans", value.toRawBits()).apply()

    /** True only the very first time the app opens. */
    fun consumeFirstRun(): Boolean {
        val first = prefs.getBoolean("firstRun", true)
        if (first) prefs.edit().putBoolean("firstRun", false).apply()
        return first
    }

    fun loadHistory(): List<HistoryEntry> = try {
        val array = JSONArray(prefs.getString("history", "[]"))
        (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            HistoryEntry(
                expression = item.getString("e"),
                result = item.getString("r"),
                angleMode = enumOrDefault(item.optString("m"), AngleMode.DEG),
            )
        }
    } catch (e: Exception) {
        emptyList() // damaged data: start with an empty history instead of crashing
    }

    fun saveHistory(entries: List<HistoryEntry>) {
        val array = JSONArray()
        entries.forEach {
            array.put(JSONObject().put("e", it.expression).put("r", it.result).put("m", it.angleMode.name))
        }
        prefs.edit().putString("history", array.toString()).apply()
    }

    private inline fun <reified T : Enum<T>> enumOrDefault(name: String?, default: T): T =
        enumValues<T>().firstOrNull { it.name == name } ?: default
}
