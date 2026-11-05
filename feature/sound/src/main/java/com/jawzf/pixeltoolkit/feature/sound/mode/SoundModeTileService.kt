package com.jawzf.pixeltoolkit.feature.sound.mode

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService

@RequiresApi(Build.VERSION_CODES.N)
class SoundModeTileService : BaseTileService() {
    private val broadcastReceiver: BroadcastReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context,
                intent: Intent,
            ) {
                if (intent.action == AudioManager.RINGER_MODE_CHANGED_ACTION) {
                    updateTile()
                }
            }
        }

    override fun onCreate() {
        super.onCreate()
        this.registerReceiver(
            broadcastReceiver,
            IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION),
        )
    }

    override fun onDestroy() {
        try {
            this.unregisterReceiver(broadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override val clickHaptic = false

    override fun onTileClick() {
        when (SoundModeHandler(this).cycleNextMode()) {
            AudioManager.RINGER_MODE_VIBRATE -> HapticUtil.performStrongDoubleHaptic(this)
            AudioManager.RINGER_MODE_SILENT -> HapticUtil.performHapticForService(this, HapticFeedbackType.SUBTLE)
            AudioManager.RINGER_MODE_NORMAL -> HapticUtil.performHapticForService(this, HapticFeedbackType.CLICK)
        }
    }

    override fun getTileLabel(): String {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> "Sound"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            else -> "Sound"
        }
    }

    override fun getTileSubtitle(): String = if (hasFeaturePermission()) "Mode" else getString(R.string.permission_missing)

    override fun hasFeaturePermission(): Boolean {
        val prefs = getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
        val useShizuku = prefs.getBoolean("sound_mode_use_shizuku", true)
        if (useShizuku && ShizukuUtils.isShizukuAvailable() && ShizukuUtils.hasPermission()) {
            return true
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        return notificationManager.isNotificationPolicyAccessGranted
    }

    override fun getTileIcon(): Icon {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        val resId =
            when (audioManager.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> R.drawable.rounded_volume_up_24
                AudioManager.RINGER_MODE_VIBRATE -> R.drawable.rounded_mobile_vibrate_24
                AudioManager.RINGER_MODE_SILENT -> R.drawable.rounded_volume_off_24
                else -> R.drawable.rounded_volume_up_24
            }
        return Icon.createWithResource(this, resId)
    }

    override fun getTileState(): Int {
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        return if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            Tile.STATE_INACTIVE
        } else {
            Tile.STATE_ACTIVE
        }
    }
}
