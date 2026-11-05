package com.jawzf.pixeltoolkit.ui.features.freeze

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.MainActivity
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.apps.NotificationApp
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.state.LocalMenuStateManager
import com.jawzf.pixeltoolkit.domain.DIYTabs
import com.jawzf.pixeltoolkit.feature.apps.freeze.FreezeManager
import com.jawzf.pixeltoolkit.ui.composables.AppGridItem
import com.jawzf.pixeltoolkit.ui.core.sheets.AssignTagsSheet
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FrozenAppsSearchResultCard(
    apps: List<NotificationApp>,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    onNavigateToFreezeTab: (() -> Unit)? = null,
) {
    if (apps.isEmpty()) return

    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    val menuState = LocalMenuStateManager.current

    val freezeTags by viewModel.freeze.freezeTags
    val freezeAppTagMap by viewModel.freeze.freezeAppTagMap
    val isTagColorCodedEnabled by viewModel.freeze.isFreezeTagColorCodedEnabled

    val frozenStates = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(apps) {
        withContext(Dispatchers.IO) {
            val states =
                apps.associate { app ->
                    app.packageName to FreezeManager.isAppFrozen(context, app.packageName)
                }
            withContext(Dispatchers.Main) {
                frozenStates.putAll(states)
            }
        }
    }

    var appForTagAssignment by remember { mutableStateOf<NotificationApp?>(null) }

    val navigateToFreezeTab = {
        HapticUtil.performUIHaptic(view)
        if (onNavigateToFreezeTab != null) {
            onNavigateToFreezeTab()
        } else {
            val intent = com.jawzf.pixeltoolkit.core.platform.AppComponents.mainIntent(context).apply {
                putExtra("target_tab", DIYTabs.FREEZE.name)
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_frozen_apps),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            AssistChip(
                onClick = { navigateToFreezeTab() },
                label = {
                    Text(
                        text = stringResource(R.string.action_view_all),
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.rounded_chevron_right_24),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                },
                colors =
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        trailingIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                border = AssistChipDefaults.assistChipBorder(true),
            )
        }

        RoundedCardContainer(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceBright,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .animateContentSize(animationSpec = tween(durationMillis = 300)),
            ) {
                val displayedApps = apps.take(4)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    displayedApps.forEach { app ->
                        val appTagIds = freezeAppTagMap[app.packageName] ?: emptyList()
                        val neverAutoFreezeTagIds =
                            freezeTags
                                .filter { it.neverAutoFreeze }
                                .map { it.id }
                                .toSet()
                        val isLockedByTag = appTagIds.any { neverAutoFreezeTagIds.contains(it) }

                        val tagColor =
                            if (isTagColorCodedEnabled) {
                                appTagIds.firstOrNull()?.let { firstTagId ->
                                    freezeTags.find { it.id == firstTagId }?.colorHex?.let { colorHex ->
                                        try {
                                            Color(android.graphics.Color.parseColor(colorHex))
                                        } catch (_: Exception) {
                                            null
                                        }
                                    }
                                }
                            } else {
                                null
                            }

                        Box(modifier = Modifier.weight(1f)) {
                            AppGridItem(
                                app = app,
                                isFrozen = frozenStates[app.packageName] ?: false,
                                isAutoFreezeEnabled = app.isEnabled,
                                isLockedByTag = isLockedByTag,
                                tagColor = tagColor,
                                isHighlighted = false,
                                menuState = menuState,
                                onClick = {
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    viewModel.freeze.launchAndUnfreezeApp(context, app.packageName)
                                },
                                onToggleFreeze = {
                                    scope.launch(Dispatchers.IO) {
                                        val isCurrentlyFrozen = FreezeManager.isAppFrozen(context, app.packageName)
                                        if (isCurrentlyFrozen) {
                                            FreezeManager.unfreezeApp(context, app.packageName)
                                        } else {
                                            FreezeManager.freezeApp(context, app.packageName)
                                        }
                                        val updatedState = FreezeManager.isAppFrozen(context, app.packageName)
                                        withContext(Dispatchers.Main) {
                                            frozenStates[app.packageName] = updatedState
                                        }
                                        viewModel.freeze.refreshFreezePickedApps(context, silent = true)
                                    }
                                },
                                onToggleAutoFreeze = { isAutoFreeze ->
                                    viewModel.freeze.updateFreezeAppAutoFreeze(
                                        context,
                                        app.packageName,
                                        isAutoFreeze,
                                    )
                                },
                                onAssignTags = {
                                    appForTagAssignment = app
                                },
                                onRemove = {
                                    viewModel.freeze.updateFreezeAppEnabled(
                                        context,
                                        app.packageName,
                                        false,
                                    )
                                },
                            )
                        }
                    }

                    val emptySlots = 4 - displayedApps.size
                    repeat(emptySlots) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }

    appForTagAssignment?.let { targetApp ->
        AssignTagsSheet(
            appName = targetApp.appName,
            availableTags = freezeTags,
            assignedTagIds = freezeAppTagMap[targetApp.packageName] ?: emptyList(),
            onDismissRequest = { appForTagAssignment = null },
            onSave = { selectedTagIds ->
                viewModel.freeze.setAppTags(context, targetApp.packageName, selectedTagIds)
            },
        )
    }
}
