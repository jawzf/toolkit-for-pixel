package com.jawzf.pixeltoolkit.core.data.power

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.action.ChargingMode
import com.jawzf.pixeltoolkit.core.data.power.ChargingModeUtil

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
