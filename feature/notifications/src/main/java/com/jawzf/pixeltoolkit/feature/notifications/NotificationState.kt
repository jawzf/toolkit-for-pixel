package com.jawzf.pixeltoolkit.feature.notifications

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissAppConfig
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissChannel
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissStat
import com.jawzf.pixeltoolkit.feature.notifications.snooze.SnoozeChannel

/** Compose state and actions for notification auto-dismiss and snoozing. */
class NotificationState(
    /** Called after a change the running notification listener must pick up. */
    private val onAutoDismissChanged: () -> Unit,
) {
    private lateinit var settings: NotificationSettings
    private var appContext: Context? = null

    val snoozeChannels =
        mutableStateOf<List<SnoozeChannel>>(emptyList())
    val isSnoozeHeadsUpEnabled = mutableStateOf(false)
    val isAutoDismissEnabled = mutableStateOf(false)

    val autoDismissConfigs =
        mutableStateOf<List<AutoDismissAppConfig>>(emptyList())
    val autoDismissChannels =
        mutableStateOf<List<AutoDismissChannel>>(emptyList())
    val autoDismissStats =
        mutableStateOf<Map<String, AutoDismissStat>>(emptyMap())

    // Notification Snooze
    val isShowNotificationSnoozeEnabled = mutableStateOf(false)
    val notificationSnoozeDefault = mutableIntStateOf(60)
    val notificationSnoozeOptions = mutableStateOf<List<Int>>(listOf(15, 30, 60, 120))

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        appContext = context.applicationContext
        settings = NotificationSettings(context)
        loadSnoozeChannels(context)
        isSnoozeHeadsUpEnabled.value = settings.getBoolean(NotificationSettings.KEY_SNOOZE_HEADS_UP_ENABLED)
        isAutoDismissEnabled.value = settings.isAutoDismissEnabled()
        loadAutoDismissConfigs()
        syncShowNotificationSnooze(context)
        loadNotificationSnoozeOptions(context)
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        when (key) {
            NotificationSettings.KEY_SNOOZE_DISCOVERED_CHANNELS,
            NotificationSettings.KEY_SNOOZE_BLOCKED_CHANNELS,
            -> {
                appContext?.let { loadSnoozeChannels(it) }
            }

            NotificationSettings.KEY_SNOOZE_HEADS_UP_ENABLED -> {
                isSnoozeHeadsUpEnabled.value = settings.getBoolean(key)
            }

            NotificationSettings.KEY_AUTO_DISMISS_ENABLED -> {
                isAutoDismissEnabled.value = settings.isAutoDismissEnabled()
            }

            NotificationSettings.KEY_AUTO_DISMISS_APPS -> {
                loadAutoDismissConfigs()
            }

            NotificationSettings.KEY_AUTO_DISMISS_CHANNELS -> {
                loadAutoDismissChannels()
            }

            NotificationSettings.KEY_AUTO_DISMISS_STATS -> {
                loadAutoDismissStats()
            }

            else -> {}
        }
    }

    fun loadAutoDismissConfigs() {
        autoDismissConfigs.value = settings.loadAutoDismissConfigs()
        loadAutoDismissChannels()
        loadAutoDismissStats()
    }

    fun loadAutoDismissChannels() {
        autoDismissChannels.value = settings.loadAutoDismissChannels()
    }

    fun loadAutoDismissStats() {
        autoDismissStats.value = settings.loadAutoDismissStats()
    }

    fun setAutoDismissEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isAutoDismissEnabled.value = enabled
        settings.setAutoDismissEnabled(enabled)
        onAutoDismissChanged()
    }

    fun saveAutoDismissSelectedApps(
        context: Context,
        apps: List<AppSelection>,
    ) {
        val currentConfigs = settings.loadAutoDismissConfigs().associateBy { it.packageName }
        val lastMinutes = settings.getAutoDismissLastMinutes()
        val enabledApps = apps.filter { it.isEnabled }
        val newConfigs =
            enabledApps.map {
                currentConfigs[it.packageName] ?: AutoDismissAppConfig(
                    packageName = it.packageName,
                    minutes = lastMinutes,
                )
            }
        settings.saveAutoDismissConfigs(newConfigs)

        val newPackageNames = newConfigs.map { it.packageName }.toSet()
        val currentStats = settings.loadAutoDismissStats().toMutableMap()
        for (pkg in newPackageNames) {
            if (pkg !in currentConfigs) {
                currentStats[pkg] = AutoDismissStat(0, System.currentTimeMillis())
            }
        }
        currentStats.keys.retainAll(newPackageNames)
        settings.saveAutoDismissStats(currentStats)

        loadAutoDismissConfigs()
        onAutoDismissChanged()
    }

    fun setAutoDismissMinutes(
        packageName: String,
        minutes: Int,
    ) {
        settings.setAutoDismissLastMinutes(minutes)
        val current = autoDismissConfigs.value.toMutableList()
        val index = current.indexOfFirst { it.packageName == packageName }
        if (index >= 0) {
            current[index] = current[index].copy(minutes = minutes)
        } else {
            current.add(AutoDismissAppConfig(packageName, minutes))
        }
        settings.saveAutoDismissConfigs(current)
        loadAutoDismissConfigs()
        onAutoDismissChanged()
    }

    fun removeAutoDismissApp(packageName: String) {
        val current = autoDismissConfigs.value.toMutableList()
        current.removeAll { it.packageName == packageName }
        settings.saveAutoDismissConfigs(current)

        val currentStats = settings.loadAutoDismissStats().toMutableMap()
        currentStats.remove(packageName)
        settings.saveAutoDismissStats(currentStats)

        loadAutoDismissConfigs()
        onAutoDismissChanged()
    }

    fun setAutoDismissChannelIncluded(
        packageName: String,
        channelId: String,
        included: Boolean,
    ) {
        val current = autoDismissConfigs.value.toMutableList()
        val index = current.indexOfFirst { it.packageName == packageName }
        if (index >= 0) {
            val config = current[index]
            val excluded = config.excludedChannels.orEmpty().toMutableList()
            if (included) {
                excluded.remove(channelId)
            } else {
                if (channelId !in excluded) {
                    excluded.add(channelId)
                }
            }
            current[index] = config.copy(excludedChannels = excluded)
            settings.saveAutoDismissConfigs(current)
            loadAutoDismissConfigs()
            onAutoDismissChanged()
        }
    }

    /**
     * Executes the load snooze channels operation.
     *
     * @param context [Context] Target context.
     */
    fun loadSnoozeChannels(context: Context) {
        val discovered = settings.loadSnoozeDiscoveredChannels()
        val blocked = settings.loadSnoozeBlockedChannels()

        val channels =
            discovered.map { channel ->
                channel.copy(isBlocked = blocked.contains(channel.id))
            }

        snoozeChannels.value = channels.distinctBy { it.id }.sortedBy { it.name }
    }

    /**
     * Executes the set snooze channel blocked operation.
     *
     * @param channelId [String] Target channel id.
     * @param blocked [Boolean] Target blocked.
     * @param context [Context] Target context.
     */
    fun setSnoozeChannelBlocked(
        channelId: String,
        blocked: Boolean,
        context: Context,
    ) {
        val currentBlocked = settings.loadSnoozeBlockedChannels().toMutableSet()
        if (blocked) {
            currentBlocked.add(channelId)
        } else {
            currentBlocked.remove(channelId)
        }
        settings.saveSnoozeBlockedChannels(currentBlocked)
        loadSnoozeChannels(context)
    }

    /**
     * Executes the set snooze heads up enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setSnoozeHeadsUpEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isSnoozeHeadsUpEnabled.value = enabled
        settings.putBoolean(NotificationSettings.KEY_SNOOZE_HEADS_UP_ENABLED, enabled)
    }

    /**
     * Executes the sync show notification snooze operation.
     *
     * @param context [Context] Target context.
     */
    fun syncShowNotificationSnooze(context: Context) {
        val enabled =
            Settings.Secure.getInt(context.contentResolver, "show_notification_snooze", 0) == 1
        isShowNotificationSnoozeEnabled.value = enabled
    }

    /**
     * Executes the set show notification snooze enabled operation.
     *
     * @param context [Context] Target context.
     * @param enabled [Boolean] Target enabled.
     */
    fun setShowNotificationSnoozeEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        try {
            Settings.Secure.putInt(
                context.contentResolver,
                "show_notification_snooze",
                if (enabled) 1 else 0,
            )
            isShowNotificationSnoozeEnabled.value = enabled
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the load notification snooze options operation.
     *
     * @param context [Context] Target context.
     */
    fun loadNotificationSnoozeOptions(context: Context) {
        val raw =
            Settings.Global.getString(context.contentResolver, "notification_snooze_options") ?: ""
        var def = 60
        var opts = listOf(15, 30, 60, 120)
        if (raw.isNotEmpty()) {
            raw.split(",").forEach { pair ->
                val parts = pair.split("=", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val value = parts[1].trim()
                    if (key == "default") {
                        def = value.toIntOrNull() ?: 60
                    } else if (key == "options_array") {
                        opts = value.split(":").mapNotNull { it.trim().toIntOrNull() }
                    }
                }
            }
        }
        notificationSnoozeDefault.intValue = def
        notificationSnoozeOptions.value = opts
    }

    /**
     * Executes the save notification snooze options operation.
     *
     * @param context [Context] Target context.
     * @param def [Int] Target def.
     * @param opts [List<Int>] Target opts.
     */
    fun saveNotificationSnoozeOptions(
        context: Context,
        def: Int,
        opts: List<Int>,
    ) {
        val serialized = "default=$def,options_array=${opts.joinToString(":")}"
        try {
            Settings.Global.putString(
                context.contentResolver,
                "notification_snooze_options",
                serialized,
            )
            notificationSnoozeDefault.intValue = def
            notificationSnoozeOptions.value = opts
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the reset notification snooze options operation.
     *
     * @param context [Context] Target context.
     */
    fun resetNotificationSnoozeOptions(context: Context) {
        try {
            Settings.Global.putString(context.contentResolver, "notification_snooze_options", null)
            notificationSnoozeDefault.intValue = 60
            notificationSnoozeOptions.value = listOf(15, 30, 60, 120)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        /** Keys MainViewModel forwards to onPreferenceChanged. */
        val PREFERENCE_KEYS =
            setOf(
                NotificationSettings.KEY_SNOOZE_DISCOVERED_CHANNELS,
                NotificationSettings.KEY_SNOOZE_BLOCKED_CHANNELS,
                NotificationSettings.KEY_SNOOZE_HEADS_UP_ENABLED,
                NotificationSettings.KEY_AUTO_DISMISS_ENABLED,
                NotificationSettings.KEY_AUTO_DISMISS_APPS,
                NotificationSettings.KEY_AUTO_DISMISS_CHANNELS,
                NotificationSettings.KEY_AUTO_DISMISS_STATS,
            )
    }
}
