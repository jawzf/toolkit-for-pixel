package com.jawzf.pixeltoolkit.feature.notifications.autodismiss

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AutoDismissChannel(
    @SerializedName("packageName") val packageName: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
)
