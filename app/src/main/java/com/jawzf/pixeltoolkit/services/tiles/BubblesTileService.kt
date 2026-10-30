package com.jawzf.pixeltoolkit.services.tiles

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.design.R

@RequiresApi(Build.VERSION_CODES.N)
class BubblesTileService : BaseTileService() {
    override fun getTileLabel(): String = "Bubbles"

    override fun getTileSubtitle(): String = if (areBubblesEnabled()) "On" else "Off"

    override fun hasFeaturePermission(): Boolean =
        checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_bubble_24)

    override fun getTileState(): Int = if (areBubblesEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        val newState = if (areBubblesEnabled()) 0 else 1
        Settings.Global.putInt(contentResolver, "notification_bubbles", newState)
    }

    private fun areBubblesEnabled(): Boolean = Settings.Global.getInt(contentResolver, "notification_bubbles", 1) == 1
}
