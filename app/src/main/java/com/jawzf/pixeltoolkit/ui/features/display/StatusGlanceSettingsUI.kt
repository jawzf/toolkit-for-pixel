package com.jawzf.pixeltoolkit.ui.features.display

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.feature.display.glance.StatusGlanceState
import com.jawzf.pixeltoolkit.feature.display.glance.ui.StatusGlanceBatteryOptionsBottomSheet

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatusGlanceSettingsUI(
    state: StatusGlanceState,
    isFoldableCameraDevice: Boolean,
    isAccessibilityEnabled: Boolean,
    isCalendarPermissionGranted: Boolean,
    isNotificationListenerEnabled: Boolean,
    permissionItemsFor: (List<String>) -> List<PermissionItem>,
    onRefreshPermissions: () -> Unit,
    actionLabel: (Action) -> String,
    gesturePicker: @Composable (currentAction: Action?, onActionSelected: (Action?) -> Unit, onClosed: () -> Unit) -> Unit,
    mediaAppSelectionSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    calendarOptionsSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val displayConfiguration = LocalConfiguration.current
    LaunchedEffect(displayConfiguration.screenWidthDp, displayConfiguration.screenHeightDp,
        displayConfiguration.orientation) {
        state.refreshStatusGlanceCameraPlacement()
    }

    var requestingPermissionsFor by remember { mutableStateOf<Pair<Int, List<String>>?>(null) }
    var showMediaAppSelectionSheet by remember { mutableStateOf(false) }
    var showBatteryOptionsSheet by remember { mutableStateOf(false) }
    var showCalendarOptionsSheet by remember { mutableStateOf(false) }

    var pickingActionForGesture by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        onRefreshPermissions()
    }

    fun applyActionForGesture(gesture: String?, action: Action?) {
        when (gesture) {
            "tap" -> state.setStatusGlanceTapAction(action)
            "double_tap" -> state.setStatusGlanceDoubleTapAction(action)
            "long_press" -> state.setStatusGlanceLongPressAction(action)
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
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                title = stringResource(R.string.status_glance_enable_title),
                description = stringResource(R.string.status_glance_enable_desc),
                iconRes = R.drawable.rounded_ad_units_24,
                isChecked = state.isStatusGlanceEnabled.value,
                onCheckedChange = { isChecked ->
                    HapticUtil.performUIHaptic(view)
                    if (isChecked && !isAccessibilityEnabled) {
                        requestingPermissionsFor = Pair(
                            R.string.feat_status_glance_title,
                            listOf("ACCESSIBILITY")
                        )
                    } else {
                        state.setStatusGlanceEnabled(isChecked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "status_glance_enabled"),
            )
        }

        AnimatedVisibility(
            visible = state.isStatusGlanceEnabled.value,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = stringResource(R.string.status_glance_section_position),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )

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
                            title = stringResource(R.string.foldable_orientation_profiles_title),
                            iconRes = R.drawable.rounded_center_focus_strong_24,
                            isChecked = state.isStatusGlanceOrientationProfiles.value,
                            onCheckedChange = { state.setStatusGlanceOrientationProfiles(it) },
                        )
                        IconToggleItem(
                            title = stringResource(R.string.foldable_hide_current_orientation_title),
                            iconRes = R.drawable.rounded_visibility_off_24,
                            isChecked = state.isStatusGlanceHiddenInCurrentOrientation.value,
                            onCheckedChange = { state.setStatusGlanceHiddenInCurrentOrientation(it) },
                        )
                    }
                    IconToggleItem(
                        title = stringResource(R.string.status_glance_auto_detect_button_title),
                        description = stringResource(R.string.status_glance_auto_detect_button_desc),
                        iconRes = R.drawable.rounded_center_focus_strong_24,
                        showToggle = false,
                        onClick = {
                            HapticUtil.performUIHaptic(view)
                            state.autoDetectStatusGlancePosition(context)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_auto_detect"),
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.status_glance_offset_x_title),
                        value = state.statusGlanceOffsetX.floatValue,
                        onValueChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceOffsetX(it)
                        },
                        valueRange = 0f..100f,
                        increment = 1f,
                        iconRes = R.drawable.rounded_border_left_24,
                        valueFormatter = { "${it.toInt()}%" },
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.status_glance_offset_y_title),
                        value = state.statusGlanceOffsetY.floatValue,
                        onValueChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceOffsetY(it)
                        },
                        valueRange = 0f..20f,
                        increment = 0.5f,
                        iconRes = R.drawable.rounded_border_top_24,
                        valueFormatter = { "%.1f%%".format(it) },
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.status_glance_max_width_title),
                        value = state.statusGlanceMaxWidth.floatValue,
                        onValueChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceMaxWidth(it)
                        },
                        valueRange = 80f..350f,
                        increment = 10f,
                        iconRes = R.drawable.rounded_arrows_outward_24,
                        valueFormatter = { "${it.toInt()} dp" },
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.status_glance_font_size_title),
                        value = state.statusGlanceFontSize.floatValue,
                        onValueChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceFontSize(it)
                        },
                        valueRange = 9f..22f,
                        increment = 1f,
                        iconRes = R.drawable.rounded_mobile_text_24,
                        valueFormatter = { "${it.toInt()} sp" },
                    )
                }

                Text(
                    text = stringResource(R.string.status_glance_section_what_to_show),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )

                RoundedCardContainer(
                    spacing = 2.dp,
                    cornerRadius = 24.dp,
                ) {
                    IconToggleItem(
                        title = stringResource(R.string.status_glance_show_flashlight_title),
                        iconRes = R.drawable.rounded_flashlight_on_24,
                        isChecked = state.isStatusGlanceShowFlashlight.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceShowFlashlight(it)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_show_flashlight"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_show_calendar_title),
                        iconRes = R.drawable.rounded_calendar_today_24,
                        isChecked = state.isStatusGlanceShowCalendar.value,
                        onCheckedChange = { isChecked ->
                            HapticUtil.performUIHaptic(view)
                            if (isChecked && !isCalendarPermissionGranted) {
                                requestingPermissionsFor = Pair(
                                    R.string.status_glance_show_calendar_title,
                                    listOf("READ_CALENDAR")
                                )
                            }
                            state.setStatusGlanceShowCalendar(isChecked)
                        },
                        onSettingsClick = {
                            showCalendarOptionsSheet = true
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_show_calendar"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_show_media_title),
                        iconRes = R.drawable.rounded_motion_play_24,
                        isChecked = state.isStatusGlanceShowMedia.value,
                        onCheckedChange = { isChecked ->
                            HapticUtil.performUIHaptic(view)
                            if (isChecked && !isNotificationListenerEnabled) {
                                requestingPermissionsFor = Pair(
                                    R.string.feat_status_glance_title,
                                    listOf("NOTIFICATION_LISTENER")
                                )
                            }
                            state.setStatusGlanceShowMedia(isChecked)
                        },
                        onSettingsClick = {
                            showMediaAppSelectionSheet = true
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_show_media"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_show_time_title),
                        iconRes = R.drawable.rounded_schedule_24,
                        isChecked = state.isStatusGlanceShowTime.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceShowTime(it)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_show_time"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_show_battery_title),
                        iconRes = R.drawable.battery_android_frame_full_24px,
                        isChecked = state.isStatusGlanceShowBattery.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceShowBattery(it)
                        },
                        onSettingsClick = {
                            showBatteryOptionsSheet = true
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_show_battery"),
                    )
                }

                Text(
                    text = stringResource(R.string.status_glance_section_gestures),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )

                RoundedCardContainer(
                    spacing = 2.dp,
                    cornerRadius = 24.dp,
                ) {
                    val tapAction = state.statusGlanceTapAction.value
                    val tapDescription = if (tapAction != null) {
                        "${stringResource(R.string.status_glance_action_tap_media_desc)} • ${tapAction.let(actionLabel)}"
                    } else {
                        stringResource(R.string.status_glance_action_tap_media_desc)
                    }
                    IconToggleItem(
                        iconRes = R.drawable.rounded_touch_app_24,
                        title = stringResource(R.string.status_glance_action_tap_title),
                        description = tapDescription,
                        showToggle = false,
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            pickingActionForGesture = "tap"
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_tap_action"),
                    )

                    val doubleTapAction = state.statusGlanceDoubleTapAction.value
                    IconToggleItem(
                        iconRes = R.drawable.rounded_touch_app_24,
                        title = stringResource(R.string.status_glance_action_double_tap_title),
                        description = doubleTapAction?.let(actionLabel) ?: stringResource(R.string.status_glance_action_none),
                        showToggle = false,
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            pickingActionForGesture = "double_tap"
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_double_tap_action"),
                    )

                    val longPressAction = state.statusGlanceLongPressAction.value
                    IconToggleItem(
                        iconRes = R.drawable.rounded_front_hand_24,
                        title = stringResource(R.string.status_glance_action_long_press_title),
                        description = longPressAction?.let(actionLabel) ?: stringResource(R.string.status_glance_action_none),
                        showToggle = false,
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            pickingActionForGesture = "long_press"
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_long_press_action"),
                    )

                    IconToggleItem(
                        iconRes = R.drawable.rounded_skip_next_24,
                        title = stringResource(R.string.status_glance_action_swipe_right_title),
                        description = stringResource(R.string.status_glance_action_swipe_right_desc),
                        showToggle = false,
                    )
                }

                Text(
                    text = stringResource(R.string.status_glance_section_appearance),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp),
                )

                RoundedCardContainer(
                    spacing = 2.dp,
                    cornerRadius = 24.dp,
                ) {
                    IconToggleItem(
                        title = stringResource(R.string.status_glance_bold_text_title),
                        description = stringResource(R.string.status_glance_bold_text_desc),
                        iconRes = R.drawable.rounded_mobile_text_24,
                        isChecked = state.isStatusGlanceBoldText.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceBoldText(it)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_bold_text"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_background_pill_title),
                        description = stringResource(R.string.status_glance_background_pill_desc),
                        iconRes = R.drawable.rounded_circle_24,
                        isChecked = state.isStatusGlanceBackgroundPill.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceBackgroundPill(it)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_background_pill"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_album_art_colors_title),
                        description = stringResource(R.string.status_glance_album_art_colors_desc),
                        iconRes = R.drawable.rounded_palette_24,
                        isChecked = state.isStatusGlanceAlbumArtColors.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceAlbumArtColors(it)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_album_art_colors"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_hide_in_quick_settings_title),
                        iconRes = R.drawable.rounded_pinch_24,
                        isChecked = state.isStatusGlanceHideInQuickSettings.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceHideInQuickSettings(it)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_hide_in_quick_settings"),
                    )

                    IconToggleItem(
                        title = stringResource(R.string.status_glance_hide_when_locked_title),
                        description = stringResource(R.string.status_glance_hide_when_locked_desc),
                        iconRes = R.drawable.rounded_lock_24,
                        isChecked = state.isStatusGlanceHideWhenLocked.value,
                        onCheckedChange = {
                            HapticUtil.performUIHaptic(view)
                            state.setStatusGlanceHideWhenLocked(it)
                        },
                        modifier = Modifier.highlight(highlightSetting == "status_glance_hide_when_locked"),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showMediaAppSelectionSheet) {
        mediaAppSelectionSheet { showMediaAppSelectionSheet = false }
    }

    if (showBatteryOptionsSheet) {
        StatusGlanceBatteryOptionsBottomSheet(
            state = state,
            onDismissRequest = { showBatteryOptionsSheet = false },
        )
    }

    if (showCalendarOptionsSheet) {
        calendarOptionsSheet { showCalendarOptionsSheet = false }
    }

    pickingActionForGesture?.let { gesture ->
        gesturePicker(
            when (gesture) {
                "tap" -> state.statusGlanceTapAction.value
                "double_tap" -> state.statusGlanceDoubleTapAction.value
                "long_press" -> state.statusGlanceLongPressAction.value
                else -> null
            },
            { action -> applyActionForGesture(gesture, action) },
            { pickingActionForGesture = null },
        )
    }
}
