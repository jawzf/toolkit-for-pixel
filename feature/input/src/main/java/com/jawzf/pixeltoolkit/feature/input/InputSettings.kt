package com.jawzf.pixeltoolkit.feature.input

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore

/** Input settings on the main preference file. Keys are unchanged from SettingsRepository. */
class InputSettings(
    context: Context,
) : PrefsStore(context) {
    /**
     * Executes the is user dictionary enabled operation.
     * @return The resulting Boolean data.
     */
    fun isUserDictionaryEnabled(): Boolean = getBoolean(KEY_USER_DICTIONARY_ENABLED, false)

    /**
     * Executes the set user dictionary enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setUserDictionaryEnabled(enabled: Boolean) = putBoolean(KEY_USER_DICTIONARY_ENABLED, enabled)

    /**
     * Executes the is accented characters enabled operation.
     * @return The resulting Boolean data.
     */
    fun isAccentedCharactersEnabled(): Boolean = getBoolean(KEY_KEYBOARD_ACCENTED_CHARACTERS, false)

    /**
     * Executes the set accented characters enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAccentedCharactersEnabled(enabled: Boolean) = putBoolean(KEY_KEYBOARD_ACCENTED_CHARACTERS, enabled)

    companion object {
        const val KEY_KEYBOARD_HEIGHT = "keyboard_height"
        const val KEY_KEYBOARD_BOTTOM_PADDING = "keyboard_bottom_padding"
        const val KEY_KEYBOARD_HAPTICS_ENABLED = "keyboard_haptics_enabled"
        const val KEY_KEYBOARD_ROUNDNESS = "keyboard_roundness"
        const val KEY_KEYBOARD_SHAPE = "keyboard_shape" // 0=Round, 1=Flat, 2=Inverse
        const val KEY_KEYBOARD_FUNCTIONS_BOTTOM = "keyboard_functions_bottom"
        const val KEY_KEYBOARD_FUNCTIONS_PADDING = "keyboard_functions_padding"
        const val KEY_KEYBOARD_HAPTIC_STRENGTH = "keyboard_haptic_strength"
        const val KEY_KEYBOARD_ALWAYS_DARK = "keyboard_always_dark"
        const val KEY_KEYBOARD_PITCH_BLACK = "keyboard_pitch_black"
        const val KEY_KEYBOARD_CLIPBOARD_ENABLED = "keyboard_clipboard_enabled"
        const val KEY_KEYBOARD_LONG_PRESS_SYMBOLS = "keyboard_long_press_symbols"
        const val KEY_KEYBOARD_ACCENTED_CHARACTERS = "keyboard_accented_characters"

        const val KEY_USER_DICTIONARY_ENABLED = "user_dictionary_enabled"
        const val KEY_USER_DICT_LAST_UPDATE = "user_dict_last_update"
    }
}
