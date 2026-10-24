package com.jawzf.pixeltoolkit.domain.model

data class ShutUpAppConfig(
    val packageName: String,
    val isEnabled: Boolean = true,
    val disableDevOptions: Boolean = true,
    val disableUsbDebugging: Boolean = true,
    val disableWirelessDebugging: Boolean = true,
    val disableAccessibility: Boolean = false,
    val autoArchive: Boolean = false,
    val restoreKeyboard: Boolean = false,
)
