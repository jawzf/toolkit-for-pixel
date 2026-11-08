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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedPicker
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandCallOptionsBottomSheet(onDismissRequest: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val settings = remember { IslandSettings(context) }
    var mode by remember {
        mutableStateOf(
            settings.getString(IslandSettings.KEY_ISLAND_CALL_SHOW_MODE, IslandSettings.ISLAND_CALL_SHOW_PEEK)
                ?: IslandSettings.ISLAND_CALL_SHOW_PEEK,
        )
    }
    var sticky by remember { mutableStateOf(settings.getBoolean(IslandSettings.KEY_ISLAND_CALL_STICKY, false)) }

    ToolkitBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.island_call_options_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            RoundedCardContainer(spacing = 2.dp, cornerRadius = 24.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                        .padding(top = 12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.island_call_show_mode_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                    val modes = listOf(
                        IslandSettings.ISLAND_CALL_SHOW_EXPANDED,
                        IslandSettings.ISLAND_CALL_SHOW_PEEK,
                        IslandSettings.ISLAND_CALL_SHOW_COMPACT,
                    )
                    val labels = mapOf(
                        IslandSettings.ISLAND_CALL_SHOW_EXPANDED to stringResource(R.string.island_call_show_expanded),
                        IslandSettings.ISLAND_CALL_SHOW_PEEK to stringResource(R.string.island_call_show_peek),
                        IslandSettings.ISLAND_CALL_SHOW_COMPACT to stringResource(R.string.island_call_show_compact),
                    )
                    SegmentedPicker(
                        items = modes,
                        selectedItem = mode,
                        onItemSelected = {
                            mode = it
                            settings.putString(IslandSettings.KEY_ISLAND_CALL_SHOW_MODE, it)
                        },
                        labelProvider = { labels[it].orEmpty() },
                        title = R.string.island_call_show_mode_title,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                if (mode != IslandSettings.ISLAND_CALL_SHOW_COMPACT) {
                    IconToggleItem(
                        iconRes = R.drawable.rounded_lock_24,
                        title = stringResource(R.string.island_call_sticky_title),
                        isChecked = sticky,
                        onCheckedChange = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            sticky = it
                            settings.putBoolean(IslandSettings.KEY_ISLAND_CALL_STICKY, it)
                        },
                    )
                }
            }
        }
    }
}
