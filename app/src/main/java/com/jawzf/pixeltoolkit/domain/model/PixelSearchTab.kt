package com.jawzf.pixeltoolkit.domain.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.jawzf.pixeltoolkit.R

enum class PixelSearchTab(
    @StringRes val labelRes: Int,
    @DrawableRes val iconRes: Int? = null,
) {
    ALL(R.string.pixel_search_tab_all),
    APPS(R.string.pixel_search_tab_apps, R.drawable.rounded_apps_24),
    MEDIA(R.string.pixel_search_tab_media, R.drawable.rounded_image_24),
    FILES(R.string.pixel_search_tab_files, R.drawable.rounded_description_24),
    CONTACTS(R.string.pixel_search_tab_contacts, R.drawable.rounded_call_24),
    SETTINGS(R.string.pixel_search_tab_settings, R.drawable.rounded_settings_24),
    WEB(R.string.pixel_search_tab_web, R.drawable.rounded_language_24),
}
