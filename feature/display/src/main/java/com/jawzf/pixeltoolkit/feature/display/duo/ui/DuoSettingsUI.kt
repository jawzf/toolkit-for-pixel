package com.jawzf.pixeltoolkit.feature.display.duo.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.components.RemapActionItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.pickers.ColorSwatchPicker
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.display.duo.DuoState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DuoSettingsUI(
    state: DuoState,
    isFoldableCameraDevice: Boolean,
    isUnsupportedFeaturesEnabled: Boolean,
    isAccessibilityEnabled: Boolean,
    isNotificationListenerEnabled: Boolean,
    isIslandEnabled: Boolean,
    isIslandBatteryIdleColorEnabled: Boolean,
    islandBatteryIdleColor: String,
    onIslandBatteryIdleColorEnabledChange: (Boolean) -> Unit,
    onIslandBatteryIdleColorChange: (String) -> Unit,
    permissionItemsFor: (List<String>) -> List<PermissionItem>,
    onRefreshPermissions: () -> Unit,
    actionLabel: (Action) -> String,
    gesturePicker: @Composable (currentAction: Action?, onActionSelected: (Action?) -> Unit, onClosed: () -> Unit) -> Unit,
    mediaAppSelectionSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val displayConfiguration = LocalConfiguration.current
    LaunchedEffect(displayConfiguration.screenWidthDp, displayConfiguration.screenHeightDp,
        displayConfiguration.orientation) {
        state.refreshDuoCameraPlacement()
    }

    var requestingPermissionsFor by remember { mutableStateOf<Pair<Int, List<String>>?>(null) }
    var showBatteryOptionsSheet by remember { mutableStateOf(false) }
    var showBatteryPercentageOptionsSheet by remember { mutableStateOf(false) }
    var showNetworkOptionsSheet by remember { mutableStateOf(false) }
    var showTimeOptionsSheet by remember { mutableStateOf(false) }
    var showMediaAppSelectionSheet by remember { mutableStateOf(false) }

    var pickingActionForGesture by remember { mutableStateOf<String?>(null) }
    var showSlideModeSheet by remember { mutableStateOf(false) }

    var showPermissionSheet by remember { mutableStateOf(false) }
    var permissionKeysToShow by remember { mutableStateOf<List<String>>(emptyList()) }
    var permissionFeatureTitle by remember { mutableStateOf<Any>("") }

    LaunchedEffect(Unit) {
        onRefreshPermissions()
    }

    fun applyActionForGesture(gesture: String?, action: Action?) {
        when (gesture) {
            "tap" -> state.setDuoTapAction(action)
            "double_tap" -> state.setDuoDoubleTapAction(action)
            "long_press" -> state.setDuoLongPressAction(action)
            "swipe_down" -> state.setDuoSwipeDownAction(action)
        }
    }

    if (requestingPermissionsFor != null) {
        val (featureTitle, permKeys) = requestingPermissionsFor!!
        val permissionItems = permissionItemsFor(permKeys)
        PermissionsBottomSheet(
            onDismissRequest = {
                requestingPermissionsFor = null
                onRefreshPermissions()
            },
            featureTitle = featureTitle,
            permissions = permissionItems,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_motion_play_24,
                title = stringResource(R.string.duo_enable_title),
                description = stringResource(R.string.duo_enable_desc),
                isChecked = state.isDuoEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !isAccessibilityEnabled) {
                        requestingPermissionsFor = Pair(R.string.duo_title, listOf("ACCESSIBILITY"))
                    } else {
                        state.setDuoEnabled(checked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "duo_enabled"),
            )
        }

        Text(
            text = stringResource(R.string.duo_section_position),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )

        RoundedCardContainer {
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
                    isChecked = state.isDuoOrientationProfiles.value,
                    onCheckedChange = { state.setDuoOrientationProfiles(it) },
                )
                IconToggleItem(
                    iconRes = R.drawable.rounded_visibility_off_24,
                    title = stringResource(R.string.foldable_hide_current_orientation_title),
                    isChecked = state.isDuoHiddenInCurrentOrientation.value,
                    onCheckedChange = { state.setDuoHiddenInCurrentOrientation(it) },
                )
            }
            IconToggleItem(
                iconRes = R.drawable.rounded_center_focus_strong_24,
                title = stringResource(R.string.duo_auto_detect_title),
                description = stringResource(R.string.duo_auto_detect_desc),
                isChecked = state.isDuoAutoDetect.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoAutoDetect(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_use_auto_detect"),
            )

            AnimatedVisibility(
                visible = !state.isDuoAutoDetect.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ConfigSliderItem(
                        title = stringResource(R.string.duo_camera_offset_x_title),
                        value = state.duoCameraOffsetX.floatValue,
                        onValueChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setDuoCameraOffsetX(it)
                        },
                        valueRange = 0f..100f,
                        increment = 1f,
                        iconRes = R.drawable.rounded_border_left_24,
                        valueFormatter = { "${it.toInt()}%" },
                    )
                    ConfigSliderItem(
                        title = stringResource(R.string.duo_camera_offset_y_title),
                        value = state.duoCameraOffsetY.floatValue,
                        onValueChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setDuoCameraOffsetY(it)
                        },
                        valueRange = 0f..20f,
                        increment = 0.5f,
                        iconRes = R.drawable.rounded_border_top_24,
                        valueFormatter = { "%.1f%%".format(it) },
                    )
                }
            }

            AnimatedVisibility(
                visible = state.hasMultipleDuoDisplays.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Text(
                    text = stringResource(R.string.duo_per_display_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }

            ConfigSliderItem(
                title = stringResource(R.string.duo_camera_size_title),
                value = state.duoCameraSize.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setDuoCameraSize(it)
                },
                valueRange = 0.05f..2.0f,
                increment = 0.05f,
                iconRes = R.drawable.rounded_arrows_outward_24,
                valueFormatter = { "%.2fx".format(it) },
            )
        }

        Text(
            text = stringResource(R.string.duo_section_style),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )

        RoundedCardContainer {
            ConfigSliderItem(
                title = stringResource(R.string.duo_ring_radius_title),
                value = state.duoRingRadius.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setDuoRingRadius(it)
                },
                valueRange = 0.1f..1.6f,
                increment = 0.05f,
                iconRes = R.drawable.rounded_circle_24,
                valueFormatter = { "%.2fx".format(it) },
            )
            ConfigSliderItem(
                title = stringResource(R.string.duo_arc_thickness_title),
                value = state.duoArcThickness.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setDuoArcThickness(it)
                },
                valueRange = 0.5f..10f,
                increment = 0.5f,
                iconRes = R.drawable.rounded_line_weight_24,
                valueFormatter = { if (it % 1f == 0f) "${it.toInt()} dp" else "%.1f dp".format(it) },
            )
            ConfigSliderItem(
                title = stringResource(R.string.duo_dot_size_title),
                value = state.duoDotSize.floatValue,
                onValueChange = {
                    HapticUtil.performUIHaptic(view)
                    state.setDuoDotSize(it)
                },
                valueRange = 0.5f..8f,
                increment = 0.5f,
                iconRes = R.drawable.rounded_circles_24,
                valueFormatter = { if (it % 1f == 0f) "${it.toInt()} dp" else "%.1f dp".format(it) },
            )
        }

        Text(
            text = stringResource(R.string.duo_section_what_to_show),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_charging_60_24,
                title = stringResource(R.string.duo_show_battery_title),
                isChecked = state.isDuoShowBattery.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoShowBattery(checked)
                },
                onSettingsClick = {
                    showBatteryOptionsSheet = true
                },
                modifier = Modifier.highlight(highlightSetting == "duo_show_battery"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_android_frame_6_24,
                title = stringResource(R.string.duo_show_battery_percentage_title),
                isChecked = state.isDuoShowBatteryPercentage.value,
                enabled = state.isDuoShowBattery.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoShowBatteryPercentage(checked)
                },
                onSettingsClick = {
                    showBatteryPercentageOptionsSheet = true
                },
                modifier = Modifier.highlight(highlightSetting == "duo_show_battery_percentage"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_signal_cellular_alt_24,
                title = stringResource(R.string.duo_show_networks_title),
                isChecked = state.isDuoShowNetworks.value,
                enabled = state.isDuoShowBattery.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoShowNetworks(checked)
                },
                onSettingsClick = {
                    showNetworkOptionsSheet = true
                },
                modifier = Modifier.highlight(highlightSetting == "duo_show_networks"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_schedule_24,
                title = stringResource(R.string.duo_show_time_title),
                isChecked = state.isDuoShowTime.value,
                enabled = state.isDuoShowBattery.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoShowTime(checked)
                },
                onSettingsClick = {
                    showTimeOptionsSheet = true
                },
                modifier = Modifier.highlight(highlightSetting == "duo_show_time"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_motion_play_24,
                title = stringResource(R.string.duo_show_media_title),
                isChecked = state.isDuoShowMedia.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !isNotificationListenerEnabled) {
                        requestingPermissionsFor = Pair(R.string.duo_title, listOf("NOTIFICATION_LISTENER"))
                    } else {
                        state.setDuoShowMedia(checked)
                    }
                },
                onSettingsClick = {
                    showMediaAppSelectionSheet = true
                },
                modifier = Modifier.highlight(highlightSetting == "duo_show_media"),
            )
            AnimatedVisibility(
                visible = state.isDuoShowMedia.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_rotate_right_24,
                    title = stringResource(R.string.duo_rotate_album_art_title),
                    isChecked = state.isDuoRotateAlbumArt.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setDuoRotateAlbumArt(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "duo_rotate_album_art"),
                )
            }
            IconToggleItem(
                iconRes = R.drawable.rounded_downloading_24,
                title = stringResource(R.string.duo_show_progress_title),
                isChecked = state.isDuoShowProgress.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !isNotificationListenerEnabled) {
                        requestingPermissionsFor = Pair(R.string.duo_title, listOf("NOTIFICATION_LISTENER"))
                    } else {
                        state.setDuoShowProgress(checked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "duo_show_progress"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_flashlight_on_24,
                title = stringResource(R.string.duo_show_flashlight_title),
                isChecked = state.isDuoShowFlashlight.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoShowFlashlight(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_show_flashlight"),
            )
        }

        Text(
            text = stringResource(R.string.duo_section_behavior),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_palette_24,
                title = stringResource(R.string.duo_material_you_title),
                isChecked = state.isDuoUseMaterialYou.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoUseMaterialYou(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_use_material_you"),
            )
            AnimatedVisibility(
                visible = !state.isDuoUseMaterialYou.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ColorSwatchPicker(
                    selectedColorHex = state.duoCustomColor.value,
                    onColorSelected = { hex ->
                        state.setDuoCustomColor(hex)
                    },
                )
            }
            IconToggleItem(
                iconRes = R.drawable.rounded_pinch_24,
                title = stringResource(R.string.island_hide_on_shade_title),
                isChecked = state.isDuoHideOnShade.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoHideOnShade(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_hide_on_shade"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_fullscreen_portrait_24,
                title = stringResource(R.string.keep_in_full_screen_title),
                isChecked = state.isDuoKeepInFullscreen.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoKeepInFullscreen(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_keep_in_fullscreen"),
            )
            AnimatedVisibility(
                visible = state.isDuoKeepInFullscreen.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_do_not_touch_24,
                    title = stringResource(R.string.disable_interactions_in_full_screen_title),
                    isChecked = state.isDuoDisableInteractionsInFullscreen.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setDuoDisableInteractionsInFullscreen(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "duo_disable_interactions_in_fullscreen"),
                )
            }
            val areUnsupportedFeaturesEnabled = isUnsupportedFeaturesEnabled
            IconToggleItem(
                iconRes = R.drawable.rounded_mobile_off_24,
                title = stringResource(R.string.duo_hide_when_screen_off_title),
                isChecked = if (areUnsupportedFeaturesEnabled) state.isDuoHideWhenScreenOff.value else true,
                enabled = areUnsupportedFeaturesEnabled,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoHideWhenScreenOff(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_hide_when_screen_off"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_nightlight_24,
                title = stringResource(R.string.duo_hide_when_screen_off_only_idle_title),
                isChecked = if (areUnsupportedFeaturesEnabled) state.isDuoHideWhenScreenOffOnlyIdle.value else false,
                enabled = areUnsupportedFeaturesEnabled && state.isDuoHideWhenScreenOff.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoHideWhenScreenOffOnlyIdle(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_hide_when_screen_off_only_idle"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_mobile_lock_portrait_24,
                title = stringResource(R.string.duo_hide_when_locked_title),
                isChecked = state.isDuoHideWhenLocked.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoHideWhenLocked(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "duo_hide_when_locked"),
            )
        }

        if (!isUnsupportedFeaturesEnabled) {
            Text(
                text = stringResource(R.string.duo_aod_burn_in_protection_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            )
        }

        Text(
            text = stringResource(R.string.duo_section_actions),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )

        RoundedCardContainer {
            val tapAction = state.duoTapAction.value
            val tapForBriefActive = state.isDuoIslandCombined.value && state.isDuoTapForBrief.value
            IconToggleItem(
                iconRes = R.drawable.rounded_pan_tool_alt_24,
                title = stringResource(R.string.duo_action_tap_title),
                description = tapAction?.let(actionLabel) ?: stringResource(R.string.duo_action_none),
                showToggle = false,
                enabled = !tapForBriefActive,
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    pickingActionForGesture = "tap"
                },
                modifier = Modifier.highlight(highlightSetting == "duo_tap_action"),
            )

            AnimatedVisibility(
                visible = state.isDuoIslandCombined.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_calendar_today_24,
                    title = stringResource(R.string.duo_tap_for_brief_title),
                    isChecked = state.isDuoTapForBrief.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setDuoTapForBrief(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "duo_tap_for_brief"),
                )
            }

            val doubleTapAction = state.duoDoubleTapAction.value
            IconToggleItem(
                iconRes = R.drawable.rounded_touch_app_24,
                title = stringResource(R.string.duo_action_double_tap_title),
                description = doubleTapAction?.let(actionLabel) ?: stringResource(R.string.duo_action_none),
                showToggle = false,
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    pickingActionForGesture = "double_tap"
                },
                modifier = Modifier.highlight(highlightSetting == "duo_double_tap_action"),
            )

            val longPressAction = state.duoLongPressAction.value
            IconToggleItem(
                iconRes = R.drawable.rounded_front_hand_24,
                title = stringResource(R.string.duo_action_long_press_title),
                description = longPressAction?.let(actionLabel) ?: stringResource(R.string.duo_action_none),
                showToggle = false,
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    pickingActionForGesture = "long_press"
                },
                modifier = Modifier.highlight(highlightSetting == "duo_long_press_action"),
            )

            val swipeDownAction = state.duoSwipeDownAction.value
            IconToggleItem(
                iconRes = R.drawable.rounded_south_24,
                title = stringResource(R.string.duo_action_swipe_down_title),
                description = swipeDownAction?.let(actionLabel) ?: stringResource(R.string.duo_action_none),
                showToggle = false,
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    pickingActionForGesture = "swipe_down"
                },
                modifier = Modifier.highlight(highlightSetting == "duo_swipe_down_action"),
            )

            val baseSlideDescription = when (state.duoSlideMode.value) {
                "volume" -> stringResource(R.string.duo_action_horizontal_slide_volume)
                "brightness" -> stringResource(R.string.duo_action_horizontal_slide_brightness)
                "sound_mode" -> stringResource(R.string.duo_action_horizontal_slide_sound_mode)
                else -> stringResource(R.string.duo_action_horizontal_slide_none)
            }
            val slideModeDescription = if (state.isDuoSlideTrack.value) {
                if (state.duoSlideMode.value != "none") {
                    "$baseSlideDescription • " + stringResource(R.string.duo_action_horizontal_slide_track)
                } else {
                    stringResource(R.string.duo_action_horizontal_slide_track)
                }
            } else {
                baseSlideDescription
            }
            IconToggleItem(
                iconRes = R.drawable.rounded_compare_arrows_24,
                title = stringResource(R.string.duo_action_horizontal_slide_title),
                description = slideModeDescription,
                showToggle = false,
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    showSlideModeSheet = true
                },
                modifier = Modifier.highlight(highlightSetting == "duo_slide_mode"),
            )

            val isMirrorableMode = state.isDuoSlideTrack.value || state.duoSlideMode.value == "sound_mode"
            AnimatedVisibility(
                visible = isMirrorableMode,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_compare_arrows_24,
                    title = stringResource(R.string.duo_slide_mirror_direction_title),
                    isChecked = state.isDuoSlideInvertDirection.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setDuoSlideInvertDirection(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "duo_slide_invert_direction"),
                )
            }
        }

        AnimatedVisibility(
            visible = state.isDuoEnabled.value && isIslandEnabled,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.rounded_link_24,
                    title = stringResource(R.string.duo_combine_with_island),
                    isChecked = state.isDuoIslandCombined.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setDuoIslandCombined(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "duo_island_combined"),
                )
            }
        }
    }

    if (showSlideModeSheet) {
        ToolkitBottomSheet(
            onDismissRequest = { showSlideModeSheet = false },
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.duo_action_horizontal_slide_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                RoundedCardContainer {
                    val modes = listOf(
                        Triple("none", R.string.duo_action_horizontal_slide_none, R.drawable.rounded_do_not_disturb_on_24),
                        Triple("volume", R.string.duo_action_horizontal_slide_volume, R.drawable.rounded_volume_up_24),
                        Triple("brightness", R.string.duo_action_horizontal_slide_brightness, R.drawable.rounded_brightness_6_24),
                        Triple("sound_mode", R.string.duo_action_horizontal_slide_sound_mode, R.drawable.rounded_mobile_sound_24),
                    )

                    modes.forEach { (modeKey, titleRes, iconRes) ->
                        RemapActionItem(
                            title = stringResource(titleRes),
                            iconRes = iconRes,
                            isSelected = state.duoSlideMode.value == modeKey,
                            onClick = {
                                state.setDuoSlideMode(modeKey)
                                showSlideModeSheet = false
                            },
                        )
                    }
                }

                RoundedCardContainer {
                    IconToggleItem(
                        iconRes = R.drawable.rounded_skip_next_24,
                        title = stringResource(R.string.duo_action_horizontal_slide_track),
                        description = stringResource(R.string.duo_action_horizontal_slide_track_desc),
                        isChecked = state.isDuoSlideTrack.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            state.setDuoSlideTrackEnabled(checked)
                        },
                    )
                }
            }
        }
    }

    pickingActionForGesture?.let { gesture ->
        val currentAction =
            when (gesture) {
                "tap" -> state.duoTapAction.value
                "double_tap" -> state.duoDoubleTapAction.value
                "long_press" -> state.duoLongPressAction.value
                "swipe_down" -> state.duoSwipeDownAction.value
                else -> null
            }
        gesturePicker(
            currentAction,
            { action -> applyActionForGesture(gesture, action) },
            { pickingActionForGesture = null },
        )
    }

    if (showPermissionSheet) {
        val permissionItems = permissionItemsFor(permissionKeysToShow)
        if (permissionItems.isNotEmpty()) {
            PermissionsBottomSheet(
                onDismissRequest = {
                    showPermissionSheet = false
                    permissionKeysToShow = emptyList()
                },
                featureTitle = permissionFeatureTitle,
                permissions = permissionItems,
            )
        }
    }

    if (showBatteryOptionsSheet) {
        DuoBatteryOptionsBottomSheet(
            state = state,
            isIslandBatteryIdleColorEnabled = isIslandBatteryIdleColorEnabled,
            islandBatteryIdleColor = islandBatteryIdleColor,
            onIslandBatteryIdleColorEnabledChange = onIslandBatteryIdleColorEnabledChange,
            onIslandBatteryIdleColorChange = onIslandBatteryIdleColorChange,
            onDismissRequest = { showBatteryOptionsSheet = false },
        )
    }

    if (showTimeOptionsSheet) {
        DuoTimeOptionsBottomSheet(
            state = state,
            onDismissRequest = { showTimeOptionsSheet = false },
        )
    }

    if (showBatteryPercentageOptionsSheet) {
        DuoBatteryPercentageOptionsBottomSheet(
            state = state,
            onDismissRequest = { showBatteryPercentageOptionsSheet = false },
        )
    }

    if (showNetworkOptionsSheet) {
        DuoNetworkOptionsBottomSheet(
            state = state,
            onDismissRequest = { showNetworkOptionsSheet = false },
        )
    }

    if (showMediaAppSelectionSheet) {
        mediaAppSelectionSheet { showMediaAppSelectionSheet = false }
    }
}
