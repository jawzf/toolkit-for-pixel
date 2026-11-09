package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.platform.features.FeatureCatalog
import com.jawzf.pixeltoolkit.core.platform.features.FeatureSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class FeatureCatalogTest {
    @Test
    fun featureCatalogReturnsConfiguredSummaries() {
        val summaries =
            listOf(
                FeatureSummary("feat_1", 101, 201),
                FeatureSummary("feat_2", 102, 202),
            )
        FeatureCatalog.init { summaries }
        assertEquals(summaries, FeatureCatalog.all())
    }

    @Test
    fun featureCatalogDefaultsToEmptyList() {
        FeatureCatalog.init { emptyList() }
        assertEquals(emptyList<FeatureSummary>(), FeatureCatalog.all())
    }
}
