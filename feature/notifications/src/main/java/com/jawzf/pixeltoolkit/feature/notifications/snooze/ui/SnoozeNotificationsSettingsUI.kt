package com.jawzf.pixeltoolkit.feature.notifications.snooze.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.feature.notifications.NotificationState

@Composable
fun SnoozeNotificationsSettingsUI(
    state: NotificationState,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val isMasterEnabled by state.isSnoozeHeadsUpEnabled

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_snooze_24,
                title = stringResource(R.string.feat_snooze_notifications_title),
                isChecked = isMasterEnabled,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    state.setSnoozeHeadsUpEnabled(checked, context)
                },
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        RoundedCardContainer(
            modifier = Modifier,
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            state.snoozeChannels.value.forEach { channel ->
                IconToggleItem(
                    iconRes =
                        when (channel.id) {
                            "DEVELOPER_OPTIONS" -> R.drawable.rounded_adb_24
                            "USB_CONNECTION" -> R.drawable.rounded_usb_24
                            "BATTERY" -> R.drawable.rounded_charger_24
                            else -> R.drawable.rounded_notification_settings_24
                        },
                    title = channel.name,
                    isChecked = channel.isBlocked,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setSnoozeChannelBlocked(channel.id, checked, context)
                    },
                    enabled = isMasterEnabled,
                    modifier = Modifier.highlight(highlightSetting == channel.id),
                )
            }

            if (state.snoozeChannels.value.isEmpty()) {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.snooze_no_channels_discovered),
                    modifier = Modifier.padding(16.dp),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
