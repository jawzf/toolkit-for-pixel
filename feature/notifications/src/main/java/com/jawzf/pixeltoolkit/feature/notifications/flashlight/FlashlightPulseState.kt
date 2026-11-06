package com.jawzf.pixeltoolkit.feature.notifications.flashlight

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings

class FlashlightPulseState {
    private lateinit var settings: NotificationSettings

    val isFlashlightPulseEnabled = mutableStateOf(false)
    val isFlashlightPulseFacedownOnly = mutableStateOf(true)
    val isFlashlightPulseUseLightingApps = mutableStateOf(true)
    val flashlightPulseMaxIntensity = mutableFloatStateOf(0.5f)
    val isFlashlightPulseDisableOnDnd = mutableStateOf(true)

    fun load(context: Context) {
        settings = NotificationSettings(context)
        isFlashlightPulseEnabled.value =
            settings.getBoolean(NotificationSettings.KEY_FLASHLIGHT_PULSE_ENABLED)
        isFlashlightPulseFacedownOnly.value =
            settings.getBoolean(
                NotificationSettings.KEY_FLASHLIGHT_PULSE_FACEDOWN_ONLY,
                true,
            )
        isFlashlightPulseUseLightingApps.value =
            settings.getBoolean(
                NotificationSettings.KEY_FLASHLIGHT_PULSE_SAME_AS_LIGHTING,
                true,
            )
        flashlightPulseMaxIntensity.floatValue =
            settings.getFloat(
                NotificationSettings.KEY_FLASHLIGHT_PULSE_MAX_INTENSITY,
                0.5f,
            )
        isFlashlightPulseDisableOnDnd.value =
            settings.getBoolean(
                NotificationSettings.KEY_FLASHLIGHT_PULSE_DISABLE_ON_DND,
                true,
            )
    }

    fun setFlashlightPulseEnabled(enabled: Boolean) {
        isFlashlightPulseEnabled.value = enabled
        settings.putBoolean(NotificationSettings.KEY_FLASHLIGHT_PULSE_ENABLED, enabled)
    }

    fun setFlashlightPulseFacedownOnly(enabled: Boolean) {
        isFlashlightPulseFacedownOnly.value = enabled
        settings.putBoolean(
            NotificationSettings.KEY_FLASHLIGHT_PULSE_FACEDOWN_ONLY,
            enabled,
        )
    }

    fun setFlashlightPulseUseLightingApps(enabled: Boolean) {
        isFlashlightPulseUseLightingApps.value = enabled
        settings.putBoolean(
            NotificationSettings.KEY_FLASHLIGHT_PULSE_SAME_AS_LIGHTING,
            enabled,
        )
    }

    fun setFlashlightPulseMaxIntensity(intensity: Float) {
        flashlightPulseMaxIntensity.floatValue = intensity
        settings.putFloat(
            NotificationSettings.KEY_FLASHLIGHT_PULSE_MAX_INTENSITY,
            intensity,
        )
    }

    fun setFlashlightPulseDisableOnDnd(enabled: Boolean) {
        isFlashlightPulseDisableOnDnd.value = enabled
        settings.putBoolean(
            NotificationSettings.KEY_FLASHLIGHT_PULSE_DISABLE_ON_DND,
            enabled,
        )
    }

    fun loadFlashlightPulseSelectedApps(): List<AppSelection> = settings.loadFlashlightPulseSelectedApps()

    fun saveFlashlightPulseSelectedApps(apps: List<AppSelection>) {
        settings.saveFlashlightPulseSelectedApps(apps)
    }

    fun updateFlashlightPulseAppEnabled(
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateFlashlightPulseAppSelection(packageName, enabled)
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            NotificationSettings.KEY_FLASHLIGHT_PULSE_MAX_INTENSITY -> {
                flashlightPulseMaxIntensity.floatValue =
                    settings.getFloat(key, 0.5f)
            }
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                NotificationSettings.KEY_FLASHLIGHT_PULSE_MAX_INTENSITY,
            )
    }
}
