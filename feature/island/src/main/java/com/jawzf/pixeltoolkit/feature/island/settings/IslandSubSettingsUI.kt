package com.jawzf.pixeltoolkit.feature.island.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.pickers.ColorSwatchPicker
import com.jawzf.pixeltoolkit.core.design.pickers.ISLAND_ICON_PASTEL_COLORS
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedPicker
import com.jawzf.pixeltoolkit.core.design.sheets.HideStatusBarInfoBottomSheet
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.island.IslandState
import com.jawzf.pixeltoolkit.feature.island.settings.AppPickerRequest
import com.jawzf.pixeltoolkit.feature.island.settings.IslandHideIconsToggle
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.HorizontalSlideModeSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandAlarmOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandBorderOutlineOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandBriefOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandCallOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandDevicesBatteryBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandNotificationHubOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandPrefToggle
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandPulseShadowOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandTimeOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandTimerOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.horizontalSlideDescription
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.rememberIslandShowsWhileLocked

const val ISLAND_PLACEMENT_FEATURE_ID = "Island placement"
const val ISLAND_VISUALS_FEATURE_ID = "Island visuals"
const val ISLAND_DURATION_FEATURE_ID = "Island duration"
const val ISLAND_BEHAVIOR_FEATURE_ID = "Island behavior"
const val ISLAND_NOTIFICATIONS_FEATURE_ID = "Island notifications"

val ISLAND_SUB_PAGE_IDS = setOf(
    ISLAND_PLACEMENT_FEATURE_ID,
    ISLAND_VISUALS_FEATURE_ID,
    ISLAND_DURATION_FEATURE_ID,
    ISLAND_BEHAVIOR_FEATURE_ID,
    ISLAND_NOTIFICATIONS_FEATURE_ID,
)

