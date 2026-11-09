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
import com.jawzf.pixeltoolkit.core.data.display.ScaleAnimationsProfile
import com.jawzf.pixeltoolkit.core.data.display.TextAnimationsSettings
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.log.LogManager
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
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
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarManager
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.domain.model.AppIcon
import com.jawzf.pixeltoolkit.domain.model.SearchableItem
import com.jawzf.pixeltoolkit.domain.registry.SearchRegistry
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.apps.applock.AppLockState
import com.jawzf.pixeltoolkit.feature.apps.consciousgate.ConsciousGateState
import com.jawzf.pixeltoolkit.feature.apps.freeze.FreezeState
import com.jawzf.pixeltoolkit.feature.apps.updates.github.GitHubUser
import com.jawzf.pixeltoolkit.feature.connectivity.ConnectivitySettings
import com.jawzf.pixeltoolkit.feature.connectivity.ConnectivityState
import com.jawzf.pixeltoolkit.feature.display.aod.AodState
import com.jawzf.pixeltoolkit.feature.display.duo.DuoIslandLink
import com.jawzf.pixeltoolkit.feature.display.duo.DuoState
import com.jawzf.pixeltoolkit.feature.display.glance.StatusGlanceState
import com.jawzf.pixeltoolkit.feature.display.lockclock.LockClockState
import com.jawzf.pixeltoolkit.feature.display.nightlight.NightLightState
import com.jawzf.pixeltoolkit.feature.display.ondisplay.ToolkitOnDisplayState
import com.jawzf.pixeltoolkit.feature.display.pixels.SmartPixelsState
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRatePanelState
import com.jawzf.pixeltoolkit.feature.display.statusbar.StatusBarControlState
import com.jawzf.pixeltoolkit.feature.input.InputState
import com.jawzf.pixeltoolkit.feature.input.remap.RemapSettings
import com.jawzf.pixeltoolkit.feature.input.remap.RemapState
import com.jawzf.pixeltoolkit.feature.input.shortcuts.LockscreenShortcutSide
import com.jawzf.pixeltoolkit.feature.input.shortcuts.SystemShortcutsState
import com.jawzf.pixeltoolkit.feature.island.IslandDuoLink
import com.jawzf.pixeltoolkit.feature.island.IslandState
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.notifications.NotificationState
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.NotificationAutoDismisser
import com.jawzf.pixeltoolkit.feature.notifications.flashlight.FlashlightPulseState
import com.jawzf.pixeltoolkit.feature.notifications.hilight.HilightState
import com.jawzf.pixeltoolkit.feature.notifications.lighting.LightingState
import com.jawzf.pixeltoolkit.feature.power.BatteryState
import com.jawzf.pixeltoolkit.feature.power.PowerState
import com.jawzf.pixeltoolkit.feature.security.SecuritySettings
import com.jawzf.pixeltoolkit.feature.security.SecurityState
import com.jawzf.pixeltoolkit.feature.sound.SoundSettings
import com.jawzf.pixeltoolkit.feature.sound.SoundState
import com.jawzf.pixeltoolkit.services.LiveUpdateSnoozer
import com.jawzf.pixeltoolkit.services.NotificationListener
import com.jawzf.pixeltoolkit.services.receivers.FlashlightActionReceiver
import com.jawzf.pixeltoolkit.services.receivers.SecurityDeviceAdminReceiver
import com.jawzf.pixeltoolkit.services.receivers.SecurityReceiver
import com.jawzf.pixeltoolkit.services.tiles.ScreenOffAccessibilityService
import com.jawzf.pixeltoolkit.utils.AppIconUtil
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
    val isLockscreenShortcutsEnabled = mutableStateOf(false)
    val lockscreenShortcutActions = mutableStateMapOf<LockscreenShortcutSide, List<Action>>()
    val lockscreenSystemShortcutsState = mutableStateOf(SystemShortcutsState.UNKNOWN)

    val isFoldableCameraDevice = mutableStateOf(false)

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
    val remap = RemapState()
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
    val duo: DuoState =
        DuoState(
            islandLink =
                object : DuoIslandLink {
                    override fun onDuoShowsTime() {
                        island.isIslandShowTimeBattery.value = false
                        settingsRepository.setIslandShowTimeBatteryEnabled(false)
                        island.setIslandShowBattery(false)
                    }

                    override fun onDuoShowsMedia() {
                        island.isIslandShowMedia.value = false
                        settingsRepository.setIslandShowMediaEnabled(false)
                    }

                    override fun onDuoShowsProgress() {
                        island.isIslandNotifKeepProgress.value = false
                        settingsRepository.setIslandNotifKeepProgressEnabled(false)
                    }

                    override fun onDuoShowsFlashlight() {
                        island.isIslandShowFlashlight.value = false
                        settingsRepository.setIslandShowFlashlightEnabled(false)
                    }

                    override fun onCombinedChanged(enabled: Boolean) {
                        if (!enabled) return
                        island.setIslandShowTimeBattery(false)
                        island.setIslandShowBattery(false)
                        island.setIslandShowMedia(true)
                        island.setIslandShowFlashlight(true)
                        island.setIslandNotifKeepProgress(true)
                    }
                },
            onFoldableChanged = { isFoldableCameraDevice.value = it },
        )
    val island: IslandState =
        IslandState(
            scope = viewModelScope,
            duoLink =
                object : IslandDuoLink {
                    override fun onIslandShowsTime() {
                        duo.isDuoShowTime.value = false
                        duoSettings.setDuoShowTimeEnabled(false)
                    }

                    override fun onIslandShowsMedia() {
                        duo.isDuoShowMedia.value = false
                        duoSettings.setDuoShowMediaEnabled(false)
                    }

                    override fun onIslandShowsProgress() {
                        duo.isDuoShowProgress.value = false
                        duoSettings.setDuoShowProgressEnabled(false)
                    }

                    override fun onIslandShowsFlashlight() {
                        duo.isDuoShowFlashlight.value = false
                        duoSettings.setDuoShowFlashlightEnabled(false)
                    }
                },
            onFoldableChanged = { isFoldableCameraDevice.value = it },
        )
    val statusGlance =
        StatusGlanceState(viewModelScope) { isFoldableCameraDevice.value = it }
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
    private lateinit var textAnimations: TextAnimationsSettings
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
                            fontScale.floatValue = textAnimations.getFontScale()
                        }

                        Settings.Secure.getUriFor("font_weight_adjustment") -> {
                            fontWeight.intValue = textAnimations.getFontWeight()
                        }

                        Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE) -> {
                            animatorDurationScale.floatValue =
                                textAnimations.getAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE)
                        }

                        Settings.Global.getUriFor(Settings.Global.TRANSITION_ANIMATION_SCALE) -> {
                            transitionAnimationScale.floatValue =
                                textAnimations.getAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE)
                        }

                        Settings.Global.getUriFor(Settings.Global.WINDOW_ANIMATION_SCALE) -> {
                            windowAnimationScale.floatValue =
                                textAnimations.getAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE)
                        }

                        Settings.Secure.getUriFor("display_density_forced") -> {
                            smallestWidth.intValue = textAnimations.getSmallestWidth()
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
                    SecuritySettings.KEY_SCREEN_LOCKED_SECURITY_ENABLED,
                    SecuritySettings.KEY_SCREEN_LOCKED_DISABLE_ON_EXTENDED_UNLOCK,
                    -> security.onPreferenceChanged(key)

                    PowerSettings.KEY_MAPS_POWER_SAVING_ENABLED -> power.onPreferenceChanged(key)

                    RemapSettings.KEY_BUTTON_REMAP_ENABLED -> remap.onPreferenceChanged(key)

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
                    in DuoState.PREFERENCE_KEYS -> duo.onPreferenceChanged(key)
                    in IslandState.PREFERENCE_KEYS -> island.onPreferenceChanged(key)
                    in StatusGlanceState.PREFERENCE_KEYS -> statusGlance.onPreferenceChanged(key)

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

                    TextAnimationsSettings.KEY_FONT_SCALE ->
                        fontScale.floatValue =
                            textAnimations.getFontScale()

                    TextAnimationsSettings.KEY_FONT_WEIGHT ->
                        fontWeight.intValue =
                            textAnimations.getFontWeight()

                    TextAnimationsSettings.KEY_ANIMATOR_DURATION_SCALE ->
                        animatorDurationScale.floatValue =
                            textAnimations.getAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE)

                    TextAnimationsSettings.KEY_TRANSITION_ANIMATION_SCALE ->
                        transitionAnimationScale.floatValue =
                            textAnimations.getAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE)

                    TextAnimationsSettings.KEY_WINDOW_ANIMATION_SCALE ->
                        windowAnimationScale.floatValue =
                            textAnimations.getAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE)

                    TextAnimationsSettings.KEY_SMALLEST_WIDTH ->
                        smallestWidth.intValue =
                            textAnimations.getSmallestWidth()

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
        textAnimations = TextAnimationsSettings(context)
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

        fontScale.floatValue = textAnimations.getFontScale()
        fontWeight.intValue = textAnimations.getFontWeight()
        animatorDurationScale.floatValue =
            textAnimations.getAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE)
        transitionAnimationScale.floatValue =
            textAnimations.getAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE)
        windowAnimationScale.floatValue =
            textAnimations.getAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE)
        smallestWidth.intValue = textAnimations.getSmallestWidth()
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
        remap.load(context)

        hilight.load(context)
        isLockscreenShortcutsEnabled.value = settingsRepository.isLockscreenShortcutsEnabled()
        sound.load(context)
        LockscreenShortcutSide.entries.forEach { side ->
            lockscreenShortcutActions[side] = settingsRepository.getRemapActions(side.prefKey)
        }
        lockscreenSystemShortcutsState.value = settingsRepository.getLockscreenSystemShortcutsState()

        smartPixels.load(context)
        duo.load(context)
        island.load(context)
        statusGlance.load(context)
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
        scaleAnimationsMode.value = textAnimations.getScaleAnimationsMode()
        isTouchSensitivityEnabled.value = textAnimations.getTouchSensitivityEnabled()
        isAutoRotateEnabled.value = textAnimations.getAutoRotateEnabled()
        screenTimeout.value = textAnimations.getScreenTimeout()
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

    fun triggerIslandPreview(context: Context) {
        val alert = TestNotificationUtil.generateRandomNotification(context)
        com.jawzf.pixeltoolkit.services.NotificationListener.notifyAlertPosted(alert)
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
        textAnimations.saveScaleAnimationsProfile(oldMode, currentProfile)

        // 2. Load new profile
        val newProfile = textAnimations.getScaleAnimationsProfile(mode)

        // 3. Update mode
        scaleAnimationsMode.value = mode
        textAnimations.setScaleAnimationsMode(mode)

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
        textAnimations.setTouchSensitivityEnabled(enabled)
    }

    /**
     * Executes the set auto rotate enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAutoRotateEnabled(enabled: Boolean) {
        isAutoRotateEnabled.value = enabled
        textAnimations.setAutoRotateEnabled(enabled)
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
        textAnimations.setScreenTimeout(timeoutMs)
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
        textAnimations.setFontScale(fontScale.floatValue)
    }

    /**
     * Executes the set font scale operation.
     *
     * @param scale [Float] Target scale.
     */
    fun setFontScale(scale: Float) {
        fontScale.floatValue = scale
        textAnimations.setFontScale(scale)
    }

    /**
     * Executes the set font weight operation.
     *
     * @param weight [Int] Target weight.
     */
    fun setFontWeight(weight: Int) {
        fontWeight.intValue = weight
        textAnimations.setFontWeight(weight)
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
        textAnimations.setAnimationScale(key, scale)
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
        textAnimations.setSmallestWidth(smallestWidth.intValue)
    }

    /**
     * Executes the set smallest width operation.
     *
     * @param width [Int] Target width.
     */
    fun setSmallestWidth(width: Int) {
        smallestWidth.intValue = width
        textAnimations.setSmallestWidth(width)
    }

    /**
     * Executes the reset scale to default operation.
     */
    fun resetScaleToDefault() {
        textAnimations.resetSmallestWidth()
        smallestWidth.intValue = textAnimations.getSmallestWidth()
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
