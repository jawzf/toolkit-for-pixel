package com.jawzf.pixeltoolkit.feature.power.battery

import android.content.Context
import com.jawzf.pixeltoolkit.feature.power.charging.ChargingMode
import com.jawzf.pixeltoolkit.feature.power.charging.ChargingModeUtil

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
