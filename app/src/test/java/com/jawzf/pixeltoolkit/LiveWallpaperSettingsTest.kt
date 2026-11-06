package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.display.wallpaper.LiveWallpaperSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class LiveWallpaperSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("live_wallpaper_prefs", LiveWallpaperSettings.LIVE_WALLPAPER_PREFS_NAME)
        assertEquals("selected_video", LiveWallpaperSettings.KEY_LIVE_WALLPAPER_SELECTED_VIDEO)
        assertEquals("playback_trigger", LiveWallpaperSettings.KEY_LIVE_WALLPAPER_PLAYBACK_TRIGGER)
        assertEquals("custom_videos", LiveWallpaperSettings.KEY_LIVE_WALLPAPER_CUSTOM_VIDEOS)
    }
}
