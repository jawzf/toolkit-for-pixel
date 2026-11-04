package com.jawzf.pixeltoolkit.feature.connectivity

import android.content.Context
import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.feature.connectivity.dns.DnsPreset

class ConnectivitySettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    fun isSimNamesApplyOnBootEnabled(): Boolean = getBoolean(KEY_SIM_NAMES_APPLY_ON_BOOT, false)

    fun setSimNamesApplyOnBootEnabled(enabled: Boolean) = putBoolean(KEY_SIM_NAMES_APPLY_ON_BOOT, enabled)

    /**
     * Executes the get private dns presets operation.
     * @return The resulting List<DnsPreset> data.
     */
    fun getPrivateDnsPresets(): List<DnsPreset> {
        val json = prefs.getString(KEY_PRIVATE_DNS_PRESETS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<DnsPreset>::class.java).toList()
            } catch (e: Exception) {
                getDefaultDnsPresets()
            }
        } else {
            getDefaultDnsPresets().also { savePrivateDnsPresets(it) }
        }
    }

    private fun getDefaultDnsPresets(): List<DnsPreset> =
        listOf(
            DnsPreset(
                name = context.getString(com.jawzf.pixeltoolkit.core.design.R.string.dns_preset_adguard),
                hostname = "dns.adguard.com",
                isDefault = true,
            ),
            DnsPreset(
                name = context.getString(com.jawzf.pixeltoolkit.core.design.R.string.dns_preset_google),
                hostname = "dns.google",
                isDefault = true,
            ),
            DnsPreset(
                name = context.getString(com.jawzf.pixeltoolkit.core.design.R.string.dns_preset_cloudflare),
                hostname = "1dot1dot1dot1.cloudflare-dns.com",
                isDefault = true,
            ),
            DnsPreset(
                name = context.getString(com.jawzf.pixeltoolkit.core.design.R.string.dns_preset_quad9),
                hostname = "dns.quad9.net",
                isDefault = true,
            ),
            DnsPreset(
                name = context.getString(com.jawzf.pixeltoolkit.core.design.R.string.dns_preset_cleanbrowsing),
                hostname = "adult-filter-dns.cleanbrowsing.org",
                isDefault = true,
            ),
        )

    /**
     * Executes the save private dns presets operation.
     *
     * @param presets [List<DnsPreset>] Target presets.
     */
    fun savePrivateDnsPresets(presets: List<DnsPreset>) {
        val json = gson.toJson(presets)
        putString(KEY_PRIVATE_DNS_PRESETS, json)
    }

    /**
     * Executes the reset private dns presets operation.
     */
    fun resetPrivateDnsPresets() {
        savePrivateDnsPresets(getDefaultDnsPresets())
    }

    companion object {
        const val KEY_PRIVATE_DNS_PRESETS = "private_dns_presets"
        const val KEY_SIM_NAMES_APPLY_ON_BOOT = "sim_names_apply_on_boot"
        const val KEY_NETWORK_DOWNLOAD_RATE_LIMIT = "network_download_rate_limit"
        const val KEY_MOBILE_DATA_ALWAYS_ON = "mobile_data_always_on"
        const val KEY_WIRELESS_DISPLAY_CERTIFICATION = "wireless_display_certification"
    }
}
