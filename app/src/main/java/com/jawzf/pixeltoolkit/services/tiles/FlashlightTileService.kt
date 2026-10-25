package com.jawzf.pixeltoolkit.services.tiles

import android.content.Intent
import android.graphics.drawable.Icon
import android.hardware.camera2.CameraManager
import android.os.Build
import android.service.quicksettings.Tile
import androidx.annotation.RequiresApi
import com.jawzf.pixeltoolkit.R
import com.jawzf.pixeltoolkit.services.receivers.FlashlightActionReceiver

@RequiresApi(Build.VERSION_CODES.N)
class FlashlightTileService : BaseTileService() {
    private var isTorchOn = false
    private val cameraManager by lazy { getSystemService(CAMERA_SERVICE) as CameraManager }

    private val isSpecialModeActive: Boolean
        get() = ScreenOffAccessibilityService.instance?.flashlightHandler?.isSpecialModeActive == true

    private val isTileActive: Boolean
        get() = isTorchOn || isSpecialModeActive

    private val torchCallback =
        object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(
                cameraId: String,
                enabled: Boolean,
            ) {
                super.onTorchModeChanged(cameraId, enabled)
                isTorchOn = enabled
                updateTile()

                val intent =
                    Intent("com.jawzf.pixeltoolkit.action.QS_TILES_WIDGET_UPDATE").apply {
                        setPackage(packageName)
                    }
                sendBroadcast(intent)
            }
        }

    override fun onCreate() {
        super.onCreate()
        try {
            cameraManager.registerTorchCallback(torchCallback, null)
        } catch (e: Exception) {
            // ignore
        }
    }

    override fun onDestroy() {
        try {
            cameraManager.unregisterTorchCallback(torchCallback)
        } catch (e: Exception) {
            // ignore
        }
        super.onDestroy()
    }

    override fun onTileClick() {
        if (isSpecialModeActive) {
            val intent =
                Intent(this, FlashlightActionReceiver::class.java).apply {
                    action = FlashlightActionReceiver.ACTION_OFF
                }
            sendBroadcast(intent)
        } else {
            val intent =
                Intent(this, FlashlightActionReceiver::class.java).apply {
                    action = FlashlightActionReceiver.ACTION_TOGGLE
                }
            sendBroadcast(intent)
        }
    }

    override fun getTileLabel(): String = "Flashlight"

    override fun getTileSubtitle(): String = if (isTileActive) "On" else "Off"

    override fun hasFeaturePermission(): Boolean = true

    override fun getTileIcon(): Icon {
        val resId =
            if (isTileActive) R.drawable.round_flashlight_on_24 else R.drawable.rounded_flashlight_on_24
        return Icon.createWithResource(this, resId)
    }

    override fun getTileState(): Int = if (isTileActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
}
