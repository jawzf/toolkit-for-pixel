package com.jawzf.pixeltoolkit.feature.input.remap

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType

/** Compose state for the button remap screen. */
class RemapState {
    private lateinit var settings: PrefsStore

    val isButtonRemapEnabled = mutableStateOf(false)
    val isButtonRemapUseShizuku = mutableStateOf(false)
    val isButtonRemapPauseOnVolumeDialog = mutableStateOf(true)
    val shizukuDetectedDevicePath = mutableStateOf<String?>(null)
    val remapActions = mutableStateMapOf<RemapSlot, List<Action>>()
    val powerChoices = mutableStateMapOf<RemapSlot, PowerChoice>()
    val isPowerCameraFlipEnabled = mutableStateOf(false)
    val remapHapticType = mutableStateOf(HapticFeedbackType.DOUBLE)

    fun load(context: Context) {
        settings = PrefsStore(context)

        isButtonRemapEnabled.value =
            settings.getBoolean(
                RemapSettings.KEY_BUTTON_REMAP_ENABLED,
                settings.getBoolean(RemapSettings.KEY_FLASHLIGHT_VOLUME_TOGGLE_ENABLED),
            )
        isButtonRemapUseShizuku.value =
            settings.getBoolean(RemapSettings.KEY_BUTTON_REMAP_USE_SHIZUKU)
        isButtonRemapPauseOnVolumeDialog.value =
            settings.getBoolean(RemapSettings.KEY_BUTTON_REMAP_PAUSE_ON_VOLUME_DIALOG, true)
        shizukuDetectedDevicePath.value =
            settings.getString(RemapSettings.KEY_SHIZUKU_DETECTED_DEVICE_PATH)

        RemapSlot.ALL.forEach { slot ->
            remapActions[slot] = settings.getRemapActions(slot.prefKey)
            if (slot.choicePrefKey != null) {
                powerChoices[slot] = settings.getPowerChoice(slot)
            }
        }
        isPowerCameraFlipEnabled.value =
            settings.getBoolean(RemapSettings.KEY_BUTTON_REMAP_POWER_CAMERA_FLIP, false)

        val hapticName =
            settings.getString(
                RemapSettings.KEY_BUTTON_REMAP_HAPTIC_TYPE,
                settings.getString(
                    RemapSettings.KEY_FLASHLIGHT_HAPTIC_TYPE,
                    HapticFeedbackType.DOUBLE.name,
                ),
            )

        remapHapticType.value =
            try {
                val type = HapticFeedbackType.valueOf(hapticName ?: HapticFeedbackType.DOUBLE.name)
                if (type.name == "LONG") HapticFeedbackType.DOUBLE else type
            } catch (e: Exception) {
                HapticFeedbackType.DOUBLE
            }
    }

    fun onPreferenceChanged(key: String) {
        if (key == RemapSettings.KEY_BUTTON_REMAP_ENABLED) {
            isButtonRemapEnabled.value = settings.getBoolean(key)
        }
    }

    /**
     * Executes the set button remap enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setButtonRemapEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isButtonRemapEnabled.value = enabled
        settings.putBoolean(RemapSettings.KEY_BUTTON_REMAP_ENABLED, enabled)
        if (!enabled) {
            PowerButtonGestures.restoreDefaults(context, settings)
        } else {
            val hasNonDefaultChoice =
                RemapSlot.ALL.any { slot ->
                    slot.choicePrefKey != null &&
                        (powerChoices[slot] ?: slot.defaultChoice) != slot.defaultChoice
                }
            if (hasNonDefaultChoice || isPowerCameraFlipEnabled.value) {
                PowerButtonGestures.sync(context, settings)
            }
        }
    }

    // Pause remap while the system volume dialog is visible
    fun setButtonRemapPauseOnVolumeDialog(enabled: Boolean) {
        isButtonRemapPauseOnVolumeDialog.value = enabled
        settings.putBoolean(RemapSettings.KEY_BUTTON_REMAP_PAUSE_ON_VOLUME_DIALOG, enabled)
    }

    /**
     * Executes the set button remap use shizuku operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setButtonRemapUseShizuku(enabled: Boolean) {
        isButtonRemapUseShizuku.value = enabled
        settings.putBoolean(RemapSettings.KEY_BUTTON_REMAP_USE_SHIZUKU, enabled)
    }

    /**
     * Sets the ordered actions for a Button Remap slot.
     */
    fun setRemapActions(
        slot: RemapSlot,
        actions: List<Action>,
    ) {
        remapActions[slot] = actions
        settings.setRemapActions(slot.prefKey, actions)
    }

    fun setPowerChoice(
        slot: RemapSlot,
        choice: PowerChoice,
        context: Context,
    ) {
        powerChoices[slot] = choice
        settings.setPowerChoice(slot, choice)
        PowerButtonGestures.sync(context, settings)
    }

    fun setPowerCameraFlipEnabled(enabled: Boolean) {
        isPowerCameraFlipEnabled.value = enabled
        settings.putBoolean(RemapSettings.KEY_BUTTON_REMAP_POWER_CAMERA_FLIP, enabled)
    }

    /**
     * Executes the set remap haptic type operation.
     *
     * @param type [HapticFeedbackType] Target type.
     */
    fun setRemapHapticType(type: HapticFeedbackType) {
        remapHapticType.value = type
        settings.putString(RemapSettings.KEY_BUTTON_REMAP_HAPTIC_TYPE, type.name)
    }
}
