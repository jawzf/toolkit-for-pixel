package com.jawzf.pixeltoolkit.ui.features.system

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.automation.ui.editor.ActionEditorHost
import com.jawzf.pixeltoolkit.ui.core.sheets.FreezeTagSettingsSheet
import com.jawzf.pixeltoolkit.ui.core.sheets.HilightEffectSettingsSheet
import com.jawzf.pixeltoolkit.ui.core.sheets.NotificationLightingActionSheet
import com.jawzf.pixeltoolkit.ui.core.sheets.OpenActivityPicker
import com.jawzf.pixeltoolkit.ui.core.sheets.ScreenOffSettingsSheet
import com.jawzf.pixeltoolkit.utils.PermissionUIHelper
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

object AppActionEditorHost : ActionEditorHost {
    @Composable
    override fun permissionItems(
        keys: List<String>,
        activity: Activity?,
    ): List<PermissionItem> {
        val context = LocalContext.current
        val viewModel = checkedMainViewModel()
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer =
                LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        viewModel.check(context)
                    }
                }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
        return PermissionUIHelper.getPermissionItems(keys, context, viewModel, activity)
    }

    override fun ownsSettings(action: Action): Boolean =
        when (action) {
            is Action.ToggleFlashlight,
            is Action.LikeCurrentSong,
            is Action.ScreenOff,
            is Action.TriggerNotificationLighting,
            is Action.Hilight,
            is Action.FreezeTag,
            is Action.OpenActivity,
            -> true
            else -> false
        }

    @Composable
    override fun ActionSettingsSheet(
        action: Action,
        onDismiss: () -> Unit,
        onSave: (Action) -> Unit,
    ) {
        when (action) {
            is Action.ToggleFlashlight -> FlashlightOptionsSheet(onDismiss)

            is Action.LikeCurrentSong -> {
                LikeSongSettingsSheet(
                    onDismiss = onDismiss,
                    viewModel = checkedMainViewModel(),
                    context = LocalContext.current,
                )
            }

            is Action.ScreenOff -> {
                ScreenOffSettingsSheet(
                    initialAction = action,
                    onDismiss = onDismiss,
                    onSave = onSave,
                )
            }

            is Action.TriggerNotificationLighting -> {
                NotificationLightingActionSheet(
                    initialAction = action,
                    onDismiss = onDismiss,
                    onSave = onSave,
                )
            }

            is Action.Hilight -> {
                HilightEffectSettingsSheet(
                    initialAction = action,
                    onDismiss = onDismiss,
                    onSave = onSave,
                )
            }

            is Action.FreezeTag -> {
                val context = LocalContext.current
                val tags = remember { AppsSettings(context).getFreezeTags() }
                FreezeTagSettingsSheet(
                    initialAction = action,
                    availableTags = tags,
                    onDismiss = onDismiss,
                    onSave = onSave,
                )
            }

            is Action.OpenActivity -> {
                OpenActivityPicker(
                    onDismiss = onDismiss,
                    onActivitySelected = onSave,
                )
            }

            else -> {}
        }
    }
}

/** Flashlight fade and always-off options for the flashlight action. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FlashlightOptionsSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val viewModel = checkedMainViewModel()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.flashlight_options_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer {
                IconToggleItem(
                    iconRes = R.drawable.rounded_blur_on_24,
                    title = stringResource(R.string.flashlight_fade_title),
                    description = stringResource(R.string.flashlight_fade_desc),
                    isChecked = viewModel.isFlashlightFadeEnabled.value,
                    onCheckedChange = { viewModel.setFlashlightFadeEnabled(it, context) },
                )

                IconToggleItem(
                    iconRes = R.drawable.rounded_flashlight_on_24,
                    title = stringResource(R.string.flashlight_always_off_title),
                    description = stringResource(R.string.flashlight_always_off_desc),
                    isChecked = viewModel.isFlashlightAlwaysTurnOffEnabled.value,
                    onCheckedChange = {
                        viewModel.setFlashlightAlwaysTurnOffEnabled(it, context)
                    },
                )
            }

            Button(
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    onDismiss()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                shape = MaterialTheme.shapes.extraLarge,
            ) {
                Text(stringResource(R.string.action_done))
            }
        }
    }
}

/** The activity's [MainViewModel], checked once so its settings are loaded; the editor activities no longer check it. */
@Composable
private fun checkedMainViewModel(): MainViewModel {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()
    return remember(viewModel) { viewModel.also { it.check(context) } }
}
