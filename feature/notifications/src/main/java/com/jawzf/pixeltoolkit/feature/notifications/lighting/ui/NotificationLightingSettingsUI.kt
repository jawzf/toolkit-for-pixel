package com.jawzf.pixeltoolkit.feature.notifications.lighting.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingColorMode
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingStyle
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.FeatureCard
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ColorCircle
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentLabel
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.notifications.lighting.LightingState
import com.jawzf.pixeltoolkit.feature.notifications.lighting.NotificationLightingRipplePosition
import com.jawzf.pixeltoolkit.feature.notifications.lighting.NotificationLightingSweepPosition
import com.jawzf.pixeltoolkit.feature.notifications.lighting.ui.AppColorSelectionSheet
import com.jawzf.pixeltoolkit.feature.notifications.lighting.ui.GlowSidesPicker
import com.jawzf.pixeltoolkit.feature.notifications.lighting.ui.NotificationLightingColorModePicker
import com.jawzf.pixeltoolkit.feature.notifications.lighting.ui.NotificationLightingStylePicker
import com.jawzf.pixeltoolkit.feature.notifications.lighting.ui.NotificationLightingSystemModePicker
import com.jawzf.pixeltoolkit.feature.notifications.lighting.ui.SweepShapesBottomSheet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationLightingSettingsUI(
    state: LightingState,
    isShizukuPermissionGranted: Boolean,
    onRefreshPermissions: () -> Unit,
    appSelectionSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current

    // App selection Logic
    var showAppSelectionSheet by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }
    var showSweepShapesSheet by remember { mutableStateOf(false) }
    var showAppColorsSheet by remember { mutableStateOf(false) }

    // Corner radius state

    // Corner radius state
    var cornerRadiusDp by remember {
        mutableFloatStateOf(
            state.loadNotificationLightingCornerRadius(),
        )
    }
    var strokeThicknessDp by remember {
        mutableFloatStateOf(
            state.loadNotificationLightingStrokeThickness(),
        )
    }

    var indicatorX by remember { mutableFloatStateOf(state.notificationLightingIndicatorX.value) }
    var indicatorY by remember { mutableFloatStateOf(state.notificationLightingIndicatorY.value) }
    var indicatorScale by remember { mutableFloatStateOf(state.notificationLightingIndicatorScale.value) }

    val coroutineScope = rememberCoroutineScope()

    val previewRipple: () -> Unit = {
        state.triggerNotificationLightingForRipple(context)
        coroutineScope.launch {
            delay(5000)
            state.removePreviewOverlay(context)
        }
    }

    val previewDash: () -> Unit = {
        state.triggerNotificationLightingForDash(context)
        coroutineScope.launch {
            delay(5000)
            state.removePreviewOverlay(context)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            state.removePreviewOverlay(context)
        }
    }

    val isNotificationLightingEnabled by state.isNotificationLightingEnabled

    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_magnify_fullscreen_24,
                title = stringResource(R.string.feat_notification_lighting_title),
                isChecked = isNotificationLightingEnabled,
                onCheckedChange = { checked ->
                    state.setNotificationLightingEnabled(checked)
                },
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_power_settings_new_24,
                title = stringResource(R.string.notification_lighting_screen_off_title),
                isChecked = state.onlyShowWhenScreenOff.value,
                onCheckedChange = { checked ->
                    state.setOnlyShowWhenScreenOff(checked)
                },
                enabled = isNotificationLightingEnabled,
                modifier = Modifier.highlight(highlightSetting == "only_screen_off"),
            )
            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_off_24,
                title = stringResource(R.string.notification_lighting_skip_silent_title),
                isChecked = state.skipSilentNotifications.value,
                onCheckedChange = { checked ->
                    state.setSkipSilentNotifications(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "skip_silent_notifications"),
            )
            IconToggleItem(
                iconRes = R.drawable.outline_circle_notifications_24,
                title = stringResource(R.string.notification_lighting_skip_persistent_title),
                isChecked = state.skipPersistentNotifications.value,
                onCheckedChange = { checked ->
                    state.setSkipPersistentNotifications(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "skip_persistent_notifications"),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App Selection Sheet Button
        Button(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                showAppSelectionSheet = true
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(64.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_apps_24),
                    contentDescription = "Apps",
                )
                Text(stringResource(R.string.action_select_apps))
            }
        }

        // Style Picker
        Text(
            text = stringResource(R.string.settings_section_style),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        NotificationLightingStylePicker(
            selectedStyle = state.notificationLightingStyle.value,
            onStyleSelected = { style ->
                if (style == NotificationLightingStyle.SYSTEM && !ShellUtils.hasPermission(context)) {
                    showPermissionsSheet = true
                    return@NotificationLightingStylePicker
                }
                state.setNotificationLightingStyle(style, context)
                if (style.usesRestrictedColorModes &&
                    state.notificationLightingColorMode.value == NotificationLightingColorMode.CUSTOM
                ) {
                    state.setNotificationLightingColorMode(
                        NotificationLightingColorMode.SYSTEM,
                    )
                }
                state.triggerNotificationLighting(context)
            },
        )

        val style = state.notificationLightingStyle.value

        // System Mode Sector (Only for SYSTEM style)
        if (style == NotificationLightingStyle.SYSTEM) {
            Text(
                text = stringResource(R.string.notification_lighting_system_mode_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            NotificationLightingSystemModePicker(
                selectedMode = state.notificationLightingSystemMode.intValue,
                onModeSelected = { mode ->
                    state.setNotificationLightingSystemMode(mode)
                    state.triggerNotificationLightingSystem(context)
                },
            )

            // Show placement sliders for custom system ripple
            if (state.notificationLightingSystemMode.intValue == 2) {
                Text(
                    text = stringResource(R.string.notification_lighting_indicator_adjustment_section),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                RoundedCardContainer(modifier = Modifier) {
                    ConfigSliderItem(
                        title = stringResource(R.string.notification_lighting_h_pos_title),
                        value = indicatorX,
                        onValueChange = { newValue ->
                            indicatorX = newValue
                            HapticUtil.performSliderHaptic(view)
                            state.triggerNotificationLightingSystem(context)
                        },
                        valueRange = 0f..100f,
                        valueFormatter = { "%.1f%%".format(it) },
                        onValueChangeFinished = {
                            state.saveNotificationLightingIndicatorX(indicatorX)
                        },
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.notification_lighting_v_pos_title),
                        value = indicatorY,
                        onValueChange = { newValue ->
                            indicatorY = newValue
                            HapticUtil.performSliderHaptic(view)
                            state.triggerNotificationLightingSystem(context)
                        },
                        valueRange = 0f..100f,
                        valueFormatter = { "%.1f%%".format(it) },
                        onValueChangeFinished = {
                            state.saveNotificationLightingIndicatorY(indicatorY)
                        },
                    )
                }
            }
        }

        // Stroke Adjustment Section (For STROKE and DASH styles, which share the outline)
        if (style == NotificationLightingStyle.STROKE || style == NotificationLightingStyle.DASH) {
            Text(
                text = stringResource(R.string.notification_lighting_stroke_adjustment_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_corner_radius_title),
                    value = cornerRadiusDp,
                    onValueChange = { newValue ->
                        cornerRadiusDp = newValue
                        HapticUtil.performSliderHaptic(view)
                        // Show preview overlay while dragging
                        state.triggerNotificationLightingWithRadiusAndThickness(
                            context,
                            newValue,
                            strokeThicknessDp,
                        )
                    },
                    valueRange = 0f..75f,
                    valueFormatter = { "%.1f".format(it) },
                    onValueChangeFinished = {
                        // Save the corner radius
                        state.saveNotificationLightingCornerRadius(cornerRadiusDp)
                        // Wait 5 seconds then remove preview overlay
                        coroutineScope.launch {
                            delay(5000)
                            state.removePreviewOverlay(context)
                        }
                    },
                    modifier = Modifier.highlight(highlightSetting == "corner_radius"),
                )

                if (style == NotificationLightingStyle.STROKE) {
                    ConfigSliderItem(
                        title = stringResource(R.string.notification_lighting_stroke_thickness_title),
                        value = strokeThicknessDp,
                        onValueChange = { newValue ->
                            strokeThicknessDp = newValue
                            HapticUtil.performSliderHaptic(view)
                            // Show preview overlay while dragging
                            state.triggerNotificationLightingWithRadiusAndThickness(
                                context,
                                cornerRadiusDp,
                                newValue,
                            )
                        },
                        modifier = Modifier.highlight(highlightSetting == "stroke_thickness"),
                        valueRange = 1f..20f,
                        valueFormatter = { "%.1f".format(it) },
                        onValueChangeFinished = {
                            // Save the stroke thickness
                            state.saveNotificationLightingStrokeThickness(
                                strokeThicknessDp,
                            )
                            // Wait 5 seconds then remove preview overlay
                            coroutineScope.launch {
                                delay(5000)
                                state.removePreviewOverlay(context)
                            }
                        },
                    )
                }
            }
        }

        if (style == NotificationLightingStyle.DASH) {
            Text(
                text = stringResource(R.string.notification_lighting_dash_adjustment_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_stroke_thickness_title),
                    value = state.dash.thickness.floatValue,
                    onValueChange = { newValue ->
                        state.dash.thickness.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 1f..20f,
                    increment = 0.5f,
                    valueFormatter = { "%.1f".format(it) },
                    onValueChangeFinished = {
                        state.dash.saveThickness(state.dash.thickness.floatValue)
                        previewDash()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_dash_length_title),
                    value = state.dash.length.floatValue,
                    onValueChange = { newValue ->
                        state.dash.length.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 5f..100f,
                    increment = 1f,
                    valueFormatter = { "%.0f%%".format(it) },
                    onValueChangeFinished = {
                        state.dash.saveLength(state.dash.length.floatValue)
                        previewDash()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_dash_glow_title),
                    value = state.dash.glow.floatValue,
                    onValueChange = { newValue ->
                        state.dash.glow.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 0f..3f,
                    increment = 0.1f,
                    valueFormatter = { "%.1fx".format(it) },
                    onValueChangeFinished = {
                        state.dash.saveGlow(state.dash.glow.floatValue)
                        previewDash()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_dash_glow_length_title),
                    value = state.dash.glowLength.floatValue,
                    onValueChange = { newValue ->
                        state.dash.glowLength.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 1f..3f,
                    increment = 0.1f,
                    valueFormatter = { "%.1fx".format(it) },
                    onValueChangeFinished = {
                        state.dash.saveGlowLength(state.dash.glowLength.floatValue)
                        previewDash()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_pulse_overlap_title),
                    value = state.dash.overlap.floatValue,
                    onValueChange = { newValue ->
                        state.dash.overlap.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 0f..90f,
                    increment = 5f,
                    valueFormatter = { "%.0f%%".format(it) },
                    enabled = state.notificationLightingPulseCount.value > 1f,
                    onValueChangeFinished = {
                        state.dash.saveOverlap(state.dash.overlap.floatValue)
                        previewDash()
                    },
                )
            }
        }

        // Glow Adjustment Section (For GLOW style)
        if (style == NotificationLightingStyle.GLOW) {
            Text(
                text = stringResource(R.string.notification_lighting_glow_adjustment_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                GlowSidesPicker(
                    selectedSides = state.notificationLightingGlowSides.value,
                    onSideToggled = { side, isChecked ->
                        val current = state.notificationLightingGlowSides.value.toMutableSet()
                        if (isChecked) current.add(side) else current.remove(side)
                        state.setNotificationLightingGlowSides(current)
                        state.triggerNotificationLighting(context)
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_glow_spread_title),
                    value = strokeThicknessDp,
                    onValueChange = { newValue ->
                        strokeThicknessDp = newValue
                        HapticUtil.performSliderHaptic(view)
                        state.triggerNotificationLightingWithRadiusAndThickness(
                            context,
                            cornerRadiusDp,
                            newValue,
                        )
                    },
                    valueRange = 1f..10f,
                    valueFormatter = { "%.1f".format(it) },
                    onValueChangeFinished = {
                        state.saveNotificationLightingStrokeThickness(
                            strokeThicknessDp,
                        )
                        coroutineScope.launch {
                            delay(2000)
                            state.removePreviewOverlay(context)
                        }
                    },
                )
            }
        }

        // Sweep Adjustment
        if (style == NotificationLightingStyle.SWEEP) {
            Text(
                text = stringResource(R.string.notification_lighting_sweep_position_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                SweepPositionPicker(
                    selectedPosition = state.notificationLightingSweepPosition.value,
                    onPositionSelected = { pos ->
                        state.setNotificationLightingSweepPosition(pos)
                        state.triggerNotificationLightingForSweep(
                            context,
                            pos,
                            state.notificationLightingSweepThickness.floatValue,
                        )
                        // Auto remove preview
                        coroutineScope.launch {
                            delay(5000)
                            state.removePreviewOverlay(context)
                        }
                    },
                )

                IconToggleItem(
                    iconRes = R.drawable.rounded_interests_24,
                    title = stringResource(R.string.notification_lighting_sweep_random_shapes_title),
                    isChecked = state.notificationLightingSweepRandomShapes.value,
                    onCheckedChange = { checked ->
                        state.saveNotificationLightingSweepRandomShapes(checked)
                        HapticUtil.performSliderHaptic(view)
                        state.triggerNotificationLightingForSweep(
                            context,
                            state.notificationLightingSweepPosition.value,
                            state.notificationLightingSweepThickness.floatValue,
                        )
                        coroutineScope.launch {
                            delay(5000)
                            state.removePreviewOverlay(context)
                        }
                    },
                )

                if (state.notificationLightingSweepRandomShapes.value) {
                    FeatureCard(
                        title = stringResource(R.string.notification_lighting_sweep_select_shapes_title),
                        description = stringResource(R.string.notification_lighting_sweep_select_shapes_desc),
                        iconRes = R.drawable.rounded_circles_24,
                        isEnabled = true,
                        showToggle = false,
                        hasMoreSettings = true,
                        onToggle = {},
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            showSweepShapesSheet = true
                        },
                    )
                }

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_sweep_thickness_title),
                    value = state.notificationLightingSweepThickness.floatValue,
                    onValueChange = { newValue ->
                        state.notificationLightingSweepThickness.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                        state.triggerNotificationLightingForSweep(
                            context,
                            state.notificationLightingSweepPosition.value,
                            newValue,
                        )
                    },
                    valueRange = 1f..50f,
                    valueFormatter = { "%.1f".format(it) },
                    onValueChangeFinished = {
                        state.saveNotificationLightingSweepThickness(
                            state.notificationLightingSweepThickness.floatValue,
                        )
                        coroutineScope.launch {
                            delay(5000)
                            state.removePreviewOverlay(context)
                        }
                    },
                )
            }
        }

        if (style == NotificationLightingStyle.RIPPLE) {
            Text(
                text = stringResource(R.string.notification_lighting_ripple_position_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                RipplePositionPicker(
                    selectedPosition = state.ripple.position.value,
                    onPositionSelected = { position ->
                        state.ripple.savePosition(position)
                        if (position == NotificationLightingRipplePosition.MANUAL) {
                            context.startActivity(
                                Intent().setClassName(context, AppComponents.RIPPLE_POSITION_PICKER_ACTIVITY),
                            )
                        } else {
                            previewRipple()
                        }
                    },
                )

                if (state.ripple.position.value ==
                    NotificationLightingRipplePosition.MANUAL
                ) {
                    FeatureCard(
                        title = stringResource(R.string.notification_lighting_ripple_position_pick_title),
                        description = stringResource(R.string.notification_lighting_ripple_position_pick_desc),
                        iconRes = R.drawable.rounded_target_24,
                        isEnabled = true,
                        showToggle = false,
                        hasMoreSettings = true,
                        onToggle = {},
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            context.startActivity(
                                Intent().setClassName(context, AppComponents.RIPPLE_POSITION_PICKER_ACTIVITY),
                            )
                        },
                    )
                }
            }

            Text(
                text = stringResource(R.string.notification_lighting_ripple_adjustment_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_auto_awesome_24,
                    title = stringResource(R.string.notification_lighting_ripple_sparkle_title),
                    isChecked = state.ripple.sparklesEnabled.value,
                    onCheckedChange = { checked ->
                        state.ripple.saveSparklesEnabled(checked)
                        HapticUtil.performSliderHaptic(view)
                        previewRipple()
                    },
                )

                if (state.ripple.sparklesEnabled.value) {
                    ConfigSliderItem(
                        title = stringResource(R.string.notification_lighting_ripple_sparkle_count_title),
                        value = state.ripple.sparkleCount.floatValue,
                        onValueChange = { newValue ->
                            state.ripple.sparkleCount.floatValue = newValue
                            HapticUtil.performSliderHaptic(view)
                        },
                        valueRange = 0f..1000f,
                        increment = 10f,
                        valueFormatter = { "%.0f".format(it) },
                        onValueChangeFinished = {
                            state.ripple.saveSparkleCount(state.ripple.sparkleCount.floatValue)
                            previewRipple()
                        },
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.notification_lighting_ripple_sparkle_size_title),
                        value = state.ripple.sparkleSize.floatValue,
                        onValueChange = { newValue ->
                            state.ripple.sparkleSize.floatValue = newValue
                            HapticUtil.performSliderHaptic(view)
                        },
                        valueRange = 0.1f..3f,
                        increment = 0.1f,
                        valueFormatter = { "%.1fx".format(it) },
                        onValueChangeFinished = {
                            state.ripple.saveSparkleSize(state.ripple.sparkleSize.floatValue)
                            previewRipple()
                        },
                    )
                }

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_ripple_speed_title),
                    value = state.ripple.speed.floatValue,
                    onValueChange = { newValue ->
                        state.ripple.speed.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 0.5f..3f,
                    increment = 0.1f,
                    valueFormatter = { "%.1fx".format(it) },
                    onValueChangeFinished = {
                        state.ripple.saveSpeed(state.ripple.speed.floatValue)
                        previewRipple()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_ripple_repeat_count_title),
                    value = state.ripple.repeatCount.floatValue,
                    onValueChange = { newValue ->
                        state.ripple.repeatCount.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 1f..10f,
                    steps = 8,
                    increment = 1f,
                    valueFormatter = { "%.0f".format(it) },
                    onValueChangeFinished = {
                        state.ripple.saveRepeatCount(state.ripple.repeatCount.floatValue)
                        previewRipple()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_pulse_overlap_title),
                    value = state.ripple.overlap.floatValue,
                    onValueChange = { newValue ->
                        state.ripple.overlap.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 0f..90f,
                    increment = 5f,
                    valueFormatter = { "%.0f%%".format(it) },
                    enabled = state.ripple.repeatCount.floatValue > 1f,
                    onValueChangeFinished = {
                        state.ripple.saveOverlap(state.ripple.overlap.floatValue)
                        previewRipple()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_ripple_wave_size_title),
                    value = state.ripple.waveSize.floatValue,
                    onValueChange = { newValue ->
                        state.ripple.waveSize.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 0.5f..8f,
                    increment = 0.1f,
                    valueFormatter = { "%.1fx".format(it) },
                    onValueChangeFinished = {
                        state.ripple.saveWaveSize(state.ripple.waveSize.floatValue)
                        previewRipple()
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_ripple_opacity_title),
                    value = state.ripple.opacity.floatValue,
                    onValueChange = { newValue ->
                        state.ripple.opacity.floatValue = newValue
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 0.2f..2f,
                    increment = 0.1f,
                    valueFormatter = { "%.1fx".format(it) },
                    onValueChangeFinished = {
                        state.ripple.saveOpacity(state.ripple.opacity.floatValue)
                        previewRipple()
                    },
                )
            }
        }

        // Indicator Adjustment Section (For INDICATOR style)
        if (style == NotificationLightingStyle.INDICATOR) {
            Text(
                text = stringResource(R.string.notification_lighting_placement_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_h_pos_title),
                    value = indicatorX,
                    onValueChange = { newValue ->
                        indicatorX = newValue
                        HapticUtil.performSliderHaptic(view)
                        state.triggerNotificationLightingForIndicator(
                            context,
                            newValue,
                            indicatorY,
                            indicatorScale,
                        )
                    },
                    valueRange = 0f..100f,
                    valueFormatter = { "%.1f%%".format(it) },
                    onValueChangeFinished = {
                        state.saveNotificationLightingIndicatorX(indicatorX)
                        coroutineScope.launch {
                            delay(5000)
                            state.removePreviewOverlay(context)
                        }
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_v_pos_title),
                    value = indicatorY,
                    onValueChange = { newValue ->
                        indicatorY = newValue
                        HapticUtil.performSliderHaptic(view)
                        state.triggerNotificationLightingForIndicator(
                            context,
                            indicatorX,
                            newValue,
                            indicatorScale,
                        )
                    },
                    valueRange = 0f..100f,
                    valueFormatter = { "%.1f%%".format(it) },
                    onValueChangeFinished = {
                        state.saveNotificationLightingIndicatorY(indicatorY)
                        coroutineScope.launch {
                            delay(5000)
                            state.removePreviewOverlay(context)
                        }
                    },
                )
            }

            Text(
                text = stringResource(R.string.notification_lighting_indicator_adjustment_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_scale_title),
                    value = indicatorScale,
                    onValueChange = { newValue ->
                        indicatorScale = newValue
                        HapticUtil.performSliderHaptic(view)
                        state.triggerNotificationLightingForIndicator(
                            context,
                            indicatorX,
                            indicatorY,
                            newValue,
                        )
                    },
                    valueRange = 0.5f..3f,
                    valueFormatter = { "%.1fx".format(it) },
                    onValueChangeFinished = {
                        state.saveNotificationLightingIndicatorScale(indicatorScale)
                        coroutineScope.launch {
                            delay(5000)
                            state.removePreviewOverlay(context)
                        }
                    },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_duration_title),
                    value = state.notificationLightingPulseDuration.value,
                    onValueChange = {
                        state.saveNotificationLightingPulseDuration(it)
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 1000f..10000f,
                    increment = 100f,
                    valueFormatter = { "%.1fs".format(it / 1000f) },
                    onValueChangeFinished = { state.triggerNotificationLighting(context) },
                )
            }
        }

        // Animation Settings (Only for STROKE, GLOW, SWEEP and DASH)
        if (style == NotificationLightingStyle.STROKE ||
            style == NotificationLightingStyle.GLOW ||
            style == NotificationLightingStyle.SWEEP ||
            style == NotificationLightingStyle.DASH
        ) {
            Text(
                text = stringResource(R.string.settings_section_animation),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(modifier = Modifier) {
                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_pulse_count_title),
                    value = state.notificationLightingPulseCount.value,
                    onValueChange = {
                        state.saveNotificationLightingPulseCount(it)
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 1f..5f,
                    steps = 3,
                    increment = 1f,
                    valueFormatter = { "%.1f".format(it) },
                    onValueChangeFinished = { state.triggerNotificationLighting(context) },
                )

                ConfigSliderItem(
                    title = stringResource(R.string.notification_lighting_pulse_duration_title),
                    value = state.notificationLightingPulseDuration.value,
                    onValueChange = {
                        state.saveNotificationLightingPulseDuration(it)
                        HapticUtil.performSliderHaptic(view)
                    },
                    valueRange = 100f..10000f,
                    increment = 100f,
                    valueFormatter = { "%.1fs".format(it / 1000f) },
                    onValueChangeFinished = { state.triggerNotificationLighting(context) },
                )
            }
        }

        // Color Mode section
        if (style != NotificationLightingStyle.SYSTEM) {
            Text(
                text = stringResource(R.string.settings_section_color_mode),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(
                modifier = Modifier,
            ) {
                NotificationLightingColorModePicker(
                    selectedMode = state.notificationLightingColorMode.value,
                    onModeSelected = { mode ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setNotificationLightingColorMode(mode)
                        state.triggerNotificationLighting(context)
                    },
                    options =
                        if (style.usesRestrictedColorModes) {
                            listOf(
                                R.string.color_mode_material_you to NotificationLightingColorMode.SYSTEM,
                                R.string.color_mode_app_specific to NotificationLightingColorMode.APP_SPECIFIC,
                            )
                        } else {
                            listOf(
                                R.string.color_mode_system to NotificationLightingColorMode.SYSTEM,
                                R.string.color_mode_custom to NotificationLightingColorMode.CUSTOM,
                                R.string.color_mode_app_specific to NotificationLightingColorMode.APP_SPECIFIC,
                            )
                        },
                )

                if (state.notificationLightingColorMode.value ==
                    NotificationLightingColorMode.APP_SPECIFIC
                ) {
                    FeatureCard(
                        title = stringResource(R.string.app_colors_card_title),
                        description = stringResource(R.string.app_colors_card_desc),
                        iconRes = R.drawable.rounded_palette_24,
                        isEnabled = true,
                        showToggle = false,
                        hasMoreSettings = true,
                        onToggle = {},
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            showAppColorsSheet = true
                        },
                    )
                }

                if (state.notificationLightingColorMode.value == NotificationLightingColorMode.CUSTOM) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceBright,
                                    shape = RoundedCornerShape(MaterialTheme.shapes.extraSmall.bottomEnd),
                                ).padding(bottom = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val allColors =
                            remember {
                                val colors = mutableListOf<Int>()
                                val totalColumns = 21

                                for (page in 0..2) {
                                    val row1 = mutableListOf<Int>()
                                    val row2 = mutableListOf<Int>()
                                    val row3 = mutableListOf<Int>()

                                    for (col in 0..6) {
                                        val globalCol = page * 7 + col
                                        val hue = (globalCol.toFloat() / totalColumns) * 360f

                                        // Row 1: Light
                                        row1.add(
                                            android.graphics.Color.HSVToColor(
                                                floatArrayOf(
                                                    hue,
                                                    0.4f,
                                                    1.0f,
                                                ),
                                            ),
                                        )
                                        // Row 2: Regular
                                        row2.add(
                                            android.graphics.Color.HSVToColor(
                                                floatArrayOf(
                                                    hue,
                                                    0.85f,
                                                    1.0f,
                                                ),
                                            ),
                                        )
                                        // Row 3: Dark
                                        row3.add(
                                            android.graphics.Color.HSVToColor(
                                                floatArrayOf(
                                                    hue,
                                                    1.0f,
                                                    0.55f,
                                                ),
                                            ),
                                        )
                                    }
                                    colors.addAll(row1)
                                    colors.addAll(row2)
                                    colors.addAll(row3)
                                }
                                colors
                            }

                        val pages = allColors.chunked(21)
                        val pagerState = rememberPagerState(pageCount = { pages.size })
                        val currentCustomColor = state.notificationLightingCustomColor.intValue

                        HorizontalPager(
                            state = pagerState,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(170.dp),
                        ) { pageIndex ->
                            val pageColors = pages[pageIndex]
                            Column(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                val rows = pageColors.chunked(7)
                                rows.forEach { rowColors ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                    ) {
                                        rowColors.forEach { colorInt ->
                                            ColorCircle(
                                                color = Color(colorInt),
                                                isSelected = currentCustomColor == colorInt,
                                                size = 36.dp,
                                                onClick = {
                                                    HapticUtil.performVirtualKeyHaptic(view)
                                                    state.setNotificationLightingCustomColor(
                                                        colorInt,
                                                    )
                                                    state.triggerNotificationLighting(context)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Pager Indicator
                        Row(
                            Modifier
                                .height(8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            repeat(pages.size) { iteration ->
                                val color =
                                    if (pagerState.currentPage ==
                                        iteration
                                    ) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    }
                                Box(
                                    modifier =
                                        Modifier
                                            .padding(horizontal = 4.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(8.dp),
                                )
                            }
                        }
                    }
                }
            }
        }

        if (style != NotificationLightingStyle.SYSTEM) {
            Text(
                text = stringResource(R.string.settings_section_ambient_display),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = stringResource(R.string.ambient_display_hint),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.rounded_nightlight_24,
                    title = stringResource(R.string.ambient_display_title),
                    description = stringResource(R.string.ambient_display_desc),
                    isChecked = state.isAmbientDisplayEnabled.value,
                    onCheckedChange = { checked ->
                        state.setAmbientDisplayEnabled(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "ambient_display"),
                )
                if (state.isAmbientDisplayEnabled.value) {
                    IconToggleItem(
                        iconRes = R.drawable.rounded_mobile_lock_portrait_24,
                        title = stringResource(R.string.ambient_show_lock_screen_title),
                        description = stringResource(R.string.ambient_show_lock_screen_desc),
                        isChecked = state.isAmbientShowLockScreenEnabled.value,
                        onCheckedChange = { checked ->
                            state.setAmbientShowLockScreenEnabled(checked)
                        },
                        modifier = Modifier.highlight(highlightSetting == "ambient_show_lock_screen"),
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        if (showAppSelectionSheet) {
            appSelectionSheet { showAppSelectionSheet = false }
        }

        if (showAppColorsSheet) {
            AppColorSelectionSheet(
                onDismissRequest = { showAppColorsSheet = false },
                onLoadAllowedApps = { state.loadNotificationLightingSelectedApps() },
            )
        }

        if (showSweepShapesSheet) {
            SweepShapesBottomSheet(
                state = state,
                onDismissRequest = { showSweepShapesSheet = false },
            )
        }

        if (showPermissionsSheet) {
            PermissionsBottomSheet(
                onDismissRequest = { showPermissionsSheet = false },
                featureTitle = R.string.notification_lighting_style_system,
                permissions =
                    listOf(
                        PermissionItem(
                            iconRes = R.drawable.rounded_adb_24,
                            title = R.string.perm_shizuku_title,
                            description = R.string.perm_shizuku_desc,
                            dependentFeatures = listOf(R.string.notification_lighting_style_system),
                            actionLabel = R.string.perm_shizuku_install_action,
                            action = {
                                val intent =
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/rushiranpise/Shizuku-Next"),
                                    )
                                context.startActivity(intent)
                            },
                            secondaryActionLabel = R.string.action_refresh,
                            secondaryAction = { onRefreshPermissions() },
                            isGranted = isShizukuPermissionGranted,
                        ),
                    ),
            )
        }

        if (style == NotificationLightingStyle.SYSTEM) {
            Text(
                text = stringResource(R.string.notification_lighting_system_disclaimer),
                style = MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RipplePositionPicker(
    selectedPosition: NotificationLightingRipplePosition,
    onPositionSelected: (NotificationLightingRipplePosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val positions =
        listOf(
            NotificationLightingRipplePosition.AUTO,
            NotificationLightingRipplePosition.CENTER,
            NotificationLightingRipplePosition.MANUAL,
        )
    val labels =
        listOf(
            stringResource(R.string.notification_lighting_ripple_position_auto),
            stringResource(R.string.notification_lighting_ripple_position_center),
            stringResource(R.string.notification_lighting_ripple_position_manual),
        )
    val view = LocalView.current

    val selectedIndex = positions.indexOf(selectedPosition).coerceAtLeast(0)

    SegmentedTrack(
        count = positions.size,
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                ).padding(horizontal = 16.dp, vertical = 12.dp),
    ) { index ->
        val pos = positions[index]
        val selected = selectedIndex == index
        Segment(
            selected = selected,
            selectionModifier =
                Modifier.selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        onPositionSelected(pos)
                    },
                ),
        ) {
            SegmentLabel(
                text = labels[index],
                selected = selected,
            )
        }
    }
}

@Composable
private fun SweepPositionPicker(
    selectedPosition: NotificationLightingSweepPosition,
    onPositionSelected: (NotificationLightingSweepPosition) -> Unit,
    modifier: Modifier = Modifier,
) {
    val positions =
        listOf(
            NotificationLightingSweepPosition.LEFT,
            NotificationLightingSweepPosition.CENTER,
            NotificationLightingSweepPosition.RIGHT,
        )
    val labels =
        listOf(
            stringResource(R.string.notification_lighting_sweep_pos_left),
            stringResource(R.string.notification_lighting_sweep_pos_center),
            stringResource(R.string.notification_lighting_sweep_pos_right),
        )
    val view = LocalView.current

    val selectedIndex = positions.indexOf(selectedPosition).coerceAtLeast(0)

    SegmentedTrack(
        count = positions.size,
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                ).padding(horizontal = 16.dp, vertical = 12.dp),
    ) { index ->
        val pos = positions[index]
        val selected = selectedIndex == index
        Segment(
            selected = selected,
            selectionModifier =
                Modifier.selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        onPositionSelected(pos)
                    },
                ),
        ) {
            SegmentLabel(
                text = labels[index],
                selected = selected,
            )
        }
    }
}
