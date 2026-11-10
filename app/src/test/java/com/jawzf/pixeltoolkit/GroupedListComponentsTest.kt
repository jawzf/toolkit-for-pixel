package com.jawzf.pixeltoolkit

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.containers.resolveRoundedCardContainerColor
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupedListComponentsTest {
    @Test
    fun iconTileShapeHasExpectedRadius() {
        assertEquals(RoundedCornerShape(11.dp), ToolkitShapes.iconTile)
    }

    @Test
    fun containerColorDefaultsToDividerWhenSpacingAtMostOneDp() {
        val divider = Color(0xFFE2E2E0)
        val resolvedDefault = resolveRoundedCardContainerColor(Color.Unspecified, 1.dp, divider)
        assertEquals(divider, resolvedDefault)

        val resolvedZero = resolveRoundedCardContainerColor(Color.Unspecified, 0.dp, divider)
        assertEquals(divider, resolvedZero)
    }

    @Test
    fun containerColorDefaultsToTransparentWhenSpacingGreaterThanOneDp() {
        val divider = Color(0xFFE2E2E0)
        val resolvedSpacingEight = resolveRoundedCardContainerColor(Color.Unspecified, 8.dp, divider)
        assertEquals(Color.Transparent, resolvedSpacingEight)

        val resolvedSpacingTwo = resolveRoundedCardContainerColor(Color.Unspecified, 2.dp, divider)
        assertEquals(Color.Transparent, resolvedSpacingTwo)
    }

    @Test
    fun containerColorPreservesExplicitColor() {
        val divider = Color(0xFFE2E2E0)
        val custom = Color(0xFF112233)
        assertEquals(custom, resolveRoundedCardContainerColor(custom, 1.dp, divider))
        assertEquals(custom, resolveRoundedCardContainerColor(custom, 8.dp, divider))
    }
}
