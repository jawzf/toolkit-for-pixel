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
    }
}
