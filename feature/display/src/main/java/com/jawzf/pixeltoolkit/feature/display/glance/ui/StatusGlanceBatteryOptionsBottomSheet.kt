package com.jawzf.pixeltoolkit.feature.display.glance.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentIcon
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentLabel
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.display.glance.StatusGlanceState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusGlanceBatteryOptionsBottomSheet(
    state: StatusGlanceState,
    onDismissRequest: () -> Unit,
) {
    val view = LocalView.current

    ToolkitBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.status_glance_battery_options_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            Text(
                text = stringResource(R.string.status_glance_battery_display_mode_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )

            val modes = listOf("icon", "percentage", "both")
            val icons = listOf(
                R.drawable.battery_android_frame_full_24px,
                R.drawable.rounded_numbers_24,
                R.drawable.battery_android_frame_plus_24px,
            )
            val labels = listOf(
                stringResource(R.string.status_glance_battery_mode_icon),
                stringResource(R.string.status_glance_battery_mode_percentage),
                stringResource(R.string.status_glance_battery_mode_both),
            )

            RoundedCardContainer {
                SegmentedTrack(
                    count = modes.size,
                    modifier =
                        Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surfaceBright,
                                shape = RoundedCornerShape(24.dp),
                            ).padding(horizontal = 16.dp, vertical = 12.dp),
                ) { index ->
                    val mode = modes[index]
                    val selected = state.statusGlanceBatteryDisplayMode.value == mode
                    Segment(
                        selected = selected,
                        selectionModifier =
                            Modifier.selectable(
                                selected = selected,
                                role = Role.RadioButton,
                                onClick = {
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    state.setStatusGlanceBatteryDisplayMode(mode)
                                },
                            ),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SegmentIcon(
                                painter = painterResource(id = icons[index]),
                                contentDescription = null,
                                selected = selected,
                            )
                            Spacer(Modifier.width(6.dp))
                            SegmentLabel(
                                text = labels[index],
                                selected = selected,
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.statusGlanceBatteryDisplayMode.value != "percentage",
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                RoundedCardContainer {
                    ConfigSliderItem(
                        title = stringResource(R.string.status_glance_battery_icon_size_title),
                        value = state.statusGlanceBatteryIconSize.floatValue,
                        onValueChange = { state.setStatusGlanceBatteryIconSize(it) },
                        valueRange = 10f..24f,
                        increment = 1f,
                        iconRes = R.drawable.battery_android_frame_full_24px,
                        valueFormatter = { "${it.toInt()} dp" },
                    )
                }
            }

            Text(
                text = stringResource(R.string.status_glance_section_what_to_show),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp),
            )

            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.battery_android_frame_alert_24px,
                    title = stringResource(R.string.status_glance_battery_show_low_title),
                    isChecked = state.isStatusGlanceBatteryShowLow.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setStatusGlanceBatteryShowLow(checked)
                    },
                )

                IconToggleItem(
                    iconRes = R.drawable.battery_android_frame_bolt_24px,
                    title = stringResource(R.string.status_glance_battery_show_charging_title),
                    isChecked = state.isStatusGlanceBatteryShowCharging.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setStatusGlanceBatteryShowCharging(checked)
                    },
                )

                IconToggleItem(
                    iconRes = R.drawable.battery_android_frame_full_24px,
                    title = stringResource(R.string.status_glance_battery_show_full_title),
                    isChecked = state.isStatusGlanceBatteryShowFull.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setStatusGlanceBatteryShowFull(checked)
                    },
                )

                IconToggleItem(
                    iconRes = R.drawable.battery_android_frame_4_24px,
                    title = stringResource(R.string.status_glance_battery_show_otherwise_title),
                    isChecked = state.isStatusGlanceBatteryShowOtherwise.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setStatusGlanceBatteryShowOtherwise(checked)
                    },
                )
            }
        }
    }
}

