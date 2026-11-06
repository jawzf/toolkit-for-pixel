package com.jawzf.pixeltoolkit.feature.input.gesture

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class BackGestureAppConfig(
    @SerializedName("packageName") val packageName: String,
    @SerializedName("leftScale") val leftScale: Float = 0f,
    @SerializedName("rightScale") val rightScale: Float = 0f,
)
