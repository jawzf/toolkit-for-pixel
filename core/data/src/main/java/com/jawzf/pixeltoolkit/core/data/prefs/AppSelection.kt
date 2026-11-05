package com.jawzf.pixeltoolkit.core.data.prefs

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class AppSelection(
    @SerializedName("packageName") val packageName: String,
    @SerializedName("isEnabled") val isEnabled: Boolean,
)
