package com.jawzf.pixeltoolkit.feature.automation.ui.editor

import android.app.Activity
import androidx.compose.runtime.Composable
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem

/** What the action editors need from the app: permission rows and the settings sheets owned by other features. */
interface ActionEditorHost {
    /** Permission rows for [keys] (AppPermission keys), refreshed when the screen resumes. */
    @Composable
    fun permissionItems(
        keys: List<String>,
        activity: Activity?,
    ): List<PermissionItem>

    /** True when the app shows [action]'s settings sheet itself. */
    fun ownsSettings(action: Action): Boolean

    /** Shows the app-owned settings sheet for [action]. [onSave] is not called for option-only sheets. */
    @Composable
    fun ActionSettingsSheet(
        action: Action,
        onDismiss: () -> Unit,
        onSave: (Action) -> Unit,
    )
}

/** Holds the app's [ActionEditorHost]. Set once in ToolkitApp.onCreate. */
object ActionEditors {
    lateinit var host: ActionEditorHost
        private set

    fun init(editorHost: ActionEditorHost) {
        host = editorHost
    }
}
