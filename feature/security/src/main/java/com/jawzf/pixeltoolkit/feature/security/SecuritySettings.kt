package com.jawzf.pixeltoolkit.feature.security

import android.content.Context
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore

class SecuritySettings(
    context: Context,
) : PrefsStore(context) {
    fun isScreenshotShareCleanupEnabled(): Boolean = getBoolean(KEY_SCREENSHOT_SHARE_CLEANUP_ENABLED, false)

    fun setScreenshotShareCleanupEnabled(enabled: Boolean) = putBoolean(KEY_SCREENSHOT_SHARE_CLEANUP_ENABLED, enabled)

    fun getScreenshotShareCleanupDelayMinutes(): Int = getInt(KEY_SCREENSHOT_SHARE_CLEANUP_DELAY, 0)

    fun setScreenshotShareCleanupDelayMinutes(minutes: Int) = putInt(KEY_SCREENSHOT_SHARE_CLEANUP_DELAY, minutes)

    fun isScreenshotAutoCleanEnabled(): Boolean = getBoolean(KEY_SCREENSHOT_AUTO_CLEAN_ENABLED, false)

    fun setScreenshotAutoCleanEnabled(enabled: Boolean) = putBoolean(KEY_SCREENSHOT_AUTO_CLEAN_ENABLED, enabled)

    fun getScreenshotAutoCleanDays(): Int = getInt(KEY_SCREENSHOT_AUTO_CLEAN_DAYS, 7)

    fun setScreenshotAutoCleanDays(days: Int) = putInt(KEY_SCREENSHOT_AUTO_CLEAN_DAYS, days)

    fun getScreenshotAutoCleanLastId(): Long = getLong(KEY_SCREENSHOT_AUTO_CLEAN_LAST_ID, 0L)

    fun setScreenshotAutoCleanLastId(id: Long) = putLong(KEY_SCREENSHOT_AUTO_CLEAN_LAST_ID, id)

    fun getScreenshotsCleanedCount(): Int = getInt(KEY_SCREENSHOTS_CLEANED_COUNT, 0)

    fun getScreenshotsCleanedBytes(): Long = getLong(KEY_SCREENSHOTS_CLEANED_BYTES, 0L)

    fun recordScreenshotCleaned(bytes: Long) =
        synchronized(SecuritySettings::class.java) {
            putInt(KEY_SCREENSHOTS_CLEANED_COUNT, getScreenshotsCleanedCount() + 1)
            putLong(KEY_SCREENSHOTS_CLEANED_BYTES, getScreenshotsCleanedBytes() + bytes)
        }

    fun recordScreenshotRestored(bytes: Long) =
        synchronized(SecuritySettings::class.java) {
            putInt(KEY_SCREENSHOTS_CLEANED_COUNT, (getScreenshotsCleanedCount() - 1).coerceAtLeast(0))
            putLong(KEY_SCREENSHOTS_CLEANED_BYTES, (getScreenshotsCleanedBytes() - bytes).coerceAtLeast(0L))
        }

    companion object {
        const val KEY_SCREENSHOT_SHARE_CLEANUP_ENABLED = "screenshot_share_cleanup_enabled"
        const val KEY_SCREENSHOT_SHARE_CLEANUP_DELAY = "screenshot_share_cleanup_delay"
        const val KEY_SCREENSHOT_AUTO_CLEAN_ENABLED = "screenshot_auto_clean_enabled"
        const val KEY_SCREENSHOT_AUTO_CLEAN_DAYS = "screenshot_auto_clean_days"
        const val KEY_SCREENSHOT_AUTO_CLEAN_LAST_ID = "screenshot_auto_clean_last_id"
        const val KEY_SCREENSHOTS_CLEANED_COUNT = "screenshots_cleaned_count"
        const val KEY_SCREENSHOTS_CLEANED_BYTES = "screenshots_cleaned_bytes"
    }
}
