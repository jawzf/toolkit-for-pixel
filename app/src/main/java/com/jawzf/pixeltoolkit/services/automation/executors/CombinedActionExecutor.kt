package com.jawzf.pixeltoolkit.services.automation.executors

import android.app.ActivityManager
import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionExecutor
import com.jawzf.pixeltoolkit.core.data.action.ActionHandlers
import com.jawzf.pixeltoolkit.core.data.action.ActionRunner
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.glance.StatusGlanceSettings
import com.jawzf.pixeltoolkit.core.data.power.CaffeinateController
import com.jawzf.pixeltoolkit.core.data.power.ChargingModeUtil
import com.jawzf.pixeltoolkit.core.data.security.DeviceLockUtils
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.haptics.performHapticFeedback
import com.jawzf.pixeltoolkit.core.platform.device.ZenModeController
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.feature.automation.diy.DIYRepository
import com.jawzf.pixeltoolkit.feature.automation.diy.displayName
import com.jawzf.pixeltoolkit.feature.automation.engine.AutomationFailureNotifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

object CombinedActionExecutor : ActionExecutor {
    override fun displayName(
        context: Context,
        action: Action,
    ): String = action.displayName(context)

    private suspend fun vibrateModeHaptic(context: Context) {
        kotlinx.coroutines.delay(350)
        HapticUtil.performStrongDoubleHaptic(context)
    }

    // Runs actions in order with a short gap, as Button remap does, so each can take effect
    override suspend fun executeAll(
        context: Context,
        actions: List<Action>,
    ) {
        actions.forEachIndexed { index, action ->
            if (index > 0) delay(ACTION_SEQUENCE_GAP_MS)
            val missing = AutomationFailureNotifier.missingPermissions(context, action)
            if (missing.isNotEmpty()) {
                Log.e("CombinedActionExecutor", "Action ${action::class.simpleName} missing permissions")
                AutomationFailureNotifier.notifyMissingPermissions(context, action, missing)
                return@forEachIndexed
            }
            try {
                execute(context, action, fromAutomation = true)
            } catch (e: Exception) {
                Log.e("CombinedActionExecutor", "Action ${action::class.simpleName} failed", e)
                AutomationFailureNotifier.notifyFailure(context, action, e.message ?: e::class.java.simpleName)
            }
        }
    }

    const val ACTION_SEQUENCE_GAP_MS = ActionRunner.ACTION_SEQUENCE_GAP_MS

