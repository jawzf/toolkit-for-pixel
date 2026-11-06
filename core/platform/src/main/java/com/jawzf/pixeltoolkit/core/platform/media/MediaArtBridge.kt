package com.jawzf.pixeltoolkit.core.platform.media

import android.graphics.Bitmap

// Album art cached by the notification listener in :app, for code in other modules. Set once in ToolkitApp.onCreate.
object MediaArtBridge {
    private var cachedArt: (Long) -> Bitmap? = { null }
    private var latestArt: () -> Bitmap? = { null }

    fun init(
        getCached: (Long) -> Bitmap?,
        getLatest: () -> Bitmap?,
    ) {
        cachedArt = getCached
        latestArt = getLatest
    }

    fun getCachedBitmap(hash: Long): Bitmap? = cachedArt(hash)

    fun getLatestArtBitmap(): Bitmap? = latestArt()
}
