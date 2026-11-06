package com.jawzf.pixeltoolkit.feature.display.duo

interface DuoIslandLink {
    fun onDuoShowsTime()

    fun onDuoShowsMedia()

    fun onDuoShowsProgress()

    fun onDuoShowsFlashlight()

    fun onCombinedChanged(enabled: Boolean)
}
