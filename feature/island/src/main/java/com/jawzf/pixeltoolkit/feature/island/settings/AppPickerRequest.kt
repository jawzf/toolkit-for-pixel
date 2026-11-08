package com.jawzf.pixeltoolkit.feature.island.settings

import android.content.Context
import androidx.compose.runtime.Composable
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection

class AppPickerRequest(
    val title: String,
    val onDismiss: () -> Unit,
    val onLoadApps: suspend (Context) -> List<AppSelection>,
    val onSaveApps: suspend (Context, List<AppSelection>) -> Unit,
    val onAppToggle: (Context, String, Boolean) -> Unit,
    val headerContent: (@Composable () -> Unit)?,
)
