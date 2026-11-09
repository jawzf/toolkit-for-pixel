package com.jawzf.pixeltoolkit.feature.sound

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionHandler
import com.jawzf.pixeltoolkit.feature.sound.mode.SoundModeHandler

/** Runs Sound's DIY actions for the action engine. */
object SoundActionHandler : ActionHandler {
    override suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean {
        when (action) {
            is Action.CycleSoundModes -> {
                SoundModeHandler(context)
                    .cycleNextMode()
            }
            else -> return false
        }
        return true
    }
}