    override suspend fun execute(
        context: Context,
        action: Action,
        fromAutomation: Boolean,
    ) {
        withContext(Dispatchers.Main) {
            if (ActionHandlers.handle(context, action)) return@withContext
            when (action) {
                is Action.TurnOnLowPower -> setLowPowerMode(context, true)
                is Action.TurnOnCaffeinate -> CaffeinateController.start(context)
                is Action.TurnOffCaffeinate -> CaffeinateController.cancelAll(context)
                is Action.Wait -> delay(action.seconds * 1000L)
                is Action.RunMacro -> runMacro(context, action)
                is Action.TurnOffLowPower -> setLowPowerMode(context, false)
                is Action.SetChargingMode ->
                    ChargingModeUtil
                        .setMode(context, action.mode)
                is Action.HapticVibration -> {
                    HapticUtil.performCustomHaptic(context, 0.6f)
                }

                is Action.TurnOnFlashlight -> toggleFlashlight(context, true)
                is Action.TurnOffFlashlight -> toggleFlashlight(context, false)
                is Action.ToggleFlashlight -> {
                    val camManager =
                        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                    try {
                        camManager.cameraIdList[0]
                        camManager.registerTorchCallback(
                            object : CameraManager.TorchCallback() {
                                override fun onTorchModeChanged(
                                    cameraId: String,
                                    enabled: Boolean,
                                ) {
                                    super.onTorchModeChanged(cameraId, enabled)
                                    camManager.unregisterTorchCallback(this)
                                    try {
                                        camManager.setTorchMode(cameraId, !enabled)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            },
                            null,
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                is Action.DimWallpaper -> {
                    ShellUtils.runCommand(
                        context,
                        "cmd wallpaper set-dim-amount ${action.dimAmount}",
                        featureName = context.getString(action.title),
                    )
                }

                is Action.DeviceEffects -> {
                    if (Build.VERSION.SDK_INT >= 35) { // Android 15+
                        val nm =
                            context.getSystemService(android.app.NotificationManager::class.java)
                        if (nm.isNotificationPolicyAccessGranted) {
                            try {
                                if (action.enabled) {
                                    // ENABLE/UPDATE EFFECTS
                                    val effectsBuilder =
                                        try {
                                            android.service.notification.ZenDeviceEffects
                                                .Builder()
                                        } catch (e: NoSuchMethodError) {
                                            try {
                                                val constructor =
                                                    android.service.notification.ZenDeviceEffects.Builder::class.java.getConstructor(
                                                        android.service.notification.ZenDeviceEffects::class.java,
                                                    )
                                                constructor.newInstance(null)
                                            } catch (refE: Exception) {
                                                null
                                            }
                                        } ?: return@withContext

                                    effectsBuilder
                                        .setShouldDisplayGrayscale(action.grayscale)
                                        .setShouldSuppressAmbientDisplay(action.suppressAmbient)
                                        .setShouldDimWallpaper(action.dimWallpaper)
                                        .setShouldUseNightMode(action.nightMode)

                                    val effects = effectsBuilder.build()

                                    val existingRule =
                                        nm.automaticZenRules.values.find { it.name == "Toolkit Focus" }
                                    val ruleKey =
                                        existingRule?.let {
                                            nm.automaticZenRules.entries
                                                .find { entry -> entry.value == it }
                                                ?.key
                                        }

                                    val componentName =
                                        android.content.ComponentName(
                                            context,
                                            com.jawzf.pixeltoolkit.services.ToolkitConditionProvider::class.java,
                                        )
                                    val conditionUri =
                                        com.jawzf.pixeltoolkit.services.ToolkitConditionProvider.CONDITION_URI

                                    val ruleBuilder =
                                        android.app.AutomaticZenRule
                                            .Builder(
                                                "Toolkit Focus",
                                                conditionUri,
                                            ).setOwner(componentName)
                                            .setDeviceEffects(effects)
                                            .setInterruptionFilter(android.app.NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                                            .setZenPolicy(
                                                android.service.notification.ZenPolicy
                                                    .Builder()
                                                    .allowAlarms(true)
                                                    .build(),
                                            ).setConditionId(conditionUri)
                                            .setConfigurationActivity(
                                                android.content.ComponentName(
                                                    context,
                                                    com.jawzf.pixeltoolkit.MainActivity::class.java,
                                                ),
                                            )

                                    if (ruleKey != null) {
                                        nm.updateAutomaticZenRule(ruleKey, ruleBuilder.build())
                                    } else {
                                        nm.addAutomaticZenRule(ruleBuilder.build())
                                    }

                                    // Trigger the condition to be TRUE
                                    com.jawzf.pixeltoolkit.services.ToolkitConditionProvider.setConditionState(
                                        context,
                                        true,
                                    )

                                    android.util.Log.d(
                                        "DeviceEffects",
                                        "Updated ZenRule for Device Effects",
                                    )
                                } else {
                                    // DISABLE EFFECTS
                                    val existingRuleEntry =
                                        nm.automaticZenRules.entries.find { it.value.name == "Toolkit Focus" }
                                    existingRuleEntry?.let { entry ->
                                        val rule = entry.value
                                        rule.isEnabled = false
                                        nm.updateAutomaticZenRule(entry.key, rule)
                                    }
                                    // Also notify condition false just in case
                                    com.jawzf.pixeltoolkit.services.ToolkitConditionProvider.setConditionState(
                                        context,
                                        false,
                                    )

                                    android.util.Log.d(
                                        "DeviceEffects",
                                        "Disabled ZenRule for Device Effects",
                                    )
                                }
                            } catch (e: Throwable) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

                is Action.SoundMode -> {
                    val audioManager =
                        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val ringerMode =
                        when (action.mode) {
                            Action.SoundModeType.SOUND -> AudioManager.RINGER_MODE_NORMAL
                            Action.SoundModeType.VIBRATE -> AudioManager.RINGER_MODE_VIBRATE
                            Action.SoundModeType.SILENT -> AudioManager.RINGER_MODE_SILENT
                        }
                    try {
                        audioManager.ringerMode = ringerMode
                        if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) vibrateModeHaptic(context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                is Action.ScreenOff -> {
                    val vibrator =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val manager =
                                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as android.os.VibratorManager
                            manager.defaultVibrator
                        } else {
                            @Suppress("DEPRECATION")
                            context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
                        }
                    if (action.haptic != HapticFeedbackType.NONE) {
                        performHapticFeedback(vibrator, action.haptic)
                    }

                    DeviceLockUtils.lockDevice(context, action.method)
                }

                is Action.MediaPlayPause -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.dispatchMediaKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                        ),
                    )
                    am.dispatchMediaKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
                        ),
                    )
                }

                is Action.MediaNext -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.dispatchMediaKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_MEDIA_NEXT,
                        ),
                    )
                    am.dispatchMediaKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_MEDIA_NEXT,
                        ),
                    )
                }

                is Action.MediaPrevious -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    am.dispatchMediaKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_DOWN,
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                        ),
                    )
                    am.dispatchMediaKeyEvent(
                        KeyEvent(
                            KeyEvent.ACTION_UP,
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
                        ),
                    )
                }

