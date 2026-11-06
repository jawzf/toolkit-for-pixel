package com.jawzf.pixeltoolkit.feature.input

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.input.gesture.BackGestureAppConfig
import com.jawzf.pixeltoolkit.feature.input.gesture.BackGestureController
import com.jawzf.pixeltoolkit.feature.input.gesture.BackGestureStore
import kotlin.math.abs

/** Input settings on the main preference file. Keys are unchanged from SettingsRepository. */
class InputSettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

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

    fun isLinkPreviewEnabled(): Boolean = getBoolean(KEY_LINK_PREVIEW_ENABLED, true)

    fun setLinkPreviewEnabled(enabled: Boolean) = putBoolean(KEY_LINK_PREVIEW_ENABLED, enabled)

    fun isLinkPreviewImagesEnabled(): Boolean = getBoolean(KEY_LINK_PREVIEW_IMAGES_ENABLED, true)

    fun setLinkPreviewImagesEnabled(enabled: Boolean) = putBoolean(KEY_LINK_PREVIEW_IMAGES_ENABLED, enabled)

    val backGesture by lazy {
        BackGestureController(
            object : BackGestureStore {
                override fun readSystemScale(left: Boolean): Float =
                    try {
                        Settings.Secure.getFloat(context.contentResolver, secureBackGestureKey(left), 1f)
                    } catch (_: Exception) {
                        1f
                    }

                override fun writeSystemScale(
                    left: Boolean,
                    scale: Float,
                ): Boolean {
                    val key = secureBackGestureKey(left)
                    try {
                        return Settings.Secure.putFloat(context.contentResolver, key, scale)
                    } catch (_: SecurityException) {
                        ShellUtils.runCommand(context, "settings put secure $key $scale", notifyOnError = false)
                    } catch (e: Exception) {
                        Log.w("BackGesture", "Failed to write $key", e)
                        return false
                    }
                    val written = readSystemScale(left)
                    if (abs(written - scale) >= 0.001f) {
                        Log.w("BackGesture", "Failed to write $key over shell, still $written")
                        return false
                    }
                    return true
                }

                override fun getOverrideOwner(): String? = getString(KEY_BACK_GESTURE_OVERRIDE_OWNER)

                override fun setOverrideOwner(owner: String?) {
                    if (owner == null) remove(KEY_BACK_GESTURE_OVERRIDE_OWNER) else putString(KEY_BACK_GESTURE_OVERRIDE_OWNER, owner)
                }

                override fun getRestoreScale(left: Boolean): Float = getFloat(restoreBackGestureKey(left), 1f)

                override fun setRestoreScale(
                    left: Boolean,
                    scale: Float,
                ) = putFloat(restoreBackGestureKey(left), scale)
            },
        )
    }

    private fun secureBackGestureKey(left: Boolean) =
        if (left) SECURE_BACK_GESTURE_INSET_SCALE_LEFT else SECURE_BACK_GESTURE_INSET_SCALE_RIGHT

    private fun restoreBackGestureKey(left: Boolean) =
        if (left) KEY_BACK_GESTURE_RESTORE_LEFT_SCALE else KEY_BACK_GESTURE_RESTORE_RIGHT_SCALE

    fun loadBackGestureAppConfigs(): List<BackGestureAppConfig> =
        try {
            gson.fromJson(getString(KEY_BACK_GESTURE_APP_CONFIGS), Array<BackGestureAppConfig>::class.java)?.toList()
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

    fun saveBackGestureAppConfigs(configs: List<BackGestureAppConfig>) = putString(KEY_BACK_GESTURE_APP_CONFIGS, gson.toJson(configs))

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

        const val KEY_LINK_PREVIEW_ENABLED = "link_preview_enabled"
        const val KEY_LINK_PREVIEW_IMAGES_ENABLED = "link_preview_images_enabled"

        const val SECURE_BACK_GESTURE_INSET_SCALE_LEFT = "back_gesture_inset_scale_left"
        const val SECURE_BACK_GESTURE_INSET_SCALE_RIGHT = "back_gesture_inset_scale_right"
        const val KEY_BACK_GESTURE_OVERRIDE_OWNER = "back_gesture_override_owner"
        const val KEY_BACK_GESTURE_RESTORE_LEFT_SCALE = "back_gesture_restore_left_scale"
        const val KEY_BACK_GESTURE_RESTORE_RIGHT_SCALE = "back_gesture_restore_right_scale"
        const val KEY_BACK_GESTURE_APP_CONFIGS = "back_gesture_app_configs"
    }
}
