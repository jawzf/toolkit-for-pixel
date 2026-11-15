package com.jawzf.pixeltoolkit.ui.activities

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.state.LocalMenuStateManager
import com.jawzf.pixeltoolkit.core.design.state.MenuStateManager
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.feature.automation.ui.DIYScreen
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

class AutomationsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            val viewModel: MainViewModel = viewModel()
            val isPitchBlackThemeEnabled by viewModel.isPitchBlackThemeEnabled

            ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                CompositionLocalProvider(
                    LocalMenuStateManager provides remember { MenuStateManager() },
                ) {
                    var showNewAutomationSheet by remember { mutableStateOf(false) }
                    var isMacrosShown by remember { mutableStateOf(false) }

                    Scaffold(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        topBar = {
                            AutomationsTopBar(
                                isMacrosShown = isMacrosShown,
                                onBack = { finish() },
                                onAdd = { showNewAutomationSheet = true },
                            )
                        },
                    ) { innerPadding ->
                        DIYScreen(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding =
                                PaddingValues(
                                    top = innerPadding.calculateTopPadding(),
                                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ),
                            showNewAutomationSheet = showNewAutomationSheet,
                            onDismissNewAutomationSheet = { showNewAutomationSheet = false },
                            onNewAutomationClick = { showNewAutomationSheet = true },
                            onShowMacrosChange = { isMacrosShown = it },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutomationsTopBar(
    isMacrosShown: Boolean,
    onBack: () -> Unit,
    onAdd: () -> Unit,
) {
    val view = LocalView.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onBack()
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_arrow_back_24),
                contentDescription = stringResource(R.string.content_desc_back),
                tint = ToolkitTheme.colors.text,
            )
        }
        Text(
            text = stringResource(R.string.diy_tab_automations),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = ToolkitTheme.colors.text,
            modifier =
                Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
        )
        IconButton(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onAdd()
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_add_24),
                contentDescription =
                    stringResource(
                        if (isMacrosShown) R.string.diy_new_macro else R.string.diy_editor_new_title,
                    ),
                tint = ToolkitTheme.colors.text,
            )
        }
    }
}
