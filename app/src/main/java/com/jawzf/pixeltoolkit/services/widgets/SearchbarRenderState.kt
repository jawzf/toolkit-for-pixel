package com.jawzf.pixeltoolkit.services.widgets

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

object SearchbarRenderState {
    var revision by mutableIntStateOf(0)
        private set

    fun bump() {
        revision++
    }
}
