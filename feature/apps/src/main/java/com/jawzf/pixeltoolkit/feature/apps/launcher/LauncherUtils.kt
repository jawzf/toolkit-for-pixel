package com.jawzf.pixeltoolkit.feature.apps.launcher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.edit

object LauncherUtils {
    const val KEY_CYCLE_LAUNCHERS = "default_launcher_cycle_packages"

    private fun homeIntent() = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)

    fun installedLaunchers(context: Context): List<String> =
        context.packageManager
            .queryIntentActivities(homeIntent(), PackageManager.MATCH_ALL)
            .filter { !it.activityInfo.name.contains("FallbackHome", ignoreCase = true) }
            .map { it.activityInfo.packageName }
            .filter { it != context.packageName && it != "android" && it != "com.android.settings" }
            .distinct()
            .sorted()

    fun currentLauncher(context: Context): String? =
        context.packageManager
            .resolveActivity(homeIntent(), PackageManager.MATCH_DEFAULT_ONLY)
            ?.activityInfo
            ?.packageName
            ?.takeIf { it != "android" && it != "com.android.settings" }

    fun label(context: Context, pkg: String): String =
        try {
            context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (_: Exception) {
            pkg
        }

    fun cycleLaunchers(context: Context): List<String> {
        val installed = installedLaunchers(context)
        val saved =
            context
                .getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
                .getString(KEY_CYCLE_LAUNCHERS, null)
                ?.split(",")
                ?.filter { it.isNotBlank() && it in installed }
        return if (saved.isNullOrEmpty()) installed else saved
    }

    fun saveCycleLaunchers(context: Context, packages: List<String>) {
        context
            .getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
            .edit { putString(KEY_CYCLE_LAUNCHERS, packages.joinToString(",")) }
    }
}
