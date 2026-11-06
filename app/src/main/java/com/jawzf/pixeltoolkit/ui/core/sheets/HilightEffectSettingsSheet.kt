package com.jawzf.pixeltoolkit.ui.core.sheets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.feature.notifications.hilight.ui.HilightEffectSheet

@Composable
fun HilightEffectSettingsSheet(
    initialAction: Action.Hilight,
    onDismiss: () -> Unit,
    onSave: (Action.Hilight) -> Unit,
) {
    HilightEffectSheet(
        title = stringResource(R.string.diy_action_hilight),
        initialEffect = initialAction.toEffect(),
        onDismiss = onDismiss,
        onSave = { onSave(Action.Hilight(it.pattern, it.color, it.durationMs, it.brightness, it.repeats)) },
    )
}
