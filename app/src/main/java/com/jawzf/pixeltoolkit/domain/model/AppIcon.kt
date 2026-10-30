package com.jawzf.pixeltoolkit.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.jawzf.pixeltoolkit.core.design.R

enum class AppIcon(
    val key: String,
    val aliasName: String,
    @StringRes val titleRes: Int,
    @DrawableRes val foregroundRes: Int,
) {
    DEFAULT(
        key = "default",
        aliasName = "com.jawzf.pixeltoolkit.MainActivity",
        titleRes = R.string.app_icon_default,
        foregroundRes = R.drawable.ic_launcher_foreground,
    ),
    LEGACY(
        key = "legacy",
        aliasName = "com.jawzf.pixeltoolkit.MainActivityLegacy",
        titleRes = R.string.app_icon_legacy,
        foregroundRes = R.drawable.ic_launcher_foreground_legacy,
    ),
    BLACK(
        key = "black",
        aliasName = "com.jawzf.pixeltoolkit.MainActivityBlack",
        titleRes = R.string.app_icon_black,
        foregroundRes = R.drawable.ic_launcher_foreground_black,
    ),
    GTA(
        key = "gta",
        aliasName = "com.jawzf.pixeltoolkit.MainActivityGta",
        titleRes = R.string.app_icon_gta,
        foregroundRes = R.drawable.ic_launcher_foreground_gta,
    ),
    GOOGLE(
        key = "google",
        aliasName = "com.jawzf.pixeltoolkit.MainActivityGoogle",
        titleRes = R.string.app_icon_google,
        foregroundRes = R.drawable.ic_launcher_foreground_google,
    );

    companion object {
        fun fromKey(key: String?): AppIcon {
            return entries.find { it.key == key } ?: DEFAULT
        }
    }
}
