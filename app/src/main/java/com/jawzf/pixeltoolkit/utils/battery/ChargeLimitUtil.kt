package com.jawzf.pixeltoolkit.utils.battery

import android.content.Context

object ChargeLimitUtil {
    private const val SHIELD_LEVEL = 78

    fun isLimitActive(context: Context): Boolean =
        ChargingModeUtil.getMode(context) == ChargingMode.LIMITED

    fun isShielded(
        context: Context,
        level: Int,
        charging: Boolean,
    ): Boolean = charging && level >= SHIELD_LEVEL && isLimitActive(context)
}
