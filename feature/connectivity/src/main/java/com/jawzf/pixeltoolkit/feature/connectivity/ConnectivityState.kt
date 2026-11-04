package com.jawzf.pixeltoolkit.feature.connectivity

import android.content.Context
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.feature.connectivity.dns.DnsPreset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Compose state for the connectivity screens. */
class ConnectivityState(
    private val scope: CoroutineScope,
) {
    private lateinit var settings: ConnectivitySettings

    val dnsPresets = mutableStateListOf<DnsPreset>()
    val networkDownloadRateLimit = mutableIntStateOf(-1)
    val isMobileDataAlwaysOnEnabled = mutableStateOf(false)
    val isWirelessDisplayCertificationEnabled = mutableStateOf(false)

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        settings = ConnectivitySettings(context)
        networkDownloadRateLimit.intValue =
            settings.getInt(ConnectivitySettings.KEY_NETWORK_DOWNLOAD_RATE_LIMIT, -1)
        isMobileDataAlwaysOnEnabled.value =
            settings.getBoolean(ConnectivitySettings.KEY_MOBILE_DATA_ALWAYS_ON, false)
        isWirelessDisplayCertificationEnabled.value =
            settings.getBoolean(
                ConnectivitySettings.KEY_WIRELESS_DISPLAY_CERTIFICATION,
                false,
            )
        reloadDnsPresets()
    }

    fun reloadDnsPresets() {
        dnsPresets.clear()
        dnsPresets.addAll(settings.getPrivateDnsPresets())
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(
        key: String,
        context: Context?,
    ) {
        when (key) {
            ConnectivitySettings.KEY_PRIVATE_DNS_PRESETS -> {
                reloadDnsPresets()
            }

            ConnectivitySettings.KEY_NETWORK_DOWNLOAD_RATE_LIMIT -> {
                networkDownloadRateLimit.intValue =
                    settings.getInt(key, -1)
                context?.let {
                    applyNetworkDownloadRateLimit(
                        it,
                        networkDownloadRateLimit.intValue,
                    )
                }
            }

            ConnectivitySettings.KEY_MOBILE_DATA_ALWAYS_ON -> {
                isMobileDataAlwaysOnEnabled.value =
                    settings.getBoolean(key)
                context?.let {
                    applyMobileDataAlwaysOn(
                        it,
                        isMobileDataAlwaysOnEnabled.value,
                    )
                }
            }

            ConnectivitySettings.KEY_WIRELESS_DISPLAY_CERTIFICATION -> {
                isWirelessDisplayCertificationEnabled.value =
                    settings.getBoolean(key)
                context?.let {
                    applyWirelessDisplayCertification(
                        it,
                        isWirelessDisplayCertificationEnabled.value,
                    )
                }
            }

            else -> {}
        }
    }

    /**
     * Executes the set network download rate limit operation.
     *
     * @param limit [Int] Target limit.
     * @param context [Context] Target context.
     */
    fun setNetworkDownloadRateLimit(
        limit: Int,
        context: Context,
    ) {
        networkDownloadRateLimit.intValue = limit
        settings.putInt(ConnectivitySettings.KEY_NETWORK_DOWNLOAD_RATE_LIMIT, limit)
        applyNetworkDownloadRateLimit(context, limit)
    }

    /**
     * Executes the set mobile data always on enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setMobileDataAlwaysOnEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isMobileDataAlwaysOnEnabled.value = enabled
        settings.putBoolean(ConnectivitySettings.KEY_MOBILE_DATA_ALWAYS_ON, enabled)
        applyMobileDataAlwaysOn(context, enabled)
    }

    /**
     * Executes the set wireless display certification enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setWirelessDisplayCertificationEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isWirelessDisplayCertificationEnabled.value = enabled
        settings.putBoolean(
            ConnectivitySettings.KEY_WIRELESS_DISPLAY_CERTIFICATION,
            enabled,
        )
        applyWirelessDisplayCertification(context, enabled)
    }

    /**
     * Executes the refresh networks state operation.
     *
     * @param context [Context] Target context.
     */
    fun refreshNetworksState(context: Context) {
        try {
            val liveRateLimit =
                Settings.Global.getInt(
                    context.contentResolver,
                    "ingress_rate_limit_bytes_per_second",
                    -1,
                )
            networkDownloadRateLimit.intValue = liveRateLimit
            settings.putInt(
                ConnectivitySettings.KEY_NETWORK_DOWNLOAD_RATE_LIMIT,
                liveRateLimit,
            )
        } catch (e: Exception) {
            // Permission restricted
        }

        try {
            val liveMobileData =
                Settings.Global.getInt(context.contentResolver, "mobile_data_always_on", 0) == 1
            isMobileDataAlwaysOnEnabled.value = liveMobileData
            settings.putBoolean(
                ConnectivitySettings.KEY_MOBILE_DATA_ALWAYS_ON,
                liveMobileData,
            )
        } catch (e: Exception) {
            // Permission restricted
        }

        try {
            val liveWirelessDisplay =
                Settings.Global.getInt(
                    context.contentResolver,
                    "wifi_display_certification_on",
                    0,
                ) == 1
            isWirelessDisplayCertificationEnabled.value = liveWirelessDisplay
            settings.putBoolean(
                ConnectivitySettings.KEY_WIRELESS_DISPLAY_CERTIFICATION,
                liveWirelessDisplay,
            )
        } catch (e: Exception) {
            // Permission restricted
        }
    }

    private fun applyNetworkDownloadRateLimit(
        context: Context,
        limit: Int,
    ) {
        val key = "ingress_rate_limit_bytes_per_second"
        var success = false
        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success = Settings.Global.putInt(context.contentResolver, key, limit)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!success) {
            scope.launch(Dispatchers.IO) {
                ShellUtils.runCommand(context, "settings put global $key $limit")
            }
        }
    }

    private fun applyMobileDataAlwaysOn(
        context: Context,
        enabled: Boolean,
    ) {
        val value = if (enabled) 1 else 0
        val key = "mobile_data_always_on"
        var success = false
        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success = Settings.Global.putInt(context.contentResolver, key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!success) {
            scope.launch(Dispatchers.IO) {
                ShellUtils.runCommand(context, "settings put global $key $value")
            }
        }
    }

    private fun applyWirelessDisplayCertification(
        context: Context,
        enabled: Boolean,
    ) {
        val value = if (enabled) 1 else 0
        val key = "wifi_display_certification_on"
        var success = false
        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success = Settings.Global.putInt(context.contentResolver, key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (!success) {
            scope.launch(Dispatchers.IO) {
                ShellUtils.runCommand(context, "settings put global $key $value")
            }
        }
    }

    /**
     * Executes the reset dns presets operation.
     */
    fun resetDnsPresets() {
        settings.resetPrivateDnsPresets()
        reloadDnsPresets()
    }

    /**
     * Executes the add dns preset operation.
     *
     * @param name [String] Target name.
     * @param hostname [String] Target hostname.
     */
    fun addDnsPreset(
        name: String,
        hostname: String,
    ) {
        val current = settings.getPrivateDnsPresets().toMutableList()
        current.add(DnsPreset(name = name, hostname = hostname))
        settings.savePrivateDnsPresets(current)
        reloadDnsPresets()
    }

    /**
     * Executes the remove dns preset operation.
     *
     * @param preset [DnsPreset] Target preset.
     */
    fun removeDnsPreset(preset: DnsPreset) {
        val current = settings.getPrivateDnsPresets().toMutableList()
        current.removeAll { it.id == preset.id }
        settings.savePrivateDnsPresets(current)
        reloadDnsPresets()
    }
}
