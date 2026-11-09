package com.jawzf.pixeltoolkit.core.data.display

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ScaleAnimationsProfile(
    @SerializedName("fontScale") val fontScale: Float = 1.0f,
    @SerializedName("fontWeight") val fontWeight: Int = 0,
    @SerializedName("animatorDurationScale") val animatorDurationScale: Float = 1.0f,
    @SerializedName("transitionAnimationScale") val transitionAnimationScale: Float = 1.0f,
    @SerializedName("windowAnimationScale") val windowAnimationScale: Float = 1.0f,
    @SerializedName("smallestWidth") val smallestWidth: Int = 360,
    @SerializedName("touchSensitivityEnabled") val touchSensitivityEnabled: Boolean = false,
    @SerializedName("autoRotateEnabled") val autoRotateEnabled: Boolean = false,
    @SerializedName("screenTimeout") val screenTimeout: Long = 30000L,
)
