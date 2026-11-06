package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("snooze_discovered_channels", NotificationSettings.KEY_SNOOZE_DISCOVERED_CHANNELS)
        assertEquals("snooze_blocked_channels", NotificationSettings.KEY_SNOOZE_BLOCKED_CHANNELS)
        assertEquals("snooze_heads_up_enabled", NotificationSettings.KEY_SNOOZE_HEADS_UP_ENABLED)
        assertEquals("auto_dismiss_notifications_enabled", NotificationSettings.KEY_AUTO_DISMISS_ENABLED)
        assertEquals("auto_dismiss_notifications_apps", NotificationSettings.KEY_AUTO_DISMISS_APPS)
        assertEquals("auto_dismiss_notifications_last_minutes", NotificationSettings.KEY_AUTO_DISMISS_LAST_MINUTES)
        assertEquals("auto_dismiss_notifications_channels", NotificationSettings.KEY_AUTO_DISMISS_CHANNELS)
        assertEquals("auto_dismiss_notifications_stats", NotificationSettings.KEY_AUTO_DISMISS_STATS)
        assertEquals("hilight_notifications_enabled", NotificationSettings.KEY_HILIGHT_NOTIFICATIONS_ENABLED)
        assertEquals("hilight_app_effects", NotificationSettings.KEY_HILIGHT_APP_EFFECTS)
        assertEquals("hilight_only_screen_off", NotificationSettings.KEY_HILIGHT_ONLY_SCREEN_OFF)
        assertEquals("hilight_cooldown_seconds", NotificationSettings.KEY_HILIGHT_COOLDOWN_SECONDS)
        assertEquals("hilight_progress_enabled", NotificationSettings.KEY_HILIGHT_PROGRESS_ENABLED)
        assertEquals("hilight_progress_color_mode", NotificationSettings.KEY_HILIGHT_PROGRESS_COLOR_MODE)
        assertEquals("hilight_progress_color", NotificationSettings.KEY_HILIGHT_PROGRESS_COLOR)
        assertEquals("hilight_progress_duration_ms", NotificationSettings.KEY_HILIGHT_PROGRESS_DURATION_MS)
        assertEquals("hilight_skip_dnd", NotificationSettings.KEY_HILIGHT_SKIP_DND)
        assertEquals("edge_lighting_enabled", NotificationSettings.KEY_EDGE_LIGHTING_ENABLED)
        assertEquals("use_ripple", NotificationSettings.KEY_USE_RIPPLE)
    }
}
