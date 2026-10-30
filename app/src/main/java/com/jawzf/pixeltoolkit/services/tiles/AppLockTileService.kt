package com.jawzf.pixeltoolkit.services.tiles

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.ui.activities.TileAuthActivity
import com.jawzf.pixeltoolkit.utils.PermissionUtils

class AppLockTileService : BaseTileService() {
    override fun onTileClick() {
        val prefs = getSharedPreferences("toolkit_prefs", MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("app_lock_enabled", false)

        val intent =
            Intent(this, TileAuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("feature_pref_key", "app_lock_enabled")
                putExtra("auth_title", "App Lock Security")
                putExtra(
                    "auth_subtitle",
                    if (isEnabled) "Authenticate to disable app lock" else "Authenticate to enable app lock",
                )
            }

        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            startActivityAndCollapse(pendingIntent)
        } else {
            startActivityAndCollapse(intent)
        }
    }

    override fun getTileLabel(): String = "App Lock"

    override fun getTileSubtitle(): String = if (getTileState() == Tile.STATE_ACTIVE) "Enabled" else "Disabled"

    override fun hasFeaturePermission(): Boolean {
        // Accessibility is required for monitoring app usage
        return PermissionUtils.isAccessibilityServiceEnabled(this)
    }

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_shield_lock_24)

    override fun getTileState(): Int {
        val enabled =
            getSharedPreferences("toolkit_prefs", MODE_PRIVATE)
                .getBoolean("app_lock_enabled", false)
        return if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
}
