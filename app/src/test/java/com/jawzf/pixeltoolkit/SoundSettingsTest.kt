package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.sound.SoundSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class SoundSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("call_vibrations_enabled", SoundSettings.KEY_CALL_VIBRATIONS_ENABLED)
        assertEquals("volume_steps_enabled", SoundSettings.KEY_VOLUME_STEPS_ENABLED)
        assertEquals("volume_steps_per_press", SoundSettings.KEY_VOLUME_STEPS_PER_PRESS)
        assertEquals("app_volume_enabled", SoundSettings.KEY_APP_VOLUME_ENABLED)
        assertEquals("app_volume_levels", SoundSettings.KEY_APP_VOLUME_LEVELS)
        assertEquals("app_volume_overlay_enabled", SoundSettings.KEY_APP_VOLUME_OVERLAY_ENABLED)
    }
}
