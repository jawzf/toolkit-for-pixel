package com.jawzf.pixeltoolkit.core.design.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.sheets.stepActionLabel
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

@Composable
fun PermissionCard(
    iconRes: Int,
    title: Any, // Can be Int (Resource ID) or String
    dependentFeatures: List<Any>, // List of Int or String
    actionLabel: Any = R.string.perm_action_grant, // Can be Int or String
    isGranted: Boolean,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryActionLabel: Any? = null, // Can be Int or String
    onSecondaryActionClick: (() -> Unit)? = null,
    shizukuActionLabel: Any? = null,
    shizukuActionEnabled: Boolean = false,
    onShizukuActionClick: (() -> Unit)? = null,
    instructions: Any? = null,
    description: Any? = null,
) {
    val colors = ToolkitTheme.colors
    val container = if (isGranted) colors.successContainer else colors.warningContainer
    val onColor = if (isGranted) colors.onSuccessContainer else colors.onWarningContainer
    val view = LocalView.current

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(container, ToolkitShapes.field)
                .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                painter = painterResource(if (isGranted) R.drawable.rounded_check_circle_24 else iconRes),
                contentDescription = null,
                tint = onColor,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                val resolvedTitle = resolveText(title).orEmpty()
                Text(
                    text = resolvedTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = onColor,
                )
                val resolvedDescription = resolveText(description)
                if (!resolvedDescription.isNullOrEmpty()) {
                    Text(
                        text = resolvedDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = onColor,
                    )
                }
                if (dependentFeatures.isNotEmpty()) {
                    val joined =
                        dependentFeatures
                            .mapNotNull { resolveText(it) }
                            .filter { it.isNotEmpty() }
                            .joinToString(", ")
                    if (joined.isNotEmpty()) {
                        Text(
                            text = stringResource(R.string.perm_used_by, joined),
                            style = MaterialTheme.typography.bodySmall,
                            color = onColor.copy(alpha = 0.8f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            if (!isGranted) {
                val resolvedActionLabel = resolveText(stepActionLabel(actionLabel)).orEmpty()
                Button(
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        onActionClick()
                    },
                    modifier = Modifier.heightIn(min = 36.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = colors.surface,
                            contentColor = onColor,
                        ),
                ) {
                    Text(resolvedActionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (!isGranted) {
            PermissionExtras(
                instructions,
                secondaryActionLabel,
                onSecondaryActionClick,
                shizukuActionLabel,
                shizukuActionEnabled,
                onShizukuActionClick,
                textColor = onColor,
                linkColor = onColor,
                startPadding = 32.dp,
            )
        }
    }
}
