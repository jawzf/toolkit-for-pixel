package com.jawzf.pixeltoolkit.core.data.backup

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Exports and imports all preference files as the backup JSON. */
class ConfigBackup(
    private val context: Context,
) {
    private val gson = Gson()

    /** Serializes all tracked preference files into a single JSON backup string. */
    fun getAllConfigsAsJsonString(): String =
        try {
            val allConfigs =
                PREF_FILES.associateWith { fileName ->
                    exportPrefFile(fileName)
                }
            gson.toJson(allConfigs)
        } catch (e: Exception) {
            "{}"
        }

    /**
     * Executes the export configs operation.
     *
     * @param outputStream [java.io.OutputStream] Target output stream.
     */
    fun exportConfigs(outputStream: java.io.OutputStream) {
        try {
            val json = getAllConfigsAsJsonString()
            outputStream.write(json.toByteArray())
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the import configs operation.
     *
     * @param inputStream [java.io.InputStream] Target input stream.
     * @param keepPrefs [Boolean] Target keep prefs.
     * @return The resulting Boolean data.
     */
    fun importConfigs(
        inputStream: java.io.InputStream,
        keepPrefs: Boolean,
    ): Boolean =
        try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, Map<String, Map<String, Any>>>>() {}.type
            val allConfigs: Map<String, Map<String, Map<String, Any>>> =
                gson.fromJson(json, type) ?: emptyMap()

            allConfigs.forEach { (fileName, prefWrapper) ->
                importPrefFile(fileName, prefWrapper, keepPrefs)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                inputStream.close()
            } catch (ignored: Exception) {
            }
        }

    /** Dumps all preferences from one file into a JSON-compatible map. */
    private fun exportPrefFile(fileName: String): Map<String, Map<String, Any>> {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val entries = mutableMapOf<String, Map<String, Any>>()
        for ((key, value) in prefs.all) {
            if (isSkippedOnExport(key)) continue
            val type = typeName(value) ?: continue
            if (value != null) {
                entries[key] = mapOf("type" to type, "value" to value)
            }
        }
        return entries
    }

    /** Determines whether a preference key should be omitted during export. */
    private fun isSkippedOnExport(key: String): Boolean = key in EXPORT_SKIPPED_KEYS || key.startsWith(LEGACY_WEATHER_API_KEY_PREFIX)

    /** Maps a preference value to its serialized type name, or null if unsupported. */
    private fun typeName(value: Any?): String? =
        when (value) {
            is Boolean -> "Boolean"
            is Int -> "Int"
            is Long -> "Long"
            is Float -> "Float"
            is String -> "String"
            is Set<*> -> "StringSet"
            else -> null
        }

    /** Imports backup entries into one preferences file, preserving sensitive keys. */
    private fun importPrefFile(
        fileName: String,
        prefWrapper: Map<String, Map<String, Any>>,
        keepPrefs: Boolean,
    ) {
        val prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        val preserved =
            PRESERVED_KEYS.mapNotNull { key ->
                if (prefs.contains(key)) key to prefs.all[key] else null
            }

        prefs.edit {
            if (!keepPrefs) clear()
            for ((key, value) in preserved) {
                if (value != null) putValue(key, value)
            }
            for ((key, item) in prefWrapper) {
                importItem(key, item)
            }
        }
    }

    /** Inserts a JSON wrapper entry into the editor if not skipped. */
    private fun SharedPreferences.Editor.importItem(
        key: String,
        item: Map<String, Any>,
    ) {
        if (isSkippedOnImport(key)) return
        val type = item["type"] as? String ?: return
        val value = item["value"] ?: return
        putItem(key, type, value)
    }

    /** Determines whether a key from the backup file should be skipped during import. */
    private fun isSkippedOnImport(key: String): Boolean =
        key == KEY_GITHUB_ACCESS_TOKEN ||
            key == KEY_SHIZUKU_AUTH_TOKEN ||
            key.startsWith(LEGACY_WEATHER_API_KEY_PREFIX)

    /** Restores a preserved preference value back into the editor. */
    private fun SharedPreferences.Editor.putValue(
        key: String,
        value: Any,
    ) {
        when (value) {
            is Boolean -> putBoolean(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is String -> putString(key, value)
            is Set<*> -> {
                @Suppress("UNCHECKED_CAST")
                putStringSet(key, value as Set<String>)
            }
        }
    }

    /** Inserts a typed preference item from the backup JSON into the editor. */
    private fun SharedPreferences.Editor.putItem(
        key: String,
        type: String,
        value: Any,
    ) {
        try {
            when (type) {
                "Boolean" -> putBoolean(key, value as Boolean)
                "Int" -> putInt(key, (value as Double).toInt())
                "Long" -> putLong(key, (value as Double).toLong())
                "Float" -> putFloat(key, (value as Double).toFloat())
                "String" -> putString(key, value as String)
                "StringSet" -> {
                    @Suppress("UNCHECKED_CAST")
                    putStringSet(key, (value as List<String>).toSet())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val KEY_GITHUB_ACCESS_TOKEN = "github_access_token"
        const val KEY_SHIZUKU_AUTH_TOKEN = "shizuku_auth_token"
        const val KEY_SNOOZE_DISCOVERED_CHANNELS = "snooze_discovered_channels"
        const val KEY_MAPS_DISCOVERED_CHANNELS = "maps_discovered_channels"
        const val KEY_SHUT_UP_ORIGINAL_SETTINGS = "shut_up_original_settings"
        const val LEGACY_WEATHER_API_KEY_PREFIX = "weather_api_key"

        private val PREF_FILES =
            listOf(
                "toolkit_prefs",
                "caffeinate_prefs",
                "link_prefs",
                "diy_automations_prefs",
                "live_wallpaper_prefs",
            )

        private val EXPORT_SKIPPED_KEYS =
            setOf(
                KEY_GITHUB_ACCESS_TOKEN,
                KEY_SHIZUKU_AUTH_TOKEN,
                KEY_SNOOZE_DISCOVERED_CHANNELS,
                KEY_MAPS_DISCOVERED_CHANNELS,
                KEY_SHUT_UP_ORIGINAL_SETTINGS,
                "battery_history_points",
            )

        private val PRESERVED_KEYS =
            setOf(
                KEY_GITHUB_ACCESS_TOKEN,
                KEY_SHIZUKU_AUTH_TOKEN,
                LEGACY_WEATHER_API_KEY_PREFIX,
                KEY_SNOOZE_DISCOVERED_CHANNELS,
                KEY_MAPS_DISCOVERED_CHANNELS,
                KEY_SHUT_UP_ORIGINAL_SETTINGS,
            )
    }
}
