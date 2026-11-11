package com.jawzf.pixeltoolkit.feature.display.ondisplay.ui

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
fun AlbumArtModePicker(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = listOf("default", "fill")
    val icons =
        listOf(
            R.drawable.rounded_award_star_24,
            R.drawable.rounded_fullscreen_portrait_24,
        )
    val labels = listOf("Default", "Fill")

    val view = LocalView.current

    RoundedCardContainer(modifier = modifier) {
        SegmentedTrack(
            count = modes.size,
            modifier =
                Modifier
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
