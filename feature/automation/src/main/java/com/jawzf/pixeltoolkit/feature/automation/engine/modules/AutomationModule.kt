package com.jawzf.pixeltoolkit.feature.automation.engine.modules

import android.content.Context
import com.jawzf.pixeltoolkit.feature.automation.diy.Automation

interface AutomationModule {
    val id: String

    fun start(context: Context)

    fun stop(context: Context)

    fun updateAutomations(automations: List<Automation>)
}
