/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Background Services & Receivers
 * File: HeadsUpNotificationsTileService.kt
 * Description: Quick Settings tile that toggles system heads-up notifications.
 */

package com.sameerasw.essentials.services.tiles

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import com.sameerasw.essentials.R

class HeadsUpNotificationsTileService : BaseTileService() {
    override fun getTileLabel(): String = getString(R.string.tile_heads_up_notifications)

    override fun getTileSubtitle(): String =
        getString(if (isHeadsUpEnabled()) R.string.tile_heads_up_shown else R.string.tile_heads_up_hidden)

    override fun hasFeaturePermission(): Boolean =
        checkCallingOrSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_GRANTED

    override fun getTileIcon(): Icon {
        val iconRes =
            if (isHeadsUpEnabled()) R.drawable.rounded_ad_units_24 else R.drawable.rounded_stay_primary_portrait_24
        return Icon.createWithResource(this, iconRes)
    }

    override fun getTileState(): Int = if (isHeadsUpEnabled()) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

    override fun onTileClick() {
        putGlobalInt(KEY, if (isHeadsUpEnabled()) 0 else 1)
    }

    private fun isHeadsUpEnabled(): Boolean = getGlobalInt(KEY, 1) == 1

    companion object {
        private const val KEY = "heads_up_notifications_enabled"
    }
}
