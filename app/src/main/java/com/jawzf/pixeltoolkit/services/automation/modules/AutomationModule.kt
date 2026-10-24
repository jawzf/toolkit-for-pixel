package com.jawzf.pixeltoolkit.services.automation.modules

import android.content.Context
import com.jawzf.pixeltoolkit.domain.diy.Automation

interface AutomationModule {
    val id: String

    fun start(context: Context)

    fun stop(context: Context)

    fun updateAutomations(automations: List<Automation>)
}
