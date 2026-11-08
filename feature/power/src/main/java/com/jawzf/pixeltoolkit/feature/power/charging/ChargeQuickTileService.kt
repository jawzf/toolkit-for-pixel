package com.jawzf.pixeltoolkit.feature.power.charging

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService

class ChargeQuickTileService : BaseTileService() {
    companion object {
        private const val ADAPTIVE_CHARGING_SETTING = "adaptive_charging_enabled"
        private const val CHARGE_OPTIMIZATION_MODE = "charge_optimization_mode"
    }

    override fun onClick() {
        if (!hasFeaturePermission()) {
            HapticUtil
                .performHapticForService(this)
            val intent =
                Intent().setClassName(this, AppComponents.FEATURE_SETTINGS_ACTIVITY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("feature", "Quick settings tiles")
                }
            val pendingIntent =
                android.app.PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
                )
            startActivityAndCollapse(pendingIntent)
            return
        }
        super.onClick()
    }

    override fun onTileClick() {
        val adaptiveChargingEnabled = getSecureInt(ADAPTIVE_CHARGING_SETTING, 0) == 1
        val chargeOptimizationEnabled = getSecureInt(CHARGE_OPTIMIZATION_MODE, 0) == 1

        val settingsRepository =
            PowerSettings(this)
        val enableAdaptive = settingsRepository.getBoolean("charge_opt_toggle_adaptive", true)
        val enableLimit = settingsRepository.getBoolean("charge_opt_toggle_limit", true)
        val enableDeactivated = settingsRepository.getBoolean("charge_opt_toggle_deactivated", true)

        val currentSelection =
            when {
                chargeOptimizationEnabled -> 2 // LIMIT
                adaptiveChargingEnabled -> 1 // ADAPTIVE
                else -> 0 // DEACTIVATED
            }

        // Determine the next state
        var nextSelection = currentSelection
        for (i in 1..3) {
            val candidate = (currentSelection + i) % 3
            val isAllowed =
                when (candidate) {
                    0 -> enableDeactivated
                    1 -> enableAdaptive
                    2 -> enableLimit
                    else -> false
                }
            if (isAllowed) {
                nextSelection = candidate
                break
            }
        }

        // Apply new values based on selection
        when (nextSelection) {
            0 -> {
                putSecureInt(CHARGE_OPTIMIZATION_MODE, 0)
                putSecureInt(ADAPTIVE_CHARGING_SETTING, 0)
            }

            1 -> {
                putSecureInt(CHARGE_OPTIMIZATION_MODE, 0)
                putSecureInt(ADAPTIVE_CHARGING_SETTING, 1)
            }

            2 -> {
                putSecureInt(CHARGE_OPTIMIZATION_MODE, 1)
                putSecureInt(ADAPTIVE_CHARGING_SETTING, 0)
            }
        }
    }

    override fun getTileLabel(): String = getString(R.string.tile_charge_optimization)

    override fun getTileSubtitle(): String {
        val adaptiveChargingEnabled = getSecureInt(ADAPTIVE_CHARGING_SETTING, 0) == 1
        val chargeOptimizationEnabled = getSecureInt(CHARGE_OPTIMIZATION_MODE, 0) == 1
        return when {
            chargeOptimizationEnabled -> getString(R.string.limit_to_80)
            adaptiveChargingEnabled -> getString(R.string.adaptive_charging)
            else -> getString(R.string.deactivated)
        }
    }

    override fun hasFeaturePermission(): Boolean = ShellUtils.isAvailable(this) && ShellUtils.hasPermission(this)

    override fun getTileIcon(): Icon {
        val adaptiveChargingEnabled = getSecureInt(ADAPTIVE_CHARGING_SETTING, 0) == 1
        val chargeOptimizationEnabled = getSecureInt(CHARGE_OPTIMIZATION_MODE, 0) == 1
        val resId =
            when {
                chargeOptimizationEnabled -> R.drawable.rounded_battery_android_frame_shield_24
                adaptiveChargingEnabled -> R.drawable.rounded_battery_android_frame_plus_24
                else -> R.drawable.outline_battery_android_frame_bolt_24
            }
        return Icon.createWithResource(this, resId)
    }

    override fun getTileState(): Int {
        val adaptiveChargingEnabled = getSecureInt(ADAPTIVE_CHARGING_SETTING, 0) == 1
        val chargeOptimizationEnabled = getSecureInt(CHARGE_OPTIMIZATION_MODE, 0) == 1
        return if (chargeOptimizationEnabled || adaptiveChargingEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
}
