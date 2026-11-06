package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.input.InputSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class InputSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("keyboard_height", InputSettings.KEY_KEYBOARD_HEIGHT)
        assertEquals("keyboard_bottom_padding", InputSettings.KEY_KEYBOARD_BOTTOM_PADDING)
        assertEquals("keyboard_haptics_enabled", InputSettings.KEY_KEYBOARD_HAPTICS_ENABLED)
        assertEquals("keyboard_roundness", InputSettings.KEY_KEYBOARD_ROUNDNESS)
        assertEquals("keyboard_shape", InputSettings.KEY_KEYBOARD_SHAPE)
        assertEquals("keyboard_functions_bottom", InputSettings.KEY_KEYBOARD_FUNCTIONS_BOTTOM)
        assertEquals("keyboard_functions_padding", InputSettings.KEY_KEYBOARD_FUNCTIONS_PADDING)
        assertEquals("keyboard_haptic_strength", InputSettings.KEY_KEYBOARD_HAPTIC_STRENGTH)
        assertEquals("keyboard_always_dark", InputSettings.KEY_KEYBOARD_ALWAYS_DARK)
        assertEquals("keyboard_pitch_black", InputSettings.KEY_KEYBOARD_PITCH_BLACK)
        assertEquals("keyboard_clipboard_enabled", InputSettings.KEY_KEYBOARD_CLIPBOARD_ENABLED)
        assertEquals("keyboard_long_press_symbols", InputSettings.KEY_KEYBOARD_LONG_PRESS_SYMBOLS)
        assertEquals("keyboard_accented_characters", InputSettings.KEY_KEYBOARD_ACCENTED_CHARACTERS)
        assertEquals("user_dictionary_enabled", InputSettings.KEY_USER_DICTIONARY_ENABLED)
        assertEquals("user_dict_last_update", InputSettings.KEY_USER_DICT_LAST_UPDATE)
        assertEquals("link_preview_enabled", InputSettings.KEY_LINK_PREVIEW_ENABLED)
        assertEquals("link_preview_images_enabled", InputSettings.KEY_LINK_PREVIEW_IMAGES_ENABLED)
        assertEquals("back_gesture_inset_scale_left", InputSettings.SECURE_BACK_GESTURE_INSET_SCALE_LEFT)
        assertEquals("back_gesture_inset_scale_right", InputSettings.SECURE_BACK_GESTURE_INSET_SCALE_RIGHT)
        assertEquals("back_gesture_override_owner", InputSettings.KEY_BACK_GESTURE_OVERRIDE_OWNER)
        assertEquals("back_gesture_restore_left_scale", InputSettings.KEY_BACK_GESTURE_RESTORE_LEFT_SCALE)
        assertEquals("back_gesture_restore_right_scale", InputSettings.KEY_BACK_GESTURE_RESTORE_RIGHT_SCALE)
        assertEquals("back_gesture_app_configs", InputSettings.KEY_BACK_GESTURE_APP_CONFIGS)
    }
}