                is Action.AIAssistant -> {
                    try {
                        val intent =
                            Intent(Intent.ACTION_ASSIST).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                is Action.ToggleMediaVolume -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val prefs =
                        context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)

                    if (currentVolume > 0) {
                        prefs.edit().putInt("last_media_volume", currentVolume).apply()
                        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_SHOW_UI)
                    } else {
                        val lastVolume =
                            prefs.getInt(
                                "last_media_volume",
                                am.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2,
                            )
                        am.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            lastVolume,
                            AudioManager.FLAG_SHOW_UI,
                        )
                    }
                }

                is Action.SetVolume -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val streamType =
                        when (action.channel) {
                            Action.VolumeChannel.MUSIC -> AudioManager.STREAM_MUSIC
                            Action.VolumeChannel.RING -> AudioManager.STREAM_RING
                            Action.VolumeChannel.ALARM -> AudioManager.STREAM_ALARM
                            Action.VolumeChannel.CALL -> AudioManager.STREAM_VOICE_CALL
                            Action.VolumeChannel.NOTIFICATION -> AudioManager.STREAM_NOTIFICATION
                            Action.VolumeChannel.SYSTEM -> AudioManager.STREAM_SYSTEM
                        }
                    val max = am.getStreamMaxVolume(streamType)
                    val target = (action.level / 100f * max).toInt().coerceIn(0, max)
                    am.setStreamVolume(streamType, target, AudioManager.FLAG_SHOW_UI)
                }

                is Action.SetMode -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                        try {
                            setMode(context, action)
                        } catch (e: Exception) {
                            if (fromAutomation) throw e
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                is Action.ToggleDoNotDisturb -> {
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    if (nm.isNotificationPolicyAccessGranted) {
                        nm.setInterruptionFilter(
                            if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                                NotificationManager.INTERRUPTION_FILTER_PRIORITY
                            } else {
                                NotificationManager.INTERRUPTION_FILTER_ALL
                            },
                        )
                    }
                }

                is Action.OpenCamera -> {
                    // The secure camera opens over the lock screen without unlocking, like the Pixel shortcut
                    val isLocked =
                        (context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager)
                            .isKeyguardLocked
                    val cameraAction =
                        if (isLocked) {
                            MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE
                        } else {
                            MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA
                        }
                    try {
                        context.startActivity(Intent(cameraAction).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                is Action.OpenQrScanner -> openQrScanner(context)

                is Action.OpenVideoCamera -> {
                    try {
                        context.startActivity(
                            Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                is Action.ToggleMute -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    if (nm.isNotificationPolicyAccessGranted) {
                        try {
                            am.ringerMode =
                                if (am.ringerMode == AudioManager.RINGER_MODE_SILENT) {
                                    AudioManager.RINGER_MODE_NORMAL
                                } else {
                                    AudioManager.RINGER_MODE_SILENT
                                }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                is Action.ToggleVibrate -> {
                    val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    if (nm.isNotificationPolicyAccessGranted) {
                        try {
                            val enteringVibrate = am.ringerMode != AudioManager.RINGER_MODE_VIBRATE
                            am.ringerMode =
                                if (enteringVibrate) AudioManager.RINGER_MODE_VIBRATE else AudioManager.RINGER_MODE_NORMAL
                            if (enteringVibrate) vibrateModeHaptic(context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                is Action.LikeCurrentSong -> {
                    context.sendBroadcast(
                        Intent("com.jawzf.pixeltoolkit.ACTION_LIKE_CURRENT_SONG").setPackage(
                            context.packageName,
                        ),
                    )
                }

                is Action.OpenApp -> {
                    try {
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(action.packageName)
                        if (launchIntent != null) {
                            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(launchIntent)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                is Action.OpenActivity -> openActivity(context, action)

                is Action.TurnOnDuo -> DuoSettings(context).setDuoEnabled(true)
                is Action.TurnOffDuo -> DuoSettings(context).setDuoEnabled(false)
                is Action.ToggleDuo -> DuoSettings(context).let { it.setDuoEnabled(!it.isDuoEnabled()) }
                is Action.TurnOnStatusGlance -> StatusGlanceSettings(context).setStatusGlanceEnabled(true)
                is Action.TurnOffStatusGlance -> StatusGlanceSettings(context).setStatusGlanceEnabled(false)
                is Action.ToggleStatusGlance -> StatusGlanceSettings(context).let { it.setStatusGlanceEnabled(!it.isStatusGlanceEnabled()) }

                is Action.PinApp -> {
                    try {
                        val useActivityTask = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        val serviceName =
                            if (useActivityTask) "activity_task" else Context.ACTIVITY_SERVICE
                        val stubClassName =
                            if (useActivityTask) "android.app.IActivityTaskManager\$Stub" else "android.app.IActivityManager\$Stub"
                        val interfaceClassName =
                            if (useActivityTask) "android.app.IActivityTaskManager" else "android.app.IActivityManager"

                        val binder = SystemServiceHelper.getSystemService(serviceName)
                        val stubClass = Class.forName(stubClassName)
                        val interfaceClass = Class.forName(interfaceClassName)

                        val serviceInstance =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                org.lsposed.hiddenapibypass.HiddenApiBypass.invoke(
                                    stubClass,
                                    null,
                                    "asInterface",
                                    ShizukuBinderWrapper(binder),
                                )
                            } else {
                                val asInterfaceMethod =
                                    stubClass.getMethod("asInterface", android.os.IBinder::class.java)
                                asInterfaceMethod.invoke(null, ShizukuBinderWrapper(binder))
                            }

                        val tasks =
                            if (useActivityTask) {
                                org.lsposed.hiddenapibypass.HiddenApiBypass.invoke(
                                    interfaceClass,
                                    serviceInstance,
                                    "getTasks",
                                    5,
                                    false,
                                    false,
                                    0,
                                ) as List<*>
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    org.lsposed.hiddenapibypass.HiddenApiBypass.invoke(
                                        interfaceClass,
                                        serviceInstance,
                                        "getTasks",
                                        5,
                                        0,
                                    ) as List<*>
                                } else {
                                    val getTasksMethod =
                                        interfaceClass.getMethod(
                                            "getTasks",
                                            Int::class.javaPrimitiveType,
                                            Int::class.javaPrimitiveType,
                                        )
                                    getTasksMethod.invoke(serviceInstance, 5, 0) as List<*>
                                }
                            }

                        var targetTaskId = -1
                        for (task in tasks) {
                            val taskInfo = task as? ActivityManager.RunningTaskInfo ?: continue
                            val topActivity = taskInfo.topActivity
                            if (topActivity != null && topActivity.packageName != context.packageName) {
                                targetTaskId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) taskInfo.taskId else @Suppress("DEPRECATION") taskInfo.id
                                break
                            }
                        }

                        if (targetTaskId == -1 && tasks.isNotEmpty()) {
                            val firstTask = tasks.firstOrNull() as? ActivityManager.RunningTaskInfo
                            if (firstTask != null) {
                                targetTaskId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) firstTask.taskId else @Suppress("DEPRECATION") firstTask.id
                            }
                        }

                        if (targetTaskId != -1) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                org.lsposed.hiddenapibypass.HiddenApiBypass.invoke(
                                    interfaceClass,
                                    serviceInstance,
                                    "startSystemLockTaskMode",
                                    targetTaskId,
                                )
                            } else {
                                val startLockTaskMethod =
                                    interfaceClass.getMethod(
                                        "startSystemLockTaskMode",
                                        Int::class.javaPrimitiveType,
                                    )
                                startLockTaskMethod.invoke(serviceInstance, targetTaskId)
                            }
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    "No active foreground task found",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast
                            .makeText(
                                context,
                                "Failed to pin app: ${e.message}",
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }
                is Action.TurnOnWifi -> setWifiEnabled(context, true)
                is Action.TurnOffWifi -> setWifiEnabled(context, false)
                is Action.ToggleWifi ->
                    isWifiEnabled(context)?.let { setWifiEnabled(context, !it) }
                        ?: showStateUnknown(context, R.string.diy_action_wifi_toggle)
                is Action.TurnOnCellularData -> setCellularDataEnabled(context, true)
                is Action.TurnOffCellularData -> setCellularDataEnabled(context, false)
                is Action.ToggleCellularData ->
                    isCellularDataEnabled(context)?.let { setCellularDataEnabled(context, !it) }
                        ?: showStateUnknown(context, R.string.diy_action_cellular_toggle)
                is Action.TurnOnAutoBrightness -> setAutoBrightnessEnabled(context, true)
                is Action.TurnOffAutoBrightness -> setAutoBrightnessEnabled(context, false)
                is Action.ToggleAutoBrightness -> setAutoBrightnessEnabled(context, !isAutoBrightnessEnabled(context))
                is Action.Keyboard -> {
                    try {
                        if (PermissionUtils.canWriteSecureSettings(context)) {
                            if (!action.inputMethodId.isNullOrBlank()) {
                                Settings.Secure.putString(
                                    context.contentResolver,
                                    Settings.Secure.DEFAULT_INPUT_METHOD,
                                    action.inputMethodId,
                                )
                            }
                            return@withContext
                        }
                        Toast
                            .makeText(
                                context,
                                R.string.diy_set_keyboard_permission_required,
                                Toast.LENGTH_SHORT,
                            ).show()
                    } catch (e: Exception) {
                        Toast
                            .makeText(
                                context,
                                context.getString(
                                    R.string.diy_set_keyboard_switch_failed,
                                    e.message ?: "",
                                ),
                                Toast.LENGTH_SHORT,
                            ).show()
                    }
                }

                is Action.CustomSettings -> {
                    val resolver = context.contentResolver
                    for (entry in action.entries) {
                        val success =
                            try {
                                when (entry.table) {
                                    Action.SettingsTable.SYSTEM -> Settings.System.putString(resolver, entry.key, entry.value)
                                    Action.SettingsTable.SECURE -> Settings.Secure.putString(resolver, entry.key, entry.value)
                                    Action.SettingsTable.GLOBAL -> Settings.Global.putString(resolver, entry.key, entry.value)
                                }
                            } catch (e: Exception) {
                                false
                            }
                        if (!success) {
                            val tableArg =
                                when (entry.table) {
                                    Action.SettingsTable.SYSTEM -> "system"
                                    Action.SettingsTable.SECURE -> "secure"
                                    Action.SettingsTable.GLOBAL -> "global"
                                }
                            val safeValue = if (entry.value.contains(" ")) "\"${entry.value}\"" else entry.value
                            ShellUtils.runCommand(
                                context,
                                "settings put $tableArg ${entry.key} $safeValue",
                                featureName = context.getString(action.title),
                            )
                        }
                    }
                }
                else -> Unit // Feature-owned actions run in ActionHandlers above.
            }
        }
    }

    private suspend fun runMacro(
        context: Context,
        action: Action.RunMacro,
    ) {
        DIYRepository.init(context)
        val macro = DIYRepository.getMacro(action.macroId)
        if (macro == null) {
            AutomationFailureNotifier.notifyFailure(context, action, context.getString(R.string.diy_macro_not_found))
            return
        }
        // Macros can't contain macros; skipping them rules out loops from edited JSON
        executeAll(context, macro.actionList.filterNot { it is Action.RunMacro })
    }

    private fun toggleFlashlight(
        context: Context,
        on: Boolean,
    ) {
        val camManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = camManager.cameraIdList[0]
            camManager.setTorchMode(cameraId, on)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private suspend fun setMode(
        context: Context,
        action: Action.SetMode,
    ) {
        val featureName = context.getString(R.string.diy_action_set_mode)
        if (!ShizukuUtils.isShizukuAvailable()) error(context.getString(R.string.shizuku_not_running_feature_desc, featureName))
        if (!ShizukuUtils.hasPermission()) error(context.getString(R.string.shizuku_permission_missing_feature_desc, featureName))
        val result =
            try {
                withContext(Dispatchers.IO) { ZenModeController.setActive(action.modeId, action.modeName, action.active) }
            } catch (e: Exception) {
                Log.e("CombinedActionExecutor", "Set Mode failed", e)
                error(context.getString(R.string.diy_set_mode_failed, action.modeName, "${e::class.java.simpleName}: ${e.message}"))
            }
        when (result) {
            ZenModeController.Result.DONE -> {}
            ZenModeController.Result.NOT_FOUND -> error(context.getString(R.string.diy_set_mode_not_found, action.modeName))
            ZenModeController.Result.NO_CONDITION -> error(context.getString(R.string.diy_set_mode_no_condition, action.modeName))
        }
    }

    private fun setLowPowerMode(
        context: Context,
        on: Boolean,
    ) {
        val value = if (on) 1 else 0
        try {
            android.provider.Settings.Global
                .putInt(context.contentResolver, "low_power", value)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isWifiEnabled(context: Context): Boolean? =
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            when (wifiManager.wifiState) {
                WifiManager.WIFI_STATE_ENABLED, WifiManager.WIFI_STATE_ENABLING -> true
                WifiManager.WIFI_STATE_DISABLED, WifiManager.WIFI_STATE_DISABLING -> false
                else -> null
            }
        } catch (_: Exception) {
            null
        }

    private fun setWifiEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        val state = if (enabled) "enable" else "disable"
        ShellUtils.runCommand(
            context,
            "svc wifi $state",
            featureName = context.getString(if (enabled) R.string.diy_action_wifi_on else R.string.diy_action_wifi_off),
        )
    }

    @Suppress("MissingPermission")
    private fun isCellularDataEnabled(context: Context): Boolean? =
        try {
            (context.applicationContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).isDataEnabled
        } catch (_: Exception) {
            readMobileDataSetting(context)
        }

    private fun readMobileDataSetting(context: Context): Boolean? {
        val subId = SubscriptionManager.getDefaultDataSubscriptionId()
        val keys =
            buildList {
                if (subId != SubscriptionManager.INVALID_SUBSCRIPTION_ID) add("mobile_data$subId")
                add("mobile_data")
            }
        for (key in keys) {
            val value =
                try {
                    Settings.Global.getInt(context.contentResolver, key, -1)
                } catch (_: Exception) {
                    -1
                }
            if (value == 1) return true
            if (value == 0) return false
        }
        return null
    }

    private fun showStateUnknown(
        context: Context,
        actionTitle: Int,
    ) {
        android.util.Log.w("CombinedActionExecutor", "Could not read current state for ${context.getString(actionTitle)}")
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            Toast
                .makeText(
                    context,
                    context.getString(R.string.diy_action_state_unknown, context.getString(actionTitle)),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    private fun setCellularDataEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        val state = if (enabled) "enable" else "disable"
        ShellUtils.runCommand(
            context,
            "svc data $state",
            featureName = context.getString(if (enabled) R.string.diy_action_cellular_on else R.string.diy_action_cellular_off),
        )
    }

    private fun isAutoBrightnessEnabled(context: Context): Boolean =
        android.provider.Settings.System.getInt(
            context.contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
            android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL,
        ) == android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC

    private fun setAutoBrightnessEnabled(
        context: Context,
        enabled: Boolean,
    ) {
        val value = if (enabled) 1 else 0
        try {
            android.provider.Settings.System.putInt(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE,
                value,
            )
        } catch (e: Exception) {
            ShellUtils.runCommand(
                context,
                "settings put system screen_brightness_mode $value",
                featureName = context.getString(if (enabled) R.string.diy_action_auto_brightness_on else R.string.diy_action_auto_brightness_off),
            )
        }
    }

    private fun openActivity(
        context: Context,
        action: Action.OpenActivity,
    ) {
        if (action.packageName.isBlank() || action.className.isBlank()) return
        val component = ComponentName(action.packageName, action.className)
        // Single quotes keep the shell from expanding the $ in nested class names
        val startCommand = "am start -n '${component.flattenToShortString()}'"
        if (action.requiresRoot) {
            if (ShellUtils.isRootEnabled(context)) {
                ShellUtils.runCommand(context, startCommand, featureName = context.getString(action.title))
            } else {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, R.string.activity_picker_root_required_toast, Toast.LENGTH_SHORT).show()
                }
            }
            return
        }
        try {
            context.startActivity(Intent().setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            // Exported activities can still require a permission we lack
            if (ShellUtils.isRootEnabled(context)) {
                ShellUtils.runCommand(context, startCommand)
            } else {
                e.printStackTrace()
            }
        }
    }

    // Same scanner System UI uses for its lock screen and Quick Settings QR shortcuts
    private fun openQrScanner(context: Context) {
        val configId =
            Resources
                .getSystem()
                .getIdentifier("config_defaultQrCodeComponent", "string", "android")
        val component =
            if (configId != 0) {
                Resources
                    .getSystem()
                    .getString(configId)
                    .takeIf { it.isNotBlank() }
                    ?.let { ComponentName.unflattenFromString(it) }
            } else {
                null
            }
        // Google's platform scanner closes itself for ordinary app callers, so start it as the shell user
        if (component != null && ShellUtils.isAvailable(context) && ShellUtils.hasPermission(context)) {
            ShellUtils.runCommand(
                context,
                "am start -n ${component.flattenToShortString()}",
                featureName = context.getString(Action.OpenQrScanner.title),
            )
            return
        }
        val intent =
            if (component != null) {
                Intent().setComponent(component)
            } else {
                Intent("com.google.android.gms.mlkit_barcode_ui.SCAN_QR_CODE")
            }
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
