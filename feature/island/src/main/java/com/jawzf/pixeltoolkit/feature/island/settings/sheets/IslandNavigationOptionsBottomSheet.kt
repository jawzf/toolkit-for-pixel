package com.jawzf.pixeltoolkit.feature.island.settings.sheets

import androidx.compose.foundation.background
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
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.SegmentedDropdownMenuItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedPicker
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.island.IslandState

private val EXPAND_DISTANCE_OPTIONS = listOf(0, 50, 100, 200, 500)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandNavigationOptionsBottomSheet(
    state: IslandState,
    onDismissRequest: () -> Unit,
) {
    val view = LocalView.current

    @Composable
    fun distanceLabel(meters: Int) =
        if (meters == 0) stringResource(R.string.island_navigation_expand_off) else stringResource(R.string.island_navigation_expand_meters, meters)

    ToolkitBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.island_navigation_options_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            RoundedCardContainer {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                        .padding(top = 12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.island_navigation_on_new_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    Text(
                        text = stringResource(R.string.island_navigation_on_new_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    val labels = mapOf(
                        IslandSettings.ISLAND_NAVIGATION_ON_NEW_EXPAND to stringResource(R.string.island_navigation_on_new_expand),
                        IslandSettings.ISLAND_NAVIGATION_ON_NEW_PEEK to stringResource(R.string.island_navigation_on_new_peek),
                        IslandSettings.ISLAND_NAVIGATION_ON_NEW_OFF to stringResource(R.string.island_navigation_expand_off),
                    )
                    SegmentedPicker(
                        items = labels.keys.toList(),
                        selectedItem = state.islandNavigationOnNew.value,
                        onItemSelected = { state.setIslandNavigationOnNew(it) },
                        labelProvider = { labels[it].orEmpty() },
                        title = R.string.island_navigation_on_new_title,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                ConfigPickerItem(
                    title = stringResource(R.string.island_navigation_expand_title),
                    selectedValue = distanceLabel(state.islandNavigationExpandDistance.intValue),
                    iconRes = R.drawable.rounded_magnify_fullscreen_24,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    EXPAND_DISTANCE_OPTIONS.forEach { meters ->
                        SegmentedDropdownMenuItem(
                            text = { Text(distanceLabel(meters)) },
                            onClick = {
                                HapticUtil.performVirtualKeyHaptic(view)
                                state.setIslandNavigationExpandDistance(meters)
                            },
                        )
                    }
                }

                IconToggleItem(
                    iconRes = R.drawable.round_navigation_24,
                    title = stringResource(R.string.island_navigation_peek_after_expand_title),
                    description = stringResource(R.string.island_navigation_peek_after_expand_desc),
                    isChecked = state.isIslandNavigationPeekAfterExpand.value,
                    onCheckedChange = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandNavigationPeekAfterExpand(it)
                    },
                )
            }
        }
    }
}
