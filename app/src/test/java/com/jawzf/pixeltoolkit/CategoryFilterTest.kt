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
                for (needsSetup in booleans) {
                    assertTrue(
                        CategoryFilter.All.matches(
                            showToggle = showToggle,
                            isOn = isOn,
                            needsSetup = needsSetup,
                        ),
                    )
                }
            }
        }
    }

    @Test
    fun onMatchesOnlyWhenToggleShownAndIsOn() {
        for (needsSetup in listOf(false, true)) {
            assertTrue(CategoryFilter.On.matches(showToggle = true, isOn = true, needsSetup = needsSetup))
            assertFalse(CategoryFilter.On.matches(showToggle = true, isOn = false, needsSetup = needsSetup))
            assertFalse(CategoryFilter.On.matches(showToggle = false, isOn = true, needsSetup = needsSetup))
            assertFalse(CategoryFilter.On.matches(showToggle = false, isOn = false, needsSetup = needsSetup))
        }
    }

    @Test
    fun needsSetupMatchesOnlyWhenSetupIsMissing() {
        val booleans = listOf(true, false)
        for (showToggle in booleans) {
            for (isOn in booleans) {
                assertTrue(
                    CategoryFilter.NeedsSetup.matches(
                        showToggle = showToggle,
                        isOn = isOn,
                        needsSetup = true,
                    ),
                )
                assertFalse(
                    CategoryFilter.NeedsSetup.matches(
                        showToggle = showToggle,
                        isOn = isOn,
                        needsSetup = false,
                    ),
                )
            }
        }
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
