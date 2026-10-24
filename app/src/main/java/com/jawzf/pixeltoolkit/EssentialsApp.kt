package com.jawzf.pixeltoolkit

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.services.receivers.SecurityReceiver
import com.jawzf.pixeltoolkit.utils.ShizukuUtils
import eu.darken.porter.bridge.PorterShizukuBridge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import io.sentry.android.core.SentryAndroid
import org.lsposed.hiddenapibypass.HiddenApiBypass

class EssentialsApp : Application() {
    companion object {
        lateinit var context: Context
            private set
    }

    private val securityReceiver = SecurityReceiver()

    override fun onCreate() {
        super.onCreate()
        com.jawzf.pixeltoolkit.data.repository.SettingsRepository(this).syncTorchRestriction()
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
        com.jawzf.pixeltoolkit.utils.LogManager
            .init(this)

        // Init Automation
        com.jawzf.pixeltoolkit.domain.diy.DIYRepository
            .init(this)
        com.jawzf.pixeltoolkit.services.automation.AutomationManager
            .init(this)
        com.jawzf.pixeltoolkit.services.CalendarSyncManager
            .init(this)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            com.jawzf.pixeltoolkit.services.DeviceInfoSyncManager
                .init(this@EssentialsApp)
        }
        com.jawzf.pixeltoolkit.utils.ServiceUtils
            .startRequiredServices(this)

        com.jawzf.pixeltoolkit.appfunctions.AppFunctionAvailabilityManager
            .updateAvailability(this)

        initSentry()

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

    private fun initSentry() {
        val repository = SettingsRepository(this)
        val mode = repository.getString(SettingsRepository.KEY_SENTRY_REPORT_MODE, "auto")

        if (mode == "off") return

        SentryAndroid.init(this) { options ->
            options.dsn =
                "https://e105699467efe3a43a16bfbad3a63b33@o4510996760887296.ingest.de.sentry.io/4510996763312208"
            options.isEnabled = true

            options.setBeforeSend { event, hint ->
                try {
                    val throwable = event.throwable
                    val threadName = event.threads?.firstOrNull()?.name ?: Thread.currentThread().name
                    val message = event.message?.formatted ?: event.exceptions?.firstOrNull()?.value
                    com.jawzf.pixeltoolkit.utils.LogManager.saveCrashReport(
                        context = this@EssentialsApp,
                        threadName = threadName,
                        throwable = throwable,
                        customMessage = message,
                    )
                } catch (e: Exception) {
                    android.util.Log.e("EssentialsApp", "Failed to save crash report locally in Sentry callback", e)
                }

                Handler(Looper.getMainLooper()).post {
                    Toast
                        .makeText(
                            this@EssentialsApp,
                            R.string.sentry_crash_toast,
                            Toast.LENGTH_LONG,
                        ).show()
                }
                event
            }
        }
    }
}
