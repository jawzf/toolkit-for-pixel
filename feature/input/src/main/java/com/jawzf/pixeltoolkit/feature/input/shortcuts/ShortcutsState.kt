package com.jawzf.pixeltoolkit.feature.input.shortcuts

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.action.Action

/** Compose state for the lock screen shortcuts screen. */
class ShortcutsState {
    private lateinit var settings: ShortcutsSettings

    val isLockscreenShortcutsEnabled = mutableStateOf(false)
    val lockscreenShortcutActions = mutableStateMapOf<LockscreenShortcutSide, List<Action>>()
    val lockscreenSystemShortcutsState = mutableStateOf(SystemShortcutsState.UNKNOWN)

    fun load(context: Context) {
        settings = ShortcutsSettings(context)
        isLockscreenShortcutsEnabled.value = settings.isLockscreenShortcutsEnabled()
        LockscreenShortcutSide.entries.forEach { side ->
            lockscreenShortcutActions[side] = settings.getRemapActions(side.prefKey)
        }
        lockscreenSystemShortcutsState.value = settings.getLockscreenSystemShortcutsState()
    }

    fun onPreferenceChanged(key: String) {
        if (key == ShortcutsSettings.KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE) {
            lockscreenSystemShortcutsState.value = settings.getLockscreenSystemShortcutsState()
        }
    }

    fun setLockscreenShortcutsEnabled(enabled: Boolean) {
        isLockscreenShortcutsEnabled.value = enabled
        settings.setLockscreenShortcutsEnabled(enabled)
    }

    fun setLockscreenShortcutActions(
        side: LockscreenShortcutSide,
        actions: List<Action>,
    ) {
        lockscreenShortcutActions[side] = actions
        settings.setRemapActions(side.prefKey, actions)
    }
}
