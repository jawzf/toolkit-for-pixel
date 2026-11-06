package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.feature.display.duo.DuoState
import org.junit.Assert.assertEquals
import org.junit.Test

class DuoStateTest {
    @Test
    fun preferenceKeysMatchesSettings() {
        val expected =
            setOf(
                DuoSettings.KEY_DUO_ENABLED,
                DuoSettings.KEY_DUO_USE_AUTO_DETECT,
                DuoSettings.KEY_DUO_CAMERA_OFFSET_X,
                DuoSettings.KEY_DUO_CAMERA_OFFSET_Y,
                DuoSettings.KEY_DUO_CAMERA_SIZE,
                DuoSettings.KEY_DUO_ARC_THICKNESS,
                DuoSettings.KEY_DUO_DOT_SIZE,
                DuoSettings.KEY_DUO_RING_RADIUS,
                DuoSettings.KEY_DUO_TIME_TEXT_SCALE,
                DuoSettings.KEY_DUO_SHOW_BATTERY,
                DuoSettings.KEY_DUO_SHOW_BATTERY_PERCENTAGE,
                DuoSettings.KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED,
                DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR,
                DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR,
                DuoSettings.KEY_DUO_BATTERY_LOW_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_LOW_COLOR,
                DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR,
                DuoSettings.KEY_DUO_BATTERY_LOW_LEVEL,
                DuoSettings.KEY_DUO_BATTERY_CRITICAL_LEVEL,
                DuoSettings.KEY_DUO_SHOW_NETWORKS,
                DuoSettings.KEY_DUO_DIFFERENTIATE_WIFI,
                DuoSettings.KEY_DUO_SHOW_TIME,
                DuoSettings.KEY_DUO_SHOW_MEDIA,
                DuoSettings.KEY_DUO_ROTATE_ALBUM_ART,
                DuoSettings.KEY_DUO_SHOW_PROGRESS,
                DuoSettings.KEY_DUO_SHOW_FLASHLIGHT,
                DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF,
                DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE,
                DuoSettings.KEY_DUO_USE_MATERIAL_YOU,
                DuoSettings.KEY_DUO_CUSTOM_COLOR,
                DuoSettings.KEY_DUO_TAP_ACTION,
                DuoSettings.KEY_DUO_DOUBLE_TAP_ACTION,
                DuoSettings.KEY_DUO_LONG_PRESS_ACTION,
                DuoSettings.KEY_DUO_SWIPE_DOWN_ACTION,
                DuoSettings.KEY_DUO_SLIDE_MODE,
                DuoSettings.KEY_DUO_SLIDE_TRACK,
                DuoSettings.KEY_DUO_SLIDE_INVERT_DIRECTION,
            )
        assertEquals(expected, DuoState.PREFERENCE_KEYS)
        assertEquals(40, DuoState.PREFERENCE_KEYS.size)
    }
}
