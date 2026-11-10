package com.jawzf.pixeltoolkit.feature.island.settings.sheets

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
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.ConfigPickerItem
import com.jawzf.pixeltoolkit.core.design.components.SegmentedDropdownMenuItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.island.IslandState

private val ALARM_WINDOW_OPTIONS = listOf(4, 8, 12, 24)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandAlarmOptionsBottomSheet(
    state: IslandState,
    onDismissRequest: () -> Unit,
) {
    val view = LocalView.current

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
                text = stringResource(R.string.island_alarm_options_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            RoundedCardContainer {
                ConfigPickerItem(
                    title = stringResource(R.string.island_alarm_window_title),
                    selectedValue = stringResource(R.string.island_alarm_window_hours, state.islandAlarmWindowHours.intValue),
                    iconRes = R.drawable.rounded_schedule_24,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    ALARM_WINDOW_OPTIONS.forEach { hours ->
                        SegmentedDropdownMenuItem(
                            text = { Text(stringResource(R.string.island_alarm_window_hours, hours)) },
                            onClick = {
                                HapticUtil.performVirtualKeyHaptic(view)
                                state.setIslandAlarmWindowHours(hours)
                            },
                        )
                    }
                }
                IslandPrefToggle(
                    settingKey = IslandSettings.KEY_ISLAND_ALARM_HIDE_TIME,
                    iconRes = R.drawable.rounded_schedule_24,
                    title = stringResource(R.string.island_alarm_hide_time_title),
                )
            }

            IslandLauncherOnlyToggle(IslandSettings.KEY_ISLAND_ALARM_LAUNCHER_ONLY)
        }
    }
}
