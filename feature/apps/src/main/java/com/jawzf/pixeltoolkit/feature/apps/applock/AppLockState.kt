package com.jawzf.pixeltoolkit.feature.apps.applock

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings

/** Compose state and actions for App lock. */
class AppLockState(
    /** Called when app lock turns on or off, so the app-detection service can start or stop. */
    private val onAppDetectionChanged: (Context) -> Unit,
) {
    private lateinit var settings: AppsSettings
    private var appContext: Context? = null

    val isAppLockEnabled = mutableStateOf(false)
    val appLockAutoLockDelayIndex = mutableIntStateOf(0)

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        appContext = context.applicationContext
        settings = AppsSettings(context)
        isAppLockEnabled.value =
            settings.getBoolean(AppsSettings.KEY_APP_LOCK_ENABLED)
        appLockAutoLockDelayIndex.intValue =
            settings.getInt(AppsSettings.KEY_APP_LOCK_AUTO_LOCK_DELAY_INDEX, 0)
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        if (key == AppsSettings.KEY_APP_LOCK_ENABLED) {
            isAppLockEnabled.value = settings.getBoolean(key)
            appContext?.let { onAppDetectionChanged(it) }
        }
    }

    /**
     * Executes the set app lock enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setAppLockEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isAppLockEnabled.value = enabled
        settings.putBoolean(AppsSettings.KEY_APP_LOCK_ENABLED, enabled)
        onAppDetectionChanged(context)
    }

    /**
     * Executes the set app lock auto lock delay index operation.
     *
     * @param index [Int] Target index.
     */
    fun setAppLockAutoLockDelayIndex(index: Int) {
        appLockAutoLockDelayIndex.intValue = index
        settings.putInt(AppsSettings.KEY_APP_LOCK_AUTO_LOCK_DELAY_INDEX, index)
    }

    fun saveAppLockSelectedApps(
        context: Context,
        apps: List<AppSelection>,
    ) {
        settings.saveAppLockSelectedApps(apps)
    }

    /**
     * Executes the load app lock selected apps operation.
     *
     * @param context [Context] Target context.
     * @return The resulting List<AppSelection> data.
     */
    fun loadAppLockSelectedApps(context: Context): List<AppSelection> = settings.loadAppLockSelectedApps()

    /**
     * Executes the update app lock app enabled operation.
     *
     * @param context [Context] Target context.
     * @param packageName [String] Target package name.
     * @param enabled [Boolean] Target enabled.
     */
    fun updateAppLockAppEnabled(
        context: Context,
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateAppLockAppSelection(packageName, enabled)
    }
}
