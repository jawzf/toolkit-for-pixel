package com.jawzf.pixeltoolkit.input

import com.jawzf.pixeltoolkit.feature.input.shortcuts.ShortcutsState
import com.jawzf.pixeltoolkit.feature.input.shortcuts.SystemShortcutsState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShortcutsStateTest {
    @Test
    fun defaultStateValues() {
        val state = ShortcutsState()
        assertFalse(state.isLockscreenShortcutsEnabled.value)
        assertTrue(state.lockscreenShortcutActions.isEmpty())
        assertEquals(SystemShortcutsState.UNKNOWN, state.lockscreenSystemShortcutsState.value)
    }
}
