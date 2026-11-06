package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.glance.StatusGlanceSettings
import com.jawzf.pixeltoolkit.feature.display.glance.StatusGlanceState
import org.junit.Assert.assertEquals
import org.junit.Test

class StatusGlanceStateTest {
    @Test
    fun preferenceKeysMatchesSettings() {
        val expected =
            setOf(
                StatusGlanceSettings.KEY_STATUS_GLANCE_ENABLED,
                StatusGlanceSettings.KEY_STATUS_GLANCE_USE_AUTO_DETECT,
                StatusGlanceSettings.KEY_STATUS_GLANCE_OFFSET_X,
                StatusGlanceSettings.KEY_STATUS_GLANCE_OFFSET_Y,
                StatusGlanceSettings.KEY_STATUS_GLANCE_MAX_WIDTH,
                StatusGlanceSettings.KEY_STATUS_GLANCE_FONT_SIZE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_FLASHLIGHT,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_CALENDAR,
                StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME,
                StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY,
                StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_MEDIA,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_TIME,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BACKGROUND_PILL,
                StatusGlanceSettings.KEY_STATUS_GLANCE_ALBUM_ART_COLORS,
                StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN,
                StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS,
                StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BOLD_TEXT,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_BATTERY,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_LOW,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_FULL,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_ICON_SIZE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_TAP_ACTION,
                StatusGlanceSettings.KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION,
                StatusGlanceSettings.KEY_STATUS_GLANCE_LONG_PRESS_ACTION,
            )
        assertEquals(expected, StatusGlanceState.PREFERENCE_KEYS)
        assertEquals(29, StatusGlanceState.PREFERENCE_KEYS.size)
    }
}
