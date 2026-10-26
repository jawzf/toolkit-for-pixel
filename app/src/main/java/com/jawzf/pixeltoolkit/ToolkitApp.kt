package com.jawzf.pixeltoolkit

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.jawzf.pixeltoolkit.services.receivers.SecurityReceiver
import com.jawzf.pixeltoolkit.utils.LogManager
import com.jawzf.pixeltoolkit.utils.ShizukuUtils
import eu.darken.porter.bridge.PorterShizukuBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.lsposed.hiddenapibypass.HiddenApiBypass

class ToolkitApp : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    private val securityReceiver = SecurityReceiver()

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        com.jawzf.pixeltoolkit.utils.HapticUtil.initialize(this)

        try {
            resources?.configuration
        } catch (e: Exception) {
        }

        try {
            HiddenApiBypass.setHiddenApiExemptions("")
        } catch (_: Throwable) {
        }

        PorterShizukuBridge.start(CoroutineScope(SupervisorJob() + Dispatchers.Default))
        ShizukuUtils.initialize()
        com.jawzf.pixeltoolkit.utils.CarConnectionMonitor
            .initialize(this)
        LogManager.init(this)

        // Init Automation
        com.jawzf.pixeltoolkit.domain.diy.DIYRepository
            .init(this)
        com.jawzf.pixeltoolkit.services.automation.AutomationManager
            .init(this)
        com.jawzf.pixeltoolkit.utils.ServiceUtils
            .startRequiredServices(this)

        val intentFilter =
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(securityReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            registerReceiver(securityReceiver, intentFilter)
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(securityReceiver)
    }
}
