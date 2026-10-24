/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: UI Feature - Display
 * File: IslandBriefOptionsBottomSheet.kt
 * Description: Bottom sheet for configuring Island Brief options.
 */

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
import com.jawzf.pixeltoolkit.ui.core.cards.IconToggleItem
import com.jawzf.pixeltoolkit.ui.core.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.ui.core.sheets.EssentialsBottomSheet
import com.jawzf.pixeltoolkit.ui.modifiers.highlight
import com.jawzf.pixeltoolkit.utils.HapticUtil
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IslandBriefOptionsBottomSheet(
    viewModel: MainViewModel,
    onDismissRequest: () -> Unit,
    highlightSetting: String? = null,
) {
    val view = LocalView.current

    EssentialsBottomSheet(
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

            RoundedCardContainer(
                spacing = 2.dp,
                cornerRadius = 24.dp,
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_alarm_24,
                    title = stringResource(R.string.island_brief_show_alarm_title),
                    isChecked = viewModel.isIslandBriefShowAlarm.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        viewModel.setIslandBriefShowAlarm(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "island_brief_show_alarm"),
                )

                IslandPrefToggle(
                    settingKey = SettingsRepository.KEY_ISLAND_BRIEF_TWO_LINE_HEADER,
                    iconRes = R.drawable.rounded_calendar_today_24,
                    title = stringResource(R.string.island_brief_two_line_header_title),
                )
            }
        }
    }
}
