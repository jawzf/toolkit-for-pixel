package com.jawzf.pixeltoolkit.feature.notifications.flashlight.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.feature.notifications.flashlight.FlashlightPulseState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FlashlightPulseSettingsUI(
    state: FlashlightPulseState,
    isNotificationListenerEnabled: Boolean,
    permissionItems: List<PermissionItem>,
    onPreview: () -> Unit,
    appSelectionSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val view = LocalView.current

    var showAppSelectionSheet by remember { mutableStateOf(false) }
    var showPermissionsSheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.settings_section_flashlight_pulse),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_flashlight_on_24,
                title = stringResource(R.string.flashlight_pulse_title),
                isChecked = state.isFlashlightPulseEnabled.value,
                enabled = isNotificationListenerEnabled,
                onDisabledClick = { showPermissionsSheet = true },
                onCheckedChange = { checked ->
                    state.setFlashlightPulseEnabled(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "flashlight_pulse" || highlightSetting == "flashlight_pulse_enabled"),
            )
            if (state.isFlashlightPulseEnabled.value) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_mobile_text_24,
                    title = stringResource(R.string.flashlight_pulse_facedown_title),
                    isChecked = state.isFlashlightPulseFacedownOnly.value,
                    onCheckedChange = { checked ->
                        state.setFlashlightPulseFacedownOnly(checked)
                    },
                    modifier =
                        Modifier.highlight(
                            highlightSetting == "flashlight_pulse_facedown" || highlightSetting == "flashlight_pulse_facedown_only",
                        ),
                )

                IconToggleItem(
                    iconRes = R.drawable.rounded_do_not_disturb_on_24,
                    title = stringResource(R.string.flashlight_pulse_disable_on_dnd_title),
                    isChecked = state.isFlashlightPulseDisableOnDnd.value,
                    onCheckedChange = { checked ->
                        state.setFlashlightPulseDisableOnDnd(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "flashlight_pulse_disable_on_dnd"),
                )

                ConfigSliderItem(
                    title = stringResource(R.string.flashlight_pulse_max_brightness),
                    value = state.flashlightPulseMaxIntensity.floatValue,
                    onValueChange = { state.setFlashlightPulseMaxIntensity(it) },
                    valueRange = 0.01f..1f,
                    valueFormatter = { "${Math.round(it * 100)}%" },
                    increment = 0.01f,
                    modifier = Modifier.highlight(highlightSetting == "flashlight_pulse_max_intensity"),
                )
            }
            IconToggleItem(
                iconRes = R.drawable.rounded_apps_24,
                title = stringResource(R.string.flashlight_pulse_same_as_lighting_title),
                isChecked = state.isFlashlightPulseUseLightingApps.value,
                onCheckedChange = { checked ->
                    state.setFlashlightPulseUseLightingApps(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "flashlight_pulse_same_apps"),
            )
        }

        // App Selection Sheet Button
        Button(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                showAppSelectionSheet = true
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = state.isFlashlightPulseEnabled.value && !state.isFlashlightPulseUseLightingApps.value,
        ) {
            Text(stringResource(R.string.action_select_apps))
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.isFlashlightPulseEnabled.value) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    onPreview()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.flashlight_pulse_preview))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        if (showAppSelectionSheet) {
            appSelectionSheet { showAppSelectionSheet = false }
        }

        if (showPermissionsSheet) {
            PermissionsBottomSheet(
                onDismissRequest = { showPermissionsSheet = false },
                featureTitle = R.string.flashlight_pulse_title,
                permissions = permissionItems,
            )
        }
    }
}
