package com.jawzf.pixeltoolkit.ui.features.system

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.utils.BiometricHelper
import com.jawzf.pixeltoolkit.utils.PermissionUIHelper
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

@Composable
fun ScreenLockedSecuritySettingsUI(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    var showPermissionSheet by remember { mutableStateOf(false) }

    val isShellGranted = ShellUtils.hasPermission(context)

    if (showPermissionSheet) {
        val permKey = if (ShellUtils.isRootEnabled(context)) "ROOT" else "SHIZUKU"
        val permissionItems = PermissionUIHelper.getPermissionItems(listOf(permKey), context, viewModel)

        if (permissionItems.isNotEmpty()) {
            PermissionsBottomSheet(
                onDismissRequest = { showPermissionSheet = false },
                featureTitle = stringResource(R.string.feat_screen_locked_security_title),
                permissions = permissionItems,
            )
        }
    }

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
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                title = stringResource(R.string.screen_locked_security_title),
                description = stringResource(R.string.screen_locked_security_desc),
                isChecked = viewModel.isScreenLockedSecurityEnabled.value,
                onCheckedChange = { isChecked ->
                    if (!isShellGranted) {
                        showPermissionSheet = true
                    } else if (context is FragmentActivity) {
                        BiometricHelper.showBiometricPrompt(
                            activity = context,
                            title = context.getString(R.string.screen_locked_security_dialog_title),
                            subtitle =
                                if (isChecked) {
                                    context.getString(R.string.screen_locked_security_auth_enable)
                                } else {
                                    context.getString(
                                        R.string.screen_locked_security_auth_disable,
                                    )
                                },
                            onSuccess = {
                                viewModel.setScreenLockedSecurityEnabled(
                                    isChecked,
                                    context,
                                )
                            },
                        )
                    } else {
                        viewModel.setScreenLockedSecurityEnabled(isChecked, context)
                    }
                },
                enabled = true,
                onDisabledClick = {
                    if (!isShellGranted) {
                        showPermissionSheet = true
                    }
                },
                iconRes = R.drawable.rounded_security_24,
                modifier = Modifier.highlight(highlightSetting == "screen_locked_security_toggle"),
            )

            IconToggleItem(
                title = stringResource(R.string.screen_locked_disable_notification_interactions_title),
                description = stringResource(R.string.screen_locked_disable_notification_interactions_desc),
                isChecked = viewModel.isDisableNotificationInteractions.value && isShellGranted,
                onCheckedChange = { isChecked ->
                    if (!isShellGranted) {
                        showPermissionSheet = true
                    } else {
                        viewModel.setDisableNotificationInteractions(isChecked, context)
                    }
                },
                onDisabledClick = {
                    if (!isShellGranted) {
                        showPermissionSheet = true
                    }
                },
                iconRes = R.drawable.outline_circle_notifications_24,
                modifier =
                    Modifier.highlight(
                        highlightSetting == "screen_locked_disable_notification_interactions",
                    ),
            )

            IconToggleItem(
                title = stringResource(R.string.screen_locked_keep_qs_on_extended_unlock_title),
                isChecked = viewModel.isScreenLockedDisableOnExtendedUnlock.value,
                onCheckedChange = { isChecked ->
                    viewModel.setScreenLockedDisableOnExtendedUnlock(isChecked)
                },
                iconRes = R.drawable.rounded_lock_24,
                modifier = Modifier.highlight(highlightSetting == "screen_locked_disable_on_extended_unlock"),
            )
        }
    }
}
