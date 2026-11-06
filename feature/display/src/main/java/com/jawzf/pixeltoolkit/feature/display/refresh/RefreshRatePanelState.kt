package com.jawzf.pixeltoolkit.feature.display.refresh

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings

class RefreshRatePanelState {
    private lateinit var settings: DisplaySettings
    private var appContext: Context? = null

    val refreshRateMode = mutableStateOf(RefreshRateUtils.MODE_FIXED)
    val fixedRefreshRate = mutableFloatStateOf(0f)
    val minRefreshRate = mutableFloatStateOf(0f)
    val peakRefreshRate = mutableFloatStateOf(0f)

    fun load(context: Context) {
        settings = DisplaySettings(context)
        appContext = context.applicationContext
        refreshRateMode.value = settings.getRefreshRateMode()
        syncRefreshRateState(context)
    }

    fun setRefreshRateMode(mode: String) {
        refreshRateMode.value = mode
        if (mode == RefreshRateUtils.MODE_RANGE) {
            if (minRefreshRate.floatValue <= 0f && peakRefreshRate.floatValue <= 0f) {
                val seedValue =
                    when {
                        fixedRefreshRate.floatValue > 0f ->
                            RefreshRateUtils.normalizeRate(
                                fixedRefreshRate.floatValue,
                            )

                        else -> 60f
                    }
                minRefreshRate.floatValue = seedValue
                peakRefreshRate.floatValue = seedValue
            }
        } else if (fixedRefreshRate.floatValue <= 0f) {
            fixedRefreshRate.floatValue =
                when {
                    peakRefreshRate.floatValue > 0f -> RefreshRateUtils.normalizeRate(peakRefreshRate.floatValue)
                    minRefreshRate.floatValue > 0f -> RefreshRateUtils.normalizeRate(minRefreshRate.floatValue)
                    else -> 60f
                }
        }
        settings.setRefreshRateMode(mode)
    }

    fun updateFixedRefreshRate(value: Float) {
        fixedRefreshRate.floatValue = value
    }

    fun updateMinRefreshRate(value: Float) {
        val safeMin = value.coerceAtMost(peakRefreshRate.floatValue.takeIf { it > 0f } ?: value)
        minRefreshRate.floatValue = safeMin
        if (peakRefreshRate.floatValue > 0f && peakRefreshRate.floatValue < safeMin) {
            peakRefreshRate.floatValue = safeMin
        }
    }

    fun updatePeakRefreshRate(value: Float) {
        val safePeak = value.coerceAtLeast(minRefreshRate.floatValue.takeIf { it > 0f } ?: value)
        peakRefreshRate.floatValue = safePeak
        if (minRefreshRate.floatValue > safePeak) {
            minRefreshRate.floatValue = safePeak
        }
    }

    fun applyFixedRefreshRate(context: Context) {
        val value = fixedRefreshRate.floatValue
        if (value <= 0f) {
            resetRefreshRate(context)
            return
        }

        if (RefreshRateUtils.applyFixedRefreshRate(context, value)) {
            val normalized = RefreshRateUtils.normalizeRate(value)
            fixedRefreshRate.floatValue = normalized
            minRefreshRate.floatValue = normalized
            peakRefreshRate.floatValue = normalized
            refreshRateMode.value = RefreshRateUtils.MODE_FIXED
            persistRefreshRateStateIfNeeded(
                mode = RefreshRateUtils.MODE_FIXED,
                fixed = normalized,
                min = normalized,
                peak = normalized,
            )
        } else {
            syncRefreshRateState(context)
        }
    }

    fun applyRefreshRateRange(context: Context) {
        val minValue = minRefreshRate.floatValue
        val peakValue = peakRefreshRate.floatValue
        if (minValue <= 0f || peakValue <= 0f) {
            persistRefreshRateStateIfNeeded(
                mode = RefreshRateUtils.MODE_RANGE,
                fixed = fixedRefreshRate.floatValue,
                min = minValue,
                peak = peakValue,
            )
            return
        }

        if (RefreshRateUtils.applyRangeRefreshRate(context, minValue, peakValue)) {
            val normalizedMin = RefreshRateUtils.normalizeRate(minValue)
            val normalizedPeak = RefreshRateUtils.normalizeRate(maxOf(minValue, peakValue))
            minRefreshRate.floatValue = normalizedMin
            peakRefreshRate.floatValue = normalizedPeak
            fixedRefreshRate.floatValue = normalizedPeak
            refreshRateMode.value = RefreshRateUtils.MODE_RANGE
            persistRefreshRateStateIfNeeded(
                mode = RefreshRateUtils.MODE_RANGE,
                fixed = normalizedPeak,
                min = normalizedMin,
                peak = normalizedPeak,
            )
        } else {
            syncRefreshRateState(context)
        }
    }

