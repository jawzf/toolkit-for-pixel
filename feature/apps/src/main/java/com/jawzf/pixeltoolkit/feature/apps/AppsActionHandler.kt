package com.jawzf.pixeltoolkit.feature.apps

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionHandler
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.feature.apps.freeze.FreezeManager

/** Runs Apps's DIY actions for the action engine. */
object AppsActionHandler : ActionHandler {
    override suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean {
        when (action) {
            is Action.FreezeTag -> freezeTag(context, action)
            is Action.FreezeApps -> {
                val failed =
                    action.packageNames.filterNot { pkg ->
                        FreezeManager
                            .freezeApp(context, pkg)
                    }
                if (failed.isNotEmpty()) error(context.getString(R.string.automation_failed_apps, failed.joinToString()))
            }
            is Action.UnfreezeApps -> {
                val failed =
                    action.packageNames.filterNot { pkg ->
                        FreezeManager
                            .unfreezeApp(context, pkg)
                    }
                if (failed.isNotEmpty()) error(context.getString(R.string.automation_failed_apps, failed.joinToString()))
            }
            else -> return false
        }
        return true
    }

    /** Freezes or unfreezes apps matching selected tag IDs. */
    private fun freezeTag(
        context: Context,
        action: Action.FreezeTag,
    ) {
        val appTagMap = AppsSettings(context).getFreezeAppTagMap()
        val selectedTags = action.tagIds.toSet()

        if (selectedTags.isNotEmpty()) {
            val matchingPackages =
                appTagMap
                    .filterValues { tags ->
                        tags.any { selectedTags.contains(it) }
                    }.keys

            matchingPackages.forEach { pkg ->
                if (action.mode == "Freeze") {
                    FreezeManager
                        .freezeApp(context, pkg)
                } else {
                    FreezeManager.unfreezeApp(
                        context,
                        pkg,
                    )
                }
            }
        }
    }
}
