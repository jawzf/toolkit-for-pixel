package com.jawzf.pixeltoolkit.services.receivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jawzf.pixeltoolkit.domain.diy.DIYRepository

class LoopBreakerReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val automationId = intent.getStringExtra("automation_id") ?: return
        DIYRepository.init(context)
        val automation = DIYRepository.getAutomation(automationId) ?: return
        DIYRepository.updateAutomation(automation.copy(isEnabled = true))
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(automationId.hashCode())
    }
}
