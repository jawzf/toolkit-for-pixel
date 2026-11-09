package com.jawzf.pixeltoolkit.input

import com.jawzf.pixeltoolkit.feature.input.remap.RemapSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class RemapSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("button_remap_enabled", RemapSettings.KEY_BUTTON_REMAP_ENABLED)
        assertEquals("button_remap_use_shizuku", RemapSettings.KEY_BUTTON_REMAP_USE_SHIZUKU)
        assertEquals("button_remap_vol_up_action_off", RemapSettings.KEY_BUTTON_REMAP_VOL_UP_ACTION_OFF)
        assertEquals("button_remap_vol_up_action", RemapSettings.KEY_BUTTON_REMAP_VOL_UP_ACTION)
        assertEquals("button_remap_vol_down_action_off", RemapSettings.KEY_BUTTON_REMAP_VOL_DOWN_ACTION_OFF)
        assertEquals("button_remap_vol_down_action", RemapSettings.KEY_BUTTON_REMAP_VOL_DOWN_ACTION)
        assertEquals("button_remap_vol_up_action_on", RemapSettings.KEY_BUTTON_REMAP_VOL_UP_ACTION_ON)
        assertEquals("button_remap_vol_down_action_on", RemapSettings.KEY_BUTTON_REMAP_VOL_DOWN_ACTION_ON)
        assertEquals("button_remap_power_hold_action_off", RemapSettings.KEY_BUTTON_REMAP_POWER_HOLD_ACTION_OFF)
        assertEquals("button_remap_power_hold_action_on", RemapSettings.KEY_BUTTON_REMAP_POWER_HOLD_ACTION_ON)
        assertEquals("button_remap_power_hold_choice_off", RemapSettings.KEY_BUTTON_REMAP_POWER_HOLD_CHOICE_OFF)
        assertEquals("button_remap_power_hold_choice_on", RemapSettings.KEY_BUTTON_REMAP_POWER_HOLD_CHOICE_ON)
        assertEquals("button_remap_power_overridden", RemapSettings.KEY_BUTTON_REMAP_POWER_OVERRIDDEN)
        assertEquals("button_remap_power_camera_flip", RemapSettings.KEY_BUTTON_REMAP_POWER_CAMERA_FLIP)
        assertEquals("button_remap_haptic_type", RemapSettings.KEY_BUTTON_REMAP_HAPTIC_TYPE)
        assertEquals("button_remap_action_migration_done", RemapSettings.KEY_BUTTON_REMAP_MIGRATION_DONE)
        assertEquals("button_remap_pause_on_volume_dialog", RemapSettings.KEY_BUTTON_REMAP_PAUSE_ON_VOLUME_DIALOG)
    }
}
