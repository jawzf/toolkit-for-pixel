package com.jawzf.pixeltoolkit.input

import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.input.shortcuts.ShortcutsSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ShortcutsSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("lockscreen_shortcuts_enabled", ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUTS_ENABLED)
        assertEquals("lockscreen_shortcut_left_actions", ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUT_LEFT_ACTIONS)
        assertEquals("lockscreen_shortcut_right_actions", ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUT_RIGHT_ACTIONS)
        assertEquals("lockscreen_system_shortcuts_state", ShortcutsSettings.KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE)
    }

    @Test
    fun aliasesMatchInSettingsRepository() {
        assertEquals(ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUTS_ENABLED, SettingsRepository.KEY_LOCKSCREEN_SHORTCUTS_ENABLED)
        assertEquals(ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUT_LEFT_ACTIONS, SettingsRepository.KEY_LOCKSCREEN_SHORTCUT_LEFT_ACTIONS)
        assertEquals(ShortcutsSettings.KEY_LOCKSCREEN_SHORTCUT_RIGHT_ACTIONS, SettingsRepository.KEY_LOCKSCREEN_SHORTCUT_RIGHT_ACTIONS)
        assertEquals(ShortcutsSettings.KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE, SettingsRepository.KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE)
    }
}
