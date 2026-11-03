package com.jawzf.pixeltoolkit.core.data.backup

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Exports and imports all preference files as the backup JSON. */
class ConfigBackup(
    private val context: Context,
) {
    private val gson = Gson()

    // Config Export/Import
    @Suppress("CyclomaticComplexMethod", "NestedBlockDepth", "ComplexCondition") // moved unchanged in tk24; simplify later
    fun getAllConfigsAsJsonString(): String {
        return try {
            val allConfigs = mutableMapOf<String, Map<String, Map<String, Any>>>()
            val prefFiles =
                listOf(
                    "toolkit_prefs",
                    "caffeinate_prefs",
                    "link_prefs",
                    "diy_automations_prefs",
                    "live_wallpaper_prefs",
                )

            prefFiles.forEach { fileName ->
                val p = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
                val wrapperMap = mutableMapOf<String, Map<String, Any>>()

                p.all.forEach { (key, value) ->
                    if (key == KEY_GITHUB_ACCESS_TOKEN ||
                        key == KEY_SHIZUKU_AUTH_TOKEN ||
                        key.startsWith(LEGACY_WEATHER_API_KEY_PREFIX) ||
                        key == KEY_SNOOZE_DISCOVERED_CHANNELS ||
                        key == KEY_MAPS_DISCOVERED_CHANNELS ||
                        key == KEY_SHUT_UP_ORIGINAL_SETTINGS ||
                        key == "battery_history_points"
                    ) {
                        return@forEach
                    }

                    val type =
                        when (value) {
                            is Boolean -> "Boolean"
                            is Int -> "Int"
                            is Long -> "Long"
                            is Float -> "Float"
                            is String -> "String"
                            is Set<*> -> "StringSet"
                            else -> "Unknown"
                        }
                    if (value != null && type != "Unknown") {
                        wrapperMap[key] = mapOf("type" to type, "value" to value)
                    }
                }
                allConfigs[fileName] = wrapperMap
            }

            gson.toJson(allConfigs)
        } catch (e: Exception) {
            "{}"
        }
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
    @Suppress("LongMethod", "CyclomaticComplexMethod", "NestedBlockDepth") // moved unchanged in tk24; simplify later
    fun importConfigs(
        inputStream: java.io.InputStream,
        keepPrefs: Boolean,
    ): Boolean {
        return try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val type = object : TypeToken<Map<String, Map<String, Map<String, Any>>>>() {}.type
            val allConfigs: Map<String, Map<String, Map<String, Any>>> =
                gson.fromJson(json, type) ?: emptyMap()

            allConfigs.forEach { (fileName, prefWrapper) ->
                val p = context.getSharedPreferences(fileName, Context.MODE_PRIVATE)

                // Preserve sensitive or volatile local state not present in backups
                val preservedValues = mutableMapOf<String, Any?>()
                val keysToPreserve =
                    listOf(
                        KEY_GITHUB_ACCESS_TOKEN,
                        KEY_SHIZUKU_AUTH_TOKEN,
                        LEGACY_WEATHER_API_KEY_PREFIX,
                        KEY_SNOOZE_DISCOVERED_CHANNELS,
                        KEY_MAPS_DISCOVERED_CHANNELS,
                        KEY_SHUT_UP_ORIGINAL_SETTINGS,
                    )
                keysToPreserve.forEach { key ->
                    if (p.contains(key)) {
                        preservedValues[key] = p.all[key]
                    }
                }

                p
                    .edit()
                    .apply {
                        if (!keepPrefs) clear()

                        // Restore preserved values
                        preservedValues.forEach { (key, value) ->
                            if (value != null) {
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
                        }

                        prefWrapper.forEach { (key, item) ->
                            // Do not import sensitive keys from the file even if they exist there
                            if (key == KEY_GITHUB_ACCESS_TOKEN ||
                                key == KEY_SHIZUKU_AUTH_TOKEN ||
                                key.startsWith(LEGACY_WEATHER_API_KEY_PREFIX)
                            ) {
                                return@forEach
                            }
                            val itemType = item["type"] as? String
                            val itemValue = item["value"]

                            if (itemType != null && itemValue != null) {
                                try {
                                    when (itemType) {
                                        "Boolean" -> putBoolean(key, itemValue as Boolean)
                                        "Int" -> putInt(key, (itemValue as Double).toInt())
                                        "Long" -> putLong(key, (itemValue as Double).toLong())
                                        "Float" -> putFloat(key, (itemValue as Double).toFloat())
                                        "String" -> putString(key, itemValue as String)
                                        "StringSet" -> {
                                            @Suppress("UNCHECKED_CAST")
                                            putStringSet(key, (itemValue as List<String>).toSet())
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }.apply()
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
    }

    companion object {
        const val KEY_GITHUB_ACCESS_TOKEN = "github_access_token"
        const val KEY_SHIZUKU_AUTH_TOKEN = "shizuku_auth_token"
        const val KEY_SNOOZE_DISCOVERED_CHANNELS = "snooze_discovered_channels"
        const val KEY_MAPS_DISCOVERED_CHANNELS = "maps_discovered_channels"
        const val KEY_SHUT_UP_ORIGINAL_SETTINGS = "shut_up_original_settings"
        const val LEGACY_WEATHER_API_KEY_PREFIX = "weather_api_key"
    }
}
