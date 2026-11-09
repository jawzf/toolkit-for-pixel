package com.jawzf.pixeltoolkit.feature.connectivity

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionHandler
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.feature.connectivity.hotspot.HotspotController
import org.lsposed.hiddenapibypass.HiddenApiBypass

/** Runs Connectivity's DIY actions for the action engine. */
object ConnectivityActionHandler : ActionHandler {
    override suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean {
        when (action) {
            is Action.TurnOnHotspot -> setHotspotEnabled(context, true)
            is Action.TurnOffHotspot -> setHotspotEnabled(context, false)
            is Action.ToggleHotspot -> setHotspotEnabled(context, !isHotspotEnabled(context))
            else -> return false
        }
        return true
    }

    private fun isHotspotEnabled(context: Context): Boolean =
        try {
            val wifiManager =
                context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val result =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    HiddenApiBypass.invoke(
                        WifiManager::class.java,
                        wifiManager,
                        "isWifiApEnabled",
                    )
                } else {
                    @Suppress("DEPRECATION")
                    WifiManager::class.java.getMethod("isWifiApEnabled").invoke(wifiManager)
                }
            result as? Boolean ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

    private fun setHotspotEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && ShellUtils.usesShizukuBinder(context) && ShizukuUtils.hasPermission()) {
            try {
                HotspotController.setEnabled(context, enabled)
                return
            } catch (e: Exception) {
                Log.e("CombinedActionExecutor", "Tethering via Shizuku failed, falling back to shell", e)
            }
        }
        val command = if (enabled) "cmd wifi start-softap" else "cmd wifi stop-softap"
        ShellUtils.runCommand(
            context,
            command,
            featureName = context.getString(if (enabled) R.string.diy_action_hotspot_on else R.string.diy_action_hotspot_off),
        )
    }
}
