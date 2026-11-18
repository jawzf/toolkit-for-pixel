package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.ui.composables.CATEGORY_FEATURE_IDS
import com.jawzf.pixeltoolkit.ui.composables.CategoryFilter
import com.jawzf.pixeltoolkit.ui.composables.matches
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryFilterTest {
    @Test
    fun allMatchesEveryCombination() {
        val booleans = listOf(true, false)
        for (showToggle in booleans) {
            for (isOn in booleans) {
                assertTrue(
                    CategoryFilter.All.matches(
                        showToggle = showToggle,
                        isOn = isOn,
                    ),
                )
            }
        }
    }

    @Test
    fun onMatchesOnlyWhenToggleShownAndIsOn() {
        assertTrue(CategoryFilter.On.matches(showToggle = true, isOn = true))
        assertFalse(CategoryFilter.On.matches(showToggle = true, isOn = false))
        assertFalse(CategoryFilter.On.matches(showToggle = false, isOn = true))
        assertFalse(CategoryFilter.On.matches(showToggle = false, isOn = false))
    }

    @Test
    fun categoryFeatureIdsContainsExpectedCategories() {
        val expected =
            setOf(
                "Display",
                "Notifications",
                "Sound",
                "Input",
                "Widgets",
                "Power and battery",
                "Security",
            )
        assertEquals(expected, CATEGORY_FEATURE_IDS)
        assertFalse(CATEGORY_FEATURE_IDS.contains("Networks"))
        assertFalse(CATEGORY_FEATURE_IDS.contains("Island"))
        assertFalse(CATEGORY_FEATURE_IDS.contains("Navigation"))
        assertFalse(CATEGORY_FEATURE_IDS.contains("Hilight"))
    }
}
