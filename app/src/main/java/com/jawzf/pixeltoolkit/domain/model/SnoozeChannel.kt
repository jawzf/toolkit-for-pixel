package com.jawzf.pixeltoolkit.domain.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SnoozeChannel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("isBlocked") val isBlocked: Boolean = false,
)
