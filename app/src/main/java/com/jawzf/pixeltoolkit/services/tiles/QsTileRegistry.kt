package com.jawzf.pixeltoolkit.services.tiles

import com.jawzf.pixeltoolkit.core.data.power.CaffeinateController
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.tiles.BaseTileService
import com.jawzf.pixeltoolkit.feature.apps.applock.AppLockTileService
import com.jawzf.pixeltoolkit.feature.apps.freeze.AppFreezingTileService
import com.jawzf.pixeltoolkit.feature.apps.launcher.DefaultLauncherTileService
import com.jawzf.pixeltoolkit.feature.connectivity.dns.PrivateDnsTileService
import com.jawzf.pixeltoolkit.feature.connectivity.nfc.NfcTileService
import com.jawzf.pixeltoolkit.feature.connectivity.sim.DataSimController
import com.jawzf.pixeltoolkit.feature.connectivity.sim.DataSimTileService
import com.jawzf.pixeltoolkit.feature.display.aod.AlwaysOnDisplayTileService
import com.jawzf.pixeltoolkit.feature.display.duo.DuoTileService
import com.jawzf.pixeltoolkit.feature.display.nightlight.DynamicNightLightTileService
import com.jawzf.pixeltoolkit.feature.display.ondisplay.ToolkitOnDisplayTileService
import com.jawzf.pixeltoolkit.feature.display.pixels.SmartPixelsTileService
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRateTileService
import com.jawzf.pixeltoolkit.feature.input.links.UrlShortenerTileService
import com.jawzf.pixeltoolkit.feature.island.IslandTileService
import com.jawzf.pixeltoolkit.feature.notifications.flashlight.FlashlightPulseTileService
import com.jawzf.pixeltoolkit.feature.notifications.lighting.NotificationLightingTileService
import com.jawzf.pixeltoolkit.feature.power.caffeinate.CaffeinateTileService
import com.jawzf.pixeltoolkit.feature.power.charging.ChargeQuickTileService
import com.jawzf.pixeltoolkit.feature.power.maps.MapsPowerSavingTileService
import com.jawzf.pixeltoolkit.feature.security.lockdown.LockdownTileService
import com.jawzf.pixeltoolkit.feature.security.lockscreen.ScreenLockedSecurityTileService
import com.jawzf.pixeltoolkit.feature.sound.mode.SoundModeTileService
import com.jawzf.pixeltoolkit.feature.sound.mono.MonoAudioTileService
import com.jawzf.pixeltoolkit.feature.tiles.AdaptiveBrightnessTileService
import com.jawzf.pixeltoolkit.feature.tiles.BubblesTileService
import com.jawzf.pixeltoolkit.feature.tiles.ColorPickerTileService
import com.jawzf.pixeltoolkit.feature.tiles.DeveloperOptionsTileService
import com.jawzf.pixeltoolkit.feature.tiles.HapticsSettings
import com.jawzf.pixeltoolkit.feature.tiles.HapticsTileService
import com.jawzf.pixeltoolkit.feature.tiles.HeadsUpNotificationsTileService
import com.jawzf.pixeltoolkit.feature.tiles.PrivateNotificationsTileService
import com.jawzf.pixeltoolkit.feature.tiles.RestartSystemUiTileService
import com.jawzf.pixeltoolkit.feature.tiles.StayAwakeTileService
import com.jawzf.pixeltoolkit.feature.tiles.TapToWakeTileService
import com.jawzf.pixeltoolkit.feature.tiles.UiBlurTileService
import com.jawzf.pixeltoolkit.feature.tiles.UsbDebuggingTileService

object QsTileRegistry {
    data class QsTileEntry(
        val iconRes: Int,
        val serviceClass: Class<*>,
    )

