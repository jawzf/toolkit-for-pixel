package com.jawzf.pixeltoolkit

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.app.ActivityCompat
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.ConfigPickerItem
import com.jawzf.pixeltoolkit.core.design.cards.FeatureCard
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.cards.PermissionCard
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.components.SegmentedDropdownMenuItem
import com.jawzf.pixeltoolkit.core.design.components.ToolkitFloatingToolbar
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.AppHapticMode
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.BlurDirection
import com.jawzf.pixeltoolkit.core.design.modifiers.liquidRipple
import com.jawzf.pixeltoolkit.core.design.modifiers.progressiveBlur
import com.jawzf.pixeltoolkit.core.design.modifiers.scrollMotionBlur
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedPicker
import com.jawzf.pixeltoolkit.core.design.sheets.InstructionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.theme.Shapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.core.platform.auth.BiometricHelper
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionGrantUtil
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.domain.DIYTabs
import com.jawzf.pixeltoolkit.domain.registry.FeatureRegistry
import com.jawzf.pixeltoolkit.domain.registry.PermissionRegistry
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.apps.updates.github.GitHubAuthViewModel
import com.jawzf.pixeltoolkit.feature.apps.updates.ui.GitHubAuthSheet
import com.jawzf.pixeltoolkit.ui.components.dialogs.AboutSection
import com.jawzf.pixeltoolkit.ui.core.pickers.AppIconPicker
import com.jawzf.pixeltoolkit.ui.core.pickers.DefaultTabPicker
import com.jawzf.pixeltoolkit.ui.core.pickers.PrivilegedModePicker
import com.jawzf.pixeltoolkit.ui.core.sheets.BugReportBottomSheet
import com.jawzf.pixeltoolkit.ui.core.sheets.ImportConfigConfirmationSheet
import com.jawzf.pixeltoolkit.ui.core.sheets.ShakeForHelp
import com.jawzf.pixeltoolkit.ui.core.sheets.UnsupportedFeaturesConfirmationSheet
import com.jawzf.pixeltoolkit.utils.PermissionUIHelper
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class SettingsActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    private val shizukuPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                viewModel.check(this)
            }
        }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isDarkMode =
            (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        window.setBackgroundDrawableResource(if (isDarkMode) android.R.color.black else R.color.app_window_background)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        // Register Shizuku permission listener
        Shizuku.addRequestPermissionResultListener(shizukuPermissionResultListener)
        setContent {
            val isPitchBlackThemeEnabled by viewModel.isPitchBlackThemeEnabled
            ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                val context = LocalContext.current
                LocalView.current

                var showBugReportSheet by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    viewModel.check(context)
                }

                if (showBugReportSheet) {
                    BugReportBottomSheet(
                        viewModel = viewModel,
                        onDismissRequest = { showBugReportSheet = false },
                    )
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

                val isBlurEnabled by viewModel.isBlurEnabled
                val isRippleEnabled by viewModel.isRippleEnabled
                var iconRippleTrigger by remember { mutableStateOf(0) }
                var iconRippleOrigin by remember { mutableStateOf(Offset.Zero) }

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .liquidRipple(
                                trigger = iconRippleTrigger,
                                origin = iconRippleOrigin,
                                enabled = isRippleEnabled,
                                durationMillis = 2800,
                                amplitudeDp = 34f,
                                frequency = 12f,
                                decay = 4.5f,
                                speedDp = 1400f,
                            )
                            .progressiveBlur(
                                blurRadius = if (isBlurEnabled) 40f else 0f,
                                height = statusBarHeightPx * 1.15f,
                                direction = BlurDirection.TOP,
                            ),
                ) {
                    val contentPadding =
                        PaddingValues(
                            top = statusBarHeight,
                            bottom = 150.dp,
                            start = 16.dp,
                            end = 16.dp,
                        )

                    val expandPermissions = intent.getBooleanExtra("expand_permissions", false)

                    SettingsContent(
                        viewModel = viewModel,
                        contentPadding = contentPadding,
                        expandPermissionsInitial = expandPermissions,
                        onAppIconSelectedWithPosition = { _, pos ->
                            iconRippleOrigin = pos
                            iconRippleTrigger++
                        },
                        onAvatarLongClickWithPosition = { pos ->
                            iconRippleOrigin = pos
                            iconRippleTrigger++
                        },
                        onRippleToggleEnabledWithPosition = { pos ->
                            iconRippleOrigin = pos
                            iconRippleTrigger++
                        },
                        modifier =
                            Modifier
                                .progressiveBlur(
                                    blurRadius = if (isBlurEnabled) 40f else 0f,
                                    height = with(LocalDensity.current) { 150.dp.toPx() },
                                    direction = BlurDirection.BOTTOM,
                                ),
                    )

                    ToolkitFloatingToolbar(
                        title = stringResource(R.string.label_settings),
                        onBackClick = { finish() },
                        modifier =
                            Modifier
                                .align(Alignment.BottomCenter)
                                .zIndex(1f),
                        fabAction = { showBugReportSheet = true },
                        fabIconRes = R.drawable.rounded_bug_report_24,
                        fabContentDescription = stringResource(R.string.action_report_bug),
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.check(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionResultListener)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode in 1001..1007) {
            viewModel.check(this)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    viewModel: MainViewModel,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    expandPermissionsInitial: Boolean = false,
    onAppIconSelectedWithPosition: ((com.jawzf.pixeltoolkit.domain.model.AppIcon, Offset) -> Unit)? = null,
    onAvatarLongClickWithPosition: ((Offset) -> Unit)? = null,
    onRippleToggleEnabledWithPosition: ((Offset) -> Unit)? = null,
) {
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled
    val isWriteSecureSettingsEnabled by viewModel.isWriteSecureSettingsEnabled
    val isPostNotificationsEnabled by viewModel.isPostNotificationsEnabled
    val isReadPhoneStateEnabled by viewModel.isReadPhoneStateEnabled
    val isShizukuPermissionGranted by viewModel.isShizukuPermissionGranted
    val isShizukuAvailable by viewModel.isShizukuAvailable
    val isOverlayPermissionGranted by viewModel.isOverlayPermissionGranted
    val isNotificationListenerEnabled by viewModel.isNotificationListenerEnabled
    val isDefaultBrowserSet by viewModel.isDefaultBrowserSet
    val isWriteSettingsEnabled by viewModel.isWriteSettingsEnabled
    val isNotificationPolicyAccessGranted by viewModel.isNotificationPolicyAccessGranted
    val isLocationPermissionGranted by viewModel.isLocationPermissionGranted
    val isBackgroundLocationPermissionGranted by viewModel.isBackgroundLocationPermissionGranted
    val isDeviceAdminEnabled by viewModel.isDeviceAdminEnabled
    val isCalendarPermissionGranted by viewModel.isCalendarPermissionGranted
    val isBubblePermissionGranted by viewModel.isBubblePermissionGranted
    val isUsageStatsPermissionGranted by viewModel.isUsageStatsPermissionGranted
    val isStoragePermissionGranted by viewModel.isStoragePermissionGranted
    val context = LocalContext.current
    val hapticMode by HapticUtil.hapticMode
    var isPermissionsExpanded by remember { mutableStateOf(expandPermissionsInitial) }
    val isRootEnabled by viewModel.isRootEnabled
    val isRootPermissionGranted by viewModel.isRootPermissionGranted
    val isDeveloperModeEnabled by viewModel.isDeveloperModeEnabled
    val grantScope = rememberCoroutineScope()
    var showInstructionsSheet by remember { mutableStateOf(false) }
    var showShizukuHelpBottomSheet by remember { mutableStateOf(false) }
    var showHiddenDebuggingHelpSheet by remember { mutableStateOf(false) }
    var showUnsupportedFeaturesSheet by remember { mutableStateOf(false) }
    var showImportConfirmSheet by remember { mutableStateOf(false) }
    var showImportBackupPicker by remember { mutableStateOf(false) }
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }

    var showGitHubAuthSheet by remember { mutableStateOf(false) }
    val gitHubAuthViewModel: GitHubAuthViewModel =
        androidx.lifecycle.viewmodel.compose
            .viewModel()
    val settingsRepo =
        remember {
            com.jawzf.pixeltoolkit.data.repository
                .SettingsRepository(context)
        }
    var currentUser by remember { mutableStateOf(AppsSettings(context).getGitHubUser()) }

    val onImportConfig: (Boolean) -> Unit = { keepPrefs ->
        selectedImportUri?.let { uri ->
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    if (viewModel.importConfigs(context, inputStream, keepPrefs)) {
                        Toast
                            .makeText(context, "Config imported successfully", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast
                            .makeText(context, "Failed to import config", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to import config", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                selectedImportUri = null
                showImportConfirmSheet = false
            }
        }
    }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("application/json"),
        ) { uri ->
            uri?.let {
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        viewModel.exportConfigs(context, outputStream)
                        Toast
                            .makeText(context, "Config exported successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to export config", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            uri?.let {
                selectedImportUri = it
                showImportConfirmSheet = true
            }
        }

    if (showInstructionsSheet) {
        InstructionsBottomSheet(
            onDismissRequest = { showInstructionsSheet = false },
        )
    }

    if (showUnsupportedFeaturesSheet) {
        UnsupportedFeaturesConfirmationSheet(
            onDismissRequest = { showUnsupportedFeaturesSheet = false },
            onConfirm = {
                val activity = context as? androidx.fragment.app.FragmentActivity
                val canAuthenticate =
                    androidx.biometric.BiometricManager
                        .from(context)
                        .canAuthenticate(BiometricHelper.allowedAuthenticators) ==
                        androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS
                if (activity == null || !canAuthenticate) {
                    showUnsupportedFeaturesSheet = false
                    viewModel.setEnableUnsupportedFeatures(true, context)
                } else {
                    BiometricHelper.showBiometricPrompt(
                        activity = activity,
                        title = context.getString(R.string.unsupported_auth_title),
                        subtitle = context.getString(R.string.unsupported_auth_subtitle),
                        onSuccess = {
                            showUnsupportedFeaturesSheet = false
                            viewModel.setEnableUnsupportedFeatures(true, context)
                        },
                    )
                }
            },
            featureTitleResIds = FeatureRegistry.getUnsupportedFeatures(context).map { it.title },
        )
    }

    if (showImportBackupPicker) {
        com.jawzf.pixeltoolkit.ui.features.system.ImportBackupPickerSheet(
            onDismissRequest = { showImportBackupPicker = false },
            onBackupSelected = {
                showImportBackupPicker = false
                selectedImportUri = it
                showImportConfirmSheet = true
            },
            onPickCustom = {
                showImportBackupPicker = false
                importLauncher.launch(arrayOf("application/json"))
            },
        )
    }

    if (showImportConfirmSheet) {
        ImportConfigConfirmationSheet(
            onDismissRequest = {
                showImportConfirmSheet = false
                selectedImportUri = null
            },
            onConfirmOverride = {
                onImportConfig(false)
            },
            onConfirmMerge = {
                onImportConfig(true)
            },
        )
    }

    ShakeForHelp(viewModel = viewModel)

    if (showHiddenDebuggingHelpSheet) {
        com.jawzf.pixeltoolkit.ui.core.sheets.HiddenDebuggingHelpBottomSheet(
            onDismissRequest = { showHiddenDebuggingHelpSheet = false },
            onAutoDetected = { viewModel.setHiddenDebuggingSupport(true) },
        )
    }

    if (showShizukuHelpBottomSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showShizukuHelpBottomSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Shizuku Auth Token",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "In shizuku, in the 'Control Shizuku with automation apps' section, Open 'View intents' and copy and paste the auth token from 'Extras' section.\n\nThis allows Toolkit to automate and re-start Shizuku on demand in features such as Shut-Up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = { showShizukuHelpBottomSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Got it")
                }
            }
        }
    }

    val isMotionBlurEnabled by viewModel.isMotionBlurEnabled
    val scrollState = rememberScrollState()
    var permissionsSectionY by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(expandPermissionsInitial, permissionsSectionY) {
        if (expandPermissionsInitial && permissionsSectionY != null) {
            scrollState.animateScrollTo(permissionsSectionY!!.toInt())
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .scrollMotionBlur(scrollState, enabled = isMotionBlurEnabled)
                .verticalScroll(scrollState)
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        val view = LocalView.current

        // Help & Guides
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_help_24,
                title = stringResource(R.string.label_help_guide),
                showToggle = false,
                onClick = {
                    showInstructionsSheet = true
                },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Customizations 
        Text(
            text = stringResource(R.string.settings_section_customizations),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            val selectedAppIcon by viewModel.selectedAppIcon
            AppIconPicker(
                selectedIcon = selectedAppIcon,
                onIconSelected = { viewModel.setAppIcon(it, context) },
                onIconSelectedWithPosition = onAppIconSelectedWithPosition,
            )

            val defaultTab by viewModel.defaultTab
            val availableTabs = remember { DIYTabs.entries }
            DefaultTabPicker(
                selectedTab = defaultTab,
                onTabSelected = { viewModel.setDefaultTab(it, context) },
                options = availableTabs,
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_touch_app_24,
                title = stringResource(R.string.setting_swipe_tabs_title),
                isChecked = viewModel.isSwipeTabsEnabled.value,
                onCheckedChange = { viewModel.setSwipeTabsEnabled(it) },
            )

            val hapticModeLabels = mapOf(
                AppHapticMode.DISABLED to stringResource(R.string.haptic_mode_disabled),
                AppHapticMode.ENABLED to stringResource(R.string.haptic_mode_enabled),
                AppHapticMode.STRONGER to stringResource(R.string.haptic_mode_stronger),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                    .padding(top = 12.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_haptics_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                SegmentedPicker(
                    items = AppHapticMode.entries,
                    selectedItem = hapticMode,
                    onItemSelected = { HapticUtil.saveHapticMode(context, it) },
                    labelProvider = { hapticModeLabels.getValue(it) },
                    modifier = Modifier.fillMaxWidth(),
                    title = stringResource(R.string.label_haptic_feedback),
                )
            }

            IconToggleItem(
                iconRes = R.drawable.rounded_invert_colors_24,
                title = stringResource(R.string.setting_pitch_black_theme_title),
                isChecked = viewModel.isPitchBlackThemeEnabled.value,
                onCheckedChange = { viewModel.setPitchBlackThemeEnabled(it, context) },
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_blur_on_24,
                title = stringResource(R.string.label_use_blur),
                description = null,
                isChecked = viewModel.isBlurSettingEnabled.value,
                onCheckedChange = { viewModel.setBlurEnabled(it, context) },
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_blur_linear_24,
                title = stringResource(R.string.label_ripple_animation),
                isChecked = viewModel.isRippleSettingEnabled.value,
                onCheckedChange = { viewModel.setRippleEnabled(it, context) },
                onCheckedChangeWithPosition = { isChecked, pos ->
                    if (isChecked) {
                        onRippleToggleEnabledWithPosition?.invoke(pos)
                    }
                },
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_settings_motion_mode_24,
                title = stringResource(R.string.label_motion_blur),
                isChecked = viewModel.isMotionBlurSettingEnabled.value,
                onCheckedChange = { viewModel.setMotionBlurEnabled(it, context) },
            )

            if (viewModel.isMotionBlurSettingEnabled.value) {
                ConfigSliderItem(
                    iconRes = R.drawable.rounded_settings_motion_mode_24,
                    title = stringResource(R.string.label_motion_blur_amount),
                    value = viewModel.motionBlurScale.floatValue,
                    onValueChange = { viewModel.setMotionBlurScale(it, context) },
                    valueRange = 0.5f..2.5f,
                    increment = 0.1f,
                    showValue = false,
                )
            }

            IconToggleItem(
                iconRes = R.drawable.rounded_mobile_vibrate_24,
                title = stringResource(R.string.label_shake_for_help),
                isChecked = viewModel.isShakeForHelpEnabled.value,
                onCheckedChange = { viewModel.setShakeForHelpEnabled(it, context) },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Permissions 
        Text(
            text = stringResource(R.string.settings_section_permissions),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceBright,
                            shape = Shapes.extraSmall,
                        )
                        .onGloballyPositioned { coordinates ->
                            permissionsSectionY = coordinates.positionInParent().y
                        }
                        .clickable { isPermissionsExpanded = !isPermissionsExpanded }
                        .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_shield_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.settings_permissions_all),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Icon(
                    painter =
                        painterResource(
                            id = if (isPermissionsExpanded) R.drawable.rounded_keyboard_arrow_up_24 else R.drawable.rounded_keyboard_arrow_down_24,
                        ),
                    contentDescription = if (isPermissionsExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            AnimatedVisibility(
                visible = isPermissionsExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                val permissionItems =
                    remember(
                        isAccessibilityEnabled,
                        isWriteSecureSettingsEnabled,
                        isRootEnabled,
                        isRootPermissionGranted,
                        isShizukuPermissionGranted,
                        isShizukuAvailable,
                        isReadPhoneStateEnabled,
                        isPostNotificationsEnabled,
                        isOverlayPermissionGranted,
                        isNotificationListenerEnabled,
                        isWriteSettingsEnabled,
                        isNotificationPolicyAccessGranted,
                        isDefaultBrowserSet,
                        isUsageStatsPermissionGranted,
                        isLocationPermissionGranted,
                        isBackgroundLocationPermissionGranted,
                        isDeviceAdminEnabled,
                        isCalendarPermissionGranted,
                        isBubblePermissionGranted,
                        isStoragePermissionGranted,
                    ) {
                        PermissionUIHelper.getAllPermissionItems(context, viewModel, context as? ComponentActivity)
                    }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    permissionItems.forEach { item ->
                        PermissionCard(
                            iconRes = item.iconRes,
                            title = item.title,
                            dependentFeatures = item.dependentFeatures,
                            actionLabel = item.actionLabel ?: R.string.perm_action_grant,
                            isGranted = item.isGranted,
                            onActionClick = { item.action?.invoke() },
                            secondaryActionLabel = item.secondaryActionLabel,
                            onSecondaryActionClick = item.secondaryAction,
                            shizukuActionLabel = item.shizukuActionLabel,
                            shizukuActionEnabled = item.shizukuActionEnabled,
                            onShizukuActionClick = item.shizukuAction,
                            instructions = item.instructions,
                            description = item.description,
                        )
                    }
                }
            }

            PrivilegedModePicker(
                selectedMode = viewModel.privilegedMode.value,
                onModeSelected = { viewModel.setPrivilegedMode(it, context) },
            )

            if (viewModel.usesAuthToken.value) Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceBright,
                            shape = MaterialTheme.shapes.extraSmall,
                        ).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val view = LocalView.current
                var tokenText by remember { mutableStateOf(viewModel.shizukuAuthToken.value) }
                var isTokenVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = tokenText,
                    onValueChange = { tokenText = it },
                    label = { Text("Shizuku auth token") },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    visualTransformation = if (isTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isTokenVisible = !isTokenVisible }) {
                            Icon(
                                painter =
                                    painterResource(
                                        id = if (isTokenVisible) R.drawable.rounded_visibility_24 else R.drawable.rounded_visibility_off_24,
                                    ),
                                contentDescription = if (isTokenVisible) "Hide token" else "Show token",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                )

                Button(
                    onClick = {
                        viewModel.setShizukuAuthToken(tokenText)
                        HapticUtil.performUIHaptic(view)
                        Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_save_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }

                OutlinedButton(
                    onClick = {
                        HapticUtil.performUIHaptic(view)
                        showShizukuHelpBottomSheet = true
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_help_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceBright,
                            shape = MaterialTheme.shapes.extraSmall,
                        ).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val view = LocalView.current
                val externalSettings = remember { SettingsRepository(context) }
                var externalToken by remember { mutableStateOf(externalSettings.getExternalControlToken().orEmpty()) }
                var isExternalTokenVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = externalToken,
                    onValueChange = { externalToken = it },
                    label = { Text(stringResource(R.string.external_control_token_title)) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    visualTransformation = if (isExternalTokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        IconButton(onClick = { isExternalTokenVisible = !isExternalTokenVisible }) {
                            Icon(
                                painter =
                                    painterResource(
                                        id = if (isExternalTokenVisible) R.drawable.rounded_visibility_24 else R.drawable.rounded_visibility_off_24,
                                    ),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                )

                OutlinedButton(
                    onClick = {
                        HapticUtil.performUIHaptic(view)
                        externalToken = java.util.UUID.randomUUID().toString().replace("-", "")
                        isExternalTokenVisible = true
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_refresh_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Button(
                    onClick = {
                        externalSettings.setExternalControlToken(externalToken)
                        HapticUtil.performUIHaptic(view)
                        Toast.makeText(context, R.string.external_control_token_saved, Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.rounded_save_24),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            IconToggleItem(
                iconRes = R.drawable.rounded_adb_24,
                title = stringResource(R.string.setting_hidden_debugging_title),
                isChecked = viewModel.isHiddenDebuggingSupport.value,
                onCheckedChange = { checked ->
                    viewModel.setHiddenDebuggingSupport(checked)
                    if (checked && !ShellUtils.hasPermission(context)) {
                        if (ShizukuUtils.isShizukuAvailable()) {
                            ShizukuUtils.requestPermission()
                        } else {
                            Toast.makeText(context, R.string.shizuku_not_running_desc, Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onInfoClick = { showHiddenDebuggingHelpSheet = true },
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_data_usage_24,
                title = stringResource(R.string.setting_use_usage_access_title),
                description = stringResource(R.string.setting_use_usage_access_desc),
                isChecked = viewModel.isUseUsageAccess.value,
                onCheckedChange = { viewModel.setUseUsageAccess(it, context) },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // More
        Text(
            text = stringResource(R.string.settings_section_more),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_release_alert_24,
                title = stringResource(R.string.setting_enable_unsupported_features_title),
                description = stringResource(R.string.setting_enable_unsupported_features_desc),
                isChecked = viewModel.isEnableUnsupportedFeatures.value,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        showUnsupportedFeaturesSheet = true
                    } else {
                        viewModel.setEnableUnsupportedFeatures(false, context)
                    }
                },
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_archive_24,
                title = stringResource(R.string.setting_show_legacy_features_title),
                description = stringResource(R.string.setting_show_legacy_features_desc),
                isChecked = viewModel.isShowLegacyFeatures.value,
                onCheckedChange = { enabled -> viewModel.setShowLegacyFeatures(enabled, context) },
            )

            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceBright,
                            shape = Shapes.extraSmall,
                        ).padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        val timeStamp =
                            SimpleDateFormat(
                                "yyyyMMdd_HHmmss",
                                Locale.getDefault(),
                            ).format(Date())
                        exportLauncher.launch("toolkit_config_$timeStamp.json")
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp),
                    shape = ButtonGroupDefaults.connectedLeadingButtonShapes().shape,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_vertical_align_top_24),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_export_config))
                }
                Button(
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        if (com.jawzf.pixeltoolkit.utils.AutoBackupManager.canListBackups(context)) {
                            showImportBackupPicker = true
                        } else {
                            importLauncher.launch(arrayOf("application/json"))
                        }
                    },
                    modifier =
                        Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 44.dp),
                    shape = ButtonGroupDefaults.connectedTrailingButtonShapes().shape,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.rounded_vertical_align_bottom_24),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_import_config))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // GitHub Section
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.settings_github_section),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        RoundedCardContainer {
            // GitHub Account Card (Tap to Sign In when logged out, Long Press to Sign Out when logged in)
            FeatureCard(
                title = if (currentUser != null) "@${currentUser?.login}" else stringResource(R.string.action_sign_in_github),
                description =
                    if (currentUser !=
                        null
                    ) {
                        "Logged in as ${currentUser?.name ?: currentUser?.login}"
                    } else {
                        stringResource(R.string.settings_github_sign_in_desc)
                    },
                isEnabled = true,
                onToggle = {},
                onClick = {
                    if (currentUser == null) {
                        HapticUtil.performUIHaptic(view)
                        showGitHubAuthSheet = true
                    }
                },
                showToggle = false,
                iconRes = R.drawable.brand_github,
                additionalMenuItems =
                    if (currentUser != null) {
                        @Composable { onDismiss ->
                            SegmentedDropdownMenuItem(
                                text = { Text("Sign Out") },
                                onClick = {
                                    onDismiss()
                                    HapticUtil.performUIHaptic(view)
                                    gitHubAuthViewModel.signOut(context)
                                    currentUser = null
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.rounded_logout_24),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                            )
                        }
                    } else {
                        null
                    },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        RoundedCardContainer {
            FeatureCard(
                title = R.string.action_restart_systemui,
                description = R.string.desc_restart_systemui,
                isEnabled = true,
                onToggle = {},
                showToggle = false,
                onClick = { viewModel.restartSystemUI() },
                iconRes = R.drawable.rounded_refresh_24,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RoundedCardContainer {
            AboutSection(
                onVersionLongClick = {
                    val newState = !isDeveloperModeEnabled
                    viewModel.setDeveloperModeEnabled(newState, context)
                    Toast
                        .makeText(
                            context,
                            if (newState) "Developer options enabled" else "Developer options disabled",
                            Toast.LENGTH_SHORT,
                        ).show()
                },
            )
        }

        if (isDeveloperModeEnabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Updates Section
            Text(
                text = "Developer Options",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer {
                com.jawzf.pixeltoolkit.ui.features.system.AutoBackupSetting()

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceBright,
                                shape = Shapes.extraSmall,
                            ).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            Toast.makeText(context, context.getString(R.string.toast_granting_permissions), Toast.LENGTH_SHORT).show()
                            grantScope.launch {
                                val done = withContext(Dispatchers.IO) { PermissionGrantUtil.grantAll(context) }
                                viewModel.check(context)
                                Toast.makeText(
                                    context,
                                    context.getString(if (done) R.string.toast_permissions_granted else R.string.shizuku_not_running_desc),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            }
                        },
                        enabled = isShizukuAvailable && isShizukuPermissionGranted,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_shield_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_grant_all_permissions))
                    }

                    Button(
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.resetOnboarding(context)
                            Toast.makeText(context, context.getString(R.string.toast_onboarding_reset), Toast.LENGTH_SHORT).show()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_refresh_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_reset_onboarding))
                    }

                    Button(
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.clearRecentSearches()
                            Toast.makeText(context, context.getString(R.string.toast_search_history_cleared), Toast.LENGTH_SHORT).show()
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_delete_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.btn_clear_search_history))
                    }
                }

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceBright,
                                shape = Shapes.extraSmall,
                            ).padding(16.dp),
                ) {
                    Button(
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            throw RuntimeException("Simulated crash from Developer Options")
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 44.dp),
                        shape = ButtonDefaults.shape,
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                            ),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.rounded_bug_report_24),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.simulate_crash),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                IconToggleItem(
                    iconRes = R.drawable.rounded_settings_accessibility_24,
                    title = stringResource(R.string.feat_auto_accessibility_title),
                    description = stringResource(R.string.feat_auto_accessibility_desc),
                    isChecked = viewModel.isAutoAccessibilityEnabled.value,
                    onCheckedChange = { viewModel.setAutoAccessibilityEnabled(it, context) },
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showGitHubAuthSheet) {
        GitHubAuthSheet(
            viewModel = gitHubAuthViewModel,
            onDismissRequest = {
                showGitHubAuthSheet = false
                currentUser = AppsSettings(context).getGitHubUser()
            },
        )
    }
}
