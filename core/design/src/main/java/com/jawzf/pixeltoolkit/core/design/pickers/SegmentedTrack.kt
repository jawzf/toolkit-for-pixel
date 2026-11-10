package com.jawzf.pixeltoolkit.core.design.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

/** Neutral track hosting segmented picker options. */
@Composable
fun SegmentedTrack(
    count: Int,
    modifier: Modifier = Modifier,
    segment: @Composable RowScope.(index: Int) -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ToolkitTheme.colors.neutralFill)
                .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(count) {
            segment(it)
        }
    }
}

/** Single segment slot within a [SegmentedTrack]. */
@Composable
fun RowScope.Segment(
    selected: Boolean,
    selectionModifier: Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (selected) ToolkitTheme.colors.surface else Color.Transparent)
                .then(selectionModifier),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Text label for a segment: primary text when selected, secondary otherwise. */
@Composable
fun SegmentLabel(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
) {
    val base = if (selected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium
    Text(
        text = text,
        style = base.merge(style),
        color = if (selected) ToolkitTheme.colors.text else ToolkitTheme.colors.textSecondary,
        maxLines = 1,
        modifier = modifier.basicMarquee(),
    )
}

/** Icon for a segment: accent when selected, secondary text colour otherwise. */
@Composable
fun SegmentIcon(
    painter: Painter,
    contentDescription: String?,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier.size(20.dp),
        tint = if (selected) ToolkitTheme.colors.accent else ToolkitTheme.colors.textSecondary,
    )
}
