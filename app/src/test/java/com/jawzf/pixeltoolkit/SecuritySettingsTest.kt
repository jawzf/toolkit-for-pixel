package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.security.SecuritySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class SecuritySettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("screenshot_share_cleanup_enabled", SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_ENABLED)
        assertEquals("screenshot_share_cleanup_delay", SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_DELAY)
        assertEquals("screenshot_auto_clean_enabled", SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_ENABLED)
        assertEquals("screenshot_auto_clean_days", SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_DAYS)
        assertEquals("screenshot_auto_clean_last_id", SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_LAST_ID)
        assertEquals("screenshots_cleaned_count", SecuritySettings.KEY_SCREENSHOTS_CLEANED_COUNT)
        assertEquals("screenshots_cleaned_bytes", SecuritySettings.KEY_SCREENSHOTS_CLEANED_BYTES)
    }
}
