package com.jawzf.pixeltoolkit.input

import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.feature.input.remap.RemapState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemapStateTest {
    @Test
    fun defaultStateValues() {
        val state = RemapState()
        assertFalse(state.isButtonRemapEnabled.value)
        assertFalse(state.isButtonRemapUseShizuku.value)
        assertTrue(state.isButtonRemapPauseOnVolumeDialog.value)
        assertNull(state.shizukuDetectedDevicePath.value)
        assertTrue(state.remapActions.isEmpty())
        assertTrue(state.powerChoices.isEmpty())
        assertFalse(state.isPowerCameraFlipEnabled.value)
        assertEquals(HapticFeedbackType.DOUBLE, state.remapHapticType.value)
    }
}
