package com.jawzf.pixeltoolkit.feature.island

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarInsetsUtils
import com.jawzf.pixeltoolkit.feature.island.service.IslandStatusBarHider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IslandState(
    private val scope: CoroutineScope,
    private val duoLink: IslandDuoLink,
    private val onFoldableChanged: (Boolean) -> Unit,
) {
    private lateinit var settings: IslandSettings
    private lateinit var duoSettings: DuoSettings

    val isIslandBatteryIdleColorEnabled = mutableStateOf(false)
    val islandBatteryIdleColor = mutableStateOf("#FFFFFF")

    val isIslandEnabled = mutableStateOf(false)
    val isIslandAutoDetect = mutableStateOf(true)
    val isIslandOrientationProfiles = mutableStateOf(false)
    val isIslandHiddenInCurrentOrientation = mutableStateOf(false)
    val islandCameraOffsetX = mutableFloatStateOf(50f)
    val islandCameraOffsetY = mutableFloatStateOf(3f)
    val islandCameraSize = mutableFloatStateOf(1.0f)
    val islandMaxWidth = mutableFloatStateOf(360f)
    val islandCutoutGap = mutableFloatStateOf(6f)
    val islandExpandedWidth = mutableFloatStateOf(360f)
    val islandExpandedRoundness = mutableFloatStateOf(24f)
    val islandFontScale = mutableFloatStateOf(1f)
    val isIslandHideInOwnerApp = mutableStateOf(false)
    val isIslandHideOnShade = mutableStateOf(false)
    val isIslandKeepOnLandscape = mutableStateOf(false)
    val isIslandKeepInFullscreen = mutableStateOf(false)
    val isIslandDisableInteractionsInFullscreen = mutableStateOf(false)
    val isIslandBondEdge = mutableStateOf(false)
    val isIslandBalanceCompact = mutableStateOf(true)
    val isIslandSecondaryBubble = mutableStateOf(true)
    val islandMaxItems = mutableStateOf(2)
    val isIslandAlwaysGestures = mutableStateOf(false)
    val islandLandscapeTopSpacing = mutableFloatStateOf(0f)
    val isIslandDismissOnOutside = mutableStateOf(false)
    val isIslandHideLiveUpdates = mutableStateOf(false)
    val isIslandHideStatusBarInsets = mutableStateOf(false)
    val islandExpandedScale = mutableFloatStateOf(1f)
    val islandCameraPosition = mutableStateOf(IslandSettings.ISLAND_CAMERA_POSITION_CENTER)
    val isIslandShowCalls = mutableStateOf(true)
    val isIslandShowTimers = mutableStateOf(true)
    val isIslandTimersShowScreenRecorder = mutableStateOf(true)
    val isIslandTimersFilterApps = mutableStateOf(false)
    val isIslandShowNetwork = mutableStateOf(true)
    val isIslandShowSignal = mutableStateOf(false)
    val isIslandShowSoundMode = mutableStateOf(true)
    val isIslandSoundModeKeepIcon = mutableStateOf(true)
    val isIslandShowAlarm = mutableStateOf(false)
    val islandAlarmWindowHours = mutableIntStateOf(12)
    val isIslandShowCaffeinate = mutableStateOf(true)
    val isIslandShowNavigation = mutableStateOf(true)
    val islandNavigationOnNew = mutableStateOf(IslandSettings.ISLAND_NAVIGATION_ON_NEW_PEEK)
    val islandNavigationExpandDistance = mutableIntStateOf(200)
    val isIslandNavigationPeekAfterExpand = mutableStateOf(false)
    val isIslandShowHotspot = mutableStateOf(false)
    val isIslandShowDevices = mutableStateOf(true)
    val isIslandBriefEnabled = mutableStateOf(false)
    val isIslandBriefShowAlarm = mutableStateOf(true)
    val islandDevicesBatteryOrder = mutableStateOf<List<String>>(emptyList())
    val islandExpandedPadding = mutableFloatStateOf(16f)
    val islandExpandedTopPadding = mutableFloatStateOf(0f)
    val islandExpandedBottomPadding = mutableFloatStateOf(12f)
    val islandExpandedTimeoutMs = mutableLongStateOf(0L)
    val isIslandSuppressSystemHeadsUp = mutableStateOf(false)
    val isIslandDynamicHideStatusBar = mutableStateOf(false)
    val isIslandCompactHideStatusBar = mutableStateOf(false)
    val isIslandHideWhenScreenOff = mutableStateOf(true)
    val islandTimeoutMs = mutableLongStateOf(4500L)
    val isIslandLineStageEnabled = mutableStateOf(false)
    val isIslandMediaPeekSongChange = mutableStateOf(true)
    val isIslandMediaKeepWhenPaused = mutableStateOf(true)
    val isIslandMediaShowPrevious = mutableStateOf(false)
    val isIslandMediaShowLike = mutableStateOf(true)
    val islandNotifPeekStyle = mutableStateOf(IslandSettings.ISLAND_NOTIF_PEEK_STYLE_FULL)
    val isIslandNotifKeepProgress = mutableStateOf(true)
    val isIslandNotifQueue = mutableStateOf(true)
    val islandNotifQueueStyle = mutableStateOf(IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_STACK)
    val isIslandNotifQueueInline = mutableStateOf(false)
    val isIslandNotifSkipSilent = mutableStateOf(true)
    val isIslandNotifFollowDnd = mutableStateOf(true)
    val isIslandNotifFilterApps = mutableStateOf(false)
    val isIslandShowNotifications = mutableStateOf(true)
    val isIslandNotifHub = mutableStateOf(false)
    val islandNotifHubIcons = mutableIntStateOf(0)
    val isIslandNotifHubIncludeSilent = mutableStateOf(false)
    val isIslandNotifHubGroupByApps = mutableStateOf(false)
    val islandNotifHubOrder = mutableStateOf(IslandSettings.ISLAND_NOTIF_HUB_ORDER_DYNAMIC)
    val isIslandNotifTapToOpen = mutableStateOf(false)
    val isIslandCatchUpEnabled = mutableStateOf(true)
    val islandCatchUpTimeoutMs = mutableLongStateOf(10000L)
    val isIslandOtpEnabled = mutableStateOf(false)
    val islandOtpTimeoutMs = mutableLongStateOf(60_000L)
    val isIslandShowGlow = mutableStateOf(true)
    val isIslandBorderOutline = mutableStateOf(false)
    val islandBorderOutlineColor = mutableStateOf(IslandSettings.ISLAND_BORDER_OUTLINE_DEFAULT_COLOR)
    val isIslandBorderOutlineDynamic = mutableStateOf(false)
    val islandBorderOutlineThickness = mutableFloatStateOf(1f)
    val isIslandBorderOutlineHiddenWhenExpanded = mutableStateOf(false)
    val isIslandPulseShadow = mutableStateOf(false)
    val islandPulseShadowSize = mutableFloatStateOf(0.5f)
    val islandPulseShadowYShift = mutableFloatStateOf(0.35f)
    val islandPulseShadowSpread = mutableFloatStateOf(2f)
    val islandPulseShadowDurationMs = mutableFloatStateOf(1450f)
    val isIslandShowMedia = mutableStateOf(true)
    val isIslandShowCalendar = mutableStateOf(false)
    val isIslandShowConsciousGate = mutableStateOf(true)
    val isIslandShowTimeBattery = mutableStateOf(false)
    val isIslandBatteryPeekAlerts = mutableStateOf(true)
    val isIslandShowBattery = mutableStateOf(false)
    val isIslandShowFlashlight = mutableStateOf(true)
    val islandBatteryStyle = mutableStateOf(IslandSettings.ISLAND_BATTERY_STYLE_RING)
    val islandDevicesBatteryStyle = mutableStateOf(IslandSettings.ISLAND_BATTERY_STYLE_RING)
    val isIslandBatteryPercentageEnabled = mutableStateOf(false)
    val isIslandBatteryPercentageConditional = mutableStateOf(false)
    val isIslandBatteryIconConditional = mutableStateOf(false)
    val isIslandBatteryOnlyLow = mutableStateOf(false)
    val isIslandDevicesBatteryOnlyLow = mutableStateOf(false)
    val islandLongPressAction = mutableStateOf<Action?>(null)
    val islandSlideMode = mutableStateOf("none")
    val isIslandSlideTrack = mutableStateOf(false)
    val isIslandLikeWhilePlaying = mutableStateOf(false)
    val isIslandSlideInvertDirection = mutableStateOf(true)

    val islandCalendarEmojis = mutableStateOf<Map<Long, String>>(emptyMap())

    fun load(context: Context) {
        settings = IslandSettings(context)
        duoSettings = DuoSettings(context)
        loadLayout()
        loadPlugins()
        loadNotifications()
        loadFeatures()
    }

    private fun loadLayout() {
        isIslandBatteryIdleColorEnabled.value = settings.isIslandBatteryIdleColorEnabled()
        islandBatteryIdleColor.value = settings.getIslandBatteryIdleColor()
        isIslandEnabled.value = settings.isIslandEnabled()
        isIslandAutoDetect.value = settings.isIslandAutoDetectEnabled()
        islandCameraOffsetX.floatValue = settings.getIslandCameraOffsetX()
        islandCameraOffsetY.floatValue = settings.getIslandCameraOffsetY()
        islandCameraSize.floatValue = settings.getIslandCameraSize()
        islandMaxWidth.floatValue = settings.getIslandMaxWidth()
        islandCutoutGap.floatValue = settings.getIslandCutoutGap()
        islandExpandedWidth.floatValue = settings.getIslandExpandedWidth()
        islandExpandedRoundness.floatValue = settings.getIslandExpandedRoundness()
        islandFontScale.floatValue = settings.getIslandFontScale()
        isIslandHideInOwnerApp.value = settings.isIslandHideInOwnerAppEnabled()
        isIslandHideOnShade.value = settings.isIslandHideOnShadeEnabled()
        isIslandKeepOnLandscape.value = settings.isIslandKeepOnLandscapeEnabled()
        isIslandKeepInFullscreen.value = settings.isIslandKeepInFullscreenEnabled()
        isIslandDisableInteractionsInFullscreen.value = settings.isIslandDisableInteractionsInFullscreenEnabled()
        isIslandBondEdge.value = settings.isIslandBondEdgeEnabled()
        isIslandBalanceCompact.value = settings.isIslandBalanceCompactEnabled()
        isIslandSecondaryBubble.value = settings.isIslandSecondaryBubbleEnabled()
        islandMaxItems.value = settings.getIslandMaxItems()
        isIslandAlwaysGestures.value = settings.isIslandAlwaysGesturesEnabled()
        islandLandscapeTopSpacing.floatValue = settings.getIslandLandscapeTopSpacing()
        isIslandDismissOnOutside.value = settings.isIslandDismissOnOutsideEnabled()
        isIslandHideLiveUpdates.value = settings.isIslandHideLiveUpdatesEnabled()
        isIslandHideStatusBarInsets.value = settings.isIslandHideStatusBarInsetsEnabled()
        islandExpandedScale.floatValue = settings.getIslandExpandedScale()
        islandCameraPosition.value = settings.getIslandCameraPosition()
    }

    private fun loadPlugins() {
        isIslandShowCalls.value = settings.isIslandShowCallsEnabled()
        isIslandShowTimers.value = settings.isIslandShowTimersEnabled()
        isIslandTimersShowScreenRecorder.value = settings.isIslandTimersShowScreenRecorderEnabled()
        isIslandTimersFilterApps.value = settings.isIslandTimersFilterAppsEnabled()
        isIslandShowNetwork.value = settings.isIslandShowNetworkEnabled()
        isIslandShowSignal.value = settings.isIslandShowSignalEnabled()
        isIslandShowSoundMode.value = settings.isIslandShowSoundModeEnabled()
        isIslandSoundModeKeepIcon.value = settings.isIslandSoundModeKeepIconEnabled()
        isIslandShowAlarm.value = settings.isIslandShowAlarmEnabled()
        islandAlarmWindowHours.intValue = settings.getIslandAlarmWindowHours()
        isIslandShowCaffeinate.value = settings.isIslandShowCaffeinateEnabled()
        isIslandShowNavigation.value = settings.isIslandShowNavigationEnabled()
        islandNavigationOnNew.value = settings.getIslandNavigationOnNew()
        islandNavigationExpandDistance.intValue = settings.getIslandNavigationExpandDistance()
        isIslandNavigationPeekAfterExpand.value = settings.isIslandNavigationPeekAfterExpandEnabled()
        isIslandShowHotspot.value = settings.isIslandShowHotspotEnabled()
        isIslandShowDevices.value = settings.isIslandShowDevicesEnabled()
        isIslandBriefEnabled.value = settings.isIslandBriefEnabled()
        isIslandBriefShowAlarm.value = settings.isIslandBriefShowAlarmEnabled()
        islandDevicesBatteryOrder.value = settings.getIslandDevicesBatteryOrder()
        islandExpandedPadding.floatValue = settings.getIslandExpandedPadding()
        islandExpandedTopPadding.floatValue = settings.getIslandExpandedTopPadding()
        islandExpandedBottomPadding.floatValue = settings.getIslandExpandedBottomPadding()
        islandExpandedTimeoutMs.longValue = settings.getIslandExpandedTimeoutMs()
        isIslandSuppressSystemHeadsUp.value = settings.isIslandSuppressSystemHeadsUpEnabled()
        isIslandDynamicHideStatusBar.value =
            settings.getBoolean(
                IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
                false,
            )
        isIslandCompactHideStatusBar.value =
            settings.getBoolean(
                IslandSettings.KEY_ISLAND_COMPACT_HIDE_STATUS_BAR,
                false,
            )
        isIslandHideWhenScreenOff.value = settings.isIslandHideWhenScreenOffEnabled()
        islandTimeoutMs.longValue = settings.getIslandTimeoutMs()
    }

    private fun loadNotifications() {
        isIslandLineStageEnabled.value = settings.isIslandLineStageEnabled()
        isIslandMediaPeekSongChange.value = settings.isIslandMediaPeekSongChangeEnabled()
        isIslandMediaKeepWhenPaused.value = settings.isIslandMediaKeepWhenPausedEnabled()
        isIslandMediaShowPrevious.value = settings.isIslandMediaShowPreviousEnabled()
        isIslandMediaShowLike.value = settings.isIslandMediaShowLikeEnabled()
        islandNotifPeekStyle.value = settings.getIslandNotifPeekStyle()
        isIslandNotifKeepProgress.value = settings.isIslandNotifKeepProgressEnabled()
        isIslandNotifQueue.value = settings.isIslandNotifQueueEnabled()
        islandNotifQueueStyle.value = settings.getIslandNotifQueueStyle()
        isIslandNotifQueueInline.value = settings.isIslandNotifQueueInlineEnabled()
        isIslandNotifSkipSilent.value = settings.isIslandNotifSkipSilentEnabled()
        isIslandNotifFollowDnd.value = settings.isIslandNotifFollowDndEnabled()
        isIslandNotifFilterApps.value = settings.isIslandNotifFilterAppsEnabled()
        isIslandShowNotifications.value = settings.isIslandShowNotificationsEnabled()
        isIslandNotifHub.value = settings.isIslandNotifHubEnabled()
        islandNotifHubIcons.intValue = settings.getIslandNotifHubIcons()
        islandNotifHubOrder.value = settings.getIslandNotifHubOrder()
        isIslandNotifHubIncludeSilent.value = settings.isIslandNotifHubIncludeSilent()
        isIslandNotifHubGroupByApps.value = settings.isIslandNotifHubGroupByApps()
        isIslandNotifTapToOpen.value = settings.isIslandNotifTapToOpenEnabled()
        isIslandCatchUpEnabled.value = settings.isIslandCatchUpEnabled()
        islandCatchUpTimeoutMs.longValue = settings.getIslandCatchUpStoredMs()
        isIslandOtpEnabled.value = settings.isIslandOtpEnabled()
        islandOtpTimeoutMs.longValue = settings.getIslandOtpTimeoutMs()
        isIslandShowGlow.value = settings.isIslandShowGlowEnabled()
        isIslandBorderOutline.value = settings.isIslandBorderOutlineEnabled()
        islandBorderOutlineColor.value = settings.getIslandBorderOutlineColor()
        isIslandBorderOutlineDynamic.value = settings.isIslandBorderOutlineDynamicEnabled()
        islandBorderOutlineThickness.floatValue = settings.getIslandBorderOutlineThickness()
        isIslandBorderOutlineHiddenWhenExpanded.value = settings.isIslandBorderOutlineHiddenWhenExpanded()
    }

    private fun loadFeatures() {
        isIslandPulseShadow.value = settings.isIslandPulseShadowEnabled()
        islandPulseShadowSize.floatValue = settings.getIslandPulseShadowSize()
        islandPulseShadowYShift.floatValue = settings.getIslandPulseShadowYShift()
        islandPulseShadowSpread.floatValue = settings.getIslandPulseShadowSpread()
        islandPulseShadowDurationMs.floatValue = settings.getIslandPulseShadowDurationMs()
        isIslandShowMedia.value = settings.isIslandShowMediaEnabled()
        isIslandShowCalendar.value = settings.isIslandShowCalendarEnabled()
        isIslandShowConsciousGate.value = settings.isIslandShowConsciousGateEnabled()
        isIslandShowTimeBattery.value = settings.isIslandShowTimeBatteryEnabled()
        isIslandBatteryPeekAlerts.value = settings.isIslandBatteryPeekAlertsEnabled()
        isIslandShowBattery.value = settings.isIslandShowBatteryEnabled()
        isIslandShowFlashlight.value = settings.isIslandShowFlashlightEnabled()
        islandBatteryStyle.value = settings.getIslandBatteryStyle()
        islandDevicesBatteryStyle.value = settings.getIslandDevicesBatteryStyle()
        isIslandBatteryPercentageEnabled.value = settings.isIslandBatteryPercentageEnabled()
        isIslandBatteryPercentageConditional.value = settings.isIslandBatteryPercentageConditional()
        isIslandBatteryIconConditional.value = settings.isIslandBatteryIconConditional()
        isIslandBatteryOnlyLow.value = settings.isIslandBatteryOnlyLowEnabled()
        isIslandDevicesBatteryOnlyLow.value = settings.isIslandDevicesBatteryOnlyLowEnabled()
        islandLongPressAction.value = settings.getIslandLongPressAction()
        islandSlideMode.value = settings.getIslandSlideMode()
        isIslandSlideTrack.value = settings.isIslandSlideTrackEnabled()
        isIslandLikeWhilePlaying.value = settings.isIslandLikeWhilePlayingEnabled()
        isIslandSlideInvertDirection.value = settings.isIslandSlideInvertDirectionEnabled()
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED ->
                isIslandBatteryIdleColorEnabled.value = settings.isIslandBatteryIdleColorEnabled()

            IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR ->
                islandBatteryIdleColor.value = settings.getIslandBatteryIdleColor()

            IslandSettings.KEY_ISLAND_ENABLED ->
                isIslandEnabled.value = settings.isIslandEnabled()

            IslandSettings.KEY_ISLAND_USE_AUTO_DETECT ->
                isIslandAutoDetect.value = settings.isIslandAutoDetectEnabled()

            IslandSettings.KEY_ISLAND_CAMERA_OFFSET_X ->
                islandCameraOffsetX.floatValue = settings.getIslandCameraOffsetX()

            IslandSettings.KEY_ISLAND_CAMERA_OFFSET_Y ->
                islandCameraOffsetY.floatValue = settings.getIslandCameraOffsetY()

            IslandSettings.KEY_ISLAND_CAMERA_SIZE ->
                islandCameraSize.floatValue = settings.getIslandCameraSize()

            IslandSettings.KEY_ISLAND_MAX_WIDTH ->
                islandMaxWidth.floatValue = settings.getIslandMaxWidth()

            IslandSettings.KEY_ISLAND_CUTOUT_GAP ->
                islandCutoutGap.floatValue = settings.getIslandCutoutGap()

            IslandSettings.KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP ->
                isIslandSuppressSystemHeadsUp.value = settings.isIslandSuppressSystemHeadsUpEnabled()

            else -> onBehaviorPreferenceChanged(key)
        }
    }

    private fun onBehaviorPreferenceChanged(key: String) {
        when (key) {
            IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR ->
                isIslandDynamicHideStatusBar.value =
                    settings.getBoolean(
                        IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
                        false,
                    )

            IslandSettings.KEY_ISLAND_HIDE_WHEN_SCREEN_OFF ->
                isIslandHideWhenScreenOff.value = settings.isIslandHideWhenScreenOffEnabled()

            IslandSettings.KEY_ISLAND_TIMEOUT_MS ->
                islandTimeoutMs.longValue = settings.getIslandTimeoutMs()

            IslandSettings.KEY_ISLAND_LINE_STAGE_ENABLED ->
                isIslandLineStageEnabled.value = settings.isIslandLineStageEnabled()

            IslandSettings.KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE ->
                isIslandMediaPeekSongChange.value = settings.isIslandMediaPeekSongChangeEnabled()

            IslandSettings.KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED ->
                isIslandMediaKeepWhenPaused.value = settings.isIslandMediaKeepWhenPausedEnabled()

            IslandSettings.KEY_ISLAND_MEDIA_SHOW_PREVIOUS ->
                isIslandMediaShowPrevious.value = settings.isIslandMediaShowPreviousEnabled()

            IslandSettings.KEY_ISLAND_MEDIA_SHOW_LIKE ->
                isIslandMediaShowLike.value = settings.isIslandMediaShowLikeEnabled()

            IslandSettings.KEY_ISLAND_NOTIF_PEEK_STYLE ->
                islandNotifPeekStyle.value = settings.getIslandNotifPeekStyle()

            IslandSettings.KEY_ISLAND_NOTIF_QUEUE ->
                isIslandNotifQueue.value = settings.isIslandNotifQueueEnabled()

            else -> onNotificationPreferenceChanged(key)
        }
    }

    private fun onNotificationPreferenceChanged(key: String) {
        when (key) {
            IslandSettings.KEY_ISLAND_NOTIF_QUEUE_STYLE ->
                islandNotifQueueStyle.value = settings.getIslandNotifQueueStyle()

            IslandSettings.KEY_ISLAND_NOTIF_QUEUE_INLINE ->
                isIslandNotifQueueInline.value = settings.isIslandNotifQueueInlineEnabled()

            IslandSettings.KEY_ISLAND_NOTIF_SKIP_SILENT ->
                isIslandNotifSkipSilent.value = settings.isIslandNotifSkipSilentEnabled()

            IslandSettings.KEY_ISLAND_NOTIF_FOLLOW_DND ->
                isIslandNotifFollowDnd.value = settings.isIslandNotifFollowDndEnabled()

            IslandSettings.KEY_ISLAND_NOTIF_FILTER_APPS ->
                isIslandNotifFilterApps.value = settings.isIslandNotifFilterAppsEnabled()

            IslandSettings.KEY_ISLAND_SHOW_NOTIFICATIONS ->
                isIslandShowNotifications.value = settings.isIslandShowNotificationsEnabled()

            IslandSettings.KEY_ISLAND_NOTIF_HUB -> syncIslandNotifHubState()

            IslandSettings.KEY_ISLAND_NOTIF_HUB_SILENT ->
                isIslandNotifHubIncludeSilent.value = settings.isIslandNotifHubIncludeSilent()

            IslandSettings.KEY_ISLAND_NOTIF_HUB_GROUP ->
                isIslandNotifHubGroupByApps.value = settings.isIslandNotifHubGroupByApps()

            IslandSettings.KEY_ISLAND_NOTIF_HUB_ORDER ->
                islandNotifHubOrder.value = settings.getIslandNotifHubOrder()

            else -> onHubPreferenceChanged(key)
        }
    }

    private fun onHubPreferenceChanged(key: String) {
        when (key) {
            IslandSettings.KEY_ISLAND_NOTIF_HUB_ICONS ->
                islandNotifHubIcons.intValue = settings.getIslandNotifHubIcons()

            IslandSettings.KEY_ISLAND_NOTIF_TAP_TO_OPEN ->
                isIslandNotifTapToOpen.value = settings.isIslandNotifTapToOpenEnabled()

            IslandSettings.KEY_ISLAND_SHOW_NAVIGATION ->
                isIslandShowNavigation.value = settings.isIslandShowNavigationEnabled()

            IslandSettings.KEY_ISLAND_NAVIGATION_ON_NEW ->
                islandNavigationOnNew.value = settings.getIslandNavigationOnNew()

            IslandSettings.KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE ->
                islandNavigationExpandDistance.intValue = settings.getIslandNavigationExpandDistance()

            IslandSettings.KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND ->
                isIslandNavigationPeekAfterExpand.value = settings.isIslandNavigationPeekAfterExpandEnabled()

            IslandSettings.KEY_ISLAND_CATCH_UP_ENABLED ->
                isIslandCatchUpEnabled.value = settings.isIslandCatchUpEnabled()

            IslandSettings.KEY_ISLAND_CATCH_UP_TIMEOUT_MS ->
                islandCatchUpTimeoutMs.longValue = settings.getIslandCatchUpStoredMs()

            IslandSettings.KEY_ISLAND_OTP_ENABLED ->
                isIslandOtpEnabled.value = settings.isIslandOtpEnabled()

            IslandSettings.KEY_ISLAND_OTP_TIMEOUT_MS ->
                islandOtpTimeoutMs.longValue = settings.getIslandOtpTimeoutMs()

            else -> Unit
        }
    }

    fun refreshIslandCameraPlacement() {
        onFoldableChanged(duoSettings.isFoldableDevice())
        isIslandOrientationProfiles.value = settings.isIslandOrientationProfilesEnabled()
        isIslandHiddenInCurrentOrientation.value = settings.isIslandHiddenInCurrentOrientation()
        islandCameraOffsetX.floatValue = settings.getIslandCameraOffsetX()
        islandCameraOffsetY.floatValue = settings.getIslandCameraOffsetY()
        islandCameraSize.floatValue = settings.getIslandCameraSize()
        islandCameraPosition.value = settings.getIslandCameraPosition()
    }

    fun setIslandOrientationProfiles(enabled: Boolean) {
        settings.setIslandOrientationProfilesEnabled(enabled)
        refreshIslandCameraPlacement()
    }

    fun setIslandHiddenInCurrentOrientation(hidden: Boolean) {
        settings.setIslandHiddenInCurrentOrientation(hidden)
        isIslandHiddenInCurrentOrientation.value = hidden
    }

    fun setIslandBatteryIdleColorEnabled(enabled: Boolean) {
        isIslandBatteryIdleColorEnabled.value = enabled
        settings.setIslandBatteryIdleColorEnabled(enabled)
    }

    fun setIslandBatteryIdleColor(colorHex: String) {
        islandBatteryIdleColor.value = colorHex
        settings.setIslandBatteryIdleColor(colorHex)
    }

    private fun combinedActive(): Boolean = duoSettings.isDuoIslandCombined()

    fun setIslandEnabled(enabled: Boolean) {
        isIslandEnabled.value = enabled
        settings.setIslandEnabled(enabled)
    }

    fun setIslandAutoDetect(enabled: Boolean) {
        isIslandAutoDetect.value = enabled
        settings.setIslandAutoDetectEnabled(enabled)
    }

    fun autoAlignIslandWithCamera(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return false
        val metrics = wm.maximumWindowMetrics
        val rect =
            try {
                metrics.windowInsets.displayCutout
                    ?.boundingRects
                    ?.let { rects -> rects.find { it.top == 0 } ?: rects.firstOrNull() }
            } catch (_: Exception) {
                null
            } ?: return false
        val density = context.resources.displayMetrics.density
        val screenWidth = metrics.bounds.width().toFloat()
        val screenHeight = metrics.bounds.height().toFloat()
        val radius = (minOf(rect.width(), rect.height()) / 2f).coerceAtLeast(12f * density)
        setIslandCameraOffsetX((rect.exactCenterX() / screenWidth * 100f).coerceIn(0f, 100f))
        setIslandCameraOffsetY((rect.exactCenterY() / screenHeight * 100f).coerceIn(0f, 20f))
        setIslandCameraSize((radius / (16f * density)).coerceIn(0.05f, 2.0f))
        setIslandAutoDetect(false)
        return true
    }

    fun setIslandCameraOffsetX(value: Float) {
        islandCameraOffsetX.floatValue = value
        settings.setIslandCameraOffsetX(value)
    }

    fun setIslandCameraOffsetY(value: Float) {
        islandCameraOffsetY.floatValue = value
        settings.setIslandCameraOffsetY(value)
    }

    fun setIslandCameraSize(value: Float) {
        islandCameraSize.floatValue = value
        settings.setIslandCameraSize(value)
    }

    fun setIslandMaxWidth(value: Float) {
        islandMaxWidth.floatValue = value
        settings.setIslandMaxWidth(value)
    }

    fun setIslandCutoutGap(value: Float) {
        islandCutoutGap.floatValue = value
        settings.setIslandCutoutGap(value)
    }

    fun setIslandExpandedWidth(value: Float) {
        islandExpandedWidth.floatValue = value
        settings.setIslandExpandedWidth(value)
    }

    fun setIslandDevicesBatteryOrder(addresses: List<String>) {
        islandDevicesBatteryOrder.value = addresses
        settings.setIslandDevicesBatteryOrder(addresses)
    }

    fun setIslandBriefShowAlarm(enabled: Boolean) {
        isIslandBriefShowAlarm.value = enabled
        settings.setIslandBriefShowAlarmEnabled(enabled)
    }

    fun setIslandBriefEnabled(enabled: Boolean) {
        isIslandBriefEnabled.value = enabled
        settings.setIslandBriefEnabled(enabled)
    }

    fun setIslandShowDevices(enabled: Boolean) {
        isIslandShowDevices.value = enabled
        settings.setIslandShowDevicesEnabled(enabled)
    }

    fun setIslandShowNavigation(enabled: Boolean) {
        isIslandShowNavigation.value = enabled
        settings.setIslandShowNavigationEnabled(enabled)
    }

    fun setIslandNavigationOnNew(mode: String) {
        islandNavigationOnNew.value = mode
        settings.setIslandNavigationOnNew(mode)
    }

    fun setIslandNavigationExpandDistance(meters: Int) {
        islandNavigationExpandDistance.intValue = meters
        settings.setIslandNavigationExpandDistance(meters)
    }

    fun setIslandNavigationPeekAfterExpand(enabled: Boolean) {
        isIslandNavigationPeekAfterExpand.value = enabled
        settings.setIslandNavigationPeekAfterExpandEnabled(enabled)
    }

    fun setIslandShowHotspot(enabled: Boolean) {
        isIslandShowHotspot.value = enabled
        settings.setIslandShowHotspotEnabled(enabled)
    }

    fun setIslandShowCaffeinate(enabled: Boolean) {
        isIslandShowCaffeinate.value = enabled
        settings.setIslandShowCaffeinateEnabled(enabled)
    }

    fun setIslandShowAlarm(enabled: Boolean) {
        isIslandShowAlarm.value = enabled
        settings.setIslandShowAlarmEnabled(enabled)
    }

    fun setIslandAlarmWindowHours(hours: Int) {
        islandAlarmWindowHours.intValue = hours
        settings.setIslandAlarmWindowHours(hours)
    }

    fun setIslandShowSoundMode(enabled: Boolean) {
        isIslandShowSoundMode.value = enabled
        settings.setIslandShowSoundModeEnabled(enabled)
    }

    fun setIslandSoundModeKeepIcon(enabled: Boolean) {
        isIslandSoundModeKeepIcon.value = enabled
        settings.setIslandSoundModeKeepIconEnabled(enabled)
    }

    fun setIslandShowSignal(enabled: Boolean) {
        isIslandShowSignal.value = enabled
        settings.setIslandShowSignalEnabled(enabled)
    }

    fun setIslandShowNetwork(enabled: Boolean) {
        isIslandShowNetwork.value = enabled
        settings.setIslandShowNetworkEnabled(enabled)
    }

    fun loadIslandCalendarEmojis() {
        islandCalendarEmojis.value = settings.getIslandCalendarEmojis()
    }

    fun setIslandCalendarEmoji(
        calendarId: Long,
        emoji: String?,
    ) {
        settings.setIslandCalendarEmoji(calendarId, emoji)
        islandCalendarEmojis.value = settings.getIslandCalendarEmojis()
    }

    fun setIslandShowTimers(enabled: Boolean) {
        isIslandShowTimers.value = enabled
        settings.setIslandShowTimersEnabled(enabled)
    }

    fun setIslandTimersShowScreenRecorder(enabled: Boolean) {
        isIslandTimersShowScreenRecorder.value = enabled
        settings.setIslandTimersShowScreenRecorderEnabled(enabled)
    }

    fun setIslandTimersFilterApps(enabled: Boolean) {
        isIslandTimersFilterApps.value = enabled
        settings.setIslandTimersFilterAppsEnabled(enabled)
    }

    fun loadIslandTimersSelectedApps(context: Context): List<AppSelection> = settings.loadIslandTimersSelectedApps()

    fun saveIslandTimersSelectedApps(
        context: Context,
        apps: List<AppSelection>,
    ) {
        settings.saveIslandTimersSelectedApps(apps)
    }

    fun updateIslandTimersAppEnabled(
        context: Context,
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateIslandTimersAppSelection(packageName, enabled)
    }

    fun setIslandShowCalls(enabled: Boolean) {
        isIslandShowCalls.value = enabled
        settings.setIslandShowCallsEnabled(enabled)
    }

    fun setIslandCameraPosition(value: String) {
        islandCameraPosition.value = value
        settings.setIslandCameraPosition(value)
    }

    fun setIslandExpandedScale(value: Float) {
        islandExpandedScale.floatValue = value
        settings.setIslandExpandedScale(value)
    }

    fun setIslandHideLiveUpdates(enabled: Boolean) {
        isIslandHideLiveUpdates.value = enabled
        settings.setIslandHideLiveUpdatesEnabled(enabled)
    }

    fun setIslandHideStatusBarInsets(
        enabled: Boolean,
        context: Context,
    ) {
        isIslandHideStatusBarInsets.value = enabled
        settings.setIslandHideStatusBarInsetsEnabled(enabled)
        if (enabled) setIslandHideLiveUpdates(false)
        val appContext = context.applicationContext
        scope.launch(Dispatchers.IO) {
            StatusBarInsetsUtils.apply(appContext, enabled)
        }
    }

    fun setIslandDismissOnOutside(enabled: Boolean) {
        isIslandDismissOnOutside.value = enabled
        settings.setIslandDismissOnOutsideEnabled(enabled)
    }

    fun setIslandHideInOwnerApp(enabled: Boolean) {
        isIslandHideInOwnerApp.value = enabled
        settings.setIslandHideInOwnerAppEnabled(enabled)
    }

    fun setIslandLandscapeTopSpacing(value: Float) {
        islandLandscapeTopSpacing.floatValue = value
        settings.setIslandLandscapeTopSpacing(value)
    }

    fun setIslandAlwaysGestures(enabled: Boolean) {
        isIslandAlwaysGestures.value = enabled
        settings.setIslandAlwaysGesturesEnabled(enabled)
    }

    fun setIslandMaxItems(value: Int) {
        islandMaxItems.value = value
        settings.setIslandMaxItems(value)
    }

    fun setIslandSecondaryBubble(enabled: Boolean) {
        isIslandSecondaryBubble.value = enabled
        settings.setIslandSecondaryBubbleEnabled(enabled)
    }

    fun setIslandBalanceCompact(enabled: Boolean) {
        isIslandBalanceCompact.value = enabled
        settings.setIslandBalanceCompactEnabled(enabled)
    }

    fun setIslandBondEdge(enabled: Boolean) {
        isIslandBondEdge.value = enabled
        settings.setIslandBondEdgeEnabled(enabled)
    }

    fun setIslandKeepInFullscreen(enabled: Boolean) {
        isIslandKeepInFullscreen.value = enabled
        settings.setIslandKeepInFullscreenEnabled(enabled)
    }

    fun setIslandDisableInteractionsInFullscreen(enabled: Boolean) {
        isIslandDisableInteractionsInFullscreen.value = enabled
        settings.setIslandDisableInteractionsInFullscreenEnabled(enabled)
    }

    fun setIslandKeepOnLandscape(enabled: Boolean) {
        isIslandKeepOnLandscape.value = enabled
        settings.setIslandKeepOnLandscapeEnabled(enabled)
    }

    fun setIslandHideOnShade(enabled: Boolean) {
        isIslandHideOnShade.value = enabled
        settings.setIslandHideOnShadeEnabled(enabled)
    }

    fun setIslandFontScale(value: Float) {
        islandFontScale.floatValue = value
        settings.setIslandFontScale(value)
    }

    fun setIslandExpandedRoundness(value: Float) {
        islandExpandedRoundness.floatValue = value
        settings.setIslandExpandedRoundness(value)
    }

    fun setIslandExpandedPadding(value: Float) {
        islandExpandedPadding.floatValue = value
        settings.setIslandExpandedPadding(value)
    }

    fun setIslandExpandedTopPadding(value: Float) {
        islandExpandedTopPadding.floatValue = value
        settings.setIslandExpandedTopPadding(value)
    }

    fun setIslandExpandedBottomPadding(value: Float) {
        islandExpandedBottomPadding.floatValue = value
        settings.setIslandExpandedBottomPadding(value)
    }

    fun setIslandExpandedTimeoutMs(value: Long) {
        islandExpandedTimeoutMs.longValue = value
        settings.setIslandExpandedTimeoutMs(value)
    }

    fun setIslandSuppressSystemHeadsUp(enabled: Boolean) {
        isIslandSuppressSystemHeadsUp.value = enabled
        settings.setIslandSuppressSystemHeadsUpEnabled(enabled)
    }

    fun setIslandCompactHideStatusBar(enabled: Boolean) {
        isIslandCompactHideStatusBar.value = enabled
        settings.putBoolean(IslandSettings.KEY_ISLAND_COMPACT_HIDE_STATUS_BAR, enabled)
    }

    fun setIslandDynamicHideStatusBar(
        enabled: Boolean,
        context: Context,
    ) {
        isIslandDynamicHideStatusBar.value = enabled
        settings.putBoolean(
            IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
            enabled,
        )
        if (!enabled) {
            IslandStatusBarHider.restore(context)
        }
    }

    fun setIslandHideWhenScreenOff(enabled: Boolean) {
        isIslandHideWhenScreenOff.value = enabled
        settings.setIslandHideWhenScreenOffEnabled(enabled)
    }

    fun setIslandTimeoutMs(value: Long) {
        islandTimeoutMs.longValue = value
        settings.setIslandTimeoutMs(value)
    }

    fun setIslandLineStageEnabled(enabled: Boolean) {
        isIslandLineStageEnabled.value = enabled
        settings.setIslandLineStageEnabled(enabled)
    }

    fun setIslandMediaPeekSongChange(enabled: Boolean) {
        isIslandMediaPeekSongChange.value = enabled
        settings.setIslandMediaPeekSongChangeEnabled(enabled)
    }

    fun setIslandMediaKeepWhenPaused(enabled: Boolean) {
        isIslandMediaKeepWhenPaused.value = enabled
        settings.setIslandMediaKeepWhenPausedEnabled(enabled)
    }

    fun setIslandMediaShowPrevious(enabled: Boolean) {
        isIslandMediaShowPrevious.value = enabled
        settings.setIslandMediaShowPreviousEnabled(enabled)
    }

    fun setIslandMediaShowLike(enabled: Boolean) {
        isIslandMediaShowLike.value = enabled
        settings.setIslandMediaShowLikeEnabled(enabled)
    }

    fun setIslandShowNotifications(enabled: Boolean) {
        isIslandShowNotifications.value = enabled
        settings.setIslandShowNotificationsEnabled(enabled)
    }

    fun setIslandNotifHub(enabled: Boolean) {
        if (enabled) settings.setIslandShowNotificationsEnabled(true)
        settings.setIslandNotifHubEnabled(enabled)
        isIslandShowNotifications.value = settings.isIslandShowNotificationsEnabled()
        syncIslandNotifHubState()
    }

    fun setIslandNotifHubIncludeSilent(enabled: Boolean) {
        isIslandNotifHubIncludeSilent.value = enabled
        settings.setIslandNotifHubIncludeSilent(enabled)
    }

    fun setIslandNotifHubGroupByApps(enabled: Boolean) {
        isIslandNotifHubGroupByApps.value = enabled
        settings.setIslandNotifHubGroupByApps(enabled)
    }

    fun setIslandNotifHubOrder(value: String) {
        islandNotifHubOrder.value = value
        settings.setIslandNotifHubOrder(value)
    }

    fun setIslandNotifHubIcons(value: Int) {
        islandNotifHubIcons.intValue = value
        settings.setIslandNotifHubIcons(value)
    }

    private fun syncIslandNotifHubState() {
        isIslandNotifHub.value = settings.isIslandNotifHubEnabled()
        isIslandNotifQueue.value = settings.isIslandNotifQueueEnabled()
        isIslandCatchUpEnabled.value = settings.isIslandCatchUpEnabled()
        islandCatchUpTimeoutMs.longValue = settings.getIslandCatchUpStoredMs()
    }

    fun setIslandNotifQueueInline(enabled: Boolean) {
        isIslandNotifQueueInline.value = enabled
        settings.setIslandNotifQueueInlineEnabled(enabled)
    }

    fun setIslandNotifQueueStyle(style: String) {
        islandNotifQueueStyle.value = style
        settings.setIslandNotifQueueStyle(style)
    }

    fun setIslandNotifQueue(enabled: Boolean) {
        isIslandNotifQueue.value = enabled
        settings.setIslandNotifQueueEnabled(enabled)
    }

    fun setIslandNotifSkipSilent(enabled: Boolean) {
        isIslandNotifSkipSilent.value = enabled
        settings.setIslandNotifSkipSilentEnabled(enabled)
    }

    fun setIslandNotifFollowDnd(enabled: Boolean) {
        isIslandNotifFollowDnd.value = enabled
        settings.setIslandNotifFollowDndEnabled(enabled)
    }

    fun setIslandNotifFilterApps(enabled: Boolean) {
        isIslandNotifFilterApps.value = enabled
        settings.setIslandNotifFilterAppsEnabled(enabled)
    }

    fun loadIslandNotifFilterApps(): List<AppSelection> = settings.loadIslandNotifFilterApps()

    fun saveIslandNotifFilterApps(apps: List<AppSelection>) {
        settings.saveIslandNotifFilterApps(apps)
    }

    fun updateIslandNotifFilterApp(
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateIslandNotifFilterAppSelection(packageName, enabled)
    }

    fun setIslandNotifTapToOpen(enabled: Boolean) {
        isIslandNotifTapToOpen.value = enabled
        settings.setIslandNotifTapToOpenEnabled(enabled)
    }

    fun setIslandNotifKeepProgress(enabled: Boolean) {
        isIslandNotifKeepProgress.value = enabled
        settings.setIslandNotifKeepProgressEnabled(enabled)
        if (enabled && combinedActive()) {
            duoLink.onIslandShowsProgress()
        }
    }

    fun setIslandNotifPeekStyle(style: String) {
        islandNotifPeekStyle.value = style
        settings.setIslandNotifPeekStyle(style)
    }

    fun setIslandCatchUpEnabled(enabled: Boolean) {
        isIslandCatchUpEnabled.value = enabled
        settings.setIslandCatchUpEnabled(enabled)
    }

    fun setIslandCatchUpTimeoutMs(value: Long) {
        islandCatchUpTimeoutMs.longValue = value
        settings.setIslandCatchUpTimeoutMs(value)
    }

    fun setIslandOtpEnabled(enabled: Boolean) {
        isIslandOtpEnabled.value = enabled
        settings.setIslandOtpEnabled(enabled)
    }

    fun setIslandOtpTimeoutMs(value: Long) {
        islandOtpTimeoutMs.longValue = value
        settings.setIslandOtpTimeoutMs(value)
    }

    fun setIslandShowGlow(enabled: Boolean) {
        isIslandShowGlow.value = enabled
        settings.setIslandShowGlowEnabled(enabled)
    }

    fun setIslandBorderOutline(enabled: Boolean) {
        isIslandBorderOutline.value = enabled
        settings.setIslandBorderOutlineEnabled(enabled)
    }

    fun setIslandBorderOutlineColor(colorHex: String) {
        islandBorderOutlineColor.value = colorHex
        settings.setIslandBorderOutlineColor(colorHex)
    }

    fun setIslandBorderOutlineDynamic(enabled: Boolean) {
        isIslandBorderOutlineDynamic.value = enabled
        settings.setIslandBorderOutlineDynamicEnabled(enabled)
    }

    fun setIslandBorderOutlineThickness(value: Float) {
        islandBorderOutlineThickness.floatValue = value
        settings.setIslandBorderOutlineThickness(value)
    }

    fun setIslandBorderOutlineHiddenWhenExpanded(hidden: Boolean) {
        isIslandBorderOutlineHiddenWhenExpanded.value = hidden
        settings.setIslandBorderOutlineHiddenWhenExpanded(hidden)
    }

    fun setIslandPulseShadow(enabled: Boolean) {
        isIslandPulseShadow.value = enabled
        settings.setIslandPulseShadowEnabled(enabled)
    }

    fun setIslandPulseShadowSize(value: Float) {
        islandPulseShadowSize.floatValue = value
        settings.setIslandPulseShadowSize(value)
    }

    fun setIslandPulseShadowYShift(value: Float) {
        islandPulseShadowYShift.floatValue = value
        settings.setIslandPulseShadowYShift(value)
    }

    fun setIslandPulseShadowSpread(value: Float) {
        islandPulseShadowSpread.floatValue = value
        settings.setIslandPulseShadowSpread(value)
    }

    fun setIslandPulseShadowDurationMs(value: Float) {
        islandPulseShadowDurationMs.floatValue = value
        settings.setIslandPulseShadowDurationMs(value)
    }

    fun setIslandShowMedia(enabled: Boolean) {
        isIslandShowMedia.value = enabled
        settings.setIslandShowMediaEnabled(enabled)
        if (enabled && combinedActive()) {
            duoLink.onIslandShowsMedia()
        }
    }

    fun setIslandShowCalendar(enabled: Boolean) {
        isIslandShowCalendar.value = enabled
        settings.setIslandShowCalendarEnabled(enabled)
    }

    fun setIslandShowConsciousGate(enabled: Boolean) {
        isIslandShowConsciousGate.value = enabled
        settings.setIslandShowConsciousGateEnabled(enabled)
    }

    fun setIslandShowFlashlight(enabled: Boolean) {
        isIslandShowFlashlight.value = enabled
        settings.setIslandShowFlashlightEnabled(enabled)
        if (enabled && combinedActive()) {
            duoLink.onIslandShowsFlashlight()
        }
    }

    fun setIslandShowTimeBattery(enabled: Boolean) {
        isIslandShowTimeBattery.value = enabled
        settings.setIslandShowTimeBatteryEnabled(enabled)
        if (enabled && combinedActive()) {
            duoLink.onIslandShowsTime()
        }
    }

    fun setIslandBatteryStyle(value: String) {
        islandBatteryStyle.value = value
        settings.setIslandBatteryStyle(value)
    }

    fun setIslandDevicesBatteryStyle(value: String) {
        islandDevicesBatteryStyle.value = value
        settings.setIslandDevicesBatteryStyle(value)
    }

    fun setIslandShowBattery(enabled: Boolean) {
        isIslandShowBattery.value = enabled
        settings.setIslandShowBatteryEnabled(enabled)
    }

    fun setIslandBatteryPeekAlerts(enabled: Boolean) {
        isIslandBatteryPeekAlerts.value = enabled
        settings.setIslandBatteryPeekAlertsEnabled(enabled)
    }

    fun setIslandBatteryPercentageEnabled(enabled: Boolean) {
        isIslandBatteryPercentageEnabled.value = enabled
        settings.setIslandBatteryPercentageEnabled(enabled)
    }

    fun setIslandDevicesBatteryOnlyLow(enabled: Boolean) {
        isIslandDevicesBatteryOnlyLow.value = enabled
        settings.setIslandDevicesBatteryOnlyLowEnabled(enabled)
    }

    fun setIslandBatteryOnlyLow(enabled: Boolean) {
        isIslandBatteryOnlyLow.value = enabled
        settings.setIslandBatteryOnlyLowEnabled(enabled)
    }

    fun setIslandBatteryIconConditional(enabled: Boolean) {
        isIslandBatteryIconConditional.value = enabled
        settings.setIslandBatteryIconConditional(enabled)
    }

    fun setIslandBatteryPercentageConditional(enabled: Boolean) {
        isIslandBatteryPercentageConditional.value = enabled
        settings.setIslandBatteryPercentageConditional(enabled)
    }

    fun setIslandLongPressAction(action: Action?) {
        islandLongPressAction.value = action
        settings.setIslandLongPressAction(action)
    }

    fun setIslandSlideMode(mode: String) {
        islandSlideMode.value = mode
        settings.setIslandSlideMode(mode)
    }

    fun setIslandLikeWhilePlayingEnabled(enabled: Boolean) {
        isIslandLikeWhilePlaying.value = enabled
        settings.setIslandLikeWhilePlayingEnabled(enabled)
    }

    fun setIslandSlideTrackEnabled(enabled: Boolean) {
        isIslandSlideTrack.value = enabled
        settings.setIslandSlideTrackEnabled(enabled)
    }

    fun setIslandSlideInvertDirection(enabled: Boolean) {
        isIslandSlideInvertDirection.value = enabled
        settings.setIslandSlideInvertDirection(enabled)
    }

    fun loadIslandMediaApps(context: Context): List<AppSelection> = settings.loadIslandMediaExcludedApps()

    fun saveIslandMediaApps(
        context: Context,
        apps: List<AppSelection>,
    ) {
        settings.saveIslandMediaExcludedApps(apps)
    }

    fun updateIslandMediaAppEnabled(
        context: Context,
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateIslandMediaExcludedAppSelection(packageName, enabled)
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED,
                IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR,
                IslandSettings.KEY_ISLAND_ENABLED,
                IslandSettings.KEY_ISLAND_USE_AUTO_DETECT,
                IslandSettings.KEY_ISLAND_CAMERA_OFFSET_X,
                IslandSettings.KEY_ISLAND_CAMERA_OFFSET_Y,
                IslandSettings.KEY_ISLAND_CAMERA_SIZE,
                IslandSettings.KEY_ISLAND_MAX_WIDTH,
                IslandSettings.KEY_ISLAND_CUTOUT_GAP,
                IslandSettings.KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP,
                IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
                IslandSettings.KEY_ISLAND_HIDE_WHEN_SCREEN_OFF,
                IslandSettings.KEY_ISLAND_TIMEOUT_MS,
                IslandSettings.KEY_ISLAND_LINE_STAGE_ENABLED,
                IslandSettings.KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE,
                IslandSettings.KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED,
                IslandSettings.KEY_ISLAND_MEDIA_SHOW_PREVIOUS,
                IslandSettings.KEY_ISLAND_MEDIA_SHOW_LIKE,
                IslandSettings.KEY_ISLAND_NOTIF_PEEK_STYLE,
                IslandSettings.KEY_ISLAND_NOTIF_QUEUE,
                IslandSettings.KEY_ISLAND_NOTIF_QUEUE_STYLE,
                IslandSettings.KEY_ISLAND_NOTIF_QUEUE_INLINE,
                IslandSettings.KEY_ISLAND_NOTIF_SKIP_SILENT,
                IslandSettings.KEY_ISLAND_NOTIF_FOLLOW_DND,
                IslandSettings.KEY_ISLAND_NOTIF_FILTER_APPS,
                IslandSettings.KEY_ISLAND_SHOW_NOTIFICATIONS,
                IslandSettings.KEY_ISLAND_NOTIF_HUB,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_SILENT,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_GROUP,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_ORDER,
                IslandSettings.KEY_ISLAND_NOTIF_HUB_ICONS,
                IslandSettings.KEY_ISLAND_NOTIF_TAP_TO_OPEN,
                IslandSettings.KEY_ISLAND_SHOW_NAVIGATION,
                IslandSettings.KEY_ISLAND_NAVIGATION_ON_NEW,
                IslandSettings.KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE,
                IslandSettings.KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND,
                IslandSettings.KEY_ISLAND_CATCH_UP_ENABLED,
                IslandSettings.KEY_ISLAND_CATCH_UP_TIMEOUT_MS,
                IslandSettings.KEY_ISLAND_OTP_ENABLED,
                IslandSettings.KEY_ISLAND_OTP_TIMEOUT_MS,
            )
    }
}
