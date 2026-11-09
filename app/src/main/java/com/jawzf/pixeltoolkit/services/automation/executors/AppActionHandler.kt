package com.jawzf.pixeltoolkit.services.automation.executors

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionHandler
import com.jawzf.pixeltoolkit.core.data.display.TextAnimationsSettings
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.glance.StatusGlanceSettings
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.services.NotificationListener
import com.jawzf.pixeltoolkit.services.tiles.ScreenOffAccessibilityService
import com.jawzf.pixeltoolkit.ui.activities.PixelSearchResultsActivity
import com.jawzf.pixeltoolkit.utils.AutomationNotificationPoster
import com.jawzf.pixeltoolkit.utils.DeviceCare
import com.jawzf.pixeltoolkit.utils.SplitScreenUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Runs App's DIY actions for the action engine. */
object AppActionHandler : ActionHandler {
    override suspend fun handle(
        context: Context,
        action: Action,
    ): Boolean {
        when (action) {
            is Action.SendNotification -> AutomationNotificationPoster.post(context, action)
            is Action.DeviceCare -> withContext(Dispatchers.IO) { DeviceCare.run(context, action) }
            is Action.TakeScreenshot -> takeScreenshot(context)
            is Action.OpenNowPlayingApp -> openNowPlayingApp(context)
            is Action.SplitScreen -> SplitScreenUtil.toggle(context)
            is Action.EssentialSearch -> openPixelSearch(context)
            is Action.OverlayControl -> applyOverlayControl(context, action)
            is Action.TurnOnIsland -> SettingsRepository(context).setIslandEnabled(true)
            is Action.TurnOffIsland -> SettingsRepository(context).setIslandEnabled(false)
            is Action.ToggleIsland -> SettingsRepository(context).let { it.setIslandEnabled(!it.isIslandEnabled()) }
            is Action.SometimesEssentials -> applySometimesEssentials(context, action)
            else -> return false
        }
        return true
    }

