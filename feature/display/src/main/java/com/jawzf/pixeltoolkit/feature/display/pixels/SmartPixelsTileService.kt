package com.jawzf.pixeltoolkit.feature.display.pixels

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings

@RequiresApi(Build.VERSION_CODES.N)
class SmartPixelsTileService : BaseTileService() {
    private val prefs by lazy {
        getSharedPreferences(PlatformPrefs.PREFS_NAME, MODE_PRIVATE)
    }

    override fun onTileClick() {
        val currentState = prefs.getBoolean(DisplaySettings.KEY_SMART_PIXELS_ENABLED, false)
        val newState = !currentState
        prefs.edit().putBoolean(DisplaySettings.KEY_SMART_PIXELS_ENABLED, newState).apply()

        updateTile()
    }

    override fun getTileLabel(): String = getString(R.string.feat_smart_pixels_title)

    override fun getTileSubtitle(): String =
        if (prefs.getBoolean(DisplaySettings.KEY_SMART_PIXELS_ENABLED, false)) getString(R.string.on) else getString(R.string.off)

    override fun hasFeaturePermission(): Boolean = PermissionUtils.isAccessibilityServiceEnabled(this)

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_grain_24)

    override fun getTileState(): Int =
        if (prefs.getBoolean(DisplaySettings.KEY_SMART_PIXELS_ENABLED, false)) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
}
