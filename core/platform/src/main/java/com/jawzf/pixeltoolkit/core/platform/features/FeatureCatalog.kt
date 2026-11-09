package com.jawzf.pixeltoolkit.core.platform.features

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/** What other modules may know about one of the app's features. */
data class FeatureSummary(
    val id: String,
    @StringRes val title: Int,
    @DrawableRes val iconRes: Int,
)

/** The app's feature list, for modules that can't see `FeatureRegistry`. Set once in ToolkitApp.onCreate. */
object FeatureCatalog {
    private var source: () -> List<FeatureSummary> = { emptyList() }

    fun init(source: () -> List<FeatureSummary>) {
        this.source = source
    }

    fun all(): List<FeatureSummary> = source()
}
