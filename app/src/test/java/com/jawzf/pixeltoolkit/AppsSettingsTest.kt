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
        assertEquals("tracked_repos", AppsSettings.KEY_TRACKED_REPOS)
        assertEquals("github_access_token", AppsSettings.KEY_GITHUB_ACCESS_TOKEN)
        assertEquals("github_user_profile", AppsSettings.KEY_GITHUB_USER_PROFILE)
        assertEquals("conscious_gate_enabled", AppsSettings.KEY_CONSCIOUS_GATE_ENABLED)
        assertEquals("conscious_gate_selected_apps", AppsSettings.KEY_CONSCIOUS_GATE_SELECTED_APPS)
        assertEquals("conscious_gate_delay_seconds", AppsSettings.KEY_CONSCIOUS_GATE_DELAY_SECONDS)
        assertEquals("conscious_gate_reappear_minutes", AppsSettings.KEY_CONSCIOUS_GATE_REAPPEAR_MINUTES)
        assertEquals("conscious_gate_feel_every_second", AppsSettings.KEY_CONSCIOUS_GATE_FEEL_EVERY_SECOND)
        assertEquals("conscious_gate_title", AppsSettings.KEY_CONSCIOUS_GATE_TITLE)
        assertEquals("conscious_gate_message", AppsSettings.KEY_CONSCIOUS_GATE_MESSAGE)
    }
}
