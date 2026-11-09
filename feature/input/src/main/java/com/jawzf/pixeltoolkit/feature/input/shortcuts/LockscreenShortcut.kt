package com.jawzf.pixeltoolkit.feature.input.shortcuts

import com.jawzf.pixeltoolkit.data.repository.SettingsRepository

enum class LockscreenShortcutSide(
    val prefKey: String,
) {
    LEFT(SettingsRepository.KEY_LOCKSCREEN_SHORTCUT_LEFT_ACTIONS),
    RIGHT(SettingsRepository.KEY_LOCKSCREEN_SHORTCUT_RIGHT_ACTIONS),
}

// UNKNOWN until the lock screen has been scanned once
enum class SystemShortcutsState {
    UNKNOWN,
    NONE,
    PRESENT,
}
