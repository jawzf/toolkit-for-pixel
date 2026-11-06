package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.input.InputSettings
import com.jawzf.pixeltoolkit.feature.input.InputState
import org.junit.Assert.assertEquals
import org.junit.Test

class InputStateTest {
    @Test
    fun preferenceKeysMatchesInputSettingsKeyboardKeys() {
        val expected =
            setOf(
                InputSettings.KEY_KEYBOARD_HEIGHT,
                InputSettings.KEY_KEYBOARD_BOTTOM_PADDING,
                InputSettings.KEY_KEYBOARD_ROUNDNESS,
                InputSettings.KEY_KEYBOARD_HAPTICS_ENABLED,
                InputSettings.KEY_KEYBOARD_FUNCTIONS_BOTTOM,
                InputSettings.KEY_KEYBOARD_FUNCTIONS_PADDING,
                InputSettings.KEY_KEYBOARD_HAPTIC_STRENGTH,
                InputSettings.KEY_KEYBOARD_SHAPE,
                InputSettings.KEY_KEYBOARD_ALWAYS_DARK,
                InputSettings.KEY_KEYBOARD_PITCH_BLACK,
                InputSettings.KEY_KEYBOARD_CLIPBOARD_ENABLED,
                InputSettings.KEY_KEYBOARD_LONG_PRESS_SYMBOLS,
                InputSettings.KEY_KEYBOARD_ACCENTED_CHARACTERS,
            )
        assertEquals(expected, InputState.PREFERENCE_KEYS)
        assertEquals(13, InputState.PREFERENCE_KEYS.size)
    }
}
