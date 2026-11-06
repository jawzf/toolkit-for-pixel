package com.jawzf.pixeltoolkit.feature.notifications.lighting

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.mutableFloatStateOf
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.notifications.lighting.DashConfig

class DashSettings(
    private val repository: () -> PrefsStore,
) {
    val thickness = mutableFloatStateOf(DashConfig.DEFAULT_THICKNESS)
    val length = mutableFloatStateOf(DashConfig.DEFAULT_LENGTH_PERCENT)
    val glow = mutableFloatStateOf(DashConfig.DEFAULT_GLOW)
    val glowLength = mutableFloatStateOf(DashConfig.DEFAULT_GLOW_LENGTH_RATIO)
    val overlap = mutableFloatStateOf(DashConfig.DEFAULT_OVERLAP_PERCENT)

    fun load() {
        val repo = repository()
        thickness.floatValue = repo.getFloat(KEY_THICKNESS, DashConfig.DEFAULT_THICKNESS)
        length.floatValue = repo.getFloat(KEY_LENGTH, DashConfig.DEFAULT_LENGTH_PERCENT)
        glow.floatValue = repo.getFloat(KEY_GLOW, DashConfig.DEFAULT_GLOW)
        glowLength.floatValue =
            repo.getFloat(KEY_GLOW_LENGTH, DashConfig.DEFAULT_GLOW_LENGTH_RATIO)
        overlap.floatValue = repo.getFloat(KEY_OVERLAP, DashConfig.DEFAULT_OVERLAP_PERCENT)
    }

    fun toConfig(): DashConfig =
        DashConfig(
            thickness = thickness.floatValue,
            lengthPercent = length.floatValue,
            glow = glow.floatValue,
            glowLengthRatio = glowLength.floatValue,
            overlapPercent = overlap.floatValue,
        )

    fun saveThickness(value: Float) = putFloat(thickness, KEY_THICKNESS, value)

    fun saveLength(value: Float) = putFloat(length, KEY_LENGTH, value)

    fun saveGlow(value: Float) = putFloat(glow, KEY_GLOW, value)

    fun saveGlowLength(value: Float) = putFloat(glowLength, KEY_GLOW_LENGTH, value)

    fun saveOverlap(value: Float) = putFloat(overlap, KEY_OVERLAP, value)

    private fun putFloat(
        state: MutableFloatState,
        key: String,
        value: Float,
    ) {
        state.floatValue = value
        repository().putFloat(key, value)
    }

    private companion object {
        const val KEY_THICKNESS = NotificationSettings.KEY_EDGE_LIGHTING_DASH_THICKNESS
        const val KEY_LENGTH = NotificationSettings.KEY_EDGE_LIGHTING_DASH_LENGTH
        const val KEY_GLOW = NotificationSettings.KEY_EDGE_LIGHTING_DASH_GLOW
        const val KEY_GLOW_LENGTH = NotificationSettings.KEY_EDGE_LIGHTING_DASH_GLOW_LENGTH
        const val KEY_OVERLAP = NotificationSettings.KEY_EDGE_LIGHTING_DASH_OVERLAP
    }
}
