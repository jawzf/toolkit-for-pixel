package com.jawzf.pixeltoolkit.ui.core.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentLabel
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack

@Composable
fun RestoreModePicker(
    selectedMode: String,
    onModeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    options: List<String> = listOf("Auto", "Notify"),
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceBright),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        ListItem(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_timer_24),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
            contentPadding =
                PaddingValues(
                    horizontal = 16.dp,
                    vertical = 16.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                ),
            content = {
                Text(
                    text = stringResource(R.string.label_restore_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
        )

        SegmentedTrack(
            count = options.size,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
        ) { index ->
            val mode = options[index]
            val selected = selectedMode == mode
            Segment(
                selected = selected,
                selectionModifier =
                    Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onModeSelected(mode) },
                    ),
            ) {
                SegmentLabel(
                    text = mode,
                    selected = selected,
                )
            }
        }
    }
}
