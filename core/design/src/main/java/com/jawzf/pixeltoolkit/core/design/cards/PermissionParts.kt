package com.jawzf.pixeltoolkit.core.design.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil

@Composable
internal fun resolveText(value: Any?): String? =
    when (value) {
        is Int -> stringResource(id = value)
        is String -> value
        else -> null
    }

/** Instructions plus the optional secondary and Shizuku/Root text buttons under a missing permission. */
@Composable
internal fun PermissionExtras(
    instructions: Any?,
    secondaryLabel: Any?,
    onSecondary: (() -> Unit)?,
    shizukuLabel: Any?,
    shizukuEnabled: Boolean,
    onShizuku: (() -> Unit)?,
    textColor: Color,
    linkColor: Color,
    startPadding: Dp,
) {
    val view = LocalView.current
    val resolvedInstructions = resolveText(instructions)
    if (!resolvedInstructions.isNullOrEmpty()) {
        Text(
            text = resolvedInstructions,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            modifier = Modifier.padding(start = startPadding),
        )
    }

    val hasSecondary = secondaryLabel != null && onSecondary != null
    val hasShizuku = shizukuLabel != null && onShizuku != null
    if (hasSecondary || hasShizuku) {
        Row(
            modifier = Modifier.padding(start = startPadding),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (hasSecondary) {
                val resolvedSecondary = resolveText(secondaryLabel).orEmpty()
                TextButton(
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        onSecondary?.invoke()
                    },
                ) {
                    Text(resolvedSecondary, color = linkColor)
                }
            }
            if (hasShizuku) {
                val resolvedShizuku = resolveText(shizukuLabel).orEmpty()
                TextButton(
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        onShizuku?.invoke()
                    },
                    enabled = shizukuEnabled,
                ) {
                    if (shizukuEnabled) {
                        Text(resolvedShizuku, color = linkColor)
                    } else {
                        Text(resolvedShizuku)
                    }
                }
            }
        }
    }
}
