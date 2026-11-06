package com.jawzf.pixeltoolkit.ui.features.display.sheets

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
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

@Composable
fun BatteryColorOptions(
    viewModel: MainViewModel,
    showIdleColor: Boolean = false,
    showLevels: Boolean = false,
) {
    val view = LocalView.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_alert_24,
                title = stringResource(R.string.duo_battery_low_color_title),
                isChecked = viewModel.duo.isDuoBatteryLowColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.duo.setDuoBatteryLowColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = viewModel.duo.isDuoBatteryLowColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (showLevels) {
                        LevelSlider(
                            title = stringResource(R.string.duo_battery_low_level_title),
                            level = viewModel.duo.duoBatteryLowLevel.intValue,
                            range = (DuoBatteryLevels.MIN + 1)..DuoBatteryLevels.MAX,
                            onLevelChange = viewModel.duo::setDuoBatteryLowLevel,
                        )
                    }
                    ColorSwatchPicker(
                        selectedColorHex = viewModel.duo.duoBatteryLowColor.value,
                        onColorSelected = { hex ->
                            viewModel.duo.setDuoBatteryLowColor(hex)
                        },
                    )
                }
            }
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_android_frame_alert_24,
                title = stringResource(R.string.duo_battery_critical_color_title),
                isChecked = viewModel.duo.isDuoBatteryCriticalColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.duo.setDuoBatteryCriticalColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = viewModel.duo.isDuoBatteryCriticalColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (showLevels) {
                        LevelSlider(
                            title = stringResource(R.string.duo_battery_critical_level_title),
                            level = viewModel.duo.duoBatteryCriticalLevel.intValue,
                            range = DuoBatteryLevels.MIN..(DuoBatteryLevels.MAX - 1),
                            onLevelChange = viewModel.duo::setDuoBatteryCriticalLevel,
                        )
                    }
                    ColorSwatchPicker(
                        selectedColorHex = viewModel.duo.duoBatteryCriticalColor.value,
                        onColorSelected = { hex ->
                            viewModel.duo.setDuoBatteryCriticalColor(hex)
                        },
                    )
                }
            }
        }

        // Battery Saver
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_android_frame_plus_24,
                title = stringResource(R.string.duo_battery_power_save_color_title),
                isChecked = viewModel.duo.isDuoBatteryPowerSaveColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.duo.setDuoBatteryPowerSaveColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = viewModel.duo.isDuoBatteryPowerSaveColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ColorSwatchPicker(
                    selectedColorHex = viewModel.duo.duoBatteryPowerSaveColor.value,
                    onColorSelected = { hex ->
                        viewModel.duo.setDuoBatteryPowerSaveColor(hex)
                    },
                )
            }
        }

        // Charging
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_bolt_24,
                title = stringResource(R.string.duo_battery_charging_color_title),
                isChecked = viewModel.duo.isDuoBatteryChargingColorEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.duo.setDuoBatteryChargingColorEnabled(checked)
                },
            )
            AnimatedVisibility(
                visible = viewModel.duo.isDuoBatteryChargingColorEnabled.value,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ColorSwatchPicker(
                    selectedColorHex = viewModel.duo.duoBatteryChargingColor.value,
                    onColorSelected = { hex ->
                        viewModel.duo.setDuoBatteryChargingColor(hex)
                    },
                    allowAuto = true,
                )
            }
        }

        if (showIdleColor) {
            RoundedCardContainer(
                spacing = 2.dp,
                cornerRadius = 24.dp,
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_battery_android_frame_6_24,
                    title = stringResource(R.string.island_battery_idle_color_title),
                    isChecked = viewModel.isIslandBatteryIdleColorEnabled.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        viewModel.setIslandBatteryIdleColorEnabled(checked)
                    },
                )
                AnimatedVisibility(
                    visible = viewModel.isIslandBatteryIdleColorEnabled.value,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    ColorSwatchPicker(
                        selectedColorHex = viewModel.islandBatteryIdleColor.value,
                        onColorSelected = { hex ->
                            viewModel.setIslandBatteryIdleColor(hex)
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
    viewModel: MainViewModel,
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

            BatteryColorOptions(viewModel, showLevels = true)
        }
    }
}
