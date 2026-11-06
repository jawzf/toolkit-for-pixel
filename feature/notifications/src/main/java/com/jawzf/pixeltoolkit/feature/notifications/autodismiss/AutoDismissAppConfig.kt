package com.jawzf.pixeltoolkit.feature.notifications.autodismiss

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AutoDismissAppConfig(
    @SerializedName("packageName") val packageName: String,
    @SerializedName("minutes") val minutes: Int,
    @SerializedName("excludedChannels") val excludedChannels: List<String>? = null,
)
