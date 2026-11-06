package com.jawzf.pixeltoolkit.feature.notifications

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.action.HilightEffect
import com.jawzf.pixeltoolkit.core.data.action.HilightProgressColorMode
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingColorMode
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingSide
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingStyle
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.design.overlay.AmbientMusicShapeHelper
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissAppConfig
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissChannel
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissStat
import com.jawzf.pixeltoolkit.feature.notifications.lighting.NotificationLightingSweepPosition
import com.jawzf.pixeltoolkit.feature.notifications.snooze.SnoozeChannel

/** Notification settings on the main preference file. Keys are unchanged from SettingsRepository. */
class NotificationSettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    fun isAutoDismissEnabled(): Boolean = getBoolean(KEY_AUTO_DISMISS_ENABLED, false)

    fun setAutoDismissEnabled(enabled: Boolean) = putBoolean(KEY_AUTO_DISMISS_ENABLED, enabled)

    fun getAutoDismissLastMinutes(): Int = getInt(KEY_AUTO_DISMISS_LAST_MINUTES, 15)

    fun setAutoDismissLastMinutes(minutes: Int) = putInt(KEY_AUTO_DISMISS_LAST_MINUTES, minutes)

    fun loadAutoDismissConfigs(): List<AutoDismissAppConfig> {
        val json = prefs.getString(KEY_AUTO_DISMISS_APPS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<AutoDismissAppConfig>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveAutoDismissConfigs(configs: List<AutoDismissAppConfig>) {
        val json = gson.toJson(configs)
        putString(KEY_AUTO_DISMISS_APPS, json)
    }

    fun loadAutoDismissChannels(): List<AutoDismissChannel> {
        val json = prefs.getString(KEY_AUTO_DISMISS_CHANNELS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<AutoDismissChannel>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveAutoDismissChannels(channels: List<AutoDismissChannel>) {
        val json = gson.toJson(channels)
        putString(KEY_AUTO_DISMISS_CHANNELS, json)
    }

    fun loadAutoDismissStats(): Map<String, AutoDismissStat> {
        val json = prefs.getString(KEY_AUTO_DISMISS_STATS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, AutoDismissStat>>() {}.type
            gson.fromJson<Map<String, AutoDismissStat>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveAutoDismissStats(stats: Map<String, AutoDismissStat>) {
        val json = gson.toJson(stats)
        putString(KEY_AUTO_DISMISS_STATS, json)
    }

    fun incrementAutoDismissCount(packageName: String) {
        val current = loadAutoDismissStats().toMutableMap()
        val existing = current[packageName]
        val updated =
            if (existing != null) {
                existing.copy(count = existing.count + 1)
            } else {
                AutoDismissStat(1, System.currentTimeMillis())
            }
        current[packageName] = updated
        saveAutoDismissStats(current)
    }

    // Snooze Notifications Helper
    fun loadSnoozeDiscoveredChannels(): List<SnoozeChannel> {
        val json = prefs.getString(KEY_SNOOZE_DISCOVERED_CHANNELS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<SnoozeChannel>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Executes the save snooze discovered channels operation.
     *
     * @param channels [List<SnoozeChannel>] Target channels.
     */
    fun saveSnoozeDiscoveredChannels(channels: List<SnoozeChannel>) {
        val json = gson.toJson(channels)
        putString(KEY_SNOOZE_DISCOVERED_CHANNELS, json)
    }

    /**
     * Executes the load snooze blocked channels operation.
     * @return The resulting Set<String> data.
     */
    fun loadSnoozeBlockedChannels(): Set<String> {
        val json = prefs.getString(KEY_SNOOZE_BLOCKED_CHANNELS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
    }

    /**
     * Executes the save snooze blocked channels operation.
     *
     * @param blockedChannels [Set<String>] Target blocked channels.
     */
    fun saveSnoozeBlockedChannels(blockedChannels: Set<String>) {
        val json = gson.toJson(blockedChannels)
        putString(KEY_SNOOZE_BLOCKED_CHANNELS, json)
    }

    // Each app the user adds gets its own effect; apps not in the map never light up
    fun getHilightAppEffects(): Map<String, HilightEffect> =
        getString(KEY_HILIGHT_APP_EFFECTS)?.let {
            try {
                gson
                    .fromJson<Map<String, HilightEffect>>(it, object : TypeToken<Map<String, HilightEffect>>() {}.type)
                    ?.mapValues { (_, effect) -> effect.withValidPattern() }
            } catch (_: Exception) {
                null
            }
        } ?: emptyMap()

    fun getHilightEffectForApp(packageName: String): HilightEffect? = getHilightAppEffects()[packageName]

    fun setHilightAppEffect(
        packageName: String,
        effect: HilightEffect?,
    ) {
        val effects = getHilightAppEffects().toMutableMap()
        if (effect == null) effects.remove(packageName) else effects[packageName] = effect
        putString(KEY_HILIGHT_APP_EFFECTS, gson.toJson(effects))
    }

    fun isHilightNotificationsEnabled(): Boolean = getBoolean(KEY_HILIGHT_NOTIFICATIONS_ENABLED, false)

    fun setHilightNotificationsEnabled(enabled: Boolean) = putBoolean(KEY_HILIGHT_NOTIFICATIONS_ENABLED, enabled)

    fun isHilightOnlyWhenScreenOff(): Boolean = getBoolean(KEY_HILIGHT_ONLY_SCREEN_OFF, true)

    fun setHilightOnlyWhenScreenOff(enabled: Boolean) = putBoolean(KEY_HILIGHT_ONLY_SCREEN_OFF, enabled)

    fun isHilightSkipDnd(): Boolean = getBoolean(KEY_HILIGHT_SKIP_DND, true)

    fun setHilightSkipDnd(enabled: Boolean) = putBoolean(KEY_HILIGHT_SKIP_DND, enabled)

    fun getHilightCooldownSeconds(): Int = getInt(KEY_HILIGHT_COOLDOWN_SECONDS, 60)

    fun setHilightCooldownSeconds(seconds: Int) = putInt(KEY_HILIGHT_COOLDOWN_SECONDS, seconds)

    fun isHilightProgressEnabled(): Boolean = getBoolean(KEY_HILIGHT_PROGRESS_ENABLED, false)

    fun setHilightProgressEnabled(enabled: Boolean) = putBoolean(KEY_HILIGHT_PROGRESS_ENABLED, enabled)

    fun getHilightProgressColorMode(): HilightProgressColorMode =
        HilightProgressColorMode.entries.find { it.name == getString(KEY_HILIGHT_PROGRESS_COLOR_MODE) }
            ?: HilightProgressColorMode.VIBGYOR

    fun setHilightProgressColorMode(mode: HilightProgressColorMode) = putString(KEY_HILIGHT_PROGRESS_COLOR_MODE, mode.name)

    fun getHilightProgressColor(): Int = getInt(KEY_HILIGHT_PROGRESS_COLOR, HilightEffect.DEFAULT_COLOR)

    fun setHilightProgressColor(color: Int) = putInt(KEY_HILIGHT_PROGRESS_COLOR, color)

    fun getHilightProgressDurationMs(): Long = getLong(KEY_HILIGHT_PROGRESS_DURATION_MS, HilightEffect.DEFAULT_DURATION_MS)

    fun setHilightProgressDurationMs(durationMs: Long) = putLong(KEY_HILIGHT_PROGRESS_DURATION_MS, durationMs)

    fun getNotificationLightingStyle(): NotificationLightingStyle {
        val styleName =
            prefs.getString(KEY_EDGE_LIGHTING_STYLE, NotificationLightingStyle.STROKE.name)
        return try {
            NotificationLightingStyle.valueOf(styleName ?: NotificationLightingStyle.STROKE.name)
        } catch (e: Exception) {
            NotificationLightingStyle.STROKE
        }
    }

    /**
     * Executes the get notification lighting color mode operation.
     * @return The resulting NotificationLightingColorMode data.
     */
    fun getNotificationLightingColorMode(): NotificationLightingColorMode {
        val colorModeName =
            prefs.getString(KEY_EDGE_LIGHTING_COLOR_MODE, NotificationLightingColorMode.SYSTEM.name)
        return try {
            NotificationLightingColorMode.valueOf(
                colorModeName ?: NotificationLightingColorMode.SYSTEM.name,
            )
        } catch (e: Exception) {
            NotificationLightingColorMode.SYSTEM
        }
    }

    /**
     * Executes the get notification lighting glow sides operation.
     * @return The resulting Set<NotificationLightingSide> data.
     */
    fun getNotificationLightingGlowSides(): Set<NotificationLightingSide> {
        val json = prefs.getString(KEY_EDGE_LIGHTING_GLOW_SIDES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<NotificationLightingSide>::class.java).toSet()
            } catch (e: Exception) {
                setOf(NotificationLightingSide.LEFT, NotificationLightingSide.RIGHT)
            }
        } else {
            setOf(NotificationLightingSide.LEFT, NotificationLightingSide.RIGHT)
        }
    }

    /**
     * Executes the save notification lighting glow sides operation.
     *
     * @param sides [Set<NotificationLightingSide>] Target sides.
     */
    fun saveNotificationLightingGlowSides(sides: Set<NotificationLightingSide>) {
        val json = gson.toJson(sides)
        putString(KEY_EDGE_LIGHTING_GLOW_SIDES, json)
    }

    /**
     * Executes the get notification lighting sweep position operation.
     * @return The resulting NotificationLightingSweepPosition data.
     */
    fun getNotificationLightingSweepPosition(): NotificationLightingSweepPosition {
        val posName =
            prefs.getString(
                KEY_EDGE_LIGHTING_SWEEP_POSITION,
                NotificationLightingSweepPosition.CENTER.name,
            )
        return try {
            NotificationLightingSweepPosition.valueOf(
                posName ?: NotificationLightingSweepPosition.CENTER.name,
            )
        } catch (e: Exception) {
            NotificationLightingSweepPosition.CENTER
        }
    }

    /**
     * Executes the save notification lighting sweep position operation.
     *
     * @param position [NotificationLightingSweepPosition] Target position.
     */
    fun saveNotificationLightingSweepPosition(position: NotificationLightingSweepPosition) {
        putString(KEY_EDGE_LIGHTING_SWEEP_POSITION, position.name)
    }

    /**
     * Executes the get notification lighting system mode operation.
     * @return The resulting Int data.
     */
    fun getNotificationLightingSystemMode(): Int = getInt(KEY_EDGE_LIGHTING_SYSTEM_MODE, 0)

    /**
     * Executes the save notification lighting system mode operation.
     *
     * @param mode [Int] Target mode.
     */
    fun saveNotificationLightingSystemMode(mode: Int) = putInt(KEY_EDGE_LIGHTING_SYSTEM_MODE, mode)

    fun loadNotificationLightingSelectedApps(): List<AppSelection> = loadAppSelection(KEY_EDGE_LIGHTING_SELECTED_APPS)

    /**
     * Executes the save notification lighting selected apps operation.
     *
     * @param apps [List<AppSelection>] Target apps.
     */
    fun saveNotificationLightingSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_EDGE_LIGHTING_SELECTED_APPS, apps)

    /**
     * Executes the update notification lighting app selection operation.
     *
     * @param packageName [String] Target package name.
     * @param enabled [Boolean] Target enabled.
     */
    fun updateNotificationLightingAppSelection(
        packageName: String,
        enabled: Boolean,
    ) {
        saveNotificationLightingSelectedApps(withSelection(loadNotificationLightingSelectedApps(), packageName, enabled))
    }

    /**
     * Executes the load flashlight pulse selected apps operation.
     */
    fun loadFlashlightPulseSelectedApps(): List<AppSelection> = loadAppSelection(KEY_FLASHLIGHT_PULSE_SELECTED_APPS)

    /**
     * Executes the save flashlight pulse selected apps operation.
     *
     * @param apps [List<AppSelection>] Target apps.
     */
    fun saveFlashlightPulseSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_FLASHLIGHT_PULSE_SELECTED_APPS, apps)

    /**
     * Executes the update flashlight pulse app selection operation.
     *
     * @param packageName [String] Target package name.
     * @param enabled [Boolean] Target enabled.
     */
    fun updateFlashlightPulseAppSelection(
        packageName: String,
        enabled: Boolean,
    ) {
        saveFlashlightPulseSelectedApps(withSelection(loadFlashlightPulseSelectedApps(), packageName, enabled))
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

    /**
     * Executes the get edge lighting sweep selected shapes operation.
     * @return The resulting Set<String> data.
     */
    fun getEdgeLightingSweepSelectedShapes(): Set<String> {
        val defaultShapes =
            AmbientMusicShapeHelper.allShapesWithNames
                .map { it.first }
                .toSet()
        val json = prefs.getString(KEY_EDGE_LIGHTING_SWEEP_SELECTED_SHAPES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toSet()
            } catch (e: Exception) {
                defaultShapes
            }
        } else {
            defaultShapes
        }
    }

    /**
     * Executes the save edge lighting sweep selected shapes operation.
     *
     * @param shapes [Set<String>] Target shapes.
     */
    fun saveEdgeLightingSweepSelectedShapes(shapes: Set<String>) {
        val json = gson.toJson(shapes)
        putString(KEY_EDGE_LIGHTING_SWEEP_SELECTED_SHAPES, json)
    }

    companion object {
        const val KEY_SNOOZE_DISCOVERED_CHANNELS = "snooze_discovered_channels"
        const val KEY_SNOOZE_BLOCKED_CHANNELS = "snooze_blocked_channels"
        const val KEY_SNOOZE_HEADS_UP_ENABLED = "snooze_heads_up_enabled"
        const val KEY_AUTO_DISMISS_ENABLED = "auto_dismiss_notifications_enabled"
        const val KEY_AUTO_DISMISS_APPS = "auto_dismiss_notifications_apps"
        const val KEY_AUTO_DISMISS_LAST_MINUTES = "auto_dismiss_notifications_last_minutes"
        const val KEY_AUTO_DISMISS_CHANNELS = "auto_dismiss_notifications_channels"
        const val KEY_AUTO_DISMISS_STATS = "auto_dismiss_notifications_stats"
        const val KEY_HILIGHT_NOTIFICATIONS_ENABLED = "hilight_notifications_enabled"
        const val KEY_HILIGHT_APP_EFFECTS = "hilight_app_effects"
        const val KEY_HILIGHT_ONLY_SCREEN_OFF = "hilight_only_screen_off"
        const val KEY_HILIGHT_COOLDOWN_SECONDS = "hilight_cooldown_seconds"
        const val KEY_HILIGHT_PROGRESS_ENABLED = "hilight_progress_enabled"
        const val KEY_HILIGHT_PROGRESS_COLOR_MODE = "hilight_progress_color_mode"
        const val KEY_HILIGHT_PROGRESS_COLOR = "hilight_progress_color"
        const val KEY_HILIGHT_PROGRESS_DURATION_MS = "hilight_progress_duration_ms"
        const val KEY_HILIGHT_SKIP_DND = "hilight_skip_dnd"
        const val KEY_EDGE_LIGHTING_ENABLED = "edge_lighting_enabled"
        const val KEY_EDGE_LIGHTING_ONLY_SCREEN_OFF = "edge_lighting_only_screen_off"
        const val KEY_EDGE_LIGHTING_AMBIENT_DISPLAY = "edge_lighting_ambient_display"
        const val KEY_EDGE_LIGHTING_AMBIENT_SHOW_LOCK_SCREEN =
            "edge_lighting_ambient_show_lock_screen"
        const val KEY_EDGE_LIGHTING_SKIP_SILENT = "edge_lighting_skip_silent"
        const val KEY_EDGE_LIGHTING_SKIP_PERSISTENT = "edge_lighting_skip_persistent"
        const val KEY_EDGE_LIGHTING_STYLE = "edge_lighting_style"
        const val KEY_EDGE_LIGHTING_COLOR_MODE = "edge_lighting_color_mode"
        const val KEY_EDGE_LIGHTING_CUSTOM_COLOR = "edge_lighting_custom_color"
        const val KEY_EDGE_LIGHTING_PULSE_COUNT = "edge_lighting_pulse_count"
        const val KEY_EDGE_LIGHTING_PULSE_DURATION = "edge_lighting_pulse_duration"
        const val KEY_EDGE_LIGHTING_INDICATOR_X = "edge_lighting_indicator_x"
        const val KEY_EDGE_LIGHTING_INDICATOR_Y = "edge_lighting_indicator_y"
        const val KEY_EDGE_LIGHTING_INDICATOR_SCALE = "edge_lighting_indicator_scale"
        const val KEY_EDGE_LIGHTING_GLOW_SIDES = "edge_lighting_glow_sides"
        const val KEY_EDGE_LIGHTING_CORNER_RADIUS = "edge_lighting_corner_radius"
        const val KEY_EDGE_LIGHTING_STROKE_THICKNESS = "edge_lighting_stroke_thickness"
        const val KEY_EDGE_LIGHTING_SELECTED_APPS = "edge_lighting_selected_apps"
        const val KEY_EDGE_LIGHTING_SWEEP_POSITION = "edge_lighting_sweep_position"
        const val KEY_EDGE_LIGHTING_SWEEP_THICKNESS = "edge_lighting_sweep_thickness"
        const val KEY_EDGE_LIGHTING_SWEEP_RANDOM_SHAPES = "edge_lighting_sweep_random_shapes"
        const val KEY_EDGE_LIGHTING_SYSTEM_MODE = "edge_lighting_system_mode"
        const val KEY_EDGE_LIGHTING_RIPPLE_SPEED = "edge_lighting_ripple_speed"
        const val KEY_EDGE_LIGHTING_RIPPLE_SPARKLE_COUNT = "edge_lighting_ripple_sparkle_count"
        const val KEY_EDGE_LIGHTING_RIPPLE_SPARKLE_SIZE = "edge_lighting_ripple_sparkle_size"
        const val KEY_EDGE_LIGHTING_RIPPLE_WAVE_SIZE = "edge_lighting_ripple_wave_size"
        const val KEY_EDGE_LIGHTING_RIPPLE_REPEAT_COUNT = "edge_lighting_ripple_repeat_count"
        const val KEY_EDGE_LIGHTING_RIPPLE_SPARKLES_ENABLED = "edge_lighting_ripple_sparkles_enabled"
        const val KEY_EDGE_LIGHTING_RIPPLE_OPACITY = "edge_lighting_ripple_opacity"
        const val KEY_EDGE_LIGHTING_RIPPLE_POSITION = "edge_lighting_ripple_position"
        const val KEY_EDGE_LIGHTING_RIPPLE_POSITION_X = "edge_lighting_ripple_position_x"
        const val KEY_EDGE_LIGHTING_RIPPLE_POSITION_Y = "edge_lighting_ripple_position_y"
        const val KEY_EDGE_LIGHTING_DASH_THICKNESS = "edge_lighting_dash_thickness"
        const val KEY_EDGE_LIGHTING_DASH_LENGTH = "edge_lighting_dash_length"
        const val KEY_EDGE_LIGHTING_DASH_GLOW = "edge_lighting_dash_glow"
        const val KEY_EDGE_LIGHTING_DASH_GLOW_LENGTH = "edge_lighting_dash_glow_length"
        const val KEY_EDGE_LIGHTING_DASH_OVERLAP = "edge_lighting_dash_overlap"
        const val KEY_EDGE_LIGHTING_RIPPLE_OVERLAP = "edge_lighting_ripple_overlap"
        const val KEY_EDGE_LIGHTING_SWEEP_SELECTED_SHAPES = "edge_lighting_sweep_selected_shapes"
        const val KEY_USE_RIPPLE = "use_ripple"
        const val KEY_FLASHLIGHT_PULSE_ENABLED = "flashlight_pulse_enabled"
        const val KEY_FLASHLIGHT_PULSE_FACEDOWN_ONLY = "flashlight_pulse_facedown_only"
        const val KEY_FLASHLIGHT_PULSE_MAX_INTENSITY = "flashlight_pulse_max_intensity"
        const val KEY_FLASHLIGHT_PULSE_DISABLE_ON_DND = "flashlight_pulse_disable_on_dnd"
        const val KEY_FLASHLIGHT_PULSE_SELECTED_APPS = "flashlight_pulse_selected_apps"
        const val KEY_FLASHLIGHT_PULSE_SAME_AS_LIGHTING = "flashlight_pulse_same_as_lighting"
    }
}
