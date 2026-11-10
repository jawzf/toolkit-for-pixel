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
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentIcon
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack

@Composable
fun NotificationLightingSystemModePicker(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = listOf(0, 1, 2) // 0: Charging, 1: Auth, 2: Custom
    val icons =
        listOf(
            R.drawable.rounded_keyboard_double_arrow_up_24,
            R.drawable.rounded_center_focus_strong_24,
            R.drawable.rounded_my_location_24,
        )
    val labels = listOf("Charging", "Auth", "Custom") // For semantics/descriptions

    val view = LocalView.current

    RoundedCardContainer(modifier = Modifier) {
        SegmentedTrack(
            count = modes.size,
            modifier =
                modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceBright,
                        shape = RoundedCornerShape(MaterialTheme.shapes.extraSmall.bottomEnd),
                    ).padding(horizontal = 16.dp, vertical = 12.dp),
        ) { index ->
            val mode = modes[index]
            val selected = selectedMode == mode
            Segment(
                selected = selected,
                selectionModifier =
                    Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            onModeSelected(mode)
                        },
                    ),
            ) {
                SegmentIcon(
                    painter = painterResource(id = icons[index]),
                    contentDescription = labels[index],
                    selected = selected,
                )
            }
        }
    }
}
