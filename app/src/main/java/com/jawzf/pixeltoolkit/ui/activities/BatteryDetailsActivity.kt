package com.jawzf.pixeltoolkit.ui.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jawzf.pixeltoolkit.ui.core.sheets.BatteryDetailsBottomSheet
import com.jawzf.pixeltoolkit.ui.theme.EssentialsTheme
import com.jawzf.pixeltoolkit.utils.battery.BatteryInfoUtil

class BatteryDetailsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: com.jawzf.pixeltoolkit.viewmodels.MainViewModel =
                androidx.lifecycle.viewmodel.compose
                    .viewModel()
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                viewModel.check(context)
            }
            val isPitchBlackThemeEnabled by viewModel.isPitchBlackThemeEnabled
            EssentialsTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                val basicDetails = remember { BatteryInfoUtil.getBasicDetails(context) }
                BatteryDetailsBottomSheet(
                    initialDetails = basicDetails,
                    onDismiss = { finish() },
                )
            }
        }
    }
}
