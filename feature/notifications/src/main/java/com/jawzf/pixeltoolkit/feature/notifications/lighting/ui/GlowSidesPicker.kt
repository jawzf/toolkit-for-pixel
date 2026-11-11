package com.jawzf.pixeltoolkit.feature.notifications.lighting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingSide
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentIcon
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack

@Composable
fun GlowSidesPicker(
    selectedSides: Set<NotificationLightingSide>,
    onSideToggled: (NotificationLightingSide, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options =
        listOf(
            NotificationLightingSide.LEFT to R.drawable.rounded_border_left_24,
            NotificationLightingSide.TOP to R.drawable.rounded_border_top_24,
            NotificationLightingSide.RIGHT to R.drawable.rounded_border_right_24,
            NotificationLightingSide.BOTTOM to R.drawable.rounded_border_bottom_24,
        )
    val view = LocalView.current

    SegmentedTrack(
        count = options.size,
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                    shape = RoundedCornerShape(MaterialTheme.shapes.extraSmall.bottomEnd),
                ).padding(horizontal = 16.dp, vertical = 12.dp),
    ) { index ->
        val (side, iconRes) = options[index]
        val selected = selectedSides.contains(side)
        Segment(
            selected = selected,
            selectionModifier =
                Modifier.toggleable(
                    value = selected,
                    role = Role.Checkbox,
                    onValueChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        onSideToggled(side, checked)
                    },
                ),
        ) {
            SegmentIcon(
                painter = painterResource(id = iconRes),
                contentDescription = side.name,
                selected = selected,
            )
        }
    }
}
