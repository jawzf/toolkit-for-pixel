package com.jawzf.pixeltoolkit.feature.display.statusbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.jawzf.pixeltoolkit.core.platform.shell.RootUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.display.statusbar.StatusBarIconViewModel

class StatusBarAutomationReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_POWER_CONNECTED ||
            action == Intent.ACTION_POWER_DISCONNECTED ||
            action == Intent.ACTION_BOOT_COMPLETED
        ) {
            updateBatteryPercentage(context, action)
        }
    }

    private fun updateBatteryPercentage(
        context: Context,
        action: String,
    ) {
        val prefs = context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean(SettingsRepository.KEY_STATUS_BAR_ICON_CONTROL_ENABLED, false)) return
        if (!prefs.contains(StatusBarIconViewModel.PREF_BATTERY_PERCENT_MODE)) return
        val mode = prefs.getInt(StatusBarIconViewModel.PREF_BATTERY_PERCENT_MODE, 0)
        val isCharging = action == Intent.ACTION_POWER_CONNECTED

        if (mode == 2) { // Charging only
            val value = if (isCharging) 1 else 0
            updateSystemSetting(context, value)
        } else if (action == Intent.ACTION_BOOT_COMPLETED) {
            val value = if (mode == 1) 1 else 0
            updateSystemSetting(context, value)
        }
    }

    private fun updateSystemSetting(
        context: Context,
        value: Int,
    ) {
        val key = "status_bar_show_battery_percent"
        var success = false
        try {
            success = Settings.System.putInt(context.contentResolver, key, value)
        } catch (e: Exception) {
            try {
                success = Settings.Secure.putInt(context.contentResolver, key, value)
            } catch (e2: Exception) {
            }
        }

        // Get current value safely
        val currentValue =
            try {
                Settings.System.getInt(context.contentResolver, key, -1)
            } catch (e: Exception) {
                -1
            }

        // Background Shizuku/Root fallback
        if (!success || currentValue != value) {
            if (ShizukuUtils
                    .hasPermission()
            ) {
                ShizukuUtils
                    .runCommand("settings put system $key $value")
                ShizukuUtils
                    .runCommand("settings put secure $key $value")
            } else if (RootUtils
                    .isRootPermissionGranted()
            ) {
                RootUtils
                    .runCommand("settings put system $key $value")
                RootUtils
                    .runCommand("settings put secure $key $value")
            }
        }
    }
}
