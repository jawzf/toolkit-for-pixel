package com.jawzf.pixeltoolkit

import android.content.Context
import com.jawzf.pixeltoolkit.domain.model.Feature
import com.jawzf.pixeltoolkit.ui.composables.activeNowFeatures
import com.jawzf.pixeltoolkit.ui.composables.featuresNeedingFix
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeSectionsTest {
    private class FakeFeature(
        id: String,
        showToggle: Boolean = true,
    ) : Feature(
            id = id,
            title = 0,
            iconRes = 0,
            category = 0,
            description = 0,
            showToggle = showToggle,
        ) {
        override fun isEnabled(viewModel: MainViewModel): Boolean = false

        override fun onToggle(
            viewModel: MainViewModel,
            context: Context,
            enabled: Boolean,
        ) = Unit
    }

    @Test
    fun pinnedFeaturesComeFirstInPinOrderThenOtherOnFeaturesWithoutDuplicates() {
        val featA = FakeFeature("a")
        val featB = FakeFeature("b")
        val featC = FakeFeature("c")
        val featD = FakeFeature("d")
        val features = listOf(featA, featB, featC, featD)

        val pinnedKeys = listOf("c", "a")
        val onFeatures = setOf("b", "c", "d")

        val result =
            activeNowFeatures(
                features = features,
                pinnedKeys = pinnedKeys,
                isOn = { it.id in onFeatures },
            )

        assertEquals(listOf(featC, featA, featB, featD), result)
    }

    @Test
    fun featureWithoutToggleIsExcludedWhenOnAndNotPinned() {
        val toggleableOn = FakeFeature("toggleable", showToggle = true)
        val nonToggleableOn = FakeFeature("non_toggleable", showToggle = false)
        val pinnedNonToggleable = FakeFeature("pinned_non_toggleable", showToggle = false)
        val features = listOf(toggleableOn, nonToggleableOn, pinnedNonToggleable)

        val result =
            activeNowFeatures(
                features = features,
                pinnedKeys = listOf("pinned_non_toggleable"),
                isOn = { true },
            )

        assertEquals(listOf(pinnedNonToggleable, toggleableOn), result)
    }

    @Test
    fun unknownPinnedKeyIsIgnored() {
        val featA = FakeFeature("a")
        val featB = FakeFeature("b")
        val features = listOf(featA, featB)

        val result =
            activeNowFeatures(
                features = features,
                pinnedKeys = listOf("unknown", "b"),
                isOn = { false },
            )

        assertEquals(listOf(featB), result)
    }

    @Test
    fun featuresNeedingFixIncludesOnlyOnFeaturesThatCannotToggle() {
        val onAndBlocked = FakeFeature("on_blocked")
        val onAndAllowed = FakeFeature("on_allowed")
        val offAndBlocked = FakeFeature("off_blocked")
        val offAndAllowed = FakeFeature("off_allowed")

        val active = listOf(onAndBlocked, onAndAllowed, offAndBlocked, offAndAllowed)
        val onSet = setOf("on_blocked", "on_allowed")
        val canToggleSet = setOf("on_allowed", "off_allowed")

        val result =
            featuresNeedingFix(
                active = active,
                isOn = { it.id in onSet },
                canToggle = { it.id in canToggleSet },
            )

        assertEquals(listOf(onAndBlocked), result)
    }
}
