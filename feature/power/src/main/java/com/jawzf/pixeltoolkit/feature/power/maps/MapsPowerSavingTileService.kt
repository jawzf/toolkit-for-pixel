package com.jawzf.pixeltoolkit.feature.power.maps

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.feature.power.PowerSettings

@RequiresApi(Build.VERSION_CODES.N)
class MapsPowerSavingTileService : BaseTileService() {
    private val settingsRepository by lazy { PowerSettings(this) }

    override fun onClick() {
        if (!hasFeaturePermission()) {
            val intent =
                Intent().setClassName(this, AppComponents.FEATURE_SETTINGS_ACTIVITY).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("feature", "Maps power saving mode")
                }
            startActivityAndCollapseCompat(intent)
            return
        }
        super.onClick()
    }

    override fun getTileLabel(): String = getString(R.string.tile_maps_power_saving)

    override fun getTileSubtitle(): String =
        if (isMapsPowerSavingEnabled()) getString(R.string.tile_active) else getString(R.string.tile_inactive)

    override fun hasFeaturePermission(): Boolean {
        val hasShell = ShellUtils.hasPermission(this)
        val hasNotif = PermissionUtils.hasNotificationListenerPermission(this)
        return hasShell && hasNotif
    }

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_navigation_24)

    override fun getTileState(): Int = if (isMapsPowerSavingEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        val newState = !isMapsPowerSavingEnabled()
        settingsRepository.putBoolean(PowerSettings.KEY_MAPS_POWER_SAVING_ENABLED, newState)
        // MapsState.isEnabled is also updated in PowerState when preference changes
    }

    private fun isMapsPowerSavingEnabled(): Boolean =
        settingsRepository.getBoolean(
            PowerSettings.KEY_MAPS_POWER_SAVING_ENABLED,
            false,
        )
}
