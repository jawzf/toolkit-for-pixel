package com.jawzf.pixeltoolkit.core.platform.tiles

import android.content.Context
import androidx.annotation.DrawableRes

/** What other modules may know about one of the app's QS tiles. */
data class QsTileSummary(
    val className: String,
    @DrawableRes val iconRes: Int,
)

/** The app's QS tiles, for modules that can't see `QsTileRegistry`. */
interface QsTileSource {
    /** The tiles the user pinned to the widget, in order. */
    fun pinned(context: Context): List<QsTileSummary>

    /** Refreshes state some tiles read before the widget draws them. */
    suspend fun prepare(context: Context)

    fun isActive(
        context: Context,
        className: String,
    ): Boolean

    fun label(
        context: Context,
        className: String,
    ): String

    fun subtitle(
        context: Context,
        className: String,
    ): String

    @DrawableRes fun icon(
        context: Context,
        className: String,
        @DrawableRes defaultIconRes: Int,
    ): Int

    /** Runs the tile's click, as tapping it in Quick Settings would. */
    fun trigger(
        context: Context,
        className: String,
    )
}

/** Holds the app's [QsTileSource]. Set once in ToolkitApp.onCreate. */
object QsTileCatalog : QsTileSource {
    private lateinit var source: QsTileSource

    fun init(tileSource: QsTileSource) {
        source = tileSource
    }

    override fun pinned(context: Context): List<QsTileSummary> = source.pinned(context)

    override suspend fun prepare(context: Context) = source.prepare(context)

    override fun isActive(
        context: Context,
        className: String,
    ): Boolean = source.isActive(context, className)

    override fun label(
        context: Context,
        className: String,
    ): String = source.label(context, className)

    override fun subtitle(
        context: Context,
        className: String,
    ): String = source.subtitle(context, className)

    @DrawableRes
    override fun icon(
        context: Context,
        className: String,
        @DrawableRes defaultIconRes: Int,
    ): Int = source.icon(context, className, defaultIconRes)

    override fun trigger(
        context: Context,
        className: String,
    ) = source.trigger(context, className)
}
