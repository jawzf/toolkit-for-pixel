package com.jawzf.pixeltoolkit.core.data.security

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.KeyEvent
import android.widget.Toast
import com.jawzf.pixeltoolkit.core.data.action.ScreenOffMethod
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils

object DeviceLockUtils {
    const val KEY_LOCKDOWN_MODE = "lockdown_mode"

    /** The running screen-off accessibility service; set by the service itself. */
    @Volatile var accessibilityService: AccessibilityService? = null

    fun isLockdownEnabled(context: Context): Boolean =
        context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE).getBoolean(KEY_LOCKDOWN_MODE, false)

    fun performLockdownTileAction(
        context: Context,
        isLongPress: Boolean,
    ): Boolean {
        val isLockdownEnabled = isLockdownEnabled(context)
        val prefs = context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)

        val selectedScreenOffMethod =
            try {
                ScreenOffMethod.valueOf(
                    prefs.getString("screen_off_method", ScreenOffMethod.ACCESSIBILITY.name)
                        ?: ScreenOffMethod.ACCESSIBILITY.name,
                )
            } catch (e: Exception) {
                ScreenOffMethod.ACCESSIBILITY
            }

        val methodToExecute =
            if (!isLongPress) {
                if (isLockdownEnabled) ScreenOffMethod.DEVICE_ADMIN else selectedScreenOffMethod
            } else {
                if (isLockdownEnabled) selectedScreenOffMethod else ScreenOffMethod.DEVICE_ADMIN
            }

        return lockDevice(context, methodToExecute)
    }

    fun lockDevice(
        context: Context,
        method: ScreenOffMethod,
    ): Boolean {
        return when (method) {
            ScreenOffMethod.ACCESSIBILITY -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.msg_feature_not_supported),
                            Toast.LENGTH_SHORT,
                        ).show()
                    return false
                }
                if (!PermissionUtils.isAccessibilityServiceEnabled(context)) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.msg_missing_accessibility_permission),
                            Toast.LENGTH_SHORT,
                        ).show()
                    return false
                }
                val service = accessibilityService
                if (service != null) {
                    performAccessibilityLock(service)
                } else {
                    val serviceIntent =
                        Intent().setClassName(context, AppComponents.SCREEN_OFF_ACCESSIBILITY_SERVICE).apply {
                            action = "LOCK_SCREEN"
                        }
                    context.startService(serviceIntent)
                }
                true
            }

            ScreenOffMethod.DEVICE_ADMIN -> {
                if (!PermissionUtils.isDeviceAdminActive(context)) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.msg_device_admin_required_lockdown),
                            Toast.LENGTH_SHORT,
                        ).show()
                    return false
                }
                val dpm =
                    context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                dpm.lockNow()
                true
            }

            ScreenOffMethod.INPUT -> {
                if (!ShellUtils.hasPermission(context)) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.msg_shizuku_root_required_lock),
                            Toast.LENGTH_SHORT,
                        ).show()
                    return false
                }
                ShellUtils.runCommand(
                    context,
                    "input keyevent ${KeyEvent.KEYCODE_POWER}",
                    featureName = context.getString(R.string.diy_action_screen_off),
                )
                true
            }
        }
    }

    fun performAccessibilityLock(service: AccessibilityService): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
        } else {
            false
        }
}
