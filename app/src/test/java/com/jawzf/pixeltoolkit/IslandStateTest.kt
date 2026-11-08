package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.feature.island.IslandState
import org.junit.Assert.assertEquals
import org.junit.Test

class IslandStateTest {
    @Test
    fun preferenceKeysMatchesSettings() {
        val expected =
            setOf(
                IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED,
                IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR,
                IslandSettings.KEY_ISLAND_ENABLED,
                IslandSettings.KEY_ISLAND_USE_AUTO_DETECT,
                IslandSettings.KEY_ISLAND_CAMERA_OFFSET_X,
                IslandSettings.KEY_ISLAND_CAMERA_OFFSET_Y,
                IslandSettings.KEY_ISLAND_CAMERA_SIZE,
                IslandSettings.KEY_ISLAND_MAX_WIDTH,
                IslandSettings.KEY_ISLAND_CUTOUT_GAP,
                IslandSettings.KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP,
                IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
                IslandSettings.KEY_ISLAND_HIDE_WHEN_SCREEN_OFF,
                IslandSettings.KEY_ISLAND_TIMEOUT_MS,
                IslandSettings.KEY_ISLAND_LINE_STAGE_ENABLED,
                IslandSettings.KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE,
                IslandSettings.KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED,
                IslandSettings.KEY_ISLAND_MEDIA_SHOW_PREVIOUS,
                IslandSettings.KEY_ISLAND_MEDIA_SHOW_LIKE,
                IslandSettings.KEY_ISLAND_NOTIF_PEEK_STYLE,
                IslandSettings.KEY_ISLAND_NOTIF_QUEUE,
                IslandSettings.KEY_ISLAND_NOTIF_QUEUE_STYLE,
                IslandSettings.KEY_ISLAND_NOTIF_QUEUE_INLINE,
                IslandSettings.KEY_ISLAND_NOTIF_SKIP_SILENT,
                IslandSettings.KEY_ISLAND_NOTIF_FOLLOW_DND,
                IslandSettings.KEY_ISLAND_NOTIF_FILTER_APPS,
                IslandSettings.KEY_ISLAND_SHOW_NOTIFICATIONS,
                IslandSettings.KEY_ISLAND_NOTIF_HUB,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_SILENT,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_GROUP,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_ORDER,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_ICONS,
                IslandSettings.KEY_ISLAND_NOTIF_TAP_TO_OPEN,
                IslandSettings.KEY_ISLAND_SHOW_NAVIGATION,
                IslandSettings.KEY_ISLAND_NAVIGATION_ON_NEW,
                IslandSettings.KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE,
                IslandSettings.KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND,
                IslandSettings.KEY_ISLAND_CATCH_UP_ENABLED,
                IslandSettings.KEY_ISLAND_CATCH_UP_TIMEOUT_MS,
                IslandSettings.KEY_ISLAND_OTP_ENABLED,
                IslandSettings.KEY_ISLAND_OTP_TIMEOUT_MS,
            )
        assertEquals(expected, IslandState.PREFERENCE_KEYS)
        assertEquals(40, IslandState.PREFERENCE_KEYS.size)
    }
}
