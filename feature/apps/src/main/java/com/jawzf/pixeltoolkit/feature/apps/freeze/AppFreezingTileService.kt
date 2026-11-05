package com.jawzf.pixeltoolkit.feature.apps.freeze

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import com.jawzf.pixeltoolkit.core.platform.AppComponents
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService

class AppFreezingTileService : BaseTileService() {
    override val isSensitiveTile: Boolean = true

    override fun onTileClick() {
        val intent =
            Intent().setClassName(this, AppComponents.APP_FREEZING_ACTIVITY).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }

    override fun getTileLabel(): String = "App Freezing"

    override fun getTileSubtitle(): String = "Launch grid"

    override fun hasFeaturePermission(): Boolean = true

    override fun getTileState(): Int = Tile.STATE_INACTIVE
}
