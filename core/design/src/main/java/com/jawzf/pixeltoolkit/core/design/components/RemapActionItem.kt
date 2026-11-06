package com.jawzf.pixeltoolkit.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil

@Composable
fun RemapActionItem(
    title: Any, // Can be Int or String
    iconRes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    hasSettings: Boolean = false,
    onSettingsClick: () -> Unit = {},
) {
    val view = LocalView.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable {
                    HapticUtil.performUIHaptic(view)
                    onClick()
                }.background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                    shape = RoundedCornerShape(MaterialTheme.shapes.extraSmall.bottomEnd),
                ).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
        )

        val resolvedTitle =
            when (title) {
                is Int -> stringResource(id = title)
                is String -> title
                else -> ""
            }

        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = resolvedTitle,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            text = resolvedTitle,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (hasSettings && isSelected) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.rounded_settings_24),
                    contentDescription = stringResource(R.string.content_desc_settings),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
