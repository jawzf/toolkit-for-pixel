package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import com.jawzf.pixeltoolkit.feature.display.nightlight.NightLightState
import org.junit.Assert.assertEquals
import org.junit.Test

class NightLightStateTest {
    @Test
    fun preferenceKeysMatchesSettings() {
        val expected = setOf(DisplaySettings.KEY_DYNAMIC_NIGHT_LIGHT_ENABLED)
        assertEquals(expected, NightLightState.PREFERENCE_KEYS)
    }
}
