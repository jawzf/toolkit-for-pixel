package com.jawzf.pixeltoolkit.core.data.action

import android.content.Context

/** Runs the actions one feature owns. Returns false for actions it doesn't handle. */
fun interface ActionHandler {
    suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean
}

/** The feature handlers the action engine asks first. Registered once in ToolkitApp.onCreate. */
object ActionHandlers {
    private val handlers = mutableListOf<ActionHandler>()

    fun register(vararg handler: ActionHandler) {
        handlers += handler
    }

    suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean = handlers.any { it.handle(context, action) }
}
