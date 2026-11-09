package com.jawzf.pixeltoolkit.feature.input.shortcuts

enum class LockscreenShortcutSide(
    val prefKey: String,
) {
    LEFT(ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUT_LEFT_ACTIONS),
    RIGHT(ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUT_RIGHT_ACTIONS),
}

// UNKNOWN until the lock screen has been scanned once
enum class SystemShortcutsState {
    UNKNOWN,
    NONE,
    PRESENT,
}
