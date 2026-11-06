package com.jawzf.pixeltoolkit.feature.display

import android.content.Context
import android.provider.Settings
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRateUtils

class DisplaySettings(
    context: Context,
) : PrefsStore(context) {
    fun getRefreshRateMode(): String =
        getString(KEY_REFRESH_RATE_MODE, RefreshRateUtils.MODE_FIXED)
            ?: RefreshRateUtils.MODE_FIXED

    fun setRefreshRateMode(mode: String) = putString(KEY_REFRESH_RATE_MODE, mode)

    fun saveRefreshRateState(
        mode: String,
        fixed: Float,
        min: Float,
        peak: Float,
    ) {
        putString(KEY_REFRESH_RATE_MODE, mode)
        putFloat(KEY_REFRESH_RATE_FIXED, fixed)
        putFloat(KEY_REFRESH_RATE_MIN, min)
        putFloat(KEY_REFRESH_RATE_PEAK, peak)
    }

    fun shouldRestoreInfinityPeakOnRefreshRateReset(): Boolean = getBoolean(KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY, false)

    fun setRestoreInfinityPeakOnRefreshRateReset(enabled: Boolean) = putBoolean(KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY, enabled)

    fun loadNotificationGlanceSelectedApps(): List<AppSelection> = loadAppSelection(KEY_NOTIFICATION_GLANCE_SELECTED_APPS)

    fun saveNotificationGlanceSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_NOTIFICATION_GLANCE_SELECTED_APPS, apps)

    fun updateNotificationGlanceAppSelection(
        packageName: String,
        enabled: Boolean,
    ) {
        saveNotificationGlanceSelectedApps(
            withSelection(loadNotificationGlanceSelectedApps(), packageName, enabled),
        )
    }

    fun loadAodWallpaperMediaExcludedApps(): List<AppSelection> = loadAppSelection(KEY_AOD_WALLPAPER_MEDIA_EXCLUDED_APPS)

    fun saveAodWallpaperMediaExcludedApps(apps: List<AppSelection>) = saveAppSelection(KEY_AOD_WALLPAPER_MEDIA_EXCLUDED_APPS, apps)

    fun updateAodWallpaperMediaExcludedAppSelection(
        packageName: String,
        enabled: Boolean,
    ) {
        saveAodWallpaperMediaExcludedApps(
            withSelection(loadAodWallpaperMediaExcludedApps(), packageName, enabled),
        )
    }

    /**
     * Executes the load dynamic night light selected apps operation.
     */
    fun loadDynamicNightLightSelectedApps(): List<AppSelection> = loadAppSelection(KEY_DYNAMIC_NIGHT_LIGHT_SELECTED_APPS)

    /**
     * Executes the save dynamic night light selected apps operation.
     *
     * @param apps [List<AppSelection>] Target apps.
     */
    fun saveDynamicNightLightSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_DYNAMIC_NIGHT_LIGHT_SELECTED_APPS, apps)

    /**
     * Executes the update dynamic night light app selection operation.
     *
     * @param packageName [String] Target package name.
     * @param enabled [Boolean] Target enabled.
     */
    fun updateDynamicNightLightAppSelection(
        packageName: String,
        enabled: Boolean,
    ) {
        saveDynamicNightLightSelectedApps(
            withSelection(loadDynamicNightLightSelectedApps(), packageName, enabled),
        )
    }

    private fun withSelection(
        current: List<AppSelection>,
        packageName: String,
        enabled: Boolean,
    ): List<AppSelection> {
        val updated = current.toMutableList()
        val index = updated.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            updated[index] = updated[index].copy(isEnabled = enabled)
        } else {
            updated.add(AppSelection(packageName, enabled))
        }
        return updated
    }

    fun isAodEnabled(): Boolean =
        Settings.Secure.getInt(
            context.contentResolver,
            "doze_always_on",
            1,
        ) == 1

    fun setAodEnabled(enabled: Boolean) {
        try {
            Settings.Secure.putInt(
                context.contentResolver,
                "doze_always_on",
                if (enabled) 1 else 0,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isAodWallpaperEnabled(): Boolean = prefs.getBoolean(KEY_AOD_WALLPAPER_ENABLED, false)

    fun setAodWallpaperEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AOD_WALLPAPER_ENABLED, enabled).apply()
    }

    fun getAodWallpaperOpacity(): Float = getFloat(KEY_AOD_WALLPAPER_OPACITY, 0.3f)

    fun setAodWallpaperOpacity(value: Float) = putFloat(KEY_AOD_WALLPAPER_OPACITY, value)

    fun getAodWallpaperTimeout(): String =
        try {
            getString(KEY_AOD_WALLPAPER_TIMEOUT, "3m") ?: "3m"
        } catch (_: Exception) {
            val oldInt = getInt(KEY_AOD_WALLPAPER_TIMEOUT, 3)
            val strVal =
                when (oldInt) {
                    0 -> "never"
                    1 -> "1m"
                    3 -> "3m"
                    5 -> "5m"
                    10 -> "10m"
                    else -> "${oldInt}m"
                }
            setAodWallpaperTimeout(strVal)
            strVal
        }

    fun setAodWallpaperTimeout(value: String) = putString(KEY_AOD_WALLPAPER_TIMEOUT, value)

    fun getAodWallpaperCustomTimeout(): Float = getFloat(KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT, 30f)

    fun setAodWallpaperCustomTimeout(value: Float) = putFloat(KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT, value)

    fun getAodWallpaperBlur(): Float = getFloat(KEY_AOD_WALLPAPER_BLUR, 0f)

    fun setAodWallpaperBlur(value: Float) = putFloat(KEY_AOD_WALLPAPER_BLUR, value)

    fun getAodWallpaperVignette(): Float = getFloat(KEY_AOD_WALLPAPER_VIGNETTE, 0f)

    fun setAodWallpaperVignette(value: Float) = putFloat(KEY_AOD_WALLPAPER_VIGNETTE, value)

    fun getAodWallpaperBlackThreshold(): Float = getFloat(KEY_AOD_WALLPAPER_BLACK_THRESHOLD, 15f)

    fun setAodWallpaperBlackThreshold(value: Float) = putFloat(KEY_AOD_WALLPAPER_BLACK_THRESHOLD, value)

    fun hasAodWallpaperCustomImage(): Boolean = getBoolean(KEY_AOD_WALLPAPER_CUSTOM_IMAGE, false)

    fun setAodWallpaperCustomImage(hasCustomImage: Boolean) = putBoolean(KEY_AOD_WALLPAPER_CUSTOM_IMAGE, hasCustomImage)

    fun isAodWallpaperUseAlbumArtEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_USE_ALBUM_ART, false)

    fun setAodWallpaperUseAlbumArt(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_USE_ALBUM_ART, enabled)

    fun isAodWallpaperKeepOnMediaEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_KEEP_ON_MEDIA, false)

    fun setAodWallpaperKeepOnMedia(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_KEEP_ON_MEDIA, enabled)

    fun isAodWallpaperExtendedMediaEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_EXTENDED_MEDIA, false)

    fun setAodWallpaperExtendedMedia(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_EXTENDED_MEDIA, enabled)

    fun isAodWallpaperExtendedAppIconEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_EXTENDED_APP_ICON, false)

    fun setAodWallpaperExtendedAppIcon(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_EXTENDED_APP_ICON, enabled)

    fun getAodWallpaperExtendedTextScale(): Float = getFloat(KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE, 1f)

    fun setAodWallpaperExtendedTextScale(value: Float) = putFloat(KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE, value)

    /**
     * Executes the get ambient music glance album art mode operation.
     * @return The resulting String data.
     */
    fun getAmbientMusicGlanceAlbumArtMode(): String = prefs.getString(KEY_AMBIENT_MUSIC_GLANCE_ALBUM_ART_MODE, "default") ?: "default"

    /**
     * Executes the set ambient music glance album art mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setAmbientMusicGlanceAlbumArtMode(mode: String) = prefs.edit().putString(KEY_AMBIENT_MUSIC_GLANCE_ALBUM_ART_MODE, mode).apply()

    /**
     * Executes the get ambient music glance clock size operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockSize(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_SIZE, 80)

    /**
     * Executes the set ambient music glance clock size operation.
     *
     * @param size [Int] Target size.
     */
    fun setAmbientMusicGlanceClockSize(size: Int) = prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_SIZE, size).apply()

    /**
     * Executes the get ambient music glance clock weight operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockWeight(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WEIGHT, 400)

    /**
     * Executes the set ambient music glance clock weight operation.
     *
     * @param weight [Int] Target weight.
     */
    fun setAmbientMusicGlanceClockWeight(weight: Int) = prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WEIGHT, weight).apply()

    /**
     * Executes the get ambient music glance clock width operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockWidth(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WIDTH, 100)

    /**
     * Executes the set ambient music glance clock width operation.
     *
     * @param width [Int] Target width.
     */
    fun setAmbientMusicGlanceClockWidth(width: Int) = prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WIDTH, width).apply()

    /**
     * Executes the get ambient music glance clock roundness operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockRoundness(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_ROUNDNESS, 50)

    /**
     * Executes the set ambient music glance clock roundness operation.
     *
     * @param roundness [Int] Target roundness.
     */
    fun setAmbientMusicGlanceClockRoundness(roundness: Int) =
        prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_ROUNDNESS, roundness).apply()

    /**
     * Executes the is ambient music glance force fill while charging enabled operation.
     * @return The resulting Boolean data.
     */
    fun isAmbientMusicGlanceForceFillWhileChargingEnabled(): Boolean =
        prefs.getBoolean(KEY_AMBIENT_MUSIC_GLANCE_FORCE_FILL_WHILE_CHARGING, false)

    /**
     * Executes the set ambient music glance force fill while charging enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAmbientMusicGlanceForceFillWhileChargingEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_AMBIENT_MUSIC_GLANCE_FORCE_FILL_WHILE_CHARGING, enabled).apply()

    /**
     * Executes the is ambient music glance respect notifications enabled operation.
     * @return The resulting Boolean data.
     */
    fun isAmbientMusicGlanceRespectNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_AMBIENT_MUSIC_GLANCE_RESPECT_NOTIFICATIONS, true)

    /**
     * Executes the set ambient music glance respect notifications enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAmbientMusicGlanceRespectNotificationsEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_AMBIENT_MUSIC_GLANCE_RESPECT_NOTIFICATIONS, enabled).apply()

    /**
     * Executes the get lock screen clock weight operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockWeight(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_WEIGHT, 300)

    /**
     * Executes the set lock screen clock weight operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockWeight(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_WEIGHT, value)

    /**
     * Executes the get lock screen clock width operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockWidth(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_WIDTH, 116)

    /**
     * Executes the set lock screen clock width operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockWidth(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_WIDTH, value)

    /**
     * Executes the get lock screen clock grade operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockGrade(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_GRADE, 0)

    /**
     * Executes the set lock screen clock grade operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockGrade(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_GRADE, value)

    /**
     * Executes the get lock screen clock roundness operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockRoundness(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_ROUNDNESS, 100)

    /**
     * Executes the set lock screen clock roundness operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockRoundness(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_ROUNDNESS, value)

    /**
     * Executes the get lock screen clock color tone operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockColorTone(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_COLOR_TONE, 75)

    /**
     * Executes the set lock screen clock color tone operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockColorTone(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_COLOR_TONE, value)

    /**
     * Executes the get lock screen clock selected color id operation.
     * @return The resulting String data.
     */
    fun getLockScreenClockSelectedColorId(): String = getString(KEY_LOCK_SCREEN_CLOCK_SELECTED_COLOR_ID, "DEFAULT") ?: "DEFAULT"

    /**
     * Executes the set lock screen clock selected color id operation.
     *
     * @param value [String] Target value.
     */
    fun setLockScreenClockSelectedColorId(value: String) = putString(KEY_LOCK_SCREEN_CLOCK_SELECTED_COLOR_ID, value)

    /**
     * Executes the get lock screen clock seed color operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockSeedColor(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_SEED_COLOR, 0)

    /**
     * Executes the set lock screen clock seed color operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockSeedColor(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_SEED_COLOR, value)

    companion object {
        const val KEY_DYNAMIC_NIGHT_LIGHT_ENABLED = "dynamic_night_light_enabled"
        const val KEY_DYNAMIC_NIGHT_LIGHT_SELECTED_APPS = "dynamic_night_light_selected_apps"
        const val KEY_SMART_PIXELS_ENABLED = "smart_pixels_enabled"
        const val KEY_SMART_PIXELS_INTENSITY = "smart_pixels_intensity"
        const val KEY_SMART_PIXELS_DISABLE_ON_CAST = "smart_pixels_disable_on_cast"
        const val KEY_REFRESH_RATE_MODE = "refresh_rate_mode"
        const val KEY_REFRESH_RATE_FIXED = "refresh_rate_fixed"
        const val KEY_REFRESH_RATE_MIN = "refresh_rate_min"
        const val KEY_REFRESH_RATE_PEAK = "refresh_rate_peak"
        const val KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY = "refresh_rate_default_peak_infinity"
        const val KEY_NOTIFICATION_GLANCE_ENABLED = "notification_glance_enabled"
        const val KEY_NOTIFICATION_GLANCE_SAME_AS_LIGHTING = "notification_glance_same_as_lighting"
        const val KEY_NOTIFICATION_GLANCE_SELECTED_APPS = "notification_glance_selected_apps"
        const val KEY_AOD_FORCE_TURN_OFF_ENABLED = "aod_force_turn_off_enabled"
        const val KEY_AOD_WALLPAPER_ENABLED = "aod_wallpaper_enabled"
        const val KEY_AOD_WALLPAPER_OPACITY = "aod_wallpaper_opacity"
        const val KEY_AOD_WALLPAPER_TIMEOUT = "aod_wallpaper_timeout"
        const val KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT = "aod_wallpaper_custom_timeout"
        const val KEY_AOD_WALLPAPER_BLUR = "aod_wallpaper_blur"
        const val KEY_AOD_WALLPAPER_VIGNETTE = "aod_wallpaper_vignette"
        const val KEY_AOD_WALLPAPER_BLACK_THRESHOLD = "aod_wallpaper_black_threshold"
        const val KEY_AOD_WALLPAPER_CUSTOM_IMAGE = "aod_wallpaper_custom_image"
        const val KEY_AOD_WALLPAPER_USE_ALBUM_ART = "aod_wallpaper_use_album_art"
        const val KEY_AOD_WALLPAPER_DISABLE_ON_DND = "aod_wallpaper_disable_on_dnd"
        const val KEY_AOD_WALLPAPER_KEEP_ON_MEDIA = "aod_wallpaper_keep_on_media"
        const val KEY_AOD_WALLPAPER_MEDIA_EXCLUDED_APPS = "aod_wallpaper_media_excluded_apps"
        const val KEY_AOD_WALLPAPER_EXTENDED_MEDIA = "aod_wallpaper_extended_media"
        const val KEY_AOD_WALLPAPER_EXTENDED_APP_ICON = "aod_wallpaper_extended_app_icon"
        const val KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE = "aod_wallpaper_extended_text_scale"
        const val KEY_AMBIENT_MUSIC_GLANCE_ENABLED = "ambient_music_glance_enabled"
        const val KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE = "ambient_music_glance_docked_mode"
        const val KEY_AMBIENT_MUSIC_GLANCE_RANDOM_SHAPES = "ambient_music_glance_random_shapes"
        const val KEY_AMBIENT_MUSIC_GLANCE_ALBUM_ART_MODE = "ambient_music_glance_album_art_mode"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_SIZE = "ambient_music_glance_clock_size"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WEIGHT = "ambient_music_glance_clock_weight"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WIDTH = "ambient_music_glance_clock_width"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_ROUNDNESS = "ambient_music_glance_clock_roundness"
        const val KEY_AMBIENT_MUSIC_GLANCE_FORCE_FILL_WHILE_CHARGING =
            "ambient_music_glance_force_fill_while_charging"
        const val KEY_AMBIENT_MUSIC_GLANCE_RESPECT_NOTIFICATIONS =
            "ambient_music_glance_respect_notifications"
        const val KEY_LOCK_SCREEN_CLOCK_WEIGHT = "lock_screen_clock_weight"
        const val KEY_LOCK_SCREEN_CLOCK_WIDTH = "lock_screen_clock_width"
        const val KEY_LOCK_SCREEN_CLOCK_GRADE = "lock_screen_clock_grade"
        const val KEY_LOCK_SCREEN_CLOCK_ROUNDNESS = "lock_screen_clock_roundness"
        const val KEY_LOCK_SCREEN_CLOCK_COLOR_TONE = "lock_screen_clock_color_tone"
        const val KEY_LOCK_SCREEN_CLOCK_SELECTED_COLOR_ID = "lock_screen_clock_selected_color_id"
        const val KEY_LOCK_SCREEN_CLOCK_SEED_COLOR = "lock_screen_clock_seed_color"
    }
}
