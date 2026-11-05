package com.jawzf.pixeltoolkit.feature.apps.freeze

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AppTag(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("colorHex") val colorHex: String,
    @SerializedName("iconName") val iconName: String,
    @SerializedName("neverAutoFreeze") val neverAutoFreeze: Boolean = false,
)
