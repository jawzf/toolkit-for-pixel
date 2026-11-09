package com.jawzf.pixeltoolkit.feature.display.animations

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import com.jawzf.pixeltoolkit.core.data.display.ScaleAnimationsProfile
import com.jawzf.pixeltoolkit.core.data.display.TextAnimationsSettings
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService

class ScaleAnimationsTileService : BaseTileService() {
    private val textAnimations: TextAnimationsSettings by lazy { TextAnimationsSettings(this) }

    override fun getTileLabel(): String = getString(R.string.tile_scale_animations)

    override fun getTileSubtitle(): String {
        val mode = textAnimations.getScaleAnimationsMode()
        return if (mode == "glove") getString(R.string.label_enabled) else getString(R.string.label_disabled)
    }

    override fun hasFeaturePermission(): Boolean = true

    override fun getTileIcon(): Icon {
        val mode = textAnimations.getScaleAnimationsMode()
        val iconRes =
            if (mode == "glove") R.drawable.round_front_hand_24 else R.drawable.rounded_front_hand_24
        return Icon.createWithResource(this, iconRes)
    }

    override fun getTileState(): Int {
        val mode = textAnimations.getScaleAnimationsMode()
        return if (mode == "glove") Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }

    override fun onTileClick() {
        val currentMode = textAnimations.getScaleAnimationsMode()
        val oldMode = currentMode
        val newMode = if (currentMode == "glove") "default" else "glove"

        textAnimations.saveScaleAnimationsProfile(oldMode, textAnimations.currentProfile())
        val newProfile = textAnimations.getScaleAnimationsProfile(newMode)
        textAnimations.setScaleAnimationsMode(newMode)
        textAnimations.applyProfile(newProfile)
    }
}
