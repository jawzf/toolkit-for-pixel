package com.jawzf.pixeltoolkit.core.data.action

import android.content.Context

/** Runs DIY actions. The engine lives in `:app`; features call it through [ActionRunner]. */
interface ActionExecutor {
    suspend fun executeAll(
        context: Context,
        actions: List<Action>,
    )

    suspend fun execute(
        context: Context,
        action: Action,
        fromAutomation: Boolean = false,
    )
}

/** Holds the app's [ActionExecutor]. Set once in ToolkitApp.onCreate. */
object ActionRunner : ActionExecutor {
    private lateinit var executor: ActionExecutor

    fun init(actionExecutor: ActionExecutor) {
        executor = actionExecutor
    }

    override suspend fun executeAll(
        context: Context,
        actions: List<Action>,
    ) = executor.executeAll(context, actions)

    override suspend fun execute(
        context: Context,
        action: Action,
        fromAutomation: Boolean,
    ) = executor.execute(context, action, fromAutomation)
}
