package com.jawzf.pixeltoolkit.core.data.glance

import android.content.Context
import android.content.res.Configuration
import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore

class StatusGlanceSettings(
    context: Context,
) : PrefsStore(context) {
    companion object {
        const val KEY_STATUS_GLANCE_ENABLED = "status_glance_enabled"
        const val KEY_STATUS_GLANCE_USE_AUTO_DETECT = "status_glance_use_auto_detect"
        const val KEY_STATUS_GLANCE_OFFSET_X = "status_glance_offset_x"
        const val KEY_STATUS_GLANCE_OFFSET_Y = "status_glance_offset_y"
        const val KEY_STATUS_GLANCE_ORIENTATION_PROFILES = "status_glance_orientation_profiles"
        const val KEY_STATUS_GLANCE_HIDE_PORTRAIT = "status_glance_hide_portrait"
        const val KEY_STATUS_GLANCE_HIDE_LANDSCAPE = "status_glance_hide_landscape"
        const val KEY_STATUS_GLANCE_MAX_WIDTH = "status_glance_max_width"
        const val KEY_STATUS_GLANCE_FONT_SIZE = "status_glance_font_size"
        const val KEY_STATUS_GLANCE_SHOW_FLASHLIGHT = "status_glance_show_flashlight"
        const val KEY_STATUS_GLANCE_SHOW_CALENDAR = "status_glance_show_calendar"
        const val KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME = "status_glance_calendar_timeframe"
        const val KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS = "status_glance_calendar_selected_calendars"
        const val KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY = "status_glance_calendar_show_all_day"
        const val KEY_STATUS_GLANCE_SHOW_MEDIA = "status_glance_show_media"
        const val KEY_STATUS_GLANCE_SHOW_TIME = "status_glance_show_time"
        const val KEY_STATUS_GLANCE_BACKGROUND_PILL = "status_glance_background_pill"
        const val KEY_STATUS_GLANCE_ALBUM_ART_COLORS = "status_glance_album_art_colors"
        const val KEY_STATUS_GLANCE_SHOW_BATTERY = "status_glance_show_battery"
        const val KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE = "status_glance_battery_display_mode"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_LOW = "status_glance_battery_show_low"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING = "status_glance_battery_show_charging"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_FULL = "status_glance_battery_show_full"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE = "status_glance_battery_show_otherwise"
        const val KEY_STATUS_GLANCE_BATTERY_ICON_SIZE = "status_glance_battery_icon_size"
        const val KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN = "status_glance_hide_when_fullscreen"
        const val KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS = "status_glance_hide_in_quick_settings"
        const val KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED = "status_glance_hide_when_locked"
        const val KEY_STATUS_GLANCE_BOLD_TEXT = "status_glance_bold_text"
        const val KEY_STATUS_GLANCE_TAP_ACTION = "status_glance_tap_action"
        const val KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION = "status_glance_double_tap_action"
        const val KEY_STATUS_GLANCE_LONG_PRESS_ACTION = "status_glance_long_press_action"
    }

    private val gson = Gson()

    private fun orientationKey(baseKey: String): String {
        val orientation =
            if (context.resources.configuration.orientation ==
                Configuration.ORIENTATION_LANDSCAPE
            ) {
                "landscape"
            } else {
                "portrait"
            }
        return "$baseKey@${DuoSettings(context).getDisplayProfileId()}@$orientation"
    }

    private fun getCameraPlacementFloat(
        baseKey: String,
        default: Float,
        useOrientation: Boolean,
    ): Float {
        val orientationKey = orientationKey(baseKey)
        val displayKey = "$baseKey@${DuoSettings(context).getDisplayProfileId()}"
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
            putFloat("$baseKey@${DuoSettings(context).getDisplayProfileId()}", value)
            putFloat(baseKey, value)
        }
    }

    private fun getCameraHidden(
        baseKey: String,
        default: Boolean,
    ): Boolean = getBoolean("$baseKey@${DuoSettings(context).getDisplayProfileId()}", getBoolean(baseKey, default))

    private fun setCameraHidden(
        baseKey: String,
        hidden: Boolean,
    ) = putBoolean("$baseKey@${DuoSettings(context).getDisplayProfileId()}", hidden)

    fun isStatusGlanceOrientationProfilesEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_ORIENTATION_PROFILES, false)

    fun setStatusGlanceOrientationProfilesEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_ORIENTATION_PROFILES, enabled)

    fun isStatusGlanceHiddenInCurrentOrientation(): Boolean =
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getCameraHidden(KEY_STATUS_GLANCE_HIDE_LANDSCAPE, true)
        } else {
            getCameraHidden(KEY_STATUS_GLANCE_HIDE_PORTRAIT, false)
        }

    fun setStatusGlanceHiddenInCurrentOrientation(hidden: Boolean) =
        setCameraHidden(
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                KEY_STATUS_GLANCE_HIDE_LANDSCAPE
            } else {
                KEY_STATUS_GLANCE_HIDE_PORTRAIT
            },
            hidden,
        )

    fun isStatusGlanceEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_ENABLED, false)

    fun setStatusGlanceEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_ENABLED, enabled)

    fun isStatusGlanceAutoDetectEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_USE_AUTO_DETECT, true)

    fun setStatusGlanceAutoDetectEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_USE_AUTO_DETECT, enabled)

    fun getStatusGlanceOffsetX(): Float =
        getCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_X, 60f, isStatusGlanceOrientationProfilesEnabled())

    fun setStatusGlanceOffsetX(value: Float) =
        setCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_X, value, isStatusGlanceOrientationProfilesEnabled())

    fun getStatusGlanceOffsetY(): Float =
        getCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_Y, 2f, isStatusGlanceOrientationProfilesEnabled())

    fun setStatusGlanceOffsetY(value: Float) =
        setCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_Y, value, isStatusGlanceOrientationProfilesEnabled())

    fun getStatusGlanceMaxWidth(): Float = getFloat(KEY_STATUS_GLANCE_MAX_WIDTH, 180f)

    fun setStatusGlanceMaxWidth(value: Float) = putFloat(KEY_STATUS_GLANCE_MAX_WIDTH, value)

    fun getStatusGlanceFontSize(): Float = getFloat(KEY_STATUS_GLANCE_FONT_SIZE, 13f)

    fun setStatusGlanceFontSize(value: Float) = putFloat(KEY_STATUS_GLANCE_FONT_SIZE, value)

    fun isStatusGlanceShowFlashlightEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_FLASHLIGHT, true)

    fun setStatusGlanceShowFlashlightEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_FLASHLIGHT, enabled)

    fun isStatusGlanceShowCalendarEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_CALENDAR, true)

    fun setStatusGlanceShowCalendarEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_CALENDAR, enabled)

    fun getStatusGlanceCalendarTimeframe(): String = getString(KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME, "today") ?: "today"

    fun setStatusGlanceCalendarTimeframe(timeframe: String) = putString(KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME, timeframe)

    fun isStatusGlanceCalendarShowAllDayEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY, false)

    fun setStatusGlanceCalendarShowAllDayEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY, enabled)

    fun getStatusGlanceCalendarSelectedCalendars(): Set<String> {
        val json = prefs.getString(KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS, null)
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

    fun saveStatusGlanceCalendarSelectedCalendars(calendarIds: Set<String>) {
        val json = gson.toJson(calendarIds)
        putString(KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS, json)
    }

    fun isStatusGlanceShowMediaEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_MEDIA, true)

    fun setStatusGlanceShowMediaEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_MEDIA, enabled)

    fun isStatusGlanceShowTimeEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_TIME, true)

    fun setStatusGlanceShowTimeEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_TIME, enabled)

    fun isStatusGlanceBackgroundPillEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BACKGROUND_PILL, false)

    fun setStatusGlanceBackgroundPillEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BACKGROUND_PILL, enabled)

    fun isStatusGlanceAlbumArtColorsEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_ALBUM_ART_COLORS, true)

    fun setStatusGlanceAlbumArtColorsEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_ALBUM_ART_COLORS, enabled)

    fun isStatusGlanceHideWhenFullscreenEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN, true)

    fun setStatusGlanceHideWhenFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN, enabled)

    fun isStatusGlanceHideInQuickSettingsEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS, true)

    fun setStatusGlanceHideInQuickSettingsEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS, enabled)

    fun isStatusGlanceHideWhenLockedEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED, true)

    fun setStatusGlanceHideWhenLockedEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED, enabled)

    fun isStatusGlanceBoldTextEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BOLD_TEXT, false)

    fun setStatusGlanceBoldTextEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BOLD_TEXT, enabled)

    fun isStatusGlanceShowBatteryEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_BATTERY, false)

    fun setStatusGlanceShowBatteryEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_BATTERY, enabled)

    fun getStatusGlanceBatteryDisplayMode(): String = getString(KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE, "icon") ?: "icon"

    fun setStatusGlanceBatteryDisplayMode(mode: String) = putString(KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE, mode)

    fun isStatusGlanceBatteryShowLowEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_LOW, true)

    fun setStatusGlanceBatteryShowLowEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_LOW, enabled)

    fun isStatusGlanceBatteryShowChargingEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING, true)

    fun setStatusGlanceBatteryShowChargingEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING, enabled)

    fun isStatusGlanceBatteryShowFullEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_FULL, true)

    fun setStatusGlanceBatteryShowFullEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_FULL, enabled)

    fun isStatusGlanceBatteryShowOtherwiseEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE, true)

    fun setStatusGlanceBatteryShowOtherwiseEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE, enabled)

    fun getStatusGlanceBatteryIconSize(): Float = getFloat(KEY_STATUS_GLANCE_BATTERY_ICON_SIZE, 16f)

    fun setStatusGlanceBatteryIconSize(size: Float) = putFloat(KEY_STATUS_GLANCE_BATTERY_ICON_SIZE, size)

    fun getStatusGlanceTapAction(): Action? = getRemapAction(KEY_STATUS_GLANCE_TAP_ACTION)

    fun setStatusGlanceTapAction(action: Action?) = setRemapAction(KEY_STATUS_GLANCE_TAP_ACTION, action)

    fun getStatusGlanceDoubleTapAction(): Action? = getRemapAction(KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION)

    fun setStatusGlanceDoubleTapAction(action: Action?) = setRemapAction(KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION, action)

    fun getStatusGlanceLongPressAction(): Action? = getRemapAction(KEY_STATUS_GLANCE_LONG_PRESS_ACTION)

    fun setStatusGlanceLongPressAction(action: Action?) = setRemapAction(KEY_STATUS_GLANCE_LONG_PRESS_ACTION, action)
}
