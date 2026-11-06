package com.jawzf.pixeltoolkit.viewmodels

import android.Manifest
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.CalendarContract
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jawzf.pixeltoolkit.ToolkitApp
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.apps.AppUtil
import com.jawzf.pixeltoolkit.core.data.backup.ConfigBackup
import com.jawzf.pixeltoolkit.core.data.duo.DuoBatteryLevels
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.log.LogManager
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.core.platform.device.DeviceUtils
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionGrantUtil
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.PrivilegedMode
import com.jawzf.pixeltoolkit.core.platform.shell.RootUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarInsetsUtils
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarManager
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.domain.model.AppIcon
import com.jawzf.pixeltoolkit.domain.model.LockscreenShortcutSide
import com.jawzf.pixeltoolkit.domain.model.PowerChoice
import com.jawzf.pixeltoolkit.domain.model.RemapSlot
import com.jawzf.pixeltoolkit.domain.model.ScaleAnimationsProfile
import com.jawzf.pixeltoolkit.domain.model.SearchableItem
import com.jawzf.pixeltoolkit.domain.model.SystemShortcutsState
import com.jawzf.pixeltoolkit.domain.registry.SearchRegistry
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.apps.applock.AppLockState
import com.jawzf.pixeltoolkit.feature.apps.consciousgate.ConsciousGateState
import com.jawzf.pixeltoolkit.feature.apps.freeze.FreezeState
import com.jawzf.pixeltoolkit.feature.apps.updates.github.GitHubUser
import com.jawzf.pixeltoolkit.feature.connectivity.ConnectivitySettings
import com.jawzf.pixeltoolkit.feature.connectivity.ConnectivityState
import com.jawzf.pixeltoolkit.feature.display.aod.AodState
import com.jawzf.pixeltoolkit.feature.display.lockclock.LockClockState
import com.jawzf.pixeltoolkit.feature.display.nightlight.NightLightState
import com.jawzf.pixeltoolkit.feature.display.ondisplay.ToolkitOnDisplayState
import com.jawzf.pixeltoolkit.feature.display.pixels.SmartPixelsState
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRatePanelState
import com.jawzf.pixeltoolkit.feature.display.statusbar.StatusBarControlState
import com.jawzf.pixeltoolkit.feature.input.InputState
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.notifications.NotificationState
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.NotificationAutoDismisser
import com.jawzf.pixeltoolkit.feature.notifications.flashlight.FlashlightPulseState
import com.jawzf.pixeltoolkit.feature.notifications.hilight.HilightState
import com.jawzf.pixeltoolkit.feature.notifications.lighting.LightingState
import com.jawzf.pixeltoolkit.feature.power.BatteryState
import com.jawzf.pixeltoolkit.feature.power.PowerSettings
import com.jawzf.pixeltoolkit.feature.power.PowerState
import com.jawzf.pixeltoolkit.feature.security.SecuritySettings
import com.jawzf.pixeltoolkit.feature.security.SecurityState
import com.jawzf.pixeltoolkit.feature.sound.SoundSettings
import com.jawzf.pixeltoolkit.feature.sound.SoundState
import com.jawzf.pixeltoolkit.island.service.IslandStatusBarHider
import com.jawzf.pixeltoolkit.services.LiveUpdateSnoozer
import com.jawzf.pixeltoolkit.services.NotificationListener
import com.jawzf.pixeltoolkit.services.receivers.FlashlightActionReceiver
import com.jawzf.pixeltoolkit.services.receivers.SecurityDeviceAdminReceiver
import com.jawzf.pixeltoolkit.services.receivers.SecurityReceiver
import com.jawzf.pixeltoolkit.services.tiles.ScreenOffAccessibilityService
import com.jawzf.pixeltoolkit.utils.AppIconUtil
import com.jawzf.pixeltoolkit.utils.PowerButtonGestures
import com.jawzf.pixeltoolkit.utils.SplitScreenUtil
import com.jawzf.pixeltoolkit.utils.SurfaceFlingerControl
import com.jawzf.pixeltoolkit.utils.TestNotificationUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime

class MainViewModel : ViewModel() {
    val isAccessibilityEnabled = mutableStateOf(false)
    val isWidgetEnabled = mutableStateOf(false)
    val isWriteSecureSettingsEnabled = mutableStateOf(false)
    val isReadPhoneStateEnabled = mutableStateOf(false)
    val isPostNotificationsEnabled = mutableStateOf(false)
    val isShizukuPermissionGranted = mutableStateOf(false)
    val isShizukuAvailable = mutableStateOf(false)
    val pinnedFeatureKeys = mutableStateOf<List<String>>(emptyList())
    val pinnedQsTileKeys = mutableStateOf<List<String>>(emptyList())
    val isNotificationListenerEnabled = mutableStateOf(false)
    val isOverlayPermissionGranted = mutableStateOf(false)
    val isNotificationLightingAccessibilityEnabled = mutableStateOf(false)
    val hapticFeedbackType = mutableStateOf(HapticFeedbackType.SUBTLE)
    val defaultTab = mutableStateOf(com.jawzf.pixeltoolkit.domain.DIYTabs.TOOLKIT)
    val selectedAppIcon = mutableStateOf(AppIcon.DEFAULT)
    val isDefaultBrowserSet = mutableStateOf(false)
    val isButtonRemapEnabled = mutableStateOf(false)
    val isButtonRemapUseShizuku = mutableStateOf(false)
    val isButtonRemapPauseOnVolumeDialog = mutableStateOf(true)
    val shizukuDetectedDevicePath = mutableStateOf<String?>(null)
    val remapActions = mutableStateMapOf<RemapSlot, List<Action>>()
    val powerChoices = mutableStateMapOf<RemapSlot, PowerChoice>()
    val isPowerCameraFlipEnabled = mutableStateOf(false)
    val isLockscreenShortcutsEnabled = mutableStateOf(false)
    val lockscreenShortcutActions = mutableStateMapOf<LockscreenShortcutSide, List<Action>>()
    val lockscreenSystemShortcutsState = mutableStateOf(SystemShortcutsState.UNKNOWN)
    val remapHapticType = mutableStateOf(HapticFeedbackType.DOUBLE)

    val isDuoEnabled = mutableStateOf(false)
    val isDuoIslandCombined = mutableStateOf(false)
    val isDuoTapForBrief = mutableStateOf(false)
    val isDuoHideOnShade = mutableStateOf(false)
    val isDuoKeepInFullscreen = mutableStateOf(false)
    val isDuoDisableInteractionsInFullscreen = mutableStateOf(false)
    val isDuoAutoDetect = mutableStateOf(true)
    val isFoldableCameraDevice = mutableStateOf(false)
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
    val isIslandBatteryIdleColorEnabled = mutableStateOf(false)
    val islandBatteryIdleColor = mutableStateOf("#FFFFFF")
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
    val islandCameraPosition = mutableStateOf(SettingsRepository.ISLAND_CAMERA_POSITION_CENTER)
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
    val islandNavigationOnNew = mutableStateOf(SettingsRepository.ISLAND_NAVIGATION_ON_NEW_PEEK)
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
    val islandNotifPeekStyle = mutableStateOf(SettingsRepository.ISLAND_NOTIF_PEEK_STYLE_FULL)
    val isIslandNotifKeepProgress = mutableStateOf(true)
    val isIslandNotifQueue = mutableStateOf(true)
    val islandNotifQueueStyle = mutableStateOf(SettingsRepository.ISLAND_NOTIF_QUEUE_STYLE_STACK)
    val isIslandNotifQueueInline = mutableStateOf(false)
    val isIslandNotifSkipSilent = mutableStateOf(true)
    val isIslandNotifFollowDnd = mutableStateOf(true)
    val isIslandNotifFilterApps = mutableStateOf(false)
    val isIslandShowNotifications = mutableStateOf(true)
    val isIslandNotifHub = mutableStateOf(false)
    val islandNotifHubIcons = mutableIntStateOf(0)
    val isIslandNotifHubIncludeSilent = mutableStateOf(false)
    val isIslandNotifHubGroupByApps = mutableStateOf(false)
    val islandNotifHubOrder = mutableStateOf(SettingsRepository.ISLAND_NOTIF_HUB_ORDER_DYNAMIC)
    val isIslandNotifTapToOpen = mutableStateOf(false)
    val isIslandCatchUpEnabled = mutableStateOf(true)
    val islandCatchUpTimeoutMs = mutableLongStateOf(10000L)
    val isIslandOtpEnabled = mutableStateOf(false)
    val islandOtpTimeoutMs = mutableLongStateOf(60_000L)
    val isIslandShowGlow = mutableStateOf(true)
    val isIslandBorderOutline = mutableStateOf(false)
    val islandBorderOutlineColor = mutableStateOf(SettingsRepository.ISLAND_BORDER_OUTLINE_DEFAULT_COLOR)
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
    val islandBatteryStyle = mutableStateOf(SettingsRepository.ISLAND_BATTERY_STYLE_RING)
    val islandDevicesBatteryStyle = mutableStateOf(SettingsRepository.ISLAND_BATTERY_STYLE_RING)
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

    val isStatusGlanceEnabled = mutableStateOf(false)
    val isStatusGlanceAutoDetect = mutableStateOf(true)
    val isStatusGlanceOrientationProfiles = mutableStateOf(false)
    val isStatusGlanceHiddenInCurrentOrientation = mutableStateOf(false)
    val statusGlanceOffsetX = mutableFloatStateOf(60f)
    val statusGlanceOffsetY = mutableFloatStateOf(2f)
    val statusGlanceMaxWidth = mutableFloatStateOf(180f)
    val statusGlanceFontSize = mutableFloatStateOf(13f)
    val isStatusGlanceShowFlashlight = mutableStateOf(true)
    val isStatusGlanceShowCalendar = mutableStateOf(true)
    val statusGlanceCalendarTimeframe = mutableStateOf("today")
    val statusGlanceCalendarShowAllDay = mutableStateOf(false)
    val statusGlanceSelectedCalendarIds = mutableStateOf<Set<String>>(emptySet())
    val statusGlanceAvailableCalendars = mutableStateListOf<CalendarAccount>()
    val islandCalendarEmojis = mutableStateOf<Map<Long, String>>(emptyMap())
    val isStatusGlanceShowMedia = mutableStateOf(true)
    val isStatusGlanceShowTime = mutableStateOf(true)
    val isStatusGlanceBackgroundPill = mutableStateOf(false)
    val isStatusGlanceAlbumArtColors = mutableStateOf(true)
    val isStatusGlanceHideWhenFullscreen = mutableStateOf(true)
    val isStatusGlanceHideInQuickSettings = mutableStateOf(true)
    val isStatusGlanceHideWhenLocked = mutableStateOf(true)
    val isStatusGlanceBoldText = mutableStateOf(false)
    val isStatusGlanceShowBattery = mutableStateOf(false)
    val statusGlanceBatteryDisplayMode = mutableStateOf("icon")
    val isStatusGlanceBatteryShowLow = mutableStateOf(true)
    val isStatusGlanceBatteryShowCharging = mutableStateOf(true)
    val isStatusGlanceBatteryShowFull = mutableStateOf(true)
    val isStatusGlanceBatteryShowOtherwise = mutableStateOf(true)
    val statusGlanceBatteryIconSize = mutableFloatStateOf(16f)
    val statusGlanceTapAction = mutableStateOf<Action?>(null)
    val statusGlanceDoubleTapAction = mutableStateOf<Action?>(null)
    val statusGlanceLongPressAction = mutableStateOf<Action?>(null)

    val isFlashlightAlwaysTurnOffEnabled = mutableStateOf(false)
    val isFlashlightFadeEnabled = mutableStateOf(false)
    val isFlashlightAdjustEnabled = mutableStateOf(false)
    val isFlashlightGlobalEnabled = mutableStateOf(false)
    val isFlashlightLiveUpdateEnabled = mutableStateOf(true)
    val flashlightLastIntensity = mutableStateOf(1)
    val isFlashlightPocketTurnOffEnabled = mutableStateOf(false)
    val isFlashlightOverheatEnabled = mutableStateOf(true)
    val isLocationPermissionGranted = mutableStateOf(false)
    val isBackgroundLocationPermissionGranted = mutableStateOf(false)
    val isFullScreenIntentPermissionGranted = mutableStateOf(false)
    val isBluetoothPermissionGranted = mutableStateOf(false)
    val isStoragePermissionGranted = mutableStateOf(false)
    val isUsageStatsPermissionGranted = mutableStateOf(false)
    val appLanguage = mutableStateOf("en")

    val isAutoAccessibilityEnabled = mutableStateOf(false)
    val isOnboardingCompleted =
        mutableStateOf(true) // Default to true so it doesn't flash on first check if not loaded
    val connectivity = ConnectivityState(viewModelScope)
    val sound = SoundState()
    val battery = BatteryState()
    val power = PowerState(viewModelScope)
    val security =
        SecurityState(viewModelScope) { context, requesterId ->
            StatusBarManager.requestRestore(context, requesterId)
        }
    val freeze = FreezeState(viewModelScope)
    val appLock = AppLockState { context -> updateAppDetectionService(context) }
    val consciousGate = ConsciousGateState { context -> updateAppDetectionService(context) }
    val notifications =
        NotificationState {
            NotificationListener.instance?.let { NotificationAutoDismisser.resync(it) }
        }
    val hilight = HilightState()
    val lighting = LightingState()
    val flashlightPulse = FlashlightPulseState()
    val input = InputState(viewModelScope) { context -> updateAppDetectionService(context) }
    val smartPixels = SmartPixelsState()
    val refreshRate = RefreshRatePanelState()
    val aod = AodState(viewModelScope)
    val toolkitOnDisplay = ToolkitOnDisplayState()
    val lockClock = LockClockState()
    val statusBarControl = StatusBarControlState()
    val nightLight = NightLightState { updateAppDetectionService(it) }
    val addedQSTiles = mutableStateOf<Set<String>>(emptySet())
    val isHideGestureBarEnabled = mutableStateOf(false)
    val isHideGestureBarOnLauncherEnabled = mutableStateOf(false)
    val isCircleToSearchGestureEnabled = mutableStateOf(false)
    val circleToSearchGestureHeight = mutableFloatStateOf(48f)
    val circleToSearchGestureWidth = mutableFloatStateOf(240f)
    val isCircleToSearchPreviewEnabled = mutableStateOf(false)
    val isDisableRotationSuggestionEnabled = mutableStateOf(false)
    val isAllowOverlaysInSettingsEnabled = mutableStateOf(false)
    val isTransparentNavigationBarEnabled = mutableStateOf(false)
    val isSplitScreenGestureEnabled = mutableStateOf(false)
    val isPreferGpuComposingEnabled = mutableStateOf(false)
    val isPixelSearchbarEnabled = mutableStateOf(false)
    val pixelSearchbarType = mutableStateOf("empty")
    val pixelSearchbarDateFormat = mutableStateOf("EEEE, MMMM d")
    val pixelSearchbarBackgroundPill = mutableStateOf(false)
    val pixelSearchbarWidgetId =
        mutableIntStateOf(android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID)
    val pixelSearchbarWidgetProvider = mutableStateOf<String?>(null)
    val pixelSearchbarScrapedLine1 = mutableStateOf("")
    val pixelSearchbarScrapedLine2 = mutableStateOf("")
    val pixelSearchbarWidgetPaddingH = mutableIntStateOf(0)
    val pixelSearchbarWidgetPaddingV = mutableIntStateOf(0)
    val pixelSearchbarTapActionEnabled = mutableStateOf(true)
    val pixelSearchbarMusicTitle = mutableStateOf("")
    val pixelSearchbarMusicArtist = mutableStateOf("")
    val pixelSearchbarMusicPackage = mutableStateOf("")

    val shutUpConfigs =
        mutableStateOf<List<com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig>>(emptyList())
    val isShutUpLoading = mutableStateOf(false)
    val isShutUpAttemptShizukuRestart = mutableStateOf(true)
    val shutUpRestoreDelay = mutableIntStateOf(10)
    val shutUpRestoreMode = mutableStateOf("Auto")
    val shutUpKeyboard = mutableStateOf("")
    val shizukuAuthToken = mutableStateOf("")

    data class CalendarAccount(
        val id: Long,
        val name: String,
        val accountName: String,
        val isSelected: Boolean,
    )

    val isDeviceAdminEnabled = mutableStateOf(false)
    val isDeveloperModeEnabled = mutableStateOf(false)
    val isNotificationPolicyAccessGranted = mutableStateOf(false)
    val isUseUsageAccess = mutableStateOf(false)
    val isHiddenDebuggingSupport = mutableStateOf(false)

    // Search state
    val searchQuery = mutableStateOf("")
    val searchResults = mutableStateOf<List<SearchableItem>>(emptyList())
    val isSearching = mutableStateOf(false)
    val recentSearches = mutableStateOf<List<SearchableItem>>(emptyList())
    private var searchJob: Job? = null

    val isRootEnabled = mutableStateOf(false)
    val privilegedMode = mutableStateOf(PrivilegedMode.AUTO)
    val usesAuthToken = mutableStateOf(true)
    val isRootAvailable = mutableStateOf(false)
    val isRootPermissionGranted = mutableStateOf(false)
    val hasPendingUpdates = mutableStateOf(false)

    val isPitchBlackThemeEnabled = mutableStateOf(false)

    val isEnableUnsupportedFeatures = mutableStateOf(false)
    val isShowLegacyFeatures = mutableStateOf(true)
    val isSecureSensitiveTilesEnabled = mutableStateOf(true)
    val isBlurEnabled = mutableStateOf(true)
    val isBlurSettingEnabled = mutableStateOf(true)
    val isRippleEnabled = mutableStateOf(true)
    val isRippleSettingEnabled = mutableStateOf(true)
    val isMotionBlurEnabled = mutableStateOf(false)
    val isMotionBlurSettingEnabled = mutableStateOf(false)
    val motionBlurScale = mutableFloatStateOf(1.0f)
    val isShakeForHelpEnabled = mutableStateOf(true)
    val isSwipeTabsEnabled = mutableStateOf(true)
    val isPowerSaveModeEnabled = mutableStateOf(false)
    private var powerSaveReceiver: BroadcastReceiver? = null

    val isKeyboardEnabled = mutableStateOf(false)
    val isKeyboardSelected = mutableStateOf(false)
    val isWriteSettingsEnabled = mutableStateOf(false)
    val isCalendarPermissionGranted = mutableStateOf(false)
    val isBubblePermissionGranted = mutableStateOf(false)
    val isSensitiveNotificationsGranted = mutableStateOf(true)
    val isLocalNetworkGranted = mutableStateOf(true)

    val scaleAnimationsMode = mutableStateOf("default")
    val isTouchSensitivityEnabled = mutableStateOf(false)
    val isAutoRotateEnabled = mutableStateOf(false)
    val screenTimeout = mutableStateOf(30000L)
    val fontScale = mutableFloatStateOf(1.0f)
    val fontWeight = mutableIntStateOf(0)
    val animatorDurationScale = mutableFloatStateOf(1.0f)
    val transitionAnimationScale = mutableFloatStateOf(1.0f)
    val windowAnimationScale = mutableFloatStateOf(1.0f)
    val smallestWidth = mutableIntStateOf(360)
    val hasShizukuPermission = mutableStateOf(false)

    lateinit var settingsRepository: SettingsRepository
    private lateinit var duoSettings: DuoSettings
    private var appContext: Context? = null

    val gitHubToken = mutableStateOf<String?>(null)
    val gitHubUser = mutableStateOf<GitHubUser?>(null)

