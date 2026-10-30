package com.jawzf.pixeltoolkit.services.tiles

import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import com.jawzf.pixeltoolkit.core.design.R

class MonoAudioTileService : BaseTileService() {
    override fun getTileLabel(): String = "Mono Audio"

    override fun getTileSubtitle(): String = if (isMonoAudioEnabled()) "On" else "Off"

    override fun hasFeaturePermission(): Boolean =
        com.jawzf.pixeltoolkit.utils.PermissionUtils
            .canWriteSecureSettings(this) ||
            (
                com.jawzf.pixeltoolkit.utils.ShellUtils
                    .isAvailable(this) &&
                    com.jawzf.pixeltoolkit.utils.ShellUtils
                        .hasPermission(this)
            )

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_headphones_24)

    override fun getTileState(): Int = if (isMonoAudioEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        val newState = if (isMonoAudioEnabled()) 0 else 1
        com.jawzf.pixeltoolkit.utils.ShellUtils.runCommand(
            this,
            "settings put system master_mono $newState",
            featureName = getTileLabel(),
        )
    }

    private fun isMonoAudioEnabled(): Boolean = Settings.System.getInt(contentResolver, "master_mono", 0) == 1
}
