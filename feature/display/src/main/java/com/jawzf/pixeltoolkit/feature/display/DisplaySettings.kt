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

    companion object {
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
    }
}
