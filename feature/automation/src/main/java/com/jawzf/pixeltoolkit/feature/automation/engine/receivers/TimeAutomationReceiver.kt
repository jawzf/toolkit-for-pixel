package com.jawzf.pixeltoolkit.feature.automation.engine.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jawzf.pixeltoolkit.core.data.action.ActionRunner
import com.jawzf.pixeltoolkit.feature.automation.diy.Automation
import com.jawzf.pixeltoolkit.feature.automation.diy.DIYRepository
import com.jawzf.pixeltoolkit.feature.automation.engine.modules.TimeModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TimeAutomationReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_TRIGGER = "com.jawzf.pixeltoolkit.ACTION_TIME_AUTOMATION_TRIGGER"
        const val EXTRA_AUTOMATION_ID = "automation_id"
        const val EXTRA_IS_ENTRY = "is_entry"
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != ACTION_TRIGGER) return

        val automationId = intent.getStringExtra(EXTRA_AUTOMATION_ID) ?: return
        val isEntry = intent.getBooleanExtra(EXTRA_IS_ENTRY, true)

        val pendingResult = goAsync()
        scope.launch {
            try {
                DIYRepository.init(context)
                val automation = DIYRepository.getAutomation(automationId) ?: return@launch
                if (!automation.isEnabled) {
                    return@launch
                }

                TimeModule.rescheduleNext(context, automation, isEntry)

                when (automation.type) {
                    Automation.Type.TRIGGER -> {
                        ActionRunner.executeAll(context, automation.actionList)
                    }

                    Automation.Type.STATE -> {
                        ActionRunner.executeAll(context, if (isEntry) automation.entryActionList else automation.exitActionList)
                    }

                    else -> {}
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
