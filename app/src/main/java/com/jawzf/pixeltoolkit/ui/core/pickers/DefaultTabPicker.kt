package com.jawzf.pixeltoolkit.ui.core.pickers

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.pickers.Segment
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentIcon
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentLabel
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedTrack
import com.jawzf.pixeltoolkit.domain.DIYTabs

@Composable
fun DefaultTabPicker(
    selectedTab: DIYTabs,
    onTabSelected: (DIYTabs) -> Unit,
    modifier: Modifier = Modifier,
    options: List<DIYTabs> = DIYTabs.entries,
) {
    val view = LocalView.current
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.extraSmall)
                .background(MaterialTheme.colorScheme.surfaceBright),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        ListItem(
            onClick = {},
            onLongClick = null,
            modifier = Modifier.fillMaxWidth(),
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_widgets_24),
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
                    text = stringResource(R.string.label_default_tab),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
        )

        SegmentedTrack(
            count = options.size,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
        ) { index ->
            val tab = options[index]
            val selected = selectedTab == tab
            Segment(
                selected = selected,
                selectionModifier =
                    Modifier.selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = {
                            HapticUtil.performUIHaptic(view)
                            onTabSelected(tab)
                        },
                    ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SegmentIcon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = null,
                        selected = selected,
                    )
                    Spacer(Modifier.width(6.dp))
                    SegmentLabel(
                        text =
                            if (tab == DIYTabs.FREEZE) {
                                stringResource(R.string.tab_freeze_title)
                            } else {
                                stringResource(
                                    tab.title,
                                )
                            },
                        selected = selected,
                    )
                }
            }
        }
    }
}
