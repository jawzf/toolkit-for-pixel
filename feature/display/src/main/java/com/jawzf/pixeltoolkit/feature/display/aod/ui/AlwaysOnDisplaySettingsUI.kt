package com.jawzf.pixeltoolkit.feature.display.aod.ui

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
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.feature.display.aod.AodState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlwaysOnDisplaySettingsUI(
    state: AodState,
    isAccessibilityEnabled: Boolean,
    isWriteSecureSettingsEnabled: Boolean,
    permissionItemsFor: (List<String>) -> List<PermissionItem>,
    onRefreshPermissions: () -> Unit,
    appSelectionSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val view = LocalView.current

    var showAppSelectionSheet by remember { mutableStateOf(false) }
    var requestingPermissionsFor by remember { mutableStateOf<Pair<Int, List<String>>?>(null) }

    LaunchedEffect(Unit) {
        onRefreshPermissions()
    }

    if (requestingPermissionsFor != null) {
        val (featureTitle, permKeys) = requestingPermissionsFor!!
        val permissionItems = permissionItemsFor(permKeys)
        PermissionsBottomSheet(
            onDismissRequest = {
                requestingPermissionsFor = null
                onRefreshPermissions()
            },
            featureTitle = featureTitle,
            permissions = permissionItems,
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RoundedCardContainer(
            modifier = Modifier,
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_mobile_text_2_24,
                title = stringResource(R.string.feat_always_on_display_title),
                isChecked = state.isAodEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !isWriteSecureSettingsEnabled) {
                        requestingPermissionsFor =
                            Pair(R.string.feat_always_on_display_title, listOf("WRITE_SECURE_SETTINGS"))
                    } else {
                        state.setAodEnabled(checked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "aod_toggle"),
            )
        }

        Text(
            text = stringResource(R.string.feat_notification_glance_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_notification_settings_24,
                title = stringResource(R.string.feat_notification_glance_title),
                isChecked = state.isNotificationGlanceEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.toggleNotificationGlanceEnabled(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "notification_glance_enabled"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_apps_24,
                title = stringResource(R.string.notification_glance_same_as_lighting_title),
                isChecked = state.isNotificationGlanceSameAsLightingEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setNotificationGlanceSameAsLightingEnabled(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "notification_glance_same_apps"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_power_settings_new_24,
                title = stringResource(R.string.feat_aod_force_turn_off_title),
                isChecked = state.isAodForceTurnOffEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked && !isAccessibilityEnabled) {
                        requestingPermissionsFor =
                            Pair(R.string.feat_aod_force_turn_off_title, listOf("ACCESSIBILITY"))
                    } else {
                        state.toggleAodForceTurnOffEnabled(checked)
                    }
                },
                enabled = true,
                onDisabledClick = {
                    if (!isAccessibilityEnabled) {
                        requestingPermissionsFor =
                            Pair(R.string.feat_aod_force_turn_off_title, listOf("ACCESSIBILITY"))
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "aod_force_turn_off"),
            )
        }

        Text(
            text = stringResource(R.string.notification_glance_desc),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.feat_aod_force_turn_off_desc),
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (!state.isNotificationGlanceSameAsLightingEnabled.value) {
            Button(
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    showAppSelectionSheet = true
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.isNotificationGlanceEnabled.value,
            ) {
                Text(stringResource(R.string.action_select_apps))
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        if (showAppSelectionSheet) {
            appSelectionSheet { showAppSelectionSheet = false }
        }
    }
}
