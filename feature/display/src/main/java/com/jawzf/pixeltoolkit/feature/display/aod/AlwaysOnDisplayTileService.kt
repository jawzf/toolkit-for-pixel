package com.jawzf.pixeltoolkit.feature.display.aod

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings

@RequiresApi(Build.VERSION_CODES.N)
class AlwaysOnDisplayTileService : BaseTileService() {
    override fun getTileLabel(): String = "Always on Display"

    override fun getTileSubtitle(): String =
        when {
            isGlanceEnabled() -> "Dynamic"
            isAodEnabled() -> "On"
            else -> "Off"
        }

    override fun hasFeaturePermission(): Boolean =
        PermissionUtils
            .canWriteSecureSettings(this) ||
            (ShellUtils.isAvailable(this) && ShellUtils.hasPermission(this))

    override fun getTileIcon(): Icon? =
        when {
            isGlanceEnabled() -> Icon.createWithResource(this, R.drawable.outline_mobile_chat_24)
            isAodEnabled() -> Icon.createWithResource(this, R.drawable.rounded_mobile_text_2_24)
            else -> Icon.createWithResource(this, R.drawable.rounded_mobile_off_24)
        }

    override fun getTileState(): Int = if (isAodEnabled() || isGlanceEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        when {
            isGlanceEnabled() -> {
                // Dynamic -> On
                setGlanceEnabled(false)
                setAodEnabled(true)
            }

            isAodEnabled() -> {
                // On -> Off
                setAodEnabled(false)
                setGlanceEnabled(false)
            }

            else -> {
                // Off -> Dynamic
                setGlanceEnabled(true)
                setAodEnabled(false)
            }
        }
    }

    private fun isAodEnabled(): Boolean = getSecureInt("doze_always_on", 0) == 1

    private fun setAodEnabled(enabled: Boolean) {
        putSecureInt("doze_always_on", if (enabled) 1 else 0)
    }

    private fun isGlanceEnabled(): Boolean =
        getSharedPreferences(PlatformPrefs.PREFS_NAME, MODE_PRIVATE)
            .getBoolean(DisplaySettings.KEY_NOTIFICATION_GLANCE_ENABLED, false)

    private fun setGlanceEnabled(enabled: Boolean) {
        getSharedPreferences(PlatformPrefs.PREFS_NAME, MODE_PRIVATE).edit().apply {
            putBoolean(DisplaySettings.KEY_NOTIFICATION_GLANCE_ENABLED, enabled)
            apply()
        }
    }
}
