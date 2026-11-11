package com.jawzf.pixeltoolkit.feature.notifications.lighting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingStyle
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentIcon
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack

@Composable
fun NotificationLightingStylePicker(
    selectedStyle: NotificationLightingStyle,
    onStyleSelected: (NotificationLightingStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    val styles =
        listOf(
            NotificationLightingStyle.STROKE,
            NotificationLightingStyle.GLOW,
            NotificationLightingStyle.INDICATOR,
            NotificationLightingStyle.SWEEP,
            NotificationLightingStyle.SYSTEM,
            NotificationLightingStyle.RIPPLE,
            NotificationLightingStyle.DASH,
        )
    val icons =
        listOf(
            R.drawable.rounded_rounded_corner_24,
            R.drawable.rounded_blur_linear_24,
            R.drawable.rounded_circles_24,
            R.drawable.rounded_target_24,
            R.drawable.rounded_mobile_24,
            R.drawable.rounded_auto_awesome_24,
            R.drawable.rounded_crop_portrait_24,
        )
    val view = LocalView.current

    val selectedIndex = styles.indexOf(selectedStyle).coerceAtLeast(0)

    RoundedCardContainer(modifier = Modifier) {
        SegmentedTrack(
            count = styles.size,
            modifier =
                modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceBright,
                        shape = RoundedCornerShape(MaterialTheme.shapes.extraSmall.bottomEnd),
                    ).padding(horizontal = 16.dp, vertical = 12.dp),
        ) { index ->
            val style = styles[index]
            val selected = selectedIndex == index
            Segment(
                selected = selected,
                selectionModifier =
                    Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            onStyleSelected(style)
                        },
                    ),
            ) {
                SegmentIcon(
                    painter = painterResource(id = icons[index]),
                    contentDescription = style.name,
                    selected = selected,
                )
            }
        }
    }
}
