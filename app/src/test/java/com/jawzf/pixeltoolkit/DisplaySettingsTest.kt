package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplaySettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("smart_pixels_enabled", DisplaySettings.KEY_SMART_PIXELS_ENABLED)
        assertEquals("smart_pixels_intensity", DisplaySettings.KEY_SMART_PIXELS_INTENSITY)
        assertEquals("smart_pixels_disable_on_cast", DisplaySettings.KEY_SMART_PIXELS_DISABLE_ON_CAST)
        assertEquals("refresh_rate_mode", DisplaySettings.KEY_REFRESH_RATE_MODE)
        assertEquals("refresh_rate_fixed", DisplaySettings.KEY_REFRESH_RATE_FIXED)
        assertEquals("refresh_rate_min", DisplaySettings.KEY_REFRESH_RATE_MIN)
        assertEquals("refresh_rate_peak", DisplaySettings.KEY_REFRESH_RATE_PEAK)
        assertEquals("refresh_rate_default_peak_infinity", DisplaySettings.KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY)
    }
}