internal val ISLAND_SUB_PAGES = listOf(
    Triple(ISLAND_PLACEMENT_FEATURE_ID, R.string.island_section_placement, R.drawable.rounded_center_focus_strong_24),
    Triple(ISLAND_VISUALS_FEATURE_ID, R.string.island_section_visuals, R.drawable.rounded_palette_24),
    Triple(ISLAND_DURATION_FEATURE_ID, R.string.island_section_duration, R.drawable.rounded_timer_24),
    Triple(ISLAND_BEHAVIOR_FEATURE_ID, R.string.island_section_behavior, R.drawable.rounded_settings_motion_mode_24),
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandPlacementSettingsUI(
    state: IslandState,
    isFoldableCameraDevice: Boolean,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val displayConfiguration = androidx.compose.ui.platform.LocalConfiguration.current
    LaunchedEffect(displayConfiguration.screenWidthDp, displayConfiguration.screenHeightDp,
        displayConfiguration.orientation) {
        state.refreshIslandCameraPlacement()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IslandPreviewControls()

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            if (isFoldableCameraDevice) {
                Text(
                    text = stringResource(R.string.foldable_camera_profile_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
                IconToggleItem(
                    iconRes = R.drawable.rounded_center_focus_strong_24,
                    title = stringResource(R.string.foldable_orientation_profiles_title),
                    isChecked = state.isIslandOrientationProfiles.value,
                    onCheckedChange = { state.setIslandOrientationProfiles(it) },
                )
                IconToggleItem(
                    iconRes = R.drawable.rounded_visibility_off_24,
                    title = stringResource(R.string.foldable_hide_current_orientation_title),
                    isChecked = state.isIslandHiddenInCurrentOrientation.value,
                    onCheckedChange = { state.setIslandHiddenInCurrentOrientation(it) },
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                    .padding(top = 12.dp)
                    .highlight(highlightSetting == "island_camera_position"),
            ) {
                Text(
                    text = stringResource(R.string.island_camera_position_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val positions = listOf(
                    IslandSettings.ISLAND_CAMERA_POSITION_LEFT,
                    IslandSettings.ISLAND_CAMERA_POSITION_CENTER,
                    IslandSettings.ISLAND_CAMERA_POSITION_RIGHT,
                )
                val positionLabels = mapOf(
                    IslandSettings.ISLAND_CAMERA_POSITION_LEFT to stringResource(R.string.island_camera_position_left),
                    IslandSettings.ISLAND_CAMERA_POSITION_CENTER to stringResource(R.string.island_camera_position_center),
                    IslandSettings.ISLAND_CAMERA_POSITION_RIGHT to stringResource(R.string.island_camera_position_right),
                )
                SegmentedPicker(
                    items = positions,
                    selectedItem = state.islandCameraPosition.value,
                    onItemSelected = { state.setIslandCameraPosition(it) },
                    labelProvider = { positionLabels[it].orEmpty() },
                    title = R.string.island_camera_position_title,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            LaunchedEffect(Unit) {
                if (state.isIslandAutoDetect.value) state.autoAlignIslandWithCamera(context)
            }

            Button(
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.autoAlignIslandWithCamera(context)
                    Toast.makeText(context, R.string.island_auto_align_toast, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .highlight(highlightSetting == "island_use_auto_detect"),
            ) {
                Icon(
                    painter = painterResource(R.drawable.rounded_center_focus_strong_24),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = stringResource(R.string.island_auto_align_camera))
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                ConfigSliderItem(
                    title = stringResource(R.string.island_camera_offset_x_title),
                    value = state.islandCameraOffsetX.floatValue,
                    onValueChange = {
                        HapticUtil.performUIHaptic(view)
                        state.setIslandCameraOffsetX(it)
                    },
                    valueRange = 0f..100f,
                    increment = 1f,
                    iconRes = R.drawable.rounded_border_left_24,
                    valueFormatter = { "%.1f%%".format(it) },
                    allowDecimals = true,
                )
                ConfigSliderItem(
                    title = stringResource(R.string.island_camera_offset_y_title),
                    value = state.islandCameraOffsetY.floatValue,
                    onValueChange = {
                        HapticUtil.performUIHaptic(view)
                        state.setIslandCameraOffsetY(it)
                    },
                    valueRange = 0f..20f,
                    increment = 0.5f,
                    iconRes = R.drawable.rounded_border_top_24,
                    valueFormatter = { "%.1f%%".format(it) },
                    allowDecimals = true,
                )
            }

            ConfigSliderItem(
                title = stringResource(R.string.island_camera_size_title),
                value = state.islandCameraSize.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandCameraSize(it)
                },
                valueRange = 0.05f..2.0f,
                increment = 0.05f,
                iconRes = R.drawable.rounded_arrows_outward_24,
                valueFormatter = { "%.2fx".format(it) },
                allowDecimals = true,
                modifier = Modifier.highlight(highlightSetting == "island_camera_size"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_max_width_title),
                value = state.islandMaxWidth.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandMaxWidth(it)
                },
                valueRange = 150f..500f,
                increment = 10f,
                iconRes = R.drawable.rounded_arrows_outward_24,
                valueFormatter = { "%.1f dp".format(it) },
                allowDecimals = true,
                modifier = Modifier.highlight(highlightSetting == "island_max_width"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_expanded_width_title),
                value = state.islandExpandedWidth.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandExpandedWidth(it)
                },
                valueRange = 200f..500f,
                increment = 10f,
                iconRes = R.drawable.rounded_arrows_outward_24,
                valueFormatter = { "%.1f dp".format(it) },
                allowDecimals = true,
                modifier = Modifier.highlight(highlightSetting == "island_expanded_width"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_cutout_gap_title),
                value = state.islandCutoutGap.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandCutoutGap(it)
                },
                valueRange = 0f..16f,
                increment = 1f,
                iconRes = R.drawable.rounded_arrows_outward_24,
                valueFormatter = { "%.1f dp".format(it) },
                allowDecimals = true,
                modifier = Modifier.highlight(highlightSetting == "island_cutout_gap"),
            )
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_mobile_rotate_24,
                title = stringResource(R.string.island_keep_on_landscape_title),
                isChecked = state.isIslandKeepOnLandscape.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandKeepOnLandscape(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_keep_on_landscape"),
            )

            if (state.isIslandKeepOnLandscape.value) {
                ConfigSliderItem(
                    title = stringResource(R.string.island_landscape_top_spacing_title),
                    value = state.islandLandscapeTopSpacing.floatValue,
                    onValueChange = {
                        HapticUtil.performUIHaptic(view)
                        state.setIslandLandscapeTopSpacing(it)
                    },
                    valueRange = 0f..60f,
                    increment = 0.5f,
                    iconRes = R.drawable.rounded_vertical_align_top_24,
                    valueFormatter = { "%.1f dp".format(it) },
                    modifier = Modifier.highlight(highlightSetting == "island_landscape_top_spacing"),
                )

                IconToggleItem(
                    iconRes = R.drawable.rounded_vertical_align_top_24,
                    title = stringResource(R.string.island_bond_edge_title),
                    isChecked = state.isIslandBondEdge.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandBondEdge(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "island_bond_edge"),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandVisualsSettingsUI(
    state: IslandState,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showBorderOutlineSheet by remember { mutableStateOf(false) }
    var showPulseShadowSheet by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IslandPreviewControls()

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            ConfigSliderItem(
                title = stringResource(R.string.island_expanded_scale_title),
                description = stringResource(R.string.island_expanded_scale_desc),
                value = state.islandExpandedScale.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandExpandedScale(it)
                },
                valueRange = 1f..1.3f,
                increment = 0.02f,
                iconRes = R.drawable.rounded_magnify_fullscreen_24,
                valueFormatter = { "${(it * 100).toInt()}%" },
                modifier = Modifier.highlight(highlightSetting == "island_expanded_scale"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_font_scale_title),
                value = state.islandFontScale.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandFontScale(it)
                },
                valueRange = 0.8f..1.3f,
                increment = 0.05f,
                iconRes = R.drawable.rounded_format_size_24,
                valueFormatter = { "${(it * 100).toInt()}%" },
                modifier = Modifier.highlight(highlightSetting == "island_font_scale"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_expanded_roundness_title),
                value = state.islandExpandedRoundness.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandExpandedRoundness(it)
                },
                valueRange = 0f..80f,
                increment = 2f,
                iconRes = R.drawable.rounded_rounded_corner_24,
                valueFormatter = { "${it.toInt()} dp" },
                modifier = Modifier.highlight(highlightSetting == "island_expanded_roundness"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_expanded_padding_title),
                value = state.islandExpandedPadding.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandExpandedPadding(it)
                },
                valueRange = 8f..48f,
                increment = 2f,
                iconRes = R.drawable.rounded_screenshot_region_24,
                valueFormatter = { "${it.toInt()} dp" },
                modifier = Modifier.highlight(highlightSetting == "island_expanded_padding"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_expanded_top_padding_title),
                value = state.islandExpandedTopPadding.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandExpandedTopPadding(it)
                },
                valueRange = 0f..40f,
                increment = 2f,
                iconRes = R.drawable.rounded_vertical_align_top_24,
                valueFormatter = { "${it.toInt()} dp" },
                modifier = Modifier.highlight(highlightSetting == "island_expanded_top_padding"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.island_expanded_bottom_padding_title),
                value = state.islandExpandedBottomPadding.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandExpandedBottomPadding(it)
                },
                valueRange = 0f..40f,
                increment = 2f,
                iconRes = R.drawable.rounded_vertical_align_bottom_24,
                valueFormatter = { "${it.toInt()} dp" },
                modifier = Modifier.highlight(highlightSetting == "island_expanded_bottom_padding"),
            )
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_square_24,
                title = stringResource(R.string.island_border_outline_title),
                description = stringResource(R.string.island_border_outline_desc),
                isChecked = state.isIslandBorderOutline.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandBorderOutline(checked)
                },
                onSettingsClick = { showBorderOutlineSheet = true },
                modifier = Modifier.highlight(highlightSetting == "island_border_outline_enabled"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_blur_on_24,
                title = stringResource(R.string.island_show_glow_title),
                isChecked = state.isIslandShowGlow.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandShowGlow(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_show_glow"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_blur_on_24,
                title = stringResource(R.string.island_pulse_shadow_title),
                description = stringResource(R.string.island_pulse_shadow_desc),
                isChecked = state.isIslandPulseShadow.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandPulseShadow(checked)
                },
                onSettingsClick = { showPulseShadowSheet = true },
                modifier = Modifier.highlight(highlightSetting == "island_pulse_shadow_on_notification"),
            )
        }

        IslandSettingsSection(R.string.island_section_icon_color) {
            val iconSettings = remember { IslandSettings(context) }
            var colorMode by remember {
                mutableStateOf(
                    iconSettings.getString(IslandSettings.KEY_ISLAND_ICON_COLOR_MODE, null) ?: IslandSettings.ISLAND_ICON_COLOR_DYNAMIC,
                )
            }
            var materialYou by remember { mutableStateOf(iconSettings.getBoolean(IslandSettings.KEY_ISLAND_ICON_MATERIAL_YOU, true)) }
            var customColor by remember {
                mutableStateOf(iconSettings.getString(IslandSettings.KEY_ISLAND_ICON_COLOR_CUSTOM, null) ?: "#FFFFFF")
            }
            val modeLabels = mapOf(
                IslandSettings.ISLAND_ICON_COLOR_DYNAMIC to stringResource(R.string.island_icon_color_dynamic),
                IslandSettings.ISLAND_ICON_COLOR_CUSTOM to stringResource(R.string.island_icon_color_custom),
            )
            SegmentedPicker(
                items = modeLabels.keys.toList(),
                selectedItem = colorMode,
                onItemSelected = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    colorMode = it
                    iconSettings.putString(IslandSettings.KEY_ISLAND_ICON_COLOR_MODE, it)
                },
                labelProvider = { modeLabels[it].orEmpty() },
                title = R.string.island_section_icon_color,
                modifier = Modifier.fillMaxWidth().highlight(highlightSetting == "island_icon_color_mode"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_palette_24,
                title = stringResource(R.string.island_icon_material_you_title),
                isChecked = materialYou,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    materialYou = checked
                    iconSettings.putBoolean(IslandSettings.KEY_ISLAND_ICON_MATERIAL_YOU, checked)
                },
            )
            AnimatedVisibility(
                visible = colorMode == IslandSettings.ISLAND_ICON_COLOR_CUSTOM && !materialYou,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                ColorSwatchPicker(
                    selectedColorHex = customColor,
                    onColorSelected = {
                        customColor = it
                        iconSettings.putString(IslandSettings.KEY_ISLAND_ICON_COLOR_CUSTOM, it)
                    },
                    colors = ISLAND_ICON_PASTEL_COLORS,
                )
            }

            var themedText by remember { mutableStateOf(iconSettings.getBoolean(IslandSettings.KEY_ISLAND_THEMED_TEXT, false)) }
            IconToggleItem(
                iconRes = R.drawable.rounded_palette_24,
                title = stringResource(R.string.island_themed_text_title),
                isChecked = themedText,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    themedText = checked
                    iconSettings.putBoolean(IslandSettings.KEY_ISLAND_THEMED_TEXT, checked)
                },
            )
        }
    }

    if (showBorderOutlineSheet) {
        IslandBorderOutlineOptionsBottomSheet(
            state = state,
            onDismissRequest = { showBorderOutlineSheet = false },
        )
    }

    if (showPulseShadowSheet) {
        IslandPulseShadowOptionsBottomSheet(
            state = state,
            onDismissRequest = { showPulseShadowSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandDurationSettingsUI(
    state: IslandState,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            ConfigSliderItem(
                title = stringResource(R.string.island_timeout_title),
                value = (state.islandTimeoutMs.longValue / 1000f),
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandTimeoutMs((it * 1000).toLong())
                },
                valueRange = 2f..60f,
                increment = 0.5f,
                iconRes = R.drawable.rounded_timer_24,
                valueFormatter = { "%.1fs".format(it) },
            )

            val infinityText = stringResource(R.string.island_timeout_infinity)
            ConfigSliderItem(
                title = stringResource(R.string.island_expanded_timeout_title),
                description = stringResource(R.string.island_expanded_timeout_desc),
                value = (state.islandExpandedTimeoutMs.longValue / 1000f),
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setIslandExpandedTimeoutMs((it * 1000).toLong())
                },
                valueRange = 0f..30f,
                increment = 1f,
                iconRes = R.drawable.rounded_schedule_24,
                valueFormatter = { if (it <= 0f) infinityText else "${it.toInt()}s" },
                modifier = Modifier.highlight(highlightSetting == "island_expanded_timeout_ms"),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandBehaviorSettingsUI(
    state: IslandState,
    isRootPermissionGranted: Boolean,
    isShizukuPermissionGranted: Boolean,
    permissionItemsFor: (List<String>) -> List<PermissionItem>,
    onRefreshPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val previewSettings = remember { IslandSettings(context) }
    var showWhen by remember { mutableStateOf(previewSettings.getIslandShowWhen()) }
    var requestingPermissionsFor by remember { mutableStateOf<Pair<Int, List<String>>?>(null) }
    var showHideStatusBarInfo by remember { mutableStateOf(false) }
    val hasShellPermission =
        if (ShellUtils.isRootEnabled(context)) {
            isRootPermissionGranted
        } else {
            isShizukuPermissionGranted
        }

    if (showHideStatusBarInfo) {
        HideStatusBarInfoBottomSheet(onDismissRequest = { showHideStatusBarInfo = false })
    }

    requestingPermissionsFor?.let { (titleRes, permKeys) ->
        PermissionsBottomSheet(
            onDismissRequest = {
                requestingPermissionsFor = null
                onRefreshPermissions()
            },
            featureTitle = stringResource(titleRes),
            permissions = permissionItemsFor(permKeys),
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                    .padding(top = 12.dp)
                    .highlight(highlightSetting == "island_show_when" || highlightSetting == "island_hide_when_screen_off"),
            ) {
                Text(
                    text = stringResource(R.string.island_show_when_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                val showWhenOptions = listOf(
                    IslandSettings.ISLAND_SHOW_WHEN_UNLOCKED,
                    IslandSettings.ISLAND_SHOW_WHEN_SCREEN_ON,
                    IslandSettings.ISLAND_SHOW_WHEN_ALWAYS,
                )
                val showWhenLabels = mapOf(
                    IslandSettings.ISLAND_SHOW_WHEN_UNLOCKED to stringResource(R.string.island_show_when_unlocked),
                    IslandSettings.ISLAND_SHOW_WHEN_SCREEN_ON to stringResource(R.string.island_show_when_screen_on),
                    IslandSettings.ISLAND_SHOW_WHEN_ALWAYS to stringResource(R.string.island_show_when_always),
                )
                SegmentedPicker(
                    items = showWhenOptions,
                    selectedItem = showWhen,
                    onItemSelected = {
                        showWhen = it
                        previewSettings.setIslandShowWhen(it)
                    },
                    labelProvider = { showWhenLabels[it].orEmpty() },
                    title = R.string.island_show_when_title,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_unread_24,
                title = stringResource(R.string.island_secondary_bubble_title),
                isChecked = state.isIslandSecondaryBubble.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandSecondaryBubble(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_secondary_bubble"),
            )

            val secondaryStyle = state.islandNotifQueueStyle.value
            val stackLabel = stringResource(R.string.island_notif_queue_style_stack)
            val overlapLabel = stringResource(R.string.island_notif_queue_style_overlap)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                    .padding(top = 12.dp)
                    .highlight(highlightSetting == "island_secondary_style"),
            ) {
                Text(
                    text = stringResource(R.string.island_secondary_style_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                SegmentedPicker(
                    items = listOf(
                        IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_STACK,
                        IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_OVERLAP,
                    ),
                    selectedItem = secondaryStyle,
                    onItemSelected = { state.setIslandNotifQueueStyle(it) },
                    labelProvider = { if (it == IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_OVERLAP) overlapLabel else stackLabel },
                    title = R.string.island_secondary_style_title,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            IconToggleItem(
                iconRes = R.drawable.rounded_motion_play_24,
                title = stringResource(R.string.island_line_peek_title),
                description = stringResource(R.string.island_line_peek_desc),
                isChecked = state.isIslandLineStageEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandLineStageEnabled(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_line_stage_enabled"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_format_align_center_24,
                title = stringResource(R.string.island_balance_compact_title),
                isChecked = state.isIslandBalanceCompact.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandBalanceCompact(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_balance_compact"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_touch_app_24,
                title = stringResource(R.string.island_dismiss_on_outside_title),
                isChecked = state.isIslandDismissOnOutside.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandDismissOnOutside(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_dismiss_on_outside"),
            )
        }

        IslandSettingsSection(R.string.island_section_hiding) {
            IconToggleItem(
                iconRes = R.drawable.rounded_visibility_off_24,
                title = stringResource(R.string.island_hide_in_owner_app_title),
                isChecked = state.isIslandHideInOwnerApp.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandHideInOwnerApp(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_hide_in_owner_app"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_pinch_24,
                title = stringResource(R.string.island_hide_on_shade_title),
                isChecked = state.isIslandHideOnShade.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandHideOnShade(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_hide_on_shade"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_fullscreen_portrait_24,
                title = stringResource(R.string.island_hide_in_fullscreen_title),
                isChecked = !state.isIslandKeepInFullscreen.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandKeepInFullscreen(!checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_keep_in_fullscreen"),
            )

            AnimatedVisibility(
                visible = state.isIslandKeepInFullscreen.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_do_not_touch_24,
                    title = stringResource(R.string.disable_interactions_in_full_screen_title),
                    isChecked = state.isIslandDisableInteractionsInFullscreen.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandDisableInteractionsInFullscreen(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "island_disable_interactions_in_fullscreen"),
                )
            }
        }

        IslandSettingsSection(R.string.island_section_statusbar) {
            IconToggleItem(
                iconRes = R.drawable.rounded_visibility_off_24,
                title = stringResource(R.string.island_hide_status_bar_title),
                onInfoClick = { showHideStatusBarInfo = true },
                isChecked = state.isIslandHideStatusBarInsets.value && hasShellPermission,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !hasShellPermission) {
                        requestingPermissionsFor =
                            Pair(
                                R.string.island_hide_status_bar_title,
                                listOf(if (ShellUtils.isRootEnabled(context)) "ROOT" else "SHIZUKU"),
                            )
                    } else {
                        state.setIslandHideStatusBarInsets(checked, context)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "island_hide_status_bar_insets"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_off_24,
                title = stringResource(R.string.island_suppress_system_heads_up_title),
                isChecked = state.isIslandSuppressSystemHeadsUp.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !PermissionUtils.canWriteSecureSettings(context) && !ShellUtils.isAvailable(context)) {
                        requestingPermissionsFor = Pair(R.string.island_suppress_system_heads_up_title, listOf("WRITE_SECURE_SETTINGS"))
                    } else {
                        state.setIslandSuppressSystemHeadsUp(checked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "island_suppress_system_heads_up"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_off_24,
                title = stringResource(R.string.island_hide_live_updates_title),
                enabled = !state.isIslandHideStatusBarInsets.value,
                isChecked = state.isIslandHideLiveUpdates.value && !state.isIslandHideStatusBarInsets.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !hasShellPermission) {
                        requestingPermissionsFor =
                            Pair(
                                R.string.island_hide_live_updates_title,
                                listOf(if (ShellUtils.isRootEnabled(context)) "ROOT" else "SHIZUKU"),
                            )
                    } else {
                        state.setIslandHideLiveUpdates(checked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "island_hide_live_updates"),
            )

            IslandHideIconsToggle(
                state = state,
                hasShellPermission = hasShellPermission,
                onNeedPermission = {
                    requestingPermissionsFor =
                        Pair(
                            R.string.island_hide_icons_title,
                            listOf(if (ShellUtils.isRootEnabled(context)) "ROOT" else "SHIZUKU"),
                        )
                },
                highlighted = highlightSetting == "island_hide_icons",
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_visibility_off_24,
                title = stringResource(R.string.island_dynamic_hide_status_bar_title),
                isChecked = state.isIslandDynamicHideStatusBar.value && hasShellPermission,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !hasShellPermission) {
                        requestingPermissionsFor =
                            Pair(
                                R.string.island_dynamic_hide_status_bar_title,
                                listOf(if (ShellUtils.isRootEnabled(context)) "ROOT" else "SHIZUKU"),
                            )
                    } else {
                        state.setIslandDynamicHideStatusBar(checked, context)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "island_dynamic_hide_status_bar"),
            )
        }
    }
}

@Composable
fun IslandPreviewControls(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val view = LocalView.current
    val previewSettings = remember { IslandSettings(context) }
    var previewRing by remember { mutableStateOf(false) }
    var previewStage by remember { mutableStateOf(IslandSettings.ISLAND_PREVIEW_STAGE_AUTO) }
    DisposableEffect(Unit) {
        onDispose {
            previewSettings.setIslandPreviewRingEnabled(false)
            previewSettings.setIslandPreviewStage(IslandSettings.ISLAND_PREVIEW_STAGE_AUTO)
        }
    }

    RoundedCardContainer(
        modifier = modifier,
        spacing = 2.dp,
        cornerRadius = 24.dp,
    ) {
        IconToggleItem(
            iconRes = R.drawable.rounded_circle_24,
            title = stringResource(R.string.island_preview_ring_title),
            isChecked = previewRing,
            onCheckedChange = { checked ->
                HapticUtil.performVirtualKeyHaptic(view)
                previewRing = checked
                previewSettings.setIslandPreviewRingEnabled(checked)
            },
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                .padding(top = 12.dp),
        ) {
            Text(
                text = stringResource(R.string.island_preview_stage_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            val stages = listOf(
                IslandSettings.ISLAND_PREVIEW_STAGE_AUTO,
                IslandSettings.ISLAND_PREVIEW_STAGE_PEEK,
                IslandSettings.ISLAND_PREVIEW_STAGE_EXPANDED,
            )
            val stageLabels = mapOf(
                IslandSettings.ISLAND_PREVIEW_STAGE_AUTO to stringResource(R.string.island_preview_stage_auto),
                IslandSettings.ISLAND_PREVIEW_STAGE_PEEK to stringResource(R.string.island_preview_stage_peek),
                IslandSettings.ISLAND_PREVIEW_STAGE_EXPANDED to stringResource(R.string.island_preview_stage_expanded),
            )
            SegmentedPicker(
                items = stages,
                selectedItem = previewStage,
                onItemSelected = {
                    previewStage = it
                    previewSettings.setIslandPreviewStage(it)
                },
                labelProvider = { stageLabels[it].orEmpty() },
                title = R.string.island_preview_stage_title,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandNotificationSettingsUI(
    state: IslandState,
    isSensitiveNotificationsGranted: Boolean,
    permissionItemsFor: (List<String>) -> List<PermissionItem>,
    onRefreshPermissions: () -> Unit,
    appPicker: @Composable (AppPickerRequest) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showFilterAppsSheet by remember { mutableStateOf(false) }
    var showHubSheet by remember { mutableStateOf(false) }
    var showOtpPermissions by remember { mutableStateOf(false) }
    var lockedDisabled by remember {
        mutableStateOf(
            IslandSettings(context).getBoolean(IslandSettings.KEY_ISLAND_NOTIF_DISABLE_LOCKED, true),
        )
    }
    var concealed by remember {
        mutableStateOf(
            IslandSettings(context).getBoolean(IslandSettings.KEY_ISLAND_NOTIF_CONCEAL_LOCKED, false),
        )
    }
    // A notification can only peek as a line while Behavior's island-wide Line peek is on.
    val linePeekOn = state.isIslandLineStageEnabled.value

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_unread_24,
                title = stringResource(R.string.island_notif_show_title),
                isChecked = state.isIslandShowNotifications.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandShowNotifications(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_show_notifications"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_touch_app_24,
                title = stringResource(R.string.island_notif_tap_to_open_title),
                description = stringResource(R.string.island_notif_tap_to_open_desc),
                isChecked = state.isIslandNotifTapToOpen.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandNotifTapToOpen(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_notif_tap_to_open"),
            )
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.outline_circle_notifications_24,
                title = stringResource(R.string.island_notif_hub_title),
                description = stringResource(R.string.island_notif_hub_desc),
                isChecked = state.isIslandNotifHub.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandNotifHub(checked)
                },
                onSettingsClick = { showHubSheet = true },
                modifier = Modifier.highlight(highlightSetting == "island_notif_hub"),
            )
        }

        IslandSettingsSection(R.string.island_notif_section_sources) {
            IconToggleItem(
                iconRes = R.drawable.rounded_filter_alt_24,
                title = stringResource(R.string.island_notif_filter_apps_title),
                description = stringResource(R.string.island_notif_filter_apps_desc),
                isChecked = state.isIslandNotifFilterApps.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandNotifFilterApps(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_notif_filter_apps"),
            )

            AnimatedVisibility(
                visible = state.isIslandNotifFilterApps.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_apps_24,
                    title = stringResource(R.string.action_select_apps),
                    showToggle = false,
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        showFilterAppsSheet = true
                    },
                )
            }

            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_off_24,
                title = stringResource(R.string.island_notif_skip_silent_title),
                description = stringResource(R.string.island_notif_skip_silent_desc),
                isChecked = state.isIslandNotifSkipSilent.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandNotifSkipSilent(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_notif_skip_silent"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_do_not_disturb_on_24,
                title = stringResource(R.string.island_notif_follow_dnd_title),
                description = stringResource(R.string.island_notif_follow_dnd_desc),
                isChecked = state.isIslandNotifFollowDnd.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandNotifFollowDnd(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_notif_follow_dnd"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_downloading_24,
                title = stringResource(R.string.island_notif_keep_progress_title),
                description = stringResource(R.string.island_notif_keep_progress_desc),
                isChecked = state.isIslandNotifKeepProgress.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandNotifKeepProgress(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_notif_keep_progress"),
            )
        }

        AnimatedVisibility(
            visible = linePeekOn,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            IslandSettingsSection(R.string.island_notif_section_heads_up) {
                val peekStyle = state.islandNotifPeekStyle.value
                val fullLabel = stringResource(R.string.island_notif_peek_style_full)
                val compactLabel = stringResource(R.string.island_notif_peek_style_compact)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                        .padding(top = 12.dp)
                        .highlight(highlightSetting == "island_notif_peek_style"),
                ) {
                    Text(
                        text = stringResource(R.string.island_notif_condensed_info_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Text(
                        text = stringResource(
                            when (peekStyle) {
                                IslandSettings.ISLAND_NOTIF_PEEK_STYLE_COMPACT -> R.string.island_notif_condensed_info_desc
                                else -> R.string.island_notif_peek_style_full_desc
                            },
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    SegmentedPicker(
                        items = listOf(
                            IslandSettings.ISLAND_NOTIF_PEEK_STYLE_FULL,
                            IslandSettings.ISLAND_NOTIF_PEEK_STYLE_COMPACT,
                        ),
                        selectedItem = peekStyle,
                        onItemSelected = { state.setIslandNotifPeekStyle(it) },
                        labelProvider = {
                            when (it) {
                                IslandSettings.ISLAND_NOTIF_PEEK_STYLE_COMPACT -> compactLabel
                                else -> fullLabel
                            }
                        },
                        title = R.string.island_notif_condensed_info_title,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        IslandSettingsSection(R.string.island_notif_section_in_island) {
            IconToggleItem(
                iconRes = R.drawable.outline_circle_notifications_24,
                title = stringResource(R.string.island_notif_queue_title),
                description = stringResource(R.string.island_notif_queue_desc),
                isChecked = state.isIslandNotifQueue.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandNotifQueue(checked)
                },
                enabled = !state.isIslandNotifHub.value,
                modifier = Modifier.highlight(highlightSetting == "island_notif_queue"),
            )

            AnimatedVisibility(
                visible = state.isIslandNotifQueue.value && !state.isIslandNotifHub.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_fullscreen_portrait_24,
                    title = stringResource(R.string.island_notif_queue_inline_title),
                    description = stringResource(R.string.island_notif_queue_inline_desc),
                    isChecked = state.isIslandNotifQueueInline.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandNotifQueueInline(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "island_notif_queue_inline"),
                )
            }

            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_unread_24,
                title = stringResource(R.string.island_catch_up_title),
                description = stringResource(R.string.island_catch_up_desc),
                isChecked = state.isIslandCatchUpEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setIslandCatchUpEnabled(checked)
                },
                enabled = !state.isIslandNotifHub.value,
                modifier = Modifier.highlight(highlightSetting == "island_catch_up"),
            )

            AnimatedVisibility(
                visible = state.isIslandCatchUpEnabled.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                val infinityText = stringResource(R.string.island_timeout_infinity)
                ConfigSliderItem(
                    title = stringResource(R.string.island_catch_up_timeout_title),
                    value = catchUpStepIndex(state.islandCatchUpTimeoutMs.longValue).toFloat(),
                    onValueChange = {
                        HapticUtil.performUIHaptic(view)
                        state.setIslandCatchUpTimeoutMs(
                            CATCH_UP_STEPS_MS[it.toInt().coerceIn(CATCH_UP_STEPS_MS.indices)]
                        )
                    },
                    valueRange = 0f..CATCH_UP_STEPS_MS.lastIndex.toFloat(),
                    increment = 1f,
                    iconRes = R.drawable.rounded_timer_24,
                    valueFormatter = {
                        formatCatchUp(
                            CATCH_UP_STEPS_MS[it.toInt().coerceIn(CATCH_UP_STEPS_MS.indices)],
                            infinityText,
                        )
                    },
                    modifier = Modifier.highlight(highlightSetting == "island_catch_up_timeout"),
                )
            }
        }

        if (showOtpPermissions) {
            PermissionsBottomSheet(
                onDismissRequest = {
                    showOtpPermissions = false
                    onRefreshPermissions()
                },
                featureTitle = stringResource(R.string.island_otp_section),
                permissions = permissionItemsFor(listOf("SENSITIVE_NOTIFICATIONS")),
            )
        }

        IslandSettingsSection(R.string.island_otp_section) {
            val otpEnabled = state.isIslandOtpEnabled.value
            IconToggleItem(
                iconRes = R.drawable.rounded_vpn_key_24,
                title = stringResource(R.string.island_otp_enabled_title),
                isChecked = otpEnabled,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !isSensitiveNotificationsGranted) {
                        showOtpPermissions = true
                    } else {
                        state.setIslandOtpEnabled(checked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "island_otp_enabled"),
            )
            AnimatedVisibility(
                visible = otpEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                IslandPrefToggle(
                    settingKey = IslandSettings.KEY_ISLAND_OTP_TAP_TO_COPY,
                    iconRes = R.drawable.rounded_content_copy_24,
                    title = stringResource(R.string.island_otp_tap_to_copy_title),
                    defaultValue = true,
                    modifier = Modifier.highlight(highlightSetting == "island_otp_tap_to_copy"),
                )
            }
            AnimatedVisibility(
                visible = otpEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                IslandPrefToggle(
                    settingKey = IslandSettings.KEY_ISLAND_OTP_DISMISS_AFTER_COPY,
                    iconRes = R.drawable.rounded_check_circle_24,
                    title = stringResource(R.string.island_otp_dismiss_after_copy_title),
                )
            }
            AnimatedVisibility(
                visible = otpEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                ConfigSliderItem(
                    title = stringResource(R.string.island_otp_timeout_title),
                    value = OTP_TIMEOUT_STEPS_MS.indexOfClosest(state.islandOtpTimeoutMs.longValue).toFloat(),
                    onValueChange = {
                        HapticUtil.performUIHaptic(view)
                        state.setIslandOtpTimeoutMs(OTP_TIMEOUT_STEPS_MS[it.toInt().coerceIn(OTP_TIMEOUT_STEPS_MS.indices)])
                    },
                    valueRange = 0f..OTP_TIMEOUT_STEPS_MS.lastIndex.toFloat(),
                    increment = 1f,
                    iconRes = R.drawable.rounded_timer_24,
                    valueFormatter = { formatCatchUp(OTP_TIMEOUT_STEPS_MS[it.toInt().coerceIn(OTP_TIMEOUT_STEPS_MS.indices)], "") },
                )
            }
            AnimatedVisibility(
                visible = otpEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                IslandPrefToggle(
                    settingKey = IslandSettings.KEY_ISLAND_OTP_BYPASS_FILTER,
                    iconRes = R.drawable.rounded_filter_alt_24,
                    title = stringResource(R.string.island_otp_bypass_filter_title),
                    defaultValue = true,
                    modifier = Modifier.highlight(highlightSetting == "island_otp_bypass_filter"),
                )
            }
        }

        if (rememberIslandShowsWhileLocked()) {
            IslandSettingsSection(R.string.island_notif_section_lock_screen) {
                IslandPrefToggle(
                    settingKey = IslandSettings.KEY_ISLAND_NOTIF_DISABLE_LOCKED,
                    iconRes = R.drawable.rounded_notifications_off_24,
                    title = stringResource(R.string.label_disabled),
                    defaultValue = true,
                    onChanged = { lockedDisabled = it },
                )
                AnimatedVisibility(
                    visible = !lockedDisabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        IslandPrefToggle(
                            settingKey = IslandSettings.KEY_ISLAND_NOTIF_CONCEAL_LOCKED,
                            iconRes = R.drawable.rounded_visibility_off_24,
                            title = stringResource(R.string.island_notif_conceal_locked_title),
                            onChanged = { concealed = it },
                        )
                        AnimatedVisibility(
                            visible = concealed,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically(),
                        ) {
                            IslandPrefToggle(
                                settingKey = IslandSettings.KEY_ISLAND_NOTIF_CONCEAL_CHAT_PICTURES,
                                iconRes = R.drawable.rounded_person_24,
                                title = stringResource(R.string.island_notif_conceal_chat_pictures_title),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showHubSheet) {
        IslandNotificationHubOptionsBottomSheet(
            state = state,
            onDismissRequest = { showHubSheet = false },
        )
    }

    if (showFilterAppsSheet) {
        appPicker(
            AppPickerRequest(
                title = stringResource(R.string.island_notif_filter_apps_title),
                onDismiss = { showFilterAppsSheet = false },
                onLoadApps = { state.loadIslandNotifFilterApps() },
                onSaveApps = { _, apps -> state.saveIslandNotifFilterApps(apps) },
                onAppToggle = { _, pkg, enabled -> state.updateIslandNotifFilterApp(pkg, enabled) },
                headerContent = null,
            ),
        )
    }
}

@Composable
private fun IslandSettingsSection(titleRes: Int, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(titleRes),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
            content = content,
        )
    }
}

private val CATCH_UP_STEPS_MS =
    longArrayOf(5_000, 10_000, 15_000, 20_000, 30_000, 45_000, 60_000, 120_000, 180_000, 300_000, 600_000, 900_000, 0)

private val OTP_TIMEOUT_STEPS_MS = longArrayOf(15_000, 30_000, 60_000, 120_000, 180_000, 300_000)

private fun LongArray.indexOfClosest(ms: Long): Int = indices.minBy { kotlin.math.abs(this[it] - ms) }

private fun catchUpStepIndex(ms: Long): Int =
    if (ms <= 0L) CATCH_UP_STEPS_MS.lastIndex
    else CATCH_UP_STEPS_MS.indices.filter { CATCH_UP_STEPS_MS[it] > 0 }.minBy { kotlin.math.abs(CATCH_UP_STEPS_MS[it] - ms) }

private fun formatCatchUp(ms: Long, infinityText: String): String =
    when {
        ms <= 0L -> infinityText
        ms < 60_000 -> "${ms / 1000}s"
        else -> "${ms / 60_000}m"
    }
