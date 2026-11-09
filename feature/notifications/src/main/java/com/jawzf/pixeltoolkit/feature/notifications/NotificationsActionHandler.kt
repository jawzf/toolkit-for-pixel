package com.jawzf.pixeltoolkit.feature.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionHandler
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingStyle
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.notifications.hilight.HilightController
import com.jawzf.pixeltoolkit.feature.notifications.lighting.DashConfig
import com.jawzf.pixeltoolkit.feature.notifications.lighting.NotificationLightingService
import com.jawzf.pixeltoolkit.feature.notifications.lighting.RippleConfig
import com.jawzf.pixeltoolkit.feature.notifications.lighting.overlay.fromPrefs
import com.jawzf.pixeltoolkit.feature.notifications.lighting.overlay.writeTo

/** Runs Notifications's DIY actions for the action engine. */
object NotificationsActionHandler : ActionHandler {
    override suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean {
        when (action) {
            is Action.TriggerNotificationLighting -> triggerNotificationLighting(context, action)
            is Action.Hilight -> HilightController.play(action.toEffect())
            else -> return false
        }
        return true
    }

    private fun prefFloat(
        prefs: SharedPreferences,
        key: String,
        default: Float,
    ): Float =
        try {
            prefs.getFloat(key, default)
        } catch (_: ClassCastException) {
            prefs.getInt(key, default.toInt()).toFloat()
        }

    fun triggerNotificationLighting(
        context: Context,
        action: Action.TriggerNotificationLighting,
    ) {
        val prefs = context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
        if (action.style == NotificationLightingStyle.SYSTEM) {
            if (!ShellUtils.hasPermission(context)) return
            val metrics = context.resources.displayMetrics
            val centerX = metrics.widthPixels / 2
            val centerY = metrics.heightPixels / 2
            val command =
                when (action.systemMode) {
                    0 -> "cmd statusbar charging-ripple"
                    1 -> "cmd statusbar auth-ripple custom $centerX $centerY"
                    else -> {
                        val posX = (prefFloat(prefs, "edge_lighting_indicator_x", 50f) / 100f * metrics.widthPixels).toInt()
                        val posY = (prefFloat(prefs, "edge_lighting_indicator_y", 2f) / 100f * metrics.heightPixels).toInt()
                        "cmd statusbar auth-ripple custom $posX $posY"
                    }
                }
            ShellUtils.runCommand(context, command, featureName = context.getString(action.title))
            return
        }

        val intent =
            Intent(context, NotificationLightingService::class.java).apply {
                putExtra("corner_radius_dp", prefFloat(prefs, "edge_lighting_corner_radius", 20f))
                putExtra("stroke_thickness_dp", prefFloat(prefs, "edge_lighting_stroke_thickness", 8f))
                putExtra("ignore_screen_state", true)
                putExtra("style", action.style.name)
                putExtra("color_mode", action.colorMode.name)
                putExtra("custom_color", action.customColor)
                putExtra("pulse_count", action.pulseCount)
                putExtra("pulse_duration", action.pulseDuration)
                putExtra("glow_sides", action.glowSides.map { it.name }.toTypedArray())
                putExtra("indicator_x", prefFloat(prefs, "edge_lighting_indicator_x", 50f))
                putExtra("indicator_y", prefFloat(prefs, "edge_lighting_indicator_y", 2f))
                putExtra("indicator_scale", prefFloat(prefs, "edge_lighting_indicator_scale", 1.0f))
                putExtra("sweep_position", prefs.getString("edge_lighting_sweep_position", "CENTER") ?: "CENTER")
                putExtra("sweep_thickness", prefFloat(prefs, "edge_lighting_sweep_thickness", 8f))
                putExtra("random_shapes", prefs.getBoolean("edge_lighting_sweep_random_shapes", true))
                RippleConfig.fromPrefs(prefs).writeTo(this)
                DashConfig.fromPrefs(prefs).writeTo(this)
            }
        try {
            if (PermissionUtils.isAccessibilityServiceEnabled(context)) {
                context.startService(intent)
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                intent.putExtra("is_foreground_start", true)
                context.startForegroundService(intent)
            }
        } catch (_: Exception) {
        }
    }
}
