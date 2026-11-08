package com.jawzf.pixeltoolkit.feature.power.battery.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.feature.power.BatteryState

@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BatteriesSettingsUI(
    state: BatteryState,
    isPostNotificationsEnabled: MutableState<Boolean>,
    isBluetoothPermissionGranted: MutableState<Boolean>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val view = LocalView.current

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val notificationPermission =
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (granted) {
                    isPostNotificationsEnabled.value = true
                    state.setBatteryNotificationEnabled(true, context)
                }
            }

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_battery_charging_60_24,
                title = stringResource(R.string.batteries_notification_title),
                isChecked = state.isBatteryNotificationEnabled.value,
                onCheckedChange = { enabled ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (enabled &&
                        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                        !isPostNotificationsEnabled.value
                    ) {
                        notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        state.setBatteryNotificationEnabled(enabled, context)
                    }
                },
            )

            var showPercentage by remember {
                mutableStateOf(
                    PowerSettings(context)
                        .isBatteryShowPercentageEnabled(),
                )
            }
            IconToggleItem(
                iconRes = R.drawable.rounded_percent_24,
                title = stringResource(R.string.battery_show_percentage_title),
                isChecked = showPercentage,
                onCheckedChange = { enabled ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    showPercentage = enabled
                    PowerSettings(context)
                        .setBatteryShowPercentageEnabled(enabled)
                    BatteriesPreviewTrigger.bump()
                    state.setBluetoothDevicesEnabled(state.isBluetoothDevicesEnabled.value, context)
                },
            )
        }

        RoundedCardContainer {
            // Bluetooth Devices
            val isBluetoothEnabled = state.isBluetoothDevicesEnabled.value
            val isPermissionGranted = isBluetoothPermissionGranted.value

            val launcher =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                ) { permissions ->
                    val allGranted = permissions.values.all { it }
                    if (allGranted) {
                        isBluetoothPermissionGranted.value = true
                        state.setBluetoothDevicesEnabled(true, context)
                    }
                }

            IconToggleItem(
                iconRes = R.drawable.rounded_bluetooth_24,
                title = stringResource(R.string.show_bluetooth_devices),
                isChecked = isBluetoothEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        if (isPermissionGranted) {
                            state.setBluetoothDevicesEnabled(true, context)
                        } else {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                                launcher.launch(
                                    arrayOf(
                                        android.Manifest.permission.BLUETOOTH_CONNECT,
                                        android.Manifest.permission.BLUETOOTH_SCAN,
                                    ),
                                )
                            } else {
                                state.setBluetoothDevicesEnabled(true, context)
                            }
                        }
                    } else {
                        state.setBluetoothDevicesEnabled(false, context)
                    }
                },
            )

            if (isBluetoothEnabled && isPermissionGranted) {
                var separateChannels by remember {
                    mutableStateOf(
                        PowerSettings(context)
                            .isBatterySeparateChannelsEnabled(),
                    )
                }
                IconToggleItem(
                    iconRes = R.drawable.rounded_earbuds_2_24,
                    title = stringResource(R.string.battery_separate_channels_title),
                    description = stringResource(R.string.battery_separate_channels_desc),
                    isChecked = separateChannels,
                    onCheckedChange = { enabled ->
                        separateChannels = enabled
                        PowerSettings(context)
                            .setBatterySeparateChannelsEnabled(enabled)
                        if (enabled && !ShellUtils.hasPermission(context)) {
                            if (ShizukuUtils.isShizukuAvailable()) {
                                ShizukuUtils.requestPermission()
                            } else {
                                android.widget.Toast
                                    .makeText(context, R.string.shizuku_not_running_desc, android.widget.Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                        state.setBluetoothDevicesEnabled(true, context)
                        BatteriesPreviewTrigger.bump()
                    },
                )
            }
        }

        run {
            Text(
                text = stringResource(R.string.bt_device_icons_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, bottom = 0.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            BluetoothDeviceIconsSection(state.isBluetoothDevicesEnabled.value && isBluetoothPermissionGranted.value)
        }

        // Max Devices Slider
        Text(
            text = stringResource(R.string.limit_max_devices),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, bottom = 0.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            ListItem(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                contentPadding =
                    PaddingValues(
                        horizontal = 16.dp,
                        vertical = 16.dp,
                    ),
                colors =
                    ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                    ),
                content = {
                    Text(
                        text = stringResource(R.string.limit_max_devices_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                supportingContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Slider(
                            value = state.batteryWidgetMaxDevices.intValue.toFloat(),
                            onValueChange = {
                                val newInt = it.toInt()
                                if (newInt != state.batteryWidgetMaxDevices.intValue) {
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    state.setBatteryWidgetMaxDevices(newInt, context)
                                }
                            },
                            valueRange = 1f..16f,
                            steps = 14,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = state.batteryWidgetMaxDevices.intValue.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                },
            )
        }
    }
}
