package com.jawzf.pixeltoolkit.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.jawzf.pixeltoolkit.MainActivity
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.domain.controller.CaffeinateController
import com.jawzf.pixeltoolkit.services.tiles.BaseTileService
import com.jawzf.pixeltoolkit.services.tiles.CaffeinateTileService
import com.jawzf.pixeltoolkit.services.tiles.DataSimController
import com.jawzf.pixeltoolkit.services.tiles.DataSimTileService
import com.jawzf.pixeltoolkit.services.tiles.FlashlightTileService
import com.jawzf.pixeltoolkit.utils.PermissionUtils
import com.jawzf.pixeltoolkit.utils.ShellUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QsTileActionRouter : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != ACTION_TRIGGER_TILE) return

        val serviceClassName = intent.getStringExtra(EXTRA_SERVICE_CLASS_NAME) ?: return
        Log.d("QsTileActionRouter", "Triggering tile action for: $serviceClassName")

        // Perform haptics on click trigger
        HapticUtil.performHapticForService(context)

        try {
            when (serviceClassName) {
                CaffeinateTileService::class.java.name -> {
                    if (CaffeinateController.isStarting.value) {
                        CaffeinateController.cycleTimeout(context)
                    } else {
                        CaffeinateController.toggle(context)
                    }
                }

                FlashlightTileService::class.java.name -> {
                    val flashlightIntent =
                        Intent(context, FlashlightActionReceiver::class.java).apply {
                            action = FlashlightActionReceiver.ACTION_TOGGLE
                        }
                    context.sendBroadcast(flashlightIntent)
                }

                DataSimTileService::class.java.name -> {
                    val pending = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (ShellUtils.hasPermission(context) && PermissionUtils.hasReadPhoneStatePermission(context)) {
                                val state = DataSimController.advance(context)
                                if (state.dataEnableFailed) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, R.string.tile_data_sim_data_enable_failed, Toast.LENGTH_LONG).show()
                                    }
                                }
                            } else {
                                fallbackLaunch(context)
                            }
                        } catch (e: Exception) {
                            Log.e("QsTileActionRouter", "Failed to switch data SIM", e)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, R.string.tile_data_sim_failed, Toast.LENGTH_SHORT).show()
                            }
                        } finally {
                            notifyWidget(context)
                            pending.finish()
                        }
                    }
                    return
                }

                else -> {
                    val clazz = Class.forName(serviceClassName)
                    if (BaseTileService::class.java.isAssignableFrom(clazz)) {
                        val tileService =
                            clazz.getDeclaredConstructor().newInstance() as BaseTileService

                        val attachBaseContextMethod =
                            android.content.ContextWrapper::class.java.getDeclaredMethod(
                                "attachBaseContext",
                                Context::class.java,
                            )
                        attachBaseContextMethod.isAccessible = true
                        attachBaseContextMethod.invoke(tileService, context)

                        tileService.onTileClick()
                    } else {
                        fallbackLaunch(context)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("QsTileActionRouter", "Failed to trigger tile $serviceClassName headlessly", e)
            fallbackLaunch(context)
        }

        // Notify widget to update state after action
        notifyWidget(context)
    }

    private fun notifyWidget(context: Context) {
        val updateIntent =
            Intent("com.jawzf.pixeltoolkit.action.QS_TILES_WIDGET_UPDATE").apply {
                setPackage(context.packageName)
            }
        context.sendBroadcast(updateIntent)
    }

    private fun fallbackLaunch(context: Context) {
        try {
            val launchIntent =
                com.jawzf.pixeltoolkit.utils.AppIconUtil.mainIntent(context).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(launchIntent)
        } catch (e: Exception) {
            Log.e("QsTileActionRouter", "Failed to launch activity", e)
        }
    }

    companion object {
        const val ACTION_TRIGGER_TILE = "com.jawzf.pixeltoolkit.action.TRIGGER_QS_TILE"
        const val EXTRA_SERVICE_CLASS_NAME = "extra_service_class_name"
    }
}
