package com.jawzf.pixeltoolkit.core.design.pickers

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
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType

@Composable
fun HapticFeedbackPicker(
    selectedFeedback: HapticFeedbackType,
    onFeedbackSelected: (HapticFeedbackType) -> Unit,
    modifier: Modifier = Modifier,
    options: List<Pair<Int, HapticFeedbackType>> =
        listOf<Pair<Int, HapticFeedbackType>>(
            R.string.haptic_none to HapticFeedbackType.NONE,
            R.string.haptic_subtle to HapticFeedbackType.SUBTLE,
            R.string.haptic_double to HapticFeedbackType.DOUBLE,
            R.string.haptic_click to HapticFeedbackType.CLICK,
        ),
) {
    val labels = options.map { it.first }
    val types = options.map { it.second }

    val selectedIndex = types.indexOf(selectedFeedback).coerceAtLeast(0)

    SegmentedTrack(
        count = options.size,
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
                        onFeedbackSelected(types[index])
                    },
                ),
        ) {
            SegmentLabel(
                text = stringResource(labels[index]),
                selected = selected,
            )
        }
    }
}
