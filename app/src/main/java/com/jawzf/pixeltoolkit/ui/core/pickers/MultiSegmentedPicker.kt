package com.jawzf.pixeltoolkit.ui.core.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.utils.HapticUtil

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> MultiSegmentedPicker(
    items: List<T>,
    selectedItems: Set<T>,
    onItemsSelected: (Set<T>) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(24.dp),
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
                .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        val modifiers = List(items.size) { Modifier.weight(1f) }

        items.forEachIndexed { index, item ->
            val isSelected = selectedItems.contains(item)
            val label = labelProvider(item)

            Box(modifier = modifiers[index]) {
                ToggleButton(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        HapticUtil.performUIHaptic(view)
                        val newSelection =
                            if (checked) {
                                selectedItems + item
                            } else {
                                if (allowEmpty || selectedItems.size > 1) selectedItems - item else selectedItems
                            }
                        onItemsSelected(newSelection)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics { role = Role.Checkbox },
                    shapes =
                        when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            items.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                ) {
                    Text(
                        label,
                        fontSize = dimensionResource(R.dimen.font_small).value.sp,
                        modifier = Modifier.basicMarquee(),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
