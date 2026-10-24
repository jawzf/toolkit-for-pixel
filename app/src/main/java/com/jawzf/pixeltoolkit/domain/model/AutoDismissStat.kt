package com.jawzf.pixeltoolkit.domain.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AutoDismissStat(
    @SerializedName("count") val count: Int,
    @SerializedName("since") val since: Long,
)
