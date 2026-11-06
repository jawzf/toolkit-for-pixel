package com.jawzf.pixeltoolkit.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.core.platform.shell.RootUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.domain.diy.Action
import com.jawzf.pixeltoolkit.domain.diy.ActionGsonAdapter
import com.jawzf.pixeltoolkit.domain.model.AppIcon
import com.jawzf.pixeltoolkit.domain.model.PowerChoice
import com.jawzf.pixeltoolkit.domain.model.RemapSlot
import com.jawzf.pixeltoolkit.domain.model.ScaleAnimationsProfile
import com.jawzf.pixeltoolkit.domain.model.SystemShortcutsState
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.power.PowerSettings
import com.jawzf.pixeltoolkit.ui.core.pickers.NetworkType
import com.jawzf.pixeltoolkit.utils.DuoBatteryLevels
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SettingsRepository(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    init {
        migrateUsageAccessKey()
        migrateRemapStringToAction()
        migratePixelSearchbarType()
    }

    private fun migrateUsageAccessKey() {
        val oldKey = "app_lock_use_usage_access"
        if (prefs.contains(oldKey)) {
            val value = prefs.getBoolean(oldKey, false)
            if (!prefs.contains(KEY_USE_USAGE_ACCESS)) {
                putBoolean(KEY_USE_USAGE_ACCESS, value)
            }
            remove(oldKey)
        }
    }

    private fun migrateRemapStringToAction() {
        if (getBoolean(KEY_BUTTON_REMAP_MIGRATION_DONE)) return

        val remapKeys =
            listOf(
                KEY_BUTTON_REMAP_VOL_UP_ACTION_OFF,
                KEY_BUTTON_REMAP_VOL_DOWN_ACTION_OFF,
                KEY_BUTTON_REMAP_VOL_UP_ACTION_ON,
                KEY_BUTTON_REMAP_VOL_DOWN_ACTION_ON,
            )
        for (key in remapKeys) {
            val raw = prefs.getString(key, null) ?: continue
            if (raw.startsWith("{") || raw.startsWith("[")) continue
            val action: Action? =
                when (raw) {
                    "Toggle flashlight" -> Action.ToggleFlashlight
                    "Media play/pause" -> Action.MediaPlayPause
                    "Media next" -> Action.MediaNext
                    "Media previous" -> Action.MediaPrevious
                    "Toggle vibrate" -> Action.ToggleVibrate
                    "Toggle mute" -> Action.ToggleMute
                    "AI assistant" -> Action.AIAssistant
                    "Take screenshot" -> Action.TakeScreenshot
                    "Cycle sound modes" -> Action.CycleSoundModes
                    "Toggle media volume" -> Action.ToggleMediaVolume
                    "Like current song" -> Action.LikeCurrentSong
                    "Circle to Search" -> Action.CircleToSearch
                    else -> null
                }
            setRemapAction(key, action)
        }

        putBoolean(KEY_BUTTON_REMAP_MIGRATION_DONE, true)
    }

    private fun migratePixelSearchbarType() {
        if (!prefs.getBoolean(KEY_PIXEL_SEARCHBAR_MIGRATED_V1, false)) {
            putBoolean(KEY_PIXEL_SEARCHBAR_MIGRATED_V1, true)
            val raw = prefs.getString(KEY_PIXEL_SEARCHBAR_TYPE, null)
            if (raw == null || raw == "empty") {
                putString(KEY_PIXEL_SEARCHBAR_TYPE, "searchbar")
            }
        }
    }

    fun getRemapAction(key: String): Action? {
        val json = prefs.getString(key, null) ?: return null
        return try {
            ActionGsonAdapter.fromJson(json)
        } catch (_: Exception) {
            null
        }
    }

    fun setRemapAction(
        key: String,
        action: Action?,
    ) {
        if (action == null) {
            prefs.edit().remove(key).apply()
        } else {
            prefs.edit().putString(key, ActionGsonAdapter.toJson(action)).apply()
        }
    }

    fun getRemapActions(key: String): List<Action> {
        val json = prefs.getString(key, null) ?: return emptyList()
        return if (json.trimStart().startsWith("[")) {
            ActionGsonAdapter.listFromJson(json)
        } else {
            listOfNotNull(ActionGsonAdapter.fromJson(json))
        }
    }

    fun setRemapActions(
        key: String,
        actions: List<Action>,
    ) {
        when (actions.size) {
            0 -> prefs.edit().remove(key).apply()
            1 -> setRemapAction(key, actions.first())
            else -> prefs.edit().putString(key, ActionGsonAdapter.listToJson(actions)).apply()
        }
    }

    fun getPowerChoice(slot: RemapSlot): PowerChoice {
        val defaultChoice = slot.defaultChoice ?: PowerChoice.ASSISTANT
        val key = slot.choicePrefKey ?: return defaultChoice
        val value = prefs.getString(key, null) ?: return defaultChoice
        return try {
            PowerChoice.valueOf(value)
        } catch (_: IllegalArgumentException) {
            defaultChoice
        }
    }

    fun setPowerChoice(
        slot: RemapSlot,
        choice: PowerChoice,
    ) {
        val key = slot.choicePrefKey ?: return
        prefs.edit().putString(key, choice.name).apply()
    }

    companion object {
        const val PREFS_NAME = "toolkit_prefs"

        // Keys
        const val KEY_DEBUGGING_TILE_TAP_ACTION = "debugging_tile_tap_action"
        const val KEY_SMART_PIXELS_ENABLED = "smart_pixels_enabled"
        const val KEY_SMART_PIXELS_INTENSITY = "smart_pixels_intensity"
        const val KEY_SMART_PIXELS_DISABLE_ON_CAST = "smart_pixels_disable_on_cast"
        const val KEY_WIDGET_ENABLED = "widget_enabled"
        const val KEY_STATUS_BAR_ICON_CONTROL_ENABLED = "status_bar_icon_control_enabled"
        const val KEY_LOCK_SCREEN_WALLPAPER_SOURCE = "lock_screen_wallpaper_source"

        const val KEY_LAST_CALL_STATE = "last_call_state"

        const val KEY_BUTTON_REMAP_ENABLED = "button_remap_enabled"
        const val KEY_FLASHLIGHT_VOLUME_TOGGLE_ENABLED =
            "flashlight_volume_toggle_enabled" // Legacy
        const val KEY_BUTTON_REMAP_USE_SHIZUKU = "button_remap_use_shizuku"
        const val KEY_SHIZUKU_DETECTED_DEVICE_PATH = "shizuku_detected_device_path"
        const val KEY_FLASHLIGHT_TRIGGER_BUTTON = "flashlight_trigger_button" // Legacy
        const val KEY_BUTTON_REMAP_VOL_UP_ACTION_OFF = "button_remap_vol_up_action_off"
        const val KEY_BUTTON_REMAP_VOL_UP_ACTION = "button_remap_vol_up_action" // Legacy
        const val KEY_BUTTON_REMAP_VOL_DOWN_ACTION_OFF = "button_remap_vol_down_action_off"
        const val KEY_BUTTON_REMAP_VOL_DOWN_ACTION = "button_remap_vol_down_action" // Legacy
        const val KEY_BUTTON_REMAP_VOL_UP_ACTION_ON = "button_remap_vol_up_action_on"
        const val KEY_BUTTON_REMAP_VOL_DOWN_ACTION_ON = "button_remap_vol_down_action_on"
        const val KEY_BUTTON_REMAP_POWER_HOLD_ACTION_OFF = "button_remap_power_hold_action_off"
        const val KEY_BUTTON_REMAP_POWER_HOLD_ACTION_ON = "button_remap_power_hold_action_on"
        const val KEY_BUTTON_REMAP_POWER_HOLD_CHOICE_OFF = "button_remap_power_hold_choice_off"
        const val KEY_BUTTON_REMAP_POWER_HOLD_CHOICE_ON = "button_remap_power_hold_choice_on"
        const val KEY_BUTTON_REMAP_POWER_OVERRIDDEN = "button_remap_power_overridden"
        const val KEY_BUTTON_REMAP_POWER_CAMERA_FLIP = "button_remap_power_camera_flip"
        const val KEY_BUTTON_REMAP_HAPTIC_TYPE = "button_remap_haptic_type"
        const val KEY_FLASHLIGHT_HAPTIC_TYPE = "flashlight_haptic_type" // Legacy
        const val KEY_BUTTON_REMAP_MIGRATION_DONE = "button_remap_action_migration_done"
        const val KEY_BUTTON_REMAP_PAUSE_ON_VOLUME_DIALOG = "button_remap_pause_on_volume_dialog"
        const val KEY_PINNED_ACTION_PREFIX = "pinned_action_"
        const val KEY_LOCKSCREEN_SHORTCUTS_ENABLED = "lockscreen_shortcuts_enabled"
        const val KEY_LOCKSCREEN_SHORTCUT_LEFT_ACTIONS = "lockscreen_shortcut_left_actions"
        const val KEY_LOCKSCREEN_SHORTCUT_RIGHT_ACTIONS = "lockscreen_shortcut_right_actions"
        const val KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE = "lockscreen_system_shortcuts_state"

        const val KEY_DYNAMIC_NIGHT_LIGHT_ENABLED = "dynamic_night_light_enabled"
        const val KEY_DYNAMIC_NIGHT_LIGHT_SELECTED_APPS = "dynamic_night_light_selected_apps"

        const val KEY_FLASHLIGHT_ALWAYS_TURN_OFF_ENABLED = "flashlight_always_turn_off_enabled"
        const val KEY_FLASHLIGHT_FADE_ENABLED = "flashlight_fade_enabled"
        const val KEY_FLASHLIGHT_ADJUST_INTENSITY_ENABLED = "flashlight_adjust_intensity_enabled"
        const val KEY_FLASHLIGHT_GLOBAL_ENABLED = "flashlight_global_enabled"
        const val KEY_FLASHLIGHT_LIVE_UPDATE_ENABLED = "flashlight_live_update_enabled"
        const val KEY_FLASHLIGHT_LAST_INTENSITY = "flashlight_last_intensity"
        const val KEY_FLASHLIGHT_POCKET_TURN_OFF_ENABLED = "flashlight_pocket_turn_off_enabled"
        const val KEY_FLASHLIGHT_OVERHEAT_PREVENTION_ENABLED =
            "flashlight_overheat_prevention_enabled"

        const val KEY_HIDE_SYSTEM_ICONS = "hide_system_icons"
        const val KEY_HIDE_SYSTEM_ICONS_LOCKED_ONLY = "hide_system_icons_locked_only"
        const val KEY_HIDE_CLOCK = "hide_clock"
        const val KEY_HIDE_NOTIFICATION_ICONS = "hide_notification_icons"
        const val KEY_HIDE_GESTURE_BAR_ENABLED = "hide_gesture_bar_enabled"
        const val KEY_HIDE_GESTURE_BAR_ON_LAUNCHER_ENABLED = "hide_gesture_bar_on_launcher_enabled"
        const val KEY_CIRCLE_TO_SEARCH_GESTURE_ENABLED = "circle_to_search_gesture_enabled"
        const val KEY_CIRCLE_TO_SEARCH_GESTURE_HEIGHT = "circle_to_search_gesture_height"
        const val KEY_CIRCLE_TO_SEARCH_GESTURE_WIDTH = "circle_to_search_gesture_width"
        const val KEY_CIRCLE_TO_SEARCH_PREVIEW_ENABLED = "circle_to_search_preview_enabled"

        const val KEY_HIDDEN_DEBUGGING_SUPPORT = "hidden_debugging_support"
        const val KEY_USE_USAGE_ACCESS = "use_usage_access"

        const val KEY_DEVELOPER_MODE_ENABLED = "developer_mode_enabled"
        const val KEY_HAPTIC_FEEDBACK_TYPE = "haptic_feedback_type"
        const val KEY_DEFAULT_TAB = "default_tab"
        const val KEY_APP_ICON = "app_icon_style"
        const val KEY_USE_ROOT = "use_root"
        const val KEY_PRIVILEGED_MODE = "privileged_mode"
        const val KEY_PITCH_BLACK_THEME_ENABLED = "pitch_black_theme_enabled"
        const val KEY_ENABLE_UNSUPPORTED_FEATURES = "enable_unsupported_features"
        const val KEY_SHOW_LEGACY_FEATURES = "show_legacy_features"

        const val KEY_PINNED_FEATURES = "pinned_features"
        const val KEY_PINNED_QS_TILES = "pinned_qs_tiles"
        const val KEY_SECURE_SENSITIVE_TILES = "secure_sensitive_tiles"
        const val KEY_LIKE_SONG_TOAST_ENABLED = "like_song_toast_enabled"
        const val KEY_LIKE_SONG_AOD_OVERLAY_ENABLED = "like_song_aod_overlay_enabled"
        const val KEY_AMBIENT_MUSIC_GLANCE_ENABLED = "ambient_music_glance_enabled"
        const val KEY_AMBIENT_MUSIC_GLANCE_DOCKED_MODE = "ambient_music_glance_docked_mode"
        const val KEY_AMBIENT_MUSIC_GLANCE_RANDOM_SHAPES = "ambient_music_glance_random_shapes"
        const val KEY_AMBIENT_MUSIC_GLANCE_ALBUM_ART_MODE = "ambient_music_glance_album_art_mode"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_SIZE = "ambient_music_glance_clock_size"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WEIGHT = "ambient_music_glance_clock_weight"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WIDTH = "ambient_music_glance_clock_width"
        const val KEY_AMBIENT_MUSIC_GLANCE_CLOCK_ROUNDNESS = "ambient_music_glance_clock_roundness"
        const val KEY_AMBIENT_MUSIC_GLANCE_FORCE_FILL_WHILE_CHARGING =
            "ambient_music_glance_force_fill_while_charging"
        const val KEY_AMBIENT_MUSIC_GLANCE_RESPECT_NOTIFICATIONS =
            "ambient_music_glance_respect_notifications"

        const val KEY_FONT_SCALE = "font_scale"
        const val KEY_FONT_WEIGHT = "font_weight"
        const val KEY_ANIMATOR_DURATION_SCALE = "animator_duration_scale"
        const val KEY_TRANSITION_ANIMATION_SCALE = "transition_animation_scale"
        const val KEY_WINDOW_ANIMATION_SCALE = "window_animation_scale"
        const val KEY_SMALLEST_WIDTH = "smallest_width"
        const val KEY_NOTIFICATION_GLANCE_ENABLED = "notification_glance_enabled"
        const val KEY_NOTIFICATION_GLANCE_SAME_AS_LIGHTING = "notification_glance_same_as_lighting"
        const val KEY_NOTIFICATION_GLANCE_SELECTED_APPS = "notification_glance_selected_apps"
        const val KEY_AOD_FORCE_TURN_OFF_ENABLED = "aod_force_turn_off_enabled"
        const val KEY_AOD_WALLPAPER_ENABLED = "aod_wallpaper_enabled"
        const val KEY_AOD_WALLPAPER_OPACITY = "aod_wallpaper_opacity"
        const val KEY_AOD_WALLPAPER_TIMEOUT = "aod_wallpaper_timeout"
        const val KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT = "aod_wallpaper_custom_timeout"
        const val KEY_AOD_WALLPAPER_BLUR = "aod_wallpaper_blur"
        const val KEY_AOD_WALLPAPER_VIGNETTE = "aod_wallpaper_vignette"
        const val KEY_AOD_WALLPAPER_BLACK_THRESHOLD = "aod_wallpaper_black_threshold"
        const val KEY_AOD_WALLPAPER_CUSTOM_IMAGE = "aod_wallpaper_custom_image"
        const val KEY_AOD_WALLPAPER_USE_ALBUM_ART = "aod_wallpaper_use_album_art"
        const val KEY_AOD_WALLPAPER_DISABLE_ON_DND = "aod_wallpaper_disable_on_dnd"
        const val KEY_AOD_WALLPAPER_KEEP_ON_MEDIA = "aod_wallpaper_keep_on_media"
        const val KEY_AOD_WALLPAPER_MEDIA_EXCLUDED_APPS = "aod_wallpaper_media_excluded_apps"
        const val KEY_AOD_WALLPAPER_EXTENDED_MEDIA = "aod_wallpaper_extended_media"
        const val KEY_AOD_WALLPAPER_EXTENDED_APP_ICON = "aod_wallpaper_extended_app_icon"
        const val KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE = "aod_wallpaper_extended_text_scale"
        const val KEY_PIXEL_SEARCH_RESULT_APPS = "pixel_search_result_apps"
        const val KEY_PIXEL_SEARCH_RESULT_CONTACTS = "pixel_search_result_contacts"
        const val KEY_PIXEL_SEARCH_RESULT_SETTINGS = "pixel_search_result_settings"
        const val KEY_PIXEL_SEARCH_RESULT_SHORTCUTS = "pixel_search_result_shortcuts"
        const val KEY_PIXEL_SEARCH_RESULT_WEB = "pixel_search_result_web"
        const val KEY_PIXEL_SEARCH_RESULT_MEDIA = "pixel_search_result_media"
        const val KEY_PIXEL_SEARCH_RESULT_FILES = "pixel_search_result_files"
        const val KEY_PIXEL_SEARCH_BUBBLES_WEB = "pixel_search_bubbles_web"
        const val KEY_PIXEL_SEARCH_ENGINE = "pixel_search_engine"
        const val KEY_AUTO_ACCESSIBILITY_ENABLED = "auto_accessibility_enabled"
        const val KEY_USE_BLUR = "use_blur"
        const val KEY_MOTION_BLUR = "motion_blur"
        const val KEY_MOTION_BLUR_SCALE = "motion_blur_scale"
        const val KEY_SHAKE_FOR_HELP = "shake_for_help"
        const val KEY_SWIPE_TABS = "swipe_tabs"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_SCALE_ANIMATIONS_MODE = "scale_animations_mode"
        const val KEY_SCALE_ANIMATIONS_DEFAULT_PROFILE = "scale_animations_default_profile"
        const val KEY_SCALE_ANIMATIONS_GLOVE_PROFILE = "scale_animations_glove_profile"
        const val KEY_REFRESH_RATE_MODE = "refresh_rate_mode"
        const val KEY_REFRESH_RATE_FIXED = "refresh_rate_fixed"
        const val KEY_REFRESH_RATE_MIN = "refresh_rate_min"
        const val KEY_REFRESH_RATE_PEAK = "refresh_rate_peak"
        const val KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY = "refresh_rate_default_peak_infinity"

        // Duo
        const val KEY_DUO_ENABLED = "duo_enabled"
        const val KEY_DUO_ISLAND_COMBINED = "duo_island_combined"
        const val KEY_DUO_TAP_FOR_BRIEF = "duo_tap_for_brief"
        const val KEY_DUO_HIDE_ON_SHADE = "duo_hide_on_shade"
        const val KEY_DUO_KEEP_IN_FULLSCREEN = "duo_keep_in_fullscreen"
        const val KEY_DUO_DISABLE_INTERACTIONS_IN_FULLSCREEN = "duo_disable_interactions_in_fullscreen"
        const val KEY_DUO_USE_AUTO_DETECT = "duo_use_auto_detect"
        const val KEY_DUO_CAMERA_OFFSET_X = "duo_camera_offset_x"
        const val KEY_DUO_CAMERA_OFFSET_Y = "duo_camera_offset_y"
        const val KEY_DUO_CAMERA_SIZE = "duo_camera_size"
        const val KEY_DUO_ORIENTATION_PROFILES = "duo_orientation_profiles"
        const val KEY_DUO_HIDE_PORTRAIT = "duo_hide_portrait"
        const val KEY_DUO_HIDE_LANDSCAPE = "duo_hide_landscape"
        const val KEY_DUO_KNOWN_DISPLAY_PROFILES = "duo_known_display_profiles"
        const val KEY_DUO_ARC_THICKNESS = "duo_arc_thickness"
        const val KEY_DUO_DOT_SIZE = "duo_dot_size"
        const val KEY_DUO_RING_RADIUS = "duo_ring_radius"
        const val KEY_DUO_TIME_TEXT_SCALE = "duo_time_text_scale"
        const val KEY_DUO_SHOW_BATTERY = "duo_show_battery"
        const val KEY_DUO_SHOW_BATTERY_PERCENTAGE = "duo_show_battery_percentage"
        const val KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED = "duo_battery_percentage_only_colored"
        const val KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED = "duo_battery_charging_color_enabled"
        const val KEY_DUO_BATTERY_CHARGING_COLOR = "duo_battery_charging_color"
        const val KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED = "duo_battery_power_save_color_enabled"
        const val KEY_DUO_BATTERY_POWER_SAVE_COLOR = "duo_battery_power_save_color"
        const val KEY_DUO_BATTERY_LOW_COLOR_ENABLED = "duo_battery_low_color_enabled"
        const val KEY_DUO_BATTERY_LOW_COLOR = "duo_battery_low_color"
        const val KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED = "island_battery_idle_color_enabled"
        const val KEY_ISLAND_BATTERY_IDLE_COLOR = "island_battery_idle_color"
        const val KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED = "duo_battery_critical_color_enabled"
        const val KEY_DUO_BATTERY_CRITICAL_COLOR = "duo_battery_critical_color"
        const val KEY_DUO_BATTERY_LOW_LEVEL = "duo_battery_low_level"
        const val KEY_DUO_BATTERY_CRITICAL_LEVEL = "duo_battery_critical_level"
        const val KEY_DUO_SHOW_NETWORKS = "duo_show_networks"
        const val KEY_DUO_DIFFERENTIATE_WIFI = "duo_differentiate_wifi"
        const val KEY_DUO_SHOW_TIME = "duo_show_time"
        const val KEY_DUO_SHOW_MEDIA = "duo_show_media"
        const val KEY_DUO_ROTATE_ALBUM_ART = "duo_rotate_album_art"
        const val KEY_DUO_SHOW_PROGRESS = "duo_show_progress"
        const val KEY_DUO_SHOW_FLASHLIGHT = "duo_show_flashlight"
        const val KEY_DUO_HIDE_WHEN_SCREEN_OFF = "duo_hide_when_screen_off"
        const val KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE = "duo_hide_when_screen_off_only_idle"
        const val KEY_DUO_HIDE_WHEN_LOCKED = "duo_hide_when_locked"
        const val KEY_DUO_USE_MATERIAL_YOU = "duo_use_material_you"
        const val KEY_DUO_CUSTOM_COLOR = "duo_custom_color"
        const val KEY_DUO_TAP_ACTION = "duo_tap_action"
        const val KEY_DUO_DOUBLE_TAP_ACTION = "duo_double_tap_action"
        const val KEY_DUO_LONG_PRESS_ACTION = "duo_long_press_action"
        const val KEY_DUO_SWIPE_DOWN_ACTION = "duo_swipe_down_action"
        const val KEY_DUO_SLIDE_MODE = "duo_slide_mode"
        const val KEY_DUO_SLIDE_TRACK = "duo_slide_track"
        const val KEY_DUO_SLIDE_INVERT_DIRECTION = "duo_slide_invert_direction"

        // Island
        const val KEY_ISLAND_ENABLED = "island_enabled"
        const val KEY_ISLAND_USE_AUTO_DETECT = "island_use_auto_detect"
        const val KEY_ISLAND_CAMERA_OFFSET_X = "island_camera_offset_x"
        const val KEY_ISLAND_CAMERA_OFFSET_Y = "island_camera_offset_y"
        const val KEY_ISLAND_CAMERA_SIZE = "island_camera_size"
        const val KEY_ISLAND_MAX_WIDTH = "island_max_width"
        const val KEY_ISLAND_CUTOUT_GAP = "island_cutout_gap"
        const val KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP = "island_suppress_system_heads_up"
        const val KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR = "island_dynamic_hide_status_bar"
        const val KEY_ISLAND_COMPACT_HIDE_STATUS_BAR = "island_compact_hide_status_bar"
        const val KEY_ISLAND_HIDE_ICONS_ENABLED = "island_hide_icons_enabled"
        const val KEY_ISLAND_HIDE_ICONS = "island_hide_icons"
        const val ISLAND_HIDE_ICON_SYSTEM = "system"
        const val ISLAND_HIDE_ICON_NOTIFICATIONS = "notifications"
        const val ISLAND_HIDE_ICON_CLOCK = "clock"
        const val KEY_ISLAND_HIDE_WHEN_SCREEN_OFF = "island_hide_when_screen_off"
        const val KEY_ISLAND_SHOW_WHEN = "island_show_when"
        const val ISLAND_SHOW_WHEN_UNLOCKED = "unlocked"
        const val ISLAND_SHOW_WHEN_SCREEN_ON = "screen_on"
        const val ISLAND_SHOW_WHEN_ALWAYS = "always"
        const val KEY_ISLAND_NOTIF_CONCEAL_LOCKED = "island_notif_conceal_locked"
        const val KEY_ISLAND_NOTIF_DISABLE_LOCKED = "island_notif_disable_locked"
        const val KEY_ISLAND_NOTIF_CONCEAL_CHAT_PICTURES = "island_notif_conceal_chat_pictures"
        const val KEY_ISLAND_CALENDAR_HIDE_LOCKED = "island_calendar_hide_locked"
        const val KEY_ISLAND_TIME_HIDE_LOCKED = "island_time_hide_locked"
        const val KEY_ISLAND_BATTERY_HIDE_LOCKED = "island_battery_hide_locked"
        const val KEY_ISLAND_TIMEOUT_MS = "island_timeout_ms"
        const val KEY_ISLAND_SHOW_GLOW = "island_show_glow"
        const val KEY_ISLAND_EXPANDED_WIDTH = "island_expanded_width"
        const val KEY_ISLAND_EXPANDED_ROUNDNESS = "island_expanded_roundness"
        const val KEY_ISLAND_EXPANDED_PADDING = "island_expanded_padding"
        const val KEY_ISLAND_EXPANDED_TOP_PADDING = "island_expanded_top_padding"
        const val KEY_ISLAND_EXPANDED_BOTTOM_PADDING = "island_expanded_bottom_padding"
        const val KEY_ISLAND_EXPANDED_TIMEOUT_MS = "island_expanded_timeout_ms"
        const val KEY_ISLAND_CATCH_UP_ENABLED = "island_catch_up_enabled"
        const val KEY_ISLAND_CATCH_UP_TIMEOUT_MS = "island_catch_up_timeout_ms"
        const val KEY_ISLAND_SHOW_MEDIA = "island_show_media"
        const val KEY_ISLAND_MEDIA_EXCLUDED_APPS = "island_media_excluded_apps"
        const val KEY_ISLAND_SHOW_CALENDAR = "island_show_calendar"
        const val KEY_ISLAND_SHOW_CONSCIOUS_GATE = "island_show_conscious_gate"
        const val KEY_ISLAND_SHOW_TIME_BATTERY = "island_show_time_battery"
        const val KEY_ISLAND_TIME_SHOW_SECONDS = "island_time_show_seconds"
        const val KEY_ISLAND_THEMED_TEXT = "island_themed_text"
        const val KEY_ISLAND_ICON_COLOR_MODE = "island_icon_color_mode"
        const val KEY_ISLAND_ICON_COLOR_CUSTOM = "island_icon_color_custom"
        const val KEY_ISLAND_ICON_MATERIAL_YOU = "island_icon_material_you"
        const val ISLAND_ICON_COLOR_DYNAMIC = "dynamic"
        const val ISLAND_ICON_COLOR_CUSTOM = "custom"
        const val KEY_ISLAND_TIME_FORMAT = "island_time_format"
        const val KEY_ISLAND_TIME_PERIOD = "island_time_period"
        const val KEY_ISLAND_TIME_LEADING_ZERO = "island_time_leading_zero"
        const val ISLAND_TIME_FORMAT_SYSTEM = "system"
        const val ISLAND_TIME_FORMAT_12H = "12h"
        const val ISLAND_TIME_FORMAT_24H = "24h"
        const val ISLAND_TIME_PERIOD_NONE = "none"
        const val ISLAND_TIME_PERIOD_AM_PM = "am_pm"
        const val KEY_ISLAND_SHOW_BATTERY = "island_show_battery"
        const val KEY_ISLAND_SHOW_FLASHLIGHT = "island_show_flashlight"
        const val KEY_ISLAND_BATTERY_STYLE = "island_battery_style"
        const val KEY_ISLAND_DEVICES_BATTERY_STYLE = "island_devices_battery_style"
        const val ISLAND_BATTERY_STYLE_RING = "ring"
        const val ISLAND_BATTERY_STYLE_ICON = "icon"
        const val KEY_ISLAND_BATTERY_PERCENTAGE = "island_battery_percentage"
        const val KEY_ISLAND_LONG_PRESS_ACTION = "island_long_press_action"
        const val KEY_ISLAND_SLIDE_MODE = "island_slide_mode"
        const val KEY_ISLAND_SLIDE_TRACK = "island_slide_track"
        const val KEY_ISLAND_LIKE_WHILE_PLAYING = "island_like_while_playing"
        const val KEY_ISLAND_SLIDE_INVERT_DIRECTION = "island_slide_invert_direction"
        const val KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL = "island_battery_percentage_conditional"
        const val KEY_ISLAND_BATTERY_ICON_CONDITIONAL = "island_battery_icon_conditional"
        const val KEY_ISLAND_BATTERY_ONLY_LOW = "island_battery_only_low"
        const val KEY_ISLAND_BATTERY_PEEK_ALERTS = "island_battery_peek_alerts"
        const val KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW = "island_devices_battery_only_low"
        const val KEY_ISLAND_EXPANDED_SCALE = "island_expanded_scale"
        const val KEY_ISLAND_FONT_SCALE = "island_font_scale"
        const val KEY_ISLAND_HIDE_IN_OWNER_APP = "island_hide_in_owner_app"
        const val KEY_ISLAND_HIDE_ON_SHADE = "island_hide_on_shade"
        const val KEY_ISLAND_KEEP_ON_LANDSCAPE = "island_keep_on_landscape"
        const val KEY_ISLAND_KEEP_IN_FULLSCREEN = "island_keep_in_fullscreen"
        const val KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN = "island_disable_interactions_in_fullscreen"
        const val KEY_ISLAND_LANDSCAPE_TOP_SPACING = "island_landscape_top_spacing"
        const val KEY_ISLAND_BOND_EDGE = "island_bond_edge"
        const val KEY_ISLAND_BALANCE_COMPACT = "island_balance_compact"
        const val KEY_ISLAND_SECONDARY_BUBBLE = "island_secondary_bubble"
        const val KEY_ISLAND_MAX_ITEMS = "island_max_items"
        const val KEY_ISLAND_ALWAYS_GESTURES = "island_always_gestures"
        const val KEY_ISLAND_DISMISS_ON_OUTSIDE = "island_dismiss_on_outside"
        const val KEY_ISLAND_HIDE_LIVE_UPDATES = "island_hide_live_updates"
        const val KEY_ISLAND_HIDE_STATUS_BAR_INSETS = "island_hide_status_bar_insets"
        const val KEY_ISLAND_CAMERA_POSITION = "island_camera_position"
        const val KEY_ISLAND_ORIENTATION_PROFILES = "island_orientation_profiles"
        const val KEY_ISLAND_HIDE_PORTRAIT = "island_hide_portrait"
        const val KEY_ISLAND_HIDE_LANDSCAPE = "island_hide_landscape"
        const val KEY_ISLAND_PREVIEW_RING = "island_preview_ring"
        const val KEY_ISLAND_PREVIEW_STAGE = "island_preview_stage"
        const val ISLAND_PREVIEW_STAGE_AUTO = "auto"
        const val ISLAND_PREVIEW_STAGE_PEEK = "peek"
        const val ISLAND_PREVIEW_STAGE_EXPANDED = "expanded"
        const val KEY_ISLAND_SHOW_CALLS = "island_show_calls"
        const val KEY_ISLAND_CALL_SHOW_MODE = "island_call_show_mode"
        const val KEY_ISLAND_CALL_STICKY = "island_call_sticky"
        const val ISLAND_CALL_SHOW_EXPANDED = "expanded"
        const val ISLAND_CALL_SHOW_PEEK = "peek"
        const val ISLAND_CALL_SHOW_COMPACT = "compact"
        const val KEY_ISLAND_SHOW_TIMERS = "island_show_timers"
        const val KEY_ISLAND_PROVIDERS = "island_providers"
        const val KEY_ISLAND_PROVIDERS_HIDDEN = "island_providers_hidden"
        const val KEY_ISLAND_PROVIDERS_LAUNCHER_ONLY = "island_providers_launcher_only"
        const val KEY_ISLAND_PROVIDERS_HIDE_LOCKED = "island_providers_hide_locked"

        const val KEY_ISLAND_CALENDAR_EMOJIS = "island_calendar_emojis"
        const val KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER = "island_timers_show_screen_recorder"
        const val KEY_ISLAND_TIMERS_FILTER_APPS = "island_timers_filter_apps"
        const val KEY_ISLAND_TIMERS_SELECTED_APPS = "island_timers_selected_apps"
        const val KEY_ISLAND_SHOW_NETWORK = "island_show_network"
        const val KEY_ISLAND_SHOW_SIGNAL = "island_show_signal"
        const val KEY_ISLAND_NETWORK_ACTIVITY = "island_network_activity"
        const val KEY_ISLAND_SIGNAL_WIFI = "island_signal_wifi"
        const val KEY_ISLAND_PRIORITY_ORDER = "island_priority_order"
        const val KEY_ISLAND_SIGNAL_LOW_ONLY = "island_signal_low_only"
        const val KEY_ISLAND_SIGNAL_NETWORK_TYPES = "island_signal_network_modes"
        const val KEY_ISLAND_SIGNAL_SHOW_MODE = "island_signal_show_mode"
        const val KEY_ISLAND_SHOW_SOUND_MODE = "island_show_sound_mode"
        const val KEY_ISLAND_SOUND_MODE_KEEP_ICON = "island_sound_mode_keep_icon"
        const val KEY_ISLAND_SHOW_ALARM = "island_show_alarm"
        const val KEY_ISLAND_TIME_BATTERY_LAUNCHER_ONLY = "island_time_battery_launcher_only"
        const val KEY_ISLAND_CALENDAR_LAUNCHER_ONLY = "island_calendar_launcher_only"
        const val KEY_ISLAND_ALARM_LAUNCHER_ONLY = "island_alarm_launcher_only"
        const val KEY_ISLAND_ALARM_WINDOW_HOURS = "island_alarm_window_hours"
        const val KEY_ISLAND_ALARM_HIDE_TIME = "island_alarm_hide_time"
        const val KEY_ISLAND_SHOW_CAFFEINATE = "island_show_caffeinate"
        const val KEY_ISLAND_CAFFEINATE_SHOW_TIME = "island_caffeinate_show_time"
        const val KEY_ISLAND_SHOW_DEVICES = "island_show_devices"
        const val KEY_ISLAND_SHOW_NAVIGATION = "island_show_navigation"
        const val KEY_ISLAND_NAVIGATION_ON_NEW = "island_navigation_on_new"
        const val KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND = "island_navigation_peek_after_expand"
        const val ISLAND_NAVIGATION_ON_NEW_EXPAND = "expand"
        const val ISLAND_NAVIGATION_ON_NEW_PEEK = "peek"
        const val ISLAND_NAVIGATION_ON_NEW_OFF = "off"
        const val KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE = "island_navigation_expand_distance"
        const val KEY_ISLAND_SHOW_HOTSPOT = "island_show_hotspot"
        const val KEY_ISLAND_BRIEF_ENABLED = "island_brief_enabled"
        const val KEY_ISLAND_BRIEF_SHOW_ALARM = "island_brief_show_alarm"
        const val KEY_ISLAND_BRIEF_TWO_LINE_HEADER = "island_brief_two_line_header"
        const val KEY_ISLAND_DEVICES_BATTERY_ORDER = "island_devices_battery_order"
        const val ISLAND_CAMERA_POSITION_LEFT = "left"
        const val ISLAND_CAMERA_POSITION_CENTER = "center"
        const val ISLAND_CAMERA_POSITION_RIGHT = "right"
        const val KEY_ISLAND_LINE_STAGE_ENABLED = "island_line_stage_enabled"
        const val KEY_ISLAND_PEEK_DURATION_MS = "island_peek_duration_ms"
        const val KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE = "island_media_peek_song_change"
        const val KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED = "island_media_keep_when_paused"
        const val KEY_ISLAND_MEDIA_SHOW_PREVIOUS = "island_media_show_previous"
        const val KEY_ISLAND_MEDIA_SHOW_LIKE = "island_media_show_like"
        const val KEY_ISLAND_NOTIF_CONDENSED_INFO = "island_notif_condensed_info"
        const val KEY_ISLAND_NOTIF_PEEK_STYLE = "island_notif_peek_style"
        const val ISLAND_NOTIF_PEEK_STYLE_FULL = "full"
        const val ISLAND_NOTIF_PEEK_STYLE_COMPACT = "compact"
        const val ISLAND_NOTIF_PEEK_STYLE_MINIMAL = "minimal"
        const val KEY_ISLAND_NOTIF_KEEP_PROGRESS = "island_notif_keep_progress"
        const val KEY_ISLAND_NOTIF_QUEUE = "island_notif_queue"
        const val KEY_ISLAND_NOTIF_QUEUE_STYLE = "island_notif_queue_style"
        const val ISLAND_NOTIF_QUEUE_STYLE_STACK = "stack"
        const val ISLAND_NOTIF_QUEUE_STYLE_OVERLAP = "overlap"
        const val KEY_ISLAND_NOTIF_QUEUE_INLINE = "island_notif_queue_inline"
        const val KEY_ISLAND_NOTIF_SKIP_SILENT = "island_notif_skip_silent"
        const val KEY_ISLAND_NOTIF_FOLLOW_DND = "island_notif_follow_dnd"
        const val KEY_ISLAND_NOTIF_FILTER_APPS = "island_notif_filter_apps"
        const val KEY_ISLAND_NOTIF_FILTER_APPS_LIST = "island_notif_filter_apps_list"
        const val KEY_ISLAND_SHOW_NOTIFICATIONS = "island_show_notifications"
        const val KEY_ISLAND_NOTIF_HUB = "island_notif_hub"
        const val KEY_ISLAND_NOTIF_HUB_ICONS = "island_notif_hub_icons"
        const val KEY_ISLAND_NOTIF_HUB_ORDER = "island_notif_hub_order"
        const val KEY_ISLAND_NOTIF_HUB_SILENT = "island_notif_hub_silent"
        const val KEY_ISLAND_NOTIF_HUB_GROUP = "island_notif_hub_group"
        const val ISLAND_NOTIF_HUB_ORDER_DYNAMIC = "dynamic"
        const val ISLAND_NOTIF_HUB_ORDER_PRIORITY = "priority"
        const val ISLAND_NOTIF_HUB_ORDER_RECENT = "recent"
        const val KEY_ISLAND_NOTIF_TAP_TO_OPEN = "island_notif_tap_to_open"
        const val KEY_ISLAND_OTP_ENABLED = "island_otp_enabled"
        const val KEY_ISLAND_OTP_TAP_TO_COPY = "island_otp_tap_to_copy"
        const val KEY_ISLAND_OTP_DISMISS_AFTER_COPY = "island_otp_dismiss_after_copy"
        const val KEY_ISLAND_OTP_BYPASS_FILTER = "island_otp_bypass_filter"
        const val KEY_ISLAND_OTP_TIMEOUT_MS = "island_otp_timeout_ms"
        const val KEY_ISLAND_BORDER_OUTLINE_ENABLED = "island_border_outline_enabled"
        const val KEY_ISLAND_BORDER_OUTLINE_COLOR = "island_border_outline_color"
        const val KEY_ISLAND_BORDER_OUTLINE_DYNAMIC = "island_border_outline_dynamic"
        const val KEY_ISLAND_BORDER_OUTLINE_THICKNESS = "island_border_outline_thickness"
        const val KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED = "island_border_outline_hide_expanded"
        const val KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION = "island_pulse_shadow_on_notification"
        const val KEY_ISLAND_PULSE_SHADOW_SIZE = "island_pulse_shadow_size"
        const val KEY_ISLAND_PULSE_SHADOW_Y_SHIFT = "island_pulse_shadow_y_shift"
        const val KEY_ISLAND_PULSE_SHADOW_SPREAD = "island_pulse_shadow_spread"
        const val KEY_ISLAND_PULSE_SHADOW_DURATION_MS = "island_pulse_shadow_duration_ms"
        const val ISLAND_BORDER_OUTLINE_DEFAULT_COLOR = "#202020"

        // Status Glance
        const val KEY_STATUS_GLANCE_ENABLED = "status_glance_enabled"
        const val KEY_STATUS_GLANCE_USE_AUTO_DETECT = "status_glance_use_auto_detect"
        const val KEY_STATUS_GLANCE_OFFSET_X = "status_glance_offset_x"
        const val KEY_STATUS_GLANCE_OFFSET_Y = "status_glance_offset_y"
        const val KEY_STATUS_GLANCE_ORIENTATION_PROFILES = "status_glance_orientation_profiles"
        const val KEY_STATUS_GLANCE_HIDE_PORTRAIT = "status_glance_hide_portrait"
        const val KEY_STATUS_GLANCE_HIDE_LANDSCAPE = "status_glance_hide_landscape"
        const val KEY_STATUS_GLANCE_MAX_WIDTH = "status_glance_max_width"
        const val KEY_STATUS_GLANCE_FONT_SIZE = "status_glance_font_size"
        const val KEY_STATUS_GLANCE_SHOW_FLASHLIGHT = "status_glance_show_flashlight"
        const val KEY_STATUS_GLANCE_SHOW_CALENDAR = "status_glance_show_calendar"
        const val KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME = "status_glance_calendar_timeframe"
        const val KEY_ISLAND_CALENDAR_PRIORITY_MINUTES = "island_calendar_priority_minutes"
        const val KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS = "status_glance_calendar_selected_calendars"
        const val KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY = "status_glance_calendar_show_all_day"
        const val KEY_STATUS_GLANCE_SHOW_MEDIA = "status_glance_show_media"
        const val KEY_STATUS_GLANCE_SHOW_TIME = "status_glance_show_time"
        const val KEY_STATUS_GLANCE_BACKGROUND_PILL = "status_glance_background_pill"
        const val KEY_STATUS_GLANCE_ALBUM_ART_COLORS = "status_glance_album_art_colors"
        const val KEY_STATUS_GLANCE_SHOW_BATTERY = "status_glance_show_battery"
        const val KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE = "status_glance_battery_display_mode"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_LOW = "status_glance_battery_show_low"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING = "status_glance_battery_show_charging"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_FULL = "status_glance_battery_show_full"
        const val KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE = "status_glance_battery_show_otherwise"
        const val KEY_STATUS_GLANCE_BATTERY_ICON_SIZE = "status_glance_battery_icon_size"
        const val KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN = "status_glance_hide_when_fullscreen"
        const val KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS = "status_glance_hide_in_quick_settings"
        const val KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED = "status_glance_hide_when_locked"
        const val KEY_STATUS_GLANCE_BOLD_TEXT = "status_glance_bold_text"
        const val KEY_STATUS_GLANCE_TAP_ACTION = "status_glance_tap_action"
        const val KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION = "status_glance_double_tap_action"
        const val KEY_STATUS_GLANCE_LONG_PRESS_ACTION = "status_glance_long_press_action"

        // Live Wallpaper
        const val LIVE_WALLPAPER_PREFS_NAME = "live_wallpaper_prefs"
        const val KEY_LIVE_WALLPAPER_SELECTED_VIDEO = "selected_video"
        const val KEY_LIVE_WALLPAPER_PLAYBACK_TRIGGER = "playback_trigger"
        const val KEY_LIVE_WALLPAPER_CUSTOM_VIDEOS = "custom_videos"
        const val LIVE_WALLPAPER_DEFAULT_VIDEO = "my_video"
        const val LIVE_WALLPAPER_TRIGGER_UNLOCK = "unlock"
        const val LIVE_WALLPAPER_TRIGGER_SCREEN_ON = "screen_on"

        const val KEY_SHUT_UP_SELECTED_APPS = "shut_up_selected_apps"
        const val KEY_SHUT_UP_ORIGINAL_SETTINGS = "shut_up_original_settings"
        const val KEY_SHUT_UP_ATTEMPT_SHIZUKU_RESTART = "shut_up_attempt_shizuku_restart"
        const val KEY_SHUT_UP_RESTORE_DELAY = "shut_up_restore_delay"
        const val KEY_SHUT_UP_RESTORE_MODE = "shut_up_restore_mode"
        const val KEY_SHUT_UP_KEYBOARD = "shut_up_keyboard"
        const val KEY_SHIZUKU_AUTH_TOKEN = "shizuku_auth_token"
        const val KEY_DISABLE_ROTATION_SUGGESTION = "disable_rotation_suggestion"
        const val KEY_ALLOW_OVERLAYS_IN_SETTINGS = "allow_overlays_in_settings"
        const val KEY_PREFER_GPU_COMPOSING = "prefer_gpu_composing"
        const val KEY_TRANSPARENT_NAVIGATION_BAR = "transparent_navigation_bar"
        const val KEY_SPLIT_SCREEN_GESTURE = "split_screen_gesture"
        const val KEY_PIXEL_SEARCHBAR = "pixel_searchbar"
        const val KEY_PIXEL_SEARCHBAR_TYPE = "pixel_searchbar_type"
        const val KEY_PIXEL_SEARCHBAR_MIGRATED_V1 = "pixel_searchbar_migrated_v1"
        const val KEY_PIXEL_SEARCHBAR_DATE_FORMAT = "pixel_searchbar_date_format"
        const val KEY_PIXEL_SEARCHBAR_BACKGROUND_PILL = "pixel_searchbar_background_pill"
        const val KEY_PIXEL_SEARCHBAR_WIDGET_ID = "pixel_searchbar_widget_id"
        const val KEY_PIXEL_SEARCHBAR_WIDGET_PROVIDER = "pixel_searchbar_widget_provider"
        const val KEY_PIXEL_SEARCHBAR_SCRAPED_LINE1 = "pixel_searchbar_scraped_line1"
        const val KEY_PIXEL_SEARCHBAR_SCRAPED_LINE2 = "pixel_searchbar_scraped_line2"
        const val KEY_PIXEL_SEARCHBAR_WIDGET_PADDING_H = "pixel_searchbar_widget_padding_h"
        const val KEY_PIXEL_SEARCHBAR_WIDGET_PADDING_V = "pixel_searchbar_widget_padding_v"
        const val KEY_PIXEL_SEARCHBAR_TAP_ACTION_ENABLED = "pixel_searchbar_tap_action_enabled"
        const val KEY_PIXEL_SEARCHBAR_WIDGET_REVISION = "pixel_searchbar_widget_revision"
        const val KEY_PIXEL_SEARCHBAR_MUSIC_TITLE = "pixel_searchbar_music_title"
        const val KEY_PIXEL_SEARCHBAR_MUSIC_ARTIST = "pixel_searchbar_music_artist"
        const val KEY_PIXEL_SEARCHBAR_MUSIC_PACKAGE = "pixel_searchbar_music_package"

        const val KEY_LOCK_SCREEN_CLOCK_WEIGHT = "lock_screen_clock_weight"
        const val KEY_LOCK_SCREEN_CLOCK_WIDTH = "lock_screen_clock_width"
        const val KEY_LOCK_SCREEN_CLOCK_GRADE = "lock_screen_clock_grade"
        const val KEY_LOCK_SCREEN_CLOCK_ROUNDNESS = "lock_screen_clock_roundness"
        const val KEY_LOCK_SCREEN_CLOCK_COLOR_TONE = "lock_screen_clock_color_tone"
        const val KEY_LOCK_SCREEN_CLOCK_SELECTED_COLOR_ID = "lock_screen_clock_selected_color_id"
        const val KEY_LOCK_SCREEN_CLOCK_SEED_COLOR = "lock_screen_clock_seed_color"
        const val KEY_RECENT_SEARCHES = "recent_searches"
        const val KEY_KEEP_PREFS = "keep_prefs"

        const val KEY_LOCKDOWN_MODE = "lockdown_mode"
        const val KEY_BUBBLE_WEB_FULLSCREEN = "bubble_web_fullscreen"
        const val KEY_UNSPLASH_ACCESS_KEY = "unsplash_access_key"
        const val KEY_EXTERNAL_CONTROL_TOKEN = "external_control_token"
    }

    fun getExternalControlToken(): String? = prefs.getString(KEY_EXTERNAL_CONTROL_TOKEN, null)?.takeIf { it.isNotBlank() }

    fun setExternalControlToken(token: String) = putString(KEY_EXTERNAL_CONTROL_TOKEN, token.trim())

    fun getUnsplashAccessKey(): String? = prefs.getString(KEY_UNSPLASH_ACCESS_KEY, null)

    fun setUnsplashAccessKey(key: String?) {
        if (key.isNullOrBlank()) {
            prefs.edit().remove(KEY_UNSPLASH_ACCESS_KEY).apply()
        } else {
            prefs.edit().putString(KEY_UNSPLASH_ACCESS_KEY, key.trim()).apply()
        }
    }

    // Observe changes
    fun observeKeyChanges(): Flow<String?> =
        callbackFlow {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    trySend(key)
                }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    val isPitchBlackThemeEnabled: Flow<Boolean> =
        callbackFlow {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == KEY_PITCH_BLACK_THEME_ENABLED) {
                        trySend(getBoolean(KEY_PITCH_BLACK_THEME_ENABLED))
                    }
                }
            trySend(getBoolean(KEY_PITCH_BLACK_THEME_ENABLED))
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    // Specific Getters with logic from ViewModel

    /**
     * Executes the get haptic feedback type operation.
     * @return The resulting HapticFeedbackType data.
     */
    fun getHapticFeedbackType(): HapticFeedbackType {
        val typeName = prefs.getString(KEY_HAPTIC_FEEDBACK_TYPE, HapticFeedbackType.SUBTLE.name)
        return try {
            HapticFeedbackType.valueOf(typeName ?: HapticFeedbackType.SUBTLE.name)
        } catch (e: Exception) {
            HapticFeedbackType.SUBTLE
        }
    }

    /**
     * Executes the get diy tab operation.
     * @return The resulting com data.
     */
    fun getDIYTab(): com.jawzf.pixeltoolkit.domain.DIYTabs {
        val tabName =
            prefs.getString(
                KEY_DEFAULT_TAB,
                com.jawzf.pixeltoolkit.domain.DIYTabs.TOOLKIT.name,
            )
        return try {
            com.jawzf.pixeltoolkit.domain.DIYTabs.valueOf(
                tabName ?: com.jawzf.pixeltoolkit.domain.DIYTabs.TOOLKIT.name,
            )
        } catch (e: Exception) {
            com.jawzf.pixeltoolkit.domain.DIYTabs.TOOLKIT
        }
    }

    /**
     * Executes the save diy tab operation.
     *
     * @param tab [com.jawzf.pixeltoolkit.domain.DIYTabs] Target tab.
     */
    fun saveDIYTab(tab: com.jawzf.pixeltoolkit.domain.DIYTabs) {
        putString(KEY_DEFAULT_TAB, tab.name)
    }

    fun getAppIcon(): AppIcon {
        val key = prefs.getString(KEY_APP_ICON, AppIcon.DEFAULT.key)
        return AppIcon.fromKey(key)
    }

    fun setAppIcon(appIcon: AppIcon) {
        putString(KEY_APP_ICON, appIcon.key)
    }

    // Feature specific App selections

    /**
     * Executes the load dynamic night light selected apps operation.
     */
    fun loadDynamicNightLightSelectedApps() = loadAppSelection(KEY_DYNAMIC_NIGHT_LIGHT_SELECTED_APPS)

    /**
     * Executes the save dynamic night light selected apps operation.
     *
     * @param apps [List<AppSelection>] Target apps.
     */
    fun saveDynamicNightLightSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_DYNAMIC_NIGHT_LIGHT_SELECTED_APPS, apps)

    /**
     * Executes the update dynamic night light app selection operation.
     *
     * @param packageName [String] Target package name.
     * @param enabled [Boolean] Target enabled.
     */
    fun updateDynamicNightLightAppSelection(
        packageName: String,
        enabled: Boolean,
    ) = updateAppSelection(KEY_DYNAMIC_NIGHT_LIGHT_SELECTED_APPS, packageName, enabled)

    /**
     * Executes the load notification glance selected apps operation.
     */
    fun loadNotificationGlanceSelectedApps() = loadAppSelection(KEY_NOTIFICATION_GLANCE_SELECTED_APPS)

    /**
     * Executes the save notification glance selected apps operation.
     *
     * @param apps [List<AppSelection>] Target apps.
     */
    fun saveNotificationGlanceSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_NOTIFICATION_GLANCE_SELECTED_APPS, apps)

    /**
     * Executes the update notification glance app selection operation.
     *
     * @param packageName [String] Target package name.
     * @param enabled [Boolean] Target enabled.
     */
    fun updateNotificationGlanceAppSelection(
        packageName: String,
        enabled: Boolean,
    ) = updateAppSelection(KEY_NOTIFICATION_GLANCE_SELECTED_APPS, packageName, enabled)

    fun loadAodWallpaperMediaExcludedApps() = loadAppSelection(KEY_AOD_WALLPAPER_MEDIA_EXCLUDED_APPS)

    fun saveAodWallpaperMediaExcludedApps(apps: List<AppSelection>) = saveAppSelection(KEY_AOD_WALLPAPER_MEDIA_EXCLUDED_APPS, apps)

    fun updateAodWallpaperMediaExcludedAppSelection(
        packageName: String,
        enabled: Boolean,
    ) = updateAppSelection(KEY_AOD_WALLPAPER_MEDIA_EXCLUDED_APPS, packageName, enabled)

    /**
     * Executes the load shut up configs operation.
     * @return The resulting List<com data.
     */
    fun loadShutUpConfigs(): List<com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig> {
        val json = prefs.getString(KEY_SHUT_UP_SELECTED_APPS, null)
        return if (json != null) {
            try {
                gson
                    .fromJson(
                        json,
                        Array<com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig>::class.java,
                    ).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Executes the save shut up configs operation.
     *
     * @param configs [List<com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig>] Target configs.
     */
    fun saveShutUpConfigs(configs: List<com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig>) {
        val json = gson.toJson(configs)
        putString(KEY_SHUT_UP_SELECTED_APPS, json)
    }

    /**
     * Executes the update shut up config operation.
     *
     * @param config [com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig] Target config.
     */
    fun updateShutUpConfig(config: com.jawzf.pixeltoolkit.domain.model.ShutUpAppConfig) {
        val current = loadShutUpConfigs().toMutableList()
        val index = current.indexOfFirst { it.packageName == config.packageName }
        if (index != -1) {
            current[index] = config
        } else {
            current.add(config)
        }
        saveShutUpConfigs(current)
    }

    /**
     * Executes the save shut up original settings operation.
     *
     * @param settings [Map<String] Target settings.
     * @param String> Target string.
     */
    fun saveShutUpOriginalSettings(settings: Map<String, String>) {
        val json = gson.toJson(settings)
        putString(KEY_SHUT_UP_ORIGINAL_SETTINGS, json)
    }

    /**
     * Executes the get shut up original settings operation.
     * @return The resulting Map<String, String> data.
     */
    fun getShutUpOriginalSettings(): Map<String, String> {
        val json = prefs.getString(KEY_SHUT_UP_ORIGINAL_SETTINGS, null) ?: return emptyMap()
        return try {
            @Suppress("UNCHECKED_CAST")
            gson.fromJson(json, Map::class.java) as Map<String, String>
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun updateAppSelection(
        key: String,
        packageName: String,
        enabled: Boolean,
    ) {
        val current = loadAppSelection(key).toMutableList()
        val index = current.indexOfFirst { it.packageName == packageName }
        if (index != -1) {
            current[index] = current[index].copy(isEnabled = enabled)
        } else {
            current.add(AppSelection(packageName, enabled))
        }
        saveAppSelection(key, current)
    }

    fun resetKeysWithPrefixes(prefixes: List<String>) {
        val protected =
            setOf(
                AppsSettings.KEY_GITHUB_ACCESS_TOKEN,
                KEY_SHIZUKU_AUTH_TOKEN,
                NotificationSettings.KEY_SNOOZE_DISCOVERED_CHANNELS,
                PowerSettings.KEY_MAPS_DISCOVERED_CHANNELS,
                KEY_SHUT_UP_ORIGINAL_SETTINGS,
            )
        val editor = prefs.edit()
        prefs.all.keys
            .filter { key -> key !in protected && prefixes.any { key.startsWith(it) } }
            .forEach { editor.remove(it) }
        editor.apply()
    }

    /**
     * Executes the get pinned features operation.
     * @return The resulting List<String> data.
     */
    fun getPinnedFeatures(): List<String> {
        val json = prefs.getString(KEY_PINNED_FEATURES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Executes the save pinned features operation.
     *
     * @param features [List<String>] Target features.
     */
    fun savePinnedFeatures(features: List<String>) {
        val json = gson.toJson(features)
        putString(KEY_PINNED_FEATURES, json)
    }

    /**
     * Executes the get pinned qs tiles operation.
     * @return The resulting List<String> data.
     */
    fun getPinnedQsTiles(): List<String> {
        val json = prefs.getString(KEY_PINNED_QS_TILES, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Executes the save pinned qs tiles operation.
     *
     * @param tiles [List<String>] Target tiles.
     */
    fun savePinnedQsTiles(tiles: List<String>) {
        val json = gson.toJson(tiles)
        putString(KEY_PINNED_QS_TILES, json)
    }

    /**
     * Executes the get recent searches operation.
     * @return The resulting List<com data.
     */
    fun getRecentSearches(): List<com.jawzf.pixeltoolkit.domain.model.SearchableItem> {
        val json = prefs.getString(KEY_RECENT_SEARCHES, null) ?: return emptyList()
        return try {
            val list = gson.fromJson(
                json,
                Array<com.jawzf.pixeltoolkit.domain.model.SearchableItem>::class.java,
            )?.toList() ?: emptyList()

            // Validate that every item has valid non-empty fields
            list.filter {
                @Suppress("SENSELESS_COMPARISON")
                it != null && !it.title.isNullOrBlank() && !it.featureKey.isNullOrBlank()
            }
        } catch (e: Throwable) {
            android.util.Log.e("SettingsRepository", "Failed to parse recent searches, clearing history: ${e.message}")
            remove(KEY_RECENT_SEARCHES)
            emptyList()
        }
    }

    /**
     * Executes the save recent searches operation.
     *
     * @param items [List<com.jawzf.pixeltoolkit.domain.model.SearchableItem>] Target items.
     */
    fun saveRecentSearches(items: List<com.jawzf.pixeltoolkit.domain.model.SearchableItem>) {
        val json = gson.toJson(items)
        putString(KEY_RECENT_SEARCHES, json)
    }

    /**
     * Executes the is shut up attempt shizuku restart enabled operation.
     * @return The resulting Boolean data.
     */
    fun isShutUpAttemptShizukuRestartEnabled(): Boolean = getBoolean(KEY_SHUT_UP_ATTEMPT_SHIZUKU_RESTART, true)

    fun isShakeForHelpEnabled(): Boolean = getBoolean(KEY_SHAKE_FOR_HELP, true)

    fun setShakeForHelpEnabled(enabled: Boolean) = putBoolean(KEY_SHAKE_FOR_HELP, enabled)

    /**
     * Executes the set shut up attempt shizuku restart enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setShutUpAttemptShizukuRestartEnabled(enabled: Boolean) = putBoolean(KEY_SHUT_UP_ATTEMPT_SHIZUKU_RESTART, enabled)

    /**
     * Executes the get shut up restore delay operation.
     * @return The resulting Int data.
     */
    fun getShutUpRestoreDelay(): Int = getInt(KEY_SHUT_UP_RESTORE_DELAY, 10)

    /**
     * Executes the set shut up restore delay operation.
     *
     * @param delaySeconds [Int] Target delay seconds.
     */
    fun setShutUpRestoreDelay(delaySeconds: Int) = putInt(KEY_SHUT_UP_RESTORE_DELAY, delaySeconds)

    /**
     * Executes the get shut up restore mode operation.
     * @return The resulting String data.
     */
    fun getShutUpKeyboard(): String = prefs.getString(KEY_SHUT_UP_KEYBOARD, "") ?: ""

    fun setShutUpKeyboard(ime: String) = putString(KEY_SHUT_UP_KEYBOARD, ime)

    fun getShutUpRestoreMode(): String = prefs.getString(KEY_SHUT_UP_RESTORE_MODE, "Auto") ?: "Auto"

    /**
     * Executes the set shut up restore mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setShutUpRestoreMode(mode: String) = putString(KEY_SHUT_UP_RESTORE_MODE, mode)

    /**
     * Executes the get shizuku auth token operation.
     * @return The resulting String data.
     */
    fun getShizukuAuthToken(): String = prefs.getString(KEY_SHIZUKU_AUTH_TOKEN, "") ?: ""

    /**
     * Executes the set shizuku auth token operation.
     *
     * @param token [String] Target token.
     */
    fun setShizukuAuthToken(token: String) = putString(KEY_SHIZUKU_AUTH_TOKEN, token)

    /**
     * Executes the get pixel searchbar type operation.
     * @return The resulting String data.
     */
    fun getPixelSearchbarType(): String = prefs.getString(KEY_PIXEL_SEARCHBAR_TYPE, "searchbar") ?: "searchbar"

    /**
     * Executes the set pixel searchbar type operation.
     *
     * @param type [String] Target type.
     */
    fun setPixelSearchbarType(type: String) = putString(KEY_PIXEL_SEARCHBAR_TYPE, type)

    /**
     * Executes the get pixel searchbar date format operation.
     * @return The resulting String data.
     */
    fun getPixelSearchbarDateFormat(): String = prefs.getString(KEY_PIXEL_SEARCHBAR_DATE_FORMAT, "EEEE, MMMM d") ?: "EEEE, MMMM d"

    /**
     * Executes the set pixel searchbar date format operation.
     *
     * @param format [String] Target format.
     */
    fun setPixelSearchbarDateFormat(format: String) = putString(KEY_PIXEL_SEARCHBAR_DATE_FORMAT, format)

    /**
     * Executes the get pixel searchbar background pill operation.
     * @return The resulting Boolean data.
     */
    fun getPixelSearchbarBackgroundPill(): Boolean = prefs.getBoolean(KEY_PIXEL_SEARCHBAR_BACKGROUND_PILL, false)

    /**
     * Executes the set pixel searchbar background pill operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setPixelSearchbarBackgroundPill(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCHBAR_BACKGROUND_PILL, enabled)

    /**
     * Executes the get pixel searchbar widget id operation.
     * @return The resulting Int data.
     */
    fun getPixelSearchbarWidgetId(): Int =
        prefs.getInt(
            KEY_PIXEL_SEARCHBAR_WIDGET_ID,
            android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID,
        )

    /**
     * Executes the set pixel searchbar widget id operation.
     *
     * @param id [Int] Target id.
     */
    fun setPixelSearchbarWidgetId(id: Int) = prefs.edit().putInt(KEY_PIXEL_SEARCHBAR_WIDGET_ID, id).apply()

    /**
     * Executes the get pixel searchbar widget provider operation.
     * @return The resulting String? data.
     */
    fun getPixelSearchbarWidgetProvider(): String? = prefs.getString(KEY_PIXEL_SEARCHBAR_WIDGET_PROVIDER, null)

    /**
     * Executes the set pixel searchbar widget provider operation.
     *
     * @param provider [String?] Target provider.
     */
    fun setPixelSearchbarWidgetProvider(provider: String?) =
        if (provider == null) {
            prefs.edit().remove(KEY_PIXEL_SEARCHBAR_WIDGET_PROVIDER).apply()
        } else {
            putString(KEY_PIXEL_SEARCHBAR_WIDGET_PROVIDER, provider)
        }

    /**
     * Executes the get pixel searchbar scraped line1 operation.
     * @return The resulting String data.
     */
    fun getPixelSearchbarScrapedLine1(): String = prefs.getString(KEY_PIXEL_SEARCHBAR_SCRAPED_LINE1, "") ?: ""

    /**
     * Executes the set pixel searchbar scraped line1 operation.
     *
     * @param text [String] Target text.
     */
    fun setPixelSearchbarScrapedLine1(text: String) = putString(KEY_PIXEL_SEARCHBAR_SCRAPED_LINE1, text)

    /**
     * Executes the get pixel searchbar scraped line2 operation.
     * @return The resulting String data.
     */
    fun getPixelSearchbarScrapedLine2(): String = prefs.getString(KEY_PIXEL_SEARCHBAR_SCRAPED_LINE2, "") ?: ""

    /**
     * Executes the set pixel searchbar scraped line2 operation.
     *
     * @param text [String] Target text.
     */
    fun setPixelSearchbarScrapedLine2(text: String) = putString(KEY_PIXEL_SEARCHBAR_SCRAPED_LINE2, text)

    /**
     * Executes the get pixel searchbar widget padding h operation.
     * @return The resulting Int data.
     */
    fun getPixelSearchbarWidgetPaddingH(): Int = prefs.getInt(KEY_PIXEL_SEARCHBAR_WIDGET_PADDING_H, 0)

    /**
     * Executes the set pixel searchbar widget padding h operation.
     *
     * @param value [Int] Target value.
     */
    fun setPixelSearchbarWidgetPaddingH(value: Int) = prefs.edit().putInt(KEY_PIXEL_SEARCHBAR_WIDGET_PADDING_H, value).apply()

    /**
     * Executes the get pixel searchbar widget padding v operation.
     * @return The resulting Int data.
     */
    fun getPixelSearchbarWidgetPaddingV(): Int = prefs.getInt(KEY_PIXEL_SEARCHBAR_WIDGET_PADDING_V, 0)

    /**
     * Executes the set pixel searchbar widget padding v operation.
     *
     * @param value [Int] Target value.
     */
    fun setPixelSearchbarWidgetPaddingV(value: Int) = prefs.edit().putInt(KEY_PIXEL_SEARCHBAR_WIDGET_PADDING_V, value).apply()

    /**
     * Executes the get pixel searchbar tap action enabled operation.
     * @return The resulting Boolean data.
     */
    fun getPixelSearchbarTapActionEnabled(): Boolean = prefs.getBoolean(KEY_PIXEL_SEARCHBAR_TAP_ACTION_ENABLED, true)

    /**
     * Executes the set pixel searchbar tap action enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setPixelSearchbarTapActionEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCHBAR_TAP_ACTION_ENABLED, enabled)

    /**
     * Executes the get pixel searchbar widget revision operation.
     * @return The resulting Int data.
     */
    fun getPixelSearchbarWidgetRevision(): Int = prefs.getInt(KEY_PIXEL_SEARCHBAR_WIDGET_REVISION, 0)

    /**
     * Executes the increment pixel searchbar widget revision operation.
     */
    fun incrementPixelSearchbarWidgetRevision() {
        val current = getPixelSearchbarWidgetRevision()
        prefs.edit().putInt(KEY_PIXEL_SEARCHBAR_WIDGET_REVISION, current + 1).apply()
        com.jawzf.pixeltoolkit.services.widgets.SearchbarRenderState.bump()
    }

    /**
     * Executes the get pixel searchbar music title operation.
     * @return The resulting String data.
     */
    fun getPixelSearchbarMusicTitle(): String = prefs.getString(KEY_PIXEL_SEARCHBAR_MUSIC_TITLE, "") ?: ""

    /**
     * Executes the set pixel searchbar music title operation.
     *
     * @param value [String] Target value.
     */
    fun setPixelSearchbarMusicTitle(value: String) = putString(KEY_PIXEL_SEARCHBAR_MUSIC_TITLE, value)

    /**
     * Executes the get pixel searchbar music artist operation.
     * @return The resulting String data.
     */
    fun getPixelSearchbarMusicArtist(): String = prefs.getString(KEY_PIXEL_SEARCHBAR_MUSIC_ARTIST, "") ?: ""

    /**
     * Executes the set pixel searchbar music artist operation.
     *
     * @param value [String] Target value.
     */
    fun setPixelSearchbarMusicArtist(value: String) = putString(KEY_PIXEL_SEARCHBAR_MUSIC_ARTIST, value)

    /**
     * Executes the get pixel searchbar music package operation.
     * @return The resulting String data.
     */
    fun getPixelSearchbarMusicPackage(): String = prefs.getString(KEY_PIXEL_SEARCHBAR_MUSIC_PACKAGE, "") ?: ""

    /**
     * Executes the set pixel searchbar music package operation.
     *
     * @param value [String] Target value.
     */
    fun setPixelSearchbarMusicPackage(value: String) = putString(KEY_PIXEL_SEARCHBAR_MUSIC_PACKAGE, value)

    // observe token changes
    val gitHubToken: Flow<String?> =
        callbackFlow {
            val listener =
                SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                    if (key == AppsSettings.KEY_GITHUB_ACCESS_TOKEN) {
                        trySend(getString(AppsSettings.KEY_GITHUB_ACCESS_TOKEN))
                    }
                }
            trySend(getString(AppsSettings.KEY_GITHUB_ACCESS_TOKEN))
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
        }

    /**
     * Executes the is enable unsupported features operation.
     * @return The resulting Boolean data.
     */
    fun isEnableUnsupportedFeatures(): Boolean = getBoolean(KEY_ENABLE_UNSUPPORTED_FEATURES, false)

    /**
     * Executes the set enable unsupported features operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setEnableUnsupportedFeatures(enabled: Boolean) {
        putBoolean(KEY_ENABLE_UNSUPPORTED_FEATURES, enabled)
    }

    fun getIslandHideIcons(): Set<String> =
        getString(KEY_ISLAND_HIDE_ICONS, "")?.split(",")?.filter { it.isNotBlank() }?.toSet().orEmpty()

    fun setIslandHideIcons(icons: Set<String>) = putString(KEY_ISLAND_HIDE_ICONS, icons.joinToString(","))

    fun isShowLegacyFeatures(): Boolean = getBoolean(KEY_SHOW_LEGACY_FEATURES, true)

    fun setShowLegacyFeatures(enabled: Boolean) = putBoolean(KEY_SHOW_LEGACY_FEATURES, enabled)

    // Live Wallpaper Helpers
    private val liveWallpaperPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(LIVE_WALLPAPER_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Executes the get live wallpaper selected video operation.
     * @return The resulting String data.
     */
    fun getLiveWallpaperSelectedVideo(): String =
        liveWallpaperPrefs.getString(
            KEY_LIVE_WALLPAPER_SELECTED_VIDEO,
            LIVE_WALLPAPER_DEFAULT_VIDEO,
        )
            ?: LIVE_WALLPAPER_DEFAULT_VIDEO

    /**
     * Executes the save live wallpaper selected video operation.
     *
     * @param video [String] Target video.
     */
    fun saveLiveWallpaperSelectedVideo(video: String) =
        liveWallpaperPrefs.edit().putString(KEY_LIVE_WALLPAPER_SELECTED_VIDEO, video).apply()

    /**
     * Executes the get live wallpaper playback trigger operation.
     * @return The resulting String data.
     */
    fun getLiveWallpaperPlaybackTrigger(): String =
        liveWallpaperPrefs.getString(
            KEY_LIVE_WALLPAPER_PLAYBACK_TRIGGER,
            LIVE_WALLPAPER_TRIGGER_UNLOCK,
        )
            ?: LIVE_WALLPAPER_TRIGGER_UNLOCK

    /**
     * Executes the save live wallpaper playback trigger operation.
     *
     * @param trigger [String] Target trigger.
     */
    fun saveLiveWallpaperPlaybackTrigger(trigger: String) =
        liveWallpaperPrefs.edit().putString(KEY_LIVE_WALLPAPER_PLAYBACK_TRIGGER, trigger).apply()

    /**
     * Executes the get live wallpaper custom videos operation.
     * @return The resulting List<String> data.
     */
    fun getLiveWallpaperCustomVideos(): List<String> {
        val stored = liveWallpaperPrefs.getString(KEY_LIVE_WALLPAPER_CUSTOM_VIDEOS, "") ?: ""
        val delimiter = if (!stored.contains("\n") && stored.contains(",")) "," else "\n"
        return stored
            .split(delimiter)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    /**
     * Executes the save live wallpaper custom videos operation.
     *
     * @param videos [List<String>] Target videos.
     */
    fun saveLiveWallpaperCustomVideos(videos: List<String>) =
        liveWallpaperPrefs
            .edit()
            .putString(
                KEY_LIVE_WALLPAPER_CUSTOM_VIDEOS,
                videos.filter { it.isNotEmpty() }.distinct().joinToString("\n"),
            ).apply()

    /**
     * Executes the add live wallpaper custom video operation.
     *
     * @param uri [String] Target uri.
     */
    fun addLiveWallpaperCustomVideo(uri: String) {
        val current = getLiveWallpaperCustomVideos().toMutableList()
        if (current.contains(uri)) {
            current.remove(uri)
        }
        current.add(0, uri)
        saveLiveWallpaperCustomVideos(if (current.size > 5) current.take(5) else current)
    }

    /**
     * Executes the get live wallpaper available videos operation.
     * @return The resulting List<String> data.
     */
    fun getLiveWallpaperAvailableVideos(): List<String> {
        val raws =
            com.jawzf.pixeltoolkit.core.design.R.raw::class.java.fields.mapNotNull { field ->
                try {
                    if (field.name.startsWith("loop_")) field.name else null
                } catch (e: Exception) {
                    null
                }
            }
        return (raws + getLiveWallpaperCustomVideos()).filter { it.isNotBlank() }.distinct()
    }

    /**
     * Executes the remove live wallpaper custom video operation.
     *
     * @param videoUri [String] Target video uri.
     */
    fun removeLiveWallpaperCustomVideo(videoUri: String) {
        val current = getLiveWallpaperCustomVideos().toMutableList()
        val removed = current.removeAll { it == videoUri || it.isBlank() }
        if (removed) {
            saveLiveWallpaperCustomVideos(current)
            // If the removed video was selected, revert to default
            if (getLiveWallpaperSelectedVideo() == videoUri) {
                saveLiveWallpaperSelectedVideo(LIVE_WALLPAPER_DEFAULT_VIDEO)
            }
        }
    }

    /**
     * Executes the get font scale operation.
     * @return The resulting Float data.
     */
    fun getFontScale(): Float =
        try {
            android.provider.Settings.System.getFloat(
                context.contentResolver,
                android.provider.Settings.System.FONT_SCALE,
            )
        } catch (e: Exception) {
            1.0f
        }

    /**
     * Executes the set font scale operation.
     *
     * @param scale [Float] Target scale.
     */
    fun setFontScale(scale: Float) {
        putFloat(KEY_FONT_SCALE, scale)
        try {
            android.provider.Settings.System.putFloat(
                context.contentResolver,
                android.provider.Settings.System.FONT_SCALE,
                scale,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get font weight operation.
     * @return The resulting Int data.
     */
    fun getFontWeight(): Int =
        try {
            android.provider.Settings.Secure.getInt(
                context.contentResolver,
                "font_weight_adjustment",
            )
        } catch (e: Exception) {
            0
        }

    /**
     * Executes the set font weight operation.
     *
     * @param weight [Int] Target weight.
     */
    fun setFontWeight(weight: Int) {
        putInt(KEY_FONT_WEIGHT, weight)
        try {
            android.provider.Settings.Secure.putInt(
                context.contentResolver,
                "font_weight_adjustment",
                weight,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get smallest width operation.
     * @return The resulting Int data.
     */
    fun getSmallestWidth(): Int {
        val forcedDensity =
            try {
                android.provider.Settings.Secure.getInt(
                    context.contentResolver,
                    "display_density_forced",
                )
            } catch (e: Exception) {
                0
            }
        if (forcedDensity > 0) {
            val metrics = context.resources.displayMetrics
            val widthPx = Math.min(metrics.widthPixels, metrics.heightPixels)
            return (widthPx * 160) / forcedDensity
        }
        return context.resources.configuration.smallestScreenWidthDp
    }

    /**
     * Executes the set smallest width operation.
     *
     * @param widthDp [Int] Target width dp.
     */
    fun setSmallestWidth(widthDp: Int) {
        putInt(KEY_SMALLEST_WIDTH, widthDp)
        val metrics = context.resources.displayMetrics
        val widthPx = Math.min(metrics.widthPixels, metrics.heightPixels)
        val density = (widthPx * 160) / widthDp

        val command = "wm density $density"
        if (ShizukuUtils.isShizukuAvailable() && ShizukuUtils.hasPermission()) {
            ShizukuUtils.runCommand(command)
        } else if (RootUtils.isRootAvailable()) {
            RootUtils.runCommand(command)
        } else {
            try {
                android.provider.Settings.Secure.putInt(
                    context.contentResolver,
                    "display_density_forced",
                    density,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Executes the reset smallest width operation.
     */
    fun resetSmallestWidth() {
        val command = "wm density reset"
        if (ShizukuUtils.isShizukuAvailable() && ShizukuUtils.hasPermission()) {
            ShizukuUtils.runCommand(command)
        } else if (RootUtils.isRootAvailable()) {
            RootUtils.runCommand(command)
        } else {
            try {
                android.provider.Settings.Secure.putString(
                    context.contentResolver,
                    "display_density_forced",
                    null,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        remove(KEY_SMALLEST_WIDTH)
    }

    /**
     * Executes the get animation scale operation.
     *
     * @param key [String] Target key.
     * @return The resulting Float data.
     */
    fun getAnimationScale(key: String): Float =
        try {
            android.provider.Settings.Global
                .getFloat(context.contentResolver, key)
        } catch (e: Exception) {
            1.0f
        }

    /**
     * Executes the set animation scale operation.
     *
     * @param key [String] Target key.
     * @param scale [Float] Target scale.
     */
    fun setAnimationScale(
        key: String,
        scale: Float,
    ) {
        when (key) {
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE ->
                putFloat(
                    KEY_ANIMATOR_DURATION_SCALE,
                    scale,
                )

            android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE ->
                putFloat(
                    KEY_TRANSITION_ANIMATION_SCALE,
                    scale,
                )

            android.provider.Settings.Global.WINDOW_ANIMATION_SCALE ->
                putFloat(
                    KEY_WINDOW_ANIMATION_SCALE,
                    scale,
                )
        }
        try {
            android.provider.Settings.Global
                .putFloat(context.contentResolver, key, scale)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the sync system settings with saved operation.
     */
    fun syncSystemSettingsWithSaved() {
        try {
            if (contains(KEY_FONT_SCALE)) {
                setFontScale(getFloat(KEY_FONT_SCALE, 1.0f))
            }
            if (contains(KEY_FONT_WEIGHT)) {
                setFontWeight(getInt(KEY_FONT_WEIGHT, 0))
            }
            if (contains(KEY_ANIMATOR_DURATION_SCALE)) {
                setAnimationScale(
                    android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                    getFloat(KEY_ANIMATOR_DURATION_SCALE, 1.0f),
                )
            }
            if (contains(KEY_TRANSITION_ANIMATION_SCALE)) {
                setAnimationScale(
                    android.provider.Settings.Global.TRANSITION_ANIMATION_SCALE,
                    getFloat(KEY_TRANSITION_ANIMATION_SCALE, 1.0f),
                )
            }
            if (contains(KEY_WINDOW_ANIMATION_SCALE)) {
                setAnimationScale(
                    android.provider.Settings.Global.WINDOW_ANIMATION_SCALE,
                    getFloat(KEY_WINDOW_ANIMATION_SCALE, 1.0f),
                )
            }
            if (contains(KEY_REFRESH_RATE_FIXED) ||
                contains(KEY_REFRESH_RATE_MIN) ||
                contains(
                    KEY_REFRESH_RATE_PEAK,
                )
            ) {
                val mode = getRefreshRateMode()
                val fixed = getFloat(KEY_REFRESH_RATE_FIXED, 0f)
                val min = getFloat(KEY_REFRESH_RATE_MIN, 0f)
                val peak = getFloat(KEY_REFRESH_RATE_PEAK, 0f)

                if (fixed <= 0f && min <= 0f && peak <= 0f) {
                    com.jawzf.pixeltoolkit.utils.RefreshRateUtils.resetRefreshRate(
                        context,
                        shouldRestoreInfinityPeakOnRefreshRateReset(),
                    )
                } else if (mode == com.jawzf.pixeltoolkit.utils.RefreshRateUtils.MODE_RANGE && min > 0f && peak > 0f) {
                    com.jawzf.pixeltoolkit.utils.RefreshRateUtils.applyRangeRefreshRate(
                        context,
                        min,
                        peak,
                    )
                } else if (fixed > 0f || peak > 0f) {
                    com.jawzf.pixeltoolkit.utils.RefreshRateUtils.applyFixedRefreshRate(
                        context,
                        if (fixed > 0f) fixed else peak,
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the is aod enabled operation.
     * @return The resulting Boolean data.
     */
    fun isAodEnabled(): Boolean =
        android.provider.Settings.Secure.getInt(
            context.contentResolver,
            "doze_always_on",
            1,
        ) == 1

    /**
     * Updates System Secure setting for Doze Always-On-Display (AOD).
     *
     * @param enabled [Boolean] True to enable Always-On-Display, false to disable.
     */
    fun setAodEnabled(enabled: Boolean) {
        try {
            android.provider.Settings.Secure.putInt(
                context.contentResolver,
                "doze_always_on",
                if (enabled) 1 else 0,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun isAodWallpaperEnabled(): Boolean = prefs.getBoolean(KEY_AOD_WALLPAPER_ENABLED, false)

    fun setAodWallpaperEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AOD_WALLPAPER_ENABLED, enabled).apply()
    }

    /**
     * Executes the get ambient music glance album art mode operation.
     * @return The resulting String data.
     */
    fun getAmbientMusicGlanceAlbumArtMode(): String = prefs.getString(KEY_AMBIENT_MUSIC_GLANCE_ALBUM_ART_MODE, "default") ?: "default"

    /**
     * Executes the set ambient music glance album art mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setAmbientMusicGlanceAlbumArtMode(mode: String) = prefs.edit().putString(KEY_AMBIENT_MUSIC_GLANCE_ALBUM_ART_MODE, mode).apply()

    /**
     * Executes the get ambient music glance clock size operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockSize(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_SIZE, 80)

    /**
     * Executes the set ambient music glance clock size operation.
     *
     * @param size [Int] Target size.
     */
    fun setAmbientMusicGlanceClockSize(size: Int) = prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_SIZE, size).apply()

    /**
     * Executes the get ambient music glance clock weight operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockWeight(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WEIGHT, 400)

    /**
     * Executes the set ambient music glance clock weight operation.
     *
     * @param weight [Int] Target weight.
     */
    fun setAmbientMusicGlanceClockWeight(weight: Int) = prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WEIGHT, weight).apply()

    /**
     * Executes the get ambient music glance clock width operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockWidth(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WIDTH, 100)

    /**
     * Executes the set ambient music glance clock width operation.
     *
     * @param width [Int] Target width.
     */
    fun setAmbientMusicGlanceClockWidth(width: Int) = prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_WIDTH, width).apply()

    /**
     * Executes the get ambient music glance clock roundness operation.
     * @return The resulting Int data.
     */
    fun getAmbientMusicGlanceClockRoundness(): Int = prefs.getInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_ROUNDNESS, 50)

    /**
     * Executes the set ambient music glance clock roundness operation.
     *
     * @param roundness [Int] Target roundness.
     */
    fun setAmbientMusicGlanceClockRoundness(roundness: Int) =
        prefs.edit().putInt(KEY_AMBIENT_MUSIC_GLANCE_CLOCK_ROUNDNESS, roundness).apply()

    /**
     * Executes the is ambient music glance force fill while charging enabled operation.
     * @return The resulting Boolean data.
     */
    fun isAmbientMusicGlanceForceFillWhileChargingEnabled(): Boolean =
        prefs.getBoolean(KEY_AMBIENT_MUSIC_GLANCE_FORCE_FILL_WHILE_CHARGING, false)

    /**
     * Executes the set ambient music glance force fill while charging enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAmbientMusicGlanceForceFillWhileChargingEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_AMBIENT_MUSIC_GLANCE_FORCE_FILL_WHILE_CHARGING, enabled).apply()

    /**
     * Executes the is ambient music glance respect notifications enabled operation.
     * @return The resulting Boolean data.
     */
    fun isAmbientMusicGlanceRespectNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_AMBIENT_MUSIC_GLANCE_RESPECT_NOTIFICATIONS, true)

    /**
     * Executes the set ambient music glance respect notifications enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAmbientMusicGlanceRespectNotificationsEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_AMBIENT_MUSIC_GLANCE_RESPECT_NOTIFICATIONS, enabled).apply()

    // Notification Glance Settings

    fun getScaleAnimationsMode(): String = getString(KEY_SCALE_ANIMATIONS_MODE, "default") ?: "default"

    /**
     * Executes the set scale animations mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setScaleAnimationsMode(mode: String) = putString(KEY_SCALE_ANIMATIONS_MODE, mode)

    /**
     * Executes the get refresh rate mode operation.
     * @return The resulting String data.
     */
    fun getRefreshRateMode(): String =
        getString(KEY_REFRESH_RATE_MODE, com.jawzf.pixeltoolkit.utils.RefreshRateUtils.MODE_FIXED)
            ?: com.jawzf.pixeltoolkit.utils.RefreshRateUtils.MODE_FIXED

    /**
     * Executes the set refresh rate mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setRefreshRateMode(mode: String) = putString(KEY_REFRESH_RATE_MODE, mode)

    /**
     * Executes the save refresh rate state operation.
     *
     * @param mode [String] Target mode.
     * @param fixed [Float] Target fixed.
     * @param min [Float] Target min.
     * @param peak [Float] Target peak.
     */
    fun saveRefreshRateState(
        mode: String,
        fixed: Float,
        min: Float,
        peak: Float,
    ) {
        putString(KEY_REFRESH_RATE_MODE, mode)
        putFloat(KEY_REFRESH_RATE_FIXED, fixed)
        putFloat(KEY_REFRESH_RATE_MIN, min)
        putFloat(KEY_REFRESH_RATE_PEAK, peak)
    }

    /**
     * Executes the should restore infinity peak on refresh rate reset operation.
     * @return The resulting Boolean data.
     */
    fun shouldRestoreInfinityPeakOnRefreshRateReset(): Boolean = getBoolean(KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY, false)

    /**
     * Executes the set restore infinity peak on refresh rate reset operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setRestoreInfinityPeakOnRefreshRateReset(enabled: Boolean) = putBoolean(KEY_REFRESH_RATE_DEFAULT_PEAK_INFINITY, enabled)

    /**
     * Executes the get scale animations profile operation.
     *
     * @param mode [String] Target mode.
     * @return The resulting ScaleAnimationsProfile data.
     */
    fun getScaleAnimationsProfile(mode: String): ScaleAnimationsProfile {
        val key =
            if (mode == "glove") KEY_SCALE_ANIMATIONS_GLOVE_PROFILE else KEY_SCALE_ANIMATIONS_DEFAULT_PROFILE
        val json = prefs.getString(key, null)
        return if (json != null) {
            try {
                gson.fromJson(json, ScaleAnimationsProfile::class.java)
            } catch (e: Exception) {
                getDefaultScaleAnimationsProfile(mode)
            }
        } else {
            getDefaultScaleAnimationsProfile(mode)
        }
    }

    private fun getDefaultScaleAnimationsProfile(mode: String): ScaleAnimationsProfile =
        if (mode == "glove") {
            ScaleAnimationsProfile(
                fontScale = 1.25f,
                smallestWidth = 385,
                touchSensitivityEnabled = true,
                autoRotateEnabled = true,
                screenTimeout = 60000L,
            )
        } else {
            ScaleAnimationsProfile()
        }

    /**
     * Executes the save scale animations profile operation.
     *
     * @param mode [String] Target mode.
     * @param profile [ScaleAnimationsProfile] Target profile.
     */
    fun saveScaleAnimationsProfile(
        mode: String,
        profile: ScaleAnimationsProfile,
    ) {
        val key =
            if (mode == "glove") KEY_SCALE_ANIMATIONS_GLOVE_PROFILE else KEY_SCALE_ANIMATIONS_DEFAULT_PROFILE
        val json = gson.toJson(profile)
        putString(key, json)
    }

    /**
     * Executes the get touch sensitivity enabled operation.
     * @return The resulting Boolean data.
     */
    fun getTouchSensitivityEnabled(): Boolean =
        try {
            android.provider.Settings.Secure.getInt(
                context.contentResolver,
                "touch_sensitivity_enabled",
                0,
            ) == 1
        } catch (e: Exception) {
            false
        }

    /**
     * Executes the set touch sensitivity enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setTouchSensitivityEnabled(enabled: Boolean) {
        try {
            android.provider.Settings.Secure.putInt(
                context.contentResolver,
                "touch_sensitivity_enabled",
                if (enabled) 1 else 0,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get auto rotate enabled operation.
     * @return The resulting Boolean data.
     */
    fun getAutoRotateEnabled(): Boolean =
        try {
            android.provider.Settings.System.getInt(
                context.contentResolver,
                android.provider.Settings.System.ACCELEROMETER_ROTATION,
                0,
            ) == 1
        } catch (e: Exception) {
            false
        }

    /**
     * Executes the set auto rotate enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAutoRotateEnabled(enabled: Boolean) {
        try {
            android.provider.Settings.System.putInt(
                context.contentResolver,
                android.provider.Settings.System.ACCELEROMETER_ROTATION,
                if (enabled) 1 else 0,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get screen timeout operation.
     * @return The resulting Long data.
     */
    fun getScreenTimeout(): Long =
        try {
            android.provider.Settings.System.getLong(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                30000L,
            )
        } catch (e: Exception) {
            30000L
        }

    /**
     * Executes the set screen timeout operation.
     *
     * @param timeoutMs [Long] Target timeout ms.
     */
    fun setScreenTimeout(timeoutMs: Long) {
        try {
            android.provider.Settings.System.putLong(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
                timeoutMs,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get lock screen clock weight operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockWeight(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_WEIGHT, 300)

    /**
     * Executes the set lock screen clock weight operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockWeight(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_WEIGHT, value)

    /**
     * Executes the get lock screen clock width operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockWidth(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_WIDTH, 116)

    /**
     * Executes the set lock screen clock width operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockWidth(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_WIDTH, value)

    /**
     * Executes the get lock screen clock grade operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockGrade(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_GRADE, 0)

    /**
     * Executes the set lock screen clock grade operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockGrade(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_GRADE, value)

    /**
     * Executes the get lock screen clock roundness operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockRoundness(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_ROUNDNESS, 100)

    /**
     * Executes the set lock screen clock roundness operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockRoundness(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_ROUNDNESS, value)

    /**
     * Executes the get lock screen clock color tone operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockColorTone(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_COLOR_TONE, 75)

    /**
     * Executes the set lock screen clock color tone operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockColorTone(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_COLOR_TONE, value)

    /**
     * Executes the get lock screen clock selected color id operation.
     * @return The resulting String data.
     */
    fun getLockScreenClockSelectedColorId(): String = getString(KEY_LOCK_SCREEN_CLOCK_SELECTED_COLOR_ID, "DEFAULT") ?: "DEFAULT"

    /**
     * Executes the set lock screen clock selected color id operation.
     *
     * @param value [String] Target value.
     */
    fun setLockScreenClockSelectedColorId(value: String) = putString(KEY_LOCK_SCREEN_CLOCK_SELECTED_COLOR_ID, value)

    /**
     * Executes the get lock screen clock seed color operation.
     * @return The resulting Int data.
     */
    fun getLockScreenClockSeedColor(): Int = getInt(KEY_LOCK_SCREEN_CLOCK_SEED_COLOR, 0)

    /**
     * Executes the set lock screen clock seed color operation.
     *
     * @param value [Int] Target value.
     */
    fun setLockScreenClockSeedColor(value: Int) = putInt(KEY_LOCK_SCREEN_CLOCK_SEED_COLOR, value)

    fun getAodWallpaperOpacity(): Float = getFloat(KEY_AOD_WALLPAPER_OPACITY, 0.3f)

    fun setAodWallpaperOpacity(value: Float) = putFloat(KEY_AOD_WALLPAPER_OPACITY, value)

    // timeout as string (e.g., "never", "5s", "15s", "30s", "1m", "3m", "5m", "10m"); default = "3m"
    fun getAodWallpaperTimeout(): String {
        return try {
            getString(KEY_AOD_WALLPAPER_TIMEOUT, "3m") ?: "3m"
        } catch (e: Exception) {
            val oldInt = getInt(KEY_AOD_WALLPAPER_TIMEOUT, 3)
            val strVal = when (oldInt) {
                0 -> "never"
                1 -> "1m"
                3 -> "3m"
                5 -> "5m"
                10 -> "10m"
                else -> "${oldInt}m"
            }
            setAodWallpaperTimeout(strVal)
            strVal
        }
    }

    fun setAodWallpaperTimeout(value: String) = putString(KEY_AOD_WALLPAPER_TIMEOUT, value)

    fun getAodWallpaperCustomTimeout(): Float = getFloat(KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT, 30f)

    fun setAodWallpaperCustomTimeout(value: Float) = putFloat(KEY_AOD_WALLPAPER_CUSTOM_TIMEOUT, value)

    // blur  0-25
    fun getAodWallpaperBlur(): Float = getFloat(KEY_AOD_WALLPAPER_BLUR, 0f)

    fun setAodWallpaperBlur(value: Float) = putFloat(KEY_AOD_WALLPAPER_BLUR, value)

    // vignette 0-100
    fun getAodWallpaperVignette(): Float = getFloat(KEY_AOD_WALLPAPER_VIGNETTE, 0f)

    fun setAodWallpaperVignette(value: Float) = putFloat(KEY_AOD_WALLPAPER_VIGNETTE, value)

    // black threshold 0-50 (default 15)
    fun getAodWallpaperBlackThreshold(): Float = getFloat(KEY_AOD_WALLPAPER_BLACK_THRESHOLD, 15f)

    fun setAodWallpaperBlackThreshold(value: Float) = putFloat(KEY_AOD_WALLPAPER_BLACK_THRESHOLD, value)

    fun hasAodWallpaperCustomImage(): Boolean = getBoolean(KEY_AOD_WALLPAPER_CUSTOM_IMAGE, false)

    fun setAodWallpaperCustomImage(hasCustomImage: Boolean) = putBoolean(KEY_AOD_WALLPAPER_CUSTOM_IMAGE, hasCustomImage)

    fun isAodWallpaperUseAlbumArtEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_USE_ALBUM_ART, false)

    fun setAodWallpaperUseAlbumArt(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_USE_ALBUM_ART, enabled)

    fun isAodWallpaperKeepOnMediaEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_KEEP_ON_MEDIA, false)

    fun setAodWallpaperKeepOnMedia(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_KEEP_ON_MEDIA, enabled)

    fun isAodWallpaperExtendedMediaEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_EXTENDED_MEDIA, false)

    fun setAodWallpaperExtendedMedia(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_EXTENDED_MEDIA, enabled)

    fun isAodWallpaperExtendedAppIconEnabled(): Boolean = getBoolean(KEY_AOD_WALLPAPER_EXTENDED_APP_ICON, false)

    fun setAodWallpaperExtendedAppIcon(enabled: Boolean) = putBoolean(KEY_AOD_WALLPAPER_EXTENDED_APP_ICON, enabled)

    fun getAodWallpaperExtendedTextScale(): Float = getFloat(KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE, 1f)

    fun setAodWallpaperExtendedTextScale(value: Float) = putFloat(KEY_AOD_WALLPAPER_EXTENDED_TEXT_SCALE, value)

    fun isPixelSearchResultAppsEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_RESULT_APPS, true)
    fun setPixelSearchResultAppsEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_RESULT_APPS, enabled)

    fun isPixelSearchResultContactsEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_RESULT_CONTACTS, true)
    fun setPixelSearchResultContactsEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_RESULT_CONTACTS, enabled)

    fun isPixelSearchResultSettingsEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_RESULT_SETTINGS, true)
    fun setPixelSearchResultSettingsEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_RESULT_SETTINGS, enabled)

    fun isPixelSearchResultShortcutsEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_RESULT_SHORTCUTS, true)
    fun setPixelSearchResultShortcutsEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_RESULT_SHORTCUTS, enabled)

    fun isPixelSearchResultWebEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_RESULT_WEB, true)
    fun setPixelSearchResultWebEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_RESULT_WEB, enabled)

    fun isPixelSearchResultMediaEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_RESULT_MEDIA, false)
    fun setPixelSearchResultMediaEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_RESULT_MEDIA, enabled)

    fun isPixelSearchResultFilesEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_RESULT_FILES, false)
    fun setPixelSearchResultFilesEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_RESULT_FILES, enabled)

    fun isPixelSearchBubblesWebEnabled(): Boolean = getBoolean(KEY_PIXEL_SEARCH_BUBBLES_WEB, false)
    fun setPixelSearchBubblesWebEnabled(enabled: Boolean) = putBoolean(KEY_PIXEL_SEARCH_BUBBLES_WEB, enabled)

    fun getPixelSearchEngine(): String = getString(KEY_PIXEL_SEARCH_ENGINE, "Google") ?: "Google"
    fun setPixelSearchEngine(engine: String) = putString(KEY_PIXEL_SEARCH_ENGINE, engine)

    fun isBubbleWebFullscreen(): Boolean = getBoolean(KEY_BUBBLE_WEB_FULLSCREEN, false)
    fun setBubbleWebFullscreen(fullscreen: Boolean) = putBoolean(KEY_BUBBLE_WEB_FULLSCREEN, fullscreen)

    fun isSecureSensitiveTilesEnabled(): Boolean = getBoolean(KEY_SECURE_SENSITIVE_TILES, true)
    fun setSecureSensitiveTilesEnabled(enabled: Boolean) = putBoolean(KEY_SECURE_SENSITIVE_TILES, enabled)

    fun isDuoEnabled(): Boolean = getBoolean(KEY_DUO_ENABLED, false)
    fun setDuoEnabled(enabled: Boolean) = putBoolean(KEY_DUO_ENABLED, enabled)

    fun isDuoIslandCombinedSetting(): Boolean = getBoolean(KEY_DUO_ISLAND_COMBINED, false)

    fun isDuoHideOnShadeEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_ON_SHADE, false)

    fun setDuoHideOnShadeEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_ON_SHADE, enabled)

    fun isDuoKeepInFullscreenEnabled(): Boolean = getBoolean(KEY_DUO_KEEP_IN_FULLSCREEN, false)

    fun setDuoKeepInFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_DUO_KEEP_IN_FULLSCREEN, enabled)

    fun isDuoDisableInteractionsInFullscreenEnabled(): Boolean = getBoolean(KEY_DUO_DISABLE_INTERACTIONS_IN_FULLSCREEN, false)

    fun setDuoDisableInteractionsInFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_DUO_DISABLE_INTERACTIONS_IN_FULLSCREEN, enabled)
    fun setDuoIslandCombined(enabled: Boolean) = putBoolean(KEY_DUO_ISLAND_COMBINED, enabled)

    // Only effective while both features are on
    fun isDuoIslandCombined(): Boolean = isDuoIslandCombinedSetting() && isDuoEnabled() && isIslandEnabled()

    fun isDuoTapForBriefEnabled(): Boolean = getBoolean(KEY_DUO_TAP_FOR_BRIEF, false)
    fun setDuoTapForBriefEnabled(enabled: Boolean) = putBoolean(KEY_DUO_TAP_FOR_BRIEF, enabled)
    fun isDuoTapForBriefActive(): Boolean = isDuoTapForBriefEnabled() && isDuoIslandCombined() && isIslandBriefEnabled()

    fun isDuoAutoDetectEnabled(): Boolean = getBoolean(KEY_DUO_USE_AUTO_DETECT, true)
    fun setDuoAutoDetectEnabled(enabled: Boolean) = putBoolean(KEY_DUO_USE_AUTO_DETECT, enabled)

    fun getDisplayProfileId(): String {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager
        val display = displayManager?.getDisplay(android.view.Display.DEFAULT_DISPLAY) ?: return "default"
        val size = android.graphics.Point()
        @Suppress("DEPRECATION")
        display.getRealSize(size)
        if (size.x <= 0 || size.y <= 0) return "default"
        return "${minOf(size.x, size.y)}x${maxOf(size.x, size.y)}"
    }

    fun markDisplayProfileSeen() {
        val seen = prefs.getStringSet(KEY_DUO_KNOWN_DISPLAY_PROFILES, emptySet()) ?: emptySet()
        val id = getDisplayProfileId()
        if (!seen.contains(id)) {
            prefs.edit().putStringSet(KEY_DUO_KNOWN_DISPLAY_PROFILES, seen + id).apply()
        }
    }

    fun getKnownDisplayProfileCount(): Int = prefs.getStringSet(KEY_DUO_KNOWN_DISPLAY_PROFILES, emptySet())?.size ?: 0

    fun isFoldableDevice(): Boolean =
        context.packageManager.hasSystemFeature("android.hardware.sensor.hinge_angle") ||
            context.packageManager.hasSystemFeature("com.google.pixel.camera.concurrent_foldable_dual_front")

    private fun orientationKey(baseKey: String): String {
        val orientation = if (context.resources.configuration.orientation ==
            android.content.res.Configuration.ORIENTATION_LANDSCAPE) "landscape" else "portrait"
        return "$baseKey@${getDisplayProfileId()}@$orientation"
    }

    private fun getCameraPlacementFloat(baseKey: String, default: Float, useOrientation: Boolean): Float {
        val orientationKey = orientationKey(baseKey)
        val displayKey = "$baseKey@${getDisplayProfileId()}"
        return when {
            useOrientation && contains(orientationKey) -> getFloat(orientationKey, default)
            contains(displayKey) -> getFloat(displayKey, default)
            else -> getFloat(baseKey, default)
        }
    }

    private fun setCameraPlacementFloat(baseKey: String, value: Float, useOrientation: Boolean) {
        if (useOrientation) {
            putFloat(orientationKey(baseKey), value)
        } else {
            putFloat("$baseKey@${getDisplayProfileId()}", value)
            putFloat(baseKey, value)
        }
    }

    private fun getCameraPlacementString(baseKey: String, default: String, useOrientation: Boolean): String {
        val displayKey = "$baseKey@${getDisplayProfileId()}"
        return when {
            useOrientation && contains(orientationKey(baseKey)) -> getString(orientationKey(baseKey), default)
            contains(displayKey) -> getString(displayKey, default)
            else -> getString(baseKey, default)
        } ?: default
    }

    private fun setCameraPlacementString(baseKey: String, value: String, useOrientation: Boolean) {
        if (useOrientation) putString(orientationKey(baseKey), value)
        else {
            putString("$baseKey@${getDisplayProfileId()}", value)
            putString(baseKey, value)
        }
    }

    private fun getCameraHidden(baseKey: String, default: Boolean): Boolean =
        getBoolean("$baseKey@${getDisplayProfileId()}", getBoolean(baseKey, default))

    private fun setCameraHidden(baseKey: String, hidden: Boolean) =
        putBoolean("$baseKey@${getDisplayProfileId()}", hidden)

    fun isDuoOrientationProfilesEnabled(): Boolean = getBoolean(KEY_DUO_ORIENTATION_PROFILES, false)
    fun setDuoOrientationProfilesEnabled(enabled: Boolean) = putBoolean(KEY_DUO_ORIENTATION_PROFILES, enabled)
    fun isDuoHiddenInCurrentOrientation(): Boolean =
        if (context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
            getCameraHidden(KEY_DUO_HIDE_LANDSCAPE, false)
        else getCameraHidden(KEY_DUO_HIDE_PORTRAIT, false)
    fun setDuoHiddenInCurrentOrientation(hidden: Boolean) =
        setCameraHidden(if (context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
            KEY_DUO_HIDE_LANDSCAPE else KEY_DUO_HIDE_PORTRAIT, hidden)

    fun isIslandOrientationProfilesEnabled(): Boolean = getBoolean(KEY_ISLAND_ORIENTATION_PROFILES, false)
    fun setIslandOrientationProfilesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_ORIENTATION_PROFILES, enabled)
    fun isIslandHiddenInCurrentOrientation(): Boolean =
        if (context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
            getCameraHidden(KEY_ISLAND_HIDE_LANDSCAPE, !isIslandKeepOnLandscapeEnabled())
        else getCameraHidden(KEY_ISLAND_HIDE_PORTRAIT, false)
    fun setIslandHiddenInCurrentOrientation(hidden: Boolean) =
        setCameraHidden(if (context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
            KEY_ISLAND_HIDE_LANDSCAPE else KEY_ISLAND_HIDE_PORTRAIT, hidden)

    fun isStatusGlanceOrientationProfilesEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_ORIENTATION_PROFILES, false)
    fun setStatusGlanceOrientationProfilesEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_ORIENTATION_PROFILES, enabled)
    fun isStatusGlanceHiddenInCurrentOrientation(): Boolean =
        if (context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
            getCameraHidden(KEY_STATUS_GLANCE_HIDE_LANDSCAPE, true)
        else getCameraHidden(KEY_STATUS_GLANCE_HIDE_PORTRAIT, false)
    fun setStatusGlanceHiddenInCurrentOrientation(hidden: Boolean) =
        setCameraHidden(if (context.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE)
            KEY_STATUS_GLANCE_HIDE_LANDSCAPE else KEY_STATUS_GLANCE_HIDE_PORTRAIT, hidden)

    private fun getDuoPlacementFloat(baseKey: String, default: Float): Float =
        getCameraPlacementFloat(baseKey, default, isDuoOrientationProfilesEnabled())

    private fun setDuoPlacementFloat(baseKey: String, value: Float) {
        setCameraPlacementFloat(baseKey, value, isDuoOrientationProfilesEnabled())
    }

    fun getDuoCameraOffsetX(): Float = getDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_X, 50f)
    fun setDuoCameraOffsetX(value: Float) = setDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_X, value)

    fun getDuoCameraOffsetY(): Float = getDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_Y, 3f)
    fun setDuoCameraOffsetY(value: Float) = setDuoPlacementFloat(KEY_DUO_CAMERA_OFFSET_Y, value)

    fun getDuoCameraSize(): Float = getDuoPlacementFloat(KEY_DUO_CAMERA_SIZE, 1.0f)
    fun setDuoCameraSize(value: Float) = setDuoPlacementFloat(KEY_DUO_CAMERA_SIZE, value)

    fun getDuoArcThickness(): Float = getFloat(KEY_DUO_ARC_THICKNESS, 4f)
    fun setDuoArcThickness(value: Float) = putFloat(KEY_DUO_ARC_THICKNESS, value)

    fun getDuoDotSize(): Float = getFloat(KEY_DUO_DOT_SIZE, 4f)
    fun setDuoDotSize(value: Float) = putFloat(KEY_DUO_DOT_SIZE, value)

    fun getDuoRingRadius(): Float = getFloat(KEY_DUO_RING_RADIUS, 1.0f)
    fun setDuoRingRadius(value: Float) = putFloat(KEY_DUO_RING_RADIUS, value)

    fun getDuoTimeTextScale(): Float = getFloat(KEY_DUO_TIME_TEXT_SCALE, 1.0f)

    fun setDuoTimeTextScale(value: Float) = putFloat(KEY_DUO_TIME_TEXT_SCALE, value)

    fun isDuoShowBatteryEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_BATTERY, true)
    fun setDuoShowBatteryEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_BATTERY, enabled)

    fun isDuoShowBatteryPercentageEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_BATTERY_PERCENTAGE, false)
    fun setDuoShowBatteryPercentageEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_BATTERY_PERCENTAGE, enabled)

    fun isDuoBatteryPercentageOnlyColoredEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED, false)
    fun setDuoBatteryPercentageOnlyColoredEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_PERCENTAGE_ONLY_COLORED, enabled)

    fun isDuoBatteryChargingColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED, true)
    fun setDuoBatteryChargingColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_CHARGING_COLOR_ENABLED, enabled)

    fun getDuoBatteryChargingColor(): String = getString(KEY_DUO_BATTERY_CHARGING_COLOR, "auto") ?: "auto"
    fun setDuoBatteryChargingColor(colorHex: String) = putString(KEY_DUO_BATTERY_CHARGING_COLOR, colorHex)

    fun isDuoBatteryPowerSaveColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED, true)
    fun setDuoBatteryPowerSaveColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_POWER_SAVE_COLOR_ENABLED, enabled)

    fun getDuoBatteryPowerSaveColor(): String = getString(KEY_DUO_BATTERY_POWER_SAVE_COLOR, "#FF9800") ?: "#FF9800"
    fun setDuoBatteryPowerSaveColor(colorHex: String) = putString(KEY_DUO_BATTERY_POWER_SAVE_COLOR, colorHex)

    fun isDuoBatteryLowColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_LOW_COLOR_ENABLED, true)
    fun setDuoBatteryLowColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_LOW_COLOR_ENABLED, enabled)

    fun getDuoBatteryLowColor(): String = getString(KEY_DUO_BATTERY_LOW_COLOR, "#FFEB3B") ?: "#FFEB3B"
    fun setDuoBatteryLowColor(colorHex: String) = putString(KEY_DUO_BATTERY_LOW_COLOR, colorHex)

    fun isIslandBatteryIdleColorEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED, false)
    fun setIslandBatteryIdleColorEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED, enabled)

    fun getIslandBatteryIdleColor(): String = getString(KEY_ISLAND_BATTERY_IDLE_COLOR, "#FFFFFF") ?: "#FFFFFF"
    fun setIslandBatteryIdleColor(colorHex: String) = putString(KEY_ISLAND_BATTERY_IDLE_COLOR, colorHex)

    fun isDuoBatteryCriticalColorEnabled(): Boolean = getBoolean(KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED, true)
    fun setDuoBatteryCriticalColorEnabled(enabled: Boolean) = putBoolean(KEY_DUO_BATTERY_CRITICAL_COLOR_ENABLED, enabled)

    fun getDuoBatteryCriticalColor(): String = getString(KEY_DUO_BATTERY_CRITICAL_COLOR, "#F44336") ?: "#F44336"
    fun setDuoBatteryCriticalColor(colorHex: String) = putString(KEY_DUO_BATTERY_CRITICAL_COLOR, colorHex)

    fun getDuoBatteryLowLevel(): Int = getInt(KEY_DUO_BATTERY_LOW_LEVEL, DuoBatteryLevels.DEFAULT_LOW)
    fun getDuoBatteryCriticalLevel(): Int = getInt(KEY_DUO_BATTERY_CRITICAL_LEVEL, DuoBatteryLevels.DEFAULT_CRITICAL)

    fun setDuoBatteryLevels(low: Int, critical: Int) {
        putInt(KEY_DUO_BATTERY_LOW_LEVEL, low)
        putInt(KEY_DUO_BATTERY_CRITICAL_LEVEL, critical)
    }

    fun isDuoShowNetworksEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_NETWORKS, true)
    fun setDuoShowNetworksEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_NETWORKS, enabled)

    fun isDuoDifferentiateWifiEnabled(): Boolean = getBoolean(KEY_DUO_DIFFERENTIATE_WIFI, true)
    fun setDuoDifferentiateWifiEnabled(enabled: Boolean) = putBoolean(KEY_DUO_DIFFERENTIATE_WIFI, enabled)

    fun isDuoShowTimeEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_TIME, false)
    fun setDuoShowTimeEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_TIME, enabled)

    fun isDuoShowMediaEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_MEDIA, true)
    fun setDuoShowMediaEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_MEDIA, enabled)

    fun isDuoRotateAlbumArtEnabled(): Boolean = getBoolean(KEY_DUO_ROTATE_ALBUM_ART, false)
    fun setDuoRotateAlbumArtEnabled(enabled: Boolean) = putBoolean(KEY_DUO_ROTATE_ALBUM_ART, enabled)

    fun isDuoShowProgressEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_PROGRESS, true)
    fun setDuoShowProgressEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_PROGRESS, enabled)

    fun isDuoShowFlashlightEnabled(): Boolean = getBoolean(KEY_DUO_SHOW_FLASHLIGHT, true)
    fun setDuoShowFlashlightEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SHOW_FLASHLIGHT, enabled)

    fun isDuoHideWhenScreenOffEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF, true)
    fun setDuoHideWhenScreenOffEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF, enabled)

    fun isDuoHideWhenScreenOffOnlyIdleEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE, false)
    fun setDuoHideWhenScreenOffOnlyIdleEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_WHEN_SCREEN_OFF_ONLY_IDLE, enabled)

    fun isDuoHideWhenLockedEnabled(): Boolean = getBoolean(KEY_DUO_HIDE_WHEN_LOCKED, false)
    fun setDuoHideWhenLockedEnabled(enabled: Boolean) = putBoolean(KEY_DUO_HIDE_WHEN_LOCKED, enabled)

    fun isDuoUseMaterialYouEnabled(): Boolean = getBoolean(KEY_DUO_USE_MATERIAL_YOU, true)
    fun setDuoUseMaterialYouEnabled(enabled: Boolean) = putBoolean(KEY_DUO_USE_MATERIAL_YOU, enabled)

    fun getDuoCustomColor(): String = getString(KEY_DUO_CUSTOM_COLOR, "#FFFFFF") ?: "#FFFFFF"
    fun setDuoCustomColor(colorHex: String) = putString(KEY_DUO_CUSTOM_COLOR, colorHex)

    fun getDuoTapAction(): Action? = getRemapAction(KEY_DUO_TAP_ACTION)
    fun setDuoTapAction(action: Action?) = setRemapAction(KEY_DUO_TAP_ACTION, action)

    fun getDuoDoubleTapAction(): Action? = getRemapAction(KEY_DUO_DOUBLE_TAP_ACTION)
    fun setDuoDoubleTapAction(action: Action?) = setRemapAction(KEY_DUO_DOUBLE_TAP_ACTION, action)

    fun getDuoLongPressAction(): Action? = getRemapAction(KEY_DUO_LONG_PRESS_ACTION)
    fun setDuoLongPressAction(action: Action?) = setRemapAction(KEY_DUO_LONG_PRESS_ACTION, action)

    fun getDuoSwipeDownAction(): Action? = getRemapAction(KEY_DUO_SWIPE_DOWN_ACTION)
    fun setDuoSwipeDownAction(action: Action?) = setRemapAction(KEY_DUO_SWIPE_DOWN_ACTION, action)

    fun getDuoSlideMode(): String = getString(KEY_DUO_SLIDE_MODE, "none") ?: "none"
    fun setDuoSlideMode(mode: String) = putString(KEY_DUO_SLIDE_MODE, mode)

    fun isDuoSlideTrackEnabled(): Boolean = getBoolean(KEY_DUO_SLIDE_TRACK, false)
    fun setDuoSlideTrackEnabled(enabled: Boolean) = putBoolean(KEY_DUO_SLIDE_TRACK, enabled)

    fun isDuoSlideInvertDirectionEnabled(): Boolean = getBoolean(KEY_DUO_SLIDE_INVERT_DIRECTION, false)
    fun setDuoSlideInvertDirection(enabled: Boolean) = putBoolean(KEY_DUO_SLIDE_INVERT_DIRECTION, enabled)

    // Island
    fun isIslandEnabled(): Boolean = getBoolean(KEY_ISLAND_ENABLED, false)
    fun setIslandEnabled(enabled: Boolean) {
        putBoolean(KEY_ISLAND_ENABLED, enabled)
        if (isIslandSuppressSystemHeadsUpEnabled()) applyHeadsUpSuppression(enabled)
    }

    fun isIslandAutoDetectEnabled(): Boolean = getBoolean(KEY_ISLAND_USE_AUTO_DETECT, true)
    fun setIslandAutoDetectEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_USE_AUTO_DETECT, enabled)

    fun getIslandCameraOffsetX(): Float = getCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_X, 50f, isIslandOrientationProfilesEnabled())
    fun setIslandCameraOffsetX(value: Float) = setCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_X, value, isIslandOrientationProfilesEnabled())

    fun getIslandCameraOffsetY(): Float = getCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_Y, 3f, isIslandOrientationProfilesEnabled())
    fun setIslandCameraOffsetY(value: Float) = setCameraPlacementFloat(KEY_ISLAND_CAMERA_OFFSET_Y, value, isIslandOrientationProfilesEnabled())

    fun getIslandCameraSize(): Float = getCameraPlacementFloat(KEY_ISLAND_CAMERA_SIZE, 1.0f, isIslandOrientationProfilesEnabled())
    fun setIslandCameraSize(value: Float) = setCameraPlacementFloat(KEY_ISLAND_CAMERA_SIZE, value, isIslandOrientationProfilesEnabled())

    fun getIslandMaxWidth(): Float = getFloat(KEY_ISLAND_MAX_WIDTH, 360f)
    fun setIslandMaxWidth(value: Float) = putFloat(KEY_ISLAND_MAX_WIDTH, value)

    fun getIslandCutoutGap(): Float = getFloat(KEY_ISLAND_CUTOUT_GAP, 6f)
    fun setIslandCutoutGap(value: Float) = putFloat(KEY_ISLAND_CUTOUT_GAP, value)

    fun isIslandSuppressSystemHeadsUpEnabled(): Boolean = getBoolean(KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP, false)
    fun setIslandSuppressSystemHeadsUpEnabled(enabled: Boolean) {
        putBoolean(KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP, enabled)
        applyHeadsUpSuppression(enabled && isIslandEnabled())
    }

    fun isIslandHideWhenScreenOffEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_WHEN_SCREEN_OFF, true)
    fun setIslandHideWhenScreenOffEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_WHEN_SCREEN_OFF, enabled)

    fun getIslandShowWhen(): String =
        getString(KEY_ISLAND_SHOW_WHEN, null)
            ?: if (isIslandHideWhenScreenOffEnabled()) ISLAND_SHOW_WHEN_UNLOCKED else ISLAND_SHOW_WHEN_ALWAYS
    fun setIslandShowWhen(value: String) = putString(KEY_ISLAND_SHOW_WHEN, value)

    fun getIslandTimeoutMs(): Long = getLong(KEY_ISLAND_TIMEOUT_MS, 4500L)
    fun setIslandTimeoutMs(value: Long) = putLong(KEY_ISLAND_TIMEOUT_MS, value)

    fun isIslandBorderOutlineEnabled(): Boolean = getBoolean(KEY_ISLAND_BORDER_OUTLINE_ENABLED, false)
    fun setIslandBorderOutlineEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BORDER_OUTLINE_ENABLED, enabled)

    fun getIslandBorderOutlineColor(): String =
        getString(KEY_ISLAND_BORDER_OUTLINE_COLOR, ISLAND_BORDER_OUTLINE_DEFAULT_COLOR) ?: ISLAND_BORDER_OUTLINE_DEFAULT_COLOR
    fun setIslandBorderOutlineColor(colorHex: String) = putString(KEY_ISLAND_BORDER_OUTLINE_COLOR, colorHex)

    fun isIslandBorderOutlineDynamicEnabled(): Boolean = getBoolean(KEY_ISLAND_BORDER_OUTLINE_DYNAMIC, false)
    fun setIslandBorderOutlineDynamicEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BORDER_OUTLINE_DYNAMIC, enabled)

    fun isIslandBorderOutlineHiddenWhenExpanded(): Boolean = getBoolean(KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED, false)
    fun setIslandBorderOutlineHiddenWhenExpanded(hidden: Boolean) = putBoolean(KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED, hidden)

    fun getIslandBorderOutlineThickness(): Float = getFloat(KEY_ISLAND_BORDER_OUTLINE_THICKNESS, 1f)
    fun setIslandBorderOutlineThickness(value: Float) = putFloat(KEY_ISLAND_BORDER_OUTLINE_THICKNESS, value)

    fun isIslandPulseShadowEnabled(): Boolean = getBoolean(KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION, false)
    fun setIslandPulseShadowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION, enabled)

    fun getIslandPulseShadowSize(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_SIZE, 0.5f)
    fun setIslandPulseShadowSize(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_SIZE, value)

    fun getIslandPulseShadowYShift(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_Y_SHIFT, 0.35f)
    fun setIslandPulseShadowYShift(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_Y_SHIFT, value)

    fun getIslandPulseShadowSpread(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_SPREAD, 2f)
    fun setIslandPulseShadowSpread(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_SPREAD, value)

    fun getIslandPulseShadowDurationMs(): Float = getFloat(KEY_ISLAND_PULSE_SHADOW_DURATION_MS, 1450f)
    fun setIslandPulseShadowDurationMs(value: Float) = putFloat(KEY_ISLAND_PULSE_SHADOW_DURATION_MS, value)

    fun isIslandShowGlowEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_GLOW, true)
    fun setIslandShowGlowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_GLOW, enabled)

    fun getIslandExpandedWidth(): Float = getFloat(KEY_ISLAND_EXPANDED_WIDTH, 360f)
    fun setIslandExpandedWidth(value: Float) = putFloat(KEY_ISLAND_EXPANDED_WIDTH, value)

    fun getIslandExpandedRoundness(): Float = getFloat(KEY_ISLAND_EXPANDED_ROUNDNESS, 24f)
    fun setIslandExpandedRoundness(value: Float) = putFloat(KEY_ISLAND_EXPANDED_ROUNDNESS, value)

    fun getIslandExpandedPadding(): Float = getFloat(KEY_ISLAND_EXPANDED_PADDING, 16f)
    fun setIslandExpandedPadding(value: Float) = putFloat(KEY_ISLAND_EXPANDED_PADDING, value)

    fun getIslandExpandedTopPadding(): Float = getFloat(KEY_ISLAND_EXPANDED_TOP_PADDING, 0f)
    fun setIslandExpandedTopPadding(value: Float) = putFloat(KEY_ISLAND_EXPANDED_TOP_PADDING, value)
    fun getIslandExpandedBottomPadding(): Float = getFloat(KEY_ISLAND_EXPANDED_BOTTOM_PADDING, 12f)
    fun setIslandExpandedBottomPadding(value: Float) = putFloat(KEY_ISLAND_EXPANDED_BOTTOM_PADDING, value)

    fun getIslandExpandedTimeoutMs(): Long = getLong(KEY_ISLAND_EXPANDED_TIMEOUT_MS, 0L)
    fun setIslandExpandedTimeoutMs(value: Long) = putLong(KEY_ISLAND_EXPANDED_TIMEOUT_MS, value)

    fun isIslandCatchUpEnabled(): Boolean = isIslandNotifHubEnabled() || getBoolean(KEY_ISLAND_CATCH_UP_ENABLED, true)
    fun setIslandCatchUpEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_CATCH_UP_ENABLED, enabled)

    // The stored value is kept as the hub's pin duration while the bubble itself never expires
    fun getIslandCatchUpStoredMs(): Long = getLong(KEY_ISLAND_CATCH_UP_TIMEOUT_MS, 10000L)

    fun getIslandCatchUpTimeoutMs(): Long = if (isIslandNotifHubEnabled()) 0L else getIslandCatchUpStoredMs()
    fun setIslandCatchUpTimeoutMs(value: Long) = putLong(KEY_ISLAND_CATCH_UP_TIMEOUT_MS, value)

    fun isIslandCatchUpInfinite(): Boolean = getIslandCatchUpTimeoutMs() <= 0L

    // Ignores the hub's override, for features that borrow the catch-up duration
    fun isIslandCatchUpStoredInfinite(): Boolean = getIslandCatchUpStoredMs() <= 0L

    fun isIslandShowMediaEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_MEDIA, true)
    fun setIslandShowMediaEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_MEDIA, enabled)

    fun loadIslandMediaExcludedApps() = loadAppSelection(KEY_ISLAND_MEDIA_EXCLUDED_APPS)
    fun saveIslandMediaExcludedApps(apps: List<AppSelection>) = saveAppSelection(KEY_ISLAND_MEDIA_EXCLUDED_APPS, apps)
    fun updateIslandMediaExcludedAppSelection(packageName: String, enabled: Boolean) =
        updateAppSelection(KEY_ISLAND_MEDIA_EXCLUDED_APPS, packageName, enabled)

    fun isIslandShowCalendarEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CALENDAR, false)
    fun setIslandShowCalendarEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CALENDAR, enabled)

    fun isIslandShowConsciousGateEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CONSCIOUS_GATE, true)
    fun setIslandShowConsciousGateEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CONSCIOUS_GATE, enabled)

    fun isIslandShowFlashlightEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_FLASHLIGHT, true)
    fun setIslandShowFlashlightEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_FLASHLIGHT, enabled)

    fun isIslandShowTimeBatteryEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_TIME_BATTERY, false)
    fun setIslandShowTimeBatteryEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_TIME_BATTERY, enabled)

    fun isIslandShowBatteryEnabled(): Boolean =
        if (contains(KEY_ISLAND_SHOW_BATTERY)) getBoolean(KEY_ISLAND_SHOW_BATTERY, false) else isIslandShowTimeBatteryEnabled()
    fun setIslandShowBatteryEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_BATTERY, enabled)

    fun isIslandBatteryPeekAlertsEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_PEEK_ALERTS, true)
    fun setIslandBatteryPeekAlertsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_PEEK_ALERTS, enabled)

    fun getIslandBatteryStyle(): String =
        getString(KEY_ISLAND_BATTERY_STYLE, ISLAND_BATTERY_STYLE_RING) ?: ISLAND_BATTERY_STYLE_RING
    fun setIslandBatteryStyle(value: String) = putString(KEY_ISLAND_BATTERY_STYLE, value)

    fun getIslandDevicesBatteryStyle(): String =
        getString(KEY_ISLAND_DEVICES_BATTERY_STYLE, ISLAND_BATTERY_STYLE_RING) ?: ISLAND_BATTERY_STYLE_RING
    fun setIslandDevicesBatteryStyle(value: String) = putString(KEY_ISLAND_DEVICES_BATTERY_STYLE, value)

    fun isIslandBatteryPercentageEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_PERCENTAGE, false)
    fun setIslandBatteryPercentageEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_PERCENTAGE, enabled)

    fun getIslandLongPressAction(): Action? = getRemapAction(KEY_ISLAND_LONG_PRESS_ACTION)
    fun setIslandLongPressAction(action: Action?) = setRemapAction(KEY_ISLAND_LONG_PRESS_ACTION, action)

    fun getIslandSlideMode(): String = getString(KEY_ISLAND_SLIDE_MODE, "none") ?: "none"
    fun setIslandSlideMode(mode: String) = putString(KEY_ISLAND_SLIDE_MODE, mode)

    fun isIslandLikeWhilePlayingEnabled(): Boolean = getBoolean(KEY_ISLAND_LIKE_WHILE_PLAYING, false)
    fun setIslandLikeWhilePlayingEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_LIKE_WHILE_PLAYING, enabled)

    fun isIslandSlideTrackEnabled(): Boolean = getBoolean(KEY_ISLAND_SLIDE_TRACK, false)
    fun setIslandSlideTrackEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SLIDE_TRACK, enabled)

    fun isIslandSlideInvertDirectionEnabled(): Boolean = getBoolean(KEY_ISLAND_SLIDE_INVERT_DIRECTION, true)
    fun setIslandSlideInvertDirection(enabled: Boolean) = putBoolean(KEY_ISLAND_SLIDE_INVERT_DIRECTION, enabled)

    fun isIslandBatteryIconConditional(): Boolean = getBoolean(KEY_ISLAND_BATTERY_ICON_CONDITIONAL, false)
    fun setIslandBatteryIconConditional(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_ICON_CONDITIONAL, enabled)
    fun isIslandBatteryPercentageConditional(): Boolean = getBoolean(KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL, false)

    fun isIslandBatteryOnlyLowEnabled(): Boolean = getBoolean(KEY_ISLAND_BATTERY_ONLY_LOW, false)
    fun setIslandBatteryOnlyLowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_ONLY_LOW, enabled)

    fun isIslandDevicesBatteryOnlyLowEnabled(): Boolean = getBoolean(KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW, false)
    fun setIslandDevicesBatteryOnlyLowEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW, enabled)
    fun setIslandBatteryPercentageConditional(enabled: Boolean) = putBoolean(KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL, enabled)

    fun getIslandCalendarEmojis(): Map<Long, String> =
        try {
            val json = org.json.JSONObject(getString(KEY_ISLAND_CALENDAR_EMOJIS, "{}") ?: "{}")
            json.keys().asSequence().mapNotNull { key -> key.toLongOrNull()?.let { it to json.getString(key) } }.toMap()
        } catch (_: Exception) {
            emptyMap()
        }

    fun setIslandCalendarEmoji(calendarId: Long, emoji: String?) {
        val updated = getIslandCalendarEmojis().toMutableMap()
        if (emoji.isNullOrBlank()) updated.remove(calendarId) else updated[calendarId] = emoji
        val json = org.json.JSONObject()
        updated.forEach { (id, value) -> json.put(id.toString(), value) }
        putString(KEY_ISLAND_CALENDAR_EMOJIS, json.toString())
    }

    fun isIslandShowTimersEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_TIMERS, true)
    fun setIslandShowTimersEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_TIMERS, enabled)

    fun isIslandTimersShowScreenRecorderEnabled(): Boolean = getBoolean(KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER, true)
    fun setIslandTimersShowScreenRecorderEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER, enabled)
    fun isIslandTimersFilterAppsEnabled(): Boolean = getBoolean(KEY_ISLAND_TIMERS_FILTER_APPS, false)
    fun setIslandTimersFilterAppsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_TIMERS_FILTER_APPS, enabled)
    fun loadIslandTimersSelectedApps() = loadAppSelection(KEY_ISLAND_TIMERS_SELECTED_APPS)
    fun saveIslandTimersSelectedApps(apps: List<AppSelection>) = saveAppSelection(KEY_ISLAND_TIMERS_SELECTED_APPS, apps)
    fun updateIslandTimersAppSelection(packageName: String, enabled: Boolean) = updateAppSelection(KEY_ISLAND_TIMERS_SELECTED_APPS, packageName, enabled)

    fun isIslandShowNetworkEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_NETWORK, true)
    fun setIslandShowNetworkEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_NETWORK, enabled)
    fun isIslandNetworkActivityEnabled(): Boolean = getBoolean(KEY_ISLAND_NETWORK_ACTIVITY, true)
    fun setIslandNetworkActivityEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NETWORK_ACTIVITY, enabled)
    fun isIslandSignalLowOnlyEnabled(): Boolean = getBoolean(KEY_ISLAND_SIGNAL_LOW_ONLY, false)
    fun setIslandSignalLowOnlyEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SIGNAL_LOW_ONLY, enabled)
    fun getIslandPriorityOrder(): List<String>? =
        getString(KEY_ISLAND_PRIORITY_ORDER, null)?.split(",")?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }
    fun setIslandPriorityOrder(order: List<String>?) = putString(KEY_ISLAND_PRIORITY_ORDER, order?.joinToString(","))
    fun isIslandSignalWifiEnabled(): Boolean = getBoolean(KEY_ISLAND_SIGNAL_WIFI, false)
    fun setIslandSignalWifiEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SIGNAL_WIFI, enabled)
    fun isIslandShowSignalEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_SIGNAL, false)
    fun setIslandShowSignalEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_SIGNAL, enabled)
    fun isIslandSignalShowModeEnabled(): Boolean = getBoolean(KEY_ISLAND_SIGNAL_SHOW_MODE, false)
    fun setIslandSignalShowModeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SIGNAL_SHOW_MODE, enabled)
    fun getIslandSignalNetworkTypes(): Set<NetworkType> =
        (getString(KEY_ISLAND_SIGNAL_NETWORK_TYPES, "NETWORK_5G,NETWORK_4G,NETWORK_3G,NETWORK_OTHER") ?: "")
            .split(",")
            .mapNotNull { name -> NetworkType.entries.firstOrNull { it.name == name } }
            .toSet()
    fun setIslandSignalNetworkTypes(types: Set<NetworkType>) = putString(KEY_ISLAND_SIGNAL_NETWORK_TYPES, types.joinToString(",") { it.name })

    fun isIslandShowSoundModeEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_SOUND_MODE, true)
    fun setIslandShowSoundModeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_SOUND_MODE, enabled)

    fun isIslandSoundModeKeepIconEnabled(): Boolean = getBoolean(KEY_ISLAND_SOUND_MODE_KEEP_ICON, true)
    fun setIslandSoundModeKeepIconEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SOUND_MODE_KEEP_ICON, enabled)

    fun isIslandShowAlarmEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_ALARM, false)
    fun setIslandShowAlarmEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_ALARM, enabled)

    fun getIslandAlarmWindowHours(): Int = getInt(KEY_ISLAND_ALARM_WINDOW_HOURS, 12)
    fun setIslandAlarmWindowHours(hours: Int) = putInt(KEY_ISLAND_ALARM_WINDOW_HOURS, hours)

    fun isIslandShowCaffeinateEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CAFFEINATE, true)
    fun setIslandShowCaffeinateEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CAFFEINATE, enabled)
    fun isIslandCaffeinateShowTimeEnabled(): Boolean = getBoolean(KEY_ISLAND_CAFFEINATE_SHOW_TIME, false)
    fun setIslandCaffeinateShowTimeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_CAFFEINATE_SHOW_TIME, enabled)

    fun isIslandShowDevicesEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_DEVICES, true)
    fun setIslandShowDevicesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_DEVICES, enabled)

    fun isIslandShowNavigationEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_NAVIGATION, true)
    fun setIslandShowNavigationEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_NAVIGATION, enabled)

    fun getIslandNavigationOnNew(): String =
        getString(KEY_ISLAND_NAVIGATION_ON_NEW, ISLAND_NAVIGATION_ON_NEW_PEEK) ?: ISLAND_NAVIGATION_ON_NEW_PEEK
    fun setIslandNavigationOnNew(mode: String) = putString(KEY_ISLAND_NAVIGATION_ON_NEW, mode)

    fun isIslandNavigationPeekAfterExpandEnabled(): Boolean = getBoolean(KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND, false)
    fun setIslandNavigationPeekAfterExpandEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND, enabled)

    fun getIslandNavigationExpandDistance(): Int = getInt(KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE, 200)
    fun setIslandNavigationExpandDistance(meters: Int) = putInt(KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE, meters)

    fun isIslandShowHotspotEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_HOTSPOT, false)
    fun setIslandShowHotspotEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_HOTSPOT, enabled)

    fun isIslandBriefEnabled(): Boolean = getBoolean(KEY_ISLAND_BRIEF_ENABLED, false)
    fun setIslandBriefEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BRIEF_ENABLED, enabled)

    fun isIslandBriefShowAlarmEnabled(): Boolean = getBoolean(KEY_ISLAND_BRIEF_SHOW_ALARM, true)
    fun setIslandBriefShowAlarmEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BRIEF_SHOW_ALARM, enabled)

    // Ordered addresses; the first connected one with a battery reading is shown
    fun getIslandDevicesBatteryOrder(): List<String> =
        getString(KEY_ISLAND_DEVICES_BATTERY_ORDER, "").orEmpty().split(',').filter { it.isNotBlank() }
    fun setIslandDevicesBatteryOrder(addresses: List<String>) = putString(KEY_ISLAND_DEVICES_BATTERY_ORDER, addresses.joinToString(","))

    fun isIslandShowCallsEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_CALLS, true)
    fun setIslandShowCallsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_CALLS, enabled)

    fun getIslandCameraPosition(): String =
        getCameraPlacementString(KEY_ISLAND_CAMERA_POSITION, ISLAND_CAMERA_POSITION_CENTER, isIslandOrientationProfilesEnabled())
    fun setIslandCameraPosition(value: String) =
        setCameraPlacementString(KEY_ISLAND_CAMERA_POSITION, value, isIslandOrientationProfilesEnabled())

    fun isIslandPreviewRingEnabled(): Boolean = getBoolean(KEY_ISLAND_PREVIEW_RING, false)
    fun setIslandPreviewRingEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_PREVIEW_RING, enabled)

    fun getIslandPreviewStage(): String =
        getString(KEY_ISLAND_PREVIEW_STAGE, ISLAND_PREVIEW_STAGE_AUTO) ?: ISLAND_PREVIEW_STAGE_AUTO
    fun setIslandPreviewStage(value: String) = putString(KEY_ISLAND_PREVIEW_STAGE, value)

    fun getIslandExpandedScale(): Float = getFloat(KEY_ISLAND_EXPANDED_SCALE, 1f)
    fun setIslandExpandedScale(value: Float) = putFloat(KEY_ISLAND_EXPANDED_SCALE, value)

    fun isIslandHideLiveUpdatesEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_LIVE_UPDATES, false)
    fun setIslandHideLiveUpdatesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_LIVE_UPDATES, enabled)
    fun isIslandHideStatusBarInsetsEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_STATUS_BAR_INSETS, false)
    fun setIslandHideStatusBarInsetsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_STATUS_BAR_INSETS, enabled)
    fun isIslandDismissOnOutsideEnabled(): Boolean = getBoolean(KEY_ISLAND_DISMISS_ON_OUTSIDE, false)
    fun setIslandDismissOnOutsideEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_DISMISS_ON_OUTSIDE, enabled)

    fun isIslandHideInOwnerAppEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_IN_OWNER_APP, false)
    fun setIslandHideInOwnerAppEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_IN_OWNER_APP, enabled)

    fun isIslandKeepOnLandscapeEnabled(): Boolean = getBoolean(KEY_ISLAND_KEEP_ON_LANDSCAPE, false)

    fun setIslandKeepOnLandscapeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_KEEP_ON_LANDSCAPE, enabled)

    fun isIslandKeepInFullscreenEnabled(): Boolean = getBoolean(KEY_ISLAND_KEEP_IN_FULLSCREEN, false)

    fun setIslandKeepInFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_KEEP_IN_FULLSCREEN, enabled)

    fun isIslandDisableInteractionsInFullscreenEnabled(): Boolean = getBoolean(KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN, false)

    fun setIslandDisableInteractionsInFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN, enabled)

    fun isIslandAlwaysGesturesEnabled(): Boolean = getBoolean(KEY_ISLAND_ALWAYS_GESTURES, false)

    fun setIslandAlwaysGesturesEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_ALWAYS_GESTURES, enabled)

    fun getIslandMaxItems(): Int = getInt(KEY_ISLAND_MAX_ITEMS, 2).coerceIn(1, 4)

    fun setIslandMaxItems(value: Int) = putInt(KEY_ISLAND_MAX_ITEMS, value)

    fun isIslandBondEdgeEnabled(): Boolean = getBoolean(KEY_ISLAND_BOND_EDGE, false)

    fun setIslandBondEdgeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BOND_EDGE, enabled)

    fun isIslandBalanceCompactEnabled(): Boolean = getBoolean(KEY_ISLAND_BALANCE_COMPACT, true)

    fun setIslandBalanceCompactEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_BALANCE_COMPACT, enabled)

    fun isIslandSecondaryBubbleEnabled(): Boolean = getBoolean(KEY_ISLAND_SECONDARY_BUBBLE, true)

    fun setIslandSecondaryBubbleEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SECONDARY_BUBBLE, enabled)

    fun getIslandLandscapeTopSpacing(): Float = getFloat(KEY_ISLAND_LANDSCAPE_TOP_SPACING, 0f)

    fun setIslandLandscapeTopSpacing(value: Float) = putFloat(KEY_ISLAND_LANDSCAPE_TOP_SPACING, value)

    fun isIslandHideOnShadeEnabled(): Boolean = getBoolean(KEY_ISLAND_HIDE_ON_SHADE, false)

    fun setIslandHideOnShadeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_HIDE_ON_SHADE, enabled)

    fun getIslandFontScale(): Float = getFloat(KEY_ISLAND_FONT_SCALE, 1f)
    fun setIslandFontScale(value: Float) = putFloat(KEY_ISLAND_FONT_SCALE, value)

    fun isIslandLineStageEnabled(): Boolean = getBoolean(KEY_ISLAND_LINE_STAGE_ENABLED, false)
    fun setIslandLineStageEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_LINE_STAGE_ENABLED, enabled)

    fun getIslandPeekDurationMs(): Long = getLong(KEY_ISLAND_PEEK_DURATION_MS, 3500L)
    fun setIslandPeekDurationMs(value: Long) = putLong(KEY_ISLAND_PEEK_DURATION_MS, value)

    fun isIslandMediaPeekSongChangeEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE, true)
    fun setIslandMediaPeekSongChangeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE, enabled)

    fun isIslandMediaKeepWhenPausedEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED, true)
    fun setIslandMediaKeepWhenPausedEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED, enabled)

    fun isIslandMediaShowPreviousEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_SHOW_PREVIOUS, false)
    fun setIslandMediaShowPreviousEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_SHOW_PREVIOUS, enabled)

    fun isIslandMediaShowLikeEnabled(): Boolean = getBoolean(KEY_ISLAND_MEDIA_SHOW_LIKE, true)
    fun setIslandMediaShowLikeEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_MEDIA_SHOW_LIKE, enabled)

    fun isIslandShowNotificationsEnabled(): Boolean = getBoolean(KEY_ISLAND_SHOW_NOTIFICATIONS, true)
    fun setIslandShowNotificationsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_SHOW_NOTIFICATIONS, enabled)

    fun isIslandNotifHubIncludeSilent(): Boolean = getBoolean(KEY_ISLAND_NOTIF_HUB_SILENT, false)
    fun setIslandNotifHubIncludeSilent(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_HUB_SILENT, enabled)

    fun isIslandNotifHubGroupByApps(): Boolean = getBoolean(KEY_ISLAND_NOTIF_HUB_GROUP, false)
    fun setIslandNotifHubGroupByApps(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_HUB_GROUP, enabled)

    fun getIslandNotifHubOrder(): String =
        getString(KEY_ISLAND_NOTIF_HUB_ORDER, null)
            ?.takeIf { it == ISLAND_NOTIF_HUB_ORDER_PRIORITY || it == ISLAND_NOTIF_HUB_ORDER_RECENT }
            ?: ISLAND_NOTIF_HUB_ORDER_DYNAMIC

    fun setIslandNotifHubOrder(value: String) = putString(KEY_ISLAND_NOTIF_HUB_ORDER, value)

    // 0 = fit to the available width
    fun getIslandNotifHubIcons(): Int = getInt(KEY_ISLAND_NOTIF_HUB_ICONS, 0).coerceIn(0, 8)
    fun setIslandNotifHubIcons(value: Int) = putInt(KEY_ISLAND_NOTIF_HUB_ICONS, value.coerceIn(0, 8))

    fun isIslandNotifHubEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_HUB, false)
    fun setIslandNotifHubEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_HUB, enabled)

    fun isIslandNotifQueueEnabled(): Boolean = isIslandNotifHubEnabled() || getBoolean(KEY_ISLAND_NOTIF_QUEUE, true)
    fun setIslandNotifQueueEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_QUEUE, enabled)

    fun getIslandNotifQueueStyle(): String =
        if (getString(KEY_ISLAND_NOTIF_QUEUE_STYLE, null) == ISLAND_NOTIF_QUEUE_STYLE_OVERLAP) ISLAND_NOTIF_QUEUE_STYLE_OVERLAP
        else ISLAND_NOTIF_QUEUE_STYLE_STACK
    fun setIslandNotifQueueStyle(style: String) = putString(KEY_ISLAND_NOTIF_QUEUE_STYLE, style)

    fun isIslandNotifQueueInlineEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_QUEUE_INLINE, false)
    fun setIslandNotifQueueInlineEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_QUEUE_INLINE, enabled)

    fun isIslandNotifSkipSilentEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_SKIP_SILENT, true)
    fun setIslandNotifSkipSilentEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_SKIP_SILENT, enabled)

    fun isIslandNotifFollowDndEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_FOLLOW_DND, true)
    fun setIslandNotifFollowDndEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_FOLLOW_DND, enabled)

    fun isIslandNotifFilterAppsEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_FILTER_APPS, false)
    fun setIslandNotifFilterAppsEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_FILTER_APPS, enabled)

    fun loadIslandNotifFilterApps() = loadAppSelection(KEY_ISLAND_NOTIF_FILTER_APPS_LIST)
    fun saveIslandNotifFilterApps(apps: List<AppSelection>) = saveAppSelection(KEY_ISLAND_NOTIF_FILTER_APPS_LIST, apps)
    fun updateIslandNotifFilterAppSelection(packageName: String, enabled: Boolean) =
        updateAppSelection(KEY_ISLAND_NOTIF_FILTER_APPS_LIST, packageName, enabled)

    fun isIslandNotifTapToOpenEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_TAP_TO_OPEN, false)
    fun setIslandNotifTapToOpenEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_TAP_TO_OPEN, enabled)

    fun isIslandOtpEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_ENABLED, false)
    fun setIslandOtpEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_OTP_ENABLED, enabled)
    fun isIslandOtpTapToCopyEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_TAP_TO_COPY, true)
    fun isIslandOtpDismissAfterCopyEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_DISMISS_AFTER_COPY, false)
    fun isIslandOtpBypassFilterEnabled(): Boolean = getBoolean(KEY_ISLAND_OTP_BYPASS_FILTER, true)
    fun getIslandOtpTimeoutMs(): Long = getLong(KEY_ISLAND_OTP_TIMEOUT_MS, 60_000L)
    fun setIslandOtpTimeoutMs(ms: Long) = putLong(KEY_ISLAND_OTP_TIMEOUT_MS, ms)


    fun getIslandNotifPeekStyle(): String =
        when (getString(KEY_ISLAND_NOTIF_PEEK_STYLE, null)) {
            ISLAND_NOTIF_PEEK_STYLE_COMPACT -> ISLAND_NOTIF_PEEK_STYLE_COMPACT
            null -> if (getBoolean(KEY_ISLAND_NOTIF_CONDENSED_INFO, false)) ISLAND_NOTIF_PEEK_STYLE_COMPACT else ISLAND_NOTIF_PEEK_STYLE_FULL
            else -> ISLAND_NOTIF_PEEK_STYLE_FULL
        }
    fun setIslandNotifPeekStyle(style: String) = putString(KEY_ISLAND_NOTIF_PEEK_STYLE, style)

    fun isIslandNotifKeepProgressEnabled(): Boolean = getBoolean(KEY_ISLAND_NOTIF_KEEP_PROGRESS, true)
    fun setIslandNotifKeepProgressEnabled(enabled: Boolean) = putBoolean(KEY_ISLAND_NOTIF_KEEP_PROGRESS, enabled)

    fun applyHeadsUpSuppression(suppress: Boolean = isIslandEnabled() && isIslandSuppressSystemHeadsUpEnabled()) {
        val targetValue = if (suppress) 0 else 1
        try {
            android.provider.Settings.Global.putInt(
                context.contentResolver,
                "heads_up_notifications_enabled",
                targetValue,
            )
        } catch (_: SecurityException) {
            ShellUtils.runCommand(
                context,
                "settings put global heads_up_notifications_enabled $targetValue",
                featureName = context.getString(com.jawzf.pixeltoolkit.core.design.R.string.island_suppress_system_heads_up_title),
            )
        } catch (_: Exception) {}
    }

    // Status Glance
    fun isStatusGlanceEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_ENABLED, false)
    fun setStatusGlanceEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_ENABLED, enabled)

    fun isStatusGlanceAutoDetectEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_USE_AUTO_DETECT, true)
    fun setStatusGlanceAutoDetectEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_USE_AUTO_DETECT, enabled)

    fun getStatusGlanceOffsetX(): Float = getCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_X, 60f, isStatusGlanceOrientationProfilesEnabled())
    fun setStatusGlanceOffsetX(value: Float) = setCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_X, value, isStatusGlanceOrientationProfilesEnabled())

    fun getStatusGlanceOffsetY(): Float = getCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_Y, 2f, isStatusGlanceOrientationProfilesEnabled())
    fun setStatusGlanceOffsetY(value: Float) = setCameraPlacementFloat(KEY_STATUS_GLANCE_OFFSET_Y, value, isStatusGlanceOrientationProfilesEnabled())

    fun getStatusGlanceMaxWidth(): Float = getFloat(KEY_STATUS_GLANCE_MAX_WIDTH, 180f)
    fun setStatusGlanceMaxWidth(value: Float) = putFloat(KEY_STATUS_GLANCE_MAX_WIDTH, value)

    fun getStatusGlanceFontSize(): Float = getFloat(KEY_STATUS_GLANCE_FONT_SIZE, 13f)
    fun setStatusGlanceFontSize(value: Float) = putFloat(KEY_STATUS_GLANCE_FONT_SIZE, value)

    fun isStatusGlanceShowFlashlightEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_FLASHLIGHT, true)
    fun setStatusGlanceShowFlashlightEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_FLASHLIGHT, enabled)

    fun isStatusGlanceShowCalendarEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_CALENDAR, true)
    fun setStatusGlanceShowCalendarEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_CALENDAR, enabled)

    fun getStatusGlanceCalendarTimeframe(): String = getString(KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME, "today") ?: "today"
    fun setStatusGlanceCalendarTimeframe(timeframe: String) = putString(KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME, timeframe)

    fun getIslandCalendarPriorityMinutes(): Int = prefs.getInt(KEY_ISLAND_CALENDAR_PRIORITY_MINUTES, 10)
    fun setIslandCalendarPriorityMinutes(minutes: Int) = prefs.edit().putInt(KEY_ISLAND_CALENDAR_PRIORITY_MINUTES, minutes).apply()

    fun isStatusGlanceCalendarShowAllDayEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY, false)
    fun setStatusGlanceCalendarShowAllDayEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY, enabled)

    fun getStatusGlanceCalendarSelectedCalendars(): Set<String> {
        val json = prefs.getString(KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, Array<String>::class.java).toSet()
            } catch (e: Exception) {
                emptySet()
            }
        } else {
            emptySet()
        }
    }

    fun saveStatusGlanceCalendarSelectedCalendars(calendarIds: Set<String>) {
        val json = gson.toJson(calendarIds)
        putString(KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS, json)
    }

    fun isStatusGlanceShowMediaEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_MEDIA, true)
    fun setStatusGlanceShowMediaEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_MEDIA, enabled)

    fun isStatusGlanceShowTimeEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_TIME, true)
    fun setStatusGlanceShowTimeEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_TIME, enabled)

    fun isStatusGlanceBackgroundPillEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BACKGROUND_PILL, false)
    fun setStatusGlanceBackgroundPillEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BACKGROUND_PILL, enabled)

    fun isStatusGlanceAlbumArtColorsEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_ALBUM_ART_COLORS, true)
    fun setStatusGlanceAlbumArtColorsEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_ALBUM_ART_COLORS, enabled)

    fun isStatusGlanceHideWhenFullscreenEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN, true)
    fun setStatusGlanceHideWhenFullscreenEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN, enabled)

    fun isStatusGlanceHideInQuickSettingsEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS, true)
    fun setStatusGlanceHideInQuickSettingsEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS, enabled)

    fun isStatusGlanceHideWhenLockedEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED, true)
    fun setStatusGlanceHideWhenLockedEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED, enabled)

    fun isStatusGlanceBoldTextEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BOLD_TEXT, false)
    fun setStatusGlanceBoldTextEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BOLD_TEXT, enabled)

    fun isStatusGlanceShowBatteryEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_SHOW_BATTERY, false)
    fun setStatusGlanceShowBatteryEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_SHOW_BATTERY, enabled)

    fun getStatusGlanceBatteryDisplayMode(): String = getString(KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE, "icon") ?: "icon"
    fun setStatusGlanceBatteryDisplayMode(mode: String) = putString(KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE, mode)

    fun isStatusGlanceBatteryShowLowEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_LOW, true)
    fun setStatusGlanceBatteryShowLowEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_LOW, enabled)

    fun isStatusGlanceBatteryShowChargingEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING, true)
    fun setStatusGlanceBatteryShowChargingEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING, enabled)

    fun isStatusGlanceBatteryShowFullEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_FULL, true)
    fun setStatusGlanceBatteryShowFullEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_FULL, enabled)

    fun isStatusGlanceBatteryShowOtherwiseEnabled(): Boolean = getBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE, true)
    fun setStatusGlanceBatteryShowOtherwiseEnabled(enabled: Boolean) = putBoolean(KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE, enabled)

    fun getStatusGlanceBatteryIconSize(): Float = getFloat(KEY_STATUS_GLANCE_BATTERY_ICON_SIZE, 16f)
    fun setStatusGlanceBatteryIconSize(size: Float) = putFloat(KEY_STATUS_GLANCE_BATTERY_ICON_SIZE, size)

    fun getStatusGlanceTapAction(): Action? = getRemapAction(KEY_STATUS_GLANCE_TAP_ACTION)
    fun setStatusGlanceTapAction(action: Action?) = setRemapAction(KEY_STATUS_GLANCE_TAP_ACTION, action)

    fun getStatusGlanceDoubleTapAction(): Action? = getRemapAction(KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION)
    fun setStatusGlanceDoubleTapAction(action: Action?) = setRemapAction(KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION, action)

    fun getStatusGlanceLongPressAction(): Action? = getRemapAction(KEY_STATUS_GLANCE_LONG_PRESS_ACTION)
    fun setStatusGlanceLongPressAction(action: Action?) = setRemapAction(KEY_STATUS_GLANCE_LONG_PRESS_ACTION, action)

    // Home screen shortcuts only carry an id; the action itself never leaves the app
    fun getPinnedAction(id: String): Action? = getRemapAction(KEY_PINNED_ACTION_PREFIX + id)

    fun savePinnedAction(
        id: String,
        action: Action,
    ) = setRemapAction(KEY_PINNED_ACTION_PREFIX + id, action)
    fun isLockscreenShortcutsEnabled(): Boolean = getBoolean(KEY_LOCKSCREEN_SHORTCUTS_ENABLED, false)

    fun setLockscreenShortcutsEnabled(enabled: Boolean) = putBoolean(KEY_LOCKSCREEN_SHORTCUTS_ENABLED, enabled)

    fun getLockscreenSystemShortcutsState(): SystemShortcutsState =
        SystemShortcutsState.entries.firstOrNull {
            it.name == getString(KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE)
        } ?: SystemShortcutsState.UNKNOWN

    fun setLockscreenSystemShortcutsState(state: SystemShortcutsState) =
        putString(KEY_LOCKSCREEN_SYSTEM_SHORTCUTS_STATE, state.name)
}
