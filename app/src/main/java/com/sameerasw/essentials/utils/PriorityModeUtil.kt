/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Utilities - General
 * File: PriorityModeUtil.kt
 * Description: Utility helper for PriorityModeUtil.kt.
 */

package com.sameerasw.essentials.utils

import android.app.NotificationManager
import android.content.Context
import android.provider.Settings

object PriorityModeUtil {
    fun isActive(context: Context): Boolean = isDndActive(context) || isBedtimeModeActive(context)

    fun isDndActive(context: Context): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        return nm?.currentInterruptionFilter?.let {
            it != NotificationManager.INTERRUPTION_FILTER_ALL &&
                it != NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        } ?: false
    }

    fun isBedtimeModeActive(context: Context): Boolean =
        try {
            Settings.Global.getInt(context.contentResolver, "bedtime_mode", 0) == 1
        } catch (_: Exception) {
            false
        }
}
