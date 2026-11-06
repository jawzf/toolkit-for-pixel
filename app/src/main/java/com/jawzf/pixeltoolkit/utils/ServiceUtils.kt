package com.jawzf.pixeltoolkit.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarManager
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.domain.diy.Automation
import com.jawzf.pixeltoolkit.domain.diy.DIYRepository
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.connectivity.ConnectivitySettings
import com.jawzf.pixeltoolkit.feature.connectivity.sim.SimCarrierUtil
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import com.jawzf.pixeltoolkit.feature.input.InputSettings
import com.jawzf.pixeltoolkit.feature.power.PowerSettings
import com.jawzf.pixeltoolkit.feature.power.battery.BatteryNotificationService
import com.jawzf.pixeltoolkit.feature.widgets.stack.StackHost
import com.jawzf.pixeltoolkit.services.AppDetectionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object ServiceUtils {
    /**
     * Executes the start required services operation.
     *
     * @param context [Context] Target context.
     */
    fun startRequiredServices(context: Context) {
        val settingsRepository = SettingsRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            startAppDetectionServiceIfNeeded(context, settingsRepository)
            startBatteryNotificationServiceIfNeeded(context, settingsRepository)
        }
        applyPostRebootSettings(context, settingsRepository)
        StackHost.startIfNeeded(context)
    }

    fun applyPostRebootSettings(
        context: Context,
        settingsRepository: SettingsRepository,
    ) {
        if (!ShizukuUtils.isShizukuAvailable() || !ShizukuUtils.hasPermission()) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            StatusBarManager.update(context)

            if (ConnectivitySettings(context).isSimNamesApplyOnBootEnabled()) {
                SimCarrierUtil.applySavedCarrierNames(context)
            }

            if (PowerSettings(context).isPowerSavingApplyOnBootEnabled()) {
                val triggerLevel =
                    android.provider.Settings.Global.getInt(
                        context.contentResolver,
                        "low_power_trigger_level",
                        0,
                    )
                if (triggerLevel > 0) {
                    ShellUtils.runCommandWithOutput(
                        context,
                        "settings put global low_power_trigger_level $triggerLevel",
                    )
                }

                val constants =
                    android.provider.Settings.Global.getString(
                        context.contentResolver,
                        "battery_saver_constants",
                    )
                if (!constants.isNullOrBlank()) {
                    ShellUtils.runCommandWithOutput(
                        context,
                        "settings put global battery_saver_constants \"$constants\"",
                    )
                }
            }
        }
    }

    private fun startAppDetectionServiceIfNeeded(
        context: Context,
        settingsRepository: SettingsRepository,
    ) {
        val isAppLockEnabled =
            settingsRepository.getBoolean(AppsSettings.KEY_APP_LOCK_ENABLED)
        val isConsciousGateEnabled =
            settingsRepository.getBoolean(AppsSettings.KEY_CONSCIOUS_GATE_ENABLED)
        val isDynamicNightLightEnabled =
            settingsRepository.getBoolean(DisplaySettings.KEY_DYNAMIC_NIGHT_LIGHT_ENABLED)
        val isHideGestureBarOnLauncherEnabled =
            settingsRepository.getBoolean(SettingsRepository.KEY_HIDE_GESTURE_BAR_ON_LAUNCHER_ENABLED)
        val hasBackGestureApps = InputSettings(context).loadBackGestureAppConfigs().isNotEmpty()
        val isUseUsageAccess =
            settingsRepository.getBoolean(SettingsRepository.KEY_USE_USAGE_ACCESS)

        val hasAppAutomations =
            DIYRepository.automations.value.any {
                it.isEnabled && it.type == Automation.Type.APP
            }

        val shutUpConfigs = settingsRepository.loadShutUpConfigs()
        val hasShutUpApps = shutUpConfigs.any { it.isEnabled }

        val shouldRun =
            (
                isUseUsageAccess &&
                    (isAppLockEnabled || isConsciousGateEnabled || isDynamicNightLightEnabled || isHideGestureBarOnLauncherEnabled || hasBackGestureApps || hasAppAutomations)
            ) ||
                hasShutUpApps

        val intent = Intent(context, AppDetectionService::class.java)
        if (shouldRun) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (AppDetectionService.isRunning) {
            context.stopService(intent)
        }
    }

    private fun startBatteryNotificationServiceIfNeeded(
        context: Context,
        settingsRepository: SettingsRepository,
    ) {
        val isBatteryNotificationEnabled = PowerSettings(context).isBatteryNotificationEnabled()

        val intent = Intent(context, BatteryNotificationService::class.java)
        if (isBatteryNotificationEnabled) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
