package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.power.CaffeinateController
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateTileService
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateWakeLockService
import com.jawzf.pixeltoolkit.services.AccessibilityShortcutService1
import com.jawzf.pixeltoolkit.services.AccessibilityShortcutService2
import com.jawzf.pixeltoolkit.services.AccessibilityShortcutService3
import com.jawzf.pixeltoolkit.services.NotificationListener
import com.jawzf.pixeltoolkit.services.SplitScreenShortcutService
import com.jawzf.pixeltoolkit.services.receivers.SecurityDeviceAdminReceiver
import com.jawzf.pixeltoolkit.services.receivers.ShizukuActionReceiver
import com.jawzf.pixeltoolkit.services.tiles.ScreenOffAccessibilityService
import com.jawzf.pixeltoolkit.ui.activities.BubbleWebActivity
import com.jawzf.pixeltoolkit.ui.activities.ColorPickerActivity
import com.jawzf.pixeltoolkit.ui.activities.RipplePositionPickerActivity
import com.jawzf.pixeltoolkit.ui.activities.WidgetStackConfigureActivity
import org.junit.Assert.assertEquals
import org.junit.Test

class AppContractTest {
    @Test
    fun componentNamesMatchClasses() {
        assertEquals(NotificationListener::class.java.name, AppComponents.NOTIFICATION_LISTENER)
        assertEquals(ScreenOffAccessibilityService::class.java.name, AppComponents.SCREEN_OFF_ACCESSIBILITY_SERVICE)
        assertEquals(SecurityDeviceAdminReceiver::class.java.name, AppComponents.SECURITY_DEVICE_ADMIN_RECEIVER)
        assertEquals(ShizukuActionReceiver::class.java.name, AppComponents.SHIZUKU_ACTION_RECEIVER)
        assertEquals(AccessibilityShortcutService1::class.java.name, AppComponents.ACCESSIBILITY_SHORTCUT_SERVICE_1)
        assertEquals(AccessibilityShortcutService2::class.java.name, AppComponents.ACCESSIBILITY_SHORTCUT_SERVICE_2)
        assertEquals(AccessibilityShortcutService3::class.java.name, AppComponents.ACCESSIBILITY_SHORTCUT_SERVICE_3)
        assertEquals(SplitScreenShortcutService::class.java.name, AppComponents.SPLIT_SCREEN_SHORTCUT_SERVICE)
        assertEquals(FeatureSettingsActivity::class.java.name, AppComponents.FEATURE_SETTINGS_ACTIVITY)
        assertEquals(BubbleWebActivity::class.java.name, AppComponents.BUBBLE_WEB_ACTIVITY)
        assertEquals(BubbleWebActivity.EXTRA_URL, AppComponents.BUBBLE_EXTRA_URL)
        assertEquals(BubbleWebActivity.EXTRA_PRIVATE_MODE, AppComponents.BUBBLE_EXTRA_PRIVATE_MODE)
        assertEquals(RipplePositionPickerActivity::class.java.name, AppComponents.RIPPLE_POSITION_PICKER_ACTIVITY)
        assertEquals(WidgetStackConfigureActivity::class.java.name, AppComponents.WIDGET_STACK_CONFIGURE_ACTIVITY)
        assertEquals(ColorPickerActivity::class.java.name, AppComponents.COLOR_PICKER_ACTIVITY)
    }

    @Test
    fun caffeinateComponentNamesMatchClasses() {
        assertEquals(CaffeinateTileService::class.java.name, CaffeinateController.TILE_SERVICE)
        assertEquals(CaffeinateWakeLockService::class.java.name, CaffeinateController.WAKE_LOCK_SERVICE)
    }

    @Test
    fun prefKeysMatchSettingsRepository() {
        assertEquals(SettingsRepository.PREFS_NAME, PlatformPrefs.PREFS_NAME)
        assertEquals(SettingsRepository.KEY_PRIVILEGED_MODE, PlatformPrefs.KEY_PRIVILEGED_MODE)
        assertEquals(SettingsRepository.KEY_USE_ROOT, PlatformPrefs.KEY_USE_ROOT)
        assertEquals(SettingsRepository.KEY_SHIZUKU_AUTH_TOKEN, PlatformPrefs.KEY_SHIZUKU_AUTH_TOKEN)
        assertEquals(SettingsRepository.KEY_SECURE_SENSITIVE_TILES, PlatformPrefs.KEY_SECURE_SENSITIVE_TILES)
        assertEquals(SettingsRepository.KEY_ENABLE_UNSUPPORTED_FEATURES, PlatformPrefs.KEY_ENABLE_UNSUPPORTED_FEATURES)
        assertEquals(SettingsRepository.KEY_HIDDEN_DEBUGGING_SUPPORT, PlatformPrefs.KEY_HIDDEN_DEBUGGING_SUPPORT)
    }
}
