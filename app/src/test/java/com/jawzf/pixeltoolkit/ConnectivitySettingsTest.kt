package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.feature.connectivity.ConnectivitySettings
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectivitySettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("private_dns_presets", ConnectivitySettings.KEY_PRIVATE_DNS_PRESETS)
        assertEquals("sim_names_apply_on_boot", ConnectivitySettings.KEY_SIM_NAMES_APPLY_ON_BOOT)
    }
}
