package com.sameerasw.essentials.utils.media

import android.content.ComponentName
import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.sameerasw.essentials.services.NotificationListener

object MusicSessionUtil {
    fun playingMusicSession(context: Context, excluded: Set<String> = emptySet()): MediaController? = try {
        val manager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager
        val component = ComponentName(context, NotificationListener::class.java)
        manager?.getActiveSessions(component)?.firstOrNull { controller ->
            val state = controller.playbackState
            state?.state == PlaybackState.STATE_PLAYING &&
                controller.packageName !in excluded &&
                (state.actions and (PlaybackState.ACTION_SKIP_TO_NEXT or PlaybackState.ACTION_SKIP_TO_PREVIOUS)) != 0L
        }
    } catch (_: Exception) {
        null
    }

    fun isPlayingMusic(context: Context, excluded: Set<String> = emptySet()): Boolean =
        playingMusicSession(context, excluded) != null
}
