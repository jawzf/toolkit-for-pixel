package com.jawzf.pixeltoolkit.core.design.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil

@Composable
fun <T> SegmentedPicker(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    iconProvider: (@Composable (T) -> Unit)? = null,
    textStyleProvider: ((T) -> TextStyle)? = null,
    modifier: Modifier = Modifier,
    cornerShape: CornerSize = MaterialTheme.shapes.extraSmall.bottomEnd,
    containerColor: Color = MaterialTheme.colorScheme.surfaceBright,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    title: Any? = null,
    description: Any? = null,
) {
    val view = LocalView.current

    Row(
        modifier =
            modifier
                .clip(RoundedCornerShape(cornerShape))
                .background(color = containerColor)
                .padding(contentPadding),
    ) {
        SegmentedTrack(items.size) { index ->
            val item = items[index]
            val selected = item == selectedItem
            val label = labelProvider(item)

            Segment(
                selected = selected,
                selectionModifier =
                    Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = {
                            HapticUtil.performUIHaptic(view)
                            onItemSelected(item)
                        },
                    ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (iconProvider != null) {
                        iconProvider(item)
                    }
                    if (label.isNotEmpty()) {
                        if (iconProvider != null) {
                            Spacer(Modifier.padding(end = 8.dp))
                        }
                        SegmentLabel(
                            text = label,
                            selected = selected,
                            style = textStyleProvider?.invoke(item) ?: TextStyle.Default,
                        )
                    }
                }
            }
        }
    }
}
