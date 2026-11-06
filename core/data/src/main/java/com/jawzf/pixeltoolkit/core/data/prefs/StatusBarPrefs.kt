package com.jawzf.pixeltoolkit.core.data.prefs

import android.content.Context

class StatusBarPrefs(
    context: Context,
) : PrefsStore(context) {
    fun getIslandHideIcons(): Set<String> =
        getString(KEY_ISLAND_HIDE_ICONS, "")
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()

    fun setIslandHideIcons(icons: Set<String>) = putString(KEY_ISLAND_HIDE_ICONS, icons.joinToString(","))

    companion object {
        const val KEY_ISLAND_HIDE_ICONS = "island_hide_icons"
        const val ISLAND_HIDE_ICON_SYSTEM = "system"
        const val ISLAND_HIDE_ICON_NOTIFICATIONS = "notifications"
        const val ISLAND_HIDE_ICON_CLOCK = "clock"
    }
}
