package com.jawzf.pixeltoolkit.core.design.apps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.core.graphics.createBitmap
import androidx.palette.graphics.Palette

/** Small cached app icons and the brand colour taken from them. */
object AppIcons {
    private const val TAG = "AppIcons"

    // Cache for extracted brand colors
    private val colorCache = mutableMapOf<String, Int>()

    // Cache for app icons to prevent repeated system calls
    private val iconCache = mutableMapOf<String, Bitmap>()

    // Target size for app icons to balance quality and performance
    private const val ICON_SIZE = 64

    /**
     * Extracts the brand color from an app's icon using the Palette API.
     * Uses internal cache for efficiency.
     */
    fun getAppBrandColor(
        context: Context,
        packageName: String,
        callback: (Int) -> Unit,
    ) {
        // Check cache first
        colorCache[packageName]?.let {
            callback(it)
            return
        }

        try {
            context.packageManager
            // Extract bitmap from drawable, handling AdaptiveIcons
            val bitmap = getLowQualityIcon(context, packageName)

            // Generate palette asynchronously
            Palette.from(bitmap).generate { palette ->
                val color =
                    palette?.getVibrantColor(Color.TRANSPARENT)
                        ?: palette?.getDominantColor(Color.GRAY)
                        ?: Color.GRAY

                // Cache the result
                colorCache[packageName] = color
                callback(color)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error extracting color for $packageName: ${e.message}")
            callback(Color.GRAY)
        }
    }

    /**
     * Helper to load and scale an app icon to a lower resolution for better performance.
     */
    fun getLowQualityIcon(
        context: Context,
        packageName: String,
    ): Bitmap {
        // Check cache first
        iconCache[packageName]?.let { return it }

        val drawable =
            try {
                context.packageManager.getApplicationIcon(packageName)
            } catch (e: Exception) {
                context.packageManager.defaultActivityIcon
            }

        val bitmap =
            when (drawable) {
                is BitmapDrawable -> {
                    val b = drawable.bitmap
                    if (b.width > ICON_SIZE || b.height > ICON_SIZE) {
                        Bitmap.createScaledBitmap(b, ICON_SIZE, ICON_SIZE, true)
                    } else {
                        b
                    }
                }

                else -> {
                    val bmp = createBitmap(ICON_SIZE, ICON_SIZE)
                    val canvas = Canvas(bmp)
                    drawable.setBounds(0, 0, ICON_SIZE, ICON_SIZE)
                    drawable.draw(canvas)
                    bmp
                }
            }

        // Cache the result
        iconCache[packageName] = bitmap
        return bitmap
    }
}
