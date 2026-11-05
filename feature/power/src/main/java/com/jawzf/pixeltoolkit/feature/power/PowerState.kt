package com.jawzf.pixeltoolkit.feature.power

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateController
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateWakeLockService
import com.jawzf.pixeltoolkit.feature.power.maps.MapsChannel
import com.jawzf.pixeltoolkit.feature.power.maps.MapsState

/** Compose state for caffeinate and Maps power saving. */
class PowerState {
    private lateinit var settings: PowerSettings

    val isCaffeinateActive = mutableStateOf(false)
    val isMapsPowerSavingEnabled = mutableStateOf(false)
    val mapsChannels = mutableStateOf<List<MapsChannel>>(emptyList())

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        settings = PowerSettings(context)
        isMapsPowerSavingEnabled.value = settings.getBoolean(PowerSettings.KEY_MAPS_POWER_SAVING_ENABLED)
        MapsState.isEnabled = isMapsPowerSavingEnabled.value
        loadMapsChannels()
        checkCaffeinateActive(context)
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        when (key) {
            PowerSettings.KEY_MAPS_POWER_SAVING_ENABLED -> {
                isMapsPowerSavingEnabled.value = settings.getBoolean(key)
                MapsState.isEnabled = isMapsPowerSavingEnabled.value
            }

            PowerSettings.KEY_MAPS_DISCOVERED_CHANNELS, PowerSettings.KEY_MAPS_DETECTION_CHANNELS -> loadMapsChannels()

            else -> {}
        }
    }

    /**
     * Executes the set maps power saving enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setMapsPowerSavingEnabled(enabled: Boolean) {
        isMapsPowerSavingEnabled.value = enabled
        MapsState.isEnabled = enabled
        settings.putBoolean(PowerSettings.KEY_MAPS_POWER_SAVING_ENABLED, enabled)
    }

    private fun loadMapsChannels() {
        val discovered = settings.loadMapsDiscoveredChannels()
        val detectionIds = settings.loadMapsDetectionChannels()

        mapsChannels.value =
            discovered
                .map { channel ->
                    channel.copy(isEnabled = detectionIds.contains(channel.id))
                }.distinctBy { it.id }
                .sortedBy { it.name }
    }

    /**
     * Executes the set maps channel detected operation.
     *
     * @param channelId [String] Target channel id.
     * @param detected [Boolean] Target detected.
     */
    fun setMapsChannelDetected(
        channelId: String,
        detected: Boolean,
    ) {
        val currentDetected = settings.loadMapsDetectionChannels().toMutableSet()
        if (detected) {
            currentDetected.add(channelId)
        } else {
            currentDetected.remove(channelId)
        }
        settings.saveMapsDetectionChannels(currentDetected)
        loadMapsChannels()
    }

    /**
     * Executes the check caffeinate active operation.
     *
     * @param context [Context] Target context.
     */
    fun checkCaffeinateActive(context: Context) {
        isCaffeinateActive.value = isCaffeinateServiceRunning(context)
    }

    /**
     * Executes the start caffeinate operation.
     *
     * @param context [Context] Target context.
     */
    fun startCaffeinate(context: Context) {
        context.startService(Intent(context, CaffeinateWakeLockService::class.java))
        isCaffeinateActive.value = true
        CaffeinateController.isActive.value = true
    }

    /**
     * Executes the stop caffeinate operation.
     *
     * @param context [Context] Target context.
     */
    fun stopCaffeinate(context: Context) {
        CaffeinateController.cancelAll(context)
        isCaffeinateActive.value = false
    }

    private fun isCaffeinateServiceRunning(context: Context): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (CaffeinateWakeLockService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }
}
