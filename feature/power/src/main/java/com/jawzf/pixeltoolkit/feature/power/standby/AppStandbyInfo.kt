package com.jawzf.pixeltoolkit.feature.power.standby

import android.graphics.drawable.Drawable

data class AppStandbyInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val bucket: Int,
)
