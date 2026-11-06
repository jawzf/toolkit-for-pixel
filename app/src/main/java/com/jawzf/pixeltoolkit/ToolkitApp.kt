package com.jawzf.pixeltoolkit

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.jawzf.pixeltoolkit.core.data.action.ActionRunner
import com.jawzf.pixeltoolkit.core.data.log.LogManager
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.platform.AppGlobals
import com.jawzf.pixeltoolkit.core.platform.device.CarConnectionMonitor
import com.jawzf.pixeltoolkit.core.platform.device.ProgressNotificationHub
import com.jawzf.pixeltoolkit.core.platform.media.MediaArtBridge
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarManager
import com.jawzf.pixeltoolkit.services.NotificationListener
import com.jawzf.pixeltoolkit.services.automation.executors.CombinedActionExecutor
import com.jawzf.pixeltoolkit.services.receivers.SecurityReceiver
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
        AppGlobals.init(this)
        ActionRunner.init(CombinedActionExecutor)
        MediaArtBridge.init(NotificationListener::getCachedBitmap, NotificationListener::getLatestArtBitmap)
        ProgressNotificationHub.init(NotificationListener::getLatestProgressNotification)
        HapticUtil.initialize(this)

        try {
            resources?.configuration
        } catch (e: Exception) {
        }

        try {
            HiddenApiBypass.setHiddenApiExemptions("")
        } catch (_: Throwable) {
        }

        PorterShizukuBridge.start(CoroutineScope(SupervisorJob() + Dispatchers.Default))
        ShizukuUtils.initialize(this) { StatusBarManager.update(it) }
        CarConnectionMonitor
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
