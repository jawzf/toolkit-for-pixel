package com.jawzf.pixeltoolkit.core.design.containers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

fun resolveRoundedCardContainerColor(
    containerColor: Color,
    spacing: Dp,
    dividerColor: Color,
): Color =
    if (containerColor.isSpecified) {
        containerColor
    } else if (spacing <= 1.dp) {
        dividerColor
    } else {
        Color.Transparent
    }

@Composable
fun RoundedCardContainer(
    modifier: Modifier = Modifier,
    spacing: Dp = 1.dp,
    cornerRadius: Dp = 16.dp,
    containerColor: Color = Color.Unspecified,
    content: @Composable ColumnScope.() -> Unit,
) {
    val background =
        resolveRoundedCardContainerColor(
            containerColor = containerColor,
            spacing = spacing,
            dividerColor = ToolkitTheme.colors.divider,
        )
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(cornerRadius))
                .background(background),
        verticalArrangement = Arrangement.spacedBy(spacing),
        content = content,
    )
}
