package com.jawzf.pixeltoolkit.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.display.wallpaper.picker.WallpaperScreen
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

class WallpaperActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        val settingsRepository = SettingsRepository(this)

        setContent {
            val isPitchBlackThemeEnabled by settingsRepository.isPitchBlackThemeEnabled.collectAsState(
                initial = false,
            )

            ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
                    val context = LocalContext.current
                    val viewModel: MainViewModel = viewModel()
                    viewModel.check(context)

                    WallpaperScreen(
                        onBack = { finish() },
                        isBlurEnabled = viewModel.isBlurEnabled.value,
                        isMotionBlurEnabled = viewModel.isMotionBlurEnabled.value,
                    )
                }
            }
        }
    }
}
