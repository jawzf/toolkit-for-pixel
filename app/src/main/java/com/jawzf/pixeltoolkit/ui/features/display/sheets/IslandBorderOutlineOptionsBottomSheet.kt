package com.jawzf.pixeltoolkit.ui.features.display.sheets

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
import com.jawzf.pixeltoolkit.R
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.ui.components.sliders.ConfigSliderItem
import com.jawzf.pixeltoolkit.ui.core.cards.IconToggleItem
import com.jawzf.pixeltoolkit.ui.core.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.ui.core.pickers.ColorSwatchPicker
import com.jawzf.pixeltoolkit.ui.core.pickers.DUO_PRESET_COLORS
import com.jawzf.pixeltoolkit.ui.core.sheets.EssentialsBottomSheet
import com.jawzf.pixeltoolkit.utils.HapticUtil
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

// Default gray first, then the shared preset palette.
private val OUTLINE_COLORS =
    listOf(SettingsRepository.ISLAND_BORDER_OUTLINE_DEFAULT_COLOR) + DUO_PRESET_COLORS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandBorderOutlineOptionsBottomSheet(
    viewModel: MainViewModel,
    onDismissRequest: () -> Unit,
) {
    val view = LocalView.current

    EssentialsBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.island_border_outline_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            RoundedCardContainer(spacing = 2.dp, cornerRadius = 24.dp) {
                ConfigSliderItem(
                    title = stringResource(R.string.island_border_outline_thickness_title),
                    value = viewModel.islandBorderOutlineThickness.floatValue,
                    onValueChange = {
                        HapticUtil.performUIHaptic(view)
                        viewModel.setIslandBorderOutlineThickness(it)
                    },
                    valueRange = 1f..5f,
                    increment = 1f,
                    iconRes = R.drawable.rounded_line_weight_24,
                    valueFormatter = { "${it.toInt()} dp" },
                )

                IconToggleItem(
                    iconRes = R.drawable.rounded_visibility_off_24,
                    title = stringResource(R.string.island_border_outline_hide_expanded_title),
                    description = stringResource(R.string.island_border_outline_hide_expanded_desc),
                    isChecked = viewModel.isIslandBorderOutlineHiddenWhenExpanded.value,
                    onCheckedChange = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        viewModel.setIslandBorderOutlineHiddenWhenExpanded(it)
                    },
                )

                IconToggleItem(
                    iconRes = R.drawable.rounded_palette_24,
                    title = stringResource(R.string.island_border_outline_dynamic_title),
                    isChecked = viewModel.isIslandBorderOutlineDynamic.value,
                    onCheckedChange = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        viewModel.setIslandBorderOutlineDynamic(it)
                    },
                )

                ColorSwatchPicker(
                    selectedColorHex = viewModel.islandBorderOutlineColor.value,
                    onColorSelected = { viewModel.setIslandBorderOutlineColor(it) },
                    colors = OUTLINE_COLORS,
                )
            }
        }
    }
}
