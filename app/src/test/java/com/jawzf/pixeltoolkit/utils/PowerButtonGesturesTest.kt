package com.jawzf.pixeltoolkit.utils

import com.jawzf.pixeltoolkit.domain.model.PowerChoice
import com.jawzf.pixeltoolkit.domain.model.RemapInput
import com.jawzf.pixeltoolkit.domain.model.RemapScreenState
import com.jawzf.pixeltoolkit.domain.model.RemapSlot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PowerButtonGesturesTest {
    @Test
    fun longPressValues() {
        assertEquals(5, PowerButtonGestures.longPressValue(PowerChoice.ASSISTANT, PowerChoice.ASSISTANT))
        assertEquals(1, PowerButtonGestures.longPressValue(PowerChoice.POWER_MENU, PowerChoice.POWER_MENU))
        assertEquals(0, PowerButtonGestures.longPressValue(PowerChoice.ASSISTANT, PowerChoice.POWER_MENU))
        assertEquals(0, PowerButtonGestures.longPressValue(PowerChoice.ACTIONS, PowerChoice.ACTIONS))
        assertEquals(0, PowerButtonGestures.longPressValue(PowerChoice.POWER_MENU, PowerChoice.ACTIONS))
    }

    @Test
    fun handledByToolkitCombinations() {
        assertFalse(PowerButtonGestures.isHandledByToolkit(PowerChoice.ASSISTANT, PowerChoice.ASSISTANT))
        assertFalse(PowerButtonGestures.isHandledByToolkit(PowerChoice.POWER_MENU, PowerChoice.POWER_MENU))
        assertTrue(PowerButtonGestures.isHandledByToolkit(PowerChoice.ASSISTANT, PowerChoice.POWER_MENU))
        assertTrue(PowerButtonGestures.isHandledByToolkit(PowerChoice.ACTIONS, PowerChoice.ACTIONS))
        assertTrue(PowerButtonGestures.isHandledByToolkit(PowerChoice.ASSISTANT, PowerChoice.ACTIONS))
    }

    @Test
    fun remapSlotDefaultsAndKeys() {
        assertEquals(
            PowerChoice.ASSISTANT,
            RemapSlot(RemapInput.POWER_HOLD, RemapScreenState.OFF).defaultChoice,
        )
        assertNull(RemapSlot(RemapInput.VOLUME_UP, RemapScreenState.OFF).choicePrefKey)
        assertNull(RemapSlot(RemapInput.VOLUME_UP, RemapScreenState.ON).choicePrefKey)
        assertNull(RemapSlot(RemapInput.VOLUME_DOWN, RemapScreenState.OFF).choicePrefKey)
        assertNull(RemapSlot(RemapInput.VOLUME_DOWN, RemapScreenState.ON).choicePrefKey)
        assertNull(RemapSlot(RemapInput.VOLUME_UP, RemapScreenState.OFF).defaultChoice)
        assertNull(RemapSlot(RemapInput.VOLUME_DOWN, RemapScreenState.ON).defaultChoice)
    }
}
