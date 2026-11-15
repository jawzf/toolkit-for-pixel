package com.jawzf.pixeltoolkit.core.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

object ToolkitButtonDefaults {
    val PairSpacing = 10.dp
    val MinHeight = 48.dp
}

@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val c = ToolkitTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = ToolkitButtonDefaults.MinHeight),
        enabled = enabled,
        shape = ToolkitShapes.pill,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = c.accent,
                contentColor = c.onAccent,
            ),
        contentPadding = PaddingValues(horizontal = 16.dp),
        content = content,
    )
}

@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    val c = ToolkitTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = ToolkitButtonDefaults.MinHeight),
        enabled = enabled,
        shape = ToolkitShapes.pill,
        colors =
            ButtonDefaults.outlinedButtonColors(
                containerColor = c.surface,
                contentColor = c.text,
            ),
        border = BorderStroke(1.dp, c.fieldBorder),
        contentPadding = PaddingValues(horizontal = 16.dp),
        content = content,
    )
}
