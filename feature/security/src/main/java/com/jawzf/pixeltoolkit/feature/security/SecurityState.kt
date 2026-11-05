package com.jawzf.pixeltoolkit.feature.security

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
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
    private val onStatusBarRestore: (context: Context, requesterId: String) -> Unit,
) {
    private lateinit var settings: SecuritySettings
    private var appContext: Context? = null

    val isScreenshotShareCleanupEnabled = mutableStateOf(false)
    val screenshotShareCleanupDelayMinutes = mutableIntStateOf(0)
    val isScreenshotAutoCleanEnabled = mutableStateOf(false)
    val screenshotAutoCleanDays = mutableIntStateOf(7)
    val screenshotsCleanedCount = mutableIntStateOf(0)
    val screenshotsCleanedBytes = mutableLongStateOf(0L)
    val isFaceUnlockBrightnessEnabled = mutableStateOf(false)
    val faceUnlockMaxBrightness = mutableIntStateOf(100)
    val faceUnlockAmbientThreshold = mutableFloatStateOf(10f)
    val isFaceUnlockTriggerUnlock = mutableStateOf(true)
    val isFaceUnlockAutoIlluminate = mutableStateOf(false)
    val isFaceUnlockLightTint = mutableStateOf(false)
    val isScreenLockedSecurityEnabled = mutableStateOf(false)
    val isDisableNotificationInteractions = mutableStateOf(false)
    val isScreenLockedDisableOnExtendedUnlock = mutableStateOf(false)

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        appContext = context.applicationContext
        settings = SecuritySettings(context)
        readAll()
    }

    private fun readAll() {
        isScreenshotShareCleanupEnabled.value = settings.isScreenshotShareCleanupEnabled()
        screenshotShareCleanupDelayMinutes.intValue = settings.getScreenshotShareCleanupDelayMinutes()
        isScreenshotAutoCleanEnabled.value = settings.isScreenshotAutoCleanEnabled()
        screenshotAutoCleanDays.intValue = settings.getScreenshotAutoCleanDays()
        screenshotsCleanedCount.intValue = settings.getScreenshotsCleanedCount()
        screenshotsCleanedBytes.longValue = settings.getScreenshotsCleanedBytes()
        isFaceUnlockBrightnessEnabled.value = settings.isFaceUnlockBrightnessEnabled()
        faceUnlockMaxBrightness.intValue = settings.getFaceUnlockMaxBrightness()
        faceUnlockAmbientThreshold.floatValue = settings.getFaceUnlockAmbientThreshold()
        isFaceUnlockTriggerUnlock.value = settings.isFaceUnlockTriggerUnlockEnabled()
        isFaceUnlockAutoIlluminate.value = settings.isFaceUnlockAutoIlluminateEnabled()
        isFaceUnlockLightTint.value = settings.isFaceUnlockLightTintEnabled()
        isScreenLockedSecurityEnabled.value =
            settings.getBoolean(SecuritySettings.KEY_SCREEN_LOCKED_SECURITY_ENABLED)
        isDisableNotificationInteractions.value =
            settings.getBoolean(
                SecuritySettings.KEY_SCREEN_LOCKED_DISABLE_NOTIFICATION_INTERACTIONS,
                false,
            )
        isScreenLockedDisableOnExtendedUnlock.value =
            settings.getBoolean(SecuritySettings.KEY_SCREEN_LOCKED_DISABLE_ON_EXTENDED_UNLOCK, false)
    }

    /** Re-reads the state when one of its preference keys changes; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        if (key in PREFERENCE_KEYS) readAll()
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

    fun setFaceUnlockBrightnessEnabled(enabled: Boolean) {
        settings.setFaceUnlockBrightnessEnabled(enabled)
        isFaceUnlockBrightnessEnabled.value = enabled
    }

    fun setFaceUnlockAmbientThreshold(value: Float) {
        settings.setFaceUnlockAmbientThreshold(value)
        faceUnlockAmbientThreshold.floatValue = value
    }

    fun setFaceUnlockMaxBrightness(value: Int) {
        settings.setFaceUnlockMaxBrightness(value)
        faceUnlockMaxBrightness.intValue = value
    }

    fun setFaceUnlockAutoIlluminate(enabled: Boolean) {
        settings.setFaceUnlockAutoIlluminateEnabled(enabled)
        isFaceUnlockAutoIlluminate.value = enabled
    }

    fun setFaceUnlockTriggerUnlock(enabled: Boolean) {
        settings.setFaceUnlockTriggerUnlockEnabled(enabled)
        isFaceUnlockTriggerUnlock.value = enabled
    }

    fun setFaceUnlockLightTint(enabled: Boolean) {
        settings.setFaceUnlockLightTintEnabled(enabled)
        isFaceUnlockLightTint.value = enabled
    }

    /**
     * Executes the set screen locked security enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setScreenLockedSecurityEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isScreenLockedSecurityEnabled.value = enabled
        settings.putBoolean(
            SecuritySettings.KEY_SCREEN_LOCKED_SECURITY_ENABLED,
            enabled,
        )
        if (!enabled) {
            onStatusBarRestore(
                context,
                REQUESTER_QS,
            )
        }
    }

    fun setScreenLockedDisableOnExtendedUnlock(enabled: Boolean) {
        isScreenLockedDisableOnExtendedUnlock.value = enabled
        settings.putBoolean(SecuritySettings.KEY_SCREEN_LOCKED_DISABLE_ON_EXTENDED_UNLOCK, enabled)
    }

    fun setDisableNotificationInteractions(
        enabled: Boolean,
        context: Context,
    ) {
        isDisableNotificationInteractions.value = enabled
        settings.putBoolean(
            SecuritySettings.KEY_SCREEN_LOCKED_DISABLE_NOTIFICATION_INTERACTIONS,
            enabled,
        )
        if (!enabled) {
            onStatusBarRestore(
                context,
                REQUESTER_NOTIFICATION_INTERACTIONS,
            )
        }
    }

    companion object {
        private val PREFERENCE_KEYS =
            setOf(
                SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_ENABLED,
                SecuritySettings.KEY_SCREENSHOT_SHARE_CLEANUP_DELAY,
                SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_ENABLED,
                SecuritySettings.KEY_SCREENSHOT_AUTO_CLEAN_DAYS,
                SecuritySettings.KEY_SCREENSHOTS_CLEANED_COUNT,
                SecuritySettings.KEY_SCREENSHOTS_CLEANED_BYTES,
                SecuritySettings.KEY_FACE_UNLOCK_BRIGHTNESS_ENABLED,
                SecuritySettings.KEY_FACE_UNLOCK_MAX_BRIGHTNESS,
                SecuritySettings.KEY_FACE_UNLOCK_AMBIENT_THRESHOLD,
                SecuritySettings.KEY_FACE_UNLOCK_TRIGGER_UNLOCK,
                SecuritySettings.KEY_FACE_UNLOCK_AUTO_ILLUMINATE,
                SecuritySettings.KEY_FACE_UNLOCK_LIGHT_TINT,
                SecuritySettings.KEY_SCREEN_LOCKED_SECURITY_ENABLED,
                SecuritySettings.KEY_SCREEN_LOCKED_DISABLE_ON_EXTENDED_UNLOCK,
            )

        const val REQUESTER_QS = "DisableQsWhenLocked"
        const val REQUESTER_NOTIFICATION_INTERACTIONS = "DisableNotificationInteractions"
    }
}
