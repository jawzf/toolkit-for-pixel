package com.jawzf.pixeltoolkit.feature.notifications.autodismiss

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AutoDismissStat(
    @SerializedName("count") val count: Int,
    @SerializedName("since") val since: Long,
)
