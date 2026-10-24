/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Background Services & Receivers
 * File: ShizukuActionReceiver.kt
 * Description: Background service component for ShizukuActionReceiver.kt.
 */

package com.jawzf.pixeltoolkit.services.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.jawzf.pixeltoolkit.R
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.utils.ShellUtils
import com.jawzf.pixeltoolkit.utils.ShizukuUtils

class ShizukuActionReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action == "com.jawzf.pixeltoolkit.ACTION_RESTART_SHIZUKU") {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.cancel(9001)

            if (ShellUtils.usesAuthToken(context)) {
                val token = SettingsRepository(context).getShizukuAuthToken()
                if (token.isEmpty()) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.toast_enter_shizuku_token),
                            Toast.LENGTH_LONG,
                        ).show()
                    return
                }
            }

            ShizukuUtils.toggleShizuku(context, start = true)
        }
    }
}
