package com.jawzf.pixeltoolkit.feature.power

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.feature.power.battery.BatteryNotificationService
import com.jawzf.pixeltoolkit.feature.power.widget.BatteriesWidgetReceiver

/** Compose state for the Batteries screens. */
class BatteryState {
    private lateinit var settings: PowerSettings

    val isBluetoothDevicesEnabled = mutableStateOf(false)
    val isBatteryNotificationEnabled = mutableStateOf(false)
    val batteryWidgetMaxDevices = mutableIntStateOf(8)

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        settings = PowerSettings(context)
        isBluetoothDevicesEnabled.value = settings.isBluetoothDevicesEnabled()
        isBatteryNotificationEnabled.value = settings.isBatteryNotificationEnabled()
        batteryWidgetMaxDevices.intValue = settings.getBatteryWidgetMaxDevices()
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        when (key) {
            PowerSettings.KEY_BATTERY_WIDGET_MAX_DEVICES -> batteryWidgetMaxDevices.intValue = settings.getBatteryWidgetMaxDevices()
            else -> {}
        }
    }

    fun setBatteryNotificationEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isBatteryNotificationEnabled.value = enabled
        settings.setBatteryNotificationEnabled(enabled)
        val intent = Intent(context, BatteryNotificationService::class.java)
        if (enabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            context.stopService(intent)
        }
    }

    fun setBluetoothDevicesEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isBluetoothDevicesEnabled.value = enabled
        settings.setBluetoothDevicesEnabled(enabled)
        updateWidget(context)
    }

    fun setBatteryWidgetMaxDevices(
        count: Int,
        context: Context,
    ) {
        batteryWidgetMaxDevices.intValue = count
        settings.setBatteryWidgetMaxDevices(count)
        updateWidget(context)
    }

    // Trigger widget update to fetch data immediately
    private fun updateWidget(context: Context) {
        val intent =
            Intent(context, BatteriesWidgetReceiver::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
        context.sendBroadcast(intent)
    }
}
