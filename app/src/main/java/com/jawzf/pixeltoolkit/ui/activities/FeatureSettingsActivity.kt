package com.jawzf.pixeltoolkit

import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.FeatureCard
import com.jawzf.pixeltoolkit.core.design.components.LottieFeatureAnimation
import com.jawzf.pixeltoolkit.core.design.components.ToolkitFloatingToolbar
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.BlurDirection
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.modifiers.progressiveBlur
import com.jawzf.pixeltoolkit.core.design.modifiers.scrollMotionBlur
import com.jawzf.pixeltoolkit.core.design.sheets.InstructionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.state.LocalMenuStateManager
import com.jawzf.pixeltoolkit.core.design.state.MenuStateManager
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.core.platform.device.DeviceUtils
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.domain.registry.FeatureRegistry
import com.jawzf.pixeltoolkit.feature.apps.applock.ui.AppLockSettingsUI
import com.jawzf.pixeltoolkit.feature.apps.consciousgate.ui.CONSCIOUS_GATE_FEATURE_ID
import com.jawzf.pixeltoolkit.feature.apps.consciousgate.ui.ConsciousGateSettingsUI
import com.jawzf.pixeltoolkit.feature.apps.freeze.ui.FreezeSettingsUI
import com.jawzf.pixeltoolkit.feature.connectivity.ui.NetworksSettingsUI
import com.jawzf.pixeltoolkit.feature.input.gesture.ui.BACK_GESTURE_FEATURE_ID
import com.jawzf.pixeltoolkit.feature.input.gesture.ui.BackGestureSettingsUI
import com.jawzf.pixeltoolkit.feature.input.ime.ui.KeyboardSettingsUI
import com.jawzf.pixeltoolkit.feature.input.links.ui.LinkPickerScreen
import com.jawzf.pixeltoolkit.feature.power.battery.ui.BatteriesPreviewCard
import com.jawzf.pixeltoolkit.feature.power.battery.ui.BatteriesSettingsUI
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateViewModel
import com.jawzf.pixeltoolkit.feature.power.caffeinate.ui.CaffeinateSettingsUI
import com.jawzf.pixeltoolkit.feature.power.maps.ui.MapsPowerSavingSettingsUI
import com.jawzf.pixeltoolkit.feature.power.saver.ui.PowerAndBatterySettingsUI
import com.jawzf.pixeltoolkit.feature.power.standby.ui.StandbyAppsSettingsUI
import com.jawzf.pixeltoolkit.feature.security.faceunlock.ui.FaceUnlockBrightnessSettingsUI
import com.jawzf.pixeltoolkit.feature.security.lockscreen.ui.ScreenLockedSecuritySettingsUI
import com.jawzf.pixeltoolkit.feature.security.screenshots.ui.ScreenshotsSettingsUI
import com.jawzf.pixeltoolkit.feature.sound.ui.AppVolumeSettingsUI
import com.jawzf.pixeltoolkit.feature.sound.ui.SoundModeTileSettingsUI
import com.jawzf.pixeltoolkit.feature.sound.ui.VolumeStepsSettingsUI
import com.jawzf.pixeltoolkit.ui.core.sheets.AppSelectionSheet
import com.jawzf.pixeltoolkit.ui.core.sheets.ShakeForHelp
import com.jawzf.pixeltoolkit.ui.core.sheets.SingleAppSelectionSheet
import com.jawzf.pixeltoolkit.ui.features.display.AodWallpaperPreviewCard
import com.jawzf.pixeltoolkit.ui.features.display.AodWallpaperSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.DuoSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.ISLAND_BEHAVIOR_FEATURE_ID
import com.jawzf.pixeltoolkit.ui.features.display.ISLAND_DURATION_FEATURE_ID
import com.jawzf.pixeltoolkit.ui.features.display.ISLAND_NOTIFICATIONS_FEATURE_ID
import com.jawzf.pixeltoolkit.ui.features.display.ISLAND_PLACEMENT_FEATURE_ID
import com.jawzf.pixeltoolkit.ui.features.display.ISLAND_SUB_PAGE_IDS
import com.jawzf.pixeltoolkit.ui.features.display.ISLAND_VISUALS_FEATURE_ID
import com.jawzf.pixeltoolkit.ui.features.display.IslandBehaviorSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.IslandDurationSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.IslandNotificationSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.IslandPlacementSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.IslandSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.IslandVisualsSettingsUI
import com.jawzf.pixeltoolkit.ui.features.display.StatusGlanceSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.ActivityLauncherSearchBar
import com.jawzf.pixeltoolkit.ui.features.system.ActivityLauncherSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.AlwaysOnDisplaySettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.AutoDismissNotificationsSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.ButtonRemapSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.DynamicNightLightSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.FlashlightPulseSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.FlashlightSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.HilightNotificationsSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.HilightProgressSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.HilightSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.HilightStatus
import com.jawzf.pixeltoolkit.ui.features.system.LiveWallpaperSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.LockScreenClockSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.LockscreenShortcutsSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.NavigationSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.NotificationLightingSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.NotificationSnoozingSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.OtherCustomizationsSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.QuickSettingsTilesSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.RefreshRateSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.ScreenOffWidgetSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.ShutUpSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.SnoozeNotificationsSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.StatusBarIconSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.TextAnimationsSettingsUI
import com.jawzf.pixeltoolkit.ui.features.system.ToolkitOnDisplaySettingsUI
import com.jawzf.pixeltoolkit.ui.features.widgets.WidgetStackSettingsUI
import com.jawzf.pixeltoolkit.utils.BiometricSecurityHelper
import com.jawzf.pixeltoolkit.utils.PermissionUIHelper
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel
import com.jawzf.pixeltoolkit.viewmodels.PermissionViewModel
import com.jawzf.pixeltoolkit.viewmodels.StatusBarIconViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
class FeatureSettingsActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
            navigationBarStyle =
                SystemBarStyle.auto(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT,
                ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val isDarkMode =
            (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        window.setBackgroundDrawableResource(if (isDarkMode) android.R.color.black else R.color.app_window_background)
        val featureId = intent.getStringExtra("feature") ?: intent.getStringExtra("FEATURE_ID") ?: ""
        val featureObj = FeatureRegistry.ALL_FEATURES.find { it.id == featureId }
        val highlightSetting = intent.getStringExtra("highlight_setting")

        if (featureId == "Link actions" || featureId == "URL Shortener") {
            val isShortenerDirect = featureId == "URL Shortener"
            setContent {
                val viewModel: MainViewModel = viewModel()
                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    viewModel.check(context)
                }
                val isPitchBlackThemeEnabled by viewModel.isPitchBlackThemeEnabled
                ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                    LinkPickerScreen(
                        uri = "https://example.com".toUri(),
                        onFinish = { finish() },
                        modifier = Modifier.fillMaxSize(),
                        demo = false,
                        initialTab = if (isShortenerDirect) 2 else 0,
                        initialOpenShorten = isShortenerDirect,
                    )
                }
            }
            return
        }

        setContent {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel()
            val statusBarViewModel: StatusBarIconViewModel = viewModel()
            val caffeinateViewModel: CaffeinateViewModel = viewModel()

            // Automatic refresh on resume
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer =
                    LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.check(context)
                            if (featureId == "Statusbar icons") {
                                statusBarViewModel.check(context)
                            }
                            if (featureId == "Caffeinate") {
                                caffeinateViewModel.check(context)
                            }
                        }
                    }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            // Initialize synchronously so settingsRepository is ready before first composition
            remember(context) { viewModel.check(context) }

            val isPitchBlackThemeEnabled by viewModel.isPitchBlackThemeEnabled
            val isBlurEnabled by viewModel.isBlurEnabled
            val pinnedFeatureKeys by viewModel.pinnedFeatureKeys

            ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalMenuStateManager provides
                        remember {
                            MenuStateManager()
                        },
                ) {
                    LocalView.current
                    val prefs = context.getSharedPreferences("toolkit_prefs", MODE_PRIVATE)

                    val vibrator =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(VIBRATOR_SERVICE) as? Vibrator
                        }

                    var selectedHaptic by remember {
                        val name =
                            prefs.getString("haptic_feedback_type", HapticFeedbackType.NONE.name)
                        mutableStateOf(
                            try {
                                HapticFeedbackType.valueOf(name ?: HapticFeedbackType.NONE.name)
                            } catch (
                                @Suppress("UNUSED_PARAMETER") e: Exception,
                            ) {
                                HapticFeedbackType.NONE
                            },
                        )
                    }

                    // Permission sheet state
                    var showPermissionSheet by remember { mutableStateOf(false) }
                    var childFeatureForPermissions by remember { mutableStateOf<String?>(null) }
                    var standbyAppsSelectedPackages by remember { mutableStateOf(setOf<String>()) }
                    var activityLauncherQuery by remember { mutableStateOf("") }
                    var isStandbyMoveSheetVisible by remember { mutableStateOf(false) }

                    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled
                    val isWriteSecureSettingsEnabled by viewModel.isWriteSecureSettingsEnabled
                    val isOverlayPermissionGranted by viewModel.isOverlayPermissionGranted
                    val isNotificationLightingAccessibilityEnabled by viewModel.isNotificationLightingAccessibilityEnabled
                    val isNotificationListenerEnabled by viewModel.isNotificationListenerEnabled
                    val isReadPhoneStateEnabled by viewModel.isReadPhoneStateEnabled
                    val isShizukuPermissionGranted by viewModel.isShizukuPermissionGranted

                    // FAB State for Notification Lighting
                    var fabExpanded by remember { mutableStateOf(true) }
                    LaunchedEffect(featureId) {
                        if (featureId == "Notification lighting") {
                            fabExpanded = true
                            delay(3000)
                            fabExpanded = false
                        }
                    }

                    // Help Sheet State
                    var showHelpSheet by remember { mutableStateOf(false) }
                    var showInstructionsSheet by remember { mutableStateOf(false) }
                    var selectedHelpFeature by remember {
                        mutableStateOf<com.jawzf.pixeltoolkit.domain.model.Feature?>(
                            null,
                        )
                    }

                    // Show permission sheet if feature has missing permissions
                    LaunchedEffect(
                        featureId,
                        isAccessibilityEnabled,
                        isWriteSecureSettingsEnabled,
                        isOverlayPermissionGranted,
                        isNotificationLightingAccessibilityEnabled,
                        isNotificationListenerEnabled,
                        isReadPhoneStateEnabled,
                        isShizukuPermissionGranted,
                    ) {
                        val hasMissingPermissions =
                            when (featureId) {
                                "Screen off widget" -> !isAccessibilityEnabled
                                "Widget stack" -> !isOverlayPermissionGranted
                                "Statusbar icons" -> !isWriteSecureSettingsEnabled
                                "Notification lighting" ->
                                    !isOverlayPermissionGranted ||
                                        !isNotificationLightingAccessibilityEnabled ||
                                        !isNotificationListenerEnabled
                                "Flashlight pulse" -> !isNotificationListenerEnabled
                                "Hilight", "Hilight notifications", "Hilight live progress" -> !isNotificationListenerEnabled || !isShizukuPermissionGranted
                                "Button remap" -> !isAccessibilityEnabled
                                "Face unlock brightness" -> !isAccessibilityEnabled
                                "Screenshots" -> !isAccessibilityEnabled || !viewModel.isStoragePermissionGranted.value
                                "Lock screen shortcuts" -> !isAccessibilityEnabled
                                "Volume steps" -> !isAccessibilityEnabled
                                "App volume" -> !isAccessibilityEnabled || !isShizukuPermissionGranted
                                "Dynamic night light" ->
                                    (if (viewModel.isUseUsageAccess.value) !viewModel.isUsageStatsPermissionGranted.value else !isAccessibilityEnabled) ||
                                        !isWriteSecureSettingsEnabled
                                "Smart pixels" -> !isAccessibilityEnabled
                                "Duo", "Status glance" -> !isAccessibilityEnabled
                                "AOD wallpaper" -> !isAccessibilityEnabled || !viewModel.isStoragePermissionGranted.value
                                "Snooze system notifications" -> !isNotificationListenerEnabled
                                "Auto-dismiss notifications" -> !isNotificationListenerEnabled
                                "Screen locked security" ->
                                    !ShellUtils.hasPermission(
                                        context,
                                    )
                                "App lock" ->
                                    !isAccessibilityEnabled ||
                                        (if (viewModel.isUseUsageAccess.value) !viewModel.isUsageStatsPermissionGranted.value else false)
                                CONSCIOUS_GATE_FEATURE_ID ->
                                    !isAccessibilityEnabled ||
                                        (if (viewModel.isUseUsageAccess.value) !viewModel.isUsageStatsPermissionGranted.value else false)
                                "Freeze" ->
                                    !ShellUtils.hasPermission(
                                        context,
                                    )

                                "Location reached" ->
                                    !viewModel.isLocationPermissionGranted.value ||
                                        !viewModel.isBackgroundLocationPermissionGranted.value
                                "Quick settings tiles" -> !viewModel.isWriteSettingsEnabled.value
                                "Screen refresh rate" ->
                                    !ShellUtils.hasPermission(
                                        context,
                                    )
                                // Top level checks for other features (rarely hit if they are children, but safe to add)
                                "Toolkit On Display" -> !isAccessibilityEnabled || !isNotificationListenerEnabled
                                "Call vibrations" -> !isReadPhoneStateEnabled || !isNotificationListenerEnabled
                                "Maps power saving mode" ->
                                    !isNotificationListenerEnabled ||
                                        !ShellUtils.hasPermission(
                                            context,
                                        )

                                "Caffeinate" -> !viewModel.isPostNotificationsEnabled.value
                                "Text and animations" -> !viewModel.isWriteSettingsEnabled.value || !isWriteSecureSettingsEnabled
                                "Always on Display" -> !isWriteSecureSettingsEnabled
                                "Lock screen clock" -> !isWriteSecureSettingsEnabled
                                "Other customizations" ->
                                    !ShellUtils.hasPermission(
                                        context,
                                    )

                                "Shut-Up!" -> !isWriteSecureSettingsEnabled || !viewModel.isUsageStatsPermissionGranted.value
                                "Power and Battery" -> !isWriteSecureSettingsEnabled
                                "Networks" ->
                                    !isWriteSecureSettingsEnabled &&
                                        !ShellUtils.hasPermission(
                                            context,
                                        )

                                else -> false
                            }
                        if (hasMissingPermissions) {
                            showPermissionSheet = true
                        }
                    }

                    if (showPermissionSheet) {
                        val featureIdForPermissions = childFeatureForPermissions ?: featureId
                        val featureObjForPermissions =
                            FeatureRegistry.ALL_FEATURES.find { it.id == featureIdForPermissions }

                        val permissionItems =
                            if (featureObjForPermissions != null) {
                                com.jawzf.pixeltoolkit.utils.PermissionUIHelper.getPermissionItems(
                                    featureObjForPermissions.permissionKeys,
                                    context,
                                    viewModel,
                                    this@FeatureSettingsActivity,
                                )
                            } else {
                                emptyList()
                            }

                        if (permissionItems.isNotEmpty()) {
                            PermissionsBottomSheet(
                                onDismissRequest = {
                                    showPermissionSheet = false
                                    childFeatureForPermissions = null
                                },
                                featureTitle =
                                    if (featureObjForPermissions != null && childFeatureForPermissions == null) {
                                        stringResource(
                                            featureObjForPermissions.title,
                                        )
                                    } else {
                                        featureIdForPermissions
                                    },
                                permissions = permissionItems,
                            )
                        }
                    }


                    if (showHelpSheet && selectedHelpFeature != null) {
                        com.jawzf.pixeltoolkit.ui.core.sheets.FeatureHelpBottomSheet(
                            onDismissRequest = {
                                showHelpSheet = false
                                selectedHelpFeature = null
                            },
                            feature = selectedHelpFeature!!,
                        )
                    }

                    if (showInstructionsSheet) {
                        InstructionsBottomSheet(
                            onDismissRequest = { showInstructionsSheet = false },
                        )
                    }


                    ShakeForHelp(feature = featureObj, viewModel = viewModel)

                    val pageTitle =
                        if (featureObj != null) stringResource(featureObj.title) else featureId
                    val hasMenu = featureObj != null && featureObj.aboutDescription != null
                    val view = LocalView.current

                    val density = LocalDensity.current
                    val minHeaderHeight = 200.dp
                    val maxHeaderHeight = 400.dp
                    var headerHeight by remember { mutableStateOf(minHeaderHeight) }

                    val nestedScrollConnection =
                        remember {
                            object : NestedScrollConnection {
                                override fun onPreScroll(
                                    available: Offset,
                                    source: NestedScrollSource,
                                ): Offset {
                                    val delta = available.y
                                    if (delta < 0 && headerHeight > minHeaderHeight) {
                                        val oldHeight = headerHeight
                                        headerHeight =
                                            with(density) {
                                                (oldHeight.toPx() + delta).toDp()
                                            }.coerceAtLeast(minHeaderHeight)
                                        val consumed = oldHeight - headerHeight
                                        return Offset(0f, with(density) { -consumed.toPx() })
                                    }
                                    return Offset.Zero
                                }

                                override fun onPostScroll(
                                    consumed: Offset,
                                    available: Offset,
                                    source: NestedScrollSource,
                                ): Offset {
                                    val delta = available.y
                                    if (delta > 0) {
                                        val oldHeight = headerHeight
                                        headerHeight =
                                            with(density) {
                                                (oldHeight.toPx() + delta).toDp()
                                            }.coerceAtMost(maxHeaderHeight)

                                        if (headerHeight == maxHeaderHeight && oldHeight < maxHeaderHeight) {
                                            HapticUtil.performLightHaptic(view)
                                        }

                                        val produced = headerHeight - oldHeight
                                        return Offset(0f, with(density) { produced.toPx() })
                                    }
                                    return Offset.Zero
                                }
                            }
                        }

                    val statusBarHeightPx =
                        with(LocalDensity.current) {
                            WindowInsets.statusBars
                                .asPaddingValues()
                                .calculateTopPadding()
                                .toPx()
                        }
                    val statusBarHeight =
                        WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

                    val isMotionBlurEnabled by viewModel.isMotionBlurEnabled
                    val scrollState = rememberScrollState()

                    Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .progressiveBlur(
                                    blurRadius = if (isBlurEnabled) 40f else 0f,
                                    height = statusBarHeightPx * 1.15f,
                                    direction = BlurDirection.TOP,
                                ),
                    ) {
                        val hasScroll =
                            featureId != "Sound mode tile" &&
                                featureId != "Activity launcher" &&
                                featureId != "Quick settings tiles"
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .progressiveBlur(
                                        blurRadius = if (isBlurEnabled) 40f else 0f,
                                        height = with(LocalDensity.current) { 150.dp.toPx() },
                                        direction = BlurDirection.BOTTOM,
                                    ).then(
                                        if (hasScroll) {
                                            Modifier
                                                .scrollMotionBlur(scrollState, enabled = isMotionBlurEnabled)
                                                .nestedScroll(nestedScrollConnection)
                                                .verticalScroll(scrollState)
                                        } else {
                                            Modifier
                                        },
                                    ),
                        ) {
                            // Top padding for status bar
                            if (featureId != "Quick settings tiles" && featureId != "Location reached" && featureId != "Activity launcher") {
                                androidx.compose.foundation.layout.Spacer(
                                    modifier =
                                        Modifier.height(
                                            statusBarHeight,
                                        ),
                                )
                            }

                            if (featureId == "AOD wallpaper") {
                                AodWallpaperPreviewCard(
                                    viewModel = viewModel,
                                    height = headerHeight - 16.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }

                            if (featureId == "Batteries") {
                                BatteriesPreviewCard(
                                    state = viewModel.battery,
                                    height = headerHeight - 16.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }

                            if (featureObj != null && featureObj.animationRes != 0) {
                                LottieFeatureAnimation(
                                    resId = featureObj.animationRes,
                                    height = headerHeight,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                )
                            }


                            val children =
                                FeatureRegistry
                                    .getFilteredFeatures(
                                        context,
                                        viewModel.isEnableUnsupportedFeatures.value,
                                        viewModel.isShowLegacyFeatures.value,
                                    ).filter { it.parentFeatureId == featureId }
                            if (children.isNotEmpty() && featureId != "Networks" && featureId != "Island" && featureId != "Navigation") {
                                if (featureId == "Hilight") HilightStatus(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

                        val sectionChildLists =
                                    run {
                                        val childMap = children.associateBy { it.id }
                                        val definedSections =
                                            when (featureId) {
                                                "Display" ->
                                                    listOf(
                                                        listOf(
                                                            "Always on Display",
                                                            "AOD wallpaper",
                                                            "Toolkit On Display",
                                                            "Lock screen clock",
                                                            "Maps power saving mode",
                                                        ),
                                                        listOf(
                                                            "Statusbar icons",
                                                            "Duo",
                                                            "Island",
                                                            "Status glance",
                                                        ),
                                                        listOf(
                                                            "Text and animations",
                                                            "Screen refresh rate",
                                                        ),
                                                        listOf(
                                                            "Caffeinate",
                                                            "Dynamic night light",
                                                            "Smart pixels",
                                                        ),
                                                        listOf(
                                                            "Other customizations",
                                                        ),
                                                    )

                                                "Notifications" ->
                                                    listOf(
                                                        listOf(
                                                            "Notification lighting",
                                                            "Flashlight pulse",
                                                            "Hilight",
                                                        ),
                                                        listOf(
                                                            "Notification snoozing",
                                                            "Snooze system notifications",
                                                        ),
                                                    )

                                                "Widgets" ->
                                                    listOf(
                                                        listOf(
                                                            "Pixel Searchbar",
                                                        ),
                                                        listOf(
                                                            "Screen off widget",
                                                            "Widget stack",
                                                        ),
                                                    )

                                                "Input" ->
                                                    listOf(
                                                        listOf(
                                                            "Button remap",
                                                            "Flashlight",
                                                            "Activity launcher",
                                                            "Lock screen shortcuts",
                                                        ),
                                                        listOf(
                                                            "Navigation",
                                                            "Link actions",
                                                            "System Keyboard",
                                                        ),
                                                    )

                                                "Power and battery" ->
                                                    listOf(
                                                        listOf(
                                                            "Power and Battery",
                                                            "Standby apps",
                                                        ),
                                                        listOf(
                                                            "Batteries",
                                                        ),
                                                    )


                                                "Security" ->
                                                    listOf(
                                                        listOf(
                                                            "Screen locked security",
                                                            "App lock",
                                                            "Shut-Up!",
                                                        ),
                                                        listOf(
                                                            "Lockdown mode",
                                                            "Face unlock brightness",
                                                            "Screenshots",
                                                        ),
                                                    )

                                                else -> null
                                            }

                                        if (definedSections != null) {
                                            val assignedIds = definedSections.flatten().toSet()
                                            val unassigned = children.filter { it.id !in assignedIds }
                                            definedSections
                                                .map { ids -> ids.mapNotNull { childMap[it] } }
                                                .filter { it.isNotEmpty() } +
                                                if (unassigned.isNotEmpty()) {
                                                    listOf(
                                                        unassigned,
                                                    )
                                                } else {
                                                    emptyList()
                                                }
                                        } else {
                                            listOf(children)
                                        }
                                    }

                                sectionChildLists.forEach { sectionChildren ->
                                    RoundedCardContainer(
                                        modifier =
                                            Modifier
                                                .padding(horizontal = 16.dp)
                                                .padding(top = 16.dp),
                                    ) {
                                        sectionChildren.forEach { child ->
                                            val permissionAwareToggle: (Boolean) -> Unit =
                                                { enabled ->
                                                    val missingPermission =
                                                        when (child.id) {
                                                            "Screen off widget" -> !isAccessibilityEnabled
                                                            "Statusbar icons" -> !isWriteSecureSettingsEnabled
                                                            "Notification lighting" ->
                                                                !isOverlayPermissionGranted ||
                                                                    !isNotificationLightingAccessibilityEnabled ||
                                                                    !isNotificationListenerEnabled
                                                            "Flashlight pulse" -> !isNotificationListenerEnabled
                                                            "Hilight", "Hilight notifications", "Hilight live progress" -> !isNotificationListenerEnabled || !isShizukuPermissionGranted
                                                            "Button remap" -> !isAccessibilityEnabled
                                                            "Face unlock brightness" -> !isAccessibilityEnabled
                                                            "Screenshots" -> !isAccessibilityEnabled || !viewModel.isStoragePermissionGranted.value
                                                            "Lock screen shortcuts" -> !isAccessibilityEnabled
                                                            "Volume steps" -> !isAccessibilityEnabled
                                                            "App volume" -> !isAccessibilityEnabled || !isShizukuPermissionGranted
                                                            "Dynamic night light" ->
                                                                (if (viewModel.isUseUsageAccess.value) !viewModel.isUsageStatsPermissionGranted.value else !isAccessibilityEnabled) ||
                                                                    !isWriteSecureSettingsEnabled
                                                            "Smart pixels" -> !isAccessibilityEnabled
                                                            "Duo", "Status glance" -> !isAccessibilityEnabled
                                                            "AOD wallpaper" -> !isAccessibilityEnabled || !viewModel.isStoragePermissionGranted.value
                                                            "Snooze system notifications" -> !isNotificationListenerEnabled
                                 "Auto-dismiss notifications" -> !isNotificationListenerEnabled
                                                            "Screen locked security" ->
                                                                !ShellUtils.hasPermission(
                                                                    context,
                                                                )
                                                            "App lock" ->
                                                                !isAccessibilityEnabled ||
                                                                    (if (viewModel.isUseUsageAccess.value) !viewModel.isUsageStatsPermissionGranted.value else false)
                                                            CONSCIOUS_GATE_FEATURE_ID ->
                                                                !isAccessibilityEnabled ||
                                                                    (if (viewModel.isUseUsageAccess.value) !viewModel.isUsageStatsPermissionGranted.value else false)
                                                            "Freeze" ->
                                                                !ShellUtils.hasPermission(
                                                                    context,
                                                                )

                                                            "Toolkit On Display" ->
                                                                !isAccessibilityEnabled ||
                                                                    !isNotificationListenerEnabled
                                                            "Call vibrations" -> !isReadPhoneStateEnabled || !isNotificationListenerEnabled

                                                            "Batteries" -> (
                                                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                                                                    androidx.core.content.ContextCompat.checkSelfPermission(
                                                                        context,
                                                                        android.Manifest.permission.BLUETOOTH_CONNECT,
                                                                    ) != android.content.pm.PackageManager.PERMISSION_GRANTED
                                                            )

                                                            "Maps power saving mode" ->
                                                                !isNotificationListenerEnabled ||
                                                                    !ShellUtils.hasPermission(
                                                                        context,
                                                                    )

                                                            "Caffeinate" -> !viewModel.isPostNotificationsEnabled.value
                                                            "Text and animations" ->
                                                                !viewModel.isWriteSettingsEnabled.value ||
                                                                    !isWriteSecureSettingsEnabled
                                                            "Always on Display" -> !isWriteSecureSettingsEnabled
                                                            "Lock screen clock" -> !isWriteSecureSettingsEnabled
                                                            "Screen refresh rate" ->
                                                                !ShellUtils.hasPermission(
                                                                    context,
                                                                )

                                                            "Shut-Up!" ->
                                                                !isWriteSecureSettingsEnabled ||
                                                                    !viewModel.isUsageStatsPermissionGranted.value
                                                            "Power and Battery" -> !isWriteSecureSettingsEnabled
                                                            "Networks" ->
                                                                !isWriteSecureSettingsEnabled &&
                                                                    !ShellUtils.hasPermission(
                                                                        context,
                                                                    )

                                                            "Call Sync" ->
                                                                !PermissionUtils.hasCallPermissions(
                                                                    context,
                                                                )
                                                            "Notification Sync" -> !viewModel.isNotificationListenerEnabled.value

                                                            "Disable safe volume warning" -> !isWriteSecureSettingsEnabled
                                                            "Notification snoozing" -> !isWriteSecureSettingsEnabled
                                                            else -> false
                                                        }

                                                    if (missingPermission) {
                                                        childFeatureForPermissions = child.id
                                                        showPermissionSheet = true
                                                    } else {
                                                        BiometricSecurityHelper.runWithAuth(
                                                            activity = this@FeatureSettingsActivity,
                                                            feature = child,
                                                            isToggle = true,
                                                            action = {
                                                                child.onToggle(
                                                                    viewModel,
                                                                    context,
                                                                    enabled,
                                                                )
                                                            },
                                                        )
                                                    }
                                                }

                                            FeatureCard(
                                                modifier = Modifier.highlight(highlightSetting == child.id),
                                                title = child.title,
                                                description = child.description,
                                                iconRes = child.iconRes,
                                                isEnabled = child.isEnabled(viewModel),
                                                isToggleEnabled =
                                                    child.isToggleEnabled(
                                                        viewModel,
                                                        context,
                                                    ),
                                                showToggle = child.showToggle,
                                                onDisabledToggleClick = {
                                                    if (child.id.startsWith("Hilight") && !DeviceUtils.isHilightDevice()) {
                                                        Toast.makeText(context, R.string.hilight_not_supported_toast, Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        permissionAwareToggle(true)
                                                    }
                                                },
                                                hasMoreSettings = child.hasMoreSettings,
                                                isBeta = child.isBeta,
                                                isLegacy = child.isLegacy,
                                                isUnsupported = !child.isDeviceSupported(context),
                                                onToggle = permissionAwareToggle,
                                                onClick = {
                                                    if (child.hasMoreSettings) {
                                                        BiometricSecurityHelper.runWithAuth(
                                                            activity = this@FeatureSettingsActivity,
                                                            feature = child,
                                                            action = {
                                                                child.onClick(context, viewModel)
                                                            },
                                                        )
                                                    }
                                                },
                                                isPinned = pinnedFeatureKeys.contains(child.id),
                                                onPinToggle = { viewModel.togglePinFeature(child.id) },
                                                onHelpClick =
                                                    if (child.aboutDescription != null) {
                                                        {
                                                            selectedHelpFeature = child
                                                            showHelpSheet = true
                                                        }
                                                    } else {
                                                        null
                                                    },
                                            )
                                        }
                                    }
                                }

                                if (featureId == "Hilight") {
                                    HilightSettingsUI(
                                        viewModel = viewModel,
                                        highlightSetting = highlightSetting,
                                    )
                                }
                            } else {
                                when (featureId) {
                                    "Screen off widget" -> {
                                        ScreenOffWidgetSettingsUI(
                                            viewModel = viewModel,
                                            selectedHaptic = selectedHaptic,
                                            onHapticSelected = { type -> selectedHaptic = type },
                                            vibrator = vibrator,
                                            prefs = prefs,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                            onShowPermissionSheet = { showPermissionSheet = it },
                                            onSetChildFeatureForPermissions = {
                                                childFeatureForPermissions = it
                                            },
                                        )
                                    }

                                    "Statusbar icons" -> {
                                        StatusBarIconSettingsUI(
                                            viewModel = statusBarViewModel,
                                            mainViewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Caffeinate" -> {
                                        CaffeinateSettingsUI(
                                            viewModel = caffeinateViewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }


                                    "Hilight notifications" -> {
                                        HilightNotificationsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Hilight live progress" -> {
                                        HilightProgressSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Notification lighting" -> {
                                        NotificationLightingSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Sound mode tile" -> {
                                        SoundModeTileSettingsUI(
                                            modifier =
                                                Modifier
                                                    .nestedScroll(nestedScrollConnection)
                                                    .padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Button remap" -> {
                                        ButtonRemapSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Activity launcher" -> {
                                        ActivityLauncherSettingsUI(
                                            query = activityLauncherQuery,
                                            modifier = Modifier.nestedScroll(nestedScrollConnection),
                                        )
                                    }

                                    "Lock screen shortcuts" -> {
                                        LockscreenShortcutsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Volume steps" -> {
                                        VolumeStepsSettingsUI(
                                            state = viewModel.sound,
                                            isAccessibilityEnabled = viewModel.isAccessibilityEnabled.value,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "App volume" -> {
                                        AppVolumeSettingsUI(
                                            state = viewModel.sound,
                                            isAccessibilityEnabled = viewModel.isAccessibilityEnabled.value,
                                            isShizukuPermissionGranted = viewModel.isShizukuPermissionGranted.value,
                                            appPicker = { onDismiss, onAppSelected ->
                                                SingleAppSelectionSheet(
                                                    onDismissRequest = onDismiss,
                                                    includeSystemApps = true,
                                                    onAppSelected = { app -> onAppSelected(app.packageName) },
                                                )
                                            },
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Flashlight" -> {
                                        FlashlightSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Dynamic night light" -> {
                                        DynamicNightLightSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Smart pixels" -> {
                                        com.jawzf.pixeltoolkit.ui.features.system.SmartPixelsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Snooze system notifications" -> {
                                        SnoozeNotificationsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Auto-dismiss notifications" -> {
                                        AutoDismissNotificationsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Notification snoozing" -> {
                                        NotificationSnoozingSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Screen locked security" -> {
                                        ScreenLockedSecuritySettingsUI(
                                            state = viewModel.security,
                                            permissionItems = { keys -> PermissionUIHelper.getPermissionItems(keys, context, viewModel) },
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Face unlock brightness" -> {
                                        FaceUnlockBrightnessSettingsUI(
                                            state = viewModel.security,
                                            isAccessibilityEnabled = viewModel.isAccessibilityEnabled.value,
                                            permissionItems = { keys -> PermissionUIHelper.getPermissionItems(keys, context, viewModel) },
                                            onPermissionsDismissed = { viewModel.check(context) },
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Screenshots" -> {
                                        ScreenshotsSettingsUI(
                                            state = viewModel.security,
                                            isAccessibilityEnabled = viewModel.isAccessibilityEnabled.value,
                                            isStoragePermissionGranted = viewModel.isStoragePermissionGranted.value,
                                            isPostNotificationsEnabled = viewModel.isPostNotificationsEnabled.value,
                                            permissionItems = { keys -> PermissionUIHelper.getPermissionItems(keys, context, viewModel) },
                                            onPermissionsDismissed = { viewModel.check(context) },
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "App lock" -> {
                                        val permissionViewModel: PermissionViewModel = viewModel()
                                        AppLockSettingsUI(
                                            state = viewModel.appLock,
                                            isUseUsageAccess = viewModel.isUseUsageAccess.value,
                                            isAccessibilityEnabled = permissionViewModel.isAccessibilityEnabled.value,
                                            isUsageStatsPermissionGranted = viewModel.isUsageStatsPermissionGranted.value,
                                            onPermissionsDismissed = {
                                                permissionViewModel.refreshPermissions(context)
                                                viewModel.check(context)
                                            },
                                            appPicker = { onDismiss ->
                                                AppSelectionSheet(
                                                    onDismissRequest = onDismiss,
                                                    onLoadApps = { viewModel.appLock.loadAppLockSelectedApps(it) },
                                                    onSaveApps = { ctx, apps -> viewModel.appLock.saveAppLockSelectedApps(ctx, apps) },
                                                    onAppToggle = { ctx, pkg, enabled ->
                                                        viewModel.appLock.updateAppLockAppEnabled(
                                                            ctx,
                                                            pkg,
                                                            enabled,
                                                        )
                                                    },
                                                )
                                            },
                                            highlightKey = highlightSetting,
                                        )
                                    }

                                    CONSCIOUS_GATE_FEATURE_ID -> {
                                        ConsciousGateSettingsUI(
                                            state = viewModel.consciousGate,
                                            isUseUsageAccess = viewModel.isUseUsageAccess.value,
                                            isAccessibilityEnabled = viewModel.isAccessibilityEnabled.value,
                                            isUsageStatsPermissionGranted = viewModel.isUsageStatsPermissionGranted.value,
                                            permissionItems = { keys ->
                                                PermissionUIHelper.getPermissionItems(keys, context, viewModel)
                                            },
                                            onPermissionsDismissed = { viewModel.check(context) },
                                            appPicker = { onDismiss ->
                                                AppSelectionSheet(
                                                    onDismissRequest = onDismiss,
                                                    onLoadApps = { viewModel.consciousGate.loadConsciousGateSelectedApps(it) },
                                                    onSaveApps = { ctx, apps ->
                                                        viewModel.consciousGate.saveConsciousGateSelectedApps(ctx, apps)
                                                    },
                                                    onAppToggle = { ctx, pkg, enabled ->
                                                        viewModel.consciousGate.updateConsciousGateAppEnabled(ctx, pkg, enabled)
                                                    },
                                                )
                                            },
                                            highlightKey = highlightSetting,
                                        )
                                    }

                                    "Freeze" -> {
                                        FreezeSettingsUI(
                                            state = viewModel.freeze,
                                            isShizukuAvailable = viewModel.isShizukuAvailable.value,
                                            isShizukuPermissionGranted = viewModel.isShizukuPermissionGranted.value,
                                            isAccessibilityEnabled = viewModel.isAccessibilityEnabled.value,
                                            isEnableUnsupportedFeatures = viewModel.isEnableUnsupportedFeatures.value,
                                            permissionItems = { keys ->
                                                PermissionUIHelper.getPermissionItems(
                                                    keys,
                                                    context,
                                                    viewModel,
                                                    context as? android.app.Activity,
                                                )
                                            },
                                            appPicker = { onDismiss ->
                                                AppSelectionSheet(
                                                    restrictSystemApps = !viewModel.isEnableUnsupportedFeatures.value,
                                                    showInvertSelection = false,
                                                    hideSystemAppsByDefault = true,
                                                    onDismissRequest = onDismiss,
                                                    onLoadApps = { viewModel.freeze.loadFreezeSelectedApps(it) },
                                                    onSaveApps = { ctx, apps -> viewModel.freeze.saveFreezeSelectedApps(ctx, apps) },
                                                    onAppToggle = { ctx, pkg, enabled ->
                                                        viewModel.freeze.updateFreezeAppEnabled(
                                                            ctx,
                                                            pkg,
                                                            enabled,
                                                        )
                                                    },
                                                )
                                            },
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightKey = highlightSetting,
                                        )
                                    }

                                    "Quick settings tiles" -> {
                                        QuickSettingsTilesSettingsUI(
                                            modifier = Modifier.fillMaxSize(),
                                            highlightSetting = highlightSetting,
                                            contentPadding =
                                                PaddingValues(
                                                    top = statusBarHeight,
                                                    bottom = 150.dp,
                                                ),
                                        )
                                    }

                                    "System Keyboard" -> {
                                        KeyboardSettingsUI(
                                            state = viewModel.input,
                                            isKeyboardEnabled = viewModel.isKeyboardEnabled.value,
                                            isKeyboardSelected = viewModel.isKeyboardSelected.value,
                                            onRefresh = { viewModel.check(context) },
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Batteries" -> {
                                        BatteriesSettingsUI(
                                            state = viewModel.battery,
                                            isPostNotificationsEnabled = viewModel.isPostNotificationsEnabled,
                                            isBluetoothPermissionGranted = viewModel.isBluetoothPermissionGranted,
                                            modifier = Modifier.padding(top = 16.dp),
                                        )
                                    }

                                    "Widget stack" -> {
                                        WidgetStackSettingsUI(
                                            modifier = Modifier.padding(top = 16.dp),
                                        )
                                    }

                                    "Toolkit On Display" -> {
                                        ToolkitOnDisplaySettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Maps power saving mode" -> {
                                        MapsPowerSavingSettingsUI(
                                            state = viewModel.power,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Duo" -> {
                                        DuoSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Island" -> {
                                        IslandSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    ISLAND_PLACEMENT_FEATURE_ID -> {
                                        IslandPlacementSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    ISLAND_VISUALS_FEATURE_ID -> {
                                        IslandVisualsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    ISLAND_DURATION_FEATURE_ID -> {
                                        IslandDurationSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    ISLAND_BEHAVIOR_FEATURE_ID -> {
                                        IslandBehaviorSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    ISLAND_NOTIFICATIONS_FEATURE_ID -> {
                                        IslandNotificationSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Status glance" -> {
                                        StatusGlanceSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Flashlight pulse" -> {
                                        FlashlightPulseSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Text and animations" -> {
                                        TextAnimationsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Screen refresh rate" -> {
                                        RefreshRateSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "AOD wallpaper" -> {
                                        AodWallpaperSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Always on Display" -> {
                                        AlwaysOnDisplaySettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "LiveWallpaper" -> {
                                        LiveWallpaperSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Navigation" -> {
                                        NavigationSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    BACK_GESTURE_FEATURE_ID -> {
                                        BackGestureSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Other customizations" -> {
                                        OtherCustomizationsSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Lock screen clock" -> {
                                        LockScreenClockSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Shut-Up!" -> {
                                        ShutUpSettingsUI(
                                            viewModel = viewModel,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightKey = highlightSetting,
                                        )
                                    }

                                    "Power and Battery" -> {
                                        PowerAndBatterySettingsUI(
                                            state = viewModel.power,
                                            isWriteSecureSettingsEnabled = viewModel.isWriteSecureSettingsEnabled.value,
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Networks" -> {
                                        NetworksSettingsUI(
                                            state = viewModel.connectivity,
                                            isShizukuAvailable = viewModel.isShizukuAvailable.value,
                                            isShellGranted =
                                                (viewModel.isShizukuAvailable.value && viewModel.isShizukuPermissionGranted.value) ||
                                                    (viewModel.isRootAvailable.value && viewModel.isRootPermissionGranted.value),
                                            isWriteSecureSettingsEnabled = viewModel.isWriteSecureSettingsEnabled.value,
                                            onRequestShizukuPermission = { viewModel.requestShizukuPermission() },
                                            modifier = Modifier.padding(top = 16.dp),
                                            highlightSetting = highlightSetting,
                                        )
                                    }

                                    "Standby apps" -> {
                                        StandbyAppsSettingsUI(
                                            state = viewModel.power,
                                            isShizukuAvailable = viewModel.isShizukuAvailable.value,
                                            isShizukuPermissionGranted = viewModel.isShizukuPermissionGranted.value,
                                            isRootAvailable = viewModel.isRootAvailable.value,
                                            isRootPermissionGranted = viewModel.isRootPermissionGranted.value,
                                            onRequestShizukuPermission = { viewModel.requestShizukuPermission() },
                                            modifier = Modifier.padding(top = 16.dp),
                                            selectedPackages = standbyAppsSelectedPackages,
                                            onSelectionChange = { standbyAppsSelectedPackages = it },
                                            showMoveSheet = isStandbyMoveSheetVisible,
                                            onShowMoveSheetChange = { isStandbyMoveSheetVisible = it },
                                        )
                                    }
                                }
                            }
                            // Bottom padding for toolbar
                            if (featureId != "Quick settings tiles" && featureId != "Location reached") {
                                androidx.compose.foundation.layout.Spacer(
                                    modifier =
                                        Modifier.height(
                                            150.dp,
                                        ),
                                )
                            }
                        }

                        val isStandbyMultiSelecting = featureId == "Standby apps" && standbyAppsSelectedPackages.isNotEmpty()

                        ToolkitFloatingToolbar(
                            title =
                                if (isStandbyMultiSelecting) {
                                    stringResource(R.string.standby_apps_selected_count, standbyAppsSelectedPackages.size)
                                } else {
                                    pageTitle
                                },
                            isBeta = if (isStandbyMultiSelecting) false else (featureObj?.isBeta ?: false),
                            onBackClick = {
                                if (isStandbyMultiSelecting) {
                                    standbyAppsSelectedPackages = emptySet()
                                } else {
                                    finish()
                                }
                            },
                            fabIconRes =
                                when {
                                    isStandbyMultiSelecting -> R.drawable.rounded_mobiledata_arrows_24
                                    (featureId == "Island" || featureId in ISLAND_SUB_PAGE_IDS) -> R.drawable.rounded_play_arrow_24
                                    else -> null
                                },
                            fabAction =
                                when {
                                    isStandbyMultiSelecting -> { { isStandbyMoveSheetVisible = true } }
                                    (featureId == "Island" || featureId in ISLAND_SUB_PAGE_IDS) -> {
                                        {
                                            viewModel.triggerIslandPreview(context)
                                        }
                                    }
                                    else -> null
                                },
                            fabContentDescription =
                                when {
                                    isStandbyMultiSelecting -> stringResource(R.string.action_move_bucket)
                                    (featureId == "Island" || featureId in ISLAND_SUB_PAGE_IDS) -> stringResource(R.string.action_preview)
                                    else -> null
                                },
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .zIndex(1f),
                            onHelpClick =
                                if (isStandbyMultiSelecting || (featureId == "Island" || featureId in ISLAND_SUB_PAGE_IDS)) {
                                    null
                                } else {
                                    {
                                        if (hasMenu) {
                                            selectedHelpFeature = featureObj
                                            showHelpSheet = true
                                        } else {
                                            showInstructionsSheet = true
                                        }
                                    }
                                },
                        )
                    }

                    if (featureId == "Activity launcher") {
                        ActivityLauncherSearchBar(
                            query = activityLauncherQuery,
                            onQueryChange = { activityLauncherQuery = it },
                            modifier =
                                Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(start = 16.dp, end = 16.dp, top = statusBarHeight + 8.dp),
                        )
                    }
                    }
                }
            }
        }
    }
}
