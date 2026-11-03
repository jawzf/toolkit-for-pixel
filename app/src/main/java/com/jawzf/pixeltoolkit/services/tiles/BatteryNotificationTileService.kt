package com.jawzf.pixeltoolkit.services.tiles

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.services.BatteryNotificationService

@RequiresApi(Build.VERSION_CODES.N)
class BatteryNotificationTileService : BaseTileService() {
    private val settingsRepository by lazy { SettingsRepository(this) }

    override fun onTileClick() {
        val newState = !settingsRepository.isBatteryNotificationEnabled()
        settingsRepository.setBatteryNotificationEnabled(newState)

        val intent = Intent(this, BatteryNotificationService::class.java)
        if (newState) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } else {
            stopService(intent)
        }
    }

    override fun getTileLabel(): String = "Battery Info"

    override fun getTileSubtitle(): String = if (settingsRepository.isBatteryNotificationEnabled()) "On" else "Off"

    override fun hasFeaturePermission(): Boolean = true

    override fun getTileState(): Int = if (settingsRepository.isBatteryNotificationEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_battery_android_frame_6_24)
}
