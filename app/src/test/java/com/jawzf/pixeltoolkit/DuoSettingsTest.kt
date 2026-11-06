package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class DuoSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("duo_enabled", DuoSettings.KEY_DUO_ENABLED)
        assertEquals("duo_slide_invert_direction", DuoSettings.KEY_DUO_SLIDE_INVERT_DIRECTION)
    }
}
