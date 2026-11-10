package com.jawzf.pixeltoolkit.feature.apps.applock.ui

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.ConfigPickerItem
import com.jawzf.pixeltoolkit.core.design.cards.FeatureCard
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.SegmentedDropdownMenuItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.platform.auth.BiometricHelper
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.feature.apps.applock.AppLockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsUI(
    state: AppLockState,
    isUseUsageAccess: Boolean,
    isAccessibilityEnabled: Boolean,
    isUsageStatsPermissionGranted: Boolean,
    onPermissionsDismissed: () -> Unit,
    appPicker: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightKey: String? = null,
) {
    val context = LocalContext.current
    var isAppSelectionSheetOpen by remember { mutableStateOf(false) }
    var showPermissionSheet by remember { mutableStateOf(false) }

    val isAppLockEnabled by state.isAppLockEnabled

    val canEnableAppLock =
        if (isUseUsageAccess) isUsageStatsPermissionGranted && isAccessibilityEnabled else isAccessibilityEnabled

    val delayLabels =
        listOf(
            stringResource(R.string.app_lock_auto_lock_delay_none),
            stringResource(R.string.app_lock_auto_lock_delay_1min),
            stringResource(R.string.app_lock_auto_lock_delay_5min),
            stringResource(R.string.app_lock_auto_lock_delay_10min),
            stringResource(R.string.app_lock_auto_lock_delay_20min),
            stringResource(R.string.app_lock_auto_lock_delay_30min),
        )

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_section_security),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer(
            modifier = Modifier,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_shield_lock_24,
                title = stringResource(R.string.app_lock_enable_title),
                isChecked = isAppLockEnabled,
                onCheckedChange = { enabled ->
                    if (context is FragmentActivity) {
                        BiometricHelper.showBiometricPrompt(
                            activity = context,
                            title = context.getString(R.string.app_lock_auth_title),
                            subtitle =
                                if (enabled) {
                                    context.getString(R.string.app_lock_enable_auth_subtitle)
                                } else {
                                    context.getString(
                                        R.string.app_lock_disable_auth_subtitle,
                                    )
                                },
                            onSuccess = { state.setAppLockEnabled(enabled, context) },
                        )
                    } else {
                        state.setAppLockEnabled(enabled, context)
                    }
                },
                enabled = canEnableAppLock,
                onDisabledClick = { showPermissionSheet = true },
                modifier = Modifier.highlight(highlightKey == "app_lock_enabled"),
            )

            FeatureCard(
                title = stringResource(R.string.app_lock_select_apps_title),
                description = stringResource(R.string.app_lock_select_apps_desc),
                iconRes = R.drawable.rounded_app_registration_24,
                isEnabled = isAppLockEnabled,
                showToggle = false,
                hasMoreSettings = true,
                onToggle = {},
                onClick = { isAppSelectionSheetOpen = true },
                modifier = Modifier.highlight(highlightKey == "app_lock_selected_apps"),
            )

            ConfigPickerItem(
                title = stringResource(R.string.app_lock_auto_lock_delay_title),
                description = stringResource(R.string.app_lock_auto_lock_delay_desc),
                iconRes = R.drawable.rounded_lock_clock_24,
                isEnabled = isAppLockEnabled,
                selectedValue = delayLabels[state.appLockAutoLockDelayIndex.intValue],
                modifier = Modifier.highlight(highlightKey == "app_lock_auto_lock_delay"),
            ) {
                delayLabels.forEachIndexed { index, label ->
                    SegmentedDropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            state.setAppLockAutoLockDelayIndex(index)
                        },
                    )
                }
            }
        }

        Text(
            text = stringResource(R.string.app_lock_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.app_lock_warning),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            text = stringResource(R.string.app_lock_biometric_note),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (isAppSelectionSheetOpen) {
            appPicker { isAppSelectionSheetOpen = false }
        }

        if (showPermissionSheet) {
            val accessibilityItem =
                PermissionItem(
                    iconRes = R.drawable.rounded_settings_accessibility_24,
                    title = R.string.perm_accessibility_title,
                    description = R.string.perm_accessibility_desc_common,
                    dependentFeatures = listOf(R.string.feat_app_lock_title),
                    actionLabel = if (isAccessibilityEnabled) R.string.perm_action_granted else R.string.perm_action_grant,
                    action = { PermissionUtils.openAccessibilitySettings(context) },
                    isGranted = isAccessibilityEnabled,
                )

            val permissionsList =
                if (isUseUsageAccess) {
                    val usageStatsItem =
                        PermissionItem(
                            iconRes = R.drawable.rounded_data_usage_24,
                            title = R.string.perm_usage_stats_title,
                            description = R.string.perm_usage_stats_desc_app_lock,
                            dependentFeatures = listOf(R.string.feat_app_lock_title),
                            actionLabel = if (isUsageStatsPermissionGranted) R.string.perm_action_granted else R.string.perm_action_grant,
                            action = { PermissionUtils.openUsageStatsSettings(context) },
                            isGranted = isUsageStatsPermissionGranted,
                        )
                    listOf(accessibilityItem, usageStatsItem)
                } else {
                    listOf(accessibilityItem)
                }

            PermissionsBottomSheet(
                onDismissRequest = {
                    showPermissionSheet = false
                    onPermissionsDismissed()
                },
                featureTitle = R.string.feat_app_lock_title,
                permissions = permissionsList,
            )
        }
    }
}
