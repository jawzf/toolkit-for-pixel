package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.prefs.StatusBarPrefs
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplaySettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("smart_pixels_enabled", DisplaySettings.KEY_SMART_PIXELS_ENABLED)
        assertEquals("smart_pixels_intensity", DisplaySettings.KEY_SMART_PIXELS_INTENSITY)
        assertEquals("smart_pixels_disable_on_cast", DisplaySettings.KEY_SMART_PIXELS_DISABLE_ON_CAST)
        assertEquals("refresh_rate_mode", DisplaySettings.KEY_REFRESH_RATE_MODE)
        assertEquals("refresh_rate_fixed", DisplaySettings.KEY_REFRESH_RATE_FIXED)
        assertEquals("refresh_rate_min", DisplaySettings.KEY_REFRESH_RATE_MIN)
        assertEquals("refresh_rate_peak", DisplaySettings.KEY_REFRESH_RATE_PEAK)
        assertEquals("refresh_rate_default_peak_infinity", DisplaySettings.KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY)
        assertEquals("aod_wallpaper_enabled", DisplaySettings.KEY_AOD_WALLPAPER_ENABLED)
        assertEquals("aod_force_turn_off_enabled", DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED)
        assertEquals("aod_wallpaper_timeout", DisplaySettings.KEY_AOD_WALLPAPER_TIMEOUT)
        assertEquals("notification_glance_enabled", DisplaySettings.KEY_NOTIFICATION_GLANCE_ENABLED)
        assertEquals("ambient_music_glance_enabled", DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED)
        assertEquals("ambient_music_glance_docked_mode", DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE)
        assertEquals("ambient_music_glance_album_art_mode", DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ALBUM_ART_MODE)
        assertEquals("lock_screen_clock_weight", DisplaySettings.KEY_LOCK_SCREEN_CLOCK_WEIGHT)
        assertEquals("lock_screen_clock_width", DisplaySettings.KEY_LOCK_SCREEN_CLOCK_WIDTH)
        assertEquals("lock_screen_clock_grade", DisplaySettings.KEY_LOCK_SCREEN_CLOCK_GRADE)
        assertEquals("lock_screen_clock_roundness", DisplaySettings.KEY_LOCK_SCREEN_CLOCK_ROUNDNESS)
        assertEquals("lock_screen_clock_color_tone", DisplaySettings.KEY_LOCK_SCREEN_CLOCK_COLOR_TONE)
        assertEquals("lock_screen_clock_selected_color_id", DisplaySettings.KEY_LOCK_SCREEN_CLOCK_SELECTED_COLOR_ID)
        assertEquals("lock_screen_clock_seed_color", DisplaySettings.KEY_LOCK_SCREEN_CLOCK_SEED_COLOR)
        assertEquals("status_bar_icon_control_enabled", PlatformPrefs.KEY_STATUS_BAR_ICON_CONTROL_ENABLED)
        assertEquals("hide_clock", PlatformPrefs.KEY_HIDE_CLOCK)
        assertEquals("island_hide_icons", StatusBarPrefs.KEY_ISLAND_HIDE_ICONS)
    }
}
