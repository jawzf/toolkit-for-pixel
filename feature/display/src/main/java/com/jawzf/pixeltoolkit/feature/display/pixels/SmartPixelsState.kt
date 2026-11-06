package com.jawzf.pixeltoolkit.feature.display.pixels

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings

class SmartPixelsState {
    private lateinit var settings: DisplaySettings

    val isSmartPixelsEnabled = mutableStateOf(false)
    val smartPixelsIntensity = mutableFloatStateOf(50f)
    val isSmartPixelsDisableOnCastEnabled = mutableStateOf(true)

    fun load(context: Context) {
        settings = DisplaySettings(context)
        isSmartPixelsEnabled.value =
            settings.getBoolean(DisplaySettings.KEY_SMART_PIXELS_ENABLED)
        smartPixelsIntensity.floatValue =
            settings.getFloat(DisplaySettings.KEY_SMART_PIXELS_INTENSITY, 50f)
        isSmartPixelsDisableOnCastEnabled.value =
            settings.getBoolean(DisplaySettings.KEY_SMART_PIXELS_DISABLE_ON_CAST, true)
    }

    fun setSmartPixelsEnabled(enabled: Boolean) {
        isSmartPixelsEnabled.value = enabled
        settings.putBoolean(DisplaySettings.KEY_SMART_PIXELS_ENABLED, enabled)
    }

    fun setSmartPixelsIntensity(intensity: Float) {
        smartPixelsIntensity.floatValue = intensity
        settings.putFloat(DisplaySettings.KEY_SMART_PIXELS_INTENSITY, intensity)
    }

    fun setSmartPixelsDisableOnCastEnabled(enabled: Boolean) {
        isSmartPixelsDisableOnCastEnabled.value = enabled
        settings.putBoolean(DisplaySettings.KEY_SMART_PIXELS_DISABLE_ON_CAST, enabled)
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            DisplaySettings.KEY_SMART_PIXELS_ENABLED ->
                isSmartPixelsEnabled.value =
                    settings.getBoolean(key)

            DisplaySettings.KEY_SMART_PIXELS_INTENSITY ->
                smartPixelsIntensity.floatValue =
                    settings.getFloat(key, 50f)

            DisplaySettings.KEY_SMART_PIXELS_DISABLE_ON_CAST ->
                isSmartPixelsDisableOnCastEnabled.value =
                    settings.getBoolean(key, true)
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                DisplaySettings.KEY_SMART_PIXELS_ENABLED,
                DisplaySettings.KEY_SMART_PIXELS_INTENSITY,
                DisplaySettings.KEY_SMART_PIXELS_DISABLE_ON_CAST,
            )
    }
}
