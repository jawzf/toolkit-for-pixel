package com.jawzf.pixeltoolkit.feature.notifications.lighting

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingColorMode
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingSide
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingStyle
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.notifications.lighting.overlay.writeTo

class LightingState {
    private lateinit var settings: NotificationSettings

    val isNotificationLightingEnabled = mutableStateOf(false)
    val onlyShowWhenScreenOff = mutableStateOf(true)
    val isAmbientDisplayEnabled = mutableStateOf(false)
    val isAmbientShowLockScreenEnabled = mutableStateOf(false)
    val edgeLightingSweepSelectedShapes = mutableStateOf<Set<String>>(emptySet())
    val skipSilentNotifications = mutableStateOf(true)
    val notificationLightingStyle = mutableStateOf(NotificationLightingStyle.STROKE)
    val notificationLightingColorMode = mutableStateOf(NotificationLightingColorMode.SYSTEM)
    val notificationLightingCustomColor = mutableIntStateOf(0xFF6200EE.toInt())
    val notificationLightingPulseCount = mutableStateOf(1f)
    val notificationLightingPulseDuration = mutableStateOf(3000f)
    val notificationLightingIndicatorX = mutableStateOf(50f)
    val notificationLightingIndicatorY = mutableStateOf(2f)
    val notificationLightingIndicatorScale = mutableStateOf(1.0f)
    val notificationLightingGlowSides =
        mutableStateOf(setOf(NotificationLightingSide.LEFT, NotificationLightingSide.RIGHT))
    val notificationLightingSweepPosition = mutableStateOf(NotificationLightingSweepPosition.CENTER)
    val notificationLightingSweepThickness = mutableFloatStateOf(8f)
    val notificationLightingSweepRandomShapes = mutableStateOf(false)
    val notificationLightingSystemMode = mutableIntStateOf(0)
    val ripple = RippleSettings { settings }
    val dash = DashSettings { settings }
    val skipPersistentNotifications = mutableStateOf(false)

