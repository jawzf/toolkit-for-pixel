package com.jawzf.pixeltoolkit.feature.notifications.lighting.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.action.NotificationLightingColorMode
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentLabel
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack

@Composable
fun NotificationLightingColorModePicker(
    selectedMode: NotificationLightingColorMode,
    onModeSelected: (NotificationLightingColorMode) -> Unit,
    modifier: Modifier = Modifier,
    options: List<Pair<Int, NotificationLightingColorMode>> =
        listOf(
            R.string.color_mode_system to NotificationLightingColorMode.SYSTEM,
            R.string.color_mode_custom to NotificationLightingColorMode.CUSTOM,
            R.string.color_mode_app_specific to NotificationLightingColorMode.APP_SPECIFIC,
        ),
) {
    val labels = options.map { stringResource(it.first) }
    val modes = options.map { it.second }

    val selectedIndex = modes.indexOf(selectedMode).coerceAtLeast(0)

    SegmentedTrack(
        count = labels.size,
        modifier =
            modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                    shape = RoundedCornerShape(MaterialTheme.shapes.extraSmall.bottomEnd),
                ).padding(horizontal = 16.dp, vertical = 12.dp),
    ) { index ->
        val selected = selectedIndex == index
        Segment(
            selected = selected,
            selectionModifier =
                Modifier.selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = {
                        onModeSelected(modes[index])
                    },
                ),
        ) {
            SegmentLabel(
                text = labels[index],
                selected = selected,
            )
        }
    }
}
