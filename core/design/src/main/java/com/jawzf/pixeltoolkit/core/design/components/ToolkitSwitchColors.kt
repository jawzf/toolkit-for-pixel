package com.jawzf.pixeltoolkit.core.design.components

import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun toolkitSwitchColors(): SwitchColors =
    SwitchDefaults.colors(
        uncheckedBorderColor = Color.Transparent,
        disabledUncheckedBorderColor = Color.Transparent,
    )
