package com.jawzf.pixeltoolkit.feature.power

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.power.CaffeinateController
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateWakeLockService
import com.jawzf.pixeltoolkit.feature.power.maps.MapsChannel
import com.jawzf.pixeltoolkit.feature.power.maps.MapsState
import com.jawzf.pixeltoolkit.feature.power.standby.AppStandbyInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Compose state for caffeinate, Maps power saving, battery saver and standby apps. */
class PowerState(
    private val scope: CoroutineScope,
) {
    private lateinit var settings: PowerSettings

    val isCaffeinateActive = mutableStateOf(false)
    val isMapsPowerSavingEnabled = mutableStateOf(false)
    val mapsChannels = mutableStateOf<List<MapsChannel>>(emptyList())
    val batterySaverConstants = mutableStateOf<Map<String, String>>(emptyMap())
    val lowPowerTriggerLevel = mutableIntStateOf(0)
    val standbyAppsList = mutableStateOf<List<AppStandbyInfo>>(emptyList())
    val isStandbyAppsLoading = mutableStateOf(false)

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

    /**
     * Executes the load battery saver constants operation.
     *
     * @param context [Context] Target context.
     */
    fun loadBatterySaverConstants(context: Context) {
        val constantsStr =
            Settings.Global.getString(context.contentResolver, "battery_saver_constants") ?: ""
        val map = mutableMapOf<String, String>()
        if (constantsStr.isNotEmpty()) {
            constantsStr.split(",").forEach { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) {
                    map[parts[0].trim()] = parts[1].trim()
                }
            }
        }
        batterySaverConstants.value = map
    }

    /**
     * Executes the update battery saver constant operation.
     *
     * @param context [Context] Target context.
     * @param key [String] Target key.
     * @param value [String] Target value.
     */
    fun updateBatterySaverConstant(
        context: Context,
        key: String,
        value: String,
    ) {
        val currentMap = batterySaverConstants.value.toMutableMap()
        currentMap[key] = value
        saveBatterySaverConstants(context, currentMap)
    }

    /**
     * Executes the reset battery saver constants operation.
     *
     * @param context [Context] Target context.
     */
    fun resetBatterySaverConstants(context: Context) {
        try {
            Settings.Global.putString(context.contentResolver, "battery_saver_constants", null)
            batterySaverConstants.value = emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveBatterySaverConstants(
        context: Context,
        map: Map<String, String>,
    ) {
        val constantsStr = map.map { "${it.key}=${it.value}" }.joinToString(",")
        try {
            Settings.Global.putString(
                context.contentResolver,
                "battery_saver_constants",
                constantsStr,
            )
            batterySaverConstants.value = map
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the sync low power trigger level operation.
     *
     * @param context [Context] Target context.
     */
    fun syncLowPowerTriggerLevel(context: Context) {
        val level = Settings.Global.getInt(context.contentResolver, "low_power_trigger_level", 0)
        lowPowerTriggerLevel.intValue = level
    }

    /**
     * Executes the set low power trigger level operation.
     *
     * @param context [Context] Target context.
     * @param level [Int] Target level.
     */
    fun setLowPowerTriggerLevel(
        context: Context,
        level: Int,
    ) {
        try {
            Settings.Global.putInt(context.contentResolver, "low_power_trigger_level", level)
            lowPowerTriggerLevel.intValue = level
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the load standby apps operation.
     *
     * @param context [Context] Target context.
     */
    fun loadStandbyApps(context: Context) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isStandbyAppsLoading.value = true
            val pm = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN).apply { addCategory(Intent.CATEGORY_LAUNCHER) }
            val apps = pm.queryIntentActivities(intent, 0)

            val appList =
                apps
                    .mapNotNull { resolveInfo ->
                        val pkg = resolveInfo.activityInfo.packageName
                        if (pkg == context.packageName) return@mapNotNull null

                        val label = resolveInfo.loadLabel(pm).toString()
                        val icon = resolveInfo.loadIcon(pm)

                        var bucket = 10
                        val output = ShellUtils.runCommandWithOutput(context, "am get-standby-bucket $pkg")
                        if (output != null) {
                            bucket = standbyBucketOf(output.lowercase().trim())
                        }

                        AppStandbyInfo(pkg, label, icon, bucket)
                    }.sortedBy { it.label.lowercase() }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                standbyAppsList.value = appList
                isStandbyAppsLoading.value = false
            }
        }
    }

    /**
     * Executes the set app standby bucket operation.
     *
     * @param packageName [String] Target package name.
     * @param targetBucket [Int] Target target bucket.
     * @param context [Context] Target context.
     */
    fun setAppStandbyBucket(
        packageName: String,
        targetBucket: Int,
        context: Context,
    ) {
        val currentList = standbyAppsList.value
        val updatedList =
            currentList.map { app ->
                if (app.packageName == packageName) {
                    app.copy(bucket = targetBucket)
                } else {
                    app
                }
            }
        standbyAppsList.value = updatedList

        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val bucketName =
                when (targetBucket) {
                    10 -> "active"
                    20 -> "working_set"
                    30 -> "frequent"
                    40 -> "rare"
                    45 -> "restricted"
                    else -> "active"
                }
            ShellUtils.runCommand(context, "am set-standby-bucket $packageName $bucketName")
        }
    }

    /**
     * Executes batch set app standby bucket operation for multiple apps.
     *
     * @param packageNames [Set<String>] Target package names.
     * @param targetBucket [Int] Target standby bucket code.
     * @param context [Context] Context for shell execution.
     */
    fun setAppsStandbyBucket(
        packageNames: Set<String>,
        targetBucket: Int,
        context: Context,
    ) {
        val currentList = standbyAppsList.value
        val updatedList =
            currentList.map { app ->
                if (packageNames.contains(app.packageName)) {
                    app.copy(bucket = targetBucket)
                } else {
                    app
                }
            }
        standbyAppsList.value = updatedList

        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val bucketName =
                when (targetBucket) {
                    10 -> "active"
                    20 -> "working_set"
                    30 -> "frequent"
                    40 -> "rare"
                    45 -> "restricted"
                    else -> "active"
                }
            packageNames.forEach { pkg ->
                ShellUtils.runCommand(context, "am set-standby-bucket $pkg $bucketName")
            }
        }
    }

    private fun standbyBucketOf(text: String): Int =
        when {
            text.contains("restricted") || text.contains("45") -> 45
            text.contains("rare") || text.contains("40") -> 40
            text.contains("frequent") || text.contains("30") -> 30
            text.contains("working") || text.contains("20") -> 20
            text.contains("active") || text.contains("10") -> 10
            else -> 10
        }
}
