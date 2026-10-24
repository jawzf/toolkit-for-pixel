/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Background Services & Receivers
 * File: DuoTileService.kt
 * Description: Quick Settings tile service component for the Duo overlay feature.
 */

package com.jawzf.pixeltoolkit.services.tiles

import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.R
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.utils.PermissionUtils

@RequiresApi(Build.VERSION_CODES.N)
class DuoTileService : BaseTileService() {
    private val settings by lazy { SettingsRepository(this) }

    override fun getTileLabel(): String = getString(R.string.duo_title)

    override fun getTileSubtitle(): String = getString(if (settings.isDuoEnabled()) R.string.on else R.string.off)

    override fun hasFeaturePermission(): Boolean = PermissionUtils.isAccessibilityServiceEnabled(this)

    override fun getTileIcon(): Icon = Icon.createWithResource(this, R.drawable.rounded_motion_play_24)

    override fun getTileState(): Int = if (settings.isDuoEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        settings.setDuoEnabled(!settings.isDuoEnabled())
    }
}
