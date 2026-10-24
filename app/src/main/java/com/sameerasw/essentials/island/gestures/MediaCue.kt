package com.sameerasw.essentials.island.gestures

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object IslandMediaCue {
    const val LIKED_MS = 1100L

    val liked = MutableStateFlow(false)

    fun flashLiked(scope: CoroutineScope) {
        scope.launch {
            liked.value = true
            delay(LIKED_MS)
            liked.value = false
        }
    }
}
