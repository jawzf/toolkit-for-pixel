package com.jawzf.pixeltoolkit.feature.display.ondisplay

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings

class ToolkitOnDisplayState {
    private lateinit var settings: DisplaySettings

    val isAmbientMusicGlanceDockedModeEnabled = mutableStateOf(false)
    val isAmbientMusicGlanceRandomShapesEnabled = mutableStateOf(false)
    val ambientMusicGlanceAlbumArtMode = mutableStateOf("default")
    val ambientMusicGlanceClockSize = mutableIntStateOf(80)
    val ambientMusicGlanceClockWeight = mutableIntStateOf(400)
    val ambientMusicGlanceClockWidth = mutableIntStateOf(100)
    val ambientMusicGlanceClockRoundness = mutableIntStateOf(50)
    val isAmbientMusicGlanceForceFillWhileChargingEnabled = mutableStateOf(false)
    val isAmbientMusicGlanceRespectNotificationsEnabled = mutableStateOf(true)
    val isAmbientMusicGlanceEnabled = mutableStateOf(false)

    fun load(context: Context) {
        settings = DisplaySettings(context)
        isAmbientMusicGlanceEnabled.value =
            settings.getBoolean(
                DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED,
                false,
            )
        isAmbientMusicGlanceDockedModeEnabled.value =
            settings.getBoolean(
                DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE,
                false,
            )
        isAmbientMusicGlanceRandomShapesEnabled.value =
            settings.getBoolean(
                DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_RANDOM_SHAPES,
                false,
            )
        ambientMusicGlanceAlbumArtMode.value =
            settings.getAmbientMusicGlanceAlbumArtMode()
        ambientMusicGlanceClockSize.intValue = settings.getAmbientMusicGlanceClockSize()
        ambientMusicGlanceClockWeight.intValue =
            settings.getAmbientMusicGlanceClockWeight()
        ambientMusicGlanceClockWidth.intValue = settings.getAmbientMusicGlanceClockWidth()
        ambientMusicGlanceClockRoundness.intValue =
            settings.getAmbientMusicGlanceClockRoundness()
        isAmbientMusicGlanceForceFillWhileChargingEnabled.value =
            settings.isAmbientMusicGlanceForceFillWhileChargingEnabled()
        isAmbientMusicGlanceRespectNotificationsEnabled.value =
            settings.isAmbientMusicGlanceRespectNotificationsEnabled()
    }

    fun setAmbientMusicGlanceEnabled(enabled: Boolean) {
        isAmbientMusicGlanceEnabled.value = enabled
        settings.putBoolean(DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED, enabled)
    }

    fun setAmbientMusicGlanceRandomShapesEnabled(enabled: Boolean) {
        isAmbientMusicGlanceRandomShapesEnabled.value = enabled
        settings.putBoolean(
            DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_RANDOM_SHAPES,
            enabled,
        )
    }

    fun setAmbientMusicGlanceAlbumArtMode(mode: String) {
        ambientMusicGlanceAlbumArtMode.value = mode
        settings.setAmbientMusicGlanceAlbumArtMode(mode)
    }

    fun setAmbientMusicGlanceClockSize(size: Int) {
        ambientMusicGlanceClockSize.intValue = size
        settings.setAmbientMusicGlanceClockSize(size)
    }

    fun setAmbientMusicGlanceClockWeight(weight: Int) {
        ambientMusicGlanceClockWeight.intValue = weight
        settings.setAmbientMusicGlanceClockWeight(weight)
    }

    fun setAmbientMusicGlanceClockWidth(width: Int) {
        ambientMusicGlanceClockWidth.intValue = width
        settings.setAmbientMusicGlanceClockWidth(width)
    }

    fun setAmbientMusicGlanceClockRoundness(roundness: Int) {
        ambientMusicGlanceClockRoundness.intValue = roundness
        settings.setAmbientMusicGlanceClockRoundness(roundness)
    }

    fun setAmbientMusicGlanceForceFillWhileChargingEnabled(enabled: Boolean) {
        isAmbientMusicGlanceForceFillWhileChargingEnabled.value = enabled
        settings.setAmbientMusicGlanceForceFillWhileChargingEnabled(enabled)
    }

    fun setAmbientMusicGlanceRespectNotificationsEnabled(enabled: Boolean) {
        isAmbientMusicGlanceRespectNotificationsEnabled.value = enabled
        settings.setAmbientMusicGlanceRespectNotificationsEnabled(enabled)
    }

    fun setAmbientMusicGlanceDockedModeEnabled(enabled: Boolean) {
        isAmbientMusicGlanceDockedModeEnabled.value = enabled
        settings.putBoolean(
            DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE,
            enabled,
        )
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED -> {
                isAmbientMusicGlanceEnabled.value = settings.getBoolean(key)
            }

            DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE -> {
                isAmbientMusicGlanceDockedModeEnabled.value =
                    settings.getBoolean(key)
            }
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED,
                DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE,
            )
    }
}
