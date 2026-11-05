package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.power.PowerSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class PowerSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("bluetooth_devices_battery", PowerSettings.KEY_BLUETOOTH_DEVICES_BATTERY)
        assertEquals("show_bluetooth_devices", PowerSettings.KEY_SHOW_BLUETOOTH_DEVICES)
        assertEquals("battery_separate_channels", PowerSettings.KEY_BATTERY_SEPARATE_CHANNELS)
        assertEquals("battery_show_percentage", PowerSettings.KEY_BATTERY_SHOW_PERCENTAGE)
        assertEquals("battery_widget_center_items", PowerSettings.KEY_BATTERY_WIDGET_CENTER_ITEMS)
        assertEquals("battery_widget_max_devices", PowerSettings.KEY_BATTERY_WIDGET_MAX_DEVICES)
        assertEquals("battery_widget_background_enabled", PowerSettings.KEY_BATTERY_WIDGET_BACKGROUND_ENABLED)
        assertEquals("battery_notification_enabled", PowerSettings.KEY_BATTERY_NOTIFICATION_ENABLED)
    }
}
