package com.jawzf.pixeltoolkit.feature.sound

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

/** Compose state for the sound screens. */
class SoundState {
    private lateinit var settings: SoundSettings

    val isVolumeStepsEnabled = mutableStateOf(false)
    val volumeStepsPerPress = mutableIntStateOf(2)
    val isAppVolumeEnabled = mutableStateOf(false)
    val isAppVolumeOverlayEnabled = mutableStateOf(false)
    val appVolumeLevels = mutableStateOf<Map<String, Float>>(emptyMap())
    val isCallVibrationsEnabled = mutableStateOf(false)
    val isAudioSafeVolumeDisabled = mutableStateOf(false)

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        settings = SoundSettings(context)
        isVolumeStepsEnabled.value = settings.isVolumeStepsEnabled()
        volumeStepsPerPress.intValue = settings.getVolumeStepsPerPress()
        isAppVolumeEnabled.value = settings.isAppVolumeEnabled()
        isAppVolumeOverlayEnabled.value = settings.isAppVolumeOverlayEnabled()
        appVolumeLevels.value = settings.getAppVolumeLevels()
        isCallVibrationsEnabled.value = settings.getBoolean(SoundSettings.KEY_CALL_VIBRATIONS_ENABLED)
        syncAudioSafeVolumeState(context)
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        when (key) {
            SoundSettings.KEY_CALL_VIBRATIONS_ENABLED -> isCallVibrationsEnabled.value = settings.getBoolean(key)
            else -> {}
        }
    }

    /**
     * Executes the set call vibrations enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setCallVibrationsEnabled(enabled: Boolean) {
        isCallVibrationsEnabled.value = enabled
        settings.putBoolean(SoundSettings.KEY_CALL_VIBRATIONS_ENABLED, enabled)
    }

    fun setVolumeStepsEnabled(enabled: Boolean) {
        isVolumeStepsEnabled.value = enabled
        settings.setVolumeStepsEnabled(enabled)
    }

    fun setVolumeStepsPerPress(steps: Int) {
        volumeStepsPerPress.intValue = steps
        settings.setVolumeStepsPerPress(steps)
    }

    fun setAppVolumeEnabled(enabled: Boolean) {
        isAppVolumeEnabled.value = enabled
        settings.setAppVolumeEnabled(enabled)
    }

    fun setAppVolumeOverlayEnabled(enabled: Boolean) {
        isAppVolumeOverlayEnabled.value = enabled
        settings.setAppVolumeOverlayEnabled(enabled)
    }

    fun setAppVolumeLevel(
        packageName: String,
        level: Float,
    ) {
        appVolumeLevels.value = appVolumeLevels.value + (packageName to level)
        settings.setAppVolumeLevels(appVolumeLevels.value)
    }

    fun removeAppVolumeLevel(packageName: String) {
        appVolumeLevels.value = appVolumeLevels.value - packageName
        settings.setAppVolumeLevels(appVolumeLevels.value)
    }

    /**
     * Executes the sync audio safe volume state operation.
     *
     * @param context [Context] Target context.
     */
    fun syncAudioSafeVolumeState(context: Context) {
        val rawValue = Settings.Global.getInt(context.contentResolver, "audio_safe_volume_state", 3)
        // 1 = Disable (toggle is on), 3 = Active (toggle is off)
        isAudioSafeVolumeDisabled.value = (rawValue == 1)
    }

    /**
     * Executes the set audio safe volume disabled operation.
     *
     * @param context [Context] Target context.
     * @param disabled [Boolean] Target disabled.
     */
    fun setAudioSafeVolumeDisabled(
        context: Context,
        disabled: Boolean,
    ) {
        val targetValue = if (disabled) 1 else 3
        try {
            Settings.Global.putInt(context.contentResolver, "audio_safe_volume_state", targetValue)
            isAudioSafeVolumeDisabled.value = disabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
