package com.jawzf.pixeltoolkit.core.design.cards

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

private val GOOGLE_SYSTEM_USER_APPS =
    setOf(
        "com.google.android.apps.scone",
        "com.google.android.marvin.talkback",
        "com.google.android.projection.gearhead",
        "com.google.android.as",
        "com.google.android.contactkeys",
        "com.google.android.safetycore",
        "com.google.android.webview",
        "com.google.android.captiveportallogin",
        "com.google.ambient.streaming",
        "com.google.android.apps.pixel.dcservice",
        "com.google.android.apps.turbo",
        "com.google.android.apps.work.clouddpc",
        "com.google.android.apps.diagnosticstool",
        "com.google.android.apps.wellbeing",
        "com.google.android.documentsui",
        "com.google.android.odad",
        "com.google.android.gms",
        "com.google.ar.core",
        "com.google.vending",
        "com.google.android.apps.carrier.carrierwifi",
        "com.google.android.modulemetadata",
        "com.google.android.networkstack",
        "com.google.android.apps.safetyhub",
        "com.google.intelligence.sense",
        "com.google.android.apps.camera.services",
        "com.google.android.apps.nexuslauncher",
        "com.google.android.apps.pixel.support",
        "com.google.android.as.oss",
        "com.android.settings",
        "com.google.android.settings.intelligence",
        "com.android.stk",
        "com.google.android.soundpicker",
        "com.google.mainline.telemetry",
        "com.google.android.apps.messaging",
    )

@Composable
fun AppToggleItem(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    enabled: Boolean = true,
    onDisabledClick: (() -> Unit)? = null,
    showToggle: Boolean = true,
    onClick: (() -> Unit)? = null,
    icon: ImageBitmap? = null,
    packageName: String? = null,
    isSystemApp: Boolean = false,
) {
    val view = LocalView.current

    val shouldShowSystemTag =
        remember(packageName) {
            packageName != null && GOOGLE_SYSTEM_USER_APPS.contains(packageName)
        }

    val onClickAction = {
        if (enabled) {
            HapticUtil.performVirtualKeyHaptic(view)
            onCheckedChange(!isChecked)
        } else if (onDisabledClick != null) {
            HapticUtil.performVirtualKeyHaptic(view)
            onDisabledClick()
        }
    }

    if (showToggle) {
        ListItem(
            checked = isChecked && enabled,
            onCheckedChange = { checked ->
                if (enabled) {
                    HapticUtil.performVirtualKeyHaptic(view)
                    onCheckedChange(checked)
                } else if (onDisabledClick != null) {
                    HapticUtil.performVirtualKeyHaptic(view)
                    onDisabledClick()
                }
            },
            onLongClick = null,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            leadingContent = { AppIcon(icon, title) },
            content = { AppTitle(title, shouldShowSystemTag) },
            supportingContent = description?.let { { AppDescription(it) } },
            trailingContent = {
                Switch(
                    checked = if (enabled) isChecked else false,
                    onCheckedChange = null,
                    enabled = enabled,
                )
            },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                ),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        )
    } else {
        ListItem(
            onClick = onClickAction,
            onLongClick = null,
            enabled = enabled,
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            leadingContent = { AppIcon(icon, title) },
            content = { AppTitle(title, shouldShowSystemTag) },
            supportingContent = description?.let { { AppDescription(it) } },
            colors =
                ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                ),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp),
        )
    }
}

@Composable
private fun AppIcon(
    icon: ImageBitmap?,
    title: String,
) {
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = title,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit,
        )
    } else {
        Spacer(modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun AppTitle(
    title: String,
    showSystemTag: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = ToolkitTheme.colors.text,
        )
        if (showSystemTag) {
            Box(
                modifier =
                    Modifier
                        .size(18.dp)
                        .background(
                            color = ToolkitTheme.colors.accent,
                            shape = CircleShape,
                        ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.round_android_24),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = ToolkitTheme.colors.onAccent,
                )
            }
        }
    }
}

@Composable
private fun AppDescription(description: String) {
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = ToolkitTheme.colors.textSecondary,
    )
}
