package com.jawzf.pixeltoolkit.utils

import com.jawzf.pixeltoolkit.core.design.R

enum class PrivilegedMode(
    val key: String,
    val labelRes: Int,
) {
    AUTO("auto", R.string.privileged_mode_auto),
    SHIZUKU("shizuku", R.string.privileged_mode_shizuku),
    DHIZUKU("dhizuku", R.string.privileged_mode_dhizuku),
    PORTER("porter", R.string.privileged_mode_porter),
    ROOT("root", R.string.privileged_mode_root),
    ;

    companion object {
        fun fromKey(key: String?): PrivilegedMode = entries.firstOrNull { it.key == key } ?: AUTO
    }
}
