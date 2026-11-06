package com.jawzf.pixeltoolkit.feature.notifications

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissAppConfig
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissChannel
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissStat
import com.jawzf.pixeltoolkit.feature.notifications.snooze.SnoozeChannel

/** Notification settings on the main preference file. Keys are unchanged from SettingsRepository. */
class NotificationSettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    fun isAutoDismissEnabled(): Boolean = getBoolean(KEY_AUTO_DISMISS_ENABLED, false)

    fun setAutoDismissEnabled(enabled: Boolean) = putBoolean(KEY_AUTO_DISMISS_ENABLED, enabled)

    fun getAutoDismissLastMinutes(): Int = getInt(KEY_AUTO_DISMISS_LAST_MINUTES, 15)

    fun setAutoDismissLastMinutes(minutes: Int) = putInt(KEY_AUTO_DISMISS_LAST_MINUTES, minutes)

    fun loadAutoDismissConfigs(): List<AutoDismissAppConfig> {
        val json = prefs.getString(KEY_AUTO_DISMISS_APPS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<AutoDismissAppConfig>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveAutoDismissConfigs(configs: List<AutoDismissAppConfig>) {
        val json = gson.toJson(configs)
        putString(KEY_AUTO_DISMISS_APPS, json)
    }

    fun loadAutoDismissChannels(): List<AutoDismissChannel> {
        val json = prefs.getString(KEY_AUTO_DISMISS_CHANNELS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<AutoDismissChannel>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun saveAutoDismissChannels(channels: List<AutoDismissChannel>) {
        val json = gson.toJson(channels)
        putString(KEY_AUTO_DISMISS_CHANNELS, json)
    }

    fun loadAutoDismissStats(): Map<String, AutoDismissStat> {
        val json = prefs.getString(KEY_AUTO_DISMISS_STATS, null) ?: return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, AutoDismissStat>>() {}.type
            gson.fromJson<Map<String, AutoDismissStat>>(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveAutoDismissStats(stats: Map<String, AutoDismissStat>) {
        val json = gson.toJson(stats)
        putString(KEY_AUTO_DISMISS_STATS, json)
    }

    fun incrementAutoDismissCount(packageName: String) {
        val current = loadAutoDismissStats().toMutableMap()
        val existing = current[packageName]
        val updated =
            if (existing != null) {
                existing.copy(count = existing.count + 1)
            } else {
                AutoDismissStat(1, System.currentTimeMillis())
            }
        current[packageName] = updated
        saveAutoDismissStats(current)
    }

    // Snooze Notifications Helper
    fun loadSnoozeDiscoveredChannels(): List<SnoozeChannel> {
        val json = prefs.getString(KEY_SNOOZE_DISCOVERED_CHANNELS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<SnoozeChannel>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Executes the save snooze discovered channels operation.
     *
     * @param channels [List<SnoozeChannel>] Target channels.
     */
    fun saveSnoozeDiscoveredChannels(channels: List<SnoozeChannel>) {
        val json = gson.toJson(channels)
        putString(KEY_SNOOZE_DISCOVERED_CHANNELS, json)
    }

    /**
     * Executes the load snooze blocked channels operation.
     * @return The resulting Set<String> data.
     */
    fun loadSnoozeBlockedChannels(): Set<String> {
        val json = prefs.getString(KEY_SNOOZE_BLOCKED_CHANNELS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
    }

    /**
     * Executes the save snooze blocked channels operation.
     *
     * @param blockedChannels [Set<String>] Target blocked channels.
     */
    fun saveSnoozeBlockedChannels(blockedChannels: Set<String>) {
        val json = gson.toJson(blockedChannels)
        putString(KEY_SNOOZE_BLOCKED_CHANNELS, json)
    }

    companion object {
        const val KEY_SNOOZE_DISCOVERED_CHANNELS = "snooze_discovered_channels"
        const val KEY_SNOOZE_BLOCKED_CHANNELS = "snooze_blocked_channels"
        const val KEY_SNOOZE_HEADS_UP_ENABLED = "snooze_heads_up_enabled"
        const val KEY_AUTO_DISMISS_ENABLED = "auto_dismiss_notifications_enabled"
        const val KEY_AUTO_DISMISS_APPS = "auto_dismiss_notifications_apps"
        const val KEY_AUTO_DISMISS_LAST_MINUTES = "auto_dismiss_notifications_last_minutes"
        const val KEY_AUTO_DISMISS_CHANNELS = "auto_dismiss_notifications_channels"
        const val KEY_AUTO_DISMISS_STATS = "auto_dismiss_notifications_stats"
    }
}
