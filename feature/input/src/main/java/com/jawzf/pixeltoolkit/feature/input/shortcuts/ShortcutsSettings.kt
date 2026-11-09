package com.jawzf.pixeltoolkit.feature.input.shortcuts

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore

class ShortcutsSettings(
    context: Context,
) : PrefsStore(context) {
    fun isLockscreenShortcutsEnabled(): Boolean = getBoolean(KEY_LOCKSCREEN_SHORTCUTS_ENABLED, false)

    fun setLockscreenShortcutsEnabled(enabled: Boolean) = putBoolean(KEY_LOCKSCREEN_SHORTCUTS_ENABLED, enabled)

    fun getLockscreenSystemShortcutsState(): SystemShortcutsState =
        SystemShortcutsState.entries.firstOrNull {
            it.name == getString(KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE)
        } ?: SystemShortcutsState.UNKNOWN

    fun setLockscreenSystemShortcutsState(state: SystemShortcutsState) = putString(KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE, state.name)

    companion object {
        const val KEY_LOCKSCREEN_SHORTCUTS_ENABLED = "lockscreen_shortcuts_enabled"
        const val KEY_LOCKSCREEN_SHORTCUT_LEFT_ACTIONS = "lockscreen_shortcut_left_actions"
        const val KEY_LOCKSCREEN_SHORTCUT_RIGHT_ACTIONS = "lockscreen_shortcut_right_actions"
        const val KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE = "lockscreen_system_shortcuts_state"
    }
}
