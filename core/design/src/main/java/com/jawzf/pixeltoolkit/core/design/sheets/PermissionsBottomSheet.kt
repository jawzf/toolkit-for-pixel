package com.jawzf.pixeltoolkit.core.design.sheets

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.PermissionExtras
import com.jawzf.pixeltoolkit.core.design.cards.resolveText
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

data class PermissionItem(
    val iconRes: Int,
    val title: Any, // Can be Int (Resource ID) or String
    val description: Any, // Can be Int or String
    val dependentFeatures: List<Any> = emptyList(), // List of Int or String
    val actionLabel: Any? = null, // Can be Int or String
    val action: (() -> Unit)? = null,
    val secondaryActionLabel: Any? = null, // Can be Int or String
    val secondaryAction: (() -> Unit)? = null,
    val isGranted: Boolean = false,
    val shizukuActionLabel: Any? = null,
    val shizukuActionEnabled: Boolean = false,
    val shizukuAction: (() -> Unit)? = null,
    val instructions: Any? = null,
)

/** True when every step is granted, so "Turn on" can be pressed. */
fun canTurnOn(permissions: List<PermissionItem>): Boolean = permissions.all { it.isGranted }

/** Label for a missing step's button: generic grant labels become a short "Grant"; specific ones (e.g. Copy ADB) stay. */
fun stepActionLabel(actionLabel: Any?): Any =
    when (actionLabel) {
        null, R.string.perm_action_grant, R.string.perm_action_enable -> R.string.perm_action_grant_short
        else -> actionLabel
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsBottomSheet(
    onDismissRequest: () -> Unit,
    featureTitle: Any, // Can be Int (Resource ID) or String
    permissions: List<PermissionItem>,
    onHelpClick: () -> Unit = {},
    caveat: Any? = null, // Int or String; shown in a tinted note when set
    onTurnOn: () -> Unit = onDismissRequest,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val colors = ToolkitTheme.colors
    val resolvedTitle = resolveText(featureTitle).orEmpty()
    val resolvedCaveat = resolveText(caveat)

    ToolkitBottomSheet(onDismissRequest = onDismissRequest, containerColor = colors.surface) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            SheetHeader(
                title = resolvedTitle,
                onViewAll = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    onDismissRequest()
                    val intent =
                        Intent()
                            .setClassName(context, SETTINGS_ACTIVITY)
                            .putExtra("expand_permissions", true)
                    context.startActivity(intent)
                },
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                permissions.forEachIndexed { index, item ->
                    PermissionStep(number = index + 1, item = item)
                }
            }

            if (resolvedCaveat != null) {
                Text(
                    text = resolvedCaveat,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.text,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .background(colors.accentTint, ToolkitShapes.field)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                )
            }

            SheetButtons(
                turnOnEnabled = canTurnOn(permissions),
                onNotNow = onDismissRequest,
                onTurnOn = onTurnOn,
            )
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    onViewAll: () -> Unit,
) {
    val colors = ToolkitTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.perm_sheet_title, title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.text,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onViewAll) {
                Text(stringResource(R.string.action_view_all))
            }
        }
        Text(
            text = stringResource(R.string.perm_sheet_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun SheetButtons(
    turnOnEnabled: Boolean,
    onNotNow: () -> Unit,
    onTurnOn: () -> Unit,
) {
    val view = LocalView.current
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onNotNow()
            },
            modifier = Modifier.weight(1f).height(48.dp),
        ) {
            Text(stringResource(R.string.action_not_now))
        }
        Button(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onTurnOn()
            },
            enabled = turnOnEnabled,
            modifier = Modifier.weight(1f).height(48.dp),
        ) {
            Text(stringResource(R.string.action_turn_on))
        }
    }
}

@Composable
private fun StepMarker(
    number: Int,
    done: Boolean,
) {
    val colors = ToolkitTheme.colors
    val markerModifier =
        if (done) {
            Modifier.background(colors.onSuccessContainer, CircleShape)
        } else {
            Modifier.border(2.dp, colors.chevron, CircleShape)
        }
    Box(
        modifier = Modifier.size(28.dp).then(markerModifier),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(
                painter = painterResource(R.drawable.rounded_check_24),
                contentDescription = null,
                tint = colors.successContainer,
                modifier = Modifier.size(16.dp),
            )
        } else {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
            )
        }
    }
}

@Composable
private fun PermissionStep(
    number: Int,
    item: PermissionItem,
) {
    val colors = ToolkitTheme.colors
    val view = LocalView.current
    val containerColor = if (item.isGranted) colors.successContainer else colors.ground

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(containerColor, ToolkitShapes.group)
                .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StepMarker(number = number, done = item.isGranted)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = resolveText(item.title).orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.text,
                )
                val desc = resolveText(item.description)
                if (!desc.isNullOrEmpty()) {
                    val descColor = if (item.isGranted) colors.onSuccessContainer else colors.textSecondary
                    Text(text = desc, style = MaterialTheme.typography.bodySmall, color = descColor)
                }
            }

            if (!item.isGranted && item.action != null) {
                Button(
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        item.action.invoke()
                    },
                    modifier = Modifier.heightIn(min = 36.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent, contentColor = colors.onAccent),
                ) {
                    Text(resolveText(stepActionLabel(item.actionLabel)).orEmpty(), style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        if (!item.isGranted) {
            PermissionExtras(
                item.instructions,
                item.secondaryActionLabel,
                item.secondaryAction,
                item.shizukuActionLabel,
                item.shizukuActionEnabled,
                item.shizukuAction,
                textColor = colors.textSecondary,
                linkColor = colors.accent,
                startPadding = 40.dp,
            )
        }
    }
}

private const val SETTINGS_ACTIVITY = "com.jawzf.pixeltoolkit.SettingsActivity"
