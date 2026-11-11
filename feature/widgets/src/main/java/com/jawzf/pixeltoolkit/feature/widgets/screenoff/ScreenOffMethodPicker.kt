package com.jawzf.pixeltoolkit.feature.widgets.screenoff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.action.ScreenOffMethod
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentLabel
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack

@Composable
fun ScreenOffMethodPicker(
    selectedMethod: ScreenOffMethod,
    onMethodSelected: (ScreenOffMethod) -> Unit,
    modifier: Modifier = Modifier,
    options: List<Pair<Int, ScreenOffMethod>> =
        listOf(
            R.string.screen_off_method_accessibility to ScreenOffMethod.ACCESSIBILITY,
            R.string.screen_off_method_input to ScreenOffMethod.INPUT,
        ),
) {
    val labels = options.map { it.first }
    val types = options.map { it.second }

    val selectedIndex = types.indexOf(selectedMethod).coerceAtLeast(0)
    val view = LocalView.current // Get the current View for haptic feedback

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
                        onMethodSelected(types[index])
                        HapticUtil.performLightHaptic(view)
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
