package com.jawzf.pixeltoolkit.feature.display.wallpaper

import android.content.Context
import android.content.SharedPreferences

class LiveWallpaperSettings(
    private val context: Context,
) {
    private val liveWallpaperPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(LIVE_WALLPAPER_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        liveWallpaperPrefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        liveWallpaperPrefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    /**
     * Executes the get live wallpaper selected video operation.
     * @return The resulting String data.
     */
    fun getLiveWallpaperSelectedVideo(): String =
        liveWallpaperPrefs.getString(
            KEY_LIVE_WALLPAPER_SELECTED_VIDEO,
            LIVE_WALLPAPER_DEFAULT_VIDEO,
        )
            ?: LIVE_WALLPAPER_DEFAULT_VIDEO

    /**
     * Executes the save live wallpaper selected video operation.
     *
     * @param video [String] Target video.
     */
    fun saveLiveWallpaperSelectedVideo(video: String) =
        liveWallpaperPrefs.edit().putString(KEY_LIVE_WALLPAPER_SELECTED_VIDEO, video).apply()

    /**
     * Executes the get live wallpaper playback trigger operation.
     * @return The resulting String data.
     */
    fun getLiveWallpaperPlaybackTrigger(): String =
        liveWallpaperPrefs.getString(
            KEY_LIVE_WALLPAPER_PLAYBACK_TRIGGER,
            LIVE_WALLPAPER_TRIGGER_UNLOCK,
        )
            ?: LIVE_WALLPAPER_TRIGGER_UNLOCK

    /**
     * Executes the save live wallpaper playback trigger operation.
     *
     * @param trigger [String] Target trigger.
     */
    fun saveLiveWallpaperPlaybackTrigger(trigger: String) =
        liveWallpaperPrefs.edit().putString(KEY_LIVE_WALLPAPER_PLAYBACK_TRIGGER, trigger).apply()

    /**
     * Executes the get live wallpaper custom videos operation.
     * @return The resulting List<String> data.
     */
    fun getLiveWallpaperCustomVideos(): List<String> {
        val stored = liveWallpaperPrefs.getString(KEY_LIVE_WALLPAPER_CUSTOM_VIDEOS, "") ?: ""
        val delimiter = if (!stored.contains("\n") && stored.contains(",")) "," else "\n"
        return stored
            .split(delimiter)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    /**
     * Executes the save live wallpaper custom videos operation.
     *
     * @param videos [List<String>] Target videos.
     */
    fun saveLiveWallpaperCustomVideos(videos: List<String>) =
        liveWallpaperPrefs
            .edit()
            .putString(
                KEY_LIVE_WALLPAPER_CUSTOM_VIDEOS,
                videos.filter { it.isNotEmpty() }.distinct().joinToString("\n"),
            ).apply()

    /**
     * Executes the add live wallpaper custom video operation.
     *
     * @param uri [String] Target uri.
     */
    fun addLiveWallpaperCustomVideo(uri: String) {
        val current = getLiveWallpaperCustomVideos().toMutableList()
        if (current.contains(uri)) {
            current.remove(uri)
        }
        current.add(0, uri)
        saveLiveWallpaperCustomVideos(if (current.size > 5) current.take(5) else current)
    }

    /**
     * Executes the get live wallpaper available videos operation.
     * @return The resulting List<String> data.
     */
    fun getLiveWallpaperAvailableVideos(): List<String> {
        val raws =
            com.jawzf.pixeltoolkit.core.design.R.raw::class.java.fields.mapNotNull { field ->
                try {
                    if (field.name.startsWith("loop_")) field.name else null
                } catch (e: Exception) {
                    null
                }
            }
        return (raws + getLiveWallpaperCustomVideos()).filter { it.isNotBlank() }.distinct()
    }

    /**
     * Executes the remove live wallpaper custom video operation.
     *
     * @param videoUri [String] Target video uri.
     */
    fun removeLiveWallpaperCustomVideo(videoUri: String) {
        val current = getLiveWallpaperCustomVideos().toMutableList()
        val removed = current.removeAll { it == videoUri || it.isBlank() }
        if (removed) {
            saveLiveWallpaperCustomVideos(current)
            // If the removed video was selected, revert to default
            if (getLiveWallpaperSelectedVideo() == videoUri) {
                saveLiveWallpaperSelectedVideo(LIVE_WALLPAPER_DEFAULT_VIDEO)
            }
        }
    }

    companion object {
        const val LIVE_WALLPAPER_PREFS_NAME = "live_wallpaper_prefs"
        const val KEY_LIVE_WALLPAPER_SELECTED_VIDEO = "selected_video"
        const val KEY_LIVE_WALLPAPER_PLAYBACK_TRIGGER = "playback_trigger"
        const val KEY_LIVE_WALLPAPER_CUSTOM_VIDEOS = "custom_videos"
        const val LIVE_WALLPAPER_DEFAULT_VIDEO = "my_video"
        const val LIVE_WALLPAPER_TRIGGER_UNLOCK = "unlock"
        const val LIVE_WALLPAPER_TRIGGER_SCREEN_ON = "screen_on"
    }
}
