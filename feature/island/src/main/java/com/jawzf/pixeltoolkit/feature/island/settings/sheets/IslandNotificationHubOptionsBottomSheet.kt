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
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedPicker
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.island.IslandState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandNotificationHubOptionsBottomSheet(
    state: IslandState,
    onDismissRequest: () -> Unit,
) {
    val view = LocalView.current

    ToolkitBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.island_notif_hub_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            RoundedCardContainer {
                val autoLabel = stringResource(R.string.island_notif_hub_icons_auto)
                val dynamicLabel = stringResource(R.string.island_notif_hub_order_dynamic)
                val priorityLabel = stringResource(R.string.island_notif_hub_order_priority)
                val recentLabel = stringResource(R.string.island_notif_hub_order_recent)
                SegmentedPicker(
                    items = listOf(
                        IslandSettings.ISLAND_NOTIF_HUB_ORDER_DYNAMIC,
                        IslandSettings.ISLAND_NOTIF_HUB_ORDER_PRIORITY,
                        IslandSettings.ISLAND_NOTIF_HUB_ORDER_RECENT,
                    ),
                    selectedItem = state.islandNotifHubOrder.value,
                    onItemSelected = { state.setIslandNotifHubOrder(it) },
                    labelProvider = {
                        when (it) {
                            IslandSettings.ISLAND_NOTIF_HUB_ORDER_PRIORITY -> priorityLabel
                            IslandSettings.ISLAND_NOTIF_HUB_ORDER_RECENT -> recentLabel
                            else -> dynamicLabel
                        }
                    },
                    title = R.string.island_notif_hub_order_title,
                    modifier = Modifier.fillMaxWidth(),
                )
                IconToggleItem(
                    iconRes = R.drawable.rounded_notifications_off_24,
                    title = stringResource(R.string.island_notif_hub_include_silent),
                    isChecked = state.isIslandNotifHubIncludeSilent.value,
                    onCheckedChange = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandNotifHubIncludeSilent(it)
                    },
                )
                IconToggleItem(
                    iconRes = R.drawable.rounded_apps_24,
                    title = stringResource(R.string.island_notif_hub_group_apps),
                    isChecked = state.isIslandNotifHubGroupByApps.value,
                    onCheckedChange = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandNotifHubGroupByApps(it)
                    },
                )
                ConfigSliderItem(
                    title = stringResource(R.string.island_hide_icon_notifications),
                    value =
                        state.islandNotifHubIcons.intValue
                            .toFloat(),
                    onValueChange = {
                        HapticUtil.performUIHaptic(view)
                        state.setIslandNotifHubIcons(it.toInt())
                    },
                    valueRange = 0f..8f,
                    increment = 1f,
                    iconRes = R.drawable.rounded_notifications_unread_24,
                    valueFormatter = { if (it.toInt() == 0) autoLabel else it.toInt().toString() },
                )
            }
        }
    }
}
