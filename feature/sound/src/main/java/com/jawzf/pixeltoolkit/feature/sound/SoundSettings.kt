package com.jawzf.pixeltoolkit.feature.sound

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore

class SoundSettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    fun isVolumeStepsEnabled(): Boolean = getBoolean(KEY_VOLUME_STEPS_ENABLED, false)

    fun setVolumeStepsEnabled(enabled: Boolean) = putBoolean(KEY_VOLUME_STEPS_ENABLED, enabled)

    fun getVolumeStepsPerPress(): Int = getInt(KEY_VOLUME_STEPS_PER_PRESS, 2)

    fun setVolumeStepsPerPress(steps: Int) = putInt(KEY_VOLUME_STEPS_PER_PRESS, steps)

    fun isAppVolumeEnabled(): Boolean = getBoolean(KEY_APP_VOLUME_ENABLED, false)

    fun setAppVolumeEnabled(enabled: Boolean) = putBoolean(KEY_APP_VOLUME_ENABLED, enabled)

    fun isAppVolumeOverlayEnabled(): Boolean = getBoolean(KEY_APP_VOLUME_OVERLAY_ENABLED, false)

    fun setAppVolumeOverlayEnabled(enabled: Boolean) = putBoolean(KEY_APP_VOLUME_OVERLAY_ENABLED, enabled)

    fun getAppVolumeLevels(): Map<String, Float> =
        getString(KEY_APP_VOLUME_LEVELS)?.let {
            try {
                gson.fromJson<Map<String, Float>>(it, object : TypeToken<Map<String, Float>>() {}.type)
            } catch (_: Exception) {
                null
            }
        } ?: emptyMap()

    fun setAppVolumeLevels(levels: Map<String, Float>) = putString(KEY_APP_VOLUME_LEVELS, gson.toJson(levels))

    companion object {
        const val KEY_CALL_VIBRATIONS_ENABLED = "call_vibrations_enabled"
        const val KEY_VOLUME_STEPS_ENABLED = "volume_steps_enabled"
        const val KEY_VOLUME_STEPS_PER_PRESS = "volume_steps_per_press"
        const val KEY_APP_VOLUME_ENABLED = "app_volume_enabled"
        const val KEY_APP_VOLUME_LEVELS = "app_volume_levels"
        const val KEY_APP_VOLUME_OVERLAY_ENABLED = "app_volume_overlay_enabled"
    }
}
