package com.jawzf.pixeltoolkit.feature.display.duo.ui

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
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.feature.display.duo.DuoState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuoBatteryPercentageOptionsBottomSheet(
    state: DuoState,
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
                text = stringResource(R.string.duo_battery_percentage_options_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
            )

            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.rounded_palette_24,
                    title = stringResource(R.string.duo_battery_percentage_only_colored_title),
                    description = stringResource(R.string.duo_battery_percentage_only_colored_desc),
                    isChecked = state.isDuoBatteryPercentageOnlyColored.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setDuoBatteryPercentageOnlyColored(checked)
                    },
                )
            }
        }
    }
}
