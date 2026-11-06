package com.jawzf.pixeltoolkit.feature.display.statusbar

import android.content.Context
import android.provider.Settings
import androidx.core.content.edit
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils

/**
 * Extensions and utilities for managing statusbar icon visibility
 */

private const val ICON_BLACKLIST_KEY = "icon_blacklist"

fun writeSecureSetting(
    context: Context,
    key: String,
    value: String?,
): Boolean {
    if (PermissionUtils.canWriteSecureSettings(context)) {
        try {
            if (Settings.Secure.putString(context.contentResolver, key, value)) return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (!ShellUtils.hasPermission(context)) return false
    val command =
        if (value == null) {
            "settings delete secure $key"
        } else {
            "settings put secure $key $value"
        }
    ShellUtils.runCommand(context, command, notifyOnError = false)
    return true
}

/**
 * Update the icon blacklist setting in secure settings
 */
fun updateIconBlacklistSetting(
    context: Context,
    blacklistNames: Set<String>,
) {
    val repository =
        com.jawzf.pixeltoolkit.data.repository
            .SettingsRepository(context)
    val isEnabled =
        repository.getBoolean(
            com.jawzf.pixeltoolkit.data.repository.SettingsRepository.KEY_STATUS_BAR_ICON_CONTROL_ENABLED,
            false,
        )
    if (!isEnabled) return

    writeSecureSetting(
        context,
        ICON_BLACKLIST_KEY,
        blacklistNames.takeIf { it.isNotEmpty() }?.joinToString(","),
    )
}

/**
 * Save icon visibility to shared preferences
 */
fun saveIconVisibilities(
    context: Context,
    visibilities: Map<String, Boolean>,
) {
    val prefs = context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        for ((iconId, isVisible) in visibilities) {
            putBoolean("icon_${iconId}_visible", isVisible)
        }
    }
}

/**
 * Load icon visibility from shared preferences
 */
fun loadIconVisibilities(
    context: Context,
    defaultVisibilities: Map<String, Boolean>,
): Map<String, Boolean> {
    val prefs = context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
    return defaultVisibilities.mapValues { (iconId, default) ->
        prefs.getBoolean("icon_${iconId}_visible", default)
    }
}

/**
 * Reset all icon visibility settings to defaults
 */
fun resetAllIconVisibilities(
    context: Context,
    defaultVisibilities: Map<String, Boolean>,
) {
    val prefs = context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        for ((iconId, default) in defaultVisibilities) {
            putBoolean("icon_${iconId}_visible", default)
        }
    }
}
