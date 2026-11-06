package com.jawzf.pixeltoolkit.core.platform.device

import android.app.PendingIntent
import android.graphics.Bitmap

data class ProgressNotificationData(
    val key: String,
    val packageName: String,
    val progress: Float,
    val isIndeterminate: Boolean,
    val icon: Bitmap?,
    val postTime: Long,
    val title: String? = null,
    val text: String? = null,
    val appName: String? = null,
    val contentIntent: PendingIntent? = null,
)
