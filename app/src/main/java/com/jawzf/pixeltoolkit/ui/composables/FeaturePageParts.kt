package com.jawzf.pixeltoolkit.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.PermissionCard
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

/** Full-screen list pages that keep the floating toolbar for now. */
val FLOATING_TOOLBAR_PAGE_IDS = setOf("Quick settings tiles", "Activity launcher", "Location reached")

/** Feature pages that show [FeatureMasterCard]; their settings UI no longer draws its own on/off row. */
val MASTER_CARD_FEATURE_IDS = setOf("Notification lighting")

@Composable
fun PageTopBar(
    onBack: () -> Unit,
    onHelp: (() -> Unit)?,
    modifier: Modifier = Modifier,
    title: String? = null,
    @DrawableRes actionIconRes: Int? = null,
    actionDescription: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopBarIconButton(
            R.drawable.rounded_arrow_back_24,
            stringResource(R.string.content_desc_back),
            onBack,
        )
        if (title != null) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = ToolkitTheme.colors.text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            )
        } else {
            Spacer(Modifier.weight(1f))
        }
        if (actionIconRes != null && onAction != null) {
            TopBarIconButton(
                actionIconRes,
                actionDescription,
                onAction,
            )
        }
        if (onHelp != null) {
            TopBarIconButton(
                R.drawable.rounded_help_24,
                stringResource(R.string.label_help_guide),
                onHelp,
            )
        }
    }
}

@Composable
private fun TopBarIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    val view = LocalView.current
    IconButton(
        onClick = {
            HapticUtil.performVirtualKeyHaptic(view)
            onClick()
        },
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = ToolkitTheme.colors.text,
        )
    }
}

@Composable
fun FeatureMasterCard(
    title: String,
    description: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = ToolkitTheme.colors
    val view = LocalView.current
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(ToolkitShapes.group)
                .background(colors.accent)
                .toggleable(
                    value = checked,
                    role = Role.Switch,
                    onValueChange = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        onCheckedChange(it)
                    },
                ).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onAccent,
            )
            if (!description.isNullOrEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onAccent.copy(alpha = 0.9f),
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = colors.accent,
                    checkedTrackColor = colors.onAccent,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = colors.onAccent,
                    uncheckedTrackColor = colors.onAccent.copy(alpha = 0.35f),
                    uncheckedBorderColor = Color.Transparent,
                ),
        )
    }
}

@Composable
fun FeatureSetupBanner(
    missing: List<PermissionItem>,
    onFix: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val names =
        missing.map { item ->
            val title = item.title
            if (title is Int) stringResource(title) else title.toString()
        }
    PermissionCard(
        iconRes = R.drawable.rounded_warning_24,
        title = pluralStringResource(R.plurals.feature_setup_needed, missing.size, missing.size),
        description = names.joinToString(", "),
        dependentFeatures = emptyList(),
        isGranted = false,
        onActionClick = onFix,
        modifier = modifier,
    )
}
