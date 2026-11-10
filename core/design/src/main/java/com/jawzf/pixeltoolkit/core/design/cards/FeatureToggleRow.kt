package com.jawzf.pixeltoolkit.core.design.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.components.IconTile
import com.jawzf.pixeltoolkit.core.design.components.toolkitSwitchColors
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureToggleRow(
    title: String,
    iconRes: Int,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchValue: Boolean,
    onSwitchChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        onClick = { onCheckedChange(!isChecked) },
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        leadingContent = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = onCheckedChange,
                )
                IconTile {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = ToolkitTheme.colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        },
        trailingContent = {
            Switch(
                checked = switchValue,
                onCheckedChange = onSwitchChange,
                enabled = isChecked,
                colors = toolkitSwitchColors(),
            )
        },
        colors =
            ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surfaceBright,
            ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        content = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
    )
}
