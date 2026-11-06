package com.jawzf.pixeltoolkit.feature.display

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRateUtils

class DisplaySettings(
    context: Context,
) : PrefsStore(context) {
    /**
     * Executes the get refresh rate mode operation.
     * @return The resulting String data.
     */
    fun getRefreshRateMode(): String =
        getString(KEY_REFRESH_RATE_MODE, RefreshRateUtils.MODE_FIXED)
            ?: RefreshRateUtils.MODE_FIXED

    /**
     * Executes the set refresh rate mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setRefreshRateMode(mode: String) = putString(KEY_REFRESH_RATE_MODE, mode)

    /**
     * Executes the save refresh rate state operation.
     *
     * @param mode [String] Target mode.
     * @param fixed [Float] Target fixed.
     * @param min [Float] Target min.
     * @param peak [Float] Target peak.
     */
    fun saveRefreshRateState(
        mode: String,
        fixed: Float,
        min: Float,
        peak: Float,
    ) {
        putString(KEY_REFRESH_RATE_MODE, mode)
        putFloat(KEY_REFRESH_RATE_FIXED, fixed)
        putFloat(KEY_REFRESH_RATE_MIN, min)
        putFloat(KEY_REFRESH_RATE_PEAK, peak)
    }

    /**
     * Executes the should restore infinity peak on refresh rate reset operation.
     * @return The resulting Boolean data.
     */
    fun shouldRestoreInfinityPeakOnRefreshRateReset(): Boolean = getBoolean(KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY, false)

    /**
     * Executes the set restore infinity peak on refresh rate reset operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setRestoreInfinityPeakOnRefreshRateReset(enabled: Boolean) = putBoolean(KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY, enabled)

    companion object {
        const val KEY_SMART_PIXELS_ENABLED = "smart_pixels_enabled"
        const val KEY_SMART_PIXELS_INTENSITY = "smart_pixels_intensity"
        const val KEY_SMART_PIXELS_DISABLE_ON_CAST = "smart_pixels_disable_on_cast"
        const val KEY_REFRESH_RATE_MODE = "refresh_rate_mode"
        const val KEY_REFRESH_RATE_FIXED = "refresh_rate_fixed"
        const val KEY_REFRESH_RATE_MIN = "refresh_rate_min"
        const val KEY_REFRESH_RATE_PEAK = "refresh_rate_peak"
        const val KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY = "refresh_rate_default_peak_infinity"
    }
}
