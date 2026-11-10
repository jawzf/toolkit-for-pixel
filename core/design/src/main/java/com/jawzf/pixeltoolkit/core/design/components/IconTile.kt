package com.jawzf.pixeltoolkit.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

@Composable
fun IconTile(
    modifier: Modifier = Modifier,
    showBackground: Boolean = true,
    content: @Composable () -> Unit,
) {
    val tileModifier =
        if (showBackground) {
            modifier
                .size(36.dp)
                .background(ToolkitTheme.colors.accentTint, ToolkitShapes.iconTile)
        } else {
            modifier.size(36.dp)
        }
    Box(
        modifier = tileModifier,
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
