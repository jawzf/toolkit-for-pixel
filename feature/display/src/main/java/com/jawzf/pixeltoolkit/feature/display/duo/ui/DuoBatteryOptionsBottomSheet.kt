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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.duo.DuoBatteryLevels
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.ColorSwatchPicker
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.display.duo.DuoState

@Composable
fun BatteryColorOptions(
    state: DuoState,
    isIslandBatteryIdleColorEnabled: Boolean,
    islandBatteryIdleColor: String,
    onIslandBatteryIdleColorEnabledChange: (Boolean) -> Unit,
    onIslandBatteryIdleColorChange: (String) -> Unit,
    showIdleColor: Boolean = false,
    showLevels: Boolean = false,
) {
    val view = LocalView.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_alert_24,
                title = stringResource(R.string.duo_battery_low_color_title),
                isChecked = state.isDuoBatteryLowColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoBatteryLowColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = state.isDuoBatteryLowColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (showLevels) {
                        LevelSlider(
                            title = stringResource(R.string.duo_battery_low_level_title),
                            level = state.duoBatteryLowLevel.intValue,
                            range = (DuoBatteryLevels.MIN + 1)..DuoBatteryLevels.MAX,
                            onLevelChange = state::setDuoBatteryLowLevel,
                        )
                    }
                    ColorSwatchPicker(
                        selectedColorHex = state.duoBatteryLowColor.value,
                        onColorSelected = { hex ->
                            state.setDuoBatteryLowColor(hex)
                        },
                    )
                }
            }
        }

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_android_frame_alert_24,
                title = stringResource(R.string.duo_battery_critical_color_title),
                isChecked = state.isDuoBatteryCriticalColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoBatteryCriticalColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = state.isDuoBatteryCriticalColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (showLevels) {
                        LevelSlider(
                            title = stringResource(R.string.duo_battery_critical_level_title),
                            level = state.duoBatteryCriticalLevel.intValue,
                            range = DuoBatteryLevels.MIN..(DuoBatteryLevels.MAX - 1),
                            onLevelChange = state::setDuoBatteryCriticalLevel,
                        )
                    }
                    ColorSwatchPicker(
                        selectedColorHex = state.duoBatteryCriticalColor.value,
                        onColorSelected = { hex ->
                            state.setDuoBatteryCriticalColor(hex)
                        },
                    )
                }
            }
        }

        // Battery Saver
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_android_frame_plus_24,
                title = stringResource(R.string.duo_battery_power_save_color_title),
                isChecked = state.isDuoBatteryPowerSaveColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoBatteryPowerSaveColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = state.isDuoBatteryPowerSaveColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ColorSwatchPicker(
                    selectedColorHex = state.duoBatteryPowerSaveColor.value,
                    onColorSelected = { hex ->
                        state.setDuoBatteryPowerSaveColor(hex)
                    },
                )
            }
        }

        // Charging
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_bolt_24,
                title = stringResource(R.string.duo_battery_charging_color_title),
                isChecked = state.isDuoBatteryChargingColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setDuoBatteryChargingColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = state.isDuoBatteryChargingColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ColorSwatchPicker(
                    selectedColorHex = state.duoBatteryChargingColor.value,
                    onColorSelected = { hex ->
                        state.setDuoBatteryChargingColor(hex)
                    },
                    allowAuto = true,
                )
            }
        }

        if (showIdleColor) {
            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.rounded_battery_android_frame_6_24,
                    title = stringResource(R.string.island_battery_idle_color_title),
                    isChecked = isIslandBatteryIdleColorEnabled,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        onIslandBatteryIdleColorEnabledChange(checked)
                    },
                )
                AnimatedVisibility(
                    visible = isIslandBatteryIdleColorEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    ColorSwatchPicker(
                        selectedColorHex = islandBatteryIdleColor,
                        onColorSelected = { hex ->
                            onIslandBatteryIdleColorChange(hex)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelSlider(
    title: String,
    level: Int,
    range: IntRange,
    onLevelChange: (Int) -> Unit,
) {
    val view = LocalView.current
    ConfigSliderItem(
        title = title,
        value = level.toFloat(),
        onValueChange = {
            HapticUtil.performUIHaptic(view)
            onLevelChange(it.toInt().coerceIn(range))
        },
        valueRange = range.first.toFloat()..range.last.toFloat(),
        increment = 1f,
        iconRes = R.drawable.rounded_percent_24,
        valueFormatter = { "${it.toInt()}%" },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuoBatteryOptionsBottomSheet(
    state: DuoState,
    isIslandBatteryIdleColorEnabled: Boolean,
    islandBatteryIdleColor: String,
    onIslandBatteryIdleColorEnabledChange: (Boolean) -> Unit,
    onIslandBatteryIdleColorChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ToolkitBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.duo_battery_options_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            BatteryColorOptions(
                state = state,
                isIslandBatteryIdleColorEnabled = isIslandBatteryIdleColorEnabled,
                islandBatteryIdleColor = islandBatteryIdleColor,
                onIslandBatteryIdleColorEnabledChange = onIslandBatteryIdleColorEnabledChange,
                onIslandBatteryIdleColorChange = onIslandBatteryIdleColorChange,
                showLevels = true,
            )
        }
    }
}
