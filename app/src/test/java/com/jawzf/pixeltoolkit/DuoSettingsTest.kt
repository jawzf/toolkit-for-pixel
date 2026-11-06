package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.glance.StatusGlanceSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class DuoSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("duo_enabled", DuoSettings.KEY_DUO_ENABLED)
        assertEquals("duo_slide_invert_direction", DuoSettings.KEY_DUO_SLIDE_INVERT_DIRECTION)
        assertEquals("status_glance_enabled", StatusGlanceSettings.KEY_STATUS_GLANCE_ENABLED)
    }
}
