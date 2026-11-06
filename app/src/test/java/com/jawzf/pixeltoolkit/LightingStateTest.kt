package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.notifications.lighting.LightingState
import org.junit.Assert.assertEquals
import org.junit.Test

class LightingStateTest {
    @Test
    fun preferenceKeysMatchesSettings() {
        val expected =
            setOf(
                NotificationSettings.KEY_EDGE_LIGHTING_ENABLED,
                NotificationSettings.KEY_EDGE_LIGHTING_SWEEP_SELECTED_SHAPES,
            )
        assertEquals(expected, LightingState.PREFERENCE_KEYS)
        assertEquals(2, LightingState.PREFERENCE_KEYS.size)
    }
}
