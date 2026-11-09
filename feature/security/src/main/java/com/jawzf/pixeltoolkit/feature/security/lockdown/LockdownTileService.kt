package com.jawzf.pixeltoolkit.feature.security.lockdown

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import com.jawzf.pixeltoolkit.core.data.security.DeviceLockUtils
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository

class LockdownTileService : BaseTileService() {
    override fun onClick() {
        if (!isDeviceSupported() && !areUnsupportedFeaturesEnabled()) return
        HapticUtil.performHapticForService(this)
        DeviceLockUtils.performLockdownTileAction(this, isLongPress = false)
    }

    override fun onTileClick() {
        DeviceLockUtils.performLockdownTileAction(this, isLongPress = false)
    }

    override fun getTileLabel(): String =
        if (lockdownModeStatus()) getString(R.string.tile_lockdown_mode) else getString(R.string.tile_lock)

    override fun getTileSubtitle(): String = getString(R.string.tile_device)

    override fun hasFeaturePermission(): Boolean = true

    override fun getTileIcon(): Icon {
        val iconRes =
            if (lockdownModeStatus()) R.drawable.rounded_shield_lock_24 else R.drawable.rounded_lock_24
        return Icon.createWithResource(this, iconRes)
    }

    override fun getTileState(): Int = Tile.STATE_INACTIVE

    private fun lockdownModeStatus(): Boolean = SettingsRepository(this).getBoolean(SettingsRepository.KEY_LOCKDOWN_MODE, false)
}
