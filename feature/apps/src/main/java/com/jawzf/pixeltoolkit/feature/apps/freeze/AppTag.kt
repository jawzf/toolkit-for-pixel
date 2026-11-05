package com.jawzf.pixeltoolkit.feature.apps.freeze

data class AppTag(
    val id: String,
    val name: String,
    val colorHex: String,
    val iconName: String,
    val neverAutoFreeze: Boolean = false,
)
