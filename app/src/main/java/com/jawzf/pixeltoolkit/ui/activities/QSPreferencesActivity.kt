package com.jawzf.pixeltoolkit.ui.activities

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import com.jawzf.pixeltoolkit.FeatureSettingsActivity
import com.jawzf.pixeltoolkit.feature.apps.applock.AppLockTileService
import com.jawzf.pixeltoolkit.feature.apps.freeze.AppFreezingTileService
import com.jawzf.pixeltoolkit.feature.connectivity.dns.PrivateDnsSettingsActivity
import com.jawzf.pixeltoolkit.feature.connectivity.dns.PrivateDnsTileService
import com.jawzf.pixeltoolkit.feature.connectivity.nfc.NfcTileService
import com.jawzf.pixeltoolkit.feature.connectivity.sim.DataSimTileService
import com.jawzf.pixeltoolkit.feature.display.aod.AlwaysOnDisplayTileService
import com.jawzf.pixeltoolkit.feature.display.ondisplay.ToolkitOnDisplayTileService
import com.jawzf.pixeltoolkit.feature.display.pixels.SmartPixelsTileService
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRateTileService
import com.jawzf.pixeltoolkit.feature.input.links.UrlShortenerTileService
import com.jawzf.pixeltoolkit.feature.notifications.flashlight.FlashlightPulseTileService
import com.jawzf.pixeltoolkit.feature.notifications.lighting.NotificationLightingTileService
import com.jawzf.pixeltoolkit.feature.power.battery.BatteryDetailsActivity
import com.jawzf.pixeltoolkit.feature.power.battery.BatteryNotificationTileService
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateTileService
import com.jawzf.pixeltoolkit.feature.power.charging.ChargeQuickTileService
import com.jawzf.pixeltoolkit.feature.power.maps.MapsPowerSavingTileService
import com.jawzf.pixeltoolkit.feature.security.lockscreen.ScreenLockedSecurityTileService
import com.jawzf.pixeltoolkit.feature.sound.mode.SoundModeTileService
import com.jawzf.pixeltoolkit.utils.DeviceLockUtils

class QSPreferencesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val componentName =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_COMPONENT_NAME, ComponentName::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_COMPONENT_NAME)
            }

        Log.d("QSPreferences", "Received long-press for: ${componentName?.className}")

        if (componentName != null) {
            if (componentName.className == "com.jawzf.pixeltoolkit.services.tiles.LockdownTileService") {
                DeviceLockUtils.performLockdownTileAction(this, isLongPress = true)
                finish()
                return
            }
            // Special case for Sound Mode to open the system volume panel
            if (componentName.className == SoundModeTileService::class.java.name) {
                val volumeIntent =
                    Intent("android.settings.panel.action.VOLUME").apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                startActivity(volumeIntent)
                finish()
                return
            }

            if (componentName.className == "com.jawzf.pixeltoolkit.services.tiles.FlashlightTileService") {
                val intent =
                    Intent(this, FlashlightIntensityActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                startActivity(intent)
                finish()
                return
            }

            if (componentName.className == SmartPixelsTileService::class.java.name) {
                val intent =
                    Intent(this, SmartPixelsIntensityActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                startActivity(intent)
                finish()
                return
            }

            if (componentName.className == ToolkitOnDisplayTileService::class.java.name) {
                val intent =
                    Intent(this, com.jawzf.pixeltoolkit.FeatureSettingsActivity::class.java).apply {
                        putExtra("feature", "Toolkit On Display")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                startActivity(intent)
                finish()
                return
            }

            if (componentName.className == PrivateDnsTileService::class.java.name) {
                val intent =
                    Intent(this, PrivateDnsSettingsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                startActivity(intent)
                finish()
                return
            }

            if (componentName.className == DataSimTileService::class.java.name) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    openSystemSettings(Settings.ACTION_MANAGE_ALL_SIM_PROFILES_SETTINGS, Settings.ACTION_WIRELESS_SETTINGS)
                } else {
                    openSystemSettings(Settings.ACTION_WIRELESS_SETTINGS)
                }
                finish()
                return
            }

            if (componentName.className == com.jawzf.pixeltoolkit.services.tiles.HapticsTileService::class.java.name) {
                openHapticsSettings()
                finish()
                return
            }

            if (componentName.className == "com.jawzf.pixeltoolkit.services.tiles.AdaptiveBrightnessTileService") {
                val displayIntent =
                    Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                startActivity(displayIntent)
                finish()
                return
            }

            if (componentName.className == "com.jawzf.pixeltoolkit.services.tiles.UsbDebuggingTileService") {
                val intent =
                    Intent(this, DebuggingSettingsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                startActivity(intent)
                finish()
                return
            }

            if (componentName.className == "com.jawzf.pixeltoolkit.services.tiles.DefaultLauncherTileService") {
                startActivity(Intent(this, DefaultLauncherSettingsActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
                finish()
                return
            }

            if (componentName.className == "com.jawzf.pixeltoolkit.services.tiles.DeveloperOptionsTileService") {
                val devIntent =
                    Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                startActivity(devIntent)
                finish()
                return
            }

            if (componentName.className == ChargeQuickTileService::class.java.name ||
                componentName.className == BatteryNotificationTileService::class.java.name
            ) {
                val intent =
                    Intent(this, BatteryDetailsActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                startActivity(intent)
                finish()
                return
            }

            val feature =
                when (componentName.className) {
                    CaffeinateTileService::class.java.name -> "Caffeinate"
                    NotificationLightingTileService::class.java.name -> "Notification lighting"
                    "com.jawzf.pixeltoolkit.services.tiles.DynamicNightLightTileService" -> "Dynamic night light"
                    AppLockTileService::class.java.name -> "App lock"
                    ScreenLockedSecurityTileService::class.java.name -> "Screen locked security"
                    AppFreezingTileService::class.java.name -> "Freeze"
                    FlashlightPulseTileService::class.java.name -> "Notification lighting"
                    "com.jawzf.pixeltoolkit.services.tiles.StayAwakeTileService" -> "Quick settings tiles"
                    NfcTileService::class.java.name -> "NFC"
                    "com.jawzf.pixeltoolkit.services.tiles.AdaptiveBrightnessTileService" -> "Quick settings tiles"
                    RefreshRateTileService::class.java.name -> "Screen refresh rate"
                    MapsPowerSavingTileService::class.java.name -> "Maps power saving mode"
                    "com.jawzf.pixeltoolkit.services.tiles.UsbDebuggingTileService" -> "Quick settings tiles"
                    BatteryNotificationTileService::class.java.name -> "Batteries"
                    ChargeQuickTileService::class.java.name -> "Batteries"
                    AlwaysOnDisplayTileService::class.java.name -> "Always on Display"
                    UrlShortenerTileService::class.java.name -> "URL Shortener"
                    else -> null
                }

            Log.d("QSPreferences", "Mapping to feature: $feature")

            if (feature != null) {
                // Check if authentication is required
                if (feature == "App lock" || feature == "Screen locked security") {
                    val authIntent =
                        Intent(this, TileAuthActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("target_feature", feature)
                            putExtra("auth_title", "$feature Settings")
                            putExtra("auth_subtitle", "Confirm identity to open settings")
                        }
                    startActivity(authIntent)
                } else {
                    val settingsIntent =
                        Intent(this, FeatureSettingsActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("feature", feature)
                        }
                    startActivity(settingsIntent)
                }
            }
        }

        finish()
    }

    private fun openHapticsSettings() {
        val vibrationSettings =
            Intent(Intent.ACTION_MAIN).apply {
                setClassName("com.android.settings", "com.android.settings.Settings\$VibrationIntensitySettingsActivity")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        try {
            startActivity(vibrationSettings)
        } catch (_: ActivityNotFoundException) {
            openSystemSettings(Settings.ACTION_SOUND_SETTINGS)
        } catch (_: SecurityException) {
            openSystemSettings(Settings.ACTION_SOUND_SETTINGS)
        }
    }

    private fun openSystemSettings(vararg actions: String) {
        for (action in actions.toList() + Settings.ACTION_SETTINGS) {
            try {
                startActivity(
                    Intent(action).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    },
                )
                return
            } catch (_: ActivityNotFoundException) {
            }
        }
    }
}
