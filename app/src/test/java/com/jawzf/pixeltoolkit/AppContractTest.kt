package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.services.AccessibilityShortcutService1
import com.jawzf.pixeltoolkit.services.AccessibilityShortcutService2
import com.jawzf.pixeltoolkit.services.AccessibilityShortcutService3
import com.jawzf.pixeltoolkit.services.NotificationListener
import com.jawzf.pixeltoolkit.services.SplitScreenShortcutService
import com.jawzf.pixeltoolkit.services.receivers.SecurityDeviceAdminReceiver
import com.jawzf.pixeltoolkit.services.receivers.ShizukuActionReceiver
import com.jawzf.pixeltoolkit.services.tiles.ScreenOffAccessibilityService
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
    }

    @Test
    fun prefKeysMatchSettingsRepository() {
        assertEquals(SettingsRepository.PREFS_NAME, PlatformPrefs.PREFS_NAME)
        assertEquals(SettingsRepository.KEY_PRIVILEGED_MODE, PlatformPrefs.KEY_PRIVILEGED_MODE)
        assertEquals(SettingsRepository.KEY_USE_ROOT, PlatformPrefs.KEY_USE_ROOT)
        assertEquals(SettingsRepository.KEY_SHIZUKU_AUTH_TOKEN, PlatformPrefs.KEY_SHIZUKU_AUTH_TOKEN)
    }
}
