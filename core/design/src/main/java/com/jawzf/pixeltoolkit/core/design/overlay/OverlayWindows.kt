package com.jawzf.pixeltoolkit.core.design.overlay

import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager

object OverlayWindows {
    /**
     * Creates WindowManager.LayoutParams configured for an notification lighting overlay.
     *
     * @param overlayType The window type (e.g., TYPE_APPLICATION_OVERLAY, TYPE_ACCESSIBILITY_OVERLAY)
     * @param flags Optional additional flags to combine with default overlay flags
     * @return Configured LayoutParams
     */
    fun createOverlayLayoutParams(
        overlayType: Int,
        flags: Int = 0,
        isTouchable: Boolean = false,
    ): WindowManager.LayoutParams {
        var baseFlags =
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD

        if (!isTouchable) {
            baseFlags = baseFlags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        }

        val params =
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                baseFlags or flags,
                PixelFormat.TRANSLUCENT,
            )
        params.gravity = Gravity.TOP or Gravity.START

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                params.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            } catch (_: Exception) {
            }
        }

        return params
    }
}
