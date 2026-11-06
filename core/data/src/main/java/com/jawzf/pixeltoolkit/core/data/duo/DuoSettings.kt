package com.jawzf.pixeltoolkit.core.data.duo

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.view.Display
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionGsonAdapter
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore

class DuoSettings(
    context: Context,
) : PrefsStore(context) {
    companion object {
        const val KEY_DUO_ENABLED = "duo_enabled"
        const val KEY_DUO_ISLAND_COMBINED = "duo_island_combined"
        const val KEY_DUO_TAP_FOR_BRIEF = "duo_tap_for_brief"
        const val KEY_DUO_HIDE_ON_SHADE = "duo_hide_on_shade"
        const val KEY_DUO_KEEP_IN_FULLSCREEN = "duo_keep_in_fullscreen"
        const val KEY_DUO_DISABLE_INTERACTIONS_IN_FULLSCREEN = "duo_disable_interactions_in_fullscreen"
        const val KEY_DUO_USE_AUTO_DETECT = "duo_use_auto_detect"
        const val KEY_DUO_CAMERA_OFFSET_X = "duo_camera_offset_x"
        const val KEY_DUO_CAMERA_OFFSET_Y = "duo_camera_offset_y"
        const val KEY_DUO_CAMERA_SIZE = "duo_camera_size"
        const val KEY_DUO_ORIENTATION_PROFILES = "duo_orientation_profiles"
        const val KEY_DUO_HIDE_PORTRAIT = "duo_hide_portrait"
        const val KEY_DUO_HIDE_LANDSCAPE = "duo_hide_landscape"
        const val KEY_DUO_KNOWN_DISPLAY_PROFILES = "duo_known_display_profiles"
        const val KEY_DUO_ARC_THICKNESS = "duo_arc_thickness"
        const val KEY_DUO_DOT_SIZE = "duo_dot_size"
        const val KEY_DUO_RING_RADIUS = "duo_ring_radius"
        const val KEY_DUO_TIME_TEXT_SCALE = "duo_time_text_scale"
        const val KEY_DUO_SHOW_BATTERY = "duo_show_battery"
        const val KEY_DUO_SHOW_BATTERY_PERCENTAGE = "duo_show_battery_percentage"
        const val KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED = "duo_battery_percentage_only_colored"
        const val KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED = "duo_battery_charging_color_enabled"
        const val KEY_DUO_BATTERY_CHARGING_COLOR = "duo_battery_charging_color"
        const val KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED = "duo_battery_power_save_color_enabled"
        const val KEY_DUO_BATTERY_POWER_SAVE_COLOR = "duo_battery_power_save_color"
        const val KEY_DUO_BATTERY_LOW_COLOR_ENABLED = "duo_battery_low_color_enabled"
        const val KEY_DUO_BATTERY_LOW_COLOR = "duo_battery_low_color"
        const val KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED = "duo_battery_critical_color_enabled"
        const val KEY_DUO_BATTERY_CRITICAL_COLOR = "duo_battery_critical_color"
        const val KEY_DUO_BATTERY_LOW_LEVEL = "duo_battery_low_level"
        const val KEY_DUO_BATTERY_CRITICAL_LEVEL = "duo_battery_critical_level"
        const val KEY_DUO_SHOW_NETWORKS = "duo_show_networks"
        const val KEY_DUO_DIFFERENTIATE_WIFI = "duo_differentiate_wifi"
        const val KEY_DUO_SHOW_TIME = "duo_show_time"
        const val KEY_DUO_SHOW_MEDIA = "duo_show_media"
        const val KEY_DUO_ROTATE_ALBUM_ART = "duo_rotate_album_art"
        const val KEY_DUO_SHOW_PROGRESS = "duo_show_progress"
        const val KEY_DUO_SHOW_FLASHLIGHT = "duo_show_flashlight"
        const val KEY_DUO_HIDE_WHEN_SCREEN_OFF = "duo_hide_when_screen_off"
        const val KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE = "duo_hide_when_screen_off_only_idle"
        const val KEY_DUO_HIDE_WHEN_LOCKED = "duo_hide_when_locked"
        const val KEY_DUO_USE_MATERIAL_YOU = "duo_use_material_you"
        const val KEY_DUO_CUSTOM_COLOR = "duo_custom_color"
        const val KEY_DUO_TAP_ACTION = "duo_tap_action"
        const val KEY_DUO_DOUBLE_TAP_ACTION = "duo_double_tap_action"
        const val KEY_DUO_LONG_PRESS_ACTION = "duo_long_press_action"
        const val KEY_DUO_SWIPE_DOWN_ACTION = "duo_swipe_down_action"
        const val KEY_DUO_SLIDE_MODE = "duo_slide_mode"
        const val KEY_DUO_SLIDE_TRACK = "duo_slide_track"
        const val KEY_DUO_SLIDE_INVERT_DIRECTION = "duo_slide_invert_direction"

        private const val KEY_ISLAND_ENABLED = "island_enabled"
        private const val KEY_ISLAND_BRIEF_ENABLED = "island_brief_enabled"
    }

    fun isDuoEnabled(): Boolean = getBoolean(KEY_DUO_ENABLED, false)

    fun setDuoEnabled(enabled: Boolean) = putBoolean(KEY_DUO_ENABLED, enabled)

    fun isDuoIslandCombinedSetting(): Boolean = getBoolean(KEY_DUO_ISLAND_COMBINED, false)

    fun isDuoHideOnShadeEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_ON_SHADE, false)

    fun setDuoHideOnShadeEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_ON_SHADE, enabled)

    fun isDuoKeepInFullscreenEnabled(): Boolean = getBoolean(KEY_DUO_KEEP_IN_FULLSCREEN, false)

    fun setDuoKeepInFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_DUO_KEEP_IN_FULLSCREEN, enabled)

    fun isDuoDisableInteractionsInFullscreenEnabled(): Boolean = getBoolean(KEY_DUO_DISABLE_INTERACTIONS_IN_FULLSCREEN, false)

    fun setDuoDisableInteractionsInFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_DUO_DISABLE_INTERACTIONS_IN_FULLSCREEN, enabled)

    fun setDuoIslandCombined(enabled: Boolean) = putBoolean(KEY_DUO_ISLAND_COMBINED, enabled)

    // Only effective while both features are on
    fun isDuoIslandCombined(): Boolean = isDuoIslandCombinedSetting() && isDuoEnabled() && getBoolean(KEY_ISLAND_ENABLED, false)

    fun isDuoTapForBriefEnabled(): Boolean = getBoolean(KEY_DUO_TAP_FOR_BRIEF, false)

    fun setDuoTapForBriefEnabled(enabled: Boolean) = putBoolean(KEY_DUO_TAP_FOR_BRIEF, enabled)

    fun isDuoTapForBriefActive(): Boolean =
        isDuoTapForBriefEnabled() && isDuoIslandCombined() && getBoolean(KEY_ISLAND_BRIEF_ENABLED, false)

    fun isDuoAutoDetectEnabled(): Boolean = getBoolean(KEY_DUO_USE_AUTO_DETECT, true)

    fun setDuoAutoDetectEnabled(enabled: Boolean) = putBoolean(KEY_DUO_USE_AUTO_DETECT, enabled)

    fun getDisplayProfileId(): String {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val display = displayManager?.getDisplay(Display.DEFAULT_DISPLAY) ?: return "default"
        val size = Point()
        @Suppress("DEPRECATION")
        display.getRealSize(size)
        if (size.x <= 0 || size.y <= 0) return "default"
        return "${minOf(size.x, size.y)}x${maxOf(size.x, size.y)}"
    }

    fun markDisplayProfileSeen() {
        val seen = prefs.getStringSet(KEY_DUO_KNOWN_DISPLAY_PROFILES, emptySet()) ?: emptySet()
        val id = getDisplayProfileId()
        if (!seen.contains(id)) {
            prefs.edit().putStringSet(KEY_DUO_KNOWN_DISPLAY_PROFILES, seen + id).apply()
        }
    }

    fun getKnownDisplayProfileCount(): Int = prefs.getStringSet(KEY_DUO_KNOWN_DISPLAY_PROFILES, emptySet())?.size ?: 0

    fun isFoldableDevice(): Boolean =
        context.packageManager.hasSystemFeature("android.hardware.sensor.hinge_angle") ||
            context.packageManager.hasSystemFeature("com.google.pixel.camera.concurrent_foldable_dual_front")

    private fun orientationKey(baseKey: String): String {
        val orientation =
            if (context.resources.configuration.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
            ) {
                "landscape"
            } else {
                "portrait"
            }
        return "$baseKey@${getDisplayProfileId()}@$orientation"
    }

    private fun getCameraPlacementFloat(
        baseKey: String,
        default: Float,
        useOrientation: Boolean,
    ): Float {
        val orientationKey = orientationKey(baseKey)
        val displayKey = "$baseKey@${getDisplayProfileId()}"
        return when {
            useOrientation && contains(orientationKey) -> getFloat(orientationKey, default)
            contains(displayKey) -> getFloat(displayKey, default)
            else -> getFloat(baseKey, default)
        }
    }

    private fun setCameraPlacementFloat(
        baseKey: String,
        value: Float,
        useOrientation: Boolean,
    ) {
        if (useOrientation) {
            putFloat(orientationKey(baseKey), value)
        } else {
            putFloat("$baseKey@${getDisplayProfileId()}", value)
            putFloat(baseKey, value)
        }
    }

    private fun getCameraHidden(
        baseKey: String,
        default: Boolean,
    ): Boolean = getBoolean("$baseKey@${getDisplayProfileId()}", getBoolean(baseKey, default))

    private fun setCameraHidden(
        baseKey: String,
        hidden: Boolean,
    ) = putBoolean("$baseKey@${getDisplayProfileId()}", hidden)

    fun isDuoOrientationProfilesEnabled(): Boolean = getBoolean(KEY_DUO_ORIENTATION_PROFILES, false)

    fun setDuoOrientationProfilesEnabled(enabled: Boolean) = putBoolean(KEY_DUO_ORIENTATION_PROFILES, enabled)

    fun isDuoHiddenInCurrentOrientation(): Boolean =
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getCameraHidden(KEY_DUO_HIDE_LANDSCAPE, false)
        } else {
            getCameraHidden(KEY_DUO_HIDE_PORTRAIT, false)
        }

    fun setDuoHiddenInCurrentOrientation(hidden: Boolean) =
        setCameraHidden(
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                KEY_DUO_HIDE_LANDSCAPE
            } else {
                KEY_DUO_HIDE_PORTRAIT
            },
            hidden,
        )

    private fun getDuoPlacementFloat(
        baseKey: String,
        default: Float,
    ): Float = getCameraPlacementFloat(baseKey, default, isDuoOrientationProfilesEnabled())

    private fun setDuoPlacementFloat(
        baseKey: String,
        value: Float,
    ) {
        setCameraPlacementFloat(baseKey, value, isDuoOrientationProfilesEnabled())
    }

    fun getDuoCameraOffsetX(): Float = getDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_X, 50f)

    fun setDuoCameraOffsetX(value: Float) = setDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_X, value)

    fun getDuoCameraOffsetY(): Float = getDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_Y, 3f)

    fun setDuoCameraOffsetY(value: Float) = setDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_Y, value)

    fun getDuoCameraSize(): Float = getDuoPlacementFloat(KEY_DUO_CAMERA_SIZE, 1.0f)

    fun setDuoCameraSize(value: Float) = setDuoPlacementFloat(KEY_DUO_CAMERA_SIZE, value)

    fun getDuoArcThickness(): Float = getFloat(KEY_DUO_ARC_THICKNESS, 4f)

    fun setDuoArcThickness(value: Float) = putFloat(KEY_DUO_ARC_THICKNESS, value)

    fun getDuoDotSize(): Float = getFloat(KEY_DUO_DOT_SIZE, 4f)

    fun setDuoDotSize(value: Float) = putFloat(KEY_DUO_DOT_SIZE, value)

    fun getDuoRingRadius(): Float = getFloat(KEY_DUO_RING_RADIUS, 1.0f)

    fun setDuoRingRadius(value: Float) = putFloat(KEY_DUO_RING_RADIUS, value)

    fun getDuoTimeTextScale(): Float = getFloat(KEY_DUO_TIME_TEXT_SCALE, 1.0f)

    fun setDuoTimeTextScale(value: Float) = putFloat(KEY_DUO_TIME_TEXT_SCALE, value)

    fun isDuoShowBatteryEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_BATTERY, true)

    fun setDuoShowBatteryEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_BATTERY, enabled)

    fun isDuoShowBatteryPercentageEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_BATTERY_PERCENTAGE, false)

    fun setDuoShowBatteryPercentageEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_BATTERY_PERCENTAGE, enabled)

    fun isDuoBatteryPercentageOnlyColoredEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED, false)

    fun setDuoBatteryPercentageOnlyColoredEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED, enabled)

    fun isDuoBatteryChargingColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED, true)

    fun setDuoBatteryChargingColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED, enabled)

    fun getDuoBatteryChargingColor(): String = getString(KEY_DUO_BATTERY_CHARGING_COLOR, "auto") ?: "auto"

    fun setDuoBatteryChargingColor(colorHex: String) = putString(KEY_DUO_BATTERY_CHARGING_COLOR, colorHex)

    fun isDuoBatteryPowerSaveColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED, true)

    fun setDuoBatteryPowerSaveColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED, enabled)

    fun getDuoBatteryPowerSaveColor(): String = getString(KEY_DUO_BATTERY_POWER_SAVE_COLOR, "#FF9800") ?: "#FF9800"

    fun setDuoBatteryPowerSaveColor(colorHex: String) = putString(KEY_DUO_BATTERY_POWER_SAVE_COLOR, colorHex)

    fun isDuoBatteryLowColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_LOW_COLOR_ENABLED, true)

    fun setDuoBatteryLowColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_LOW_COLOR_ENABLED, enabled)

    fun getDuoBatteryLowColor(): String = getString(KEY_DUO_BATTERY_LOW_COLOR, "#FFEB3B") ?: "#FFEB3B"

    fun setDuoBatteryLowColor(colorHex: String) = putString(KEY_DUO_BATTERY_LOW_COLOR, colorHex)

    fun isDuoBatteryCriticalColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED, true)

    fun setDuoBatteryCriticalColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED, enabled)

    fun getDuoBatteryCriticalColor(): String = getString(KEY_DUO_BATTERY_CRITICAL_COLOR, "#F44336") ?: "#F44336"

    fun setDuoBatteryCriticalColor(colorHex: String) = putString(KEY_DUO_BATTERY_CRITICAL_COLOR, colorHex)

    fun getDuoBatteryLowLevel(): Int = getInt(KEY_DUO_BATTERY_LOW_LEVEL, DuoBatteryLevels.DEFAULT_LOW)

    fun getDuoBatteryCriticalLevel(): Int = getInt(KEY_DUO_BATTERY_CRITICAL_LEVEL, DuoBatteryLevels.DEFAULT_CRITICAL)

    fun setDuoBatteryLevels(
        low: Int,
        critical: Int,
    ) {
        putInt(KEY_DUO_BATTERY_LOW_LEVEL, low)
        putInt(KEY_DUO_BATTERY_CRITICAL_LEVEL, critical)
    }

    fun isDuoShowNetworksEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_NETWORKS, true)

    fun setDuoShowNetworksEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_NETWORKS, enabled)

    fun isDuoDifferentiateWifiEnabled(): Boolean = getBoolean(KEY_DUO_DIFFERENTIATE_WIFI, true)

    fun setDuoDifferentiateWifiEnabled(enabled: Boolean) = putBoolean(KEY_DUO_DIFFERENTIATE_WIFI, enabled)

    fun isDuoShowTimeEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_TIME, false)

    fun setDuoShowTimeEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_TIME, enabled)

    fun isDuoShowMediaEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_MEDIA, true)

    fun setDuoShowMediaEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_MEDIA, enabled)

    fun isDuoRotateAlbumArtEnabled(): Boolean = getBoolean(KEY_DUO_ROTATE_ALBUM_ART, false)

    fun setDuoRotateAlbumArtEnabled(enabled: Boolean) = putBoolean(KEY_DUO_ROTATE_ALBUM_ART, enabled)

    fun isDuoShowProgressEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_PROGRESS, true)

    fun setDuoShowProgressEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_PROGRESS, enabled)

    fun isDuoShowFlashlightEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_FLASHLIGHT, true)

    fun setDuoShowFlashlightEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_FLASHLIGHT, enabled)

    fun isDuoHideWhenScreenOffEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF, true)

    fun setDuoHideWhenScreenOffEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF, enabled)

    fun isDuoHideWhenScreenOffOnlyIdleEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE, false)

    fun setDuoHideWhenScreenOffOnlyIdleEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE, enabled)

    fun isDuoHideWhenLockedEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_WHEN_LOCKED, false)

    fun setDuoHideWhenLockedEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_WHEN_LOCKED, enabled)

    fun isDuoUseMaterialYouEnabled(): Boolean = getBoolean(KEY_DUO_USE_MATERIAL_YOU, true)

    fun setDuoUseMaterialYouEnabled(enabled: Boolean) = putBoolean(KEY_DUO_USE_MATERIAL_YOU, enabled)

    fun getDuoCustomColor(): String = getString(KEY_DUO_CUSTOM_COLOR, "#FFFFFF") ?: "#FFFFFF"

    fun setDuoCustomColor(colorHex: String) = putString(KEY_DUO_CUSTOM_COLOR, colorHex)

    private fun getRemapAction(key: String): Action? {
        val json = prefs.getString(key, null) ?: return null
        return try {
            ActionGsonAdapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    private fun setRemapAction(
        key: String,
        action: Action?,
    ) {
        if (action == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putString(key, ActionGsonAdapter.toJson(action)).apply()
        }
    }

    fun getDuoTapAction(): Action? = getRemapAction(KEY_DUO_TAP_ACTION)

    fun setDuoTapAction(action: Action?) = setRemapAction(KEY_DUO_TAP_ACTION, action)

    fun getDuoDoubleTapAction(): Action? = getRemapAction(KEY_DUO_DOUBLE_TAP_ACTION)

    fun setDuoDoubleTapAction(action: Action?) = setRemapAction(KEY_DUO_DOUBLE_TAP_ACTION, action)

    fun getDuoLongPressAction(): Action? = getRemapAction(KEY_DUO_LONG_PRESS_ACTION)

    fun setDuoLongPressAction(action: Action?) = setRemapAction(KEY_DUO_LONG_PRESS_ACTION, action)

    fun getDuoSwipeDownAction(): Action? = getRemapAction(KEY_DUO_SWIPE_DOWN_ACTION)

    fun setDuoSwipeDownAction(action: Action?) = setRemapAction(KEY_DUO_SWIPE_DOWN_ACTION, action)

    fun getDuoSlideMode(): String = getString(KEY_DUO_SLIDE_MODE, "none") ?: "none"

    fun setDuoSlideMode(mode: String) = putString(KEY_DUO_SLIDE_MODE, mode)

    fun isDuoSlideTrackEnabled(): Boolean = getBoolean(KEY_DUO_SLIDE_TRACK, false)

    fun setDuoSlideTrackEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SLIDE_TRACK, enabled)

    fun isDuoSlideInvertDirectionEnabled(): Boolean = getBoolean(KEY_DUO_SLIDE_INVERT_DIRECTION, false)

    fun setDuoSlideInvertDirection(enabled: Boolean) = putBoolean(KEY_DUO_SLIDE_INVERT_DIRECTION, enabled)
}
