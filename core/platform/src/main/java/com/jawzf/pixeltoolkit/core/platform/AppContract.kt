package com.jawzf.pixeltoolkit.core.platform

import android.content.Context
import android.content.Intent

/** Class names of components that stay in :app, for code in core and feature modules. */
object AppComponents {
    const val NOTIFICATION_LISTENER = "com.jawzf.pixeltoolkit.services.NotificationListener"
    const val SCREEN_OFF_ACCESSIBILITY_SERVICE = "com.jawzf.pixeltoolkit.services.tiles.ScreenOffAccessibilityService"
    const val SECURITY_DEVICE_ADMIN_RECEIVER = "com.jawzf.pixeltoolkit.services.receivers.SecurityDeviceAdminReceiver"
    const val SHIZUKU_ACTION_RECEIVER = "com.jawzf.pixeltoolkit.services.receivers.ShizukuActionReceiver"
    const val ACCESSIBILITY_SHORTCUT_SERVICE_1 = "com.jawzf.pixeltoolkit.services.AccessibilityShortcutService1"
    const val ACCESSIBILITY_SHORTCUT_SERVICE_2 = "com.jawzf.pixeltoolkit.services.AccessibilityShortcutService2"
    const val ACCESSIBILITY_SHORTCUT_SERVICE_3 = "com.jawzf.pixeltoolkit.services.AccessibilityShortcutService3"
    const val SPLIT_SCREEN_SHORTCUT_SERVICE = "com.jawzf.pixeltoolkit.services.SplitScreenShortcutService"
    const val FEATURE_SETTINGS_ACTIVITY = "com.jawzf.pixeltoolkit.FeatureSettingsActivity"
    const val TILE_AUTH_ACTIVITY = "com.jawzf.pixeltoolkit.ui.activities.TileAuthActivity"
    const val MAIN_ACTIVITY = "com.jawzf.pixeltoolkit.MainActivity"
    const val APP_FREEZING_ACTIVITY = "com.jawzf.pixeltoolkit.ui.activities.AppFreezingActivity"
    const val BUBBLE_WEB_ACTIVITY = "com.jawzf.pixeltoolkit.ui.activities.BubbleWebActivity"
    const val BUBBLE_EXTRA_URL = "extra_bubble_url"
    const val BUBBLE_EXTRA_PRIVATE_MODE = "extra_bubble_private_mode"
    const val RIPPLE_POSITION_PICKER_ACTIVITY = "com.jawzf.pixeltoolkit.ui.activities.RipplePositionPickerActivity"
    const val WIDGET_STACK_CONFIGURE_ACTIVITY = "com.jawzf.pixeltoolkit.ui.activities.WidgetStackConfigureActivity"
    const val COLOR_PICKER_ACTIVITY = "com.jawzf.pixeltoolkit.ui.activities.ColorPickerActivity"
    const val CONSCIOUS_GATE_FEATURE_ID = "Conscious gate"
    const val ACTION_LIKE_CURRENT_SONG = "com.jawzf.pixeltoolkit.ACTION_LIKE_CURRENT_SONG"

    /** The app's launch intent, or MainActivity when the launcher alias is disabled. */
    fun mainIntent(context: Context): Intent =
        context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: Intent().setClassName(context, MAIN_ACTIVITY)
}

/** Preference keys the platform helpers read; the same values as in SettingsRepository. */
object PlatformPrefs {
    const val PREFS_NAME = "toolkit_prefs"
    const val KEY_PRIVILEGED_MODE = "privileged_mode"
    const val KEY_USE_ROOT = "use_root"
    const val KEY_SHIZUKU_AUTH_TOKEN = "shizuku_auth_token"
    const val KEY_SECURE_SENSITIVE_TILES = "secure_sensitive_tiles"
    const val KEY_ENABLE_UNSUPPORTED_FEATURES = "enable_unsupported_features"
    const val KEY_PITCH_BLACK_THEME_ENABLED = "pitch_black_theme_enabled"
    const val KEY_HIDE_SYSTEM_ICONS = "hide_system_icons"
    const val KEY_HIDE_SYSTEM_ICONS_LOCKED_ONLY = "hide_system_icons_locked_only"
    const val KEY_HIDE_CLOCK = "hide_clock"
    const val KEY_HIDE_NOTIFICATION_ICONS = "hide_notification_icons"
    const val KEY_HIDE_GESTURE_BAR_ENABLED = "hide_gesture_bar_enabled"
    const val KEY_STATUS_BAR_ICON_CONTROL_ENABLED = "status_bar_icon_control_enabled"
    const val KEY_HIDDEN_DEBUGGING_SUPPORT = "hidden_debugging_support"
    const val KEY_PINNED_FEATURES = "pinned_features"
}
