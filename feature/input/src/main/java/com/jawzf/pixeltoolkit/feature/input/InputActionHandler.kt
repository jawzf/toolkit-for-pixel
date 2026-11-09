package com.jawzf.pixeltoolkit.feature.input

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionHandler
import com.jawzf.pixeltoolkit.feature.input.gesture.OmniTriggerUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Runs Input's DIY actions for the action engine. */
object InputActionHandler : ActionHandler {
    override suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean {
        when (action) {
            is Action.CircleToSearch -> {
                OmniTriggerUtil
                    .trigger(context)
            }
            is Action.TurnOffBackGesture -> withContext(Dispatchers.IO) { InputSettings(context).backGesture.turnOff() }
            is Action.TurnOnBackGesture -> withContext(Dispatchers.IO) { InputSettings(context).backGesture.turnOn() }
            else -> return false
        }
        return true
    }
}