    private val contentObserver =
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(
                selfChange: Boolean,
                uri: Uri?,
            ) {
                uri?.let {
                    when (it) {
                        Settings.System.getUriFor(Settings.System.FONT_SCALE) -> {
                            fontScale.floatValue = settingsRepository.getFontScale()
                        }

                        Settings.Secure.getUriFor("font_weight_adjustment") -> {
                            fontWeight.intValue = settingsRepository.getFontWeight()
                        }

                        Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE) -> {
                            animatorDurationScale.floatValue =
                                settingsRepository.getAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE)
                        }

                        Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE) -> {
                            transitionAnimationScale.floatValue =
                                settingsRepository.getAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE)
                        }

                        Settings.Global.getUriFor(Settings.Global.WINDOW_ANIMATION_SCALE) -> {
                            windowAnimationScale.floatValue =
                                settingsRepository.getAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE)
                        }

                        Settings.Secure.getUriFor("display_density_forced") -> {
                            smallestWidth.intValue = settingsRepository.getSmallestWidth()
                        }

                        Settings.Secure.getUriFor("doze_always_on") -> {
                            aod.syncAodEnabled()
                        }

                        Settings.Secure.getUriFor("sysui_qs_tiles") -> {
                            appContext?.let { updateAddedQSTiles(it) }
                        }

                        Settings.System.getUriFor("peak_refresh_rate"),
                        Settings.System.getUriFor("min_refresh_rate"),
                        -> {
                            appContext?.let { refreshRate.syncRefreshRateState(it) }
                        }

                        Settings.Global.getUriFor("battery_saver_constants") -> {
                            appContext?.let { power.loadBatterySaverConstants(it) }
                        }

                        Settings.Global.getUriFor("audio_safe_volume_state") -> {
                            appContext?.let { sound.syncAudioSafeVolumeState(it) }
                        }

                        Settings.Global.getUriFor("low_power_trigger_level") -> {
                            appContext?.let { power.syncLowPowerTriggerLevel(it) }
                        }

                        Settings.Secure.getUriFor("show_notification_snooze") -> {
                            appContext?.let { notifications.syncShowNotificationSnooze(it) }
                        }

                        Settings.Global.getUriFor("notification_snooze_options") -> {
                            appContext?.let { notifications.loadNotificationSnoozeOptions(it) }
                        }
                    }
                }
            }
        }

    private val preferenceChangeListener =
        object : android.content.SharedPreferences.OnSharedPreferenceChangeListener {
            override fun onSharedPreferenceChanged(
                sharedPreferences: android.content.SharedPreferences?,
                key: String?,
            ) {
                if (key == null) return

                when (key) {
                    DuoSettings.KEY_DUO_ENABLED ->
                        isDuoEnabled.value = duoSettings.isDuoEnabled()

                    DuoSettings.KEY_DUO_USE_AUTO_DETECT ->
                        isDuoAutoDetect.value = duoSettings.isDuoAutoDetectEnabled()

                    DuoSettings.KEY_DUO_CAMERA_OFFSET_X ->
                        duoCameraOffsetX.floatValue = duoSettings.getDuoCameraOffsetX()

                    DuoSettings.KEY_DUO_CAMERA_OFFSET_Y ->
                        duoCameraOffsetY.floatValue = duoSettings.getDuoCameraOffsetY()

                    DuoSettings.KEY_DUO_CAMERA_SIZE ->
                        duoCameraSize.floatValue = duoSettings.getDuoCameraSize()

                    DuoSettings.KEY_DUO_ARC_THICKNESS ->
                        duoArcThickness.floatValue = duoSettings.getDuoArcThickness()

                    DuoSettings.KEY_DUO_DOT_SIZE ->
                        duoDotSize.floatValue = duoSettings.getDuoDotSize()

                    DuoSettings.KEY_DUO_RING_RADIUS ->
                        duoRingRadius.floatValue = duoSettings.getDuoRingRadius()

                    DuoSettings.KEY_DUO_TIME_TEXT_SCALE ->
                        duoTimeTextScale.floatValue = duoSettings.getDuoTimeTextScale()

                    DuoSettings.KEY_DUO_SHOW_BATTERY ->
                        isDuoShowBattery.value = duoSettings.isDuoShowBatteryEnabled()

                    DuoSettings.KEY_DUO_SHOW_BATTERY_PERCENTAGE ->
                        isDuoShowBatteryPercentage.value = duoSettings.isDuoShowBatteryPercentageEnabled()

                    DuoSettings.KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED ->
                        isDuoBatteryPercentageOnlyColored.value = duoSettings.isDuoBatteryPercentageOnlyColoredEnabled()

                    DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED ->
                        isDuoBatteryChargingColorEnabled.value = duoSettings.isDuoBatteryChargingColorEnabled()

                    DuoSettings.KEY_DUO_BATTERY_CHARGING_COLOR ->
                        duoBatteryChargingColor.value = duoSettings.getDuoBatteryChargingColor()

                    DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED ->
                        isDuoBatteryPowerSaveColorEnabled.value = duoSettings.isDuoBatteryPowerSaveColorEnabled()

                    DuoSettings.KEY_DUO_BATTERY_POWER_SAVE_COLOR ->
                        duoBatteryPowerSaveColor.value = duoSettings.getDuoBatteryPowerSaveColor()

                    DuoSettings.KEY_DUO_BATTERY_LOW_COLOR_ENABLED ->
                        isDuoBatteryLowColorEnabled.value = duoSettings.isDuoBatteryLowColorEnabled()

                    DuoSettings.KEY_DUO_BATTERY_LOW_COLOR ->
                        duoBatteryLowColor.value = duoSettings.getDuoBatteryLowColor()

                    SettingsRepository.KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED ->
                        isIslandBatteryIdleColorEnabled.value = settingsRepository.isIslandBatteryIdleColorEnabled()

                    SettingsRepository.KEY_ISLAND_BATTERY_IDLE_COLOR ->
                        islandBatteryIdleColor.value = settingsRepository.getIslandBatteryIdleColor()

                    DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED ->
                        isDuoBatteryCriticalColorEnabled.value = duoSettings.isDuoBatteryCriticalColorEnabled()

                    DuoSettings.KEY_DUO_BATTERY_CRITICAL_COLOR ->
                        duoBatteryCriticalColor.value = duoSettings.getDuoBatteryCriticalColor()

                    DuoSettings.KEY_DUO_BATTERY_LOW_LEVEL ->
                        duoBatteryLowLevel.intValue = duoSettings.getDuoBatteryLowLevel()

                    DuoSettings.KEY_DUO_BATTERY_CRITICAL_LEVEL ->
                        duoBatteryCriticalLevel.intValue = duoSettings.getDuoBatteryCriticalLevel()

                    DuoSettings.KEY_DUO_SHOW_NETWORKS ->
                        isDuoShowNetworks.value = duoSettings.isDuoShowNetworksEnabled()

                    DuoSettings.KEY_DUO_DIFFERENTIATE_WIFI ->
                        isDuoDifferentiateWifi.value = duoSettings.isDuoDifferentiateWifiEnabled()

                    DuoSettings.KEY_DUO_SHOW_TIME ->
                        isDuoShowTime.value = duoSettings.isDuoShowTimeEnabled()

                    DuoSettings.KEY_DUO_SHOW_MEDIA ->
                        isDuoShowMedia.value = duoSettings.isDuoShowMediaEnabled()

                    DuoSettings.KEY_DUO_ROTATE_ALBUM_ART ->
                        isDuoRotateAlbumArt.value = duoSettings.isDuoRotateAlbumArtEnabled()

                    DuoSettings.KEY_DUO_SHOW_PROGRESS ->
                        isDuoShowProgress.value = duoSettings.isDuoShowProgressEnabled()

                    DuoSettings.KEY_DUO_SHOW_FLASHLIGHT ->
                        isDuoShowFlashlight.value = duoSettings.isDuoShowFlashlightEnabled()

                    DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF ->
                        isDuoHideWhenScreenOff.value = duoSettings.isDuoHideWhenScreenOffEnabled()

                    DuoSettings.KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE ->
                        isDuoHideWhenScreenOffOnlyIdle.value = duoSettings.isDuoHideWhenScreenOffOnlyIdleEnabled()

                    DuoSettings.KEY_DUO_USE_MATERIAL_YOU ->
                        isDuoUseMaterialYou.value = duoSettings.isDuoUseMaterialYouEnabled()

                    DuoSettings.KEY_DUO_CUSTOM_COLOR ->
                        duoCustomColor.value = duoSettings.getDuoCustomColor()

                    DuoSettings.KEY_DUO_TAP_ACTION ->
                        duoTapAction.value = duoSettings.getDuoTapAction()

                    DuoSettings.KEY_DUO_DOUBLE_TAP_ACTION ->
                        duoDoubleTapAction.value = duoSettings.getDuoDoubleTapAction()

                    DuoSettings.KEY_DUO_LONG_PRESS_ACTION ->
                        duoLongPressAction.value = duoSettings.getDuoLongPressAction()

                    DuoSettings.KEY_DUO_SWIPE_DOWN_ACTION ->
                        duoSwipeDownAction.value = duoSettings.getDuoSwipeDownAction()

                    DuoSettings.KEY_DUO_SLIDE_MODE ->
                        duoSlideMode.value = duoSettings.getDuoSlideMode()

                    DuoSettings.KEY_DUO_SLIDE_TRACK ->
                        isDuoSlideTrack.value = duoSettings.isDuoSlideTrackEnabled()

                    DuoSettings.KEY_DUO_SLIDE_INVERT_DIRECTION ->
                        isDuoSlideInvertDirection.value = duoSettings.isDuoSlideInvertDirectionEnabled()

                    SettingsRepository.KEY_ISLAND_ENABLED ->
                        isIslandEnabled.value = settingsRepository.isIslandEnabled()

                    SettingsRepository.KEY_ISLAND_USE_AUTO_DETECT ->
                        isIslandAutoDetect.value = settingsRepository.isIslandAutoDetectEnabled()

                    SettingsRepository.KEY_ISLAND_CAMERA_OFFSET_X ->
                        islandCameraOffsetX.floatValue = settingsRepository.getIslandCameraOffsetX()

                    SettingsRepository.KEY_ISLAND_CAMERA_OFFSET_Y ->
                        islandCameraOffsetY.floatValue = settingsRepository.getIslandCameraOffsetY()

                    SettingsRepository.KEY_ISLAND_CAMERA_SIZE ->
                        islandCameraSize.floatValue = settingsRepository.getIslandCameraSize()

                    SettingsRepository.KEY_ISLAND_MAX_WIDTH ->
                        islandMaxWidth.floatValue = settingsRepository.getIslandMaxWidth()

                    SettingsRepository.KEY_ISLAND_CUTOUT_GAP ->
                        islandCutoutGap.floatValue = settingsRepository.getIslandCutoutGap()

                    SettingsRepository.KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP ->
                        isIslandSuppressSystemHeadsUp.value = settingsRepository.isIslandSuppressSystemHeadsUpEnabled()

                    SettingsRepository.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR ->
                        isIslandDynamicHideStatusBar.value =
                            settingsRepository.getBoolean(
                                SettingsRepository.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
                                false,
                            )

                    SettingsRepository.KEY_ISLAND_HIDE_WHEN_SCREEN_OFF ->
                        isIslandHideWhenScreenOff.value = settingsRepository.isIslandHideWhenScreenOffEnabled()

                    SettingsRepository.KEY_ISLAND_TIMEOUT_MS ->
                        islandTimeoutMs.longValue = settingsRepository.getIslandTimeoutMs()

                    SettingsRepository.KEY_ISLAND_LINE_STAGE_ENABLED ->
                        isIslandLineStageEnabled.value = settingsRepository.isIslandLineStageEnabled()

                    SettingsRepository.KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE ->
                        isIslandMediaPeekSongChange.value = settingsRepository.isIslandMediaPeekSongChangeEnabled()

                    SettingsRepository.KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED ->
                        isIslandMediaKeepWhenPaused.value = settingsRepository.isIslandMediaKeepWhenPausedEnabled()

                    SettingsRepository.KEY_ISLAND_MEDIA_SHOW_PREVIOUS ->
                        isIslandMediaShowPrevious.value = settingsRepository.isIslandMediaShowPreviousEnabled()

                    SettingsRepository.KEY_ISLAND_MEDIA_SHOW_LIKE ->
                        isIslandMediaShowLike.value = settingsRepository.isIslandMediaShowLikeEnabled()

                    SettingsRepository.KEY_ISLAND_NOTIF_PEEK_STYLE ->
                        islandNotifPeekStyle.value = settingsRepository.getIslandNotifPeekStyle()

                    SettingsRepository.KEY_ISLAND_NOTIF_QUEUE ->
                        isIslandNotifQueue.value = settingsRepository.isIslandNotifQueueEnabled()

                    SettingsRepository.KEY_ISLAND_NOTIF_QUEUE_STYLE ->
                        islandNotifQueueStyle.value = settingsRepository.getIslandNotifQueueStyle()

                    SettingsRepository.KEY_ISLAND_NOTIF_QUEUE_INLINE ->
                        isIslandNotifQueueInline.value = settingsRepository.isIslandNotifQueueInlineEnabled()

                    SettingsRepository.KEY_ISLAND_NOTIF_SKIP_SILENT ->
                        isIslandNotifSkipSilent.value = settingsRepository.isIslandNotifSkipSilentEnabled()

                    SettingsRepository.KEY_ISLAND_NOTIF_FOLLOW_DND ->
                        isIslandNotifFollowDnd.value = settingsRepository.isIslandNotifFollowDndEnabled()

                    SettingsRepository.KEY_ISLAND_NOTIF_FILTER_APPS ->
                        isIslandNotifFilterApps.value = settingsRepository.isIslandNotifFilterAppsEnabled()

                    SettingsRepository.KEY_ISLAND_SHOW_NOTIFICATIONS ->
                        isIslandShowNotifications.value = settingsRepository.isIslandShowNotificationsEnabled()

                    SettingsRepository.KEY_ISLAND_NOTIF_HUB -> syncIslandNotifHubState()

                    SettingsRepository.KEY_ISLAND_NOTIF_HUB_SILENT ->
                        isIslandNotifHubIncludeSilent.value = settingsRepository.isIslandNotifHubIncludeSilent()

                    SettingsRepository.KEY_ISLAND_NOTIF_HUB_GROUP ->
                        isIslandNotifHubGroupByApps.value = settingsRepository.isIslandNotifHubGroupByApps()

                    SettingsRepository.KEY_ISLAND_NOTIF_HUB_ORDER ->
                        islandNotifHubOrder.value = settingsRepository.getIslandNotifHubOrder()

                    SettingsRepository.KEY_ISLAND_NOTIF_HUB_ICONS ->
                        islandNotifHubIcons.intValue = settingsRepository.getIslandNotifHubIcons()

                    SettingsRepository.KEY_ISLAND_NOTIF_TAP_TO_OPEN ->
                        isIslandNotifTapToOpen.value = settingsRepository.isIslandNotifTapToOpenEnabled()

                    SettingsRepository.KEY_ISLAND_SHOW_NAVIGATION ->
                        isIslandShowNavigation.value = settingsRepository.isIslandShowNavigationEnabled()

                    SettingsRepository.KEY_ISLAND_NAVIGATION_ON_NEW ->
                        islandNavigationOnNew.value = settingsRepository.getIslandNavigationOnNew()

                    SettingsRepository.KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE ->
                        islandNavigationExpandDistance.intValue = settingsRepository.getIslandNavigationExpandDistance()

                    SettingsRepository.KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND ->
                        isIslandNavigationPeekAfterExpand.value = settingsRepository.isIslandNavigationPeekAfterExpandEnabled()

                    SettingsRepository.KEY_ISLAND_CATCH_UP_ENABLED ->
                        isIslandCatchUpEnabled.value = settingsRepository.isIslandCatchUpEnabled()

                    SettingsRepository.KEY_ISLAND_CATCH_UP_TIMEOUT_MS ->
                        islandCatchUpTimeoutMs.longValue = settingsRepository.getIslandCatchUpStoredMs()

                    SettingsRepository.KEY_ISLAND_OTP_ENABLED ->
                        isIslandOtpEnabled.value = settingsRepository.isIslandOtpEnabled()

                    SettingsRepository.KEY_ISLAND_OTP_TIMEOUT_MS ->
                        islandOtpTimeoutMs.longValue = settingsRepository.getIslandOtpTimeoutMs()

                    SettingsRepository.KEY_STATUS_GLANCE_ENABLED ->
                        isStatusGlanceEnabled.value = settingsRepository.isStatusGlanceEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_USE_AUTO_DETECT ->
                        isStatusGlanceAutoDetect.value = settingsRepository.isStatusGlanceAutoDetectEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_OFFSET_X ->
                        statusGlanceOffsetX.floatValue = settingsRepository.getStatusGlanceOffsetX()

                    SettingsRepository.KEY_STATUS_GLANCE_OFFSET_Y ->
                        statusGlanceOffsetY.floatValue = settingsRepository.getStatusGlanceOffsetY()

                    SettingsRepository.KEY_STATUS_GLANCE_MAX_WIDTH ->
                        statusGlanceMaxWidth.floatValue = settingsRepository.getStatusGlanceMaxWidth()

                    SettingsRepository.KEY_STATUS_GLANCE_FONT_SIZE ->
                        statusGlanceFontSize.floatValue = settingsRepository.getStatusGlanceFontSize()

                    SettingsRepository.KEY_STATUS_GLANCE_SHOW_FLASHLIGHT ->
                        isStatusGlanceShowFlashlight.value = settingsRepository.isStatusGlanceShowFlashlightEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_SHOW_CALENDAR ->
                        isStatusGlanceShowCalendar.value = settingsRepository.isStatusGlanceShowCalendarEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME ->
                        statusGlanceCalendarTimeframe.value = settingsRepository.getStatusGlanceCalendarTimeframe()

                    SettingsRepository.KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY ->
                        statusGlanceCalendarShowAllDay.value = settingsRepository.isStatusGlanceCalendarShowAllDayEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS ->
                        statusGlanceSelectedCalendarIds.value = settingsRepository.getStatusGlanceCalendarSelectedCalendars()

                    SettingsRepository.KEY_STATUS_GLANCE_SHOW_MEDIA ->
                        isStatusGlanceShowMedia.value = settingsRepository.isStatusGlanceShowMediaEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_SHOW_TIME ->
                        isStatusGlanceShowTime.value = settingsRepository.isStatusGlanceShowTimeEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_BACKGROUND_PILL ->
                        isStatusGlanceBackgroundPill.value = settingsRepository.isStatusGlanceBackgroundPillEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_ALBUM_ART_COLORS ->
                        isStatusGlanceAlbumArtColors.value = settingsRepository.isStatusGlanceAlbumArtColorsEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN ->
                        isStatusGlanceHideWhenFullscreen.value = settingsRepository.isStatusGlanceHideWhenFullscreenEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS ->
                        isStatusGlanceHideInQuickSettings.value = settingsRepository.isStatusGlanceHideInQuickSettingsEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED ->
                        isStatusGlanceHideWhenLocked.value = settingsRepository.isStatusGlanceHideWhenLockedEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_BOLD_TEXT ->
                        isStatusGlanceBoldText.value = settingsRepository.isStatusGlanceBoldTextEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_SHOW_BATTERY ->
                        isStatusGlanceShowBattery.value = settingsRepository.isStatusGlanceShowBatteryEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE ->
                        statusGlanceBatteryDisplayMode.value = settingsRepository.getStatusGlanceBatteryDisplayMode()

                    SettingsRepository.KEY_STATUS_GLANCE_BATTERY_SHOW_LOW ->
                        isStatusGlanceBatteryShowLow.value = settingsRepository.isStatusGlanceBatteryShowLowEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING ->
                        isStatusGlanceBatteryShowCharging.value = settingsRepository.isStatusGlanceBatteryShowChargingEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_BATTERY_SHOW_FULL ->
                        isStatusGlanceBatteryShowFull.value = settingsRepository.isStatusGlanceBatteryShowFullEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE ->
                        isStatusGlanceBatteryShowOtherwise.value = settingsRepository.isStatusGlanceBatteryShowOtherwiseEnabled()

                    SettingsRepository.KEY_STATUS_GLANCE_BATTERY_ICON_SIZE ->
                        statusGlanceBatteryIconSize.floatValue = settingsRepository.getStatusGlanceBatteryIconSize()

                    SettingsRepository.KEY_STATUS_GLANCE_TAP_ACTION ->
                        statusGlanceTapAction.value = settingsRepository.getStatusGlanceTapAction()

                    SettingsRepository.KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION ->
                        statusGlanceDoubleTapAction.value = settingsRepository.getStatusGlanceDoubleTapAction()

                    SettingsRepository.KEY_STATUS_GLANCE_LONG_PRESS_ACTION ->
                        statusGlanceLongPressAction.value = settingsRepository.getStatusGlanceLongPressAction()

                    SecuritySettings.KEY_SCREEN_LOCKED_SECURITY_ENABLED,
                    SecuritySettings.KEY_SCREEN_LOCKED_DISABLE_ON_EXTENDED_UNLOCK,
                    -> security.onPreferenceChanged(key)

                    PowerSettings.KEY_MAPS_POWER_SAVING_ENABLED -> power.onPreferenceChanged(key)

                    SettingsRepository.KEY_BUTTON_REMAP_ENABLED ->
                        isButtonRemapEnabled.value =
                            settingsRepository.getBoolean(key)

                    SettingsRepository.KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE ->
                        lockscreenSystemShortcutsState.value =
                            settingsRepository.getLockscreenSystemShortcutsState()

                    AppsSettings.KEY_APP_LOCK_ENABLED -> appLock.onPreferenceChanged(key)

                    SettingsRepository.KEY_USE_USAGE_ACCESS -> {
                        isUseUsageAccess.value = settingsRepository.getBoolean(key)
                        appContext?.let { updateAppDetectionService(it) }
                    }

                    in FreezeState.PREFERENCE_KEYS -> freeze.onPreferenceChanged(key)

                    SettingsRepository.KEY_USE_ROOT, SettingsRepository.KEY_PRIVILEGED_MODE -> refreshPrivilegedMode()

                    SettingsRepository.KEY_DEVELOPER_MODE_ENABLED -> {
                        isDeveloperModeEnabled.value = settingsRepository.getBoolean(key)
                    }

                    SettingsRepository.KEY_PITCH_BLACK_THEME_ENABLED ->
                        isPitchBlackThemeEnabled.value =
                            settingsRepository.getBoolean(key)

                    SettingsRepository.KEY_ENABLE_UNSUPPORTED_FEATURES -> {
                        isEnableUnsupportedFeatures.value =
                            settingsRepository.isEnableUnsupportedFeatures()
                        if (searchQuery.value.isNotBlank()) {
                            appContext?.let { onSearchQueryChanged(searchQuery.value, it) }
                        }
                    }

                    SettingsRepository.KEY_SECURE_SENSITIVE_TILES -> {
                        isSecureSensitiveTilesEnabled.value =
                            settingsRepository.isSecureSensitiveTilesEnabled()
                    }

                    in InputState.PREFERENCE_KEYS -> input.onPreferenceChanged(key)

                    PowerSettings.KEY_BATTERY_WIDGET_MAX_DEVICES -> battery.onPreferenceChanged(key)

                    in NotificationState.PREFERENCE_KEYS -> notifications.onPreferenceChanged(key)
                    in LightingState.PREFERENCE_KEYS -> lighting.onPreferenceChanged(key)
                    in FlashlightPulseState.PREFERENCE_KEYS -> flashlightPulse.onPreferenceChanged(key)
                    in SmartPixelsState.PREFERENCE_KEYS -> smartPixels.onPreferenceChanged(key)
                    in RefreshRatePanelState.PREFERENCE_KEYS -> refreshRate.onPreferenceChanged(key)
                    in AodState.PREFERENCE_KEYS -> aod.onPreferenceChanged(key)
                    in ToolkitOnDisplayState.PREFERENCE_KEYS -> toolkitOnDisplay.onPreferenceChanged(key)
                    in StatusBarControlState.PREFERENCE_KEYS -> statusBarControl.onPreferenceChanged(key)
                    in NightLightState.PREFERENCE_KEYS -> nightLight.onPreferenceChanged(key)

                    PowerSettings.KEY_MAPS_DISCOVERED_CHANNELS, PowerSettings.KEY_MAPS_DETECTION_CHANNELS -> power.onPreferenceChanged(key)

                    SettingsRepository.KEY_PINNED_FEATURES -> {
                        pinnedFeatureKeys.value = settingsRepository.getPinnedFeatures()
                    }

                    SoundSettings.KEY_CALL_VIBRATIONS_ENABLED -> sound.onPreferenceChanged(key)

                    SettingsRepository.KEY_LIKE_SONG_TOAST_ENABLED -> {
                        isLikeSongToastEnabled.value = settingsRepository.getBoolean(key)
                    }

                    SettingsRepository.KEY_LIKE_SONG_AOD_OVERLAY_ENABLED -> {
                        isLikeSongAodOverlayEnabled.value = settingsRepository.getBoolean(key)
                    }

                    SettingsRepository.KEY_ONBOARDING_COMPLETED -> {
                        isOnboardingCompleted.value = settingsRepository.getBoolean(key, false)
                    }

                    AppsSettings.KEY_TRACKED_REPOS -> {
                        appContext?.let { refreshTrackedUpdates(it) }
                    }

                    SettingsRepository.KEY_FONT_SCALE ->
                        fontScale.floatValue =
                            settingsRepository.getFontScale()

                    SettingsRepository.KEY_FONT_WEIGHT ->
                        fontWeight.intValue =
                            settingsRepository.getFontWeight()

                    SettingsRepository.KEY_ANIMATOR_DURATION_SCALE ->
                        animatorDurationScale.floatValue =
                            settingsRepository.getAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE)

                    SettingsRepository.KEY_TRANSITION_ANIMATION_SCALE ->
                        transitionAnimationScale.floatValue =
                            settingsRepository.getAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE)

                    SettingsRepository.KEY_WINDOW_ANIMATION_SCALE ->
                        windowAnimationScale.floatValue =
                            settingsRepository.getAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE)

                    SettingsRepository.KEY_SMALLEST_WIDTH ->
                        smallestWidth.intValue =
                            settingsRepository.getSmallestWidth()

                    SecuritySettings.KEY_FACE_UNLOCK_BRIGHTNESS_ENABLED,
                    SecuritySettings.KEY_FACE_UNLOCK_MAX_BRIGHTNESS,
                    SecuritySettings.KEY_FACE_UNLOCK_AMBIENT_THRESHOLD,
                    SecuritySettings.KEY_FACE_UNLOCK_TRIGGER_UNLOCK,
                    SecuritySettings.KEY_FACE_UNLOCK_AUTO_ILLUMINATE,
                    SecuritySettings.KEY_FACE_UNLOCK_LIGHT_TINT,
                    SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_ENABLED,
                    SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_DELAY,
                    SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_ENABLED,
                    SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_DAYS,
                    SecuritySettings.KEY_SCREENSHOTS_CLEANED_COUNT,
                    SecuritySettings.KEY_SCREENSHOTS_CLEANED_BYTES,
                    -> security.onPreferenceChanged(key)

                    SettingsRepository.KEY_AUTO_ACCESSIBILITY_ENABLED ->
                        isAutoAccessibilityEnabled.value =
                            settingsRepository.getBoolean(key)

                    SettingsRepository.KEY_USE_BLUR -> {
                        appContext?.let { updateBlurState(it) }
                    }

                    NotificationSettings.KEY_USE_RIPPLE -> {
                        appContext?.let { updateRippleState(it) }
                    }

                    SettingsRepository.KEY_MOTION_BLUR -> {
                        appContext?.let { updateMotionBlurState(it) }
                    }

                    SettingsRepository.KEY_SHAKE_FOR_HELP -> {
                        isShakeForHelpEnabled.value = settingsRepository.isShakeForHelpEnabled()
                    }

                    ConnectivitySettings.KEY_PRIVATE_DNS_PRESETS,
                    ConnectivitySettings.KEY_NETWORK_DOWNLOAD_RATE_LIMIT,
                    ConnectivitySettings.KEY_MOBILE_DATA_ALWAYS_ON,
                    ConnectivitySettings.KEY_WIRELESS_DISPLAY_CERTIFICATION,
                    -> connectivity.onPreferenceChanged(key, appContext)

                    SettingsRepository.KEY_FLASHLIGHT_POCKET_TURN_OFF_ENABLED -> {
                        isFlashlightPocketTurnOffEnabled.value = settingsRepository.getBoolean(key)
                    }

                    SettingsRepository.KEY_FLASHLIGHT_OVERHEAT_PREVENTION_ENABLED -> {
                        isFlashlightOverheatEnabled.value = settingsRepository.getBoolean(key, true)
                    }

                    SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_ENABLED -> {
                        isCircleToSearchGestureEnabled.value = settingsRepository.getBoolean(key)
                    }

                    SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_HEIGHT -> {
                        circleToSearchGestureHeight.floatValue =
                            settingsRepository.getFloat(key, 48f)
                    }

                    SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_WIDTH -> {
                        circleToSearchGestureWidth.floatValue =
                            settingsRepository.getFloat(key, 240f)
                    }

                    SettingsRepository.KEY_CIRCLE_TO_SEARCH_PREVIEW_ENABLED -> {
                        isCircleToSearchPreviewEnabled.value = settingsRepository.getBoolean(key)
                    }

                    SettingsRepository.KEY_HIDE_GESTURE_BAR_ON_LAUNCHER_ENABLED -> {
                        isHideGestureBarOnLauncherEnabled.value = settingsRepository.getBoolean(key)
                        appContext?.let { updateAppDetectionService(it) }
                    }

                    SettingsRepository.KEY_SHUT_UP_ATTEMPT_SHIZUKU_RESTART -> {
                        isShutUpAttemptShizukuRestart.value =
                            settingsRepository.isShutUpAttemptShizukuRestartEnabled()
                    }

                    SettingsRepository.KEY_SHUT_UP_RESTORE_DELAY -> {
                        shutUpRestoreDelay.intValue =
                            settingsRepository.getShutUpRestoreDelay()
                    }

                    SettingsRepository.KEY_SHUT_UP_KEYBOARD -> {
                        shutUpKeyboard.value = settingsRepository.getShutUpKeyboard()
                    }

                    SettingsRepository.KEY_SHUT_UP_RESTORE_MODE -> {
                        shutUpRestoreMode.value =
                            settingsRepository.getShutUpRestoreMode()
                    }

                    SettingsRepository.KEY_SHIZUKU_AUTH_TOKEN -> {
                        shizukuAuthToken.value =
                            settingsRepository.getShizukuAuthToken()
                    }

                    SettingsRepository.KEY_DISABLE_ROTATION_SUGGESTION -> {
                        isDisableRotationSuggestionEnabled.value =
                            settingsRepository.getBoolean(key)
                        appContext?.let {
                            applyDisableRotationSuggestion(
                                it,
                                isDisableRotationSuggestionEnabled.value,
                            )
                        }
                    }

                    SettingsRepository.KEY_ALLOW_OVERLAYS_IN_SETTINGS -> {
                        isAllowOverlaysInSettingsEnabled.value =
                            settingsRepository.getBoolean(key)
                        appContext?.let {
                            applyAllowOverlaysInSettings(
                                it,
                                isAllowOverlaysInSettingsEnabled.value,
                            )
                        }
                    }

                    SettingsRepository.KEY_TRANSPARENT_NAVIGATION_BAR -> {
                        isTransparentNavigationBarEnabled.value =
                            settingsRepository.getBoolean(key)
                        appContext?.let {
                            applyTransparentNavigationBar(
                                it,
                                isTransparentNavigationBarEnabled.value,
                            )
                        }
                    }

                    SettingsRepository.KEY_PREFER_GPU_COMPOSING -> {
                        isPreferGpuComposingEnabled.value =
                            settingsRepository.getBoolean(key)
                        appContext?.let {
                            applyPreferGpuComposing(
                                it,
                                isPreferGpuComposingEnabled.value,
                            )
                        }
                    }

                    SettingsRepository.KEY_PIXEL_SEARCHBAR -> {
                        isPixelSearchbarEnabled.value =
                            settingsRepository.getBoolean(key)
                        appContext?.let {
                            applyPixelSearchbarSetting(
                                it,
                                isPixelSearchbarEnabled.value,
                            )
                        }
                    }
                }
            }
        }

    /**
     * Applies and updates the application-wide display language locale.
     *
     * @param languageCode [String] BCP-47 language tag (e.g., "en", "si", "de").
     */
    fun setAppLanguage(languageCode: String) {
        appLanguage.value = languageCode
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Loads saved ShutUp per-app media ducking and mute configurations from persistent storage.
     */
    fun loadShutUpConfigs() {
        shutUpConfigs.value = settingsRepository.loadShutUpConfigs()
    }

    /**
     * Updates ducking or mute configuration for a specific target package.
     *
     * @param config [com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig] The updated ShutUpAppConfig object to store.
     */
    fun updateShutUpConfig(config: com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig) {
        settingsRepository.updateShutUpConfig(config)
        loadShutUpConfigs()
    }

    /**
     * Executes the remove shut up config operation.
     *
     * @param packageName [String] Target package name.
     */
    fun removeShutUpConfig(packageName: String) {
        val current = shutUpConfigs.value.toMutableList()
        current.removeAll { it.packageName == packageName }
        settingsRepository.saveShutUpConfigs(current)
        loadShutUpConfigs()
    }

    /**
     * Executes the set shut up attempt shizuku restart enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setShutUpAttemptShizukuRestartEnabled(enabled: Boolean) {
        isShutUpAttemptShizukuRestart.value = enabled
        settingsRepository.setShutUpAttemptShizukuRestartEnabled(enabled)
    }

    /**
     * Executes the set shut up restore delay operation.
     *
     * @param delaySeconds [Int] Target delay seconds.
     */
    fun setShutUpRestoreDelay(delaySeconds: Int) {
        shutUpRestoreDelay.intValue = delaySeconds
        settingsRepository.setShutUpRestoreDelay(delaySeconds)
    }

    /**
     * Executes the set shut up restore mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setShutUpKeyboard(ime: String) {
        shutUpKeyboard.value = ime
        settingsRepository.setShutUpKeyboard(ime)
    }

    fun setShutUpRestoreMode(mode: String) {
        shutUpRestoreMode.value = mode
        settingsRepository.setShutUpRestoreMode(mode)
    }

    /**
     * Executes the set shizuku auth token operation.
     *
     * @param token [String] Target token.
     */
    fun setShizukuAuthToken(token: String) {
        shizukuAuthToken.value = token
        settingsRepository.setShizukuAuthToken(token)
    }

    /**
     * Executes the save shut up selected apps operation.
     *
     * @param context [Context] Target context.
     * @param apps [List<AppSelection>] Target apps.
     */
    fun saveShutUpSelectedApps(
        context: Context,
        apps: List<AppSelection>,
    ) {
        val currentConfigs = settingsRepository.loadShutUpConfigs().associateBy { it.packageName }
        val newConfigs =
            apps.filter { it.isEnabled }.map {
                currentConfigs[it.packageName] ?: com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig(
                    it.packageName,
                )
            }
        settingsRepository.saveShutUpConfigs(newConfigs)
        loadShutUpConfigs()
    }

    fun createShutUpShortcut(
        context: Context,
        config: com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig,
    ) {
        val appName =
            try {
                val appInfo = context.packageManager.getApplicationInfo(config.packageName, 0)
                context.packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                config.packageName
            }

        val intent =
            Intent(context, com.jawzf.pixeltoolkit.ShutUpShortcutActivity::class.java).apply {
                action = Intent.ACTION_MAIN
                putExtra("package_name", config.packageName)
                data = Uri.parse("shutup://${config.packageName}")
            }

        if (androidx.core.content.pm.ShortcutManagerCompat
                .isRequestPinShortcutSupported(context)
        ) {
            val appIcon = AppUtil.getShortcutIcon(context, config.packageName)

            val pinShortcutInfo =
                androidx.core.content.pm.ShortcutInfoCompat
                    .Builder(context, config.packageName)
                    .setShortLabel(appName)
                    .setIcon(
                        androidx.core.graphics.drawable.IconCompat
                            .createWithBitmap(appIcon),
                    ).setIntent(intent)
                    .build()

            androidx.core.content.pm.ShortcutManagerCompat.requestPinShortcut(
                context,
                pinShortcutInfo,
                null,
            )
            Toast
                .makeText(
                    context,
                    context.getString(R.string.shut_up_shortcut_created, appName),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    /**
     * Executes the check operation.
     *
     * @param context [Context] Target context.
     */
    fun check(context: Context) {
        appContext = context.applicationContext
        settingsRepository = SettingsRepository(context)
        duoSettings = DuoSettings(context)
        NotificationAutoDismisser.isSnoozedByUs = { LiveUpdateSnoozer.isSnoozedByUs(it) }
        notifications.load(context)
        lighting.load(context)
        flashlightPulse.load(context)
        gitHubUser.value = AppsSettings(context).getGitHubUser()

        // Sync with system per-app language settings
        val currentLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentLocales.isEmpty) {
            val locale = currentLocales.get(0)
            val langTag = locale?.toLanguageTag() ?: "en"
            appLanguage.value =
                when {
                    langTag.startsWith("pt-BR") -> "pt-BR"
                    langTag.startsWith("pt-PT") -> "pt-PT"
                    langTag.startsWith("pt") -> "pt-BR" // Fallback to Brazilian Portuguese as primary translated option
                    else -> locale?.language ?: "en"
                }
        } else {
            appLanguage.value = "en"
        }

        isAccessibilityEnabled.value = PermissionUtils.isAccessibilityServiceEnabled(context)
        isWriteSecureSettingsEnabled.value = PermissionUtils.canWriteSecureSettings(context)
        isBubblePermissionGranted.value = PermissionUtils.hasBubblePermission(context)
        isSensitiveNotificationsGranted.value = PermissionGrantUtil.hasSensitiveNotifications(context)
        isLocalNetworkGranted.value = PermissionUtils.hasLocalNetworkPermission(context)
        isShizukuAvailable.value = ShizukuUtils.isShizukuAvailable()
        isShizukuPermissionGranted.value = ShizukuUtils.hasPermission()
        isAutoAccessibilityEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_AUTO_ACCESSIBILITY_ENABLED)
        isHideGestureBarEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_HIDE_GESTURE_BAR_ENABLED, false)
        isCircleToSearchGestureEnabled.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_ENABLED,
                false,
            )
        circleToSearchGestureHeight.floatValue =
            settingsRepository.getFloat(SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_HEIGHT, 48f)
        circleToSearchGestureWidth.floatValue =
            settingsRepository.getFloat(SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_WIDTH, 240f)
        isCircleToSearchPreviewEnabled.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_CIRCLE_TO_SEARCH_PREVIEW_ENABLED,
                false,
            )
        isHideGestureBarOnLauncherEnabled.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_HIDE_GESTURE_BAR_ON_LAUNCHER_ENABLED,
                false,
            )

        isShutUpAttemptShizukuRestart.value =
            settingsRepository.isShutUpAttemptShizukuRestartEnabled()
        shutUpRestoreDelay.intValue =
            settingsRepository.getShutUpRestoreDelay()
        shutUpKeyboard.value = settingsRepository.getShutUpKeyboard()
        shutUpRestoreMode.value =
            settingsRepository.getShutUpRestoreMode()
        shizukuAuthToken.value =
            settingsRepository.getShizukuAuthToken()
        isDisableRotationSuggestionEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_DISABLE_ROTATION_SUGGESTION, false)
        isAllowOverlaysInSettingsEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_ALLOW_OVERLAYS_IN_SETTINGS, false)
        isTransparentNavigationBarEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_TRANSPARENT_NAVIGATION_BAR, false)
        isSplitScreenGestureEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_SPLIT_SCREEN_GESTURE, false)
        isPreferGpuComposingEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_PREFER_GPU_COMPOSING, false)
        isPixelSearchbarEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_PIXEL_SEARCHBAR, false)
        pixelSearchbarType.value =
            settingsRepository.getPixelSearchbarType()
        pixelSearchbarDateFormat.value =
            settingsRepository.getPixelSearchbarDateFormat()
        pixelSearchbarBackgroundPill.value =
            settingsRepository.getPixelSearchbarBackgroundPill()
        pixelSearchbarWidgetId.intValue =
            settingsRepository.getPixelSearchbarWidgetId()
        pixelSearchbarWidgetProvider.value =
            settingsRepository.getPixelSearchbarWidgetProvider()
        pixelSearchbarScrapedLine1.value =
            settingsRepository.getPixelSearchbarScrapedLine1()
        pixelSearchbarScrapedLine2.value =
            settingsRepository.getPixelSearchbarScrapedLine2()
        pixelSearchbarWidgetPaddingH.intValue =
            settingsRepository.getPixelSearchbarWidgetPaddingH()
        pixelSearchbarWidgetPaddingV.intValue =
            settingsRepository.getPixelSearchbarWidgetPaddingV()
        pixelSearchbarTapActionEnabled.value =
            settingsRepository.getPixelSearchbarTapActionEnabled()
        pixelSearchbarMusicTitle.value =
            settingsRepository.getPixelSearchbarMusicTitle()
        pixelSearchbarMusicArtist.value =
            settingsRepository.getPixelSearchbarMusicArtist()
        pixelSearchbarMusicPackage.value =
            settingsRepository.getPixelSearchbarMusicPackage()
        lockClock.load(context)
        statusBarControl.load(context)
        nightLight.load(context)
        loadShutUpConfigs()
        recentSearches.value = settingsRepository.getRecentSearches()

        if (isHideGestureBarEnabled.value) {
            applyHideGestureBar(context, true)
        }

        updateAppDetectionService(context)

        if (isAutoAccessibilityEnabled.value && !isAccessibilityEnabled.value) {
            val serviceName =
                "${context.packageName}/${ScreenOffAccessibilityService::class.java.name}"
            var success = false

            if (isWriteSecureSettingsEnabled.value) {
                try {
                    val enabledServices =
                        Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        ) ?: ""
                    val newServices =
                        if (enabledServices.isEmpty()) {
                            serviceName
                        } else if (!enabledServices.contains(
                                serviceName,
                            )
                        ) {
                            "$enabledServices:$serviceName"
                        } else {
                            enabledServices
                        }
                    Settings.Secure.putString(
                        context.contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        newServices,
                    )
                    Settings.Secure.putString(
                        context.contentResolver,
                        Settings.Secure.ACCESSIBILITY_ENABLED,
                        "1",
                    )
                    success = true
                } catch (e: Exception) {
                    success = false
                }
            }

            if (success) {
                isAccessibilityEnabled.value =
                    PermissionUtils.isAccessibilityServiceEnabled(context)
                if (isAccessibilityEnabled.value) {
                    Toast
                        .makeText(
                            context,
                            "Accessibility auto-granted",
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }

        isReadPhoneStateEnabled.value = PermissionUtils.hasReadPhoneStatePermission(context)
        isPostNotificationsEnabled.value = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        isNotificationListenerEnabled.value =
            PermissionUtils.hasNotificationListenerPermission(context)
        isOverlayPermissionGranted.value = PermissionUtils.canDrawOverlays(context)
        isNotificationLightingAccessibilityEnabled.value =
            PermissionUtils.isNotificationLightingAccessibilityServiceEnabled(context)
        isDefaultBrowserSet.value = PermissionUtils.isDefaultBrowser(context)
        isLocationPermissionGranted.value = PermissionUtils.hasLocationPermission(context)
        isBackgroundLocationPermissionGranted.value =
            PermissionUtils.hasBackgroundLocationPermission(context)
        isFullScreenIntentPermissionGranted.value = PermissionUtils.canUseFullScreenIntent(context)
        isKeyboardEnabled.value = PermissionUtils.isKeyboardEnabled(context)
        isKeyboardSelected.value = PermissionUtils.isKeyboardSelected(context)
        isWriteSettingsEnabled.value = PermissionUtils.canWriteSystemSettings(context)
        isNotificationPolicyAccessGranted.value =
            PermissionUtils.hasNotificationPolicyAccess(context)
        isCalendarPermissionGranted.value = PermissionUtils.hasReadCalendarPermission(context)
        isUsageStatsPermissionGranted.value = PermissionUtils.hasUsageStatsPermission(context)

        isBluetoothPermissionGranted.value = PermissionUtils.hasBluetoothPermission(context)
        isStoragePermissionGranted.value = PermissionUtils.hasStoragePermission(context)

        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.FONT_SCALE),
            false,
            contentObserver,
        )
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor("font_weight_adjustment"),
            false,
            contentObserver,
        )
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            contentObserver,
        )
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE),
            false,
            contentObserver,
        )
        context.contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.WINDOW_ANIMATION_SCALE),
            false,
            contentObserver,
        )
        context.contentResolver.registerContentObserver(
            Settings.Secure.getUriFor("display_density_forced"),
            false,
            contentObserver,
        )
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor("peak_refresh_rate"),
            false,
            contentObserver,
        )
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor("min_refresh_rate"),
            false,
            contentObserver,
        )

        try {
            context.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("doze_always_on"),
                false,
                contentObserver,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("sysui_qs_tiles"),
                false,
                contentObserver,
            )
        } catch (e: Exception) {
            // This might fail on Android 14+ for some system keys
            e.printStackTrace()
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.Global.getUriFor("battery_saver_constants"),
                false,
                contentObserver,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.Global.getUriFor("audio_safe_volume_state"),
                false,
                contentObserver,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.Global.getUriFor("low_power_trigger_level"),
                false,
                contentObserver,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor("show_notification_snooze"),
                false,
                contentObserver,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            context.contentResolver.registerContentObserver(
                Settings.Global.getUriFor("notification_snooze_options"),
                false,
                contentObserver,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        power.loadBatterySaverConstants(context)
        power.syncLowPowerTriggerLevel(context)

        isPowerSaveModeEnabled.value = DeviceUtils.isPowerSaveMode(context)
        updateBlurState(context)
        updateRippleState(context)
        updateMotionBlurState(context)
        updateAddedQSTiles(context)

        if (powerSaveReceiver == null) {
            powerSaveReceiver =
                object : BroadcastReceiver() {
                    override fun onReceive(
                        context: Context?,
                        intent: Intent?,
                    ) {
                        if (intent?.action == PowerManager.ACTION_POWER_SAVE_MODE_CHANGED) {
                            context?.let {
                                isPowerSaveModeEnabled.value = DeviceUtils.isPowerSaveMode(it)
                                updateBlurState(it)
                                updateRippleState(it)
                                updateMotionBlurState(it)
                            }
                        }
                    }
                }
            context.applicationContext.registerReceiver(
                powerSaveReceiver,
                IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED),
            )
        }

        settingsRepository.registerOnSharedPreferenceChangeListener(preferenceChangeListener)

        viewModelScope.launch {
            settingsRepository.gitHubToken.collect {
                gitHubToken.value = it
            }
        }

        isWidgetEnabled.value = settingsRepository.getBoolean(SettingsRepository.KEY_WIDGET_ENABLED)

        fontScale.floatValue = settingsRepository.getFontScale()
        fontWeight.intValue = settingsRepository.getFontWeight()
        animatorDurationScale.floatValue =
            settingsRepository.getAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE)
        transitionAnimationScale.floatValue =
            settingsRepository.getAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE)
        windowAnimationScale.floatValue =
            settingsRepository.getAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE)
        smallestWidth.intValue = settingsRepository.getSmallestWidth()
        refreshRate.load(context)
        aod.load(context)
        toolkitOnDisplay.load(context)
        hasShizukuPermission.value = ShizukuUtils.hasPermission() || RootUtils.isRootAvailable()

        isUseUsageAccess.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_USE_USAGE_ACCESS)
        isHiddenDebuggingSupport.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_HIDDEN_DEBUGGING_SUPPORT)
        isOnboardingCompleted.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_ONBOARDING_COMPLETED, false)

        refreshPrivilegedMode()

        if (isRootEnabled.value) {
            isRootAvailable.value = RootUtils.isRootAvailable()
            isRootPermissionGranted.value =
                RootUtils.isRootPermissionGranted()
        } else {
            isRootAvailable.value = false
            isRootPermissionGranted.value = false
        }

        hapticFeedbackType.value = settingsRepository.getHapticFeedbackType()
        defaultTab.value = settingsRepository.getDIYTab()
        selectedAppIcon.value = settingsRepository.getAppIcon()
        isSwipeTabsEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_SWIPE_TABS, true)
        isShakeForHelpEnabled.value = settingsRepository.isShakeForHelpEnabled()

        power.load(context)

        // Button Remap & Migration
        isButtonRemapEnabled.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_BUTTON_REMAP_ENABLED,
                settingsRepository.getBoolean(SettingsRepository.KEY_FLASHLIGHT_VOLUME_TOGGLE_ENABLED),
            )
        isButtonRemapUseShizuku.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_BUTTON_REMAP_USE_SHIZUKU)
        isButtonRemapPauseOnVolumeDialog.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_BUTTON_REMAP_PAUSE_ON_VOLUME_DIALOG, true)
        shizukuDetectedDevicePath.value =
            settingsRepository.getString(SettingsRepository.KEY_SHIZUKU_DETECTED_DEVICE_PATH)

        val oldTrigger =
            settingsRepository.getString(
                SettingsRepository.KEY_FLASHLIGHT_TRIGGER_BUTTON,
                "Volume Up",
            )

        val hasLegacyToggle =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_FLASHLIGHT_VOLUME_TOGGLE_ENABLED,
                false,
            ) // Default false here as key check logic

        RemapSlot.ALL.forEach { slot ->
            remapActions[slot] = settingsRepository.getRemapActions(slot.prefKey)
            if (slot.choicePrefKey != null) {
                powerChoices[slot] = settingsRepository.getPowerChoice(slot)
            }
        }
        isPowerCameraFlipEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_BUTTON_REMAP_POWER_CAMERA_FLIP, false)

        hilight.load(context)
        isLockscreenShortcutsEnabled.value = settingsRepository.isLockscreenShortcutsEnabled()
        sound.load(context)
        LockscreenShortcutSide.entries.forEach { side ->
            lockscreenShortcutActions[side] = settingsRepository.getRemapActions(side.prefKey)
        }
        lockscreenSystemShortcutsState.value = settingsRepository.getLockscreenSystemShortcutsState()

        val hapticName =
            settingsRepository.getString(
                SettingsRepository.KEY_BUTTON_REMAP_HAPTIC_TYPE,
                settingsRepository.getString(
                    SettingsRepository.KEY_FLASHLIGHT_HAPTIC_TYPE,
                    HapticFeedbackType.DOUBLE.name,
                ),
            )

        remapHapticType.value =
            try {
                val type = HapticFeedbackType.valueOf(hapticName ?: HapticFeedbackType.DOUBLE.name)
                if (type.name == "LONG") HapticFeedbackType.DOUBLE else type
            } catch (e: Exception) {
                HapticFeedbackType.DOUBLE
            }

        smartPixels.load(context)
        isDuoEnabled.value = duoSettings.isDuoEnabled()
        isDuoAutoDetect.value = duoSettings.isDuoAutoDetectEnabled()
        duoCameraOffsetX.floatValue = duoSettings.getDuoCameraOffsetX()
        duoCameraOffsetY.floatValue = duoSettings.getDuoCameraOffsetY()
        duoCameraSize.floatValue = duoSettings.getDuoCameraSize()
        duoArcThickness.floatValue = duoSettings.getDuoArcThickness()
        duoDotSize.floatValue = duoSettings.getDuoDotSize()
        duoRingRadius.floatValue = duoSettings.getDuoRingRadius()
        duoTimeTextScale.floatValue = duoSettings.getDuoTimeTextScale()
        isDuoShowBattery.value = duoSettings.isDuoShowBatteryEnabled()
        isDuoShowBatteryPercentage.value = duoSettings.isDuoShowBatteryPercentageEnabled()
        isDuoBatteryPercentageOnlyColored.value = duoSettings.isDuoBatteryPercentageOnlyColoredEnabled()
        isDuoBatteryChargingColorEnabled.value = duoSettings.isDuoBatteryChargingColorEnabled()
        duoBatteryChargingColor.value = duoSettings.getDuoBatteryChargingColor()
        isDuoBatteryPowerSaveColorEnabled.value = duoSettings.isDuoBatteryPowerSaveColorEnabled()
        duoBatteryPowerSaveColor.value = duoSettings.getDuoBatteryPowerSaveColor()
        isDuoBatteryLowColorEnabled.value = duoSettings.isDuoBatteryLowColorEnabled()
        duoBatteryLowColor.value = duoSettings.getDuoBatteryLowColor()
        isIslandBatteryIdleColorEnabled.value = settingsRepository.isIslandBatteryIdleColorEnabled()
        islandBatteryIdleColor.value = settingsRepository.getIslandBatteryIdleColor()
        isDuoBatteryCriticalColorEnabled.value = duoSettings.isDuoBatteryCriticalColorEnabled()
        duoBatteryCriticalColor.value = duoSettings.getDuoBatteryCriticalColor()
        duoBatteryLowLevel.intValue = duoSettings.getDuoBatteryLowLevel()
        duoBatteryCriticalLevel.intValue = duoSettings.getDuoBatteryCriticalLevel()
        isDuoShowNetworks.value = duoSettings.isDuoShowNetworksEnabled()
        isDuoDifferentiateWifi.value = duoSettings.isDuoDifferentiateWifiEnabled()
        isDuoShowTime.value = duoSettings.isDuoShowTimeEnabled()
        isDuoShowMedia.value = duoSettings.isDuoShowMediaEnabled()
        isDuoRotateAlbumArt.value = duoSettings.isDuoRotateAlbumArtEnabled()
        isDuoShowProgress.value = duoSettings.isDuoShowProgressEnabled()
        isDuoShowFlashlight.value = duoSettings.isDuoShowFlashlightEnabled()
        isDuoHideWhenScreenOff.value = duoSettings.isDuoHideWhenScreenOffEnabled()
        isDuoHideWhenScreenOffOnlyIdle.value = duoSettings.isDuoHideWhenScreenOffOnlyIdleEnabled()
        isDuoHideWhenLocked.value = duoSettings.isDuoHideWhenLockedEnabled()
        isDuoUseMaterialYou.value = duoSettings.isDuoUseMaterialYouEnabled()
        duoCustomColor.value = duoSettings.getDuoCustomColor()
        duoTapAction.value = duoSettings.getDuoTapAction()
        duoDoubleTapAction.value = duoSettings.getDuoDoubleTapAction()
        duoLongPressAction.value = duoSettings.getDuoLongPressAction()
        duoSwipeDownAction.value = duoSettings.getDuoSwipeDownAction()
        duoSlideMode.value = duoSettings.getDuoSlideMode()
        isDuoSlideTrack.value = duoSettings.isDuoSlideTrackEnabled()
        isDuoSlideInvertDirection.value = duoSettings.isDuoSlideInvertDirectionEnabled()
        isIslandEnabled.value = settingsRepository.isIslandEnabled()
        isIslandAutoDetect.value = settingsRepository.isIslandAutoDetectEnabled()
        islandCameraOffsetX.floatValue = settingsRepository.getIslandCameraOffsetX()
        islandCameraOffsetY.floatValue = settingsRepository.getIslandCameraOffsetY()
        islandCameraSize.floatValue = settingsRepository.getIslandCameraSize()
        islandMaxWidth.floatValue = settingsRepository.getIslandMaxWidth()
        islandCutoutGap.floatValue = settingsRepository.getIslandCutoutGap()
        islandExpandedWidth.floatValue = settingsRepository.getIslandExpandedWidth()
        islandExpandedRoundness.floatValue = settingsRepository.getIslandExpandedRoundness()
        islandFontScale.floatValue = settingsRepository.getIslandFontScale()
        isIslandHideInOwnerApp.value = settingsRepository.isIslandHideInOwnerAppEnabled()
        isIslandHideOnShade.value = settingsRepository.isIslandHideOnShadeEnabled()
        isIslandKeepOnLandscape.value = settingsRepository.isIslandKeepOnLandscapeEnabled()
        isIslandKeepInFullscreen.value = settingsRepository.isIslandKeepInFullscreenEnabled()
        isIslandDisableInteractionsInFullscreen.value = settingsRepository.isIslandDisableInteractionsInFullscreenEnabled()
        isIslandBondEdge.value = settingsRepository.isIslandBondEdgeEnabled()
        isIslandBalanceCompact.value = settingsRepository.isIslandBalanceCompactEnabled()
        isIslandSecondaryBubble.value = settingsRepository.isIslandSecondaryBubbleEnabled()
        islandMaxItems.value = settingsRepository.getIslandMaxItems()
        isIslandAlwaysGestures.value = settingsRepository.isIslandAlwaysGesturesEnabled()
        islandLandscapeTopSpacing.floatValue = settingsRepository.getIslandLandscapeTopSpacing()
        isIslandDismissOnOutside.value = settingsRepository.isIslandDismissOnOutsideEnabled()
        isIslandHideLiveUpdates.value = settingsRepository.isIslandHideLiveUpdatesEnabled()
        isIslandHideStatusBarInsets.value = settingsRepository.isIslandHideStatusBarInsetsEnabled()
        islandExpandedScale.floatValue = settingsRepository.getIslandExpandedScale()
        islandCameraPosition.value = settingsRepository.getIslandCameraPosition()
        isIslandShowCalls.value = settingsRepository.isIslandShowCallsEnabled()
        isIslandShowTimers.value = settingsRepository.isIslandShowTimersEnabled()
        isIslandTimersShowScreenRecorder.value = settingsRepository.isIslandTimersShowScreenRecorderEnabled()
        isIslandTimersFilterApps.value = settingsRepository.isIslandTimersFilterAppsEnabled()
        isIslandShowNetwork.value = settingsRepository.isIslandShowNetworkEnabled()
        isIslandShowSignal.value = settingsRepository.isIslandShowSignalEnabled()
        isIslandShowSoundMode.value = settingsRepository.isIslandShowSoundModeEnabled()
        isIslandSoundModeKeepIcon.value = settingsRepository.isIslandSoundModeKeepIconEnabled()
        isIslandShowAlarm.value = settingsRepository.isIslandShowAlarmEnabled()
        islandAlarmWindowHours.intValue = settingsRepository.getIslandAlarmWindowHours()
        isDuoIslandCombined.value = duoSettings.isDuoIslandCombinedSetting()
        isDuoTapForBrief.value = duoSettings.isDuoTapForBriefEnabled()
        isDuoHideOnShade.value = duoSettings.isDuoHideOnShadeEnabled()
        isDuoKeepInFullscreen.value = duoSettings.isDuoKeepInFullscreenEnabled()
        isDuoDisableInteractionsInFullscreen.value = duoSettings.isDuoDisableInteractionsInFullscreenEnabled()
        isIslandShowCaffeinate.value = settingsRepository.isIslandShowCaffeinateEnabled()
        isIslandShowNavigation.value = settingsRepository.isIslandShowNavigationEnabled()
        islandNavigationOnNew.value = settingsRepository.getIslandNavigationOnNew()
        islandNavigationExpandDistance.intValue = settingsRepository.getIslandNavigationExpandDistance()
        isIslandNavigationPeekAfterExpand.value = settingsRepository.isIslandNavigationPeekAfterExpandEnabled()
        isIslandShowHotspot.value = settingsRepository.isIslandShowHotspotEnabled()
        isIslandShowDevices.value = settingsRepository.isIslandShowDevicesEnabled()
        isIslandBriefEnabled.value = settingsRepository.isIslandBriefEnabled()
        isIslandBriefShowAlarm.value = settingsRepository.isIslandBriefShowAlarmEnabled()
        islandDevicesBatteryOrder.value = settingsRepository.getIslandDevicesBatteryOrder()
        islandExpandedPadding.floatValue = settingsRepository.getIslandExpandedPadding()
        islandExpandedTopPadding.floatValue = settingsRepository.getIslandExpandedTopPadding()
        islandExpandedBottomPadding.floatValue = settingsRepository.getIslandExpandedBottomPadding()
        islandExpandedTimeoutMs.longValue = settingsRepository.getIslandExpandedTimeoutMs()
        isIslandSuppressSystemHeadsUp.value = settingsRepository.isIslandSuppressSystemHeadsUpEnabled()
        isIslandDynamicHideStatusBar.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
                false,
            )
        isIslandCompactHideStatusBar.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_ISLAND_COMPACT_HIDE_STATUS_BAR,
                false,
            )
        isIslandHideWhenScreenOff.value = settingsRepository.isIslandHideWhenScreenOffEnabled()
        islandTimeoutMs.longValue = settingsRepository.getIslandTimeoutMs()
        isIslandLineStageEnabled.value = settingsRepository.isIslandLineStageEnabled()
        isIslandMediaPeekSongChange.value = settingsRepository.isIslandMediaPeekSongChangeEnabled()
        isIslandMediaKeepWhenPaused.value = settingsRepository.isIslandMediaKeepWhenPausedEnabled()
        isIslandMediaShowPrevious.value = settingsRepository.isIslandMediaShowPreviousEnabled()
        isIslandMediaShowLike.value = settingsRepository.isIslandMediaShowLikeEnabled()
        islandNotifPeekStyle.value = settingsRepository.getIslandNotifPeekStyle()
        isIslandNotifKeepProgress.value = settingsRepository.isIslandNotifKeepProgressEnabled()
        isIslandNotifQueue.value = settingsRepository.isIslandNotifQueueEnabled()
        islandNotifQueueStyle.value = settingsRepository.getIslandNotifQueueStyle()
        isIslandNotifQueueInline.value = settingsRepository.isIslandNotifQueueInlineEnabled()
        isIslandNotifSkipSilent.value = settingsRepository.isIslandNotifSkipSilentEnabled()
        isIslandNotifFollowDnd.value = settingsRepository.isIslandNotifFollowDndEnabled()
        isIslandNotifFilterApps.value = settingsRepository.isIslandNotifFilterAppsEnabled()
        isIslandShowNotifications.value = settingsRepository.isIslandShowNotificationsEnabled()
        isIslandNotifHub.value = settingsRepository.isIslandNotifHubEnabled()
        islandNotifHubIcons.intValue = settingsRepository.getIslandNotifHubIcons()
        islandNotifHubOrder.value = settingsRepository.getIslandNotifHubOrder()
        isIslandNotifHubIncludeSilent.value = settingsRepository.isIslandNotifHubIncludeSilent()
        isIslandNotifHubGroupByApps.value = settingsRepository.isIslandNotifHubGroupByApps()
        isIslandNotifTapToOpen.value = settingsRepository.isIslandNotifTapToOpenEnabled()
        isIslandCatchUpEnabled.value = settingsRepository.isIslandCatchUpEnabled()
        islandCatchUpTimeoutMs.longValue = settingsRepository.getIslandCatchUpStoredMs()
        isIslandOtpEnabled.value = settingsRepository.isIslandOtpEnabled()
        islandOtpTimeoutMs.longValue = settingsRepository.getIslandOtpTimeoutMs()
        isIslandShowGlow.value = settingsRepository.isIslandShowGlowEnabled()
        isIslandBorderOutline.value = settingsRepository.isIslandBorderOutlineEnabled()
        islandBorderOutlineColor.value = settingsRepository.getIslandBorderOutlineColor()
        isIslandBorderOutlineDynamic.value = settingsRepository.isIslandBorderOutlineDynamicEnabled()
        islandBorderOutlineThickness.floatValue = settingsRepository.getIslandBorderOutlineThickness()
        isIslandBorderOutlineHiddenWhenExpanded.value = settingsRepository.isIslandBorderOutlineHiddenWhenExpanded()
        isIslandPulseShadow.value = settingsRepository.isIslandPulseShadowEnabled()
        islandPulseShadowSize.floatValue = settingsRepository.getIslandPulseShadowSize()
        islandPulseShadowYShift.floatValue = settingsRepository.getIslandPulseShadowYShift()
        islandPulseShadowSpread.floatValue = settingsRepository.getIslandPulseShadowSpread()
        islandPulseShadowDurationMs.floatValue = settingsRepository.getIslandPulseShadowDurationMs()
        isIslandShowMedia.value = settingsRepository.isIslandShowMediaEnabled()
        isIslandShowCalendar.value = settingsRepository.isIslandShowCalendarEnabled()
        isIslandShowConsciousGate.value = settingsRepository.isIslandShowConsciousGateEnabled()
        isIslandShowTimeBattery.value = settingsRepository.isIslandShowTimeBatteryEnabled()
        isIslandBatteryPeekAlerts.value = settingsRepository.isIslandBatteryPeekAlertsEnabled()
        isIslandShowBattery.value = settingsRepository.isIslandShowBatteryEnabled()
        isIslandShowFlashlight.value = settingsRepository.isIslandShowFlashlightEnabled()
        islandBatteryStyle.value = settingsRepository.getIslandBatteryStyle()
        islandDevicesBatteryStyle.value = settingsRepository.getIslandDevicesBatteryStyle()
        isIslandBatteryPercentageEnabled.value = settingsRepository.isIslandBatteryPercentageEnabled()
        isIslandBatteryPercentageConditional.value = settingsRepository.isIslandBatteryPercentageConditional()
        isIslandBatteryIconConditional.value = settingsRepository.isIslandBatteryIconConditional()
        isIslandBatteryOnlyLow.value = settingsRepository.isIslandBatteryOnlyLowEnabled()
        isIslandDevicesBatteryOnlyLow.value = settingsRepository.isIslandDevicesBatteryOnlyLowEnabled()
        islandLongPressAction.value = settingsRepository.getIslandLongPressAction()
        islandSlideMode.value = settingsRepository.getIslandSlideMode()
        isIslandSlideTrack.value = settingsRepository.isIslandSlideTrackEnabled()
        isIslandLikeWhilePlaying.value = settingsRepository.isIslandLikeWhilePlayingEnabled()
        isIslandSlideInvertDirection.value = settingsRepository.isIslandSlideInvertDirectionEnabled()
        isStatusGlanceEnabled.value = settingsRepository.isStatusGlanceEnabled()
        isStatusGlanceAutoDetect.value = settingsRepository.isStatusGlanceAutoDetectEnabled()
        statusGlanceOffsetX.floatValue = settingsRepository.getStatusGlanceOffsetX()
        statusGlanceOffsetY.floatValue = settingsRepository.getStatusGlanceOffsetY()
        statusGlanceMaxWidth.floatValue = settingsRepository.getStatusGlanceMaxWidth()
        statusGlanceFontSize.floatValue = settingsRepository.getStatusGlanceFontSize()
        isStatusGlanceShowFlashlight.value = settingsRepository.isStatusGlanceShowFlashlightEnabled()
        isStatusGlanceShowCalendar.value = settingsRepository.isStatusGlanceShowCalendarEnabled()
        statusGlanceCalendarTimeframe.value = settingsRepository.getStatusGlanceCalendarTimeframe()
        statusGlanceCalendarShowAllDay.value = settingsRepository.isStatusGlanceCalendarShowAllDayEnabled()
        statusGlanceSelectedCalendarIds.value = settingsRepository.getStatusGlanceCalendarSelectedCalendars()
        isStatusGlanceShowMedia.value = settingsRepository.isStatusGlanceShowMediaEnabled()
        isStatusGlanceShowTime.value = settingsRepository.isStatusGlanceShowTimeEnabled()
        isStatusGlanceBackgroundPill.value = settingsRepository.isStatusGlanceBackgroundPillEnabled()
        isStatusGlanceAlbumArtColors.value = settingsRepository.isStatusGlanceAlbumArtColorsEnabled()
        isStatusGlanceHideWhenFullscreen.value = settingsRepository.isStatusGlanceHideWhenFullscreenEnabled()
        isStatusGlanceHideInQuickSettings.value = settingsRepository.isStatusGlanceHideInQuickSettingsEnabled()
        isStatusGlanceHideWhenLocked.value = settingsRepository.isStatusGlanceHideWhenLockedEnabled()
        isStatusGlanceBoldText.value = settingsRepository.isStatusGlanceBoldTextEnabled()
        isStatusGlanceShowBattery.value = settingsRepository.isStatusGlanceShowBatteryEnabled()
        statusGlanceBatteryDisplayMode.value = settingsRepository.getStatusGlanceBatteryDisplayMode()
        isStatusGlanceBatteryShowLow.value = settingsRepository.isStatusGlanceBatteryShowLowEnabled()
        isStatusGlanceBatteryShowCharging.value = settingsRepository.isStatusGlanceBatteryShowChargingEnabled()
        isStatusGlanceBatteryShowFull.value = settingsRepository.isStatusGlanceBatteryShowFullEnabled()
        isStatusGlanceBatteryShowOtherwise.value = settingsRepository.isStatusGlanceBatteryShowOtherwiseEnabled()
        statusGlanceBatteryIconSize.floatValue = settingsRepository.getStatusGlanceBatteryIconSize()
        statusGlanceTapAction.value = settingsRepository.getStatusGlanceTapAction()
        statusGlanceDoubleTapAction.value = settingsRepository.getStatusGlanceDoubleTapAction()
        statusGlanceLongPressAction.value = settingsRepository.getStatusGlanceLongPressAction()
        isFlashlightAlwaysTurnOffEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_FLASHLIGHT_ALWAYS_TURN_OFF_ENABLED)
        isFlashlightFadeEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_FLASHLIGHT_FADE_ENABLED)
        isFlashlightAdjustEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_FLASHLIGHT_ADJUST_INTENSITY_ENABLED)
        isFlashlightGlobalEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_FLASHLIGHT_GLOBAL_ENABLED)
        isFlashlightLiveUpdateEnabled.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_FLASHLIGHT_LIVE_UPDATE_ENABLED,
                true,
            )
        flashlightLastIntensity.value =
            settingsRepository.getInt(SettingsRepository.KEY_FLASHLIGHT_LAST_INTENSITY, 1)
        isFlashlightPocketTurnOffEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_FLASHLIGHT_POCKET_TURN_OFF_ENABLED)
        isFlashlightOverheatEnabled.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_FLASHLIGHT_OVERHEAT_PREVENTION_ENABLED,
                true,
            )
        isPitchBlackThemeEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_PITCH_BLACK_THEME_ENABLED)
        isEnableUnsupportedFeatures.value = settingsRepository.isEnableUnsupportedFeatures()
        isShowLegacyFeatures.value = settingsRepository.isShowLegacyFeatures()
        isSecureSensitiveTilesEnabled.value = settingsRepository.isSecureSensitiveTilesEnabled()

        input.load(context)

        battery.load(context)

        isDeviceAdminEnabled.value = isDeviceAdminActive(context)

        consciousGate.load(context)
        freeze.load(context)
        appLock.load(context)

        isDeveloperModeEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_DEVELOPER_MODE_ENABLED)

        connectivity.load(context)

        pinnedFeatureKeys.value = settingsRepository.getPinnedFeatures()
        pinnedQsTileKeys.value = settingsRepository.getPinnedQsTiles()
        isLikeSongToastEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_LIKE_SONG_TOAST_ENABLED, true)
        isLikeSongAodOverlayEnabled.value =
            settingsRepository.getBoolean(
                SettingsRepository.KEY_LIKE_SONG_AOD_OVERLAY_ENABLED,
                false,
            )
        pixelSearchResultApps.value = settingsRepository.isPixelSearchResultAppsEnabled()
        pixelSearchResultMedia.value = settingsRepository.isPixelSearchResultMediaEnabled()
        pixelSearchResultFiles.value = settingsRepository.isPixelSearchResultFilesEnabled()
        pixelSearchResultContacts.value = settingsRepository.isPixelSearchResultContactsEnabled()
        pixelSearchResultSettings.value = settingsRepository.isPixelSearchResultSettingsEnabled()
        pixelSearchResultShortcuts.value = settingsRepository.isPixelSearchResultShortcutsEnabled()
        pixelSearchResultWeb.value = settingsRepository.isPixelSearchResultWebEnabled()
        pixelSearchBubblesWeb.value = settingsRepository.isPixelSearchBubblesWebEnabled()
        pixelSearchEngine.value = settingsRepository.getPixelSearchEngine()
        security.load(context)
        scaleAnimationsMode.value = settingsRepository.getScaleAnimationsMode()
        isTouchSensitivityEnabled.value = settingsRepository.getTouchSensitivityEnabled()
        isAutoRotateEnabled.value = settingsRepository.getAutoRotateEnabled()
        screenTimeout.value = settingsRepository.getScreenTimeout()
        isPowerSaveModeEnabled.value = DeviceUtils.isPowerSaveMode(context)
        updateBlurState(context)
        updateRippleState(context)
        updateMotionBlurState(context)

        refreshTrackedUpdates(context)
        if (battery.isBatteryNotificationEnabled.value) {
            startBatteryNotificationService(context)
        }

        isLockdownModeEnabled.value =
            settingsRepository.getBoolean(SettingsRepository.KEY_LOCKDOWN_MODE)
    }

    private fun startBatteryNotificationService(context: Context) {
        com.jawzf.pixeltoolkit.utils.ServiceUtils
            .startRequiredServices(context)
    }

    /**
     * Executes the set enable unsupported features operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setEnableUnsupportedFeatures(
        enabled: Boolean,
        context: Context,
    ) {
        isEnableUnsupportedFeatures.value = enabled
        settingsRepository.setEnableUnsupportedFeatures(enabled)
        if (searchQuery.value.isNotBlank()) {
            onSearchQueryChanged(searchQuery.value, context)
        }
    }

    fun setShowLegacyFeatures(
        enabled: Boolean,
        context: Context,
    ) {
        isShowLegacyFeatures.value = enabled
        settingsRepository.setShowLegacyFeatures(enabled)
        if (searchQuery.value.isNotBlank()) {
            onSearchQueryChanged(searchQuery.value, context)
        }
    }

    fun setSecureSensitiveTilesEnabled(enabled: Boolean) {
        isSecureSensitiveTilesEnabled.value = enabled
        settingsRepository.setSecureSensitiveTilesEnabled(enabled)
    }

    /**
     * Executes the on search query changed operation.
     *
     * @param query [String] Target query.
     * @param context [Context] Target context.
     */
    fun onSearchQueryChanged(
        query: String,
        context: Context,
    ) {
        searchQuery.value = query
        searchJob?.cancel()

        if (query.isBlank()) {
            searchResults.value = emptyList()
            isSearching.value = false
            return
        }

        isSearching.value = true
        searchJob =
            viewModelScope.launch(Dispatchers.Default) {
                delay(300)
                val results =
                    SearchRegistry.search(
                        context,
                        query,
                        isEnableUnsupportedFeatures.value,
                        isShowLegacyFeatures.value,
                    )
                withContext(Dispatchers.Main) {
                    searchResults.value = results
                    isSearching.value = false
                }
            }
    }

    /**
     * Executes the add recent search operation.
     *
     * @param item [SearchableItem] Target item.
     */
    fun addRecentSearch(item: SearchableItem) {
        val current = recentSearches.value.toMutableList()
        // Remove existing to move to top
        current.removeAll {
            it.title == item.title &&
                it.featureKey == item.featureKey &&
                it.targetSettingHighlightKey == item.targetSettingHighlightKey
        }
        current.add(0, item)
        // Limit to 10
        val limited = current.take(10)
        recentSearches.value = limited
        settingsRepository.saveRecentSearches(limited)
    }

    /**
     * Executes the clear recent searches operation.
     */
    fun clearRecentSearches() {
        recentSearches.value = emptyList()
        settingsRepository.saveRecentSearches(emptyList())
    }

    /**
     * Executes the toggle pin feature operation.
     *
     * @param featureId [String] Target feature id.
     */
    fun togglePinFeature(featureId: String) {
        val current = pinnedFeatureKeys.value.toMutableList()
        if (current.contains(featureId)) {
            current.remove(featureId)
        } else {
            current.add(featureId) // Append at the end to keep order
        }
        pinnedFeatureKeys.value = current
        settingsRepository.savePinnedFeatures(current)

        appContext?.let { context ->
            com.jawzf.pixeltoolkit.utils.ShortcutUtil
                .updateLauncherDynamicShortcuts(context)
            val intent =
                Intent("com.jawzf.pixeltoolkit.action.FAVORITES_WIDGET_UPDATE").apply {
                    setPackage(context.packageName)
                }
            context.sendBroadcast(intent)
        }
    }

    fun updatePinnedFeatures(newOrder: List<String>) {
        pinnedFeatureKeys.value = newOrder
        settingsRepository.savePinnedFeatures(newOrder)

        appContext?.let { context ->
            com.jawzf.pixeltoolkit.utils.ShortcutUtil
                .updateLauncherDynamicShortcuts(context)
            val intent =
                Intent("com.jawzf.pixeltoolkit.action.FAVORITES_WIDGET_UPDATE").apply {
                    setPackage(context.packageName)
                }
            context.sendBroadcast(intent)
        }
    }

    /**
     * Executes the toggle pin qs tile operation.
     *
     * @param serviceClassName [String] Target service class name.
     */
    fun togglePinQsTile(serviceClassName: String) {
        val current = pinnedQsTileKeys.value.toMutableList()
        if (current.contains(serviceClassName)) {
            current.remove(serviceClassName)
        } else {
            current.add(serviceClassName)
        }
        pinnedQsTileKeys.value = current
        settingsRepository.savePinnedQsTiles(current)

        appContext?.let { context ->
            val intent =
                Intent("com.jawzf.pixeltoolkit.action.QS_TILES_WIDGET_UPDATE").apply {
                    setPackage(context.packageName)
                }
            context.sendBroadcast(intent)
        }
    }

    /**
     * Executes the set developer mode enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setDeveloperModeEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isDeveloperModeEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_DEVELOPER_MODE_ENABLED, enabled)
    }

    /**
     * Executes the set root enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setPrivilegedMode(
        mode: PrivilegedMode,
        context: Context,
    ) {
        settingsRepository.putString(SettingsRepository.KEY_PRIVILEGED_MODE, mode.key)
        settingsRepository.putBoolean(SettingsRepository.KEY_USE_ROOT, mode == PrivilegedMode.ROOT)
        refreshPrivilegedMode()
        check(context)
    }

    private fun refreshPrivilegedMode() {
        val ctx = ToolkitApp.context
        privilegedMode.value = ShellUtils.getSelectedMode(ctx)
        isRootEnabled.value = ShellUtils.isRootEnabled(ctx)
        usesAuthToken.value = ShellUtils.usesAuthToken(ctx)
    }

    /**
     * Executes the set pitch black theme enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setPitchBlackThemeEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isPitchBlackThemeEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_PITCH_BLACK_THEME_ENABLED, enabled)
    }

    /**
     * Executes the set blur enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setBlurEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        settingsRepository.putBoolean(SettingsRepository.KEY_USE_BLUR, enabled)
        updateBlurState(context)
    }

    /**
     * Executes the set ripple animation enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setRippleEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        settingsRepository.putBoolean(NotificationSettings.KEY_USE_RIPPLE, enabled)
        updateRippleState(context)
    }

    fun setMotionBlurEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        settingsRepository.putBoolean(SettingsRepository.KEY_MOTION_BLUR, enabled)
        updateMotionBlurState(context)
    }

    fun setMotionBlurScale(
        scale: Float,
        context: Context? = null,
    ) {
        motionBlurScale.floatValue = scale
        settingsRepository.putFloat(SettingsRepository.KEY_MOTION_BLUR_SCALE, scale)
    }

    fun setShakeForHelpEnabled(
        enabled: Boolean,
        context: Context? = null,
    ) {
        settingsRepository.setShakeForHelpEnabled(enabled)
        isShakeForHelpEnabled.value = enabled
    }

    /**
     * Executes the set swipe tabs enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setSwipeTabsEnabled(enabled: Boolean) {
        settingsRepository.putBoolean(SettingsRepository.KEY_SWIPE_TABS, enabled)
        isSwipeTabsEnabled.value = enabled
    }

    private fun updateBlurState(context: Context) {
        val useBlurSetting = settingsRepository.getBoolean(SettingsRepository.KEY_USE_BLUR, true)
        val isPowerSave = DeviceUtils.isPowerSaveMode(context)

        isBlurSettingEnabled.value = useBlurSetting
        isBlurEnabled.value = useBlurSetting && !isPowerSave
    }

    private fun updateRippleState(context: Context) {
        val useRippleSetting = settingsRepository.getBoolean(NotificationSettings.KEY_USE_RIPPLE, true)
        val isPowerSave = DeviceUtils.isPowerSaveMode(context)

        isRippleSettingEnabled.value = useRippleSetting
        isRippleEnabled.value = useRippleSetting && !isPowerSave
    }

    private fun updateMotionBlurState(context: Context) {
        val useMotionBlurSetting = settingsRepository.getBoolean(SettingsRepository.KEY_MOTION_BLUR, false)
        val isPowerSave = DeviceUtils.isPowerSaveMode(context)

        isMotionBlurSettingEnabled.value = useMotionBlurSetting
        isMotionBlurEnabled.value = useMotionBlurSetting && !isPowerSave
        motionBlurScale.floatValue = settingsRepository.getFloat(SettingsRepository.KEY_MOTION_BLUR_SCALE, 1.0f)
    }

    /**
     * Executes the refresh tracked updates operation.
     *
     * @param context [Context] Target context.
     */
    fun refreshTrackedUpdates(context: Context) {
        val trackedRepos = AppsSettings(context).getTrackedRepos()
        if (trackedRepos.isEmpty()) {
            hasPendingUpdates.value = false
            return
        }

        hasPendingUpdates.value = trackedRepos.any { it.isUpdateAvailable }
    }

    private fun isDeviceAdminActive(context: Context): Boolean = PermissionUtils.isDeviceAdminActive(context)

    /**
     * Executes the request device admin operation.
     *
     * @param context [Context] Target context.
     */
    fun requestDeviceAdmin(context: Context) {
        val adminComponent = ComponentName(context, SecurityDeviceAdminReceiver::class.java)
        val intent =
            Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                putExtra(
                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.perm_device_admin_explanation),
                )
            }
        if (context is Activity) {
            context.startActivity(intent)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Executes the request read phone state permission operation.
     *
     * @param activity [Activity] Target activity.
     */
    fun requestReadPhoneStatePermission(activity: Activity) {
        PermissionUtils.requestPermissionsSafely(
            activity,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            1005,
        )
    }

    /**
     * Executes the set widget enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setWidgetEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isWidgetEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_WIDGET_ENABLED, enabled)
    }

    /**
     * Executes the set hide gesture bar enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setHideGestureBarEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isHideGestureBarEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_HIDE_GESTURE_BAR_ENABLED, enabled)
        applyHideGestureBar(context, enabled)
    }

    /**
     * Executes the set circle to search gesture enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setCircleToSearchGestureEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isCircleToSearchGestureEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_ENABLED,
            enabled,
        )
    }

    /**
     * Executes the set circle to search gesture height operation.
     *
     * @param height [Float] Target height.
     */
    fun setCircleToSearchGestureHeight(height: Float) {
        circleToSearchGestureHeight.floatValue = height
        settingsRepository.putFloat(SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_HEIGHT, height)
    }

    /**
     * Executes the set circle to search gesture width operation.
     *
     * @param width [Float] Target width.
     */
    fun setCircleToSearchGestureWidth(width: Float) {
        circleToSearchGestureWidth.floatValue = width
        settingsRepository.putFloat(SettingsRepository.KEY_CIRCLE_TO_SEARCH_GESTURE_WIDTH, width)
    }

    /**
     * Executes the set circle to search preview enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setCircleToSearchPreviewEnabled(enabled: Boolean) {
        isCircleToSearchPreviewEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_CIRCLE_TO_SEARCH_PREVIEW_ENABLED,
            enabled,
        )
    }

    /**
     * Executes the set hide gesture bar on launcher enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setHideGestureBarOnLauncherEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isHideGestureBarOnLauncherEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_HIDE_GESTURE_BAR_ON_LAUNCHER_ENABLED,
            enabled,
        )

        if (!enabled) {
            StatusBarManager.requestRestore(
                context,
                "GestureBarAutomation",
            )
        }

        updateAppDetectionService(context)
    }

    /**
     * Executes the set disable rotation suggestion enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setDisableRotationSuggestionEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isDisableRotationSuggestionEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_DISABLE_ROTATION_SUGGESTION, enabled)
        applyDisableRotationSuggestion(context, enabled)
    }

    /**
     * Executes the set allow overlays in settings enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setAllowOverlaysInSettingsEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isAllowOverlaysInSettingsEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_ALLOW_OVERLAYS_IN_SETTINGS, enabled)
        applyAllowOverlaysInSettings(context, enabled)
    }

    /**
     * Executes the refresh allow overlays in settings state operation.
     *
     * @param context [Context] Target context.
     */
    fun refreshAllowOverlaysInSettingsState(context: Context) {
        try {
            val currentVal =
                Settings.Secure.getInt(context.contentResolver, "secure_overlay_settings", 0) == 1
            isAllowOverlaysInSettingsEnabled.value = currentVal
            settingsRepository.putBoolean(
                SettingsRepository.KEY_ALLOW_OVERLAYS_IN_SETTINGS,
                currentVal,
            )
        } catch (e: Exception) {
            // Secure setting not accessible without permission
        }
    }

    private fun applyAllowOverlaysInSettings(
        context: Context,
        enabled: Boolean,
    ) {
        val value = if (enabled) 1 else 0
        val key = "secure_overlay_settings"
        var success = false

        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success = Settings.Secure.putInt(context.contentResolver, key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!success) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                ShellUtils.runCommand(context, "settings put secure $key $value")
            }
        }
    }

    /**
     * Executes the set transparent navigation bar enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setTransparentNavigationBarEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isTransparentNavigationBarEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_TRANSPARENT_NAVIGATION_BAR, enabled)
        applyTransparentNavigationBar(context, enabled)
    }

    /**
     * Executes the refresh transparent navigation bar state operation.
     *
     * @param context [Context] Target context.
     */
    fun refreshTransparentNavigationBarState(context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val pkg = "com.android.internal.systemui.navbar.transparent"
            val output = ShellUtils.runCommandWithOutput(context, "cmd overlay list")
            if (output != null) {
                val isEnabled =
                    output.lines().any { line ->
                        line.contains("[x]") && line.contains(pkg)
                    }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isTransparentNavigationBarEnabled.value = isEnabled
                    settingsRepository.putBoolean(
                        SettingsRepository.KEY_TRANSPARENT_NAVIGATION_BAR,
                        isEnabled,
                    )
                }
            }
        }
    }

    fun setSplitScreenGestureEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isSplitScreenGestureEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_SPLIT_SCREEN_GESTURE, enabled)
        viewModelScope.launch(Dispatchers.IO) {
            SplitScreenUtil.setGestureEnabled(context, enabled)
        }
    }

    // The gesture binding lives in system settings, where the user can also remove it.
    fun refreshSplitScreenGestureState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val isEnabled = SplitScreenUtil.isGestureEnabled(context) ?: return@launch
            withContext(Dispatchers.Main) {
                isSplitScreenGestureEnabled.value = isEnabled
                settingsRepository.putBoolean(SettingsRepository.KEY_SPLIT_SCREEN_GESTURE, isEnabled)
            }
        }
    }

    private fun applyTransparentNavigationBar(
        context: Context,
        enabled: Boolean,
    ) {
        val pkg = "com.android.internal.systemui.navbar.transparent"
        val action = if (enabled) "enable" else "disable"
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            ShellUtils.runCommand(context, "cmd overlay $action --user current $pkg")
        }
    }

    /**
     * Executes the set prefer gpu composing enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setPreferGpuComposingEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isPreferGpuComposingEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_PREFER_GPU_COMPOSING, enabled)
        applyPreferGpuComposing(context, enabled)
    }

    /**
     * Executes the refresh prefer gpu composing state operation.
     *
     * @param context [Context] Target context.
     */
    fun refreshPreferGpuComposingState(context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val isShellGranted =
                (isShizukuAvailable.value && isShizukuPermissionGranted.value) ||
                    (isRootAvailable.value && isRootPermissionGranted.value)
            if (isShellGranted) {
                val isRoot = isRootAvailable.value && isRootPermissionGranted.value
                val liveValue = SurfaceFlingerControl.isHwOverlaysDisabled(context, isRoot)
                isPreferGpuComposingEnabled.value = liveValue
                settingsRepository.putBoolean(
                    SettingsRepository.KEY_PREFER_GPU_COMPOSING,
                    liveValue,
                )
            }
        }
    }

    private fun applyPreferGpuComposing(
        context: Context,
        enabled: Boolean,
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val isRoot = isRootAvailable.value && isRootPermissionGranted.value
            SurfaceFlingerControl.setDisableHwOverlays(enabled, isRoot)
        }
    }

    private fun applyDisableRotationSuggestion(
        context: Context,
        enabled: Boolean,
    ) {
        val value = if (enabled) 0 else 1
        val key = "show_rotation_suggestions"

        var success = false
        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success = Settings.Secure.putInt(context.contentResolver, key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!success) {
            val command = "settings put secure $key $value"
            if (ShizukuUtils.hasPermission()) {
                ShizukuUtils.runCommand(command)
            } else if (RootUtils.isRootPermissionGranted()) {
                RootUtils.runCommand(command)
            }
        }
    }

    /**
     * Executes the set pixel searchbar enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isPixelSearchbarEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_PIXEL_SEARCHBAR, enabled)
        applyPixelSearchbarSetting(context, enabled)
    }

    private fun applyPixelSearchbarSetting(
        context: Context,
        enabled: Boolean,
    ) {
        writePixelSearchbarSetting(context, enabled)
        forceStopNexusLauncher()
    }

    private fun writePixelSearchbarSetting(
        context: Context,
        enabled: Boolean,
    ) {
        val value = if (enabled) "com.jawzf.pixeltoolkit" else null
        val key = "selected_search_engine"

        var success = false
        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success = Settings.Secure.putString(context.contentResolver, key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!success) {
            val command =
                if (enabled) {
                    "settings put secure $key com.jawzf.pixeltoolkit"
                } else {
                    "settings delete secure $key"
                }
            if (ShizukuUtils.hasPermission()) {
                ShizukuUtils.runCommand(command)
            } else if (RootUtils.isRootPermissionGranted()) {
                RootUtils.runCommand(command)
            }
        }
    }

    private fun forceStopNexusLauncher() {
        val forceStopCommand = "am force-stop com.google.android.apps.nexuslauncher"
        if (ShizukuUtils.hasPermission()) {
            ShizukuUtils.runCommand(forceStopCommand)
        } else if (RootUtils.isRootPermissionGranted()) {
            RootUtils.runCommand(forceStopCommand)
        }
    }

    /**
     * Executes the set pixel searchbar type operation.
     *
     * @param type [String] Target type.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarType(
        type: String,
        context: Context,
    ) {
        pixelSearchbarType.value = type
        settingsRepository.setPixelSearchbarType(type)
        if (type == "music") {
            updateMediaFromActiveSession(context)
        }
        updatePixelSearchbarWidget(context)

        if (isPixelSearchbarEnabled.value) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                writePixelSearchbarSetting(context, false)
                kotlinx.coroutines.delay(300)
                writePixelSearchbarSetting(context, true)
                forceStopNexusLauncher()
            }
        } else {
            forceStopNexusLauncher()
        }
    }

    /**
     * Executes the set pixel searchbar date format operation.
     *
     * @param format [String] Target format.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarDateFormat(
        format: String,
        context: Context,
    ) {
        pixelSearchbarDateFormat.value = format
        settingsRepository.setPixelSearchbarDateFormat(format)
        updatePixelSearchbarWidget(context)

        // Force stop nexus launcher to apply setting
        val forceStopCommand = "am force-stop com.google.android.apps.nexuslauncher"
        if (ShizukuUtils.hasPermission()) {
            ShizukuUtils.runCommand(forceStopCommand)
        } else if (RootUtils.isRootPermissionGranted()) {
            RootUtils.runCommand(forceStopCommand)
        }
    }

    /**
     * Executes the set pixel searchbar background pill operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarBackgroundPill(
        enabled: Boolean,
        context: Context,
    ) {
        pixelSearchbarBackgroundPill.value = enabled
        settingsRepository.setPixelSearchbarBackgroundPill(enabled)
        updatePixelSearchbarWidget(context)

        // Force stop nexus launcher to apply setting
        val forceStopCommand = "am force-stop com.google.android.apps.nexuslauncher"
        if (ShizukuUtils.hasPermission()) {
            ShizukuUtils.runCommand(forceStopCommand)
        } else if (RootUtils.isRootPermissionGranted()) {
            RootUtils.runCommand(forceStopCommand)
        }
    }

    /**
     * Executes the set pixel searchbar widget id operation.
     *
     * @param id [Int] Target id.
     * @param provider [String?] Target provider.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarWidgetId(
        id: Int,
        provider: String?,
        context: Context,
    ) {
        pixelSearchbarWidgetId.intValue = id
        pixelSearchbarWidgetProvider.value = provider
        settingsRepository.setPixelSearchbarWidgetId(id)
        settingsRepository.setPixelSearchbarWidgetProvider(provider)
        updatePixelSearchbarWidget(context)
    }

    /**
     * Executes the clear pixel searchbar widget operation.
     *
     * @param context [Context] Target context.
     */
    fun clearPixelSearchbarWidget(context: Context) {
        val previousId = pixelSearchbarWidgetId.intValue
        if (previousId != android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID) {
            try {
                android.appwidget
                    .AppWidgetHost(context, com.jawzf.pixeltoolkit.services.widgets.WidgetScraperService.HOST_ID)
                    .deleteAppWidgetId(previousId)
            } catch (_: Exception) {
            }
        }
        pixelSearchbarWidgetId.intValue = android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
        pixelSearchbarWidgetProvider.value = null
        settingsRepository.setPixelSearchbarWidgetId(android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID)
        settingsRepository.setPixelSearchbarWidgetProvider(null)
        context.stopService(
            android.content.Intent(
                context,
                com.jawzf.pixeltoolkit.services.widgets.WidgetScraperService::class.java,
            ),
        )
        updatePixelSearchbarWidget(context)
    }

    /**
     * Executes the update pixel searchbar scraped text operation.
     *
     * @param line1 [String] Target line1.
     * @param line2 [String] Target line2.
     * @param context [Context] Target context.
     */
    fun updatePixelSearchbarScrapedText(
        line1: String,
        line2: String,
        context: Context,
    ) {
        pixelSearchbarScrapedLine1.value = line1
        pixelSearchbarScrapedLine2.value = line2
        settingsRepository.setPixelSearchbarScrapedLine1(line1)
        settingsRepository.setPixelSearchbarScrapedLine2(line2)
        updatePixelSearchbarWidget(context)
    }

    /**
     * Executes the set pixel searchbar widget padding h operation.
     *
     * @param value [Int] Target value.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarWidgetPaddingH(
        value: Int,
        context: Context,
    ) {
        pixelSearchbarWidgetPaddingH.intValue = value
        settingsRepository.setPixelSearchbarWidgetPaddingH(value)
        updatePixelSearchbarWidget(context)
    }

    /**
     * Executes the set pixel searchbar widget padding v operation.
     *
     * @param value [Int] Target value.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarWidgetPaddingV(
        value: Int,
        context: Context,
    ) {
        pixelSearchbarWidgetPaddingV.intValue = value
        settingsRepository.setPixelSearchbarWidgetPaddingV(value)
        updatePixelSearchbarWidget(context)
    }

    val pixelSearchResultApps = mutableStateOf(true)
    val pixelSearchResultMedia = mutableStateOf(false)
    val pixelSearchResultFiles = mutableStateOf(false)
    val pixelSearchResultContacts = mutableStateOf(true)
    val pixelSearchResultSettings = mutableStateOf(true)
    val pixelSearchResultShortcuts = mutableStateOf(true)
    val pixelSearchResultWeb = mutableStateOf(true)
    val pixelSearchBubblesWeb = mutableStateOf(false)
    val pixelSearchEngine = mutableStateOf("Google")

    fun setPixelSearchResultAppsEnabled(enabled: Boolean) {
        pixelSearchResultApps.value = enabled
        settingsRepository.setPixelSearchResultAppsEnabled(enabled)
    }

    fun setPixelSearchResultMediaEnabled(enabled: Boolean) {
        pixelSearchResultMedia.value = enabled
        settingsRepository.setPixelSearchResultMediaEnabled(enabled)
    }

    fun setPixelSearchResultFilesEnabled(enabled: Boolean) {
        pixelSearchResultFiles.value = enabled
        settingsRepository.setPixelSearchResultFilesEnabled(enabled)
    }

    fun setPixelSearchResultContactsEnabled(enabled: Boolean) {
        pixelSearchResultContacts.value = enabled
        settingsRepository.setPixelSearchResultContactsEnabled(enabled)
    }

    fun setPixelSearchResultSettingsEnabled(enabled: Boolean) {
        pixelSearchResultSettings.value = enabled
        settingsRepository.setPixelSearchResultSettingsEnabled(enabled)
    }

    fun setPixelSearchResultShortcutsEnabled(enabled: Boolean) {
        pixelSearchResultShortcuts.value = enabled
        settingsRepository.setPixelSearchResultShortcutsEnabled(enabled)
    }

    fun setPixelSearchResultWebEnabled(enabled: Boolean) {
        pixelSearchResultWeb.value = enabled
        settingsRepository.setPixelSearchResultWebEnabled(enabled)
    }

    fun setPixelSearchBubblesWebEnabled(enabled: Boolean) {
        pixelSearchBubblesWeb.value = enabled
        settingsRepository.setPixelSearchBubblesWebEnabled(enabled)
    }

    fun setPixelSearchEngine(engine: String) {
        pixelSearchEngine.value = engine
        settingsRepository.setPixelSearchEngine(engine)
    }

    /**
     * Executes the set pixel searchbar tap action enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setPixelSearchbarTapActionEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        pixelSearchbarTapActionEnabled.value = enabled
        settingsRepository.setPixelSearchbarTapActionEnabled(enabled)
        updatePixelSearchbarWidget(context)
    }

    fun updatePixelSearchbarMusic(
        title: String,
        artist: String,
        packageName: String,
        context: Context,
    ) {
        pixelSearchbarMusicTitle.value = title
        pixelSearchbarMusicArtist.value = artist
        pixelSearchbarMusicPackage.value = packageName
        settingsRepository.setPixelSearchbarMusicTitle(title)
        settingsRepository.setPixelSearchbarMusicArtist(artist)
        settingsRepository.setPixelSearchbarMusicPackage(packageName)
        settingsRepository.incrementPixelSearchbarWidgetRevision()
        updatePixelSearchbarWidget(context)
    }

    /**
     * Executes the update media from active session operation.
     *
     * @param context [Context] Target context.
     */
    fun updateMediaFromActiveSession(context: Context) {
        try {
            val manager =
                context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? android.media.session.MediaSessionManager
                    ?: return
            val componentName =
                android.content.ComponentName(
                    context,
                    com.jawzf.pixeltoolkit.services.NotificationListener::class.java,
                )
            val sessions = manager.getActiveSessions(componentName)
            val activeSession =
                sessions
                    .sortedWith(
                        compareByDescending<android.media.session.MediaController> {
                            val state = it.playbackState?.state
                            state == android.media.session.PlaybackState.STATE_PLAYING ||
                                state == android.media.session.PlaybackState.STATE_BUFFERING
                        }.thenByDescending {
                            val state = it.playbackState?.state
                            state == android.media.session.PlaybackState.STATE_PAUSED
                        },
                    ).firstOrNull()

            if (activeSession != null) {
                val metadata = activeSession.metadata
                val title =
                    metadata?.getString(android.media.MediaMetadata.METADATA_KEY_TITLE) ?: ""
                val artist =
                    metadata?.getString(android.media.MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                val packageName = activeSession.packageName

                val artwork =
                    metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ALBUM_ART)
                        ?: metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_ART)
                        ?: metadata?.getBitmap(android.media.MediaMetadata.METADATA_KEY_DISPLAY_ICON)

                val filesDirFile = File(context.filesDir, "music_artwork.png")
                if (artwork != null) {
                    try {
                        FileOutputStream(filesDirFile).use { out ->
                            artwork.compress(Bitmap.CompressFormat.PNG, 100, out)
                        }
                    } catch (_: Exception) {
                    }
                } else {
                    if (filesDirFile.exists()) filesDirFile.delete()
                }

                pixelSearchbarMusicTitle.value = title
                pixelSearchbarMusicArtist.value = artist
                pixelSearchbarMusicPackage.value = packageName
                settingsRepository.setPixelSearchbarMusicTitle(title)
                settingsRepository.setPixelSearchbarMusicArtist(artist)
                settingsRepository.setPixelSearchbarMusicPackage(packageName)
                settingsRepository.incrementPixelSearchbarWidgetRevision()
            }
        } catch (_: Exception) {
        }
    }

    /**
     * Executes the update pixel searchbar widget operation.
     *
     * @param context [Context] Target context.
     */
    fun updatePixelSearchbarWidget(context: Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
                val widget =
                    com.jawzf.pixeltoolkit.services.widgets
                        .PixelSearchbarWidget()
                val glanceIds =
                    manager.getGlanceIds(com.jawzf.pixeltoolkit.services.widgets.PixelSearchbarWidget::class.java)
                for (glanceId in glanceIds) {
                    widget.update(context, glanceId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun applyHideGestureBar(
        context: Context,
        enabled: Boolean,
    ) {
        if (enabled) {
            StatusBarManager.requestDisable(
                context,
                "HideGestureBar",
                setOf(StatusBarManager.FLAG_HOME),
            )
        } else {
            StatusBarManager.requestRestore(
                context,
                "HideGestureBar",
            )
        }
    }

    /**
     * Executes the set button remap enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setButtonRemapEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isButtonRemapEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_BUTTON_REMAP_ENABLED, enabled)
        if (!enabled) {
            PowerButtonGestures.restoreDefaults(context, settingsRepository)
        } else {
            val hasNonDefaultChoice =
                RemapSlot.ALL.any { slot ->
                    slot.choicePrefKey != null &&
                        (powerChoices[slot] ?: slot.defaultChoice) != slot.defaultChoice
                }
            if (hasNonDefaultChoice || isPowerCameraFlipEnabled.value) {
                PowerButtonGestures.sync(context, settingsRepository)
            }
        }
    }

    // Pause remap while the system volume dialog is visible
    fun setButtonRemapPauseOnVolumeDialog(
        enabled: Boolean,
        context: Context,
    ) {
        isButtonRemapPauseOnVolumeDialog.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_BUTTON_REMAP_PAUSE_ON_VOLUME_DIALOG, enabled)
    }

    /**
     * Executes the set button remap use shizuku operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setButtonRemapUseShizuku(
        enabled: Boolean,
        context: Context,
    ) {
        isButtonRemapUseShizuku.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_BUTTON_REMAP_USE_SHIZUKU, enabled)
    }

    /**
     * Sets the ordered actions for a Button Remap slot.
     */
    fun setRemapActions(
        slot: RemapSlot,
        actions: List<Action>,
    ) {
        remapActions[slot] = actions
        settingsRepository.setRemapActions(slot.prefKey, actions)
    }

    fun setPowerChoice(
        slot: RemapSlot,
        choice: PowerChoice,
        context: Context,
    ) {
        powerChoices[slot] = choice
        settingsRepository.setPowerChoice(slot, choice)
        PowerButtonGestures.sync(context, settingsRepository)
    }

    fun setPowerCameraFlipEnabled(enabled: Boolean) {
        isPowerCameraFlipEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_BUTTON_REMAP_POWER_CAMERA_FLIP, enabled)
    }

    fun setLockscreenShortcutsEnabled(enabled: Boolean) {
        isLockscreenShortcutsEnabled.value = enabled
        settingsRepository.setLockscreenShortcutsEnabled(enabled)
    }

    fun setLockscreenShortcutActions(
        side: LockscreenShortcutSide,
        actions: List<Action>,
    ) {
        lockscreenShortcutActions[side] = actions
        settingsRepository.setRemapActions(side.prefKey, actions)
    }

    /**
     * Executes the set remap haptic type operation.
     *
     * @param type [HapticFeedbackType] Target type.
     * @param context [Context] Target context.
     */
    fun setRemapHapticType(
        type: HapticFeedbackType,
        context: Context,
    ) {
        remapHapticType.value = type
        settingsRepository.putString(SettingsRepository.KEY_BUTTON_REMAP_HAPTIC_TYPE, type.name)
    }

    fun setDuoEnabled(enabled: Boolean) {
        isDuoEnabled.value = enabled
        duoSettings.setDuoEnabled(enabled)
    }

    fun setDuoAutoDetect(enabled: Boolean) {
        isDuoAutoDetect.value = enabled
        duoSettings.setDuoAutoDetectEnabled(enabled)
    }

    fun setDuoCameraOffsetX(value: Float) {
        duoCameraOffsetX.floatValue = value
        duoSettings.setDuoCameraOffsetX(value)
    }

    fun setDuoCameraOffsetY(value: Float) {
        duoCameraOffsetY.floatValue = value
        duoSettings.setDuoCameraOffsetY(value)
    }

    fun setDuoCameraSize(value: Float) {
        duoCameraSize.floatValue = value
        duoSettings.setDuoCameraSize(value)
    }

    fun refreshDuoCameraPlacement() {
        duoSettings.markDisplayProfileSeen()
        isFoldableCameraDevice.value = duoSettings.isFoldableDevice()
        isDuoOrientationProfiles.value = duoSettings.isDuoOrientationProfilesEnabled()
        isDuoHiddenInCurrentOrientation.value = duoSettings.isDuoHiddenInCurrentOrientation()
        duoCameraOffsetX.floatValue = duoSettings.getDuoCameraOffsetX()
        duoCameraOffsetY.floatValue = duoSettings.getDuoCameraOffsetY()
        duoCameraSize.floatValue = duoSettings.getDuoCameraSize()
        hasMultipleDuoDisplays.value = duoSettings.isFoldableDevice()
    }

    fun setDuoOrientationProfiles(enabled: Boolean) {
        duoSettings.setDuoOrientationProfilesEnabled(enabled)
        refreshDuoCameraPlacement()
    }

    fun setDuoHiddenInCurrentOrientation(hidden: Boolean) {
        duoSettings.setDuoHiddenInCurrentOrientation(hidden)
        isDuoHiddenInCurrentOrientation.value = hidden
    }

    fun refreshIslandCameraPlacement() {
        isFoldableCameraDevice.value = duoSettings.isFoldableDevice()
        isIslandOrientationProfiles.value = settingsRepository.isIslandOrientationProfilesEnabled()
        isIslandHiddenInCurrentOrientation.value = settingsRepository.isIslandHiddenInCurrentOrientation()
        islandCameraOffsetX.floatValue = settingsRepository.getIslandCameraOffsetX()
        islandCameraOffsetY.floatValue = settingsRepository.getIslandCameraOffsetY()
        islandCameraSize.floatValue = settingsRepository.getIslandCameraSize()
        islandCameraPosition.value = settingsRepository.getIslandCameraPosition()
    }

    fun setIslandOrientationProfiles(enabled: Boolean) {
        settingsRepository.setIslandOrientationProfilesEnabled(enabled)
        refreshIslandCameraPlacement()
    }

    fun setIslandHiddenInCurrentOrientation(hidden: Boolean) {
        settingsRepository.setIslandHiddenInCurrentOrientation(hidden)
        isIslandHiddenInCurrentOrientation.value = hidden
    }

    fun setDuoArcThickness(value: Float) {
        duoArcThickness.floatValue = value
        duoSettings.setDuoArcThickness(value)
    }

    fun setDuoDotSize(value: Float) {
        duoDotSize.floatValue = value
        duoSettings.setDuoDotSize(value)
    }

    fun setDuoRingRadius(value: Float) {
        duoRingRadius.floatValue = value
        duoSettings.setDuoRingRadius(value)
    }

    fun setDuoTimeTextScale(value: Float) {
        duoTimeTextScale.floatValue = value
        duoSettings.setDuoTimeTextScale(value)
    }

    fun setDuoShowBattery(enabled: Boolean) {
        isDuoShowBattery.value = enabled
        duoSettings.setDuoShowBatteryEnabled(enabled)
        if (!enabled) {
            isDuoShowBatteryPercentage.value = false
            duoSettings.setDuoShowBatteryPercentageEnabled(false)
            isDuoShowNetworks.value = false
            duoSettings.setDuoShowNetworksEnabled(false)
            isDuoShowTime.value = false
            duoSettings.setDuoShowTimeEnabled(false)
        }
    }

    fun setDuoShowBatteryPercentage(enabled: Boolean) {
        isDuoShowBatteryPercentage.value = enabled
        duoSettings.setDuoShowBatteryPercentageEnabled(enabled)
    }

    fun setDuoBatteryPercentageOnlyColored(enabled: Boolean) {
        isDuoBatteryPercentageOnlyColored.value = enabled
        duoSettings.setDuoBatteryPercentageOnlyColoredEnabled(enabled)
    }

    fun setDuoBatteryChargingColorEnabled(enabled: Boolean) {
        isDuoBatteryChargingColorEnabled.value = enabled
        duoSettings.setDuoBatteryChargingColorEnabled(enabled)
    }

    fun setDuoBatteryChargingColor(colorHex: String) {
        duoBatteryChargingColor.value = colorHex
        duoSettings.setDuoBatteryChargingColor(colorHex)
    }

    fun setDuoBatteryPowerSaveColorEnabled(enabled: Boolean) {
        isDuoBatteryPowerSaveColorEnabled.value = enabled
        duoSettings.setDuoBatteryPowerSaveColorEnabled(enabled)
    }

    fun setDuoBatteryPowerSaveColor(colorHex: String) {
        duoBatteryPowerSaveColor.value = colorHex
        duoSettings.setDuoBatteryPowerSaveColor(colorHex)
    }

    fun setDuoBatteryLowColorEnabled(enabled: Boolean) {
        isDuoBatteryLowColorEnabled.value = enabled
        duoSettings.setDuoBatteryLowColorEnabled(enabled)
    }

    fun setDuoBatteryLowColor(colorHex: String) {
        duoBatteryLowColor.value = colorHex
        duoSettings.setDuoBatteryLowColor(colorHex)
    }

    fun setIslandBatteryIdleColorEnabled(enabled: Boolean) {
        isIslandBatteryIdleColorEnabled.value = enabled
        settingsRepository.setIslandBatteryIdleColorEnabled(enabled)
    }

    fun setIslandBatteryIdleColor(colorHex: String) {
        islandBatteryIdleColor.value = colorHex
        settingsRepository.setIslandBatteryIdleColor(colorHex)
    }

    fun setDuoBatteryCriticalColorEnabled(enabled: Boolean) {
        isDuoBatteryCriticalColorEnabled.value = enabled
        duoSettings.setDuoBatteryCriticalColorEnabled(enabled)
    }

    fun setDuoBatteryCriticalColor(colorHex: String) {
        duoBatteryCriticalColor.value = colorHex
        duoSettings.setDuoBatteryCriticalColor(colorHex)
    }

    fun setDuoBatteryLowLevel(level: Int) {
        val (low, critical) = DuoBatteryLevels.withLow(level, duoBatteryCriticalLevel.intValue)
        applyDuoBatteryLevels(low, critical)
    }

    fun setDuoBatteryCriticalLevel(level: Int) {
        val (low, critical) = DuoBatteryLevels.withCritical(level, duoBatteryLowLevel.intValue)
        applyDuoBatteryLevels(low, critical)
    }

    private fun applyDuoBatteryLevels(low: Int, critical: Int) {
        duoBatteryLowLevel.intValue = low
        duoBatteryCriticalLevel.intValue = critical
        duoSettings.setDuoBatteryLevels(low, critical)
    }

    fun setDuoShowNetworks(enabled: Boolean) {
        isDuoShowNetworks.value = enabled
        duoSettings.setDuoShowNetworksEnabled(enabled)
        if (enabled) {
            isDuoShowTime.value = false
            duoSettings.setDuoShowTimeEnabled(false)
        }
    }

    fun setDuoDifferentiateWifi(enabled: Boolean) {
        isDuoDifferentiateWifi.value = enabled
        duoSettings.setDuoDifferentiateWifiEnabled(enabled)
    }

    fun setDuoShowTime(enabled: Boolean) {
        isDuoShowTime.value = enabled
        duoSettings.setDuoShowTimeEnabled(enabled)
        if (enabled && combinedActive()) {
            isIslandShowTimeBattery.value = false
            settingsRepository.setIslandShowTimeBatteryEnabled(false)
            setIslandShowBattery(false)
        }
        if (enabled) {
            isDuoShowNetworks.value = false
            duoSettings.setDuoShowNetworksEnabled(false)
        }
    }

    fun setDuoShowMedia(enabled: Boolean) {
        isDuoShowMedia.value = enabled
        duoSettings.setDuoShowMediaEnabled(enabled)
        if (enabled && combinedActive()) {
            isIslandShowMedia.value = false
            settingsRepository.setIslandShowMediaEnabled(false)
        }
    }

    fun setDuoRotateAlbumArt(enabled: Boolean) {
        isDuoRotateAlbumArt.value = enabled
        duoSettings.setDuoRotateAlbumArtEnabled(enabled)
    }

    fun setDuoShowProgress(enabled: Boolean) {
        isDuoShowProgress.value = enabled
        duoSettings.setDuoShowProgressEnabled(enabled)
        if (enabled && combinedActive()) {
            isIslandNotifKeepProgress.value = false
            settingsRepository.setIslandNotifKeepProgressEnabled(false)
        }
    }

    fun setDuoShowFlashlight(enabled: Boolean) {
        isDuoShowFlashlight.value = enabled
        duoSettings.setDuoShowFlashlightEnabled(enabled)
        if (enabled && combinedActive()) {
            isIslandShowFlashlight.value = false
            settingsRepository.setIslandShowFlashlightEnabled(false)
        }
    }

    private fun combinedActive(): Boolean = duoSettings.isDuoIslandCombined()

    fun setDuoKeepInFullscreen(enabled: Boolean) {
        isDuoKeepInFullscreen.value = enabled
        duoSettings.setDuoKeepInFullscreenEnabled(enabled)
    }

    fun setDuoDisableInteractionsInFullscreen(enabled: Boolean) {
        isDuoDisableInteractionsInFullscreen.value = enabled
        duoSettings.setDuoDisableInteractionsInFullscreenEnabled(enabled)
    }

    fun setDuoHideOnShade(enabled: Boolean) {
        isDuoHideOnShade.value = enabled
        duoSettings.setDuoHideOnShadeEnabled(enabled)
    }

    fun setDuoTapForBrief(enabled: Boolean) {
        isDuoTapForBrief.value = enabled
        duoSettings.setDuoTapForBriefEnabled(enabled)
    }

    fun setDuoIslandCombined(enabled: Boolean) {
        isDuoIslandCombined.value = enabled
        duoSettings.setDuoIslandCombined(enabled)
        if (!enabled) return
        // Duo owns the idle state, Island owns everything with more info
        setDuoShowBattery(true)
        setDuoShowNetworks(true)
        setIslandShowTimeBattery(false)
        setIslandShowBattery(false)
        isDuoShowMedia.value = false
        duoSettings.setDuoShowMediaEnabled(false)
        isDuoShowFlashlight.value = false
        duoSettings.setDuoShowFlashlightEnabled(false)
        isDuoShowProgress.value = false
        duoSettings.setDuoShowProgressEnabled(false)
        setIslandShowMedia(true)
        setIslandShowFlashlight(true)
        setIslandNotifKeepProgress(true)
    }

    fun setIslandEnabled(enabled: Boolean) {
        isIslandEnabled.value = enabled
        settingsRepository.setIslandEnabled(enabled)
    }

    fun setIslandAutoDetect(enabled: Boolean) {
        isIslandAutoDetect.value = enabled
        settingsRepository.setIslandAutoDetectEnabled(enabled)
    }

    fun autoAlignIslandWithCamera(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return false
        val metrics = wm.maximumWindowMetrics
        val rect =
            try {
                metrics.windowInsets.displayCutout?.boundingRects
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
        settingsRepository.setIslandCameraOffsetX(value)
    }

    fun setIslandCameraOffsetY(value: Float) {
        islandCameraOffsetY.floatValue = value
        settingsRepository.setIslandCameraOffsetY(value)
    }

    fun setIslandCameraSize(value: Float) {
        islandCameraSize.floatValue = value
        settingsRepository.setIslandCameraSize(value)
    }

    fun setIslandMaxWidth(value: Float) {
        islandMaxWidth.floatValue = value
        settingsRepository.setIslandMaxWidth(value)
    }

    fun setIslandCutoutGap(value: Float) {
        islandCutoutGap.floatValue = value
        settingsRepository.setIslandCutoutGap(value)
    }

    fun setIslandExpandedWidth(value: Float) {
        islandExpandedWidth.floatValue = value
        settingsRepository.setIslandExpandedWidth(value)
    }

    fun setIslandDevicesBatteryOrder(addresses: List<String>) {
        islandDevicesBatteryOrder.value = addresses
        settingsRepository.setIslandDevicesBatteryOrder(addresses)
    }

    fun setIslandBriefShowAlarm(enabled: Boolean) {
        isIslandBriefShowAlarm.value = enabled
        settingsRepository.setIslandBriefShowAlarmEnabled(enabled)
    }

    fun setIslandBriefEnabled(enabled: Boolean) {
        isIslandBriefEnabled.value = enabled
        settingsRepository.setIslandBriefEnabled(enabled)
    }

    fun setIslandShowDevices(enabled: Boolean) {
        isIslandShowDevices.value = enabled
        settingsRepository.setIslandShowDevicesEnabled(enabled)
    }

    fun setIslandShowNavigation(enabled: Boolean) {
        isIslandShowNavigation.value = enabled
        settingsRepository.setIslandShowNavigationEnabled(enabled)
    }

    fun setIslandNavigationOnNew(mode: String) {
        islandNavigationOnNew.value = mode
        settingsRepository.setIslandNavigationOnNew(mode)
    }

    fun setIslandNavigationExpandDistance(meters: Int) {
        islandNavigationExpandDistance.intValue = meters
        settingsRepository.setIslandNavigationExpandDistance(meters)
    }

    fun setIslandNavigationPeekAfterExpand(enabled: Boolean) {
        isIslandNavigationPeekAfterExpand.value = enabled
        settingsRepository.setIslandNavigationPeekAfterExpandEnabled(enabled)
    }

    fun setIslandShowHotspot(enabled: Boolean) {
        isIslandShowHotspot.value = enabled
        settingsRepository.setIslandShowHotspotEnabled(enabled)
    }

    fun setIslandShowCaffeinate(enabled: Boolean) {
        isIslandShowCaffeinate.value = enabled
        settingsRepository.setIslandShowCaffeinateEnabled(enabled)
    }

    fun setIslandShowAlarm(enabled: Boolean) {
        isIslandShowAlarm.value = enabled
        settingsRepository.setIslandShowAlarmEnabled(enabled)
    }

    fun setIslandAlarmWindowHours(hours: Int) {
        islandAlarmWindowHours.intValue = hours
        settingsRepository.setIslandAlarmWindowHours(hours)
    }

    fun setIslandShowSoundMode(enabled: Boolean) {
        isIslandShowSoundMode.value = enabled
        settingsRepository.setIslandShowSoundModeEnabled(enabled)
    }

    fun setIslandSoundModeKeepIcon(enabled: Boolean) {
        isIslandSoundModeKeepIcon.value = enabled
        settingsRepository.setIslandSoundModeKeepIconEnabled(enabled)
    }

    fun setIslandShowSignal(enabled: Boolean) {
        isIslandShowSignal.value = enabled
        settingsRepository.setIslandShowSignalEnabled(enabled)
    }

    fun setIslandShowNetwork(enabled: Boolean) {
        isIslandShowNetwork.value = enabled
        settingsRepository.setIslandShowNetworkEnabled(enabled)
    }

    fun loadIslandCalendarEmojis() {
        islandCalendarEmojis.value = settingsRepository.getIslandCalendarEmojis()
    }

    fun setIslandCalendarEmoji(calendarId: Long, emoji: String?) {
        settingsRepository.setIslandCalendarEmoji(calendarId, emoji)
        islandCalendarEmojis.value = settingsRepository.getIslandCalendarEmojis()
    }

    fun setIslandShowTimers(enabled: Boolean) {
        isIslandShowTimers.value = enabled
        settingsRepository.setIslandShowTimersEnabled(enabled)
    }

    fun setIslandTimersShowScreenRecorder(enabled: Boolean) {
        isIslandTimersShowScreenRecorder.value = enabled
        settingsRepository.setIslandTimersShowScreenRecorderEnabled(enabled)
    }

    fun setIslandTimersFilterApps(enabled: Boolean) {
        isIslandTimersFilterApps.value = enabled
        settingsRepository.setIslandTimersFilterAppsEnabled(enabled)
    }

    fun loadIslandTimersSelectedApps(context: Context): List<AppSelection> = settingsRepository.loadIslandTimersSelectedApps()

    fun saveIslandTimersSelectedApps(context: Context, apps: List<AppSelection>) {
        settingsRepository.saveIslandTimersSelectedApps(apps)
    }

    fun updateIslandTimersAppEnabled(context: Context, packageName: String, enabled: Boolean) {
        settingsRepository.updateIslandTimersAppSelection(packageName, enabled)
    }

    fun setIslandShowCalls(enabled: Boolean) {
        isIslandShowCalls.value = enabled
        settingsRepository.setIslandShowCallsEnabled(enabled)
    }

    fun setIslandCameraPosition(value: String) {
        islandCameraPosition.value = value
        settingsRepository.setIslandCameraPosition(value)
    }

    fun setIslandExpandedScale(value: Float) {
        islandExpandedScale.floatValue = value
        settingsRepository.setIslandExpandedScale(value)
    }

    fun setIslandHideLiveUpdates(enabled: Boolean) {
        isIslandHideLiveUpdates.value = enabled
        settingsRepository.setIslandHideLiveUpdatesEnabled(enabled)
    }

    fun setIslandHideStatusBarInsets(
        enabled: Boolean,
        context: Context,
    ) {
        isIslandHideStatusBarInsets.value = enabled
        settingsRepository.setIslandHideStatusBarInsetsEnabled(enabled)
        if (enabled) setIslandHideLiveUpdates(false)
        val appContext = context.applicationContext
        viewModelScope.launch(Dispatchers.IO) {
            StatusBarInsetsUtils.apply(appContext, enabled)
        }
    }

    fun setIslandDismissOnOutside(enabled: Boolean) {
        isIslandDismissOnOutside.value = enabled
        settingsRepository.setIslandDismissOnOutsideEnabled(enabled)
    }

    fun setIslandHideInOwnerApp(enabled: Boolean) {
        isIslandHideInOwnerApp.value = enabled
        settingsRepository.setIslandHideInOwnerAppEnabled(enabled)
    }

    fun setIslandLandscapeTopSpacing(value: Float) {
        islandLandscapeTopSpacing.floatValue = value
        settingsRepository.setIslandLandscapeTopSpacing(value)
    }

    fun setIslandAlwaysGestures(enabled: Boolean) {
        isIslandAlwaysGestures.value = enabled
        settingsRepository.setIslandAlwaysGesturesEnabled(enabled)
    }

    fun setIslandMaxItems(value: Int) {
        islandMaxItems.value = value
        settingsRepository.setIslandMaxItems(value)
    }

    fun setIslandSecondaryBubble(enabled: Boolean) {
        isIslandSecondaryBubble.value = enabled
        settingsRepository.setIslandSecondaryBubbleEnabled(enabled)
    }

    fun setIslandBalanceCompact(enabled: Boolean) {
        isIslandBalanceCompact.value = enabled
        settingsRepository.setIslandBalanceCompactEnabled(enabled)
    }

    fun setIslandBondEdge(enabled: Boolean) {
        isIslandBondEdge.value = enabled
        settingsRepository.setIslandBondEdgeEnabled(enabled)
    }

    fun setIslandKeepInFullscreen(enabled: Boolean) {
        isIslandKeepInFullscreen.value = enabled
        settingsRepository.setIslandKeepInFullscreenEnabled(enabled)
    }

    fun setIslandDisableInteractionsInFullscreen(enabled: Boolean) {
        isIslandDisableInteractionsInFullscreen.value = enabled
        settingsRepository.setIslandDisableInteractionsInFullscreenEnabled(enabled)
    }

    fun setIslandKeepOnLandscape(enabled: Boolean) {
        isIslandKeepOnLandscape.value = enabled
        settingsRepository.setIslandKeepOnLandscapeEnabled(enabled)
    }

    fun setIslandHideOnShade(enabled: Boolean) {
        isIslandHideOnShade.value = enabled
        settingsRepository.setIslandHideOnShadeEnabled(enabled)
    }

    fun setIslandFontScale(value: Float) {
        islandFontScale.floatValue = value
        settingsRepository.setIslandFontScale(value)
    }

    fun setIslandExpandedRoundness(value: Float) {
        islandExpandedRoundness.floatValue = value
        settingsRepository.setIslandExpandedRoundness(value)
    }

    fun setIslandExpandedPadding(value: Float) {
        islandExpandedPadding.floatValue = value
        settingsRepository.setIslandExpandedPadding(value)
    }

    fun setIslandExpandedTopPadding(value: Float) {
        islandExpandedTopPadding.floatValue = value
        settingsRepository.setIslandExpandedTopPadding(value)
    }

    fun setIslandExpandedBottomPadding(value: Float) {
        islandExpandedBottomPadding.floatValue = value
        settingsRepository.setIslandExpandedBottomPadding(value)
    }

    fun setIslandExpandedTimeoutMs(value: Long) {
        islandExpandedTimeoutMs.longValue = value
        settingsRepository.setIslandExpandedTimeoutMs(value)
    }

    fun setIslandSuppressSystemHeadsUp(enabled: Boolean) {
        isIslandSuppressSystemHeadsUp.value = enabled
        settingsRepository.setIslandSuppressSystemHeadsUpEnabled(enabled)
    }

    fun setIslandCompactHideStatusBar(enabled: Boolean) {
        isIslandCompactHideStatusBar.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_ISLAND_COMPACT_HIDE_STATUS_BAR, enabled)
    }

    fun setIslandDynamicHideStatusBar(
        enabled: Boolean,
        context: Context,
    ) {
        isIslandDynamicHideStatusBar.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR,
            enabled,
        )
        if (!enabled) {
            IslandStatusBarHider.restore(context)
        }
    }

    fun setIslandHideWhenScreenOff(enabled: Boolean) {
        isIslandHideWhenScreenOff.value = enabled
        settingsRepository.setIslandHideWhenScreenOffEnabled(enabled)
    }

    fun setIslandTimeoutMs(value: Long) {
        islandTimeoutMs.longValue = value
        settingsRepository.setIslandTimeoutMs(value)
    }

    fun setIslandLineStageEnabled(enabled: Boolean) {
        isIslandLineStageEnabled.value = enabled
        settingsRepository.setIslandLineStageEnabled(enabled)
    }

    fun setIslandMediaPeekSongChange(enabled: Boolean) {
        isIslandMediaPeekSongChange.value = enabled
        settingsRepository.setIslandMediaPeekSongChangeEnabled(enabled)
    }

    fun setIslandMediaKeepWhenPaused(enabled: Boolean) {
        isIslandMediaKeepWhenPaused.value = enabled
        settingsRepository.setIslandMediaKeepWhenPausedEnabled(enabled)
    }

    fun setIslandMediaShowPrevious(enabled: Boolean) {
        isIslandMediaShowPrevious.value = enabled
        settingsRepository.setIslandMediaShowPreviousEnabled(enabled)
    }

    fun setIslandMediaShowLike(enabled: Boolean) {
        isIslandMediaShowLike.value = enabled
        settingsRepository.setIslandMediaShowLikeEnabled(enabled)
    }

    fun setIslandShowNotifications(enabled: Boolean) {
        isIslandShowNotifications.value = enabled
        settingsRepository.setIslandShowNotificationsEnabled(enabled)
    }

    fun setIslandNotifHub(enabled: Boolean) {
        if (enabled) settingsRepository.setIslandShowNotificationsEnabled(true)
        settingsRepository.setIslandNotifHubEnabled(enabled)
        isIslandShowNotifications.value = settingsRepository.isIslandShowNotificationsEnabled()
        syncIslandNotifHubState()
    }

    fun setIslandNotifHubIncludeSilent(enabled: Boolean) {
        isIslandNotifHubIncludeSilent.value = enabled
        settingsRepository.setIslandNotifHubIncludeSilent(enabled)
    }

    fun setIslandNotifHubGroupByApps(enabled: Boolean) {
        isIslandNotifHubGroupByApps.value = enabled
        settingsRepository.setIslandNotifHubGroupByApps(enabled)
    }

    fun setIslandNotifHubOrder(value: String) {
        islandNotifHubOrder.value = value
        settingsRepository.setIslandNotifHubOrder(value)
    }

    fun setIslandNotifHubIcons(value: Int) {
        islandNotifHubIcons.intValue = value
        settingsRepository.setIslandNotifHubIcons(value)
    }

    private fun syncIslandNotifHubState() {
        isIslandNotifHub.value = settingsRepository.isIslandNotifHubEnabled()
        isIslandNotifQueue.value = settingsRepository.isIslandNotifQueueEnabled()
        isIslandCatchUpEnabled.value = settingsRepository.isIslandCatchUpEnabled()
        islandCatchUpTimeoutMs.longValue = settingsRepository.getIslandCatchUpStoredMs()
    }

    fun setIslandNotifQueueInline(enabled: Boolean) {
        isIslandNotifQueueInline.value = enabled
        settingsRepository.setIslandNotifQueueInlineEnabled(enabled)
    }

    fun setIslandNotifQueueStyle(style: String) {
        islandNotifQueueStyle.value = style
        settingsRepository.setIslandNotifQueueStyle(style)
    }

    fun setIslandNotifQueue(enabled: Boolean) {
        isIslandNotifQueue.value = enabled
        settingsRepository.setIslandNotifQueueEnabled(enabled)
    }

    fun setIslandNotifSkipSilent(enabled: Boolean) {
        isIslandNotifSkipSilent.value = enabled
        settingsRepository.setIslandNotifSkipSilentEnabled(enabled)
    }

    fun setIslandNotifFollowDnd(enabled: Boolean) {
        isIslandNotifFollowDnd.value = enabled
        settingsRepository.setIslandNotifFollowDndEnabled(enabled)
    }

    fun setIslandNotifFilterApps(enabled: Boolean) {
        isIslandNotifFilterApps.value = enabled
        settingsRepository.setIslandNotifFilterAppsEnabled(enabled)
    }

    fun loadIslandNotifFilterApps(): List<AppSelection> = settingsRepository.loadIslandNotifFilterApps()

    fun saveIslandNotifFilterApps(apps: List<AppSelection>) {
        settingsRepository.saveIslandNotifFilterApps(apps)
    }

    fun updateIslandNotifFilterApp(packageName: String, enabled: Boolean) {
        settingsRepository.updateIslandNotifFilterAppSelection(packageName, enabled)
    }

    fun setIslandNotifTapToOpen(enabled: Boolean) {
        isIslandNotifTapToOpen.value = enabled
        settingsRepository.setIslandNotifTapToOpenEnabled(enabled)
    }

    fun setIslandNotifKeepProgress(enabled: Boolean) {
        isIslandNotifKeepProgress.value = enabled
        settingsRepository.setIslandNotifKeepProgressEnabled(enabled)
        if (enabled && combinedActive()) {
            isDuoShowProgress.value = false
            duoSettings.setDuoShowProgressEnabled(false)
        }
    }

    fun setIslandNotifPeekStyle(style: String) {
        islandNotifPeekStyle.value = style
        settingsRepository.setIslandNotifPeekStyle(style)
    }

    fun setIslandCatchUpEnabled(enabled: Boolean) {
        isIslandCatchUpEnabled.value = enabled
        settingsRepository.setIslandCatchUpEnabled(enabled)
    }

    fun setIslandCatchUpTimeoutMs(value: Long) {
        islandCatchUpTimeoutMs.longValue = value
        settingsRepository.setIslandCatchUpTimeoutMs(value)
    }

    fun setIslandOtpEnabled(enabled: Boolean) {
        isIslandOtpEnabled.value = enabled
        settingsRepository.setIslandOtpEnabled(enabled)
    }

    fun setIslandOtpTimeoutMs(value: Long) {
        islandOtpTimeoutMs.longValue = value
        settingsRepository.setIslandOtpTimeoutMs(value)
    }

    fun setIslandShowGlow(enabled: Boolean) {
        isIslandShowGlow.value = enabled
        settingsRepository.setIslandShowGlowEnabled(enabled)
    }

    fun setIslandBorderOutline(enabled: Boolean) {
        isIslandBorderOutline.value = enabled
        settingsRepository.setIslandBorderOutlineEnabled(enabled)
    }

    fun setIslandBorderOutlineColor(colorHex: String) {
        islandBorderOutlineColor.value = colorHex
        settingsRepository.setIslandBorderOutlineColor(colorHex)
    }

    fun setIslandBorderOutlineDynamic(enabled: Boolean) {
        isIslandBorderOutlineDynamic.value = enabled
        settingsRepository.setIslandBorderOutlineDynamicEnabled(enabled)
    }

    fun setIslandBorderOutlineThickness(value: Float) {
        islandBorderOutlineThickness.floatValue = value
        settingsRepository.setIslandBorderOutlineThickness(value)
    }

    fun setIslandBorderOutlineHiddenWhenExpanded(hidden: Boolean) {
        isIslandBorderOutlineHiddenWhenExpanded.value = hidden
        settingsRepository.setIslandBorderOutlineHiddenWhenExpanded(hidden)
    }

    fun setIslandPulseShadow(enabled: Boolean) {
        isIslandPulseShadow.value = enabled
        settingsRepository.setIslandPulseShadowEnabled(enabled)
    }

    fun setIslandPulseShadowSize(value: Float) {
        islandPulseShadowSize.floatValue = value
        settingsRepository.setIslandPulseShadowSize(value)
    }

    fun setIslandPulseShadowYShift(value: Float) {
        islandPulseShadowYShift.floatValue = value
        settingsRepository.setIslandPulseShadowYShift(value)
    }

    fun setIslandPulseShadowSpread(value: Float) {
        islandPulseShadowSpread.floatValue = value
        settingsRepository.setIslandPulseShadowSpread(value)
    }

    fun setIslandPulseShadowDurationMs(value: Float) {
        islandPulseShadowDurationMs.floatValue = value
        settingsRepository.setIslandPulseShadowDurationMs(value)
    }

    fun setIslandShowMedia(enabled: Boolean) {
        isIslandShowMedia.value = enabled
        settingsRepository.setIslandShowMediaEnabled(enabled)
        if (enabled && combinedActive()) {
            isDuoShowMedia.value = false
            duoSettings.setDuoShowMediaEnabled(false)
        }
    }

    fun setIslandShowCalendar(enabled: Boolean) {
        isIslandShowCalendar.value = enabled
        settingsRepository.setIslandShowCalendarEnabled(enabled)
    }

    fun setIslandShowConsciousGate(enabled: Boolean) {
        isIslandShowConsciousGate.value = enabled
        settingsRepository.setIslandShowConsciousGateEnabled(enabled)
    }

    fun setIslandShowFlashlight(enabled: Boolean) {
        isIslandShowFlashlight.value = enabled
        settingsRepository.setIslandShowFlashlightEnabled(enabled)
        if (enabled && combinedActive()) {
            isDuoShowFlashlight.value = false
            duoSettings.setDuoShowFlashlightEnabled(false)
        }
    }

    fun setIslandShowTimeBattery(enabled: Boolean) {
        isIslandShowTimeBattery.value = enabled
        settingsRepository.setIslandShowTimeBatteryEnabled(enabled)
        if (enabled && combinedActive()) {
            isDuoShowTime.value = false
            duoSettings.setDuoShowTimeEnabled(false)
        }
    }

    fun setIslandBatteryStyle(value: String) {
        islandBatteryStyle.value = value
        settingsRepository.setIslandBatteryStyle(value)
    }

    fun setIslandDevicesBatteryStyle(value: String) {
        islandDevicesBatteryStyle.value = value
        settingsRepository.setIslandDevicesBatteryStyle(value)
    }

    fun setIslandShowBattery(enabled: Boolean) {
        isIslandShowBattery.value = enabled
        settingsRepository.setIslandShowBatteryEnabled(enabled)
    }

    fun setIslandBatteryPeekAlerts(enabled: Boolean) {
        isIslandBatteryPeekAlerts.value = enabled
        settingsRepository.setIslandBatteryPeekAlertsEnabled(enabled)
    }

    fun setIslandBatteryPercentageEnabled(enabled: Boolean) {
        isIslandBatteryPercentageEnabled.value = enabled
        settingsRepository.setIslandBatteryPercentageEnabled(enabled)
    }

    fun setIslandDevicesBatteryOnlyLow(enabled: Boolean) {
        isIslandDevicesBatteryOnlyLow.value = enabled
        settingsRepository.setIslandDevicesBatteryOnlyLowEnabled(enabled)
    }

    fun setIslandBatteryOnlyLow(enabled: Boolean) {
        isIslandBatteryOnlyLow.value = enabled
        settingsRepository.setIslandBatteryOnlyLowEnabled(enabled)
    }

    fun setIslandBatteryIconConditional(enabled: Boolean) {
        isIslandBatteryIconConditional.value = enabled
        settingsRepository.setIslandBatteryIconConditional(enabled)
    }

    fun setIslandBatteryPercentageConditional(enabled: Boolean) {
        isIslandBatteryPercentageConditional.value = enabled
        settingsRepository.setIslandBatteryPercentageConditional(enabled)
    }

    fun setIslandLongPressAction(action: Action?) {
        islandLongPressAction.value = action
        settingsRepository.setIslandLongPressAction(action)
    }

    fun setIslandSlideMode(mode: String) {
        islandSlideMode.value = mode
        settingsRepository.setIslandSlideMode(mode)
    }

    fun setIslandLikeWhilePlayingEnabled(enabled: Boolean) {
        isIslandLikeWhilePlaying.value = enabled
        settingsRepository.setIslandLikeWhilePlayingEnabled(enabled)
    }

    fun setIslandSlideTrackEnabled(enabled: Boolean) {
        isIslandSlideTrack.value = enabled
        settingsRepository.setIslandSlideTrackEnabled(enabled)
    }

    fun setIslandSlideInvertDirection(enabled: Boolean) {
        isIslandSlideInvertDirection.value = enabled
        settingsRepository.setIslandSlideInvertDirection(enabled)
    }

    fun loadIslandMediaApps(context: Context): List<AppSelection> = settingsRepository.loadIslandMediaExcludedApps()

    fun saveIslandMediaApps(context: Context, apps: List<AppSelection>) {
        settingsRepository.saveIslandMediaExcludedApps(apps)
    }

    fun updateIslandMediaAppEnabled(context: Context, packageName: String, enabled: Boolean) {
        settingsRepository.updateIslandMediaExcludedAppSelection(packageName, enabled)
    }

    fun triggerIslandPreview(context: Context) {
        val alert = TestNotificationUtil.generateRandomNotification(context)
        com.jawzf.pixeltoolkit.services.NotificationListener.notifyAlertPosted(alert)
    }

    fun setDuoHideWhenScreenOff(enabled: Boolean) {
        isDuoHideWhenScreenOff.value = enabled
        duoSettings.setDuoHideWhenScreenOffEnabled(enabled)
    }

    fun setDuoHideWhenScreenOffOnlyIdle(enabled: Boolean) {
        isDuoHideWhenScreenOffOnlyIdle.value = enabled
        duoSettings.setDuoHideWhenScreenOffOnlyIdleEnabled(enabled)
    }

    fun setDuoHideWhenLocked(enabled: Boolean) {
        isDuoHideWhenLocked.value = enabled
        duoSettings.setDuoHideWhenLockedEnabled(enabled)
    }

    fun setDuoUseMaterialYou(enabled: Boolean) {
        isDuoUseMaterialYou.value = enabled
        duoSettings.setDuoUseMaterialYouEnabled(enabled)
    }

    fun setDuoCustomColor(colorHex: String) {
        duoCustomColor.value = colorHex
        duoSettings.setDuoCustomColor(colorHex)
    }

    fun setDuoTapAction(action: Action?) {
        duoTapAction.value = action
        duoSettings.setDuoTapAction(action)
    }

    fun setDuoDoubleTapAction(action: Action?) {
        duoDoubleTapAction.value = action
        duoSettings.setDuoDoubleTapAction(action)
    }

    fun setDuoLongPressAction(action: Action?) {
        duoLongPressAction.value = action
        duoSettings.setDuoLongPressAction(action)
    }

    fun setDuoSwipeDownAction(action: Action?) {
        duoSwipeDownAction.value = action
        duoSettings.setDuoSwipeDownAction(action)
    }

    fun setDuoSlideMode(mode: String) {
        duoSlideMode.value = mode
        duoSettings.setDuoSlideMode(mode)
    }

    fun setDuoSlideTrackEnabled(enabled: Boolean) {
        isDuoSlideTrack.value = enabled
        duoSettings.setDuoSlideTrackEnabled(enabled)
    }

    fun setDuoSlideInvertDirection(enabled: Boolean) {
        isDuoSlideInvertDirection.value = enabled
        duoSettings.setDuoSlideInvertDirection(enabled)
    }

    fun setStatusGlanceEnabled(enabled: Boolean) {
        isStatusGlanceEnabled.value = enabled
        settingsRepository.setStatusGlanceEnabled(enabled)
    }

    fun setStatusGlanceAutoDetect(enabled: Boolean) {
        isStatusGlanceAutoDetect.value = enabled
        settingsRepository.setStatusGlanceAutoDetectEnabled(enabled)
    }

    fun refreshStatusGlanceCameraPlacement() {
        isFoldableCameraDevice.value = duoSettings.isFoldableDevice()
        isStatusGlanceOrientationProfiles.value = settingsRepository.isStatusGlanceOrientationProfilesEnabled()
        isStatusGlanceHiddenInCurrentOrientation.value = settingsRepository.isStatusGlanceHiddenInCurrentOrientation()
        statusGlanceOffsetX.floatValue = settingsRepository.getStatusGlanceOffsetX()
        statusGlanceOffsetY.floatValue = settingsRepository.getStatusGlanceOffsetY()
    }

    fun setStatusGlanceOrientationProfiles(enabled: Boolean) {
        settingsRepository.setStatusGlanceOrientationProfilesEnabled(enabled)
        refreshStatusGlanceCameraPlacement()
    }

    fun setStatusGlanceHiddenInCurrentOrientation(hidden: Boolean) {
        settingsRepository.setStatusGlanceHiddenInCurrentOrientation(hidden)
        isStatusGlanceHiddenInCurrentOrientation.value = hidden
    }

    fun setStatusGlanceOffsetX(value: Float) {
        statusGlanceOffsetX.floatValue = value
        settingsRepository.setStatusGlanceOffsetX(value)
    }

    fun setStatusGlanceOffsetY(value: Float) {
        statusGlanceOffsetY.floatValue = value
        settingsRepository.setStatusGlanceOffsetY(value)
    }

    fun setStatusGlanceShowFlashlight(enabled: Boolean) {
        isStatusGlanceShowFlashlight.value = enabled
        settingsRepository.setStatusGlanceShowFlashlightEnabled(enabled)
    }

    fun setStatusGlanceShowCalendar(enabled: Boolean) {
        isStatusGlanceShowCalendar.value = enabled
        settingsRepository.setStatusGlanceShowCalendarEnabled(enabled)
    }

    fun setStatusGlanceCalendarTimeframe(timeframe: String) {
        statusGlanceCalendarTimeframe.value = timeframe
        settingsRepository.setStatusGlanceCalendarTimeframe(timeframe)
    }

    fun setStatusGlanceCalendarShowAllDay(enabled: Boolean) {
        statusGlanceCalendarShowAllDay.value = enabled
        settingsRepository.setStatusGlanceCalendarShowAllDayEnabled(enabled)
    }

    fun fetchStatusGlanceCalendars(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val savedSelected = settingsRepository.getStatusGlanceCalendarSelectedCalendars()
            withContext(Dispatchers.Main) {
                statusGlanceSelectedCalendarIds.value = savedSelected
            }

            val calendars = mutableListOf<CalendarAccount>()
            val projection =
                arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.ACCOUNT_NAME,
                    CalendarContract.Calendars.CALENDAR_COLOR,
                )

            context.contentResolver
                .query(
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    val nameColumn = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                    val accountColumn = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn) ?: "Unnamed Calendar"
                        val account = cursor.getString(accountColumn) ?: "Local"

                        calendars.add(
                            CalendarAccount(
                                id,
                                name,
                                account,
                                savedSelected.isEmpty() || savedSelected.contains(id.toString()),
                            ),
                        )
                    }
                }

            withContext(Dispatchers.Main) {
                statusGlanceAvailableCalendars.clear()
                statusGlanceAvailableCalendars.addAll(calendars)
            }
        }
    }

    fun toggleStatusGlanceCalendarSelection(calendarId: Long) {
        val currentIds = statusGlanceSelectedCalendarIds.value.toMutableSet()
        val idString = calendarId.toString()
        if (currentIds.contains(idString)) {
            currentIds.remove(idString)
        } else {
            currentIds.add(idString)
        }
        statusGlanceSelectedCalendarIds.value = currentIds
        settingsRepository.saveStatusGlanceCalendarSelectedCalendars(currentIds)

        val index = statusGlanceAvailableCalendars.indexOfFirst { it.id == calendarId }
        if (index != -1) {
            statusGlanceAvailableCalendars[index] =
                statusGlanceAvailableCalendars[index].copy(isSelected = currentIds.contains(idString))
        }
    }

    fun setStatusGlanceShowMedia(enabled: Boolean) {
        isStatusGlanceShowMedia.value = enabled
        settingsRepository.setStatusGlanceShowMediaEnabled(enabled)
    }

    fun setStatusGlanceShowTime(enabled: Boolean) {
        isStatusGlanceShowTime.value = enabled
        settingsRepository.setStatusGlanceShowTimeEnabled(enabled)
    }

    fun setStatusGlanceBackgroundPill(enabled: Boolean) {
        isStatusGlanceBackgroundPill.value = enabled
        settingsRepository.setStatusGlanceBackgroundPillEnabled(enabled)
    }

    fun setStatusGlanceAlbumArtColors(enabled: Boolean) {
        isStatusGlanceAlbumArtColors.value = enabled
        settingsRepository.setStatusGlanceAlbumArtColorsEnabled(enabled)
    }

    fun setStatusGlanceHideWhenFullscreen(enabled: Boolean) {
        isStatusGlanceHideWhenFullscreen.value = enabled
        settingsRepository.setStatusGlanceHideWhenFullscreenEnabled(enabled)
    }

    fun setStatusGlanceHideInQuickSettings(enabled: Boolean) {
        isStatusGlanceHideInQuickSettings.value = enabled
        settingsRepository.setStatusGlanceHideInQuickSettingsEnabled(enabled)
    }

    fun setStatusGlanceHideWhenLocked(enabled: Boolean) {
        isStatusGlanceHideWhenLocked.value = enabled
        settingsRepository.setStatusGlanceHideWhenLockedEnabled(enabled)
    }

    fun setStatusGlanceBoldText(enabled: Boolean) {
        isStatusGlanceBoldText.value = enabled
        settingsRepository.setStatusGlanceBoldTextEnabled(enabled)
    }

    fun setStatusGlanceShowBattery(enabled: Boolean) {
        isStatusGlanceShowBattery.value = enabled
        settingsRepository.setStatusGlanceShowBatteryEnabled(enabled)
    }

    fun setStatusGlanceBatteryDisplayMode(mode: String) {
        statusGlanceBatteryDisplayMode.value = mode
        settingsRepository.setStatusGlanceBatteryDisplayMode(mode)
    }

    fun setStatusGlanceBatteryShowLow(enabled: Boolean) {
        isStatusGlanceBatteryShowLow.value = enabled
        settingsRepository.setStatusGlanceBatteryShowLowEnabled(enabled)
    }

    fun setStatusGlanceBatteryShowCharging(enabled: Boolean) {
        isStatusGlanceBatteryShowCharging.value = enabled
        settingsRepository.setStatusGlanceBatteryShowChargingEnabled(enabled)
    }

    fun setStatusGlanceBatteryShowFull(enabled: Boolean) {
        isStatusGlanceBatteryShowFull.value = enabled
        settingsRepository.setStatusGlanceBatteryShowFullEnabled(enabled)
    }

    fun setStatusGlanceBatteryShowOtherwise(enabled: Boolean) {
        isStatusGlanceBatteryShowOtherwise.value = enabled
        settingsRepository.setStatusGlanceBatteryShowOtherwiseEnabled(enabled)
    }

    fun setStatusGlanceBatteryIconSize(size: Float) {
        statusGlanceBatteryIconSize.floatValue = size
        settingsRepository.setStatusGlanceBatteryIconSize(size)
    }

    fun setStatusGlanceTapAction(action: Action?) {
        statusGlanceTapAction.value = action
        settingsRepository.setStatusGlanceTapAction(action)
    }

    fun setStatusGlanceDoubleTapAction(action: Action?) {
        statusGlanceDoubleTapAction.value = action
        settingsRepository.setStatusGlanceDoubleTapAction(action)
    }

    fun setStatusGlanceLongPressAction(action: Action?) {
        statusGlanceLongPressAction.value = action
        settingsRepository.setStatusGlanceLongPressAction(action)
    }

    fun setStatusGlanceMaxWidth(value: Float) {
        statusGlanceMaxWidth.floatValue = value
        settingsRepository.setStatusGlanceMaxWidth(value)
    }

    fun setStatusGlanceFontSize(value: Float) {
        statusGlanceFontSize.floatValue = value
        settingsRepository.setStatusGlanceFontSize(value)
    }

    fun autoDetectStatusGlancePosition(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val dm = context.resources.displayMetrics
        val screenWidth = dm.widthPixels.toFloat()
        val screenHeight = dm.heightPixels.toFloat()
        val density = dm.density

        var detectedXPercent = 60f
        var detectedYPercent = 2f

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            val cutout = wm.defaultDisplay?.cutout
            if (cutout != null && cutout.boundingRects.isNotEmpty()) {
                val targetRect = cutout.boundingRects.firstOrNull()
                if (targetRect != null) {
                    detectedXPercent = ((targetRect.right + 8f * density) / screenWidth) * 100f
                    detectedYPercent = (targetRect.centerY().toFloat() / screenHeight) * 100f
                }
            } else {
                val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
                val statusBarHeight = if (resourceId > 0) context.resources.getDimensionPixelSize(resourceId).toFloat() else 24f * density
                detectedXPercent = 50f
                detectedYPercent = ((statusBarHeight / 2f) / screenHeight) * 100f
            }
        }

        setStatusGlanceOffsetX(detectedXPercent.coerceIn(0f, 100f))
        setStatusGlanceOffsetY(detectedYPercent.coerceIn(0f, 20f))
    }

    /**
     * Executes the set use usage access operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setHiddenDebuggingSupport(enabled: Boolean) {
        isHiddenDebuggingSupport.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_HIDDEN_DEBUGGING_SUPPORT, enabled)
    }

    fun setUseUsageAccess(
        enabled: Boolean,
        context: Context,
    ) {
        isUseUsageAccess.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_USE_USAGE_ACCESS, enabled)
        updateAppDetectionService(context)
    }

    private fun updateAppDetectionService(context: Context) {
        com.jawzf.pixeltoolkit.utils.ServiceUtils
            .startRequiredServices(context)
    }

    val isLikeSongToastEnabled = mutableStateOf(false)

    /**
     * Executes the set like song toast enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setLikeSongToastEnabled(enabled: Boolean) {
        isLikeSongToastEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_LIKE_SONG_TOAST_ENABLED, enabled)
    }

    val isLikeSongAodOverlayEnabled = mutableStateOf(false)

    /**
     * Executes the set like song aod overlay enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setLikeSongAodOverlayEnabled(enabled: Boolean) {
        isLikeSongAodOverlayEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_LIKE_SONG_AOD_OVERLAY_ENABLED, enabled)
    }

    /**
     * Executes the switch scale animations mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun switchScaleAnimationsMode(mode: String) {
        val oldMode = scaleAnimationsMode.value
        if (oldMode == mode) return

        // 1. Save current state to old profile slot
        val currentProfile =
            ScaleAnimationsProfile(
                fontScale = fontScale.floatValue,
                fontWeight = fontWeight.intValue,
                animatorDurationScale = animatorDurationScale.floatValue,
                transitionAnimationScale = transitionAnimationScale.floatValue,
                windowAnimationScale = windowAnimationScale.floatValue,
                smallestWidth = smallestWidth.intValue,
                touchSensitivityEnabled = isTouchSensitivityEnabled.value,
                autoRotateEnabled = isAutoRotateEnabled.value,
                screenTimeout = screenTimeout.value,
            )
        settingsRepository.saveScaleAnimationsProfile(oldMode, currentProfile)

        // 2. Load new profile
        val newProfile = settingsRepository.getScaleAnimationsProfile(mode)

        // 3. Update mode
        scaleAnimationsMode.value = mode
        settingsRepository.setScaleAnimationsMode(mode)

        // 4. Apply new profile
        setFontScale(newProfile.fontScale)
        setFontWeight(newProfile.fontWeight)
        setAnimationScale(
            Settings.Global.ANIMATOR_DURATION_SCALE,
            newProfile.animatorDurationScale,
        )
        setAnimationScale(
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            newProfile.transitionAnimationScale,
        )
        setAnimationScale(
            Settings.Global.WINDOW_ANIMATION_SCALE,
            newProfile.windowAnimationScale,
        )
        setSmallestWidth(newProfile.smallestWidth)
        setTouchSensitivityEnabled(newProfile.touchSensitivityEnabled)
        setAutoRotateEnabled(newProfile.autoRotateEnabled)
        setScreenTimeout(newProfile.screenTimeout)
    }

    /**
     * Executes the set touch sensitivity enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setTouchSensitivityEnabled(enabled: Boolean) {
        isTouchSensitivityEnabled.value = enabled
        settingsRepository.setTouchSensitivityEnabled(enabled)
    }

    /**
     * Executes the set auto rotate enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAutoRotateEnabled(enabled: Boolean) {
        isAutoRotateEnabled.value = enabled
        settingsRepository.setAutoRotateEnabled(enabled)
    }

    /**
     * Executes the restart system ui operation.
     */
    fun restartSystemUI() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (ShizukuUtils.isShizukuAvailable() && ShizukuUtils.hasPermission()) {
                    ShizukuUtils.runCommand("am crash com.android.systemui")
                } else if (RootUtils.isRootAvailable() && RootUtils.isRootPermissionGranted()) {
                    RootUtils.runCommand("am crash com.android.systemui")
                }
            }
        }
    }

    /**
     * Executes the set screen timeout operation.
     *
     * @param timeoutMs [Long] Target timeout ms.
     */
    fun setScreenTimeout(timeoutMs: Long) {
        screenTimeout.value = timeoutMs
        settingsRepository.setScreenTimeout(timeoutMs)
    }

    /**
     * Executes the update font scale operation.
     *
     * @param scale [Float] Target scale.
     */
    fun updateFontScale(scale: Float) {
        fontScale.floatValue = scale
    }

    /**
     * Executes the save font scale operation.
     */
    fun saveFontScale() {
        settingsRepository.setFontScale(fontScale.floatValue)
    }

    /**
     * Executes the set font scale operation.
     *
     * @param scale [Float] Target scale.
     */
    fun setFontScale(scale: Float) {
        fontScale.floatValue = scale
        settingsRepository.setFontScale(scale)
    }

    /**
     * Executes the set font weight operation.
     *
     * @param weight [Int] Target weight.
     */
    fun setFontWeight(weight: Int) {
        fontWeight.intValue = weight
        settingsRepository.setFontWeight(weight)
    }

    /**
     * Executes the set animation scale operation.
     *
     * @param key [String] Target key.
     * @param scale [Float] Target scale.
     */
    fun setAnimationScale(
        key: String,
        scale: Float,
    ) {
        when (key) {
            Settings.Global.ANIMATOR_DURATION_SCALE ->
                animatorDurationScale.floatValue =
                    scale

            Settings.Global.TRANSITION_ANIMATION_SCALE ->
                transitionAnimationScale.floatValue =
                    scale

            Settings.Global.WINDOW_ANIMATION_SCALE ->
                windowAnimationScale.floatValue =
                    scale
        }
        settingsRepository.setAnimationScale(key, scale)
    }

    /**
     * Executes the reset text to default operation.
     */
    fun resetTextToDefault() {
        setFontScale(1.0f)
        setFontWeight(0)
    }

    /**
     * Executes the reset animations to default operation.
     */
    fun resetAnimationsToDefault() {
        setAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE, 1.0f)
        setAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE, 1.0f)
        setAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE, 1.0f)
    }

    /**
     * Executes the update smallest width operation.
     *
     * @param width [Int] Target width.
     */
    fun updateSmallestWidth(width: Int) {
        smallestWidth.intValue = width
    }

    /**
     * Executes the save smallest width operation.
     */
    fun saveSmallestWidth() {
        settingsRepository.setSmallestWidth(smallestWidth.intValue)
    }

    /**
     * Executes the set smallest width operation.
     *
     * @param width [Int] Target width.
     */
    fun setSmallestWidth(width: Int) {
        smallestWidth.intValue = width
        settingsRepository.setSmallestWidth(width)
    }

    /**
     * Executes the reset scale to default operation.
     */
    fun resetScaleToDefault() {
        settingsRepository.resetSmallestWidth()
        smallestWidth.intValue = settingsRepository.getSmallestWidth()
    }

    /**
     * Executes the preview flashlight pulse operation.
     *
     * @param context [Context] Target context.
     */
    fun previewFlashlightPulse(context: Context) {
        val intent =
            Intent(context, FlashlightActionReceiver::class.java).apply {
                action = FlashlightActionReceiver.ACTION_PULSE_NOTIFICATION
                putExtra(FlashlightActionReceiver.EXTRA_IS_PREVIEW, true)
            }
        context.sendBroadcast(intent)
    }

    /**
     * Executes the request write secure settings permission operation.
     *
     * @param context [Context] Target context.
     */
    fun requestWriteSecureSettingsPermission(context: Context) {
        val adbCommand =
            "adb shell pm grant ${context.packageName} android.permission.WRITE_SECURE_SETTINGS"
        val clipboard =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("adb_command", adbCommand)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Executes the request usage stats permission operation.
     *
     * @param context [Context] Target context.
     */
    fun requestUsageStatsPermission(context: Context) {
        PermissionUtils.openUsageStatsSettings(context)
    }

    /**
     * Executes the request write settings permission operation.
     *
     * @param context [Context] Target context.
     */
    fun requestWriteSettingsPermission(context: Context) {
        PermissionUtils.openWriteSettings(context)
    }

    /**
     * Executes the set haptic feedback operation.
     *
     * @param type [HapticFeedbackType] Target type.
     * @param context [Context] Target context.
     */
    fun setHapticFeedback(
        type: HapticFeedbackType,
        context: Context,
    ) {
        hapticFeedbackType.value = type
        settingsRepository.putString(SettingsRepository.KEY_HAPTIC_FEEDBACK_TYPE, type.name)
    }

    /**
     * Executes the set default tab operation.
     *
     * @param tab [com.jawzf.pixeltoolkit.domain.DIYTabs] Target tab.
     * @param context [Context] Target context.
     */
    fun setDefaultTab(
        tab: com.jawzf.pixeltoolkit.domain.DIYTabs,
        context: Context,
    ) {
        defaultTab.value = tab
        settingsRepository.saveDIYTab(tab)
    }

    fun setAppIcon(
        appIcon: AppIcon,
        context: Context,
    ) {
        selectedAppIcon.value = appIcon
        settingsRepository.setAppIcon(appIcon)
        AppIconUtil.setAppIcon(context, appIcon)
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean = PermissionUtils.isAccessibilityServiceEnabled(context)

    /**
     * Executes the can write secure settings operation.
     *
     * @param context [Context] Target context.
     * @return The resulting Boolean data.
     */
    fun canWriteSecureSettings(context: Context): Boolean = PermissionUtils.canWriteSecureSettings(context)

    /**
     * Executes the request read phone state permission operation.
     *
     * @param activity [androidx.activity.ComponentActivity] Target activity.
     */
    fun requestReadPhoneStatePermission(activity: androidx.activity.ComponentActivity) {
        PermissionUtils.requestPermissionsSafely(
            activity,
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            1001,
        )
    }

    /**
     * Executes the request location permission operation.
     *
     * @param activity [androidx.activity.ComponentActivity] Target activity.
     */
    fun requestLocationPermission(activity: androidx.activity.ComponentActivity) {
        PermissionUtils.requestPermissionsSafely(
            activity,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            1003,
        )
    }

    /**
     * Executes the request background location permission operation.
     *
     * @param activity [androidx.activity.ComponentActivity] Target activity.
     */
    fun requestBackgroundLocationPermission(activity: androidx.activity.ComponentActivity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            PermissionUtils.requestPermissionsSafely(
                activity,
                arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                1004,
            )
        }
    }

    /**
     * Executes the request bluetooth permission operation.
     *
     * @param activity [androidx.activity.ComponentActivity] Target activity.
     */
    fun requestBluetoothPermission(activity: androidx.activity.ComponentActivity) {
        PermissionUtils.requestPermissionsSafely(
            activity,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
            } else {
                arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
            },
            1005,
        )
    }

    /**
     * Executes the request calendar permission operation.
     *
     * @param activity [androidx.activity.ComponentActivity] Target activity.
     */
    fun requestCalendarPermission(activity: androidx.activity.ComponentActivity) {
        PermissionUtils.requestPermissionsSafely(
            activity,
            arrayOf(
                Manifest.permission.READ_CALENDAR,
                Manifest.permission.WRITE_CALENDAR,
            ),
            1006,
        )
    }

    /**
     * Executes the request notification permission operation.
     *
     * @param activity [androidx.activity.ComponentActivity] Target activity.
     */
    fun requestNotificationPermission(activity: androidx.activity.ComponentActivity) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.requestPermissionsSafely(
                activity,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                1002,
            )
        }
    }

    /**
     * Executes the request full screen intent permission operation.
     *
     * @param context [Context] Target context.
     */
    fun requestFullScreenIntentPermission(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback to special app access
                val intent = Intent(Settings.ACTION_CONDITION_PROVIDER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    private fun hasNotificationListenerPermission(context: Context): Boolean = PermissionUtils.hasNotificationListenerPermission(context)

    /**
     * Executes the request notification listener permission operation.
     *
     * @param context [Context] Target context.
     */
    fun requestNotificationListenerPermission(context: Context) {
        val intent =
            Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        context.startActivity(intent)
    }

    /**
     * Executes the request shizuku permission operation.
     */
    fun requestShizukuPermission() {
        ShizukuUtils.requestPermission()
        isShizukuPermissionGranted.value = ShizukuUtils.hasPermission()
    }

    /**
     * Executes the grant write secure settings with shizuku operation.
     *
     * @param context [Context] Target context.
     * @return The resulting Boolean data.
     */
    fun grantWriteSecureSettingsWithShizuku(context: Context): Boolean {
        val success = ShizukuUtils.grantWriteSecureSettingsPermission()
        if (success) {
            // Refresh the write secure settings check
            isWriteSecureSettingsEnabled.value = canWriteSecureSettings(context)
            isShizukuPermissionGranted.value = ShizukuUtils.hasPermission()
        }
        return success
    }

    private fun canDrawOverlays(context: Context): Boolean = PermissionUtils.canDrawOverlays(context)

    private fun isNotificationLightingAccessibilityServiceEnabled(context: Context): Boolean =
        PermissionUtils.isNotificationLightingAccessibilityServiceEnabled(context)

    private fun isDefaultBrowser(context: Context): Boolean = PermissionUtils.isDefaultBrowser(context)

    /**
     * Executes the set flashlight always turn off enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setFlashlightAlwaysTurnOffEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFlashlightAlwaysTurnOffEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_FLASHLIGHT_ALWAYS_TURN_OFF_ENABLED,
            enabled,
        )
    }

    /**
     * Executes the set flashlight pocket turn off enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setFlashlightPocketTurnOffEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFlashlightPocketTurnOffEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_FLASHLIGHT_POCKET_TURN_OFF_ENABLED,
            enabled,
        )
    }

    /**
     * Executes the set flashlight overheat enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setFlashlightOverheatEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFlashlightOverheatEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_FLASHLIGHT_OVERHEAT_PREVENTION_ENABLED,
            enabled,
        )
    }

    /**
     * Executes the set flashlight fade enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setFlashlightFadeEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFlashlightFadeEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_FLASHLIGHT_FADE_ENABLED, enabled)
    }

    /**
     * Executes the set flashlight adjust enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setFlashlightAdjustEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFlashlightAdjustEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_FLASHLIGHT_ADJUST_INTENSITY_ENABLED,
            enabled,
        )
    }

    /**
     * Executes the set flashlight global enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setFlashlightGlobalEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFlashlightGlobalEnabled.value = enabled
        settingsRepository.putBoolean(SettingsRepository.KEY_FLASHLIGHT_GLOBAL_ENABLED, enabled)
    }

    /**
     * Executes the set flashlight live update enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setFlashlightLiveUpdateEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFlashlightLiveUpdateEnabled.value = enabled
        settingsRepository.putBoolean(
            SettingsRepository.KEY_FLASHLIGHT_LIVE_UPDATE_ENABLED,
            enabled,
        )
    }

    /**
     * Executes the set flashlight last intensity operation.
     *
     * @param intensity [Int] Target intensity.
     * @param context [Context] Target context.
     */
    fun setFlashlightLastIntensity(
        intensity: Int,
        context: Context,
    ) {
        flashlightLastIntensity.value = intensity
        settingsRepository.putInt(SettingsRepository.KEY_FLASHLIGHT_LAST_INTENSITY, intensity)
    }

    /**
     * Executes the export configs operation.
     *
     * @param context [Context] Target context.
     * @param outputStream [java.io.OutputStream] Target output stream.
     */
    fun exportConfigs(
        context: Context,
        outputStream: java.io.OutputStream,
    ) {
        ConfigBackup(context).exportConfigs(outputStream)
    }

    fun importConfigs(
        context: Context,
        inputStream: java.io.InputStream,
        keepPrefs: Boolean,
    ): Boolean {
        val success = ConfigBackup(context).importConfigs(inputStream, keepPrefs)
        if (success) {
            settingsRepository.syncSystemSettingsWithSaved()
            com.jawzf.pixeltoolkit.domain.diy.DIYRepository
                .reloadAutomations()
            freeze.refreshFreezePickedApps(context, silent = true)
            check(context)
        }
        return success
    }

    /**
     * Executes the set auto accessibility enabled operation.
     *
     * @param isEnabled [Boolean] Target is enabled.
     * @param context [Context] Target context.
     */
    fun setAutoAccessibilityEnabled(
        isEnabled: Boolean,
        context: Context,
    ) {
        settingsRepository.putBoolean(SettingsRepository.KEY_AUTO_ACCESSIBILITY_ENABLED, isEnabled)
        isAutoAccessibilityEnabled.value = isEnabled
    }

    /**
     * Executes the generate bug report operation.
     *
     * @param context [Context] Target context.
     * @return The resulting String data.
     */
    fun generateBugReport(context: Context): String {
        return LogManager.generateReport(context)
    }

    override fun onCleared() {
        super.onCleared()
        appContext?.let { context ->
            try {
                context.contentResolver.unregisterContentObserver(contentObserver)
            } catch (e: Exception) {
            }
            try {
                powerSaveReceiver?.let { context.unregisterReceiver(it) }
            } catch (e: Exception) {
            }
        }
        if (::settingsRepository.isInitialized) {
            settingsRepository.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        }
    }

    /**
     * Executes the set onboarding completed operation.
     *
     * @param completed [Boolean] Target completed.
     * @param context [Context] Target context.
     */
    fun setOnboardingCompleted(
        completed: Boolean,
        context: Context,
    ) {
        isOnboardingCompleted.value = completed
        settingsRepository.putBoolean(SettingsRepository.KEY_ONBOARDING_COMPLETED, completed)
    }

    /**
     * Executes the reset onboarding operation.
     *
     * @param context [Context] Target context.
     */
    fun resetOnboarding(context: Context) {
        setOnboardingCompleted(false, context)
        // Reset tab to TOOLKIT
        setDefaultTab(com.jawzf.pixeltoolkit.domain.DIYTabs.TOOLKIT, context)
    }

    private fun updateAddedQSTiles(context: Context) {
        var tilesString = ""

        if (ShellUtils.hasPermission(context)) {
            try {
                tilesString = ShellUtils.runCommandWithOutput(context, "settings get secure sysui_qs_tiles", notifyOnError = false) ?: ""
                if (tilesString == "null") tilesString = ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val fromSecureSettings =
            tilesString
                .split(",")
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .toSet()

        val prefs = context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
        val fromPrefs =
            prefs.all
                .filter { (key, value) -> key.endsWith("_is_added") && value == true }
                .keys
                .map { it.removeSuffix("_is_added") }
                .toSet()

        addedQSTiles.value = fromSecureSettings + fromPrefs
    }

    val isLockdownModeEnabled = mutableStateOf(false)

    /**
     * Executes the toggle lockdown mode operation.
     */
    fun toggleLockdownMode() {
        settingsRepository.putBoolean(
            SettingsRepository.KEY_LOCKDOWN_MODE,
            !isLockdownModeEnabled.value,
        )
        isLockdownModeEnabled.value = !isLockdownModeEnabled.value
    }
}
