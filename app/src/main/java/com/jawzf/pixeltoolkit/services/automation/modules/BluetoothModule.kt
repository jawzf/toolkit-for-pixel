package com.jawzf.pixeltoolkit.services.automation.modules

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.jawzf.pixeltoolkit.domain.diy.Automation
import com.jawzf.pixeltoolkit.domain.diy.Trigger
import com.jawzf.pixeltoolkit.services.automation.executors.CombinedActionExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BluetoothModule : AutomationModule {
    companion object {
        const val ID = "bluetooth_module"
    }

    override val id: String = ID
    private var automations: List<Automation> = emptyList()
    private val scope = CoroutineScope(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    private val receiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                val device =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(
                            BluetoothDevice.EXTRA_DEVICE,
                            BluetoothDevice::class.java,
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    } ?: return

                val address = device.address ?: return

                when (intent.action) {
                    BluetoothDevice.ACTION_ACL_CONNECTED ->
                        handleTrigger(context, address) {
                            it is Trigger.BluetoothConnected && it.deviceAddress == address
                        }

                    BluetoothDevice.ACTION_ACL_DISCONNECTED ->
                        handleTrigger(context, address) {
                            it is Trigger.BluetoothDisconnected && it.deviceAddress == address
                        }
                }
            }
        }

    private var appContext: Context? = null

    override fun start(context: Context) {
        val appCtx = context.applicationContext
        appContext = appCtx
        val filter =
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            }
        appCtx.registerReceiver(receiver, filter)
    }

    override fun stop(context: Context) {
        val appCtx = appContext ?: context.applicationContext
        try {
            appCtx.unregisterReceiver(receiver)
        } catch (e: Exception) {
            // Ignore if not registered
        }
        appContext = null
    }

    override fun updateAutomations(automations: List<Automation>) {
        this.automations = automations
    }

    private fun handleTrigger(
        context: Context,
        address: String,
        matches: (Trigger) -> Boolean,
    ) {
        scope.launch {
            automations
                .filter { it.type == Automation.Type.TRIGGER && it.trigger?.let(matches) == true }
                .forEach { automation ->
                    CombinedActionExecutor.executeAll(context, automation.actionList)
                }
        }
    }
}
