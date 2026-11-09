@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.jawzf.pixeltoolkit.feature.automation.ui.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.components.ToolkitFloatingToolbar
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.feature.automation.diy.ActionRegistry
import com.jawzf.pixeltoolkit.feature.automation.diy.DIYRepository
import com.jawzf.pixeltoolkit.feature.automation.diy.Macro
import com.jawzf.pixeltoolkit.feature.automation.ui.editor.ActionSequenceEditor
import java.util.UUID

class MacroEditorActivity : ComponentActivity() {
    companion object {
        fun createIntent(
            context: Context,
            macroId: String?,
        ): Intent =
            Intent(context, MacroEditorActivity::class.java).apply {
                putExtra(AppComponents.EXTRA_MACRO_ID, macroId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        DIYRepository.init(applicationContext)

        val macroId = intent.getStringExtra(AppComponents.EXTRA_MACRO_ID)
        val existing = macroId?.let { DIYRepository.getMacro(it) }
        val isEditMode = existing != null

        setContent {
            val context = LocalContext.current
            val view = LocalView.current
            val isPitchBlackThemeEnabled = remember { PrefsStore(context).getBoolean(PlatformPrefs.KEY_PITCH_BLACK_THEME_ENABLED) }
            ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                var name by rememberSaveable { mutableStateOf(existing?.name.orEmpty()) }
                var actions by remember { mutableStateOf(existing?.actionList.orEmpty()) }
                var showDiscardDialog by remember { mutableStateOf(false) }

                val handleBackClick = {
                    showDiscardDialog = true
                }

                BackHandler {
                    handleBackClick()
                }

                if (showDiscardDialog) {
                    AlertDialog(
                        onDismissRequest = { showDiscardDialog = false },
                        title = { Text(stringResource(R.string.diy_discard_warning_title)) },
                        text = { Text(stringResource(R.string.diy_discard_warning_desc)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    showDiscardDialog = false
                                    finish()
                                },
                            ) {
                                Text(
                                    text = stringResource(R.string.translation_discard),
                                    color = MaterialTheme.colorScheme.error,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    showDiscardDialog = false
                                },
                            ) {
                                Text(stringResource(R.string.action_cancel))
                            }
                        },
                    )
                }

                val isValid = name.isNotBlank() && actions.isNotEmpty()

                Scaffold(
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) { _ ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(
                                        start = 16.dp,
                                        end = 16.dp,
                                        top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                                    ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            RoundedCardContainer {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text(stringResource(R.string.diy_macro_name)) },
                                    singleLine = true,
                                )
                            }

                            ActionSequenceEditor(
                                actions = actions,
                                onActionsChange = { actions = it },
                                screenOnOnly = false,
                                listKey = "macro",
                                emptyText = stringResource(R.string.diy_no_actions),
                                categories = remember { ActionRegistry.getCategories() },
                                showMacros = false,
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(
                                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp,
                                    ),
                            )
                        }

                        ToolkitFloatingToolbar(
                            title =
                                stringResource(
                                    if (isEditMode) R.string.diy_edit_macro else R.string.diy_new_macro,
                                ),
                            onBackClick = handleBackClick,
                            fabAction =
                                if (isValid) {
                                    {
                                        val macro =
                                            Macro(
                                                id = existing?.id ?: UUID.randomUUID().toString(),
                                                name = name.trim(),
                                                actions = actions,
                                            )
                                        if (isEditMode) {
                                            DIYRepository.updateMacro(macro)
                                        } else {
                                            DIYRepository.addMacro(macro)
                                        }
                                        finish()
                                    }
                                } else {
                                    null
                                },
                            fabIconRes = R.drawable.rounded_check_24,
                            fabContentDescription = stringResource(R.string.action_save),
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .zIndex(1f),
                        )
                    }
                }
            }
        }
    }
}
