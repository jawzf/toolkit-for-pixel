package com.jawzf.pixeltoolkit.feature.input

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.feature.input.gesture.BackGestureAppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

/** Compose state and actions for the keyboard. */
class InputState(
    private val scope: CoroutineScope,
    /** Called when the per-app list changes, so the app-detection service can start or stop. */
    private val onAppDetectionChanged: (Context) -> Unit,
) {
    private lateinit var settings: InputSettings

    val keyboardHeight = mutableFloatStateOf(54f)
    val keyboardBottomPadding = mutableFloatStateOf(0f)
    val keyboardRoundness = mutableFloatStateOf(24f)
    val isKeyboardHapticsEnabled = mutableStateOf(true)
    val isKeyboardFunctionsBottom = mutableStateOf(false)
    val keyboardFunctionsPadding = mutableFloatStateOf(0f)
    val keyboardHapticStrength = mutableFloatStateOf(0.5f)
    val keyboardShape = mutableIntStateOf(0) // 0=Round, 1=Flat, 2=Inverse
    val isKeyboardAlwaysDark = mutableStateOf(false)
    val isKeyboardPitchBlack = mutableStateOf(false)
    val isKeyboardClipboardEnabled = mutableStateOf(true)

    val isUserDictionaryEnabled = mutableStateOf(false)
    val userDictionaryWords = mutableStateOf<Map<String, Long>>(emptyMap())
    val isUserDictionarySheetVisible = mutableStateOf(false)
    val isLongPressSymbolsEnabled = mutableStateOf(false)
    val isAccentedCharactersEnabled = mutableStateOf(false)

    val backGestureLeftScale = mutableFloatStateOf(1f)
    val backGestureRightScale = mutableFloatStateOf(1f)
    val backGestureAppConfigs = mutableStateOf<List<BackGestureAppConfig>>(emptyList())

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        settings = InputSettings(context)
        readAll()
    }

    private fun readAll() {
        keyboardHeight.floatValue =
            settings.getFloat(InputSettings.KEY_KEYBOARD_HEIGHT, 54f)
        keyboardBottomPadding.floatValue =
            settings.getFloat(InputSettings.KEY_KEYBOARD_BOTTOM_PADDING, 0f)
        keyboardRoundness.floatValue =
            settings.getFloat(InputSettings.KEY_KEYBOARD_ROUNDNESS, 24f)
        isKeyboardHapticsEnabled.value =
            settings.getBoolean(InputSettings.KEY_KEYBOARD_HAPTICS_ENABLED, true)
        isKeyboardFunctionsBottom.value =
            settings.getBoolean(InputSettings.KEY_KEYBOARD_FUNCTIONS_BOTTOM, false)
        keyboardFunctionsPadding.floatValue =
            settings.getFloat(InputSettings.KEY_KEYBOARD_FUNCTIONS_PADDING, 0f)
        keyboardHapticStrength.floatValue =
            settings.getFloat(InputSettings.KEY_KEYBOARD_HAPTIC_STRENGTH, 0.5f)
        keyboardShape.intValue = settings.getInt(InputSettings.KEY_KEYBOARD_SHAPE, 0)
        isKeyboardAlwaysDark.value =
            settings.getBoolean(InputSettings.KEY_KEYBOARD_ALWAYS_DARK, false)
        isKeyboardPitchBlack.value =
            settings.getBoolean(InputSettings.KEY_KEYBOARD_PITCH_BLACK, false)
        isKeyboardClipboardEnabled.value =
            settings.getBoolean(InputSettings.KEY_KEYBOARD_CLIPBOARD_ENABLED, true)
        isUserDictionaryEnabled.value =
            settings.getBoolean(InputSettings.KEY_USER_DICTIONARY_ENABLED, false)
        isLongPressSymbolsEnabled.value =
            settings.getBoolean(InputSettings.KEY_KEYBOARD_LONG_PRESS_SYMBOLS, false)
        isAccentedCharactersEnabled.value =
            settings.getBoolean(
                InputSettings.KEY_KEYBOARD_ACCENTED_CHARACTERS,
                false,
            )
    }

    /** Re-reads the state when a keyboard preference changes; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        if (key in PREFERENCE_KEYS) readAll()
    }

    /**
     * Executes the set user dictionary enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setUserDictionaryEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isUserDictionaryEnabled.value = enabled
        settings.setUserDictionaryEnabled(enabled)
    }

    /**
     * Executes the set long press symbols enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setLongPressSymbolsEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isLongPressSymbolsEnabled.value = enabled
        settings.putBoolean(InputSettings.KEY_KEYBOARD_LONG_PRESS_SYMBOLS, enabled)
    }

    /**
     * Executes the set accented characters enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setAccentedCharactersEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isAccentedCharactersEnabled.value = enabled
        settings.setAccentedCharactersEnabled(enabled)
    }

    /**
     * Executes the load user dictionary words operation.
     *
     * @param context [Context] Target context.
     */
    fun loadUserDictionaryWords(context: Context) {
        scope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, "user_dict.txt")
            if (file.exists()) {
                val map = mutableMapOf<String, Long>()
                file.forEachLine { line ->
                    val parts = line.split(" ")
                    if (parts.size >= 2) {
                        map[parts[0]] = parts[1].toLongOrNull() ?: 1L
                    }
                }
                withContext(Dispatchers.Main) {
                    userDictionaryWords.value = map
                }
            } else {
                withContext(Dispatchers.Main) {
                    userDictionaryWords.value = emptyMap()
                }
            }
        }
    }

    /**
     * Executes the delete user word operation.
     *
     * @param word [String] Target word.
     * @param context [Context] Target context.
     */
    fun deleteUserWord(
        word: String,
        context: Context,
    ) {
        scope.launch(Dispatchers.IO) {
            // Read, remove, write
            val file = File(context.filesDir, "user_dict.txt")
            if (file.exists()) {
                val lines = file.readLines().filter { !it.startsWith("$word ") }
                file.writeText(lines.joinToString("\n"))
                loadUserDictionaryWords(context)
                settings.putLong(
                    InputSettings.KEY_USER_DICT_LAST_UPDATE,
                    System.currentTimeMillis(),
                )
            }
        }
    }

    /**
     * Executes the clear user dictionary operation.
     *
     * @param context [Context] Target context.
     */
    fun clearUserDictionary(context: Context) {
        scope.launch(Dispatchers.IO) {
            val file = File(context.filesDir, "user_dict.txt")
            if (file.exists()) {
                file.delete()
                withContext(Dispatchers.Main) {
                    userDictionaryWords.value = emptyMap()
                }
                settings.putLong(
                    InputSettings.KEY_USER_DICT_LAST_UPDATE,
                    System.currentTimeMillis(),
                )
            }
        }
    }

    /**
     * Executes the open ime settings operation.
     *
     * @param context [Context] Target context.
     */
    fun openImeSettings(context: Context) {
        val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Executes the show ime picker operation.
     *
     * @param context [Context] Target context.
     */
    fun showImePicker(context: Context) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    /**
     * Executes the set keyboard height operation.
     *
     * @param height [Float] Target height.
     * @param context [Context] Target context.
     */
    fun setKeyboardHeight(
        height: Float,
        context: Context,
    ) {
        keyboardHeight.floatValue = height
        settings.putFloat(InputSettings.KEY_KEYBOARD_HEIGHT, height)
    }

    /**
     * Executes the set keyboard bottom padding operation.
     *
     * @param padding [Float] Target padding.
     * @param context [Context] Target context.
     */
    fun setKeyboardBottomPadding(
        padding: Float,
        context: Context,
    ) {
        keyboardBottomPadding.floatValue = padding
        settings.putFloat(InputSettings.KEY_KEYBOARD_BOTTOM_PADDING, padding)
    }

    /**
     * Executes the set keyboard roundness operation.
     *
     * @param roundness [Float] Target roundness.
     * @param context [Context] Target context.
     */
    fun setKeyboardRoundness(
        roundness: Float,
        context: Context,
    ) {
        keyboardRoundness.floatValue = roundness
        settings.putFloat(InputSettings.KEY_KEYBOARD_ROUNDNESS, roundness)
    }

    /**
     * Executes the set keyboard haptics enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setKeyboardHapticsEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isKeyboardHapticsEnabled.value = enabled
        settings.putBoolean(InputSettings.KEY_KEYBOARD_HAPTICS_ENABLED, enabled)
    }

    /**
     * Executes the set keyboard functions bottom operation.
     *
     * @param isBottom [Boolean] Target is bottom.
     * @param context [Context] Target context.
     */
    fun setKeyboardFunctionsBottom(
        isBottom: Boolean,
        context: Context,
    ) {
        isKeyboardFunctionsBottom.value = isBottom
        settings.putBoolean(InputSettings.KEY_KEYBOARD_FUNCTIONS_BOTTOM, isBottom)
    }

    /**
     * Executes the set keyboard functions padding operation.
     *
     * @param padding [Float] Target padding.
     * @param context [Context] Target context.
     */
    fun setKeyboardFunctionsPadding(
        padding: Float,
        context: Context,
    ) {
        keyboardFunctionsPadding.floatValue = padding
        settings.putFloat(InputSettings.KEY_KEYBOARD_FUNCTIONS_PADDING, padding)
    }

    /**
     * Executes the set keyboard haptic strength operation.
     *
     * @param strength [Float] Target strength.
     * @param context [Context] Target context.
     */
    fun setKeyboardHapticStrength(
        strength: Float,
        context: Context,
    ) {
        keyboardHapticStrength.floatValue = strength
        settings.putFloat(InputSettings.KEY_KEYBOARD_HAPTIC_STRENGTH, strength)
    }

    /**
     * Executes the set keyboard shape operation.
     *
     * @param shape [Int] Target shape.
     * @param context [Context] Target context.
     */
    fun setKeyboardShape(
        shape: Int,
        context: Context,
    ) {
        keyboardShape.intValue = shape
        settings.putInt(InputSettings.KEY_KEYBOARD_SHAPE, shape)
    }

    /**
     * Executes the set keyboard always dark operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setKeyboardAlwaysDark(
        enabled: Boolean,
        context: Context,
    ) {
        isKeyboardAlwaysDark.value = enabled
        settings.putBoolean(InputSettings.KEY_KEYBOARD_ALWAYS_DARK, enabled)
    }

    /**
     * Executes the set keyboard pitch black operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setKeyboardPitchBlack(
        enabled: Boolean,
        context: Context,
    ) {
        isKeyboardPitchBlack.value = enabled
        settings.putBoolean(InputSettings.KEY_KEYBOARD_PITCH_BLACK, enabled)
    }

    /**
     * Executes the set keyboard clipboard enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setKeyboardClipboardEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isKeyboardClipboardEnabled.value = enabled
        settings.putBoolean(InputSettings.KEY_KEYBOARD_CLIPBOARD_ENABLED, enabled)
    }

    fun refreshBackGestureState() {
        backGestureLeftScale.floatValue = settings.backGesture.defaultScale(left = true)
        backGestureRightScale.floatValue = settings.backGesture.defaultScale(left = false)
        backGestureAppConfigs.value = settings.loadBackGestureAppConfigs()
    }

    fun applyBackGestureScale(left: Boolean) {
        val scale = if (left) backGestureLeftScale.floatValue else backGestureRightScale.floatValue
        scope.launch(Dispatchers.IO) {
            settings.backGesture.setDefaultScale(left, scale)
        }
    }

    fun updateBackGestureAppConfig(
        config: BackGestureAppConfig,
        context: Context,
    ) {
        val current = backGestureAppConfigs.value
        val configs =
            if (current.any { it.packageName == config.packageName }) {
                current.map { if (it.packageName == config.packageName) config else it }
            } else {
                current + config
            }
        backGestureAppConfigs.value = configs
        settings.saveBackGestureAppConfigs(configs)
        onAppDetectionChanged(context)
    }

    fun removeBackGestureAppConfig(
        packageName: String,
        context: Context,
    ) {
        val configs = backGestureAppConfigs.value.filterNot { it.packageName == packageName }
        backGestureAppConfigs.value = configs
        settings.saveBackGestureAppConfigs(configs)
        onAppDetectionChanged(context)
        scope.launch(Dispatchers.IO) {
            settings.backGesture.onAppRemoved(packageName)
        }
    }

    companion object {
        /** Keys MainViewModel forwards to onPreferenceChanged. */
        val PREFERENCE_KEYS =
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
    }
}
