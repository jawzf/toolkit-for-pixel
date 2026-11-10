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
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.island.IslandState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandBriefOptionsBottomSheet(
    state: IslandState,
    onDismissRequest: () -> Unit,
    highlightSetting: String? = null,
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
                text = stringResource(R.string.island_brief_options_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.rounded_alarm_24,
                    title = stringResource(R.string.island_brief_show_alarm_title),
                    isChecked = state.isIslandBriefShowAlarm.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setIslandBriefShowAlarm(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "island_brief_show_alarm"),
                )

                IslandPrefToggle(
                    settingKey = IslandSettings.KEY_ISLAND_BRIEF_TWO_LINE_HEADER,
                    iconRes = R.drawable.rounded_calendar_today_24,
                    title = stringResource(R.string.island_brief_two_line_header_title),
                )
            }
        }
    }
}
