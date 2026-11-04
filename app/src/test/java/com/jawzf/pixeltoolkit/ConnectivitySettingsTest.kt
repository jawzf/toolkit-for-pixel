package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.feature.connectivity.ConnectivitySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectivitySettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("private_dns_presets", ConnectivitySettings.KEY_PRIVATE_DNS_PRESETS)
        assertEquals("sim_names_apply_on_boot", ConnectivitySettings.KEY_SIM_NAMES_APPLY_ON_BOOT)
        assertEquals("network_download_rate_limit", ConnectivitySettings.KEY_NETWORK_DOWNLOAD_RATE_LIMIT)
        assertEquals("mobile_data_always_on", ConnectivitySettings.KEY_MOBILE_DATA_ALWAYS_ON)
        assertEquals("wireless_display_certification", ConnectivitySettings.KEY_WIRELESS_DISPLAY_CERTIFICATION)
        assertEquals("pitch_black_theme_enabled", PlatformPrefs.KEY_PITCH_BLACK_THEME_ENABLED)
    }
}
