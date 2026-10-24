package com.jawzf.pixeltoolkit.services.tiles

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import androidx.core.content.edit
import com.jawzf.pixeltoolkit.R
import com.jawzf.pixeltoolkit.utils.DeviceUtils
import com.jawzf.pixeltoolkit.utils.PermissionUtils

class PocketModeTileService : BaseTileService() {
    override fun isDeviceSupported(): Boolean = !DeviceUtils.isGoogleDevice()

    override fun getTileLabel(): String = getString(R.string.feat_pocket_mode_title)

    override fun getTileSubtitle(): String = if (getTileState() == Tile.STATE_ACTIVE) "Enabled" else "Disabled"

    override fun hasFeaturePermission(): Boolean = PermissionUtils.isAccessibilityServiceEnabled(this)

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.ic_pocket_mode)

    override fun getTileState(): Int {
        val enabled =
            getSharedPreferences("essentials_prefs", MODE_PRIVATE)
                .getBoolean("pocket_mode_enabled", false)
        return if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }

    override fun onTileClick() {
        val prefs = getSharedPreferences("essentials_prefs", MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("pocket_mode_enabled", false)
        prefs.edit { putBoolean("pocket_mode_enabled", !isEnabled) }
    }
}
