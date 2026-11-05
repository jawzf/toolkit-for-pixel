package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class AppsSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("freeze_when_locked_enabled", AppsSettings.KEY_FREEZE_WHEN_LOCKED_ENABLED)
        assertEquals("freeze_lock_delay_index", AppsSettings.KEY_FREEZE_LOCK_DELAY_INDEX)
        assertEquals("freeze_auto_excluded_apps", AppsSettings.KEY_FREEZE_AUTO_EXCLUDED_APPS)
        assertEquals("freeze_dont_freeze_active_apps", AppsSettings.KEY_FREEZE_DONT_FREEZE_ACTIVE_APPS)
        assertEquals("freeze_mode", AppsSettings.KEY_FREEZE_MODE)
        assertEquals("freeze_show_in_launcher", AppsSettings.KEY_FREEZE_SHOW_IN_LAUNCHER)
        assertEquals("freeze_tags", AppsSettings.KEY_FREEZE_TAGS)
        assertEquals("freeze_app_tag_map", AppsSettings.KEY_FREEZE_APP_TAG_MAP)
        assertEquals("freeze_tag_color_coded_enabled", AppsSettings.KEY_FREEZE_TAG_COLOR_CODED_ENABLED)
        assertEquals("freeze_selected_apps", AppsSettings.KEY_FREEZE_SELECTED_APPS)
        assertEquals("app_lock_enabled", AppsSettings.KEY_APP_LOCK_ENABLED)
        assertEquals("app_lock_selected_apps", AppsSettings.KEY_APP_LOCK_SELECTED_APPS)
        assertEquals("app_lock_auto_lock_delay_index", AppsSettings.KEY_APP_LOCK_AUTO_LOCK_DELAY_INDEX)
    }
}
