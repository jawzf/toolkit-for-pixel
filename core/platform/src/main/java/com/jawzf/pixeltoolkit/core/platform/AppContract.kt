package com.jawzf.pixeltoolkit.core.platform

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
}

/** Preference keys the platform helpers read; the same values as in SettingsRepository. */
object PlatformPrefs {
    const val PREFS_NAME = "toolkit_prefs"
    const val KEY_PRIVILEGED_MODE = "privileged_mode"
    const val KEY_USE_ROOT = "use_root"
    const val KEY_SHIZUKU_AUTH_TOKEN = "shizuku_auth_token"
    const val KEY_SECURE_SENSITIVE_TILES = "secure_sensitive_tiles"
    const val KEY_ENABLE_UNSUPPORTED_FEATURES = "enable_unsupported_features"
}
