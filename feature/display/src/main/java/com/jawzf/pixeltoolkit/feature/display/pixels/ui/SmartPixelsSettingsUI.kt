package com.jawzf.pixeltoolkit.feature.display.pixels.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.feature.display.pixels.SmartPixelsState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SmartPixelsSettingsUI(
    state: SmartPixelsState,
    isAccessibilityEnabled: Boolean,
    onRefreshPermissions: () -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val view = LocalView.current
    val isSmartPixelsEnabled by state.isSmartPixelsEnabled

    LaunchedEffect(Unit) {
        onRefreshPermissions()
    }

    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        var sliderValue by remember(state.smartPixelsIntensity.floatValue) {
            mutableStateOf(state.smartPixelsIntensity.floatValue)
        }

        RoundedCardContainer {
            ConfigSliderItem(
                title = stringResource(R.string.smart_pixels_intensity_title),
                value = sliderValue,
                onValueChange = { value ->
                    sliderValue = value
                },
                onValueChangeFinished = {
                    HapticUtil.performUIHaptic(view)
                    state.setSmartPixelsIntensity(sliderValue)
                },
                valueRange = 10f..90f,
                increment = 5f,
                iconRes = R.drawable.rounded_blur_linear_24,
                enabled = isSmartPixelsEnabled && isAccessibilityEnabled,
                modifier = Modifier.highlight(highlightSetting == "smart_pixels_intensity_slider"),
            )
        }

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_cast_24,
                title = stringResource(R.string.smart_pixels_disable_on_cast_title),
                description = stringResource(R.string.smart_pixels_disable_on_cast_desc),
                isChecked = state.isSmartPixelsDisableOnCastEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performUIHaptic(view)
                    state.setSmartPixelsDisableOnCastEnabled(checked)
                },
                enabled = isSmartPixelsEnabled && isAccessibilityEnabled,
                modifier = Modifier.highlight(highlightSetting == "smart_pixels_disable_on_cast_toggle"),
            )
        }
    }
}
