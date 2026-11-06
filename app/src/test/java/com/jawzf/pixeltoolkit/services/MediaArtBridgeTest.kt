package com.jawzf.pixeltoolkit.services

import com.jawzf.pixeltoolkit.core.platform.media.MediaArtBridge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MediaArtBridgeTest {
    @Test
    fun delegatesToInitializedCallbacks() {
        var queriedHash = -1L
        var latestCalled = false
        MediaArtBridge.init(
            getCached = { hash ->
                queriedHash = hash
                null
            },
            getLatest = {
                latestCalled = true
                null
            },
        )

        assertNull(MediaArtBridge.getCachedBitmap(42L))
        assertEquals(42L, queriedHash)

        assertNull(MediaArtBridge.getLatestArtBitmap())
        assertEquals(true, latestCalled)
    }
}
