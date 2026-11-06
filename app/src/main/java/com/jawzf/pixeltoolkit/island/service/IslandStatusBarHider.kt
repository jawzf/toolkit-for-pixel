package com.jawzf.pixeltoolkit.island.service

import android.content.Context
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarManager

object IslandStatusBarHider {
    private const val REQUESTER_ID = "IslandDynamicHide"

    fun apply(
        context: Context,
        hide: Boolean,
    ) {
        if (hide) {
            StatusBarManager.requestDisable(
                context,
                REQUESTER_ID,
                setOf(
                    StatusBarManager.FLAG_SYSTEM_ICONS,
                    StatusBarManager.FLAG_NOTIFICATION_ICONS,
                    StatusBarManager.FLAG_CLOCK,
                ),
            )
        } else {
            StatusBarManager.requestRestore(context, REQUESTER_ID)
        }
    }

    fun applyFlags(
        context: Context,
        flags: Set<String>,
    ) {
        if (flags.isEmpty()) {
            StatusBarManager.requestRestore(context, REQUESTER_ID)
        } else {
            StatusBarManager.requestDisable(context, REQUESTER_ID, flags)
        }
    }

    fun restore(context: Context) {
        StatusBarManager.requestRestore(context, REQUESTER_ID)
    }
}