    val ALL_TILES: List<QsTileEntry> =
        listOf(
            QsTileEntry(
                R.drawable.rounded_blur_on_24,
                UiBlurTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_bubble_24,
                BubblesTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_notifications_off_24,
                PrivateNotificationsTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_notifications_unread_24,
                HeadsUpNotificationsTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_touch_app_24,
                TapToWakeTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_mobile_text_2_24,
                AlwaysOnDisplayTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_coffee_24,
                CaffeinateTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_volume_up_24,
                SoundModeTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_blur_linear_24,
                NotificationLightingTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_nightlight_24,
                DynamicNightLightTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_security_24,
                ScreenLockedSecurityTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_shield_lock_24,
                AppLockTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_headphones_24,
                MonoAudioTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_flashlight_on_24,
                FlashlightTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_app_badging_24,
                AppFreezingTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.outline_backlight_high_24,
                FlashlightPulseTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_av_timer_24,
                StayAwakeTileService::class.java,
            ),
            QsTileEntry(R.drawable.rounded_nfc_24, NfcTileService::class.java),
            QsTileEntry(
                R.drawable.rounded_brightness_auto_24,
                AdaptiveBrightnessTileService::class.java,
            ),
            QsTileEntry(R.drawable.rounded_mobile_vibrate_24, HapticsTileService::class.java),
            QsTileEntry(R.drawable.rounded_sim_card_24, DataSimTileService::class.java),
            QsTileEntry(
                R.drawable.rounded_front_hand_24,
                ScaleAnimationsTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.reopen_window_24px,
                RestartSystemUiTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_shutter_speed_24,
                RefreshRateTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_navigation_24,
                MapsPowerSavingTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_dns_24,
                PrivateDnsTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_adb_24,
                UsbDebuggingTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_colorize_24,
                ColorPickerTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_home_24,
                DefaultLauncherTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_mobile_code_24,
                DeveloperOptionsTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_battery_android_frame_shield_24,
                ChargeQuickTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_grain_24,
                SmartPixelsTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_live_tv_24,
                ToolkitOnDisplayTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_lock_24,
                LockdownTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_link_24,
                UrlShortenerTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_motion_play_24,
                DuoTileService::class.java,
            ),
            QsTileEntry(
                R.drawable.rounded_upcoming_24,
                IslandTileService::class.java,
            ),
        )

    private val tilesMapByClassName: Map<String, QsTileEntry> by lazy {
        ALL_TILES.associateBy { it.serviceClass.name }
    }

    fun getTileByClassName(className: String): QsTileEntry? = tilesMapByClassName[className]

    fun isTileActive(
        context: android.content.Context,
        className: String,
    ): Boolean =
        try {
            when (className) {
                HapticsTileService::class.java.name -> HapticsSettings.isEnabled(context)
                DataSimTileService::class.java.name ->
                    PermissionUtils.hasReadPhoneStatePermission(context) &&
                        ShellUtils.hasPermission(context) &&
                        DataSimController.lastKnownState?.next != null
                CaffeinateTileService::class.java.name -> {
                    CaffeinateController.isActive.value ||
                            CaffeinateController.isStarting.value
                }

                FlashlightTileService::class.java.name -> {
                    val instance = ScreenOffAccessibilityService.instance
                    if (instance != null) {
                        instance.flashlightHandler.isTorchOn
                    } else {
                        val clazz = Class.forName(className)
                        val tileService =
                            clazz.getDeclaredConstructor().newInstance() as BaseTileService
                        val attachBaseContextMethod =
                            android.content.ContextWrapper::class.java.getDeclaredMethod(
                                "attachBaseContext",
                                android.content.Context::class.java,
                            )
                        attachBaseContextMethod.isAccessible = true
                        attachBaseContextMethod.invoke(tileService, context)
                        val getTileStateMethod =
                            BaseTileService::class.java.getDeclaredMethod("getTileState")
                        getTileStateMethod.isAccessible = true
                        (getTileStateMethod.invoke(tileService) as Int) == android.service.quicksettings.Tile.STATE_ACTIVE
                    }
                }

                else -> {
                    val clazz = Class.forName(className)
                    if (BaseTileService::class.java.isAssignableFrom(clazz)) {
                        val tileService =
                            clazz.getDeclaredConstructor().newInstance() as BaseTileService
                        val attachBaseContextMethod =
                            android.content.ContextWrapper::class.java.getDeclaredMethod(
                                "attachBaseContext",
                                android.content.Context::class.java,
                            )
                        attachBaseContextMethod.isAccessible = true
                        attachBaseContextMethod.invoke(tileService, context)

                        val getTileStateMethod =
                            BaseTileService::class.java.getDeclaredMethod("getTileState")
                        getTileStateMethod.isAccessible = true
                        val state = getTileStateMethod.invoke(tileService) as Int
                        state == android.service.quicksettings.Tile.STATE_ACTIVE
                    } else {
                        false
                    }
                }
            }
        } catch (e: Exception) {
            false
        }

