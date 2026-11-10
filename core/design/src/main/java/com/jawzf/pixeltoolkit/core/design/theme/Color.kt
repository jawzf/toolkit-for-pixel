package com.jawzf.pixeltoolkit.core.design.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver

/** The accent the user picks. [Wallpaper] takes the wallpaper's primary colour on Android 12+, else falls back to [Blue]. */
enum class Accent { Blue, Teal, Violet, Wallpaper }

/** Quiet Utility colour tokens. Read them in composables with `ToolkitTheme.colors`. */
@Immutable
data class ToolkitColors(
    val ground: Color, // screen background
    val surface: Color, // grouped cards, sheets, fields
    val divider: Color, // inset dividers inside groups
    val fieldBorder: Color, // outlined fields, unselected chips
    val neutralFill: Color, // segmented picker track, neutral chips
    val text: Color,
    val textSecondary: Color,
    val chevron: Color,
    val accent: Color,
    val onAccent: Color, // text/icons on a solid accent fill
    val accentTint: Color, // opaque: accent at 10% (light) / 16% (dark) over surface
    val warningContainer: Color,
    val onWarningContainer: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val isDark: Boolean,
)

private val LightTokens =
    ToolkitColors(
        ground = Color(0xFFF5F4F0),
        surface = Color(0xFFFFFFFF),
        divider = Color(0xFFEEECE7),
        fieldBorder = Color(0xFFE4E2DC),
        neutralFill = Color(0xFFF0EFEA),
        text = Color(0xFF1B1C1E),
        textSecondary = Color(0xFF5E6168),
        chevron = Color(0xFF8A8D93),
        accent = Color(0xFF1F5FD1),
        onAccent = Color(0xFFFFFFFF),
        accentTint = Color(0xFF1F5FD1).copy(alpha = 0.10f).compositeOver(Color(0xFFFFFFFF)),
        warningContainer = Color(0xFFFBF0DC),
        onWarningContainer = Color(0xFF5C3A00),
        successContainer = Color(0xFFEDF6F0),
        onSuccessContainer = Color(0xFF1D6B45),
        isDark = false,
    )

private val DarkTokens =
    ToolkitColors(
        ground = Color(0xFF121211),
        surface = Color(0xFF1C1C1A),
        divider = Color(0xFF2A2926),
        fieldBorder = Color(0xFF34332F),
        neutralFill = Color(0xFF262522),
        text = Color(0xFFECEBE6),
        textSecondary = Color(0xFFA4A6AB),
        chevron = Color(0xFF7C7F85),
        accent = Color(0xFF7FA7F0),
        onAccent = Color(0xFF101114),
        accentTint = Color(0xFF7FA7F0).copy(alpha = 0.16f).compositeOver(Color(0xFF1C1C1A)),
        warningContainer = Color(0xFF3A2E14),
        onWarningContainer = Color(0xFFF2D49B),
        successContainer = Color(0xFF16281E),
        onSuccessContainer = Color(0xFF7FD1A4),
        isDark = true,
    )

fun accentColor(
    accent: Accent,
    dark: Boolean,
): Color =
    when (accent) {
        Accent.Blue, Accent.Wallpaper -> if (dark) Color(0xFF7FA7F0) else Color(0xFF1F5FD1)
        Accent.Teal -> if (dark) Color(0xFF5CC2B1) else Color(0xFF0F7B6C)
        Accent.Violet -> if (dark) Color(0xFFB39AF0) else Color(0xFF7A4BD1)
    }

fun toolkitColors(
    accent: Color,
    dark: Boolean,
    pitchBlack: Boolean = false,
): ToolkitColors {
    val base = if (dark) DarkTokens else LightTokens
    val surface = if (dark && pitchBlack) Color(0xFF111111) else base.surface
    val ground = if (dark && pitchBlack) Color(0xFF000000) else base.ground
    return base.copy(
        ground = ground,
        surface = surface,
        accent = accent,
        accentTint = accent.copy(alpha = if (dark) 0.16f else 0.10f).compositeOver(surface),
    )
}

fun ToolkitColors.toColorScheme(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = accentTint,
        onPrimaryContainer = accent,
        inversePrimary = accent,
        secondary = textSecondary,
        onSecondary = surface,
        secondaryContainer = neutralFill,
        onSecondaryContainer = text,
        tertiary = accent,
        onTertiary = onAccent,
        tertiaryContainer = accentTint,
        onTertiaryContainer = accent,
        background = ground,
        onBackground = text,
        surface = surface,
        onSurface = text,
        surfaceVariant = neutralFill,
        onSurfaceVariant = textSecondary,
        surfaceTint = surface,
        surfaceBright = surface,
        surfaceDim = ground,
        surfaceContainerLowest = surface,
        surfaceContainerLow = surface,
        // Inherited screens paint their background with surfaceContainer and cards with surfaceBright.
        surfaceContainer = ground,
        surfaceContainerHigh = neutralFill,
        surfaceContainerHighest = neutralFill,
        outline = fieldBorder,
        outlineVariant = divider,
        inverseSurface = text,
        inverseOnSurface = surface,
    )
}

val LocalToolkitColors = staticCompositionLocalOf { toolkitColors(accentColor(Accent.Blue, false), dark = false) }
