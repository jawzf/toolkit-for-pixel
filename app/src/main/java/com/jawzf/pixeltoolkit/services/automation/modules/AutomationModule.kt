/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Background Services & Receivers
 * File: AutomationModule.kt
 * Description: Background service component for AutomationModule.kt.
 */

package com.jawzf.pixeltoolkit.services.automation.modules

import android.content.Context
import com.jawzf.pixeltoolkit.domain.diy.Automation

interface AutomationModule {
    val id: String

    fun start(context: Context)

    fun stop(context: Context)

    fun updateAutomations(automations: List<Automation>)
}
