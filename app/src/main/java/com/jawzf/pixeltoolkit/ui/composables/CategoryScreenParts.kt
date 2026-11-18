package com.jawzf.pixeltoolkit.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

val CATEGORY_FEATURE_IDS =
    setOf(
        "Display",
        "Notifications",
        "Sound",
        "Input",
        "Widgets",
        "Power and battery",
        "Security",
    )

enum class CategoryFilter { All, On, NeedsSetup }

/** "On" only matches rows with a toggle, and "Needs setup" matches any row whose permission sheet has a step not done. */
fun CategoryFilter.matches(
    showToggle: Boolean,
    isOn: Boolean,
    needsSetup: Boolean,
): Boolean =
    when (this) {
        CategoryFilter.All -> true
        CategoryFilter.On -> showToggle && isOn
        CategoryFilter.NeedsSetup -> needsSetup
    }

@Composable
fun CategoryTopBar(
    onBack: () -> Unit,
    onHelp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onBack()
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_arrow_back_24),
                contentDescription = stringResource(R.string.content_desc_back),
                tint = ToolkitTheme.colors.text,
            )
        }
        Spacer(Modifier.weight(1f))
        IconButton(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onHelp()
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_help_24),
                contentDescription = stringResource(R.string.label_help_guide),
                tint = ToolkitTheme.colors.text,
            )
        }
    }
}

@Composable
fun CategoryHeader(
    @DrawableRes iconRes: Int,
    title: String,
    total: Int,
    onCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(48.dp)
                    .background(ToolkitTheme.colors.accentTint, ToolkitShapes.field),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = ToolkitTheme.colors.accent,
                modifier = Modifier.size(24.dp),
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = ToolkitTheme.colors.text,
            )
            Text(
                text =
                    stringResource(
                        R.string.category_summary,
                        pluralStringResource(R.plurals.category_feature_count, total, total),
                        onCount,
                    ),
                style = MaterialTheme.typography.bodyMedium,
                color = ToolkitTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
fun CategoryFilterChips(
    selected: CategoryFilter,
    onSelect: (CategoryFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CategoryFilter.entries.forEach { filter ->
            val labelRes =
                when (filter) {
                    CategoryFilter.All -> R.string.category_filter_all
                    CategoryFilter.On -> R.string.category_filter_on
                    CategoryFilter.NeedsSetup -> R.string.category_filter_needs_setup
                }
            CategoryChip(
                label = stringResource(labelRes),
                selected = filter == selected,
                onClick = { onSelect(filter) },
            )
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    val colors = ToolkitTheme.colors
    Box(
        modifier = Modifier.heightIn(min = 44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .height(36.dp)
                    .clip(ToolkitShapes.pill)
                    .background(if (selected) colors.text else colors.surface)
                    .then(
                        if (selected) {
                            Modifier
                        } else {
                            Modifier.border(1.dp, colors.fieldBorder, ToolkitShapes.pill)
                        },
                    ).selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            onClick()
                        },
                    ).padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (selected) colors.ground else colors.text,
            )
        }
    }
}

@Composable
fun CategoryEmpty(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.category_filter_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = ToolkitTheme.colors.textSecondary,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
    )
}