    fun resetRefreshRate(context: Context) {
        val restoreInfinityPeak = settings.shouldRestoreInfinityPeakOnRefreshRateReset()
        if (RefreshRateUtils.resetRefreshRate(context, restoreInfinityPeak)) {
            fixedRefreshRate.floatValue = 0f
            minRefreshRate.floatValue = 0f
            peakRefreshRate.floatValue = 0f
            persistRefreshRateStateIfNeeded(
                mode = refreshRateMode.value,
                fixed = 0f,
                min = 0f,
                peak = 0f,
            )
        } else {
            syncRefreshRateState(context)
        }
    }

    fun syncRefreshRateState(context: Context) {
        val refreshRateState = RefreshRateUtils.getCurrentState(context)
        if (refreshRateState.isSystemManaged) {
            settings.setRestoreInfinityPeakOnRefreshRateReset(
                refreshRateState.usesInfinityDefaultPeak,
            )
        }
        val actualMin = refreshRateState.min
        val actualPeak = refreshRateState.peak
        val hasCustom = !refreshRateState.isSystemManaged && (actualMin > 0f || actualPeak > 0f)
        val storedMode = settings.getRefreshRateMode()

        if (!hasCustom) {
            fixedRefreshRate.floatValue = 0f
            minRefreshRate.floatValue = 0f
            peakRefreshRate.floatValue = 0f
            persistRefreshRateStateIfNeeded(
                mode = storedMode,
                fixed = 0f,
                min = 0f,
                peak = 0f,
            )
            return
        }

        val resolvedMin = if (actualMin > 0f) actualMin else actualPeak
        val resolvedPeak = if (actualPeak > 0f) actualPeak else actualMin
        val resolvedMode =
            if (resolvedMin > 0f && resolvedPeak > 0f && resolvedMin != resolvedPeak) {
                RefreshRateUtils.MODE_RANGE
            } else {
                storedMode
            }

        refreshRateMode.value = resolvedMode
        fixedRefreshRate.floatValue = resolvedPeak
        minRefreshRate.floatValue = resolvedMin
        peakRefreshRate.floatValue = resolvedPeak
        persistRefreshRateStateIfNeeded(
            mode = resolvedMode,
            fixed = resolvedPeak,
            min = resolvedMin,
            peak = resolvedPeak,
        )
    }

    private fun persistRefreshRateStateIfNeeded(
        mode: String,
        fixed: Float,
        min: Float,
        peak: Float,
    ) {
        val storedMode = settings.getRefreshRateMode()
        val storedFixed = settings.getFloat(DisplaySettings.KEY_REFRESH_RATE_FIXED, 0f)
        val storedMin = settings.getFloat(DisplaySettings.KEY_REFRESH_RATE_MIN, 0f)
        val storedPeak = settings.getFloat(DisplaySettings.KEY_REFRESH_RATE_PEAK, 0f)

        val isUnchanged = storedMode == mode && storedFixed == fixed && storedMin == min && storedPeak == peak
        if (isUnchanged) return

        settings.saveRefreshRateState(
            mode = mode,
            fixed = fixed,
            min = min,
            peak = peak,
        )
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            DisplaySettings.KEY_REFRESH_RATE_MODE ->
                refreshRateMode.value =
                    settings.getRefreshRateMode()

            DisplaySettings.KEY_REFRESH_RATE_FIXED,
            DisplaySettings.KEY_REFRESH_RATE_MIN,
            DisplaySettings.KEY_REFRESH_RATE_PEAK,
            -> {
                appContext?.let { syncRefreshRateState(it) }
            }
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                DisplaySettings.KEY_REFRESH_RATE_MODE,
                DisplaySettings.KEY_REFRESH_RATE_FIXED,
                DisplaySettings.KEY_REFRESH_RATE_MIN,
                DisplaySettings.KEY_REFRESH_RATE_PEAK,
            )
    }
}
