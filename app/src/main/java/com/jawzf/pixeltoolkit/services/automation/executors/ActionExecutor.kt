package com.jawzf.pixeltoolkit.services.automation.executors

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.action.Action

interface ActionExecutor {
    suspend fun execute(
        context: Context,
        action: Action,
    )
}
