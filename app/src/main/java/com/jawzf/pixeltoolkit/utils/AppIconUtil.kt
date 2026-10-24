package com.jawzf.pixeltoolkit.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.jawzf.pixeltoolkit.domain.model.AppIcon

object AppIconUtil {
    fun mainIntent(context: Context): android.content.Intent =
        context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: android.content.Intent(context, com.jawzf.pixeltoolkit.MainActivity::class.java)

    fun setAppIcon(context: Context, targetIcon: AppIcon) {
        val packageManager = context.packageManager

        val targetComponent = ComponentName(context.packageName, targetIcon.aliasName)
        packageManager.setComponentEnabledSetting(
            targetComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP,
        )

        for (icon in AppIcon.entries) {
            if (icon != targetIcon) {
                val component = ComponentName(context.packageName, icon.aliasName)
                packageManager.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }
}
