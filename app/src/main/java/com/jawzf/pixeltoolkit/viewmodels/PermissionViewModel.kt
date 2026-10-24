package com.jawzf.pixeltoolkit.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jawzf.pixeltoolkit.utils.PermissionUtils
import com.jawzf.pixeltoolkit.utils.ShizukuUtils

class PermissionViewModel : ViewModel() {
    val isAccessibilityEnabled = mutableStateOf(false)
    val isWidgetEnabled = mutableStateOf(false)
    val isStatusBarIconControlEnabled = mutableStateOf(false)
    val isWriteSecureSettingsEnabled = mutableStateOf(false)
    val isReadPhoneStateEnabled = mutableStateOf(false)
    val isPostNotificationsEnabled = mutableStateOf(false)
    val isShizukuPermissionGranted = mutableStateOf(false)
    val isShizukuAvailable = mutableStateOf(false)
    val isNotificationListenerEnabled = mutableStateOf(false)
    val isOverlayPermissionGranted = mutableStateOf(false)
    val isNotificationLightingAccessibilityEnabled = mutableStateOf(false)

    /**
     * Refreshes all system and app permissions status.
     *
     * @param context [Context] Application context used for checking system services and permissions.
     */
    fun refreshPermissions(context: Context) {
        isAccessibilityEnabled.value = PermissionUtils.isAccessibilityServiceEnabled(context)
        isWriteSecureSettingsEnabled.value = PermissionUtils.canWriteSecureSettings(context)
        isReadPhoneStateEnabled.value = PermissionUtils.hasReadPhoneStatePermission(context)
        isPostNotificationsEnabled.value = PermissionUtils.isPostNotificationsEnabled(context)
        isShizukuAvailable.value = ShizukuUtils.isShizukuAvailable()
        isShizukuPermissionGranted.value = ShizukuUtils.hasPermission()
        isNotificationListenerEnabled.value =
            PermissionUtils.hasNotificationListenerPermission(context)
        isOverlayPermissionGranted.value = PermissionUtils.canDrawOverlays(context)
        isNotificationLightingAccessibilityEnabled.value =
            PermissionUtils.isNotificationLightingAccessibilityServiceEnabled(context)
    }
}
