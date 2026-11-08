package com.jawzf.pixeltoolkit.feature.power.battery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.feature.power.battery.ui.BatteryDetailsBottomSheet

class BatteryDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val isPitchBlackThemeEnabled =
                remember { PowerSettings(context).getBoolean(PlatformPrefs.KEY_PITCH_BLACK_THEME_ENABLED) }
            ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                val basicDetails = remember { BatteryInfoUtil.getBasicDetails(context) }
                BatteryDetailsBottomSheet(
                    initialDetails = basicDetails,
                    onDismiss = { finish() },
                )
            }
        }
    }
}
