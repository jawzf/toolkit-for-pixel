package com.jawzf.pixeltoolkit.core.data.apps

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.design.apps.AppIcons

object AppColorUtil {
    private const val KEY_APP_CUSTOM_COLORS = "app_custom_colors"
    private val gson = Gson()

    @Volatile
    private var cache: Map<String, Int>? = null

    fun getOverrides(context: Context): Map<String, Int> {
        cache?.let { return it }
        val json =
            PrefsStore(context)
                .getString(KEY_APP_CUSTOM_COLORS, null)
        val parsed =
            if (json.isNullOrBlank()) {
                emptyMap()
            } else {
                try {
                    val type = object : TypeToken<Map<String, Int>>() {}.type
                    gson.fromJson<Map<String, Int>>(json, type) ?: emptyMap()
                } catch (_: Exception) {
                    emptyMap()
                }
            }
        cache = parsed
        return parsed
    }

    fun getOverride(
        context: Context,
        packageName: String,
    ): Int? = getOverrides(context)[packageName]

    fun setOverride(
        context: Context,
        packageName: String,
        color: Int?,
    ) {
        val updated = getOverrides(context).toMutableMap()
        if (color == null) updated.remove(packageName) else updated[packageName] = color
        cache = updated
        PrefsStore(context)
            .putString(KEY_APP_CUSTOM_COLORS, gson.toJson(updated))
    }

    fun clearAll(context: Context) {
        cache = emptyMap()
        PrefsStore(context)
            .putString(KEY_APP_CUSTOM_COLORS, gson.toJson(emptyMap<String, Int>()))
    }

    fun invalidateCache() {
        cache = null
    }

    fun resolveColor(
        context: Context,
        packageName: String,
        callback: (Int) -> Unit,
    ) {
        val override = getOverride(context, packageName)
        if (override != null) {
            callback(override)
            return
        }
        AppIcons.getAppBrandColor(context, packageName, callback)
    }
}
