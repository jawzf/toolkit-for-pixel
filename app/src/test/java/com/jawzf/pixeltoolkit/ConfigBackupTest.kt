package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.backup.ConfigBackup
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfigBackupTest {
    @Test
    fun excludedKeysMatchSettingsRepository() {
        assertEquals(AppsSettings.KEY_GITHUB_ACCESS_TOKEN, ConfigBackup.KEY_GITHUB_ACCESS_TOKEN)
        assertEquals(SettingsRepository.KEY_SHIZUKU_AUTH_TOKEN, ConfigBackup.KEY_SHIZUKU_AUTH_TOKEN)
        assertEquals(NotificationSettings.KEY_SNOOZE_DISCOVERED_CHANNELS, ConfigBackup.KEY_SNOOZE_DISCOVERED_CHANNELS)
        assertEquals(PowerSettings.KEY_MAPS_DISCOVERED_CHANNELS, ConfigBackup.KEY_MAPS_DISCOVERED_CHANNELS)
        assertEquals(SettingsRepository.KEY_SHUT_UP_ORIGINAL_SETTINGS, ConfigBackup.KEY_SHUT_UP_ORIGINAL_SETTINGS)
        assertEquals(PlatformPrefs.PREFS_NAME, SettingsRepository.PREFS_NAME)
    }
}
