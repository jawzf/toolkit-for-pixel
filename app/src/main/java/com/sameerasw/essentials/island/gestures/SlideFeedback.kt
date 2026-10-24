package com.sameerasw.essentials.island.gestures

import kotlinx.coroutines.flow.MutableStateFlow

enum class RingMode { Normal, Vibrate, Silent }

sealed interface SlideFeedback {
    data class Level(val brightness: Boolean, val percent: Int) : SlideFeedback
    data class Sound(val mode: RingMode) : SlideFeedback
    data class Track(val next: Boolean, val armed: Boolean) : SlideFeedback
}

object IslandSlideFeedback {
    val state = MutableStateFlow<SlideFeedback?>(null)

    fun publish(feedback: SlideFeedback?) {
        state.value = feedback
    }
}
