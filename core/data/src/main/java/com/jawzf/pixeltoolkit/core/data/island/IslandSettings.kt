package com.jawzf.pixeltoolkit.core.data.island

import android.content.Context
import android.content.res.Configuration
import android.provider.Settings
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.pickers.NetworkType
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import org.json.JSONObject

open class IslandSettings(
    context: Context,
) : PrefsStore(context) {
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

    private fun getCameraPlacementString(
        baseKey: String,
        default: String,
        useOrientation: Boolean,
    ): String {
        val displayKey = "$baseKey@${DuoSettings(context).getDisplayProfileId()}"
        return when {
            useOrientation && contains(orientationKey(baseKey)) -> getString(orientationKey(baseKey), default)
            contains(displayKey) -> getString(displayKey, default)
            else -> getString(baseKey, default)
        } ?: default
    }

    private fun setCameraPlacementString(
        baseKey: String,
        value: String,
        useOrientation: Boolean,
    ) {
        if (useOrientation) {
            putString(orientationKey(baseKey), value)
        } else {
            putString("$baseKey@${DuoSettings(context).getDisplayProfileId()}", value)
            putString(baseKey, value)
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

    private fun updateAppSelection(
        key: String,
        packageName: String,
        enabled: Boolean,
    ) {
        val current = loadAppSelection(key).toMutableList()
        val index = current.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            current[index] = current[index].copy(isEnabled = enabled)
        } else {
            current.add(AppSelection(packageName, enabled))
        }
        saveAppSelection(key, current)
    }

    fun isIslandOrientationProfilesEnabled(): Boolean = getBoolean(KEY_ISLAND_ORIENTATION_PROFILES, false)

    fun setIslandOrientationProfilesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_ORIENTATION_PROFILES, enabled)

    fun isIslandHiddenInCurrentOrientation(): Boolean =
        if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getCameraHidden(KEY_ISLAND_HIDE_LANDSCAPE, !isIslandKeepOnLandscapeEnabled())
        } else {
            getCameraHidden(KEY_ISLAND_HIDE_PORTRAIT, false)
        }

    fun setIslandHiddenInCurrentOrientation(hidden: Boolean) =
        setCameraHidden(
            if (context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                KEY_ISLAND_HIDE_LANDSCAPE
            } else {
                KEY_ISLAND_HIDE_PORTRAIT
            },
            hidden,
        )

    fun isIslandBatteryIdleColorEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED, false)

    fun setIslandBatteryIdleColorEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED, enabled)

    fun getIslandBatteryIdleColor(): String = getString(KEY_ISLAND_BATTERY_IDLE_COLOR, "#FFFFFF") ?: "#FFFFFF"

    fun setIslandBatteryIdleColor(colorHex: String) = putString(KEY_ISLAND_BATTERY_IDLE_COLOR, colorHex)

    // Island
    fun isIslandEnabled(): Boolean = getBoolean(KEY_ISLAND_ENABLED, false)

    fun setIslandEnabled(enabled: Boolean) {
        putBoolean(KEY_ISLAND_ENABLED, enabled)
        if (isIslandSuppressSystemHeadsUpEnabled()) applyHeadsUpSuppression(enabled)
    }

    fun isIslandAutoDetectEnabled(): Boolean = getBoolean(KEY_ISLAND_USE_AUTO_DETECT, true)

    fun setIslandAutoDetectEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_USE_AUTO_DETECT, enabled)

    fun getIslandCameraOffsetX(): Float = getCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_X, 50f, isIslandOrientationProfilesEnabled())

    fun setIslandCameraOffsetX(value: Float) =
        setCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_X, value, isIslandOrientationProfilesEnabled())

    fun getIslandCameraOffsetY(): Float = getCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_Y, 3f, isIslandOrientationProfilesEnabled())

    fun setIslandCameraOffsetY(value: Float) =
        setCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_Y, value, isIslandOrientationProfilesEnabled())

    fun getIslandCameraSize(): Float = getCameraPlacementFloat(KEY_ISLAND_CAMERA_SIZE, 1.0f, isIslandOrientationProfilesEnabled())

    fun setIslandCameraSize(value: Float) = setCameraPlacementFloat(KEY_ISLAND_CAMERA_SIZE, value, isIslandOrientationProfilesEnabled())

    fun getIslandMaxWidth(): Float = getFloat(KEY_ISLAND_MAX_WIDTH, 360f)

    fun setIslandMaxWidth(value: Float) = putFloat(KEY_ISLAND_MAX_WIDTH, value)

    fun getIslandCutoutGap(): Float = getFloat(KEY_ISLAND_CUTOUT_GAP, 6f)

    fun setIslandCutoutGap(value: Float) = putFloat(KEY_ISLAND_CUTOUT_GAP, value)

    fun isIslandSuppressSystemHeadsUpEnabled(): Boolean = getBoolean(KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP, false)

    fun setIslandSuppressSystemHeadsUpEnabled(enabled: Boolean) {
        putBoolean(KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP, enabled)
        applyHeadsUpSuppression(enabled && isIslandEnabled())
    }

    fun isIslandHideWhenScreenOffEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_WHEN_SCREEN_OFF, true)

    fun setIslandHideWhenScreenOffEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_WHEN_SCREEN_OFF, enabled)

    fun getIslandShowWhen(): String =
        getString(KEY_ISLAND_SHOW_WHEN, null)
            ?: if (isIslandHideWhenScreenOffEnabled()) ISLAND_SHOW_WHEN_UNLOCKED else ISLAND_SHOW_WHEN_ALWAYS

    fun setIslandShowWhen(value: String) = putString(KEY_ISLAND_SHOW_WHEN, value)

    fun getIslandTimeoutMs(): Long = getLong(KEY_ISLAND_TIMEOUT_MS, 4500L)

    fun setIslandTimeoutMs(value: Long) = putLong(KEY_ISLAND_TIMEOUT_MS, value)

    fun isIslandBorderOutlineEnabled(): Boolean = getBoolean(KEY_ISLAND_BORDER_OUTLINE_ENABLED, false)

    fun setIslandBorderOutlineEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BORDER_OUTLINE_ENABLED, enabled)

    fun getIslandBorderOutlineColor(): String =
        getString(KEY_ISLAND_BORDER_OUTLINE_COLOR, ISLAND_BORDER_OUTLINE_DEFAULT_COLOR) ?: ISLAND_BORDER_OUTLINE_DEFAULT_COLOR

    fun setIslandBorderOutlineColor(colorHex: String) = putString(KEY_ISLAND_BORDER_OUTLINE_COLOR, colorHex)

    fun isIslandBorderOutlineDynamicEnabled(): Boolean = getBoolean(KEY_ISLAND_BORDER_OUTLINE_DYNAMIC, false)

    fun setIslandBorderOutlineDynamicEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BORDER_OUTLINE_DYNAMIC, enabled)

    fun isIslandBorderOutlineHiddenWhenExpanded(): Boolean = getBoolean(KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED, false)

    fun setIslandBorderOutlineHiddenWhenExpanded(hidden: Boolean) = putBoolean(KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED, hidden)

    fun getIslandBorderOutlineThickness(): Float = getFloat(KEY_ISLAND_BORDER_OUTLINE_THICKNESS, 1f)

    fun setIslandBorderOutlineThickness(value: Float) = putFloat(KEY_ISLAND_BORDER_OUTLINE_THICKNESS, value)

    fun isIslandPulseShadowEnabled(): Boolean = getBoolean(KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION, false)

    fun setIslandPulseShadowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION, enabled)

    fun getIslandPulseShadowSize(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_SIZE, 0.5f)

    fun setIslandPulseShadowSize(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_SIZE, value)

    fun getIslandPulseShadowYShift(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_Y_SHIFT, 0.35f)

    fun setIslandPulseShadowYShift(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_Y_SHIFT, value)

    fun getIslandPulseShadowSpread(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_SPREAD, 2f)

    fun setIslandPulseShadowSpread(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_SPREAD, value)

    fun getIslandPulseShadowDurationMs(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_DURATION_MS, 1450f)

    fun setIslandPulseShadowDurationMs(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_DURATION_MS, value)

    fun isIslandShowGlowEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_GLOW, true)

    fun setIslandShowGlowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_GLOW, enabled)

    fun getIslandExpandedWidth(): Float = getFloat(KEY_ISLAND_EXPANDED_WIDTH, 360f)

    fun setIslandExpandedWidth(value: Float) = putFloat(KEY_ISLAND_EXPANDED_WIDTH, value)

    fun getIslandExpandedRoundness(): Float = getFloat(KEY_ISLAND_EXPANDED_ROUNDNESS, 24f)

    fun setIslandExpandedRoundness(value: Float) = putFloat(KEY_ISLAND_EXPANDED_ROUNDNESS, value)

    fun getIslandExpandedPadding(): Float = getFloat(KEY_ISLAND_EXPANDED_PADDING, 16f)

    fun setIslandExpandedPadding(value: Float) = putFloat(KEY_ISLAND_EXPANDED_PADDING, value)

    fun getIslandExpandedTopPadding(): Float = getFloat(KEY_ISLAND_EXPANDED_TOP_PADDING, 0f)

    fun setIslandExpandedTopPadding(value: Float) = putFloat(KEY_ISLAND_EXPANDED_TOP_PADDING, value)

    fun getIslandExpandedBottomPadding(): Float = getFloat(KEY_ISLAND_EXPANDED_BOTTOM_PADDING, 12f)

    fun setIslandExpandedBottomPadding(value: Float) = putFloat(KEY_ISLAND_EXPANDED_BOTTOM_PADDING, value)

    fun getIslandExpandedTimeoutMs(): Long = getLong(KEY_ISLAND_EXPANDED_TIMEOUT_MS, 0L)

    fun setIslandExpandedTimeoutMs(value: Long) = putLong(KEY_ISLAND_EXPANDED_TIMEOUT_MS, value)

    fun isIslandCatchUpEnabled(): Boolean = isIslandNotifHubEnabled() || getBoolean(KEY_ISLAND_CATCH_UP_ENABLED, true)

    fun setIslandCatchUpEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_CATCH_UP_ENABLED, enabled)

    // The stored value is kept as the hub's pin duration while the bubble itself never expires
    fun getIslandCatchUpStoredMs(): Long = getLong(KEY_ISLAND_CATCH_UP_TIMEOUT_MS, 10000L)

    fun getIslandCatchUpTimeoutMs(): Long = if (isIslandNotifHubEnabled()) 0L else getIslandCatchUpStoredMs()

    fun setIslandCatchUpTimeoutMs(value: Long) = putLong(KEY_ISLAND_CATCH_UP_TIMEOUT_MS, value)

    fun isIslandCatchUpInfinite(): Boolean = getIslandCatchUpTimeoutMs() <= 0L

    // Ignores the hub's override, for features that borrow the catch-up duration
    fun isIslandCatchUpStoredInfinite(): Boolean = getIslandCatchUpStoredMs() <= 0L

    fun isIslandShowMediaEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_MEDIA, true)

    fun setIslandShowMediaEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_MEDIA, enabled)

    fun loadIslandMediaExcludedApps() = loadAppSelection(KEY_ISLAND_MEDIA_EXCLUDED_APPS)

    fun saveIslandMediaExcludedApps(apps: List<AppSelection>) = saveAppSelection(KEY_ISLAND_MEDIA_EXCLUDED_APPS, apps)

    fun updateIslandMediaExcludedAppSelection(
        packageName: String,
        enabled: Boolean,
    ) = updateAppSelection(KEY_ISLAND_MEDIA_EXCLUDED_APPS, packageName, enabled)

    fun isIslandShowCalendarEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CALENDAR, false)

    fun setIslandShowCalendarEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CALENDAR, enabled)

    fun isIslandShowConsciousGateEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CONSCIOUS_GATE, true)

    fun setIslandShowConsciousGateEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CONSCIOUS_GATE, enabled)

    fun isIslandShowFlashlightEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_FLASHLIGHT, true)

    fun setIslandShowFlashlightEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_FLASHLIGHT, enabled)

    fun isIslandShowTimeBatteryEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_TIME_BATTERY, false)

    fun setIslandShowTimeBatteryEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_TIME_BATTERY, enabled)

    fun isIslandShowBatteryEnabled(): Boolean =
        if (contains(KEY_ISLAND_SHOW_BATTERY)) getBoolean(KEY_ISLAND_SHOW_BATTERY, false) else isIslandShowTimeBatteryEnabled()

    fun setIslandShowBatteryEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_BATTERY, enabled)

    fun isIslandBatteryPeekAlertsEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_PEEK_ALERTS, true)

    fun setIslandBatteryPeekAlertsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_PEEK_ALERTS, enabled)

    fun getIslandBatteryStyle(): String = getString(KEY_ISLAND_BATTERY_STYLE, ISLAND_BATTERY_STYLE_RING) ?: ISLAND_BATTERY_STYLE_RING

    fun setIslandBatteryStyle(value: String) = putString(KEY_ISLAND_BATTERY_STYLE, value)

    fun getIslandDevicesBatteryStyle(): String =
        getString(KEY_ISLAND_DEVICES_BATTERY_STYLE, ISLAND_BATTERY_STYLE_RING) ?: ISLAND_BATTERY_STYLE_RING

    fun setIslandDevicesBatteryStyle(value: String) = putString(KEY_ISLAND_DEVICES_BATTERY_STYLE, value)

    fun isIslandBatteryPercentageEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_PERCENTAGE, false)

    fun setIslandBatteryPercentageEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_PERCENTAGE, enabled)

    fun getIslandLongPressAction(): Action? = getRemapAction(KEY_ISLAND_LONG_PRESS_ACTION)

    fun setIslandLongPressAction(action: Action?) = setRemapAction(KEY_ISLAND_LONG_PRESS_ACTION, action)

    fun getIslandSlideMode(): String = getString(KEY_ISLAND_SLIDE_MODE, "none") ?: "none"

    fun setIslandSlideMode(mode: String) = putString(KEY_ISLAND_SLIDE_MODE, mode)

    fun isIslandLikeWhilePlayingEnabled(): Boolean = getBoolean(KEY_ISLAND_LIKE_WHILE_PLAYING, false)

    fun setIslandLikeWhilePlayingEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_LIKE_WHILE_PLAYING, enabled)

    fun isIslandSlideTrackEnabled(): Boolean = getBoolean(KEY_ISLAND_SLIDE_TRACK, false)

    fun setIslandSlideTrackEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SLIDE_TRACK, enabled)

    fun isIslandSlideInvertDirectionEnabled(): Boolean = getBoolean(KEY_ISLAND_SLIDE_INVERT_DIRECTION, true)

    fun setIslandSlideInvertDirection(enabled: Boolean) = putBoolean(KEY_ISLAND_SLIDE_INVERT_DIRECTION, enabled)

    fun isIslandBatteryIconConditional(): Boolean = getBoolean(KEY_ISLAND_BATTERY_ICON_CONDITIONAL, false)

    fun setIslandBatteryIconConditional(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_ICON_CONDITIONAL, enabled)

    fun isIslandBatteryPercentageConditional(): Boolean = getBoolean(KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL, false)

    fun isIslandBatteryOnlyLowEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_ONLY_LOW, false)

    fun setIslandBatteryOnlyLowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_ONLY_LOW, enabled)

    fun isIslandDevicesBatteryOnlyLowEnabled(): Boolean = getBoolean(KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW, false)

    fun setIslandDevicesBatteryOnlyLowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW, enabled)

    fun setIslandBatteryPercentageConditional(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL, enabled)

    fun getIslandCalendarEmojis(): Map<Long, String> =
        try {
            val json = JSONObject(getString(KEY_ISLAND_CALENDAR_EMOJIS, "{}") ?: "{}")
            json
                .keys()
                .asSequence()
                .mapNotNull { key -> key.toLongOrNull()?.let { it to json.getString(key) } }
                .toMap()
        } catch (_: Exception) {
            emptyMap()
        }

    fun setIslandCalendarEmoji(
        calendarId: Long,
        emoji: String?,
    ) {
        val updated = getIslandCalendarEmojis().toMutableMap()
        if (emoji.isNullOrBlank()) updated.remove(calendarId) else updated[calendarId] = emoji
        val json = JSONObject()
        updated.forEach { (id, value) -> json.put(id.toString(), value) }
        putString(KEY_ISLAND_CALENDAR_EMOJIS, json.toString())
    }

    fun isIslandShowTimersEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_TIMERS, true)

    fun setIslandShowTimersEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_TIMERS, enabled)

    fun isIslandTimersShowScreenRecorderEnabled(): Boolean = getBoolean(KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER, true)

    fun setIslandTimersShowScreenRecorderEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER, enabled)

    fun isIslandTimersFilterAppsEnabled(): Boolean = getBoolean(KEY_ISLAND_TIMERS_FILTER_APPS, false)

    fun setIslandTimersFilterAppsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_TIMERS_FILTER_APPS, enabled)

    fun loadIslandTimersSelectedApps() = loadAppSelection(KEY_ISLAND_TIMERS_SELECTED_APPS)

    fun saveIslandTimersSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_ISLAND_TIMERS_SELECTED_APPS, apps)

    fun updateIslandTimersAppSelection(
        packageName: String,
        enabled: Boolean,
    ) = updateAppSelection(KEY_ISLAND_TIMERS_SELECTED_APPS, packageName, enabled)

    fun isIslandShowNetworkEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_NETWORK, true)

    fun setIslandShowNetworkEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_NETWORK, enabled)

    fun isIslandNetworkActivityEnabled(): Boolean = getBoolean(KEY_ISLAND_NETWORK_ACTIVITY, true)

    fun setIslandNetworkActivityEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NETWORK_ACTIVITY, enabled)

    fun isIslandSignalLowOnlyEnabled(): Boolean = getBoolean(KEY_ISLAND_SIGNAL_LOW_ONLY, false)

    fun setIslandSignalLowOnlyEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SIGNAL_LOW_ONLY, enabled)

    fun getIslandPriorityOrder(): List<String>? =
        getString(KEY_ISLAND_PRIORITY_ORDER, null)?.split(",")?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

    fun setIslandPriorityOrder(order: List<String>?) = putString(KEY_ISLAND_PRIORITY_ORDER, order?.joinToString(","))

    fun isIslandSignalWifiEnabled(): Boolean = getBoolean(KEY_ISLAND_SIGNAL_WIFI, false)

    fun setIslandSignalWifiEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SIGNAL_WIFI, enabled)

    fun isIslandShowSignalEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_SIGNAL, false)

    fun setIslandShowSignalEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_SIGNAL, enabled)

    fun isIslandSignalShowModeEnabled(): Boolean = getBoolean(KEY_ISLAND_SIGNAL_SHOW_MODE, false)

    fun setIslandSignalShowModeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SIGNAL_SHOW_MODE, enabled)

    fun getIslandSignalNetworkTypes(): Set<NetworkType> =
        (getString(KEY_ISLAND_SIGNAL_NETWORK_TYPES, "NETWORK_5G,NETWORK_4G,NETWORK_3G,NETWORK_OTHER") ?: "")
            .split(",")
            .mapNotNull { name -> NetworkType.entries.firstOrNull { it.name == name } }
            .toSet()

    fun setIslandSignalNetworkTypes(types: Set<NetworkType>) =
        putString(KEY_ISLAND_SIGNAL_NETWORK_TYPES, types.joinToString(",") { it.name })

    fun isIslandShowSoundModeEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_SOUND_MODE, true)

    fun setIslandShowSoundModeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_SOUND_MODE, enabled)

    fun isIslandSoundModeKeepIconEnabled(): Boolean = getBoolean(KEY_ISLAND_SOUND_MODE_KEEP_ICON, true)

    fun setIslandSoundModeKeepIconEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SOUND_MODE_KEEP_ICON, enabled)

    fun isIslandShowAlarmEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_ALARM, false)

    fun setIslandShowAlarmEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_ALARM, enabled)

    fun getIslandAlarmWindowHours(): Int = getInt(KEY_ISLAND_ALARM_WINDOW_HOURS, 12)

    fun setIslandAlarmWindowHours(hours: Int) = putInt(KEY_ISLAND_ALARM_WINDOW_HOURS, hours)

    fun isIslandShowCaffeinateEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CAFFEINATE, true)

    fun setIslandShowCaffeinateEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CAFFEINATE, enabled)

    fun isIslandCaffeinateShowTimeEnabled(): Boolean = getBoolean(KEY_ISLAND_CAFFEINATE_SHOW_TIME, false)

    fun setIslandCaffeinateShowTimeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_CAFFEINATE_SHOW_TIME, enabled)

    fun isIslandShowDevicesEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_DEVICES, true)

    fun setIslandShowDevicesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_DEVICES, enabled)

    fun isIslandShowNavigationEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_NAVIGATION, true)

    fun setIslandShowNavigationEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_NAVIGATION, enabled)

    fun getIslandNavigationOnNew(): String =
        getString(KEY_ISLAND_NAVIGATION_ON_NEW, ISLAND_NAVIGATION_ON_NEW_PEEK) ?: ISLAND_NAVIGATION_ON_NEW_PEEK

    fun setIslandNavigationOnNew(mode: String) = putString(KEY_ISLAND_NAVIGATION_ON_NEW, mode)

    fun isIslandNavigationPeekAfterExpandEnabled(): Boolean = getBoolean(KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND, false)

    fun setIslandNavigationPeekAfterExpandEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND, enabled)

    fun getIslandNavigationExpandDistance(): Int = getInt(KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE, 200)

    fun setIslandNavigationExpandDistance(meters: Int) = putInt(KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE, meters)

    fun isIslandShowHotspotEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_HOTSPOT, false)

    fun setIslandShowHotspotEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_HOTSPOT, enabled)

    fun isIslandBriefEnabled(): Boolean = getBoolean(KEY_ISLAND_BRIEF_ENABLED, false)

    fun setIslandBriefEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BRIEF_ENABLED, enabled)

    fun isIslandBriefShowAlarmEnabled(): Boolean = getBoolean(KEY_ISLAND_BRIEF_SHOW_ALARM, true)

    fun setIslandBriefShowAlarmEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BRIEF_SHOW_ALARM, enabled)

    // Ordered addresses; the first connected one with a battery reading is shown
    fun getIslandDevicesBatteryOrder(): List<String> =
        getString(KEY_ISLAND_DEVICES_BATTERY_ORDER, "").orEmpty().split(',').filter { it.isNotBlank() }

    fun setIslandDevicesBatteryOrder(addresses: List<String>) = putString(KEY_ISLAND_DEVICES_BATTERY_ORDER, addresses.joinToString(","))

    fun isIslandShowCallsEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CALLS, true)

    fun setIslandShowCallsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CALLS, enabled)

    fun getIslandCameraPosition(): String =
        getCameraPlacementString(KEY_ISLAND_CAMERA_POSITION, ISLAND_CAMERA_POSITION_CENTER, isIslandOrientationProfilesEnabled())

    fun setIslandCameraPosition(value: String) =
        setCameraPlacementString(KEY_ISLAND_CAMERA_POSITION, value, isIslandOrientationProfilesEnabled())

    fun isIslandPreviewRingEnabled(): Boolean = getBoolean(KEY_ISLAND_PREVIEW_RING, false)

    fun setIslandPreviewRingEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_PREVIEW_RING, enabled)

    fun getIslandPreviewStage(): String = getString(KEY_ISLAND_PREVIEW_STAGE, ISLAND_PREVIEW_STAGE_AUTO) ?: ISLAND_PREVIEW_STAGE_AUTO

    fun setIslandPreviewStage(value: String) = putString(KEY_ISLAND_PREVIEW_STAGE, value)

    fun getIslandExpandedScale(): Float = getFloat(KEY_ISLAND_EXPANDED_SCALE, 1f)

    fun setIslandExpandedScale(value: Float) = putFloat(KEY_ISLAND_EXPANDED_SCALE, value)

    fun isIslandHideLiveUpdatesEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_LIVE_UPDATES, false)

    fun setIslandHideLiveUpdatesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_LIVE_UPDATES, enabled)

    fun isIslandHideStatusBarInsetsEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_STATUS_BAR_INSETS, false)

    fun setIslandHideStatusBarInsetsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_STATUS_BAR_INSETS, enabled)

    fun isIslandDismissOnOutsideEnabled(): Boolean = getBoolean(KEY_ISLAND_DISMISS_ON_OUTSIDE, false)

    fun setIslandDismissOnOutsideEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_DISMISS_ON_OUTSIDE, enabled)

    fun isIslandHideInOwnerAppEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_IN_OWNER_APP, false)

    fun setIslandHideInOwnerAppEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_IN_OWNER_APP, enabled)

    fun isIslandKeepOnLandscapeEnabled(): Boolean = getBoolean(KEY_ISLAND_KEEP_ON_LANDSCAPE, false)

    fun setIslandKeepOnLandscapeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_KEEP_ON_LANDSCAPE, enabled)

    fun isIslandKeepInFullscreenEnabled(): Boolean = getBoolean(KEY_ISLAND_KEEP_IN_FULLSCREEN, false)

    fun setIslandKeepInFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_KEEP_IN_FULLSCREEN, enabled)

    fun isIslandDisableInteractionsInFullscreenEnabled(): Boolean = getBoolean(KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN, false)

    fun setIslandDisableInteractionsInFullscreenEnabled(enabled: Boolean) =
        putBoolean(KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN, enabled)

    fun isIslandAlwaysGesturesEnabled(): Boolean = getBoolean(KEY_ISLAND_ALWAYS_GESTURES, false)

    fun setIslandAlwaysGesturesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_ALWAYS_GESTURES, enabled)

    fun getIslandMaxItems(): Int = getInt(KEY_ISLAND_MAX_ITEMS, 2).coerceIn(1, 4)

    fun setIslandMaxItems(value: Int) = putInt(KEY_ISLAND_MAX_ITEMS, value)

    fun isIslandBondEdgeEnabled(): Boolean = getBoolean(KEY_ISLAND_BOND_EDGE, false)

    fun setIslandBondEdgeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BOND_EDGE, enabled)

    fun isIslandBalanceCompactEnabled(): Boolean = getBoolean(KEY_ISLAND_BALANCE_COMPACT, true)

    fun setIslandBalanceCompactEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BALANCE_COMPACT, enabled)

    fun isIslandSecondaryBubbleEnabled(): Boolean = getBoolean(KEY_ISLAND_SECONDARY_BUBBLE, true)

    fun setIslandSecondaryBubbleEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SECONDARY_BUBBLE, enabled)

    fun getIslandLandscapeTopSpacing(): Float = getFloat(KEY_ISLAND_LANDSCAPE_TOP_SPACING, 0f)

    fun setIslandLandscapeTopSpacing(value: Float) = putFloat(KEY_ISLAND_LANDSCAPE_TOP_SPACING, value)

    fun isIslandHideOnShadeEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_ON_SHADE, false)

    fun setIslandHideOnShadeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_ON_SHADE, enabled)

    fun getIslandFontScale(): Float = getFloat(KEY_ISLAND_FONT_SCALE, 1f)

    fun setIslandFontScale(value: Float) = putFloat(KEY_ISLAND_FONT_SCALE, value)

    fun isIslandLineStageEnabled(): Boolean = getBoolean(KEY_ISLAND_LINE_STAGE_ENABLED, false)

    fun setIslandLineStageEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_LINE_STAGE_ENABLED, enabled)

    fun getIslandPeekDurationMs(): Long = getLong(KEY_ISLAND_PEEK_DURATION_MS, 3500L)

    fun setIslandPeekDurationMs(value: Long) = putLong(KEY_ISLAND_PEEK_DURATION_MS, value)

    fun isIslandMediaPeekSongChangeEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE, true)

    fun setIslandMediaPeekSongChangeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE, enabled)

    fun isIslandMediaKeepWhenPausedEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED, true)

    fun setIslandMediaKeepWhenPausedEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED, enabled)

    fun isIslandMediaShowPreviousEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_SHOW_PREVIOUS, false)

    fun setIslandMediaShowPreviousEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_SHOW_PREVIOUS, enabled)

    fun isIslandMediaShowLikeEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_SHOW_LIKE, true)

    fun setIslandMediaShowLikeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_SHOW_LIKE, enabled)

    fun isIslandShowNotificationsEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_NOTIFICATIONS, true)

    fun setIslandShowNotificationsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_NOTIFICATIONS, enabled)

    fun isIslandNotifHubIncludeSilent(): Boolean = getBoolean(KEY_ISLAND_NOTIF_HUB_SILENT, false)

    fun setIslandNotifHubIncludeSilent(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_HUB_SILENT, enabled)

    fun isIslandNotifHubGroupByApps(): Boolean = getBoolean(KEY_ISLAND_NOTIF_HUB_GROUP, false)

    fun setIslandNotifHubGroupByApps(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_HUB_GROUP, enabled)

    fun getIslandNotifHubOrder(): String =
        getString(KEY_ISLAND_NOTIF_HUB_ORDER, null)
            ?.takeIf { it == ISLAND_NOTIF_HUB_ORDER_PRIORITY || it == ISLAND_NOTIF_HUB_ORDER_RECENT }
            ?: ISLAND_NOTIF_HUB_ORDER_DYNAMIC

    fun setIslandNotifHubOrder(value: String) = putString(KEY_ISLAND_NOTIF_HUB_ORDER, value)

    // 0 = fit to the available width
    fun getIslandNotifHubIcons(): Int = getInt(KEY_ISLAND_NOTIF_HUB_ICONS, 0).coerceIn(0, 8)

    fun setIslandNotifHubIcons(value: Int) = putInt(KEY_ISLAND_NOTIF_HUB_ICONS, value.coerceIn(0, 8))

    fun isIslandNotifHubEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_HUB, false)

    fun setIslandNotifHubEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_HUB, enabled)

    fun isIslandNotifQueueEnabled(): Boolean = isIslandNotifHubEnabled() || getBoolean(KEY_ISLAND_NOTIF_QUEUE, true)

    fun setIslandNotifQueueEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_QUEUE, enabled)

    fun getIslandNotifQueueStyle(): String =
        if (getString(KEY_ISLAND_NOTIF_QUEUE_STYLE, null) == ISLAND_NOTIF_QUEUE_STYLE_OVERLAP) {
            ISLAND_NOTIF_QUEUE_STYLE_OVERLAP
        } else {
            ISLAND_NOTIF_QUEUE_STYLE_STACK
        }

    fun setIslandNotifQueueStyle(style: String) = putString(KEY_ISLAND_NOTIF_QUEUE_STYLE, style)

    fun isIslandNotifQueueInlineEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_QUEUE_INLINE, false)

    fun setIslandNotifQueueInlineEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_QUEUE_INLINE, enabled)

    fun isIslandNotifSkipSilentEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_SKIP_SILENT, true)

    fun setIslandNotifSkipSilentEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_SKIP_SILENT, enabled)

    fun isIslandNotifFollowDndEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_FOLLOW_DND, true)

    fun setIslandNotifFollowDndEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_FOLLOW_DND, enabled)

    fun isIslandNotifFilterAppsEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_FILTER_APPS, false)

    fun setIslandNotifFilterAppsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_FILTER_APPS, enabled)

    fun loadIslandNotifFilterApps() = loadAppSelection(KEY_ISLAND_NOTIF_FILTER_APPS_LIST)

    fun saveIslandNotifFilterApps(apps: List<AppSelection>) = saveAppSelection(KEY_ISLAND_NOTIF_FILTER_APPS_LIST, apps)

    fun updateIslandNotifFilterAppSelection(
        packageName: String,
        enabled: Boolean,
    ) = updateAppSelection(KEY_ISLAND_NOTIF_FILTER_APPS_LIST, packageName, enabled)

    fun isIslandNotifTapToOpenEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_TAP_TO_OPEN, false)

    fun setIslandNotifTapToOpenEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_TAP_TO_OPEN, enabled)

    fun isIslandOtpEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_ENABLED, false)

    fun setIslandOtpEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_OTP_ENABLED, enabled)

    fun isIslandOtpTapToCopyEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_TAP_TO_COPY, true)

    fun isIslandOtpDismissAfterCopyEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_DISMISS_AFTER_COPY, false)

    fun isIslandOtpBypassFilterEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_BYPASS_FILTER, true)

    fun getIslandOtpTimeoutMs(): Long = getLong(KEY_ISLAND_OTP_TIMEOUT_MS, 60_000L)

    fun setIslandOtpTimeoutMs(ms: Long) = putLong(KEY_ISLAND_OTP_TIMEOUT_MS, ms)

    fun getIslandNotifPeekStyle(): String =
        when (getString(KEY_ISLAND_NOTIF_PEEK_STYLE, null)) {
            ISLAND_NOTIF_PEEK_STYLE_COMPACT -> ISLAND_NOTIF_PEEK_STYLE_COMPACT
            null ->
                if (getBoolean(KEY_ISLAND_NOTIF_CONDENSED_INFO, false)) {
                    ISLAND_NOTIF_PEEK_STYLE_COMPACT
                } else {
                    ISLAND_NOTIF_PEEK_STYLE_FULL
                }
            else -> ISLAND_NOTIF_PEEK_STYLE_FULL
        }

    fun setIslandNotifPeekStyle(style: String) = putString(KEY_ISLAND_NOTIF_PEEK_STYLE, style)

    fun isIslandNotifKeepProgressEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_KEEP_PROGRESS, true)

    fun setIslandNotifKeepProgressEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_KEEP_PROGRESS, enabled)

    fun applyHeadsUpSuppression(suppress: Boolean = isIslandEnabled() && isIslandSuppressSystemHeadsUpEnabled()) {
        val targetValue = if (suppress) 0 else 1
        try {
            Settings.Global.putInt(
                context.contentResolver,
                "heads_up_notifications_enabled",
                targetValue,
            )
        } catch (_: SecurityException) {
            ShellUtils.runCommand(
                context,
                "settings put global heads_up_notifications_enabled $targetValue",
                featureName = context.getString(R.string.island_suppress_system_heads_up_title),
            )
        } catch (_: Exception) {
        }
    }

    fun getIslandCalendarPriorityMinutes(): Int = prefs.getInt(KEY_ISLAND_CALENDAR_PRIORITY_MINUTES, 10)

    fun setIslandCalendarPriorityMinutes(minutes: Int) = prefs.edit().putInt(KEY_ISLAND_CALENDAR_PRIORITY_MINUTES, minutes).apply()

    companion object {
        const val KEY_ISLAND_ENABLED = "island_enabled"
        const val KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED = "island_battery_idle_color_enabled"
        const val KEY_ISLAND_BATTERY_IDLE_COLOR = "island_battery_idle_color"
        const val KEY_ISLAND_USE_AUTO_DETECT = "island_use_auto_detect"
        const val KEY_ISLAND_CAMERA_OFFSET_X = "island_camera_offset_x"
        const val KEY_ISLAND_CAMERA_OFFSET_Y = "island_camera_offset_y"
        const val KEY_ISLAND_CAMERA_SIZE = "island_camera_size"
        const val KEY_ISLAND_MAX_WIDTH = "island_max_width"
        const val KEY_ISLAND_CUTOUT_GAP = "island_cutout_gap"
        const val KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP = "island_suppress_system_heads_up"
        const val KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR = "island_dynamic_hide_status_bar"
        const val KEY_ISLAND_COMPACT_HIDE_STATUS_BAR = "island_compact_hide_status_bar"
        const val KEY_ISLAND_HIDE_ICONS_ENABLED = "island_hide_icons_enabled"
        const val KEY_ISLAND_HIDE_WHEN_SCREEN_OFF = "island_hide_when_screen_off"
        const val KEY_ISLAND_SHOW_WHEN = "island_show_when"
        const val ISLAND_SHOW_WHEN_UNLOCKED = "unlocked"
        const val ISLAND_SHOW_WHEN_SCREEN_ON = "screen_on"
        const val ISLAND_SHOW_WHEN_ALWAYS = "always"
        const val KEY_ISLAND_NOTIF_CONCEAL_LOCKED = "island_notif_conceal_locked"
        const val KEY_ISLAND_NOTIF_DISABLE_LOCKED = "island_notif_disable_locked"
        const val KEY_ISLAND_NOTIF_CONCEAL_CHAT_PICTURES = "island_notif_conceal_chat_pictures"
        const val KEY_ISLAND_CALENDAR_HIDE_LOCKED = "island_calendar_hide_locked"
        const val KEY_ISLAND_TIME_HIDE_LOCKED = "island_time_hide_locked"
        const val KEY_ISLAND_BATTERY_HIDE_LOCKED = "island_battery_hide_locked"
        const val KEY_ISLAND_TIMEOUT_MS = "island_timeout_ms"
        const val KEY_ISLAND_SHOW_GLOW = "island_show_glow"
        const val KEY_ISLAND_EXPANDED_WIDTH = "island_expanded_width"
        const val KEY_ISLAND_EXPANDED_ROUNDNESS = "island_expanded_roundness"
        const val KEY_ISLAND_EXPANDED_PADDING = "island_expanded_padding"
        const val KEY_ISLAND_EXPANDED_TOP_PADDING = "island_expanded_top_padding"
        const val KEY_ISLAND_EXPANDED_BOTTOM_PADDING = "island_expanded_bottom_padding"
        const val KEY_ISLAND_EXPANDED_TIMEOUT_MS = "island_expanded_timeout_ms"
        const val KEY_ISLAND_CATCH_UP_ENABLED = "island_catch_up_enabled"
        const val KEY_ISLAND_CATCH_UP_TIMEOUT_MS = "island_catch_up_timeout_ms"
        const val KEY_ISLAND_SHOW_MEDIA = "island_show_media"
        const val KEY_ISLAND_MEDIA_EXCLUDED_APPS = "island_media_excluded_apps"
        const val KEY_ISLAND_SHOW_CALENDAR = "island_show_calendar"
        const val KEY_ISLAND_SHOW_CONSCIOUS_GATE = "island_show_conscious_gate"
        const val KEY_ISLAND_SHOW_TIME_BATTERY = "island_show_time_battery"
        const val KEY_ISLAND_TIME_SHOW_SECONDS = "island_time_show_seconds"
        const val KEY_ISLAND_THEMED_TEXT = "island_themed_text"
        const val KEY_ISLAND_ICON_COLOR_MODE = "island_icon_color_mode"
        const val KEY_ISLAND_ICON_COLOR_CUSTOM = "island_icon_color_custom"
        const val KEY_ISLAND_ICON_MATERIAL_YOU = "island_icon_material_you"
        const val ISLAND_ICON_COLOR_DYNAMIC = "dynamic"
        const val ISLAND_ICON_COLOR_CUSTOM = "custom"
        const val KEY_ISLAND_TIME_FORMAT = "island_time_format"
        const val KEY_ISLAND_TIME_PERIOD = "island_time_period"
        const val KEY_ISLAND_TIME_LEADING_ZERO = "island_time_leading_zero"
        const val ISLAND_TIME_FORMAT_SYSTEM = "system"
        const val ISLAND_TIME_FORMAT_12H = "12h"
        const val ISLAND_TIME_FORMAT_24H = "24h"
        const val ISLAND_TIME_PERIOD_NONE = "none"
        const val ISLAND_TIME_PERIOD_AM_PM = "am_pm"
        const val KEY_ISLAND_SHOW_BATTERY = "island_show_battery"
        const val KEY_ISLAND_SHOW_FLASHLIGHT = "island_show_flashlight"
        const val KEY_ISLAND_BATTERY_STYLE = "island_battery_style"
        const val KEY_ISLAND_DEVICES_BATTERY_STYLE = "island_devices_battery_style"
        const val ISLAND_BATTERY_STYLE_RING = "ring"
        const val ISLAND_BATTERY_STYLE_ICON = "icon"
        const val KEY_ISLAND_BATTERY_PERCENTAGE = "island_battery_percentage"
        const val KEY_ISLAND_LONG_PRESS_ACTION = "island_long_press_action"
        const val KEY_ISLAND_SLIDE_MODE = "island_slide_mode"
        const val KEY_ISLAND_SLIDE_TRACK = "island_slide_track"
        const val KEY_ISLAND_LIKE_WHILE_PLAYING = "island_like_while_playing"
        const val KEY_ISLAND_SLIDE_INVERT_DIRECTION = "island_slide_invert_direction"
        const val KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL = "island_battery_percentage_conditional"
        const val KEY_ISLAND_BATTERY_ICON_CONDITIONAL = "island_battery_icon_conditional"
        const val KEY_ISLAND_BATTERY_ONLY_LOW = "island_battery_only_low"
        const val KEY_ISLAND_BATTERY_PEEK_ALERTS = "island_battery_peek_alerts"
        const val KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW = "island_devices_battery_only_low"
        const val KEY_ISLAND_EXPANDED_SCALE = "island_expanded_scale"
        const val KEY_ISLAND_FONT_SCALE = "island_font_scale"
        const val KEY_ISLAND_HIDE_IN_OWNER_APP = "island_hide_in_owner_app"
        const val KEY_ISLAND_HIDE_ON_SHADE = "island_hide_on_shade"
        const val KEY_ISLAND_KEEP_ON_LANDSCAPE = "island_keep_on_landscape"
        const val KEY_ISLAND_KEEP_IN_FULLSCREEN = "island_keep_in_fullscreen"
        const val KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN = "island_disable_interactions_in_fullscreen"
        const val KEY_ISLAND_LANDSCAPE_TOP_SPACING = "island_landscape_top_spacing"
        const val KEY_ISLAND_BOND_EDGE = "island_bond_edge"
        const val KEY_ISLAND_BALANCE_COMPACT = "island_balance_compact"
        const val KEY_ISLAND_SECONDARY_BUBBLE = "island_secondary_bubble"
        const val KEY_ISLAND_MAX_ITEMS = "island_max_items"
        const val KEY_ISLAND_ALWAYS_GESTURES = "island_always_gestures"
        const val KEY_ISLAND_DISMISS_ON_OUTSIDE = "island_dismiss_on_outside"
        const val KEY_ISLAND_HIDE_LIVE_UPDATES = "island_hide_live_updates"
        const val KEY_ISLAND_HIDE_STATUS_BAR_INSETS = "island_hide_status_bar_insets"
        const val KEY_ISLAND_CAMERA_POSITION = "island_camera_position"
        const val KEY_ISLAND_ORIENTATION_PROFILES = "island_orientation_profiles"
        const val KEY_ISLAND_HIDE_PORTRAIT = "island_hide_portrait"
        const val KEY_ISLAND_HIDE_LANDSCAPE = "island_hide_landscape"
        const val KEY_ISLAND_PREVIEW_RING = "island_preview_ring"
        const val KEY_ISLAND_PREVIEW_STAGE = "island_preview_stage"
        const val ISLAND_PREVIEW_STAGE_AUTO = "auto"
        const val ISLAND_PREVIEW_STAGE_PEEK = "peek"
        const val ISLAND_PREVIEW_STAGE_EXPANDED = "expanded"
        const val KEY_ISLAND_SHOW_CALLS = "island_show_calls"
        const val KEY_ISLAND_CALL_SHOW_MODE = "island_call_show_mode"
        const val KEY_ISLAND_CALL_STICKY = "island_call_sticky"
        const val ISLAND_CALL_SHOW_EXPANDED = "expanded"
        const val ISLAND_CALL_SHOW_PEEK = "peek"
        const val ISLAND_CALL_SHOW_COMPACT = "compact"
        const val KEY_ISLAND_SHOW_TIMERS = "island_show_timers"
        const val KEY_ISLAND_PROVIDERS = "island_providers"
        const val KEY_ISLAND_PROVIDERS_HIDDEN = "island_providers_hidden"
        const val KEY_ISLAND_PROVIDERS_LAUNCHER_ONLY = "island_providers_launcher_only"
        const val KEY_ISLAND_PROVIDERS_HIDE_LOCKED = "island_providers_hide_locked"

        const val KEY_ISLAND_CALENDAR_EMOJIS = "island_calendar_emojis"
        const val KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER = "island_timers_show_screen_recorder"
        const val KEY_ISLAND_TIMERS_FILTER_APPS = "island_timers_filter_apps"
        const val KEY_ISLAND_TIMERS_SELECTED_APPS = "island_timers_selected_apps"
        const val KEY_ISLAND_SHOW_NETWORK = "island_show_network"
        const val KEY_ISLAND_SHOW_SIGNAL = "island_show_signal"
        const val KEY_ISLAND_NETWORK_ACTIVITY = "island_network_activity"
        const val KEY_ISLAND_SIGNAL_WIFI = "island_signal_wifi"
        const val KEY_ISLAND_PRIORITY_ORDER = "island_priority_order"
        const val KEY_ISLAND_SIGNAL_LOW_ONLY = "island_signal_low_only"
        const val KEY_ISLAND_SIGNAL_NETWORK_TYPES = "island_signal_network_modes"
        const val KEY_ISLAND_SIGNAL_SHOW_MODE = "island_signal_show_mode"
        const val KEY_ISLAND_SHOW_SOUND_MODE = "island_show_sound_mode"
        const val KEY_ISLAND_SOUND_MODE_KEEP_ICON = "island_sound_mode_keep_icon"
        const val KEY_ISLAND_SHOW_ALARM = "island_show_alarm"
        const val KEY_ISLAND_TIME_BATTERY_LAUNCHER_ONLY = "island_time_battery_launcher_only"
        const val KEY_ISLAND_CALENDAR_LAUNCHER_ONLY = "island_calendar_launcher_only"
        const val KEY_ISLAND_ALARM_LAUNCHER_ONLY = "island_alarm_launcher_only"
        const val KEY_ISLAND_ALARM_WINDOW_HOURS = "island_alarm_window_hours"
        const val KEY_ISLAND_ALARM_HIDE_TIME = "island_alarm_hide_time"
        const val KEY_ISLAND_SHOW_CAFFEINATE = "island_show_caffeinate"
        const val KEY_ISLAND_CAFFEINATE_SHOW_TIME = "island_caffeinate_show_time"
        const val KEY_ISLAND_SHOW_DEVICES = "island_show_devices"
        const val KEY_ISLAND_SHOW_NAVIGATION = "island_show_navigation"
        const val KEY_ISLAND_NAVIGATION_ON_NEW = "island_navigation_on_new"
        const val KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND = "island_navigation_peek_after_expand"
        const val ISLAND_NAVIGATION_ON_NEW_EXPAND = "expand"
        const val ISLAND_NAVIGATION_ON_NEW_PEEK = "peek"
        const val ISLAND_NAVIGATION_ON_NEW_OFF = "off"
        const val KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE = "island_navigation_expand_distance"
        const val KEY_ISLAND_SHOW_HOTSPOT = "island_show_hotspot"
        const val KEY_ISLAND_BRIEF_ENABLED = "island_brief_enabled"
        const val KEY_ISLAND_BRIEF_SHOW_ALARM = "island_brief_show_alarm"
        const val KEY_ISLAND_BRIEF_TWO_LINE_HEADER = "island_brief_two_line_header"
        const val KEY_ISLAND_DEVICES_BATTERY_ORDER = "island_devices_battery_order"
        const val ISLAND_CAMERA_POSITION_LEFT = "left"
        const val ISLAND_CAMERA_POSITION_CENTER = "center"
        const val ISLAND_CAMERA_POSITION_RIGHT = "right"
        const val KEY_ISLAND_LINE_STAGE_ENABLED = "island_line_stage_enabled"
        const val KEY_ISLAND_PEEK_DURATION_MS = "island_peek_duration_ms"
        const val KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE = "island_media_peek_song_change"
        const val KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED = "island_media_keep_when_paused"
        const val KEY_ISLAND_MEDIA_SHOW_PREVIOUS = "island_media_show_previous"
        const val KEY_ISLAND_MEDIA_SHOW_LIKE = "island_media_show_like"
        const val KEY_ISLAND_NOTIF_CONDENSED_INFO = "island_notif_condensed_info"
        const val KEY_ISLAND_NOTIF_PEEK_STYLE = "island_notif_peek_style"
        const val ISLAND_NOTIF_PEEK_STYLE_FULL = "full"
        const val ISLAND_NOTIF_PEEK_STYLE_COMPACT = "compact"
        const val ISLAND_NOTIF_PEEK_STYLE_MINIMAL = "minimal"
        const val KEY_ISLAND_NOTIF_KEEP_PROGRESS = "island_notif_keep_progress"
        const val KEY_ISLAND_NOTIF_QUEUE = "island_notif_queue"
        const val KEY_ISLAND_NOTIF_QUEUE_STYLE = "island_notif_queue_style"
        const val ISLAND_NOTIF_QUEUE_STYLE_STACK = "stack"
        const val ISLAND_NOTIF_QUEUE_STYLE_OVERLAP = "overlap"
        const val KEY_ISLAND_NOTIF_QUEUE_INLINE = "island_notif_queue_inline"
        const val KEY_ISLAND_NOTIF_SKIP_SILENT = "island_notif_skip_silent"
        const val KEY_ISLAND_NOTIF_FOLLOW_DND = "island_notif_follow_dnd"
        const val KEY_ISLAND_NOTIF_FILTER_APPS = "island_notif_filter_apps"
        const val KEY_ISLAND_NOTIF_FILTER_APPS_LIST = "island_notif_filter_apps_list"
        const val KEY_ISLAND_SHOW_NOTIFICATIONS = "island_show_notifications"
        const val KEY_ISLAND_NOTIF_HUB = "island_notif_hub"
        const val KEY_ISLAND_NOTIF_HUB_ICONS = "island_notif_hub_icons"
        const val KEY_ISLAND_NOTIF_HUB_ORDER = "island_notif_hub_order"
        const val KEY_ISLAND_NOTIF_HUB_SILENT = "island_notif_hub_silent"
        const val KEY_ISLAND_NOTIF_HUB_GROUP = "island_notif_hub_group"
        const val ISLAND_NOTIF_HUB_ORDER_DYNAMIC = "dynamic"
        const val ISLAND_NOTIF_HUB_ORDER_PRIORITY = "priority"
        const val ISLAND_NOTIF_HUB_ORDER_RECENT = "recent"
        const val KEY_ISLAND_NOTIF_TAP_TO_OPEN = "island_notif_tap_to_open"
        const val KEY_ISLAND_OTP_ENABLED = "island_otp_enabled"
        const val KEY_ISLAND_OTP_TAP_TO_COPY = "island_otp_tap_to_copy"
        const val KEY_ISLAND_OTP_DISMISS_AFTER_COPY = "island_otp_dismiss_after_copy"
        const val KEY_ISLAND_OTP_BYPASS_FILTER = "island_otp_bypass_filter"
        const val KEY_ISLAND_OTP_TIMEOUT_MS = "island_otp_timeout_ms"
        const val KEY_ISLAND_BORDER_OUTLINE_ENABLED = "island_border_outline_enabled"
        const val KEY_ISLAND_BORDER_OUTLINE_COLOR = "island_border_outline_color"
        const val KEY_ISLAND_BORDER_OUTLINE_DYNAMIC = "island_border_outline_dynamic"
        const val KEY_ISLAND_BORDER_OUTLINE_THICKNESS = "island_border_outline_thickness"
        const val KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED = "island_border_outline_hide_expanded"
        const val KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION = "island_pulse_shadow_on_notification"
        const val KEY_ISLAND_PULSE_SHADOW_SIZE = "island_pulse_shadow_size"
        const val KEY_ISLAND_PULSE_SHADOW_Y_SHIFT = "island_pulse_shadow_y_shift"
        const val KEY_ISLAND_PULSE_SHADOW_SPREAD = "island_pulse_shadow_spread"
        const val KEY_ISLAND_PULSE_SHADOW_DURATION_MS = "island_pulse_shadow_duration_ms"
        const val ISLAND_BORDER_OUTLINE_DEFAULT_COLOR = "#202020"

        const val KEY_ISLAND_CALENDAR_PRIORITY_MINUTES = "island_calendar_priority_minutes"
    }
}
