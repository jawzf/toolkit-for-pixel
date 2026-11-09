package com.jawzf.pixeltoolkit.services.tiles

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.DrawableRes
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.QsTileSource
import com.jawzf.pixeltoolkit.core.platform.tiles.QsTileSummary
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.connectivity.sim.DataSimController
import com.jawzf.pixeltoolkit.feature.connectivity.sim.DataSimTileService
import com.jawzf.pixeltoolkit.services.receivers.QsTileActionRouter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Default [QsTileSource] implementation using [QsTileRegistry]. */
object AppQsTileSource : QsTileSource {
    override fun pinned(context: Context): List<QsTileSummary> =
        SettingsRepository(context)
            .getPinnedQsTiles()
            .mapNotNull { QsTileRegistry.getTileByClassName(it) }
            .map { QsTileSummary(it.serviceClass.name, it.iconRes) }

    override suspend fun prepare(context: Context) {
        if (
            DataSimTileService::class.java.name in SettingsRepository(context).getPinnedQsTiles() &&
            PermissionUtils.hasReadPhoneStatePermission(context)
        ) {
            withContext(Dispatchers.IO) {
                try {
                    DataSimController.read(context)
                } catch (e: Exception) {
                    Log.w("QsTilesWidget", "Cannot refresh data SIM state", e)
                }
            }
        }
    }

    override fun isActive(
        context: Context,
        className: String,
    ): Boolean = QsTileRegistry.isTileActive(context, className)

    override fun label(
        context: Context,
        className: String,
    ): String = QsTileRegistry.getTileLabel(context, className)

    override fun subtitle(
        context: Context,
        className: String,
    ): String = QsTileRegistry.getTileSubtitle(context, className)

    @DrawableRes
    override fun icon(
        context: Context,
        className: String,
        @DrawableRes defaultIconRes: Int,
    ): Int = QsTileRegistry.getTileIcon(context, className, defaultIconRes)

    override fun trigger(
        context: Context,
        className: String,
    ) {
        val intent =
            Intent(context, QsTileActionRouter::class.java).apply {
                action = QsTileActionRouter.ACTION_TRIGGER_TILE
                putExtra(QsTileActionRouter.EXTRA_SERVICE_CLASS_NAME, className)
            }
        context.sendBroadcast(intent)
    }
}
