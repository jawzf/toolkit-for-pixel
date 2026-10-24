package com.jawzf.pixeltoolkit.utils

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.jawzf.pixeltoolkit.R
import com.jawzf.pixeltoolkit.services.LiveWallpaperService

object LiveWallpaperUtil {
    fun openLiveWallpaperPicker(context: Context) {
        val previewIntent =
            Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(context, LiveWallpaperService::class.java),
                )
            }
        val chooserIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)

        for (intent in listOf(previewIntent, chooserIntent)) {
            try {
                context.startActivity(intent)
                return
            } catch (_: ActivityNotFoundException) {
            } catch (_: SecurityException) {
            }
        }
        Toast.makeText(context, R.string.label_live_wallpaper_unsupported, Toast.LENGTH_SHORT).show()
    }
}
