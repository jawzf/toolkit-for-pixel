package com.jawzf.pixeltoolkit.feature.security

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.feature.security.screenshots.ScreenshotCleaner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Compose state for the security screens. */
class SecurityState(
    private val scope: CoroutineScope,
) {
    private lateinit var settings: SecuritySettings
    private var appContext: Context? = null

    val isScreenshotShareCleanupEnabled = mutableStateOf(false)
    val screenshotShareCleanupDelayMinutes = mutableIntStateOf(0)
    val isScreenshotAutoCleanEnabled = mutableStateOf(false)
    val screenshotAutoCleanDays = mutableIntStateOf(7)
    val screenshotsCleanedCount = mutableIntStateOf(0)
    val screenshotsCleanedBytes = mutableLongStateOf(0L)

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        appContext = context.applicationContext
        settings = SecuritySettings(context)
        isScreenshotShareCleanupEnabled.value = settings.isScreenshotShareCleanupEnabled()
        screenshotShareCleanupDelayMinutes.intValue = settings.getScreenshotShareCleanupDelayMinutes()
        isScreenshotAutoCleanEnabled.value = settings.isScreenshotAutoCleanEnabled()
        screenshotAutoCleanDays.intValue = settings.getScreenshotAutoCleanDays()
        screenshotsCleanedCount.intValue = settings.getScreenshotsCleanedCount()
        screenshotsCleanedBytes.longValue = settings.getScreenshotsCleanedBytes()
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        when (key) {
            SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_ENABLED ->
                isScreenshotShareCleanupEnabled.value = settings.isScreenshotShareCleanupEnabled()

            SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_DELAY ->
                screenshotShareCleanupDelayMinutes.intValue = settings.getScreenshotShareCleanupDelayMinutes()

            SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_ENABLED ->
                isScreenshotAutoCleanEnabled.value = settings.isScreenshotAutoCleanEnabled()

            SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_DAYS ->
                screenshotAutoCleanDays.intValue = settings.getScreenshotAutoCleanDays()

            SecuritySettings.KEY_SCREENSHOTS_CLEANED_COUNT ->
                screenshotsCleanedCount.intValue = settings.getScreenshotsCleanedCount()

            SecuritySettings.KEY_SCREENSHOTS_CLEANED_BYTES ->
                screenshotsCleanedBytes.longValue = settings.getScreenshotsCleanedBytes()

            else -> {}
        }
    }

    fun setScreenshotShareCleanupEnabled(enabled: Boolean) {
        settings.setScreenshotShareCleanupEnabled(enabled)
        isScreenshotShareCleanupEnabled.value = enabled
        if (!enabled) appContext?.let { ScreenshotCleaner.cancelAll(it, ScreenshotCleaner.TAG_SHARED) }
    }

    fun setScreenshotShareCleanupDelayMinutes(minutes: Int) {
        settings.setScreenshotShareCleanupDelayMinutes(minutes)
        screenshotShareCleanupDelayMinutes.intValue = minutes
    }

    fun setScreenshotAutoCleanEnabled(enabled: Boolean) {
        isScreenshotAutoCleanEnabled.value = enabled
        val context = appContext
        if (context == null) {
            settings.setScreenshotAutoCleanEnabled(enabled)
            return
        }
        scope.launch(Dispatchers.IO) {
            if (enabled) {
                settings.setScreenshotAutoCleanLastId(ScreenshotCleaner.latestScreenshotId(context))
            } else {
                ScreenshotCleaner.cancelAll(context, ScreenshotCleaner.TAG_AUTO_CLEAN)
            }
            settings.setScreenshotAutoCleanEnabled(enabled)
        }
    }

    fun setScreenshotAutoCleanDays(days: Int) {
        settings.setScreenshotAutoCleanDays(days)
        screenshotAutoCleanDays.intValue = days
    }
}
