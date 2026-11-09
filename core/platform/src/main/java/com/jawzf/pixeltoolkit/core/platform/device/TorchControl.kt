package com.jawzf.pixeltoolkit.core.platform.device

/** The flashlight as other features see it; implemented by :app's FlashlightHandler. */
interface TorchControl {
    val isTorchOn: Boolean

    fun adjustFlashlightIntensity(increase: Boolean)

    fun toggleFlashlight(overrideIntensity: Int? = null)
}
