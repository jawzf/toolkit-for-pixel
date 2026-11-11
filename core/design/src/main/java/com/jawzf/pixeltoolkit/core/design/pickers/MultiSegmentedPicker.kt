package com.jawzf.pixeltoolkit.core.design.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil

@Composable
fun <T> MultiSegmentedPicker(
    items: List<T>,
    selectedItems: Set<T>,
    onItemsSelected: (Set<T>) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    allowEmpty: Boolean = true,
    title: Any? = null,
    description: Any? = null,
) {
    val view = LocalView.current

    Row(
        modifier =
            modifier
                .clip(shape)
                .background(color = MaterialTheme.colorScheme.surfaceBright)
                .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        SegmentedTrack(items.size) { index ->
            val item = items[index]
            val isSelected = selectedItems.contains(item)
            val label = labelProvider(item)

            Segment(
                selected = isSelected,
                selectionModifier =
                    Modifier.toggleable(
                        value = isSelected,
                        role = Role.Checkbox,
                        onValueChange = { checked ->
                            HapticUtil.performUIHaptic(view)
                            val newSelection =
                                if (checked) {
                                    selectedItems + item
                                } else {
                                    if (allowEmpty || selectedItems.size > 1) selectedItems - item else selectedItems
                                }
                            onItemsSelected(newSelection)
                        },
                    ),
            ) {
                SegmentLabel(
                    text = label,
                    selected = isSelected,
                )
            }
        }
    }
}
