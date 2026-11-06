package com.jawzf.pixeltoolkit.feature.display.duo

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.duo.DuoBatteryLevels
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings

class DuoState(
    private val islandLink: DuoIslandLink,
    private val onFoldableChanged: (Boolean) -> Unit,
) {
    private lateinit var settings: DuoSettings

    val isDuoEnabled = mutableStateOf(false)
    val isDuoIslandCombined = mutableStateOf(false)
    val isDuoTapForBrief = mutableStateOf(false)
    val isDuoHideOnShade = mutableStateOf(false)
    val isDuoKeepInFullscreen = mutableStateOf(false)
    val isDuoDisableInteractionsInFullscreen = mutableStateOf(false)
    val isDuoAutoDetect = mutableStateOf(true)
    val isDuoOrientationProfiles = mutableStateOf(false)
    val isDuoHiddenInCurrentOrientation = mutableStateOf(false)
    val hasMultipleDuoDisplays = mutableStateOf(false)
    val duoCameraOffsetX = mutableFloatStateOf(50f)
    val duoCameraOffsetY = mutableFloatStateOf(3f)
    val duoCameraSize = mutableFloatStateOf(1.0f)
    val duoArcThickness = mutableFloatStateOf(4f)
    val duoDotSize = mutableFloatStateOf(4f)
    val duoRingRadius = mutableFloatStateOf(1.0f)
    val duoTimeTextScale = mutableFloatStateOf(1.0f)
    val isDuoShowBattery = mutableStateOf(true)
    val isDuoShowBatteryPercentage = mutableStateOf(false)
    val isDuoBatteryPercentageOnlyColored = mutableStateOf(false)
    val isDuoBatteryChargingColorEnabled = mutableStateOf(true)
    val duoBatteryChargingColor = mutableStateOf("auto")
    val isDuoBatteryPowerSaveColorEnabled = mutableStateOf(true)
    val duoBatteryPowerSaveColor = mutableStateOf("#FF9800")
    val isDuoBatteryLowColorEnabled = mutableStateOf(true)
    val duoBatteryLowColor = mutableStateOf("#FFEB3B")
    val duoBatteryLowLevel = mutableIntStateOf(DuoBatteryLevels.DEFAULT_LOW)
    val duoBatteryCriticalLevel = mutableIntStateOf(DuoBatteryLevels.DEFAULT_CRITICAL)
    val isDuoBatteryCriticalColorEnabled = mutableStateOf(true)
    val duoBatteryCriticalColor = mutableStateOf("#F44336")
    val isDuoShowNetworks = mutableStateOf(true)
    val isDuoDifferentiateWifi = mutableStateOf(true)
    val isDuoShowTime = mutableStateOf(false)
    val isDuoShowMedia = mutableStateOf(true)
    val isDuoRotateAlbumArt = mutableStateOf(false)
    val isDuoShowProgress = mutableStateOf(true)
    val isDuoShowFlashlight = mutableStateOf(true)
    val isDuoHideWhenScreenOff = mutableStateOf(true)
    val isDuoHideWhenScreenOffOnlyIdle = mutableStateOf(false)
    val isDuoHideWhenLocked = mutableStateOf(false)
    val isDuoUseMaterialYou = mutableStateOf(true)
    val duoCustomColor = mutableStateOf("#FFFFFF")
    val duoTapAction = mutableStateOf<Action?>(null)
    val duoDoubleTapAction = mutableStateOf<Action?>(null)
    val duoLongPressAction = mutableStateOf<Action?>(null)
    val duoSwipeDownAction = mutableStateOf<Action?>(null)
    val duoSlideMode = mutableStateOf("none")
    val isDuoSlideTrack = mutableStateOf(false)
    val isDuoSlideInvertDirection = mutableStateOf(false)

    fun load(context: Context) {
        settings = DuoSettings(context)
        isDuoEnabled.value = settings.isDuoEnabled()
        isDuoAutoDetect.value = settings.isDuoAutoDetectEnabled()
        duoCameraOffsetX.floatValue = settings.getDuoCameraOffsetX()
        duoCameraOffsetY.floatValue = settings.getDuoCameraOffsetY()
        duoCameraSize.floatValue = settings.getDuoCameraSize()
        duoArcThickness.floatValue = settings.getDuoArcThickness()
        duoDotSize.floatValue = settings.getDuoDotSize()
        duoRingRadius.floatValue = settings.getDuoRingRadius()
        duoTimeTextScale.floatValue = settings.getDuoTimeTextScale()
        isDuoShowBattery.value = settings.isDuoShowBatteryEnabled()
        isDuoShowBatteryPercentage.value = settings.isDuoShowBatteryPercentageEnabled()
        isDuoBatteryPercentageOnlyColored.value = settings.isDuoBatteryPercentageOnlyColoredEnabled()
        isDuoBatteryChargingColorEnabled.value = settings.isDuoBatteryChargingColorEnabled()
        duoBatteryChargingColor.value = settings.getDuoBatteryChargingColor()
        isDuoBatteryPowerSaveColorEnabled.value = settings.isDuoBatteryPowerSaveColorEnabled()
        duoBatteryPowerSaveColor.value = settings.getDuoBatteryPowerSaveColor()
        isDuoBatteryLowColorEnabled.value = settings.isDuoBatteryLowColorEnabled()
        duoBatteryLowColor.value = settings.getDuoBatteryLowColor()
        isDuoBatteryCriticalColorEnabled.value = settings.isDuoBatteryCriticalColorEnabled()
        duoBatteryCriticalColor.value = settings.getDuoBatteryCriticalColor()
        duoBatteryLowLevel.intValue = settings.getDuoBatteryLowLevel()
        duoBatteryCriticalLevel.intValue = settings.getDuoBatteryCriticalLevel()
        isDuoShowNetworks.value = settings.isDuoShowNetworksEnabled()
        isDuoDifferentiateWifi.value = settings.isDuoDifferentiateWifiEnabled()
        isDuoShowTime.value = settings.isDuoShowTimeEnabled()
        isDuoShowMedia.value = settings.isDuoShowMediaEnabled()
        isDuoRotateAlbumArt.value = settings.isDuoRotateAlbumArtEnabled()
        isDuoShowProgress.value = settings.isDuoShowProgressEnabled()
        isDuoShowFlashlight.value = settings.isDuoShowFlashlightEnabled()
        isDuoHideWhenScreenOff.value = settings.isDuoHideWhenScreenOffEnabled()
        isDuoHideWhenScreenOffOnlyIdle.value = settings.isDuoHideWhenScreenOffOnlyIdleEnabled()
        isDuoHideWhenLocked.value = settings.isDuoHideWhenLockedEnabled()
        isDuoUseMaterialYou.value = settings.isDuoUseMaterialYouEnabled()
        duoCustomColor.value = settings.getDuoCustomColor()
        duoTapAction.value = settings.getDuoTapAction()
        duoDoubleTapAction.value = settings.getDuoDoubleTapAction()
        duoLongPressAction.value = settings.getDuoLongPressAction()
        duoSwipeDownAction.value = settings.getDuoSwipeDownAction()
        duoSlideMode.value = settings.getDuoSlideMode()
        isDuoSlideTrack.value = settings.isDuoSlideTrackEnabled()
        isDuoSlideInvertDirection.value = settings.isDuoSlideInvertDirectionEnabled()
        isDuoIslandCombined.value = settings.isDuoIslandCombinedSetting()
        isDuoTapForBrief.value = settings.isDuoTapForBriefEnabled()
        isDuoHideOnShade.value = settings.isDuoHideOnShadeEnabled()
        isDuoKeepInFullscreen.value = settings.isDuoKeepInFullscreenEnabled()
        isDuoDisableInteractionsInFullscreen.value = settings.isDuoDisableInteractionsInFullscreenEnabled()
    }

    fun setDuoEnabled(enabled: Boolean) {
        isDuoEnabled.value = enabled
        settings.setDuoEnabled(enabled)
    }

    fun setDuoAutoDetect(enabled: Boolean) {
        isDuoAutoDetect.value = enabled
        settings.setDuoAutoDetectEnabled(enabled)
    }

    fun setDuoCameraOffsetX(value: Float) {
        duoCameraOffsetX.floatValue = value
        settings.setDuoCameraOffsetX(value)
    }

    fun setDuoCameraOffsetY(value: Float) {
        duoCameraOffsetY.floatValue = value
        settings.setDuoCameraOffsetY(value)
    }

    fun setDuoCameraSize(value: Float) {
        duoCameraSize.floatValue = value
        settings.setDuoCameraSize(value)
    }

    fun refreshDuoCameraPlacement() {
        settings.markDisplayProfileSeen()
        onFoldableChanged(settings.isFoldableDevice())
        isDuoOrientationProfiles.value = settings.isDuoOrientationProfilesEnabled()
        isDuoHiddenInCurrentOrientation.value = settings.isDuoHiddenInCurrentOrientation()
        duoCameraOffsetX.floatValue = settings.getDuoCameraOffsetX()
        duoCameraOffsetY.floatValue = settings.getDuoCameraOffsetY()
        duoCameraSize.floatValue = settings.getDuoCameraSize()
        hasMultipleDuoDisplays.value = settings.isFoldableDevice()
    }

    fun setDuoOrientationProfiles(enabled: Boolean) {
        settings.setDuoOrientationProfilesEnabled(enabled)
        refreshDuoCameraPlacement()
    }

    fun setDuoHiddenInCurrentOrientation(hidden: Boolean) {
        settings.setDuoHiddenInCurrentOrientation(hidden)
        isDuoHiddenInCurrentOrientation.value = hidden
    }

    fun setDuoArcThickness(value: Float) {
        duoArcThickness.floatValue = value
        settings.setDuoArcThickness(value)
    }

    fun setDuoDotSize(value: Float) {
        duoDotSize.floatValue = value
        settings.setDuoDotSize(value)
    }

    fun setDuoRingRadius(value: Float) {
        duoRingRadius.floatValue = value
        settings.setDuoRingRadius(value)
    }

    fun setDuoTimeTextScale(value: Float) {
        duoTimeTextScale.floatValue = value
        settings.setDuoTimeTextScale(value)
    }

    fun setDuoShowBattery(enabled: Boolean) {
        isDuoShowBattery.value = enabled
        settings.setDuoShowBatteryEnabled(enabled)
        if (!enabled) {
            isDuoShowBatteryPercentage.value = false
            settings.setDuoShowBatteryPercentageEnabled(false)
            isDuoShowNetworks.value = false
            settings.setDuoShowNetworksEnabled(false)
            isDuoShowTime.value = false
            settings.setDuoShowTimeEnabled(false)
        }
    }

    fun setDuoShowBatteryPercentage(enabled: Boolean) {
        isDuoShowBatteryPercentage.value = enabled
        settings.setDuoShowBatteryPercentageEnabled(enabled)
    }

    fun setDuoBatteryPercentageOnlyColored(enabled: Boolean) {
        isDuoBatteryPercentageOnlyColored.value = enabled
        settings.setDuoBatteryPercentageOnlyColoredEnabled(enabled)
    }

    fun setDuoBatteryChargingColorEnabled(enabled: Boolean) {
        isDuoBatteryChargingColorEnabled.value = enabled
        settings.setDuoBatteryChargingColorEnabled(enabled)
    }

    fun setDuoBatteryChargingColor(colorHex: String) {
        duoBatteryChargingColor.value = colorHex
        settings.setDuoBatteryChargingColor(colorHex)
    }

    fun setDuoBatteryPowerSaveColorEnabled(enabled: Boolean) {
        isDuoBatteryPowerSaveColorEnabled.value = enabled
        settings.setDuoBatteryPowerSaveColorEnabled(enabled)
    }

    fun setDuoBatteryPowerSaveColor(colorHex: String) {
        duoBatteryPowerSaveColor.value = colorHex
        settings.setDuoBatteryPowerSaveColor(colorHex)
    }

    fun setDuoBatteryLowColorEnabled(enabled: Boolean) {
        isDuoBatteryLowColorEnabled.value = enabled
        settings.setDuoBatteryLowColorEnabled(enabled)
    }

    fun setDuoBatteryLowColor(colorHex: String) {
        duoBatteryLowColor.value = colorHex
        settings.setDuoBatteryLowColor(colorHex)
    }

    fun setDuoBatteryCriticalColorEnabled(enabled: Boolean) {
        isDuoBatteryCriticalColorEnabled.value = enabled
        settings.setDuoBatteryCriticalColorEnabled(enabled)
    }

    fun setDuoBatteryCriticalColor(colorHex: String) {
        duoBatteryCriticalColor.value = colorHex
        settings.setDuoBatteryCriticalColor(colorHex)
    }

    fun setDuoBatteryLowLevel(level: Int) {
        val (low, critical) = DuoBatteryLevels.withLow(level, duoBatteryCriticalLevel.intValue)
        applyDuoBatteryLevels(low, critical)
    }

    fun setDuoBatteryCriticalLevel(level: Int) {
        val (low, critical) = DuoBatteryLevels.withCritical(level, duoBatteryLowLevel.intValue)
        applyDuoBatteryLevels(low, critical)
    }

    private fun applyDuoBatteryLevels(
        low: Int,
        critical: Int,
    ) {
        duoBatteryLowLevel.intValue = low
        duoBatteryCriticalLevel.intValue = critical
        settings.setDuoBatteryLevels(low, critical)
    }

    fun setDuoShowNetworks(enabled: Boolean) {
        isDuoShowNetworks.value = enabled
        settings.setDuoShowNetworksEnabled(enabled)
        if (enabled) {
            isDuoShowTime.value = false
            settings.setDuoShowTimeEnabled(false)
        }
    }

    fun setDuoDifferentiateWifi(enabled: Boolean) {
        isDuoDifferentiateWifi.value = enabled
        settings.setDuoDifferentiateWifiEnabled(enabled)
    }

    fun setDuoShowTime(enabled: Boolean) {
        isDuoShowTime.value = enabled
        settings.setDuoShowTimeEnabled(enabled)
        if (enabled && settings.isDuoIslandCombined()) {
            islandLink.onDuoShowsTime()
        }
        if (enabled) {
            isDuoShowNetworks.value = false
            settings.setDuoShowNetworksEnabled(false)
        }
    }

    fun setDuoShowMedia(enabled: Boolean) {
        isDuoShowMedia.value = enabled
        settings.setDuoShowMediaEnabled(enabled)
        if (enabled && settings.isDuoIslandCombined()) {
            islandLink.onDuoShowsMedia()
        }
    }

    fun setDuoRotateAlbumArt(enabled: Boolean) {
        isDuoRotateAlbumArt.value = enabled
        settings.setDuoRotateAlbumArtEnabled(enabled)
    }

    fun setDuoShowProgress(enabled: Boolean) {
        isDuoShowProgress.value = enabled
        settings.setDuoShowProgressEnabled(enabled)
        if (enabled && settings.isDuoIslandCombined()) {
            islandLink.onDuoShowsProgress()
        }
    }

    fun setDuoShowFlashlight(enabled: Boolean) {
        isDuoShowFlashlight.value = enabled
        settings.setDuoShowFlashlightEnabled(enabled)
        if (enabled && settings.isDuoIslandCombined()) {
            islandLink.onDuoShowsFlashlight()
        }
    }

    fun setDuoKeepInFullscreen(enabled: Boolean) {
        isDuoKeepInFullscreen.value = enabled
        settings.setDuoKeepInFullscreenEnabled(enabled)
    }

    fun setDuoDisableInteractionsInFullscreen(enabled: Boolean) {
        isDuoDisableInteractionsInFullscreen.value = enabled
        settings.setDuoDisableInteractionsInFullscreenEnabled(enabled)
    }

    fun setDuoHideOnShade(enabled: Boolean) {
        isDuoHideOnShade.value = enabled
        settings.setDuoHideOnShadeEnabled(enabled)
    }

    fun setDuoTapForBrief(enabled: Boolean) {
        isDuoTapForBrief.value = enabled
        settings.setDuoTapForBriefEnabled(enabled)
    }

    fun setDuoIslandCombined(enabled: Boolean) {
        isDuoIslandCombined.value = enabled
        settings.setDuoIslandCombined(enabled)
        if (!enabled) return
        setDuoShowBattery(true)
        setDuoShowNetworks(true)
        islandLink.onCombinedChanged(enabled)
        isDuoShowMedia.value = false
        settings.setDuoShowMediaEnabled(false)
        isDuoShowFlashlight.value = false
        settings.setDuoShowFlashlightEnabled(false)
        isDuoShowProgress.value = false
        settings.setDuoShowProgressEnabled(false)
    }

    fun setDuoHideWhenScreenOff(enabled: Boolean) {
        isDuoHideWhenScreenOff.value = enabled
        settings.setDuoHideWhenScreenOffEnabled(enabled)
    }

    fun setDuoHideWhenScreenOffOnlyIdle(enabled: Boolean) {
        isDuoHideWhenScreenOffOnlyIdle.value = enabled
        settings.setDuoHideWhenScreenOffOnlyIdleEnabled(enabled)
    }

    fun setDuoHideWhenLocked(enabled: Boolean) {
        isDuoHideWhenLocked.value = enabled
        settings.setDuoHideWhenLockedEnabled(enabled)
    }

    fun setDuoUseMaterialYou(enabled: Boolean) {
        isDuoUseMaterialYou.value = enabled
        settings.setDuoUseMaterialYouEnabled(enabled)
    }

    fun setDuoCustomColor(colorHex: String) {
        duoCustomColor.value = colorHex
        settings.setDuoCustomColor(colorHex)
    }

    fun setDuoTapAction(action: Action?) {
        duoTapAction.value = action
        settings.setDuoTapAction(action)
    }

    fun setDuoDoubleTapAction(action: Action?) {
        duoDoubleTapAction.value = action
        settings.setDuoDoubleTapAction(action)
    }

    fun setDuoLongPressAction(action: Action?) {
        duoLongPressAction.value = action
        settings.setDuoLongPressAction(action)
    }

    fun setDuoSwipeDownAction(action: Action?) {
        duoSwipeDownAction.value = action
        settings.setDuoSwipeDownAction(action)
    }

    fun setDuoSlideMode(mode: String) {
        duoSlideMode.value = mode
        settings.setDuoSlideMode(mode)
    }

    fun setDuoSlideTrackEnabled(enabled: Boolean) {
        isDuoSlideTrack.value = enabled
        settings.setDuoSlideTrackEnabled(enabled)
    }

    fun setDuoSlideInvertDirection(enabled: Boolean) {
        isDuoSlideInvertDirection.value = enabled
        settings.setDuoSlideInvertDirection(enabled)
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            DuoSettings.KEY_DUO_ENABLED ->
                isDuoEnabled.value = settings.isDuoEnabled()

            DuoSettings.KEY_DUO_USE_AUTO_DETECT ->
                isDuoAutoDetect.value = settings.isDuoAutoDetectEnabled()

            DuoSettings.KEY_DUO_CAMERA_OFFSET_X ->
                duoCameraOffsetX.floatValue = settings.getDuoCameraOffsetX()

            DuoSettings.KEY_DUO_CAMERA_OFFSET_Y ->
                duoCameraOffsetY.floatValue = settings.getDuoCameraOffsetY()

            DuoSettings.KEY_DUO_CAMERA_SIZE ->
                duoCameraSize.floatValue = settings.getDuoCameraSize()

            DuoSettings.KEY_DUO_ARC_THICKNESS ->
                duoArcThickness.floatValue = settings.getDuoArcThickness()

            DuoSettings.KEY_DUO_DOT_SIZE ->
                duoDotSize.floatValue = settings.getDuoDotSize()

            DuoSettings.KEY_DUO_RING_RADIUS ->
                duoRingRadius.floatValue = settings.getDuoRingRadius()

            DuoSettings.KEY_DUO_TIME_TEXT_SCALE ->
                duoTimeTextScale.floatValue = settings.getDuoTimeTextScale()

            else -> onBatteryPreferenceChanged(key)
        }
    }

    private fun onBatteryPreferenceChanged(key: String) {
        when (key) {
            DuoSettings.KEY_DUO_SHOW_BATTERY ->
                isDuoShowBattery.value = settings.isDuoShowBatteryEnabled()

            DuoSettings.KEY_DUO_SHOW_BATTERY_PERCENTAGE ->
                isDuoShowBatteryPercentage.value = settings.isDuoShowBatteryPercentageEnabled()

            DuoSettings.KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED ->
                isDuoBatteryPercentageOnlyColored.value =
                    settings.isDuoBatteryPercentageOnlyColoredEnabled()

            DuoSettings.KEY_DUO_BATTERY_LOW_LEVEL ->
                duoBatteryLowLevel.intValue = settings.getDuoBatteryLowLevel()

            DuoSettings.KEY_DUO_BATTERY_CRITICAL_LEVEL ->
                duoBatteryCriticalLevel.intValue = settings.getDuoBatteryCriticalLevel()

            else -> onBatteryColorPreferenceChanged(key)
        }
    }

    private fun onBatteryColorPreferenceChanged(key: String) {
        when (key) {
            DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED ->
                isDuoBatteryChargingColorEnabled.value =
                    settings.isDuoBatteryChargingColorEnabled()

            DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR ->
                duoBatteryChargingColor.value = settings.getDuoBatteryChargingColor()

            DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED ->
                isDuoBatteryPowerSaveColorEnabled.value =
                    settings.isDuoBatteryPowerSaveColorEnabled()

            DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR ->
                duoBatteryPowerSaveColor.value = settings.getDuoBatteryPowerSaveColor()

            DuoSettings.KEY_DUO_BATTERY_LOW_COLOR_ENABLED ->
                isDuoBatteryLowColorEnabled.value = settings.isDuoBatteryLowColorEnabled()

            DuoSettings.KEY_DUO_BATTERY_LOW_COLOR ->
                duoBatteryLowColor.value = settings.getDuoBatteryLowColor()

            DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED ->
                isDuoBatteryCriticalColorEnabled.value =
                    settings.isDuoBatteryCriticalColorEnabled()

            DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR ->
                duoBatteryCriticalColor.value = settings.getDuoBatteryCriticalColor()

            else -> onDisplayPreferenceChanged(key)
        }
    }

    private fun onDisplayPreferenceChanged(key: String) {
        when (key) {
            DuoSettings.KEY_DUO_SHOW_NETWORKS ->
                isDuoShowNetworks.value = settings.isDuoShowNetworksEnabled()

            DuoSettings.KEY_DUO_DIFFERENTIATE_WIFI ->
                isDuoDifferentiateWifi.value = settings.isDuoDifferentiateWifiEnabled()

            DuoSettings.KEY_DUO_SHOW_TIME ->
                isDuoShowTime.value = settings.isDuoShowTimeEnabled()

            DuoSettings.KEY_DUO_SHOW_MEDIA ->
                isDuoShowMedia.value = settings.isDuoShowMediaEnabled()

            DuoSettings.KEY_DUO_ROTATE_ALBUM_ART ->
                isDuoRotateAlbumArt.value = settings.isDuoRotateAlbumArtEnabled()

            DuoSettings.KEY_DUO_SHOW_PROGRESS ->
                isDuoShowProgress.value = settings.isDuoShowProgressEnabled()

            DuoSettings.KEY_DUO_SHOW_FLASHLIGHT ->
                isDuoShowFlashlight.value = settings.isDuoShowFlashlightEnabled()

            DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF ->
                isDuoHideWhenScreenOff.value = settings.isDuoHideWhenScreenOffEnabled()

            DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE ->
                isDuoHideWhenScreenOffOnlyIdle.value =
                    settings.isDuoHideWhenScreenOffOnlyIdleEnabled()

            else -> onActionPreferenceChanged(key)
        }
    }

    private fun onActionPreferenceChanged(key: String) {
        when (key) {
            DuoSettings.KEY_DUO_USE_MATERIAL_YOU ->
                isDuoUseMaterialYou.value = settings.isDuoUseMaterialYouEnabled()

            DuoSettings.KEY_DUO_CUSTOM_COLOR ->
                duoCustomColor.value = settings.getDuoCustomColor()

            DuoSettings.KEY_DUO_TAP_ACTION ->
                duoTapAction.value = settings.getDuoTapAction()

            DuoSettings.KEY_DUO_DOUBLE_TAP_ACTION ->
                duoDoubleTapAction.value = settings.getDuoDoubleTapAction()

            DuoSettings.KEY_DUO_LONG_PRESS_ACTION ->
                duoLongPressAction.value = settings.getDuoLongPressAction()

            DuoSettings.KEY_DUO_SWIPE_DOWN_ACTION ->
                duoSwipeDownAction.value = settings.getDuoSwipeDownAction()

            DuoSettings.KEY_DUO_SLIDE_MODE ->
                duoSlideMode.value = settings.getDuoSlideMode()

            DuoSettings.KEY_DUO_SLIDE_TRACK ->
                isDuoSlideTrack.value = settings.isDuoSlideTrackEnabled()

            DuoSettings.KEY_DUO_SLIDE_INVERT_DIRECTION ->
                isDuoSlideInvertDirection.value = settings.isDuoSlideInvertDirectionEnabled()
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                DuoSettings.KEY_DUO_ENABLED,
                DuoSettings.KEY_DUO_USE_AUTO_DETECT,
                DuoSettings.KEY_DUO_CAMERA_OFFSET_X,
                DuoSettings.KEY_DUO_CAMERA_OFFSET_Y,
                DuoSettings.KEY_DUO_CAMERA_SIZE,
                DuoSettings.KEY_DUO_ARC_THICKNESS,
                DuoSettings.KEY_DUO_DOT_SIZE,
                DuoSettings.KEY_DUO_RING_RADIUS,
                DuoSettings.KEY_DUO_TIME_TEXT_SCALE,
                DuoSettings.KEY_DUO_SHOW_BATTERY,
                DuoSettings.KEY_DUO_SHOW_BATTERY_PERCENTAGE,
                DuoSettings.KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED,
                DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR,
                DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR,
                DuoSettings.KEY_DUO_BATTERY_LOW_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_LOW_COLOR,
                DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED,
                DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR,
                DuoSettings.KEY_DUO_BATTERY_LOW_LEVEL,
                DuoSettings.KEY_DUO_BATTERY_CRITICAL_LEVEL,
                DuoSettings.KEY_DUO_SHOW_NETWORKS,
                DuoSettings.KEY_DUO_DIFFERENTIATE_WIFI,
                DuoSettings.KEY_DUO_SHOW_TIME,
                DuoSettings.KEY_DUO_SHOW_MEDIA,
                DuoSettings.KEY_DUO_ROTATE_ALBUM_ART,
                DuoSettings.KEY_DUO_SHOW_PROGRESS,
                DuoSettings.KEY_DUO_SHOW_FLASHLIGHT,
                DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF,
                DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE,
                DuoSettings.KEY_DUO_USE_MATERIAL_YOU,
                DuoSettings.KEY_DUO_CUSTOM_COLOR,
                DuoSettings.KEY_DUO_TAP_ACTION,
                DuoSettings.KEY_DUO_DOUBLE_TAP_ACTION,
                DuoSettings.KEY_DUO_LONG_PRESS_ACTION,
                DuoSettings.KEY_DUO_SWIPE_DOWN_ACTION,
                DuoSettings.KEY_DUO_SLIDE_MODE,
                DuoSettings.KEY_DUO_SLIDE_TRACK,
                DuoSettings.KEY_DUO_SLIDE_INVERT_DIRECTION,
            )
    }
}
