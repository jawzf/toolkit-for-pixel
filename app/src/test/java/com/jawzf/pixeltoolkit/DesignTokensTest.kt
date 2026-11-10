package com.jawzf.pixeltoolkit

import androidx.compose.ui.graphics.Color
import com.jawzf.pixeltoolkit.core.design.theme.Accent
import com.jawzf.pixeltoolkit.core.design.theme.accentColor
import com.jawzf.pixeltoolkit.core.design.theme.toColorScheme
import com.jawzf.pixeltoolkit.core.design.theme.toolkitColors
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DesignTokensTest {
    @Test
    fun accentColorReturnsExpectedPalette() {
        assertEquals(Color(0xFF1F5FD1), accentColor(Accent.Blue, dark = false))
        assertEquals(Color(0xFF7FA7F0), accentColor(Accent.Blue, dark = true))

        assertEquals(Color(0xFF0F7B6C), accentColor(Accent.Teal, dark = false))
        assertEquals(Color(0xFF5CC2B1), accentColor(Accent.Teal, dark = true))

        assertEquals(Color(0xFF7A4BD1), accentColor(Accent.Violet, dark = false))
        assertEquals(Color(0xFFB39AF0), accentColor(Accent.Violet, dark = true))

        assertEquals(Color(0xFF1F5FD1), accentColor(Accent.Wallpaper, dark = false))
        assertEquals(Color(0xFF7FA7F0), accentColor(Accent.Wallpaper, dark = true))
    }

    @Test
    fun toolkitColorsRespectsThemeMode() {
        val blueLight = accentColor(Accent.Blue, dark = false)
        val lightColors = toolkitColors(blueLight, dark = false)
        assertEquals(Color(0xFFF5F4F0), lightColors.ground)
        assertEquals(Color(0xFFFFFFFF), lightColors.surface)
        assertEquals(Color(0xFF1B1C1E), lightColors.text)

        val blueDark = accentColor(Accent.Blue, dark = true)
        val darkColors = toolkitColors(blueDark, dark = true)
        assertEquals(Color(0xFF121211), darkColors.ground)
        assertEquals(Color(0xFF1C1C1A), darkColors.surface)

        val pitchBlackDark = toolkitColors(blueDark, dark = true, pitchBlack = true)
        assertEquals(Color(0xFF000000), pitchBlackDark.ground)
        assertEquals(Color(0xFF111111), pitchBlackDark.surface)

        val pitchBlackLight = toolkitColors(blueLight, dark = false, pitchBlack = true)
        assertEquals(Color(0xFFF5F4F0), pitchBlackLight.ground)
    }

    @Test
    fun accentTintIsOpaqueAndDistinct() {
        val lightTokens = toolkitColors(accentColor(Accent.Blue, dark = false), dark = false)
        assertEquals(1f, lightTokens.accentTint.alpha, 0.001f)
        assertNotEquals(lightTokens.accent, lightTokens.accentTint)
        assertNotEquals(lightTokens.surface, lightTokens.accentTint)

        val darkTokens = toolkitColors(accentColor(Accent.Blue, dark = true), dark = true)
        assertEquals(1f, darkTokens.accentTint.alpha, 0.001f)
        assertNotEquals(darkTokens.accent, darkTokens.accentTint)
        assertNotEquals(darkTokens.surface, darkTokens.accentTint)
    }

    @Test
    fun toColorSchemeMapsTokensCorrectly() {
        for (dark in listOf(false, true)) {
            val accent = accentColor(Accent.Blue, dark = dark)
            val tokens = toolkitColors(accent, dark = dark)
            val scheme = tokens.toColorScheme()
            assertEquals(tokens.accent, scheme.primary)
            assertEquals(tokens.ground, scheme.background)
            assertEquals(tokens.surface, scheme.surface)
            assertEquals(tokens.surface, scheme.surfaceBright)
            assertEquals(tokens.surface, scheme.surfaceContainerLow)
            assertEquals(tokens.ground, scheme.surfaceContainer)
            assertEquals(tokens.neutralFill, scheme.surfaceContainerHighest)
            assertEquals(tokens.divider, scheme.outlineVariant)
            assertEquals(tokens.textSecondary, scheme.onSurfaceVariant)
        }
    }
}
