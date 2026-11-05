package com.jawzf.pixeltoolkit.feature.sound.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.feature.sound.SoundState
import kotlin.math.roundToInt

@Composable
fun VolumeStepsSettingsUI(
    state: SoundState,
    isAccessibilityEnabled: Boolean,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val steps = state.volumeStepsPerPress.intValue

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_volume_up_24,
                title = stringResource(R.string.volume_steps_enable_title),
                isChecked = state.isVolumeStepsEnabled.value,
                onCheckedChange = { state.setVolumeStepsEnabled(it) },
                enabled = isAccessibilityEnabled,
                modifier = Modifier.highlight(highlightSetting == "enable_volume_steps"),
            )

            ConfigSliderItem(
                title = stringResource(R.string.volume_steps_per_press_title),
                value = steps.toFloat(),
                onValueChange = { state.setVolumeStepsPerPress(it.roundToInt()) },
                valueRange = 1f..5f,
                steps = 3,
                increment = 1f,
                valueFormatter = { it.roundToInt().toString() },
                enabled = state.isVolumeStepsEnabled.value,
                iconRes = R.drawable.rounded_steps_24,
                modifier = Modifier.highlight(highlightSetting == "volume_steps_per_press"),
            )
        }

        Text(
            text = stringResource(R.string.volume_steps_hint),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