    fun getTileLabel(
        context: android.content.Context,
        className: String,
    ): String =
        try {
            val clazz = Class.forName(className)
            if (BaseTileService::class.java.isAssignableFrom(clazz)) {
                val tileService =
                    clazz.getDeclaredConstructor().newInstance() as BaseTileService
                val attachBaseContextMethod =
                    android.content.ContextWrapper::class.java.getDeclaredMethod(
                        "attachBaseContext",
                        android.content.Context::class.java,
                    )
                attachBaseContextMethod.isAccessible = true
                attachBaseContextMethod.invoke(tileService, context)

                val getTileLabelMethod =
                    BaseTileService::class.java.getDeclaredMethod("getTileLabel")
                getTileLabelMethod.isAccessible = true
                (getTileLabelMethod.invoke(tileService) as? String) ?: ""
            } else {
                ""
            }
        } catch (e: Exception) {
            ""
        }

    fun getTileSubtitle(
        context: android.content.Context,
        className: String,
    ): String =
        try {
            when (className) {
                HapticsTileService::class.java.name ->
                    context.getString(if (HapticsSettings.isEnabled(context)) R.string.tile_active else R.string.tile_inactive)
                DataSimTileService::class.java.name -> {
                    val state = DataSimController.lastKnownState
                    val selected = state?.selected
                    when {
                        !PermissionUtils.hasReadPhoneStatePermission(context) || !ShellUtils.hasPermission(context) -> context.getString(R.string.permission_missing)
                        selected != null -> selected.name ?: context.getString(R.string.tile_data_sim_slot, selected.slot + 1)
                        state == null -> context.getString(R.string.tile_data_sim_loading)
                        else -> context.getString(R.string.tile_data_sim_no_selection)
                    }
                }
                CaffeinateTileService::class.java.name -> {
                    if (CaffeinateController.isActive.value) {
                        "Active"
                    } else if (CaffeinateController.isStarting.value) {
                        "Starting"
                    } else {
                        "Off"
                    }
                }

                FlashlightTileService::class.java.name -> {
                    val instance = ScreenOffAccessibilityService.instance
                    val isTorchOn = instance?.flashlightHandler?.isTorchOn ?: false
                    if (isTorchOn) "On" else "Off"
                }

                else -> {
                    val clazz = Class.forName(className)
                    if (BaseTileService::class.java.isAssignableFrom(clazz)) {
                        val tileService =
                            clazz.getDeclaredConstructor().newInstance() as BaseTileService
                        val attachBaseContextMethod =
                            android.content.ContextWrapper::class.java.getDeclaredMethod(
                                "attachBaseContext",
                                android.content.Context::class.java,
                            )
                        attachBaseContextMethod.isAccessible = true
                        attachBaseContextMethod.invoke(tileService, context)

                        val getTileSubtitleMethod =
                            BaseTileService::class.java.getDeclaredMethod("getTileSubtitle")
                        getTileSubtitleMethod.isAccessible = true
                        (getTileSubtitleMethod.invoke(tileService) as? String) ?: ""
                    } else {
                        ""
                    }
                }
            }
        } catch (e: Exception) {
            ""
        }

    fun getTileIcon(
        context: android.content.Context,
        className: String,
        fallbackIconRes: Int,
    ): Int =
        try {
            when (className) {
                FlashlightTileService::class.java.name -> {
                    val instance = ScreenOffAccessibilityService.instance
                    val isTorchOn =
                        if (instance != null) {
                            instance.flashlightHandler.isTorchOn
                        } else {
                            false
                        }
                    if (isTorchOn) R.drawable.round_flashlight_on_24 else R.drawable.rounded_flashlight_on_24
                }

                else -> {
                    val clazz = Class.forName(className)
                    if (BaseTileService::class.java.isAssignableFrom(clazz)) {
                        val tileService =
                            clazz.getDeclaredConstructor().newInstance() as BaseTileService
                        val attachBaseContextMethod =
                            android.content.ContextWrapper::class.java.getDeclaredMethod(
                                "attachBaseContext",
                                android.content.Context::class.java,
                            )
                        attachBaseContextMethod.isAccessible = true
                        attachBaseContextMethod.invoke(tileService, context)

                        val getTileIconMethod =
                            BaseTileService::class.java.getDeclaredMethod("getTileIcon")
                        getTileIconMethod.isAccessible = true
                        val iconObj =
                            getTileIconMethod.invoke(tileService) as? android.graphics.drawable.Icon
                        if (iconObj != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            val resId = iconObj.resId
                            if (resId != 0) resId else fallbackIconRes
                        } else {
                            fallbackIconRes
                        }
                    } else {
                        fallbackIconRes
                    }
                }
            }
        } catch (e: Exception) {
            fallbackIconRes
        }
}
