package com.jawzf.pixeltoolkit.core.design.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

val DUO_PRESET_COLORS =
    listOf(
        "#FFFFFF", // Pure White
        "#00E676", // Vibrant Spring Green (Default Charging)
        "#4CAF50", // Standard Green
        "#00BCD4", // Cyan
        "#00E5FF", // Electric Cyan
        "#2196F3", // Blue
        "#3F51B5", // Indigo
        "#9C27B0", // Purple
        "#E91E63", // Pink
        "#FFEB3B", // Vivid Yellow (Default Low)
        "#FFD600", // Bright Gold
        "#FF9800", // Orange
        "#FF5722", // Deep Orange
        "#F44336", // Vivid Red (Default Critical)
        "#FF1744", // Bright Red
    )

val ISLAND_ICON_PASTEL_COLORS =
    listOf(
        "#FFFFFF",
        "#FFB3BA",
        "#FFDFBA",
        "#FFFFBA",
        "#BAFFC9",
        "#A0E7E5",
        "#BAE1FF",
        "#C7CEEA",
        "#D5BAFF",
        "#E0BBE4",
        "#FFC8DD",
        "#FFDAC1",
        "#CDEAC0",
        "#B4F8C8",
        "#FBE7C6",
    )

@Composable
fun ColorSwatchPicker(
    selectedColorHex: String,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    colors: List<String> = DUO_PRESET_COLORS,
    allowAuto: Boolean = false,
    autoColor: Color? = null,
) {
    val effectiveColors =
        remember(colors, allowAuto) {
            if (allowAuto) listOf("auto") + colors else colors
        }
    val view = LocalView.current

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceBright)
                .padding(vertical = 8.dp),
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(effectiveColors) { colorHex ->
                val isAuto = colorHex.equals("auto", ignoreCase = true)
                val isSelected = colorHex.equals(selectedColorHex, ignoreCase = true)

                Box(
                    modifier =
                        Modifier
                            .size(44.dp)
                            .semantics { selected = isSelected }
                            .clickable {
                                HapticUtil.performVirtualKeyHaptic(view)
                                onColorSelected(if (isAuto) "auto" else colorHex)
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSelected) {
                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .border(2.dp, ToolkitTheme.colors.text, CircleShape),
                        )
                    }

                    if (isAuto) {
                        val autoGradient =
                            Brush.linearGradient(
                                colors =
                                    if (autoColor != null) {
                                        listOf(autoColor, autoColor)
                                    } else {
                                        listOf(
                                            Color(0xFF00E676),
                                            Color(0xFF00E5FF),
                                        )
                                    },
                            )

                        val autoContentColor =
                            if (autoColor != null) {
                                if (autoColor.luminance() > 0.5f) Color.Black else Color.White
                            } else {
                                Color(0xFF003822)
                            }

                        Box(
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(autoGradient)
                                    .border(1.dp, ToolkitTheme.colors.fieldBorder, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_check_24),
                                    contentDescription = stringResource(R.string.duo_battery_charging_color_auto),
                                    tint = autoContentColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            } else {
                                Text(
                                    text = "A",
                                    color = autoContentColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    } else {
                        val parsedColor =
                            try {
                                Color(android.graphics.Color.parseColor(colorHex))
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                        val isLightColor =
                            try {
                                val c = android.graphics.Color.parseColor(colorHex)
                                val r = android.graphics.Color.red(c) / 255.0
                                val g = android.graphics.Color.green(c) / 255.0
                                val b = android.graphics.Color.blue(c) / 255.0
                                (0.299 * r + 0.587 * g + 0.114 * b) > 0.5
                            } catch (_: Exception) {
                                false
                            }

                        Box(
                            modifier =
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .border(1.dp, ToolkitTheme.colors.fieldBorder, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_check_24),
                                    contentDescription = null,
                                    tint = if (isLightColor) Color.Black else Color.White,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
