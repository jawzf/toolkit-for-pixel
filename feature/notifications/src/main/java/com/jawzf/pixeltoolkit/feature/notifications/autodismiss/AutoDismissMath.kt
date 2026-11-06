package com.jawzf.pixeltoolkit.feature.notifications.autodismiss

import com.jawzf.pixeltoolkit.feature.notifications.autodismiss.AutoDismissAppConfig

val AUTO_DISMISS_MINUTES = listOf(1, 5, 10, 15, 30, 60, 120, 180, 360, 720, 1440)

fun autoDismissDeadline(
    packageName: String,
    postTime: Long,
    isClearable: Boolean,
    isGroupSummary: Boolean,
    ownPackage: String,
    configs: List<AutoDismissAppConfig>,
    channelId: String? = null,
): Long? {
    if (packageName == ownPackage) return null
    if (!isClearable) return null
    if (isGroupSummary) return null
    val config = configs.firstOrNull { it.packageName == packageName } ?: return null
    if (channelId != null && channelId in config.excludedChannels.orEmpty()) return null
    return postTime + config.minutes * 60_000L
}
