package com.jawzf.pixeltoolkit.core.design.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil

@Composable
fun ToolkitTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    pitchBlackTheme: Boolean = false,
    accent: Accent = Accent.Blue,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val accentColor =
        if (accent == Accent.Wallpaper && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)).primary
        } else {
            accentColor(accent, darkTheme)
        }
    val colors =
        remember(accentColor, darkTheme, pitchBlackTheme) {
            toolkitColors(accentColor, darkTheme, pitchBlackTheme)
        }

    val gatedHaptics = HapticUtil.gate(context, LocalHapticFeedback.current)
    CompositionLocalProvider(
        LocalHapticFeedback provides gatedHaptics,
        LocalToolkitColors provides colors,
    ) {
        MaterialTheme(
            colorScheme = colors.toColorScheme(),
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

object ToolkitTheme {
    val colors: ToolkitColors
        @Composable
        @ReadOnlyComposable
        get() = LocalToolkitColors.current
}
