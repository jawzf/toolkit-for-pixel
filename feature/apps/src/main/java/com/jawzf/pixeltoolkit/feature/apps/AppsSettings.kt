package com.jawzf.pixeltoolkit.feature.apps

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.apps.freeze.AppTag

/** App settings (freeze, and later app lock, updates and conscious gate) on the main preference file. Keys are unchanged from SettingsRepository. */
class AppsSettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    /**
     * Executes the get freeze auto excluded apps operation.
     * @return The resulting Set<String> data.
     */
    fun getFreezeAutoExcludedApps(): Set<String> {
        val json = prefs.getString(KEY_FREEZE_AUTO_EXCLUDED_APPS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
    }

    /**
     * Executes the save freeze auto excluded apps operation.
     *
     * @param apps [Set<String>] Target apps.
     */
    fun saveFreezeAutoExcludedApps(apps: Set<String>) {
        val json = gson.toJson(apps)
        putString(KEY_FREEZE_AUTO_EXCLUDED_APPS, json)
    }

    /**
     * Executes the get freeze tags operation.
     * @return The resulting List<AppTag> data.
     */
    fun getFreezeTags(): List<AppTag> {
        val json = prefs.getString(KEY_FREEZE_TAGS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<AppTag>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Executes the save freeze tags operation.
     *
     * @param tags [List<AppTag>] Target tags.
     */
    fun saveFreezeTags(tags: List<AppTag>) {
        val json = gson.toJson(tags)
        putString(KEY_FREEZE_TAGS, json)
    }

    /**
     * Executes the get freeze app tag map operation.
     * @return The resulting Map<String, List<String>> data.
     */
    fun getFreezeAppTagMap(): Map<String, List<String>> {
        val json = prefs.getString(KEY_FREEZE_APP_TAG_MAP, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, List<String>>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * Executes the save freeze app tag map operation.
     *
     * @param map [Map<String] Target map.
     * @param List<String>> Target list string.
     */
    fun saveFreezeAppTagMap(map: Map<String, List<String>>) {
        val json = gson.toJson(map)
        putString(KEY_FREEZE_APP_TAG_MAP, json)
    }

    /**
     * Executes the get freeze mode operation.
     * @return The resulting Int data.
     */
    fun getFreezeMode(): Int = getInt(KEY_FREEZE_MODE, 0)

    fun loadFreezeSelectedApps(): List<AppSelection> = loadAppSelection(KEY_FREEZE_SELECTED_APPS)

    /** Only enabled apps are stored. */
    fun saveFreezeSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_FREEZE_SELECTED_APPS, apps.filter { it.isEnabled })

    fun updateFreezeAppSelection(
        packageName: String,
        enabled: Boolean,
    ) {
        val current = loadFreezeSelectedApps().toMutableList()
        val index = current.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            current[index] = current[index].copy(isEnabled = enabled)
        } else {
            current.add(AppSelection(packageName, enabled))
        }
        saveFreezeSelectedApps(current)
    }

    fun loadAppLockSelectedApps(): List<AppSelection> = loadAppSelection(KEY_APP_LOCK_SELECTED_APPS)

    fun saveAppLockSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_APP_LOCK_SELECTED_APPS, apps)

    fun updateAppLockAppSelection(
        packageName: String,
        enabled: Boolean,
    ) {
        val current = loadAppLockSelectedApps().toMutableList()
        val index = current.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            current[index] = current[index].copy(isEnabled = enabled)
        } else {
            current.add(AppSelection(packageName, enabled))
        }
        saveAppLockSelectedApps(current)
    }

    companion object {
        const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        const val KEY_APP_LOCK_SELECTED_APPS = "app_lock_selected_apps"
        const val KEY_APP_LOCK_AUTO_LOCK_DELAY_INDEX = "app_lock_auto_lock_delay_index"

        const val KEY_FREEZE_SELECTED_APPS = "freeze_selected_apps"
        const val KEY_FREEZE_WHEN_LOCKED_ENABLED = "freeze_when_locked_enabled"
        const val KEY_FREEZE_LOCK_DELAY_INDEX = "freeze_lock_delay_index"
        const val KEY_FREEZE_AUTO_EXCLUDED_APPS = "freeze_auto_excluded_apps"
        const val KEY_FREEZE_DONT_FREEZE_ACTIVE_APPS = "freeze_dont_freeze_active_apps"
        const val KEY_FREEZE_MODE = "freeze_mode"
        const val KEY_FREEZE_SHOW_IN_LAUNCHER = "freeze_show_in_launcher"
        const val KEY_FREEZE_TAGS = "freeze_tags"
        const val KEY_FREEZE_APP_TAG_MAP = "freeze_app_tag_map"
        const val KEY_FREEZE_TAG_COLOR_CODED_ENABLED = "freeze_tag_color_coded_enabled"
    }
}
