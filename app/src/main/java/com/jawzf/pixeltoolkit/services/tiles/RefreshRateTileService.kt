package com.jawzf.pixeltoolkit.services.tiles

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.FeatureSettingsActivity
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.utils.RefreshRateUtils

@RequiresApi(Build.VERSION_CODES.N)
class RefreshRateTileService : BaseTileService() {
    override fun onClick() {
        if (!hasFeaturePermission()) {
            val intent =
                Intent(this, FeatureSettingsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("feature", "Quick settings tiles")
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val pendingIntent =
                    android.app.PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
                    )
                startActivityAndCollapse(pendingIntent)
            } else {
                @Suppress("DEPRECATION")
                startActivityAndCollapse(intent)
            }
            return
        }
        super.onClick()
    }

    override fun onTileClick() {
        val nextPreset = RefreshRateUtils.getNextPreset(this)
        if (nextPreset <= 0) {
            val settingsRepository = SettingsRepository(this)
            RefreshRateUtils.resetRefreshRate(
                this,
                settingsRepository.shouldRestoreInfinityPeakOnRefreshRateReset(),
            )
        } else {
            RefreshRateUtils.applyFixedRefreshRate(this, nextPreset.toFloat())
        }
    }

    override fun getTileLabel(): String = getString(R.string.tile_refresh_rate)

    override fun getTileSubtitle(): String = RefreshRateUtils.getDisplaySubtitle(this)

    override fun hasFeaturePermission(): Boolean = ShellUtils.hasPermission(this)

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_shutter_speed_24)

    override fun getTileState(): Int =
        if (RefreshRateUtils.hasCustomRefreshRate(this)) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
}
