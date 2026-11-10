package com.jawzf.pixeltoolkit.feature.island.settings.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.island.IslandState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandBatteryOptionsBottomSheet(
    state: IslandState,
    batteryColorOptions: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val view = LocalView.current

    ToolkitBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.rounded_battery_alert_24,
                    title = stringResource(R.string.island_battery_peek_alerts_title),
                    isChecked = state.isIslandBatteryPeekAlerts.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandBatteryPeekAlerts(checked)
                    },
                )
            }

            if (rememberIslandShowsWhileLocked()) {
                RoundedCardContainer {
                    IslandPrefToggle(
                        settingKey = IslandSettings.KEY_ISLAND_BATTERY_HIDE_LOCKED,
                        iconRes = R.drawable.rounded_mobile_lock_portrait_24,
                        title = stringResource(R.string.island_hide_when_locked_title),
                    )
                }
            }

            IslandBatteryOptions(
                state = state,
                batteryColorOptions = batteryColorOptions,
                onlyLow = state.isIslandBatteryOnlyLow.value,
                onOnlyLowChange = state::setIslandBatteryOnlyLow,
                showIconConditional = true,
            )
        }
    }
}
