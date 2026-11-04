package com.jawzf.pixeltoolkit.services.tiles

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository

@RequiresApi(Build.VERSION_CODES.N)
class IslandTileService : BaseTileService() {
    private val settings by lazy { SettingsRepository(this) }

    override fun getTileLabel(): String = getString(R.string.island_title)

    override fun getTileSubtitle(): String = getString(if (settings.isIslandEnabled()) R.string.on else R.string.off)

    override fun hasFeaturePermission(): Boolean =
        PermissionUtils.isAccessibilityServiceEnabled(this) &&
            PermissionUtils.hasNotificationListenerPermission(this)

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_upcoming_24)

    override fun getTileState(): Int = if (settings.isIslandEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        settings.setIslandEnabled(!settings.isIslandEnabled())
    }
}
