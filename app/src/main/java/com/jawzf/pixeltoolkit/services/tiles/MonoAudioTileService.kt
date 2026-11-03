package com.jawzf.pixeltoolkit.services.tiles

import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils

class MonoAudioTileService : BaseTileService() {
    override fun getTileLabel(): String = "Mono Audio"

    override fun getTileSubtitle(): String = if (isMonoAudioEnabled()) "On" else "Off"

    override fun hasFeaturePermission(): Boolean =
        PermissionUtils
            .canWriteSecureSettings(this) ||
            (
                ShellUtils
                    .isAvailable(this) &&
                    ShellUtils
                        .hasPermission(this)
            )

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_headphones_24)

    override fun getTileState(): Int = if (isMonoAudioEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        val newState = if (isMonoAudioEnabled()) 0 else 1
        ShellUtils.runCommand(
            this,
            "settings put system master_mono $newState",
            featureName = getTileLabel(),
        )
    }

    private fun isMonoAudioEnabled(): Boolean = Settings.System.getInt(contentResolver, "master_mono", 0) == 1
}
