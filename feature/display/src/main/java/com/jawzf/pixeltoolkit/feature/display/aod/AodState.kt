package com.jawzf.pixeltoolkit.feature.display.aod

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.apps.AppUtil
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class AodState(
    private val scope: CoroutineScope,
) {
    private lateinit var settings: DisplaySettings

    val isAodEnabled = mutableStateOf(false)
    val isNotificationGlanceEnabled = mutableStateOf(false)
    val isAodForceTurnOffEnabled = mutableStateOf(false)
    val isAodWallpaperEnabled = mutableStateOf(false)
    val aodWallpaperOpacity = mutableFloatStateOf(0.3f)
    val aodWallpaperTimeout = mutableStateOf("3m")
    val aodWallpaperCustomTimeout = mutableFloatStateOf(30f)
    val aodWallpaperBlur = mutableFloatStateOf(0f)
    val aodWallpaperVignette = mutableFloatStateOf(0f)
    val aodWallpaperBlackThreshold = mutableFloatStateOf(15f)
    val hasAodWallpaperCustomImage = mutableStateOf(false)
    val isAodWallpaperUseAlbumArt = mutableStateOf(false)
    val isAodWallpaperDisableOnDnd = mutableStateOf(false)
    val isAodWallpaperKeepOnMedia = mutableStateOf(false)
    val isAodWallpaperExtendedMedia = mutableStateOf(false)
    val isAodWallpaperExtendedAppIcon = mutableStateOf(false)
    val aodWallpaperExtendedTextScale = mutableFloatStateOf(1f)
    val currentWallpaperBitmap = mutableStateOf<Bitmap?>(null)
    val isNotificationGlanceSameAsLightingEnabled = mutableStateOf(true)

    fun load(context: Context) {
        settings = DisplaySettings(context)
        isAodEnabled.value = settings.isAodEnabled()
        isNotificationGlanceEnabled.value =
            settings.getBoolean(DisplaySettings.KEY_NOTIFICATION_GLANCE_ENABLED)
        isAodForceTurnOffEnabled.value =
            settings.getBoolean(DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED)
        isAodWallpaperEnabled.value =
            settings.getBoolean(DisplaySettings.KEY_AOD_WALLPAPER_ENABLED)
        aodWallpaperOpacity.floatValue =
            settings.getAodWallpaperOpacity()
        aodWallpaperTimeout.value =
            settings.getAodWallpaperTimeout()
        aodWallpaperCustomTimeout.floatValue =
            settings.getAodWallpaperCustomTimeout()
        aodWallpaperBlur.floatValue =
            settings.getAodWallpaperBlur()
        aodWallpaperVignette.floatValue =
            settings.getAodWallpaperVignette()
        aodWallpaperBlackThreshold.floatValue =
            settings.getAodWallpaperBlackThreshold()
        hasAodWallpaperCustomImage.value =
            settings.hasAodWallpaperCustomImage()
        isAodWallpaperUseAlbumArt.value =
            settings.isAodWallpaperUseAlbumArtEnabled()
        isAodWallpaperDisableOnDnd.value =
            settings.getBoolean(
                DisplaySettings.KEY_AOD_WALLPAPER_DISABLE_ON_DND,
                false,
            )
        isAodWallpaperKeepOnMedia.value =
            settings.isAodWallpaperKeepOnMediaEnabled()
        isAodWallpaperExtendedMedia.value =
            settings.isAodWallpaperExtendedMediaEnabled()
        isAodWallpaperExtendedAppIcon.value =
            settings.isAodWallpaperExtendedAppIconEnabled()
        aodWallpaperExtendedTextScale.floatValue =
            settings.getAodWallpaperExtendedTextScale()
        isNotificationGlanceSameAsLightingEnabled.value =
            settings.getBoolean(
                DisplaySettings.KEY_NOTIFICATION_GLANCE_SAME_AS_LIGHTING,
                true,
            )
    }

    fun syncAodEnabled() {
        isAodEnabled.value = settings.isAodEnabled()
    }

    fun setAodEnabled(enabled: Boolean) {
        isAodEnabled.value = enabled
        settings.setAodEnabled(enabled)
    }

    fun toggleNotificationGlanceEnabled(enabled: Boolean) {
        settings.putBoolean(DisplaySettings.KEY_NOTIFICATION_GLANCE_ENABLED, enabled)
        isNotificationGlanceEnabled.value = enabled
    }

    fun toggleAodForceTurnOffEnabled(enabled: Boolean) {
        settings.putBoolean(DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED, enabled)
        isAodForceTurnOffEnabled.value = enabled
    }

    fun toggleAodWallpaperEnabled(enabled: Boolean) {
        settings.setAodWallpaperEnabled(enabled)
        isAodWallpaperEnabled.value = enabled
    }

    fun setAodWallpaperOpacity(opacity: Float) {
        settings.setAodWallpaperOpacity(opacity)
        aodWallpaperOpacity.floatValue = opacity
    }

    fun setAodWallpaperTimeout(timeout: String) {
        settings.setAodWallpaperTimeout(timeout)
        aodWallpaperTimeout.value = timeout
    }

    fun setAodWallpaperCustomTimeout(seconds: Float) {
        settings.setAodWallpaperCustomTimeout(seconds)
        aodWallpaperCustomTimeout.floatValue = seconds
    }

    fun setAodWallpaperBlur(radius: Float) {
        settings.setAodWallpaperBlur(radius)
        aodWallpaperBlur.floatValue = radius
    }

    fun setAodWallpaperVignette(intensity: Float) {
        settings.setAodWallpaperVignette(intensity)
        aodWallpaperVignette.floatValue = intensity
    }

    fun setAodWallpaperBlackThreshold(threshold: Float) {
        settings.setAodWallpaperBlackThreshold(threshold)
        aodWallpaperBlackThreshold.floatValue = threshold
    }

    fun setAodWallpaperDisableOnDnd(enabled: Boolean) {
        settings.putBoolean(
            DisplaySettings.KEY_AOD_WALLPAPER_DISABLE_ON_DND,
            enabled,
        )
        isAodWallpaperDisableOnDnd.value = enabled
    }

    fun setAodWallpaperUseAlbumArt(enabled: Boolean) {
        settings.setAodWallpaperUseAlbumArt(enabled)
        isAodWallpaperUseAlbumArt.value = enabled
    }

    fun setAodWallpaperKeepOnMedia(enabled: Boolean) {
        settings.setAodWallpaperKeepOnMedia(enabled)
        isAodWallpaperKeepOnMedia.value = enabled
    }

    fun setAodWallpaperExtendedMedia(enabled: Boolean) {
        settings.setAodWallpaperExtendedMedia(enabled)
        isAodWallpaperExtendedMedia.value = enabled
    }

    fun setAodWallpaperExtendedTextScale(value: Float) {
        settings.setAodWallpaperExtendedTextScale(value)
        aodWallpaperExtendedTextScale.floatValue = value
    }

    fun setAodWallpaperExtendedAppIcon(enabled: Boolean) {
        settings.setAodWallpaperExtendedAppIcon(enabled)
        isAodWallpaperExtendedAppIcon.value = enabled
    }

    fun loadAodWallpaperMediaApps(): List<AppSelection> = settings.loadAodWallpaperMediaExcludedApps()

    fun saveAodWallpaperMediaApps(apps: List<AppSelection>) {
        settings.saveAodWallpaperMediaExcludedApps(apps)
    }

    fun updateAodWallpaperMediaAppEnabled(
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateAodWallpaperMediaExcludedAppSelection(packageName, enabled)
    }

    fun setCustomAodWallpaper(
        context: Context,
        uri: Uri,
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "custom_aod_wallpaper.png")
                val sampledBitmap = AppUtil.decodeSampledBitmapFromUri(context, uri, reqWidth = 1440, reqHeight = 3200)
                if (sampledBitmap != null) {
                    FileOutputStream(file).use { output ->
                        sampledBitmap.compress(Bitmap.CompressFormat.PNG, 95, output)
                    }
                    settings.setAodWallpaperCustomImage(true)
                    withContext(Dispatchers.Main) {
                        hasAodWallpaperCustomImage.value = true
                        currentWallpaperBitmap.value = sampledBitmap
                    }
                }
            } catch (e: Throwable) {
                Log.e("AodState", "Failed to save custom AOD wallpaper", e)
            }
        }
    }

    fun removeCustomAodWallpaper(context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, "custom_aod_wallpaper.png")
                if (file.exists()) {
                    file.delete()
                }
                settings.setAodWallpaperCustomImage(false)
                withContext(Dispatchers.Main) {
                    hasAodWallpaperCustomImage.value = false
                }
                loadCurrentWallpaperBitmap(context)
            } catch (e: Throwable) {
                Log.e("AodState", "Failed to remove custom AOD wallpaper", e)
            }
        }
    }

    fun loadCurrentWallpaperBitmap(context: Context) {
        scope.launch(Dispatchers.IO) {
            try {
                if (settings.hasAodWallpaperCustomImage()) {
                    val file = File(context.filesDir, "custom_aod_wallpaper.png")
                    if (file.exists()) {
                        val bitmap = AppUtil.decodeSampledBitmapFromFile(file.absolutePath, reqWidth = 1440, reqHeight = 3200)
                        if (bitmap != null) {
                            withContext(Dispatchers.Main) {
                                currentWallpaperBitmap.value = bitmap
                            }
                            return@launch
                        }
                    }
                }

                val bitmap = loadSystemWallpaper(context)
                if (bitmap != null) {
                    withContext(Dispatchers.Main) {
                        currentWallpaperBitmap.value = bitmap
                    }
                }
            } catch (_: Throwable) {
            }
        }
    }

    private fun loadSystemWallpaper(context: Context): Bitmap? {
        if (!PermissionUtils.hasManageExternalStoragePermission(context)) return null
        val wallpaperManager = WallpaperManager.getInstance(context)
        val drawable =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                wallpaperManager.getDrawable(WallpaperManager.FLAG_LOCK)
                    ?: wallpaperManager.drawable
            } else {
                wallpaperManager.drawable
            } ?: return null
        return drawableToBitmap(drawable)
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val orig = drawable.bitmap
            if (orig.width > 2560 || orig.height > 2560) {
                val scale = minOf(1440f / orig.width, 3200f / orig.height, 1.0f)
                if (scale < 1.0f) {
                    return Bitmap.createScaledBitmap(
                        orig,
                        (orig.width * scale).toInt(),
                        (orig.height * scale).toInt(),
                        true,
                    )
                }
            }
            return orig
        }
        val width = if (drawable.intrinsicWidth in 1..2560) drawable.intrinsicWidth else 1080
        val height = if (drawable.intrinsicHeight in 1..3200) drawable.intrinsicHeight else 2400
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bmp
    }

    fun setNotificationGlanceSameAsLightingEnabled(enabled: Boolean) {
        isNotificationGlanceSameAsLightingEnabled.value = enabled
        settings.putBoolean(
            DisplaySettings.KEY_NOTIFICATION_GLANCE_SAME_AS_LIGHTING,
            enabled,
        )
    }

    fun loadNotificationGlanceSelectedApps(): List<AppSelection> = settings.loadNotificationGlanceSelectedApps()

    fun saveNotificationGlanceSelectedApps(apps: List<AppSelection>) {
        settings.saveNotificationGlanceSelectedApps(apps)
    }

    fun updateNotificationGlanceAppEnabled(
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateNotificationGlanceAppSelection(packageName, enabled)
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            DisplaySettings.KEY_NOTIFICATION_GLANCE_ENABLED ->
                isNotificationGlanceEnabled.value =
                    settings.getBoolean(key)

            DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED ->
                isAodForceTurnOffEnabled.value =
                    settings.getBoolean(key)

            DisplaySettings.KEY_NOTIFICATION_GLANCE_SAME_AS_LIGHTING ->
                isNotificationGlanceSameAsLightingEnabled.value =
                    settings.getBoolean(key, true)

            DisplaySettings.KEY_AOD_WALLPAPER_ENABLED ->
                isAodWallpaperEnabled.value =
                    settings.getBoolean(key)

            else -> onWallpaperPreferenceChanged(key)
        }
    }

    private fun onWallpaperPreferenceChanged(key: String) {
        when (key) {
            DisplaySettings.KEY_AOD_WALLPAPER_OPACITY ->
                aodWallpaperOpacity.floatValue =
                    settings.getFloat(key, 0.3f)

            DisplaySettings.KEY_AOD_WALLPAPER_TIMEOUT ->
                aodWallpaperTimeout.value =
                    settings.getAodWallpaperTimeout()

            DisplaySettings.KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT ->
                aodWallpaperCustomTimeout.floatValue =
                    settings.getAodWallpaperCustomTimeout()

            DisplaySettings.KEY_AOD_WALLPAPER_BLUR ->
                aodWallpaperBlur.floatValue =
                    settings.getAodWallpaperBlur()

            DisplaySettings.KEY_AOD_WALLPAPER_VIGNETTE ->
                aodWallpaperVignette.floatValue =
                    settings.getAodWallpaperVignette()

            DisplaySettings.KEY_AOD_WALLPAPER_BLACK_THRESHOLD ->
                aodWallpaperBlackThreshold.floatValue =
                    settings.getAodWallpaperBlackThreshold()

            DisplaySettings.KEY_AOD_WALLPAPER_CUSTOM_IMAGE ->
                hasAodWallpaperCustomImage.value =
                    settings.hasAodWallpaperCustomImage()

            else -> onWallpaperMediaPreferenceChanged(key)
        }
    }

    private fun onWallpaperMediaPreferenceChanged(key: String) {
        when (key) {
            DisplaySettings.KEY_AOD_WALLPAPER_USE_ALBUM_ART ->
                isAodWallpaperUseAlbumArt.value =
                    settings.getBoolean(key)

            DisplaySettings.KEY_AOD_WALLPAPER_KEEP_ON_MEDIA ->
                isAodWallpaperKeepOnMedia.value =
                    settings.getBoolean(key)

            DisplaySettings.KEY_AOD_WALLPAPER_EXTENDED_MEDIA ->
                isAodWallpaperExtendedMedia.value =
                    settings.getBoolean(key)

            DisplaySettings.KEY_AOD_WALLPAPER_EXTENDED_APP_ICON ->
                isAodWallpaperExtendedAppIcon.value =
                    settings.getBoolean(key)

            DisplaySettings.KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE ->
                aodWallpaperExtendedTextScale.floatValue =
                    settings.getAodWallpaperExtendedTextScale()
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                DisplaySettings.KEY_NOTIFICATION_GLANCE_ENABLED,
                DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED,
                DisplaySettings.KEY_AOD_WALLPAPER_ENABLED,
                DisplaySettings.KEY_AOD_WALLPAPER_OPACITY,
                DisplaySettings.KEY_AOD_WALLPAPER_TIMEOUT,
                DisplaySettings.KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT,
                DisplaySettings.KEY_AOD_WALLPAPER_BLUR,
                DisplaySettings.KEY_AOD_WALLPAPER_VIGNETTE,
                DisplaySettings.KEY_AOD_WALLPAPER_BLACK_THRESHOLD,
                DisplaySettings.KEY_AOD_WALLPAPER_CUSTOM_IMAGE,
                DisplaySettings.KEY_AOD_WALLPAPER_USE_ALBUM_ART,
                DisplaySettings.KEY_AOD_WALLPAPER_KEEP_ON_MEDIA,
                DisplaySettings.KEY_AOD_WALLPAPER_EXTENDED_MEDIA,
                DisplaySettings.KEY_AOD_WALLPAPER_EXTENDED_APP_ICON,
                DisplaySettings.KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE,
                DisplaySettings.KEY_NOTIFICATION_GLANCE_SAME_AS_LIGHTING,
            )
    }
}
