package com.jawzf.pixeltoolkit.feature.automation.engine

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.permissions.AppPermission
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.automation.diy.Automation
import com.jawzf.pixeltoolkit.feature.automation.diy.conditionText
import com.jawzf.pixeltoolkit.feature.automation.engine.LoopBreakerReceiver

object AutomationFailureNotifier {
    private const val CHANNEL_ID = "automations"

    private fun createChannelIfNeeded(
        context: Context,
        manager: NotificationManager,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.automation_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ),
            )
        }
    }

    fun missingPermissions(
        context: Context,
        action: Action,
    ): List<AppPermission> =
        action.permissions
            .mapNotNull { key ->
                if (key.equals("SHIZUKU", true) || key.equals("ROOT", true)) {
                    if (ShellUtils.isRootEnabled(context)) AppPermission.ROOT else AppPermission.SHIZUKU
                } else {
                    AppPermission.fromKey(key)
                }
            }.distinct()
            .filterNot { it.isGranted(context) }

    fun notifyMissingPermissions(
        context: Context,
        action: Action,
        missing: List<AppPermission>,
    ) {
        val reason = context.getString(R.string.automation_failed_missing, missing.joinToString { context.getString(it.titleRes) })
        notifyFailure(context, action, reason)
    }

    fun notifyFailure(
        context: Context,
        action: Action,
        reason: String,
    ) {
        if (!PermissionUtils.isPostNotificationsEnabled(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannelIfNeeded(context, manager)

        val actionName = context.getString(action.title)
        val openApp =
            PendingIntent.getActivity(
                context,
                0,
                com.jawzf.pixeltoolkit.core.platform.AppComponents.mainIntent(context).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        val text = context.getString(R.string.automation_failed_text, actionName, reason)
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(context.getString(R.string.automation_failed_title))
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build()
        manager.notify(action::class.java.name.hashCode(), notification)
    }

    fun notifyLoopBreak(
        context: Context,
        automation: Automation,
    ) {
        if (!PermissionUtils.isPostNotificationsEnabled(context)) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createChannelIfNeeded(context, manager)

        val openApp =
            PendingIntent.getActivity(
                context,
                0,
                Intent().setClassName(context, AppComponents.MAIN_ACTIVITY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val reenableIntent =
            Intent(context, LoopBreakerReceiver::class.java).apply {
                putExtra("automation_id", automation.id)
            }
        val reenablePendingIntent =
            PendingIntent.getBroadcast(
                context,
                automation.id.hashCode(),
                reenableIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val condText = conditionText(context, automation)
        val text = context.getString(R.string.automation_paused_text, condText)
        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(context.getString(R.string.automation_paused_title))
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setContentIntent(openApp)
                .addAction(
                    0,
                    context.getString(R.string.automation_paused_reenable),
                    reenablePendingIntent,
                ).setAutoCancel(true)
                .build()
        manager.notify(automation.id.hashCode(), notification)
    }
}
