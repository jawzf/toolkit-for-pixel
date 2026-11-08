package com.jawzf.pixeltoolkit.core.data.power

import android.content.Context
import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.data.power.BluetoothBatteryUtils
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.power.maps.MapsChannel

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

    // Maps Channels Helper
    fun loadMapsDiscoveredChannels(): List<MapsChannel> {
        val json = prefs.getString(KEY_MAPS_DISCOVERED_CHANNELS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<MapsChannel>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Executes the save maps discovered channels operation.
     *
     * @param channels [List<MapsChannel>] Target channels.
     */
    fun saveMapsDiscoveredChannels(channels: List<MapsChannel>) {
        val json = gson.toJson(channels)
        putString(KEY_MAPS_DISCOVERED_CHANNELS, json)
    }

    /**
     * Executes the load maps detection channels operation.
     * @return The resulting Set<String> data.
     */
    fun loadMapsDetectionChannels(): Set<String> {
        val json = prefs.getString(KEY_MAPS_DETECTION_CHANNELS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            // Default to navigation related channel IDs if none are selected yet
            setOf(
                "navigation_notification_channel",
                "primary_navigation_channel_v1",
                "primary_navigation_channel_v2",
            )
        }
    }

    /**
     * Executes the save maps detection channels operation.
     *
     * @param channels [Set<String>] Target channels.
     */
    fun saveMapsDetectionChannels(channels: Set<String>) {
        val json = gson.toJson(channels)
        putString(KEY_MAPS_DETECTION_CHANNELS, json)
    }

    fun isPowerSavingApplyOnBootEnabled(): Boolean = getBoolean(KEY_POWER_SAVING_APPLY_ON_BOOT, false)

    fun setPowerSavingApplyOnBootEnabled(enabled: Boolean) = putBoolean(KEY_POWER_SAVING_APPLY_ON_BOOT, enabled)

    companion object {
        const val KEY_BLUETOOTH_DEVICES_BATTERY = "bluetooth_devices_battery"
        const val KEY_SHOW_BLUETOOTH_DEVICES = "show_bluetooth_devices"
        const val KEY_BATTERY_SEPARATE_CHANNELS = "battery_separate_channels"
        const val KEY_BATTERY_SHOW_PERCENTAGE = "battery_show_percentage"
        const val KEY_BATTERY_WIDGET_CENTER_ITEMS = "battery_widget_center_items"
        const val KEY_BATTERY_WIDGET_MAX_DEVICES = "battery_widget_max_devices"
        const val KEY_BATTERY_WIDGET_BACKGROUND_ENABLED = "battery_widget_background_enabled"
        const val KEY_BATTERY_NOTIFICATION_ENABLED = "battery_notification_enabled"
        const val KEY_MAPS_POWER_SAVING_ENABLED = "maps_power_saving_enabled"
        const val KEY_MAPS_DISCOVERED_CHANNELS = "maps_discovered_channels"
        const val KEY_MAPS_DETECTION_CHANNELS = "maps_detection_channels"
        const val KEY_POWER_SAVING_APPLY_ON_BOOT = "power_saving_apply_on_boot"
    }
}
