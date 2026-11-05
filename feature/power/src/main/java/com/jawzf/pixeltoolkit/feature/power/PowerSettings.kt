package com.jawzf.pixeltoolkit.feature.power

import android.content.Context
import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.power.bluetooth.BluetoothBatteryUtils

class PowerSettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    /**
     * Executes the save bluetooth devices battery operation.
     *
     * @param devices [List<BluetoothBatteryUtils.BluetoothDeviceBattery>] Target devices.
     */
    fun saveBluetoothDevicesBattery(devices: List<BluetoothBatteryUtils.BluetoothDeviceBattery>) {
        val json = gson.toJson(devices)
        putString(KEY_BLUETOOTH_DEVICES_BATTERY, json)
    }

    /**
     * Executes the is bluetooth devices enabled operation.
     * @return The resulting Boolean data.
     */
    fun isBatteryShowPercentageEnabled(): Boolean = getBoolean(KEY_BATTERY_SHOW_PERCENTAGE, false)

    fun setBatteryShowPercentageEnabled(enabled: Boolean) = putBoolean(KEY_BATTERY_SHOW_PERCENTAGE, enabled)

    fun isBatterySeparateChannelsEnabled(): Boolean = getBoolean(KEY_BATTERY_SEPARATE_CHANNELS, false)

    fun setBatterySeparateChannelsEnabled(enabled: Boolean) = putBoolean(KEY_BATTERY_SEPARATE_CHANNELS, enabled)

    fun isBluetoothDevicesEnabled(): Boolean = getBoolean(KEY_SHOW_BLUETOOTH_DEVICES, false)

    /**
     * Executes the set bluetooth devices enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setBluetoothDevicesEnabled(enabled: Boolean) = putBoolean(KEY_SHOW_BLUETOOTH_DEVICES, enabled)

    /**
     * Executes the get battery widget max devices operation.
     * @return The resulting Int data.
     */
    fun getBatteryWidgetMaxDevices(): Int = getInt(KEY_BATTERY_WIDGET_MAX_DEVICES, 8)

    /**
     * Executes the set battery widget max devices operation.
     *
     * @param count [Int] Target count.
     */
    fun setBatteryWidgetMaxDevices(count: Int) = putInt(KEY_BATTERY_WIDGET_MAX_DEVICES, count)

    /**
     * Executes the is battery widget background enabled operation.
     * @return The resulting Boolean data.
     */
    fun isBatteryWidgetBackgroundEnabled(): Boolean = getBoolean(KEY_BATTERY_WIDGET_BACKGROUND_ENABLED, true)

    /**
     * Executes the set battery widget background enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setBatteryWidgetBackgroundEnabled(enabled: Boolean) = putBoolean(KEY_BATTERY_WIDGET_BACKGROUND_ENABLED, enabled)

    /**
     * Executes the is battery notification enabled operation.
     * @return The resulting Boolean data.
     */
    fun isBatteryNotificationEnabled(): Boolean = getBoolean(KEY_BATTERY_NOTIFICATION_ENABLED, false)

    /**
     * Executes the set battery notification enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setBatteryNotificationEnabled(enabled: Boolean) = putBoolean(KEY_BATTERY_NOTIFICATION_ENABLED, enabled)

    companion object {
        const val KEY_BLUETOOTH_DEVICES_BATTERY = "bluetooth_devices_battery"
        const val KEY_SHOW_BLUETOOTH_DEVICES = "show_bluetooth_devices"
        const val KEY_BATTERY_SEPARATE_CHANNELS = "battery_separate_channels"
        const val KEY_BATTERY_SHOW_PERCENTAGE = "battery_show_percentage"
        const val KEY_BATTERY_WIDGET_CENTER_ITEMS = "battery_widget_center_items"
        const val KEY_BATTERY_WIDGET_MAX_DEVICES = "battery_widget_max_devices"
        const val KEY_BATTERY_WIDGET_BACKGROUND_ENABLED = "battery_widget_background_enabled"
        const val KEY_BATTERY_NOTIFICATION_ENABLED = "battery_notification_enabled"
    }
}
