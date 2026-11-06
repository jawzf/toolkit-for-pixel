package com.jawzf.pixeltoolkit.feature.notifications.autodismiss

import android.app.Notification
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.service.notification.NotificationListenerService.Ranking
import android.service.notification.StatusBarNotification
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissAppConfig
import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissChannel

object NotificationAutoDismisser {
    private val handler = Handler(Looper.getMainLooper())
    private val pending = HashMap<String, Runnable>()
    private var knownChannels: HashSet<String>? = null

    var isSnoozedByUs: (String) -> Boolean = { false }

    fun onPosted(
        listener: NotificationListenerService,
        sbn: StatusBarNotification,
        isRepost: Boolean,
    ) {
        val repo = NotificationSettings(listener)
        val configs = repo.loadAutoDismissConfigs()
        learnChannel(listener, sbn, configs, repo)

        if (!repo.isAutoDismissEnabled()) return
        val key = sbn.key
        if (isSnoozedByUs(key)) return
        if (isRepost && key in pending) return

        remove(key)

        val now = System.currentTimeMillis()
        val deadline = deadlineOf(listener, sbn, configs, now) ?: return

        schedule(listener, key, deadline)
    }

    fun onRemoved(key: String) {
        remove(key)
    }

    fun resync(listener: NotificationListenerService) {
        clear()
        val repo = NotificationSettings(listener)
        if (!repo.isAutoDismissEnabled()) return

        val configs = repo.loadAutoDismissConfigs()
        val active =
            try {
                listener.activeNotifications.orEmpty()
            } catch (_: Exception) {
                emptyArray()
            }
        val now = System.currentTimeMillis()
        val overdue = mutableListOf<StatusBarNotification>()
        for (sbn in active) {
            if (isSnoozedByUs(sbn.key)) continue
            val deadline = deadlineOf(listener, sbn, configs, sbn.postTime) ?: continue

            if (deadline <= now) {
                overdue.add(sbn)
            } else {
                schedule(listener, sbn.key, deadline)
            }
        }

        val overdueKeys = overdue.map { it.key }.toSet()
        for (sbn in overdue) {
            dismiss(listener, sbn.key, sbn, overdueKeys)
        }
    }

    fun clear() {
        pending.values.forEach { handler.removeCallbacks(it) }
        pending.clear()
    }

    private fun remove(key: String) {
        pending.remove(key)?.let { handler.removeCallbacks(it) }
    }

    private fun schedule(
        listener: NotificationListenerService,
        key: String,
        deadline: Long,
    ) {
        remove(key)
        val now = System.currentTimeMillis()
        if (deadline <= now) {
            dismiss(listener, key)
            return
        }
        val uptimeMillis = SystemClock.uptimeMillis() + (deadline - now)
        val runnable =
            Runnable {
                pending.remove(key)
                dismiss(listener, key)
            }
        pending[key] = runnable
        handler.postAtTime(runnable, uptimeMillis)
    }

    private fun dismiss(
        listener: NotificationListenerService,
        key: String,
        sbn: StatusBarNotification? = null,
        dismissedKeys: Set<String> = emptySet(),
    ) {
        if (isSnoozedByUs(key)) return
        val active =
            try {
                listener.activeNotifications.orEmpty()
            } catch (_: Exception) {
                emptyArray()
            }
        val target = sbn ?: active.firstOrNull { it.key == key } ?: return

        try {
            listener.cancelNotification(key)
            NotificationSettings(listener).incrementAutoDismissCount(target.packageName)
        } catch (_: Exception) {
        }

        val groupKey = target.groupKey ?: return
        val hasOtherChildren =
            active.any { other ->
                other.key != key &&
                    other.key !in dismissedKeys &&
                    other.packageName == target.packageName &&
                    other.groupKey == groupKey &&
                    !other.isGroupSummary()
            }

        if (!hasOtherChildren) {
            active
                .filter { candidate ->
                    candidate.key != key &&
                        candidate.packageName == target.packageName &&
                        candidate.groupKey == groupKey &&
                        candidate.isGroupSummary()
                }.forEach { summary ->
                    try {
                        listener.cancelNotification(summary.key)
                    } catch (_: Exception) {
                    }
                }
        }
    }

    private fun ensureKnownChannels(repo: NotificationSettings): HashSet<String> {
        val set = knownChannels ?: repo.loadAutoDismissChannels().mapTo(HashSet()) { "${it.packageName}/${it.id}" }
        knownChannels = set
        return set
    }

    private fun learnChannel(
        listener: NotificationListenerService,
        sbn: StatusBarNotification,
        configs: List<AutoDismissAppConfig>,
        repo: NotificationSettings,
    ) {
        val pkg = sbn.packageName
        if (configs.none { it.packageName == pkg }) return
        val channelId = sbn.notification.channelId
        if (channelId.isNullOrBlank()) return

        val known = ensureKnownChannels(repo)
        val channelKey = "$pkg/$channelId"
        if (channelKey in known) return

        val rankingName =
            try {
                val map = listener.currentRanking
                if (map != null) {
                    val ranking = Ranking()
                    if (map.getRanking(sbn.key, ranking)) {
                        ranking.channel?.name?.toString()
                    } else {
                        null
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

        val name = if (!rankingName.isNullOrBlank()) rankingName else channelId

        known.add(channelKey)
        val channels = repo.loadAutoDismissChannels().toMutableList()
        channels.add(AutoDismissChannel(packageName = pkg, id = channelId, name = name))
        repo.saveAutoDismissChannels(channels)
    }

    private fun deadlineOf(
        listener: NotificationListenerService,
        sbn: StatusBarNotification,
        configs: List<AutoDismissAppConfig>,
        postTime: Long,
    ): Long? =
        autoDismissDeadline(
            packageName = sbn.packageName,
            postTime = postTime,
            isClearable = sbn.isClearable,
            isGroupSummary = sbn.isGroupSummary(),
            ownPackage = listener.packageName,
            configs = configs,
            channelId = sbn.notification.channelId,
        )

    private fun StatusBarNotification.isGroupSummary(): Boolean = (notification.flags and Notification.FLAG_GROUP_SUMMARY) != 0
}