    /** Takes a screenshot using the accessibility service if running. */
    private fun takeScreenshot(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val serviceInst = ScreenOffAccessibilityService.instance
            if (serviceInst != null) {
                serviceInst.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
            } else {
                Toast
                    .makeText(
                        context,
                        "Accessibility service is not running",
                        Toast.LENGTH_SHORT,
                    ).show()
            }
        }
    }

    /** Opens the Pixel search results activity. */
    private fun openPixelSearch(context: Context) {
        try {
            val intent =
                Intent(context, PixelSearchResultsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Applies overlay control configuration for duo, island, and glance overlays. */
    private fun applyOverlayControl(
        context: Context,
        action: Action.OverlayControl,
    ) {
        val settings = SettingsRepository(context)
        val duo = DuoSettings(context)
        val glance = StatusGlanceSettings(context)

        fun resolve(
            mode: Action.OverlayMode,
            current: Boolean,
        ): Boolean? =
            when (mode) {
                Action.OverlayMode.SKIP -> null
                Action.OverlayMode.OFF -> false
                Action.OverlayMode.ON -> true
                Action.OverlayMode.TOGGLE -> !current
            }
        resolve(action.duo, duo.isDuoEnabled())?.let(duo::setDuoEnabled)
        resolve(action.island, settings.isIslandEnabled())?.let(settings::setIslandEnabled)
        resolve(action.statusGlance, glance.isStatusGlanceEnabled())?.let(glance::setStatusGlanceEnabled)
    }

    /** Applies sometimes essentials actions for display, power, and notifications. */
    private fun applySometimesEssentials(
        context: Context,
        action: Action.SometimesEssentials,
    ) {
        val repository = SettingsRepository(context)

        if (action.changeNotificationLighting) {
            repository.putBoolean(
                NotificationSettings.KEY_EDGE_LIGHTING_ENABLED,
                action.notificationLightingEnabled,
            )
        }

        if (action.changeFlashlightPulse) {
            repository.putBoolean(
                NotificationSettings.KEY_FLASHLIGHT_PULSE_ENABLED,
                action.flashlightPulseEnabled,
            )
        }

        if (action.changeBatteryNotification) {
            PowerSettings(context).setBatteryNotificationEnabled(action.batteryNotificationEnabled)
        }

        if (action.changeSmartPixels) {
            repository.putBoolean(
                DisplaySettings.KEY_SMART_PIXELS_ENABLED,
                action.smartPixelsEnabled,
            )
        }

        applyToolkitOnDisplay(action, repository)
        applyAlwaysOnDisplay(context, action, repository)

        if (action.changeGloveMode) {
            val textAnimations = TextAnimationsSettings(context)
            val newMode = if (action.gloveModeEnabled) "glove" else "default"
            val currentMode = textAnimations.getScaleAnimationsMode()
            if (currentMode != newMode) {
                val profile = textAnimations.getScaleAnimationsProfile(newMode)
                textAnimations.setScaleAnimationsMode(newMode)
                textAnimations.applyProfile(profile)
            }
        }

        applyLockScreenClock(context, action)

        if (action.changeSyncSoundModeWatch) {
            val prefs =
                context.getSharedPreferences("toolkit_prefs", Context.MODE_PRIVATE)
            prefs
                .edit()
                .putBoolean(
                    "watch_sync_sound_mode_enabled",
                    action.syncSoundModeWatchEnabled,
                ).apply()
        }
    }

    /** Applies ambient music glance display settings. */
    private fun applyToolkitOnDisplay(
        action: Action.SometimesEssentials,
        repository: SettingsRepository,
    ) {
        if (action.changeToolkitOnDisplay) {
            when (action.toolkitOnDisplayMode) {
                "Off" -> {
                    repository.putBoolean(
                        DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED,
                        false,
                    )
                    repository.putBoolean(
                        DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE,
                        false,
                    )
                }

                "On" -> {
                    repository.putBoolean(
                        DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED,
                        true,
                    )
                    repository.putBoolean(
                        DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE,
                        false,
                    )
                }

                "Docked" -> {
                    repository.putBoolean(
                        DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_ENABLED,
                        true,
                    )
                    repository.putBoolean(
                        DisplaySettings.KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE,
                        true,
                    )
                }
            }
        }
    }

    /** Applies always-on display mode and force turn-off preference. */
    private fun applyAlwaysOnDisplay(
        context: Context,
        action: Action.SometimesEssentials,
        repository: SettingsRepository,
    ) {
        if (action.changeAlwaysOnDisplay) {
            val displaySettings = DisplaySettings(context)
            when (action.alwaysOnDisplayMode) {
                "Off" -> {
                    displaySettings.setAodEnabled(false)
                    repository.putBoolean(
                        DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED,
                        false,
                    )
                }

                "On" -> {
                    displaySettings.setAodEnabled(true)
                    repository.putBoolean(
                        DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED,
                        false,
                    )
                }

                "Dynamic" -> {
                    displaySettings.setAodEnabled(true)
                    repository.putBoolean(
                        DisplaySettings.KEY_AOD_FORCE_TURN_OFF_ENABLED,
                        true,
                    )
                }
            }
        }
    }

    /** Updates the custom lock screen clock face setting. */
    private fun applyLockScreenClock(
        context: Context,
        action: Action.SometimesEssentials,
    ) {
        if (action.changeLockScreenClock) {
            val key = "lock_screen_custom_clock_face"
            val value = "{\"clockId\":\"${action.lockScreenClockStyle}\"}"
            val success =
                try {
                    Settings.Secure.putString(
                        context.contentResolver,
                        key,
                        value,
                    )
                } catch (e: Exception) {
                    false
                }
            if (!success) {
                val command = "settings put secure $key $value"
                ShellUtils
                    .runCommand(context, command)
            }
        }
    }

    /** Opens the active Now Playing media session app if available. */
    private fun openNowPlayingApp(context: Context) {
        try {
            val manager =
                context.getSystemService(Context.MEDIA_SESSION_SERVICE) as? MediaSessionManager ?: return
            val componentName = ComponentName(context, NotificationListener::class.java)
            val sessions = manager.getActiveSessions(componentName)
            val activeSession =
                sessions
                    .sortedWith(
                        compareByDescending<MediaController> {
                            val state = it.playbackState?.state
                            state == PlaybackState.STATE_PLAYING || state == PlaybackState.STATE_BUFFERING
                        }.thenByDescending {
                            val state = it.playbackState?.state
                            state == PlaybackState.STATE_PAUSED
                        },
                    ).firstOrNull()

            val packageName = activeSession?.packageName
            if (!packageName.isNullOrEmpty()) {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                }
            }
        } catch (_: Exception) {
        }
    }
}