    fun load(context: Context) {
        settings = NotificationSettings(context)
        notificationLightingSystemMode.intValue = settings.getNotificationLightingSystemMode()
        edgeLightingSweepSelectedShapes.value = settings.getEdgeLightingSweepSelectedShapes()
        isNotificationLightingEnabled.value =
            settings.getBoolean(NotificationSettings.KEY_EDGE_LIGHTING_ENABLED)
        onlyShowWhenScreenOff.value =
            settings.getBoolean(NotificationSettings.KEY_EDGE_LIGHTING_ONLY_SCREEN_OFF, true)
        isAmbientDisplayEnabled.value =
            settings.getBoolean(NotificationSettings.KEY_EDGE_LIGHTING_AMBIENT_DISPLAY)
        isAmbientShowLockScreenEnabled.value =
            settings.getBoolean(NotificationSettings.KEY_EDGE_LIGHTING_AMBIENT_SHOW_LOCK_SCREEN)
        skipSilentNotifications.value =
            settings.getBoolean(NotificationSettings.KEY_EDGE_LIGHTING_SKIP_SILENT, true)
        skipPersistentNotifications.value =
            settings.getBoolean(NotificationSettings.KEY_EDGE_LIGHTING_SKIP_PERSISTENT)
        notificationLightingStyle.value = settings.getNotificationLightingStyle()
        notificationLightingColorMode.value = settings.getNotificationLightingColorMode()
        notificationLightingCustomColor.intValue =
            settings.getInt(NotificationSettings.KEY_EDGE_LIGHTING_CUSTOM_COLOR, 0xFF6200EE.toInt())
        notificationLightingPulseCount.value =
            settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_PULSE_COUNT, 1f)
        notificationLightingPulseDuration.value =
            settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_PULSE_DURATION, 3000f)
        notificationLightingIndicatorX.value =
            settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_INDICATOR_X, 50f)
        notificationLightingIndicatorY.value =
            settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_INDICATOR_Y, 2f)
        notificationLightingIndicatorScale.value =
            settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_INDICATOR_SCALE, 1.0f)
        notificationLightingGlowSides.value = settings.getNotificationLightingGlowSides()
        notificationLightingSweepPosition.value = settings.getNotificationLightingSweepPosition()
        notificationLightingSweepThickness.floatValue =
            settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_SWEEP_THICKNESS, 8f)
        notificationLightingSweepRandomShapes.value =
            settings.getBoolean(NotificationSettings.KEY_EDGE_LIGHTING_SWEEP_RANDOM_SHAPES, true)
        ripple.load()
        dash.load()
    }

    fun setNotificationLightingEnabled(enabled: Boolean) {
        isNotificationLightingEnabled.value = enabled
        settings.putBoolean(NotificationSettings.KEY_EDGE_LIGHTING_ENABLED, enabled)
    }

    fun setOnlyShowWhenScreenOff(enabled: Boolean) {
        onlyShowWhenScreenOff.value = enabled
        settings.putBoolean(NotificationSettings.KEY_EDGE_LIGHTING_ONLY_SCREEN_OFF, enabled)
    }

    fun setAmbientDisplayEnabled(enabled: Boolean) {
        isAmbientDisplayEnabled.value = enabled
        settings.putBoolean(NotificationSettings.KEY_EDGE_LIGHTING_AMBIENT_DISPLAY, enabled)
    }

    fun setAmbientShowLockScreenEnabled(enabled: Boolean) {
        isAmbientShowLockScreenEnabled.value = enabled
        settings.putBoolean(
            NotificationSettings.KEY_EDGE_LIGHTING_AMBIENT_SHOW_LOCK_SCREEN,
            enabled,
        )
    }

    fun setSkipSilentNotifications(enabled: Boolean) {
        skipSilentNotifications.value = enabled
        settings.putBoolean(NotificationSettings.KEY_EDGE_LIGHTING_SKIP_SILENT, enabled)
    }

    fun setSkipPersistentNotifications(enabled: Boolean) {
        skipPersistentNotifications.value = enabled
        settings.putBoolean(NotificationSettings.KEY_EDGE_LIGHTING_SKIP_PERSISTENT, enabled)
    }

    fun setNotificationLightingStyle(
        style: NotificationLightingStyle,
        context: Context,
    ) {
        if (style == NotificationLightingStyle.SYSTEM && !ShellUtils.hasPermission(context)) {
            return
        }
        notificationLightingStyle.value = style
        settings.putString(NotificationSettings.KEY_EDGE_LIGHTING_STYLE, style.name)
    }

    fun setNotificationLightingSystemMode(mode: Int) {
        notificationLightingSystemMode.intValue = mode
        settings.saveNotificationLightingSystemMode(mode)
    }

    fun setNotificationLightingColorMode(mode: NotificationLightingColorMode) {
        notificationLightingColorMode.value = mode
        settings.putString(NotificationSettings.KEY_EDGE_LIGHTING_COLOR_MODE, mode.name)
    }

    fun setNotificationLightingCustomColor(color: Int) {
        notificationLightingCustomColor.intValue = color
        settings.putInt(NotificationSettings.KEY_EDGE_LIGHTING_CUSTOM_COLOR, color)
    }

    fun saveNotificationLightingPulseCount(count: Float) {
        notificationLightingPulseCount.value = count
        settings.putFloat(NotificationSettings.KEY_EDGE_LIGHTING_PULSE_COUNT, count)
    }

    fun saveNotificationLightingPulseDuration(duration: Float) {
        notificationLightingPulseDuration.value = duration
        settings.putFloat(NotificationSettings.KEY_EDGE_LIGHTING_PULSE_DURATION, duration)
    }

    private fun Intent.addLightingExtras(
        cornerRadiusDp: Float? = null,
        strokeThicknessDp: Float? = null,
        isPreview: Boolean = true,
        styleOverride: NotificationLightingStyle? = null,
    ) {
        val radius =
            cornerRadiusDp
                ?: settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_CORNER_RADIUS, 20f)
        val thickness =
            strokeThicknessDp
                ?: settings.getFloat(
                    NotificationSettings.KEY_EDGE_LIGHTING_STROKE_THICKNESS,
                    8f,
                )

        putExtra("corner_radius_dp", radius)
        putExtra("stroke_thickness_dp", thickness)
        putExtra("is_preview", isPreview)
        putExtra("ignore_screen_state", true)
        putExtra("style", (styleOverride ?: notificationLightingStyle.value).name)
        putExtra("color_mode", notificationLightingColorMode.value.name)
        putExtra("custom_color", notificationLightingCustomColor.intValue)
        putExtra("pulse_count", notificationLightingPulseCount.value.toInt())
        putExtra("pulse_duration", notificationLightingPulseDuration.value.toLong())
        putExtra(
            "glow_sides",
            notificationLightingGlowSides.value.map { it.name }.toTypedArray(),
        )
        putExtra("indicator_x", notificationLightingIndicatorX.value)
        putExtra("indicator_y", notificationLightingIndicatorY.value)
        putExtra("indicator_scale", notificationLightingIndicatorScale.value)
        putExtra("sweep_position", notificationLightingSweepPosition.value.name)
        putExtra("sweep_thickness", notificationLightingSweepThickness.floatValue)
        putExtra("random_shapes", notificationLightingSweepRandomShapes.value)
        putExtra("system_lighting_mode", notificationLightingSystemMode.intValue)
        ripple.toConfig().writeTo(this)
        dash.toConfig().writeTo(this)
    }

    fun triggerNotificationLighting(context: Context) {
        if (notificationLightingStyle.value == NotificationLightingStyle.SYSTEM) {
            triggerNotificationLightingSystem(context)
            return
        }
        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(isPreview = false)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerNotificationLightingSystem(context: Context) {
        if (!ShellUtils.hasPermission(context)) return

        val windowManager =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val (widthPx, heightPx) =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val bounds = windowManager.currentWindowMetrics.bounds
                Pair(bounds.width(), bounds.height())
            } else {
                val metrics = android.util.DisplayMetrics()
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay.getRealMetrics(metrics)
                Pair(metrics.widthPixels, metrics.heightPixels)
            }
        val centerX = widthPx / 2
        val centerY = heightPx / 2

        val command =
            if (notificationLightingSystemMode.intValue == 0) {
                "cmd statusbar charging-ripple"
            } else if (notificationLightingSystemMode.intValue == 1) {
                "cmd statusbar auth-ripple custom $centerX $centerY"
            } else {
                val posX = (notificationLightingIndicatorX.value / 100f * widthPx).toInt()
                val posY = (notificationLightingIndicatorY.value / 100f * heightPx).toInt()
                "cmd statusbar auth-ripple custom $posX $posY"
            }

        ShellUtils.runCommand(context, command)
    }

    fun triggerNotificationLightingPreview(context: Context) {
        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(isPreview = true)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerNotificationLightingWithRadius(
        context: Context,
        cornerRadiusDp: Float,
    ) {
        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(cornerRadiusDp = cornerRadiusDp)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerNotificationLightingWithRadiusAndThickness(
        context: Context,
        cornerRadiusDp: Float,
        strokeThicknessDp: Float,
    ) {
        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(cornerRadiusDp, strokeThicknessDp)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerNotificationLightingForIndicator(
        context: Context,
        x: Float,
        y: Float,
        scale: Float,
    ) {
        notificationLightingIndicatorX.value = x
        notificationLightingIndicatorY.value = y
        notificationLightingIndicatorScale.value = scale

        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(styleOverride = NotificationLightingStyle.INDICATOR)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerNotificationLightingForDash(context: Context) {
        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(styleOverride = NotificationLightingStyle.DASH)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerNotificationLightingForRipple(context: Context) {
        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(styleOverride = NotificationLightingStyle.RIPPLE)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun triggerNotificationLightingForSweep(
        context: Context,
        position: NotificationLightingSweepPosition,
        thickness: Float,
    ) {
        notificationLightingSweepPosition.value = position
        notificationLightingSweepThickness.floatValue = thickness

        try {
            val intent =
                Intent(context, NotificationLightingService::class.java).apply {
                    addLightingExtras(styleOverride = NotificationLightingStyle.SWEEP)
                }
            context.startService(intent)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun removePreviewOverlay(context: Context) {
        try {
            val intent1 =
                Intent(context, NotificationLightingService::class.java).apply {
                    putExtra("remove_preview", true)
                }
            context.startService(intent1)

            val intent2 =
                Intent().setClassName(context, AppComponents.SCREEN_OFF_ACCESSIBILITY_SERVICE).apply {
                    action = "SHOW_NOTIFICATION_LIGHTING"
                    putExtra("remove_preview", true)
                }
            context.startService(intent2)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun saveNotificationLightingSelectedApps(apps: List<AppSelection>) {
        settings.saveNotificationLightingSelectedApps(apps)
    }

    fun loadNotificationLightingSelectedApps(): List<AppSelection> = settings.loadNotificationLightingSelectedApps()

    fun updateNotificationLightingAppEnabled(
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateNotificationLightingAppSelection(packageName, enabled)
    }

    fun saveNotificationLightingCornerRadius(radiusDp: Float) {
        settings.putFloat(NotificationSettings.KEY_EDGE_LIGHTING_CORNER_RADIUS, radiusDp)
    }

    fun loadNotificationLightingCornerRadius(): Float = settings.getFloat(NotificationSettings.KEY_EDGE_LIGHTING_CORNER_RADIUS, 20f)

    fun saveNotificationLightingStrokeThickness(thicknessDp: Float) {
        settings.putFloat(
            NotificationSettings.KEY_EDGE_LIGHTING_STROKE_THICKNESS,
            thicknessDp,
        )
    }

    fun loadNotificationLightingStrokeThickness(): Float =
        settings.getFloat(
            NotificationSettings.KEY_EDGE_LIGHTING_STROKE_THICKNESS,
            8f,
        )

    fun setNotificationLightingGlowSides(sides: Set<NotificationLightingSide>) {
        notificationLightingGlowSides.value = sides
        settings.saveNotificationLightingGlowSides(sides)
    }

    fun saveNotificationLightingIndicatorX(x: Float) {
        notificationLightingIndicatorX.value = x
        settings.putFloat(NotificationSettings.KEY_EDGE_LIGHTING_INDICATOR_X, x)
    }

    fun saveNotificationLightingIndicatorY(y: Float) {
        notificationLightingIndicatorY.value = y
        settings.putFloat(NotificationSettings.KEY_EDGE_LIGHTING_INDICATOR_Y, y)
    }

    fun saveNotificationLightingIndicatorScale(scale: Float) {
        notificationLightingIndicatorScale.value = scale
        settings.putFloat(NotificationSettings.KEY_EDGE_LIGHTING_INDICATOR_SCALE, scale)
    }

    fun setNotificationLightingSweepPosition(position: NotificationLightingSweepPosition) {
        notificationLightingSweepPosition.value = position
        settings.saveNotificationLightingSweepPosition(position)
    }

    fun saveNotificationLightingSweepThickness(thickness: Float) {
        notificationLightingSweepThickness.floatValue = thickness
        settings.putFloat(NotificationSettings.KEY_EDGE_LIGHTING_SWEEP_THICKNESS, thickness)
    }

    fun saveNotificationLightingSweepRandomShapes(enabled: Boolean) {
        notificationLightingSweepRandomShapes.value = enabled
        settings.putBoolean(
            NotificationSettings.KEY_EDGE_LIGHTING_SWEEP_RANDOM_SHAPES,
            enabled,
        )
    }

    fun saveEdgeLightingSweepSelectedShapes(shapes: Set<String>) {
        edgeLightingSweepSelectedShapes.value = shapes
        settings.saveEdgeLightingSweepSelectedShapes(shapes)
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            NotificationSettings.KEY_EDGE_LIGHTING_ENABLED ->
                isNotificationLightingEnabled.value = settings.getBoolean(key)
            NotificationSettings.KEY_EDGE_LIGHTING_SWEEP_SELECTED_SHAPES ->
                edgeLightingSweepSelectedShapes.value = settings.getEdgeLightingSweepSelectedShapes()
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                NotificationSettings.KEY_EDGE_LIGHTING_ENABLED,
                NotificationSettings.KEY_EDGE_LIGHTING_SWEEP_SELECTED_SHAPES,
            )
    }
}
