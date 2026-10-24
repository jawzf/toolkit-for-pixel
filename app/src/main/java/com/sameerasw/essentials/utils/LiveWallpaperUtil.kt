/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Utilities - General
 * File: LiveWallpaperUtil.kt
 * Description: Helper for opening the system live wallpaper preview.
 */

package com.sameerasw.essentials.utils

import android.app.WallpaperManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.sameerasw.essentials.R
import com.sameerasw.essentials.services.LiveWallpaperService

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
