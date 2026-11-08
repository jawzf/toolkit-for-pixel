package com.jawzf.pixeltoolkit.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionGsonAdapter
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.core.platform.shell.RootUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.domain.model.AppIcon
import com.jawzf.pixeltoolkit.domain.model.PowerChoice
import com.jawzf.pixeltoolkit.domain.model.RemapSlot
import com.jawzf.pixeltoolkit.domain.model.ScaleAnimationsProfile
import com.jawzf.pixeltoolkit.domain.model.SystemShortcutsState
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRateUtils
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
import com.jawzf.pixeltoolkit.feature.power.PowerSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SettingsRepository(
    context: Context,
) : IslandSettings(context) {
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
        const val KEY_WIDGET_ENABLED = "widget_enabled"
        const val KEY_STATUS_BAR_ICON_CONTROL_ENABLED = PlatformPrefs.KEY_STATUS_BAR_ICON_CONTROL_ENABLED
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

        const val KEY_FLASHLIGHT_ALWAYS_TURN_OFF_ENABLED = "flashlight_always_turn_off_enabled"
        const val KEY_FLASHLIGHT_FADE_ENABLED = "flashlight_fade_enabled"
        const val KEY_FLASHLIGHT_ADJUST_INTENSITY_ENABLED = "flashlight_adjust_intensity_enabled"
        const val KEY_FLASHLIGHT_GLOBAL_ENABLED = "flashlight_global_enabled"
        const val KEY_FLASHLIGHT_LIVE_UPDATE_ENABLED = "flashlight_live_update_enabled"
        const val KEY_FLASHLIGHT_LAST_INTENSITY = "flashlight_last_intensity"
        const val KEY_FLASHLIGHT_POCKET_TURN_OFF_ENABLED = "flashlight_pocket_turn_off_enabled"
        const val KEY_FLASHLIGHT_OVERHEAT_PREVENTION_ENABLED =
            "flashlight_overheat_prevention_enabled"

        const val KEY_HIDE_SYSTEM_ICONS = PlatformPrefs.KEY_HIDE_SYSTEM_ICONS
        const val KEY_HIDE_SYSTEM_ICONS_LOCKED_ONLY = PlatformPrefs.KEY_HIDE_SYSTEM_ICONS_LOCKED_ONLY
        const val KEY_HIDE_CLOCK = PlatformPrefs.KEY_HIDE_CLOCK
        const val KEY_HIDE_NOTIFICATION_ICONS = PlatformPrefs.KEY_HIDE_NOTIFICATION_ICONS
        const val KEY_HIDE_GESTURE_BAR_ENABLED = PlatformPrefs.KEY_HIDE_GESTURE_BAR_ENABLED
        const val KEY_HIDE_GESTURE_BAR_ON_LAUNCHER_ENABLED = "hide_gesture_bar_on_launcher_enabled"
        const val KEY_CIRCLE_TO_SEARCH_GESTURE_ENABLED = "circle_to_search_gesture_enabled"
        const val KEY_CIRCLE_TO_SEARCH_GESTURE_HEIGHT = "circle_to_search_gesture_height"
        const val KEY_CIRCLE_TO_SEARCH_GESTURE_WIDTH = "circle_to_search_gesture_width"
        const val KEY_CIRCLE_TO_SEARCH_PREVIEW_ENABLED = "circle_to_search_preview_enabled"

        const val KEY_HIDDEN_DEBUGGING_SUPPORT = PlatformPrefs.KEY_HIDDEN_DEBUGGING_SUPPORT
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

        const val KEY_FONT_SCALE = "font_scale"
        const val KEY_FONT_WEIGHT = "font_weight"
        const val KEY_ANIMATOR_DURATION_SCALE = "animator_duration_scale"
        const val KEY_TRANSITION_ANIMATION_SCALE = "transition_animation_scale"
        const val KEY_WINDOW_ANIMATION_SCALE = "window_animation_scale"
        const val KEY_SMALLEST_WIDTH = "smallest_width"
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

        // Island keys: aliases until the island callers move (tk38)
        const val KEY_ISLAND_ENABLED = IslandSettings.KEY_ISLAND_ENABLED
        const val KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED = IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR_ENABLED
        const val KEY_ISLAND_BATTERY_IDLE_COLOR = IslandSettings.KEY_ISLAND_BATTERY_IDLE_COLOR
        const val KEY_ISLAND_USE_AUTO_DETECT = IslandSettings.KEY_ISLAND_USE_AUTO_DETECT
        const val KEY_ISLAND_CAMERA_OFFSET_X = IslandSettings.KEY_ISLAND_CAMERA_OFFSET_X
        const val KEY_ISLAND_CAMERA_OFFSET_Y = IslandSettings.KEY_ISLAND_CAMERA_OFFSET_Y
        const val KEY_ISLAND_CAMERA_SIZE = IslandSettings.KEY_ISLAND_CAMERA_SIZE
        const val KEY_ISLAND_MAX_WIDTH = IslandSettings.KEY_ISLAND_MAX_WIDTH
        const val KEY_ISLAND_CUTOUT_GAP = IslandSettings.KEY_ISLAND_CUTOUT_GAP
        const val KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP = IslandSettings.KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP
        const val KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR = IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR
        const val KEY_ISLAND_COMPACT_HIDE_STATUS_BAR = IslandSettings.KEY_ISLAND_COMPACT_HIDE_STATUS_BAR
        const val KEY_ISLAND_HIDE_ICONS_ENABLED = IslandSettings.KEY_ISLAND_HIDE_ICONS_ENABLED
        const val KEY_ISLAND_HIDE_WHEN_SCREEN_OFF = IslandSettings.KEY_ISLAND_HIDE_WHEN_SCREEN_OFF
        const val KEY_ISLAND_SHOW_WHEN = IslandSettings.KEY_ISLAND_SHOW_WHEN
        const val ISLAND_SHOW_WHEN_UNLOCKED = IslandSettings.ISLAND_SHOW_WHEN_UNLOCKED
        const val ISLAND_SHOW_WHEN_SCREEN_ON = IslandSettings.ISLAND_SHOW_WHEN_SCREEN_ON
        const val ISLAND_SHOW_WHEN_ALWAYS = IslandSettings.ISLAND_SHOW_WHEN_ALWAYS
        const val KEY_ISLAND_NOTIF_CONCEAL_LOCKED = IslandSettings.KEY_ISLAND_NOTIF_CONCEAL_LOCKED
        const val KEY_ISLAND_NOTIF_DISABLE_LOCKED = IslandSettings.KEY_ISLAND_NOTIF_DISABLE_LOCKED
        const val KEY_ISLAND_NOTIF_CONCEAL_CHAT_PICTURES = IslandSettings.KEY_ISLAND_NOTIF_CONCEAL_CHAT_PICTURES
        const val KEY_ISLAND_CALENDAR_HIDE_LOCKED = IslandSettings.KEY_ISLAND_CALENDAR_HIDE_LOCKED
        const val KEY_ISLAND_TIME_HIDE_LOCKED = IslandSettings.KEY_ISLAND_TIME_HIDE_LOCKED
        const val KEY_ISLAND_BATTERY_HIDE_LOCKED = IslandSettings.KEY_ISLAND_BATTERY_HIDE_LOCKED
        const val KEY_ISLAND_TIMEOUT_MS = IslandSettings.KEY_ISLAND_TIMEOUT_MS
        const val KEY_ISLAND_SHOW_GLOW = IslandSettings.KEY_ISLAND_SHOW_GLOW
        const val KEY_ISLAND_EXPANDED_WIDTH = IslandSettings.KEY_ISLAND_EXPANDED_WIDTH
        const val KEY_ISLAND_EXPANDED_ROUNDNESS = IslandSettings.KEY_ISLAND_EXPANDED_ROUNDNESS
        const val KEY_ISLAND_EXPANDED_PADDING = IslandSettings.KEY_ISLAND_EXPANDED_PADDING
        const val KEY_ISLAND_EXPANDED_TOP_PADDING = IslandSettings.KEY_ISLAND_EXPANDED_TOP_PADDING
        const val KEY_ISLAND_EXPANDED_BOTTOM_PADDING = IslandSettings.KEY_ISLAND_EXPANDED_BOTTOM_PADDING
        const val KEY_ISLAND_EXPANDED_TIMEOUT_MS = IslandSettings.KEY_ISLAND_EXPANDED_TIMEOUT_MS
        const val KEY_ISLAND_CATCH_UP_ENABLED = IslandSettings.KEY_ISLAND_CATCH_UP_ENABLED
        const val KEY_ISLAND_CATCH_UP_TIMEOUT_MS = IslandSettings.KEY_ISLAND_CATCH_UP_TIMEOUT_MS
        const val KEY_ISLAND_SHOW_MEDIA = IslandSettings.KEY_ISLAND_SHOW_MEDIA
        const val KEY_ISLAND_MEDIA_EXCLUDED_APPS = IslandSettings.KEY_ISLAND_MEDIA_EXCLUDED_APPS
        const val KEY_ISLAND_SHOW_CALENDAR = IslandSettings.KEY_ISLAND_SHOW_CALENDAR
        const val KEY_ISLAND_SHOW_CONSCIOUS_GATE = IslandSettings.KEY_ISLAND_SHOW_CONSCIOUS_GATE
        const val KEY_ISLAND_SHOW_TIME_BATTERY = IslandSettings.KEY_ISLAND_SHOW_TIME_BATTERY
        const val KEY_ISLAND_TIME_SHOW_SECONDS = IslandSettings.KEY_ISLAND_TIME_SHOW_SECONDS
        const val KEY_ISLAND_THEMED_TEXT = IslandSettings.KEY_ISLAND_THEMED_TEXT
        const val KEY_ISLAND_ICON_COLOR_MODE = IslandSettings.KEY_ISLAND_ICON_COLOR_MODE
        const val KEY_ISLAND_ICON_COLOR_CUSTOM = IslandSettings.KEY_ISLAND_ICON_COLOR_CUSTOM
        const val KEY_ISLAND_ICON_MATERIAL_YOU = IslandSettings.KEY_ISLAND_ICON_MATERIAL_YOU
        const val ISLAND_ICON_COLOR_DYNAMIC = IslandSettings.ISLAND_ICON_COLOR_DYNAMIC
        const val ISLAND_ICON_COLOR_CUSTOM = IslandSettings.ISLAND_ICON_COLOR_CUSTOM
        const val KEY_ISLAND_TIME_FORMAT = IslandSettings.KEY_ISLAND_TIME_FORMAT
        const val KEY_ISLAND_TIME_PERIOD = IslandSettings.KEY_ISLAND_TIME_PERIOD
        const val KEY_ISLAND_TIME_LEADING_ZERO = IslandSettings.KEY_ISLAND_TIME_LEADING_ZERO
        const val ISLAND_TIME_FORMAT_SYSTEM = IslandSettings.ISLAND_TIME_FORMAT_SYSTEM
        const val ISLAND_TIME_FORMAT_12H = IslandSettings.ISLAND_TIME_FORMAT_12H
        const val ISLAND_TIME_FORMAT_24H = IslandSettings.ISLAND_TIME_FORMAT_24H
        const val ISLAND_TIME_PERIOD_NONE = IslandSettings.ISLAND_TIME_PERIOD_NONE
        const val ISLAND_TIME_PERIOD_AM_PM = IslandSettings.ISLAND_TIME_PERIOD_AM_PM
        const val KEY_ISLAND_SHOW_BATTERY = IslandSettings.KEY_ISLAND_SHOW_BATTERY
        const val KEY_ISLAND_SHOW_FLASHLIGHT = IslandSettings.KEY_ISLAND_SHOW_FLASHLIGHT
        const val KEY_ISLAND_BATTERY_STYLE = IslandSettings.KEY_ISLAND_BATTERY_STYLE
        const val KEY_ISLAND_DEVICES_BATTERY_STYLE = IslandSettings.KEY_ISLAND_DEVICES_BATTERY_STYLE
        const val ISLAND_BATTERY_STYLE_RING = IslandSettings.ISLAND_BATTERY_STYLE_RING
        const val ISLAND_BATTERY_STYLE_ICON = IslandSettings.ISLAND_BATTERY_STYLE_ICON
        const val KEY_ISLAND_BATTERY_PERCENTAGE = IslandSettings.KEY_ISLAND_BATTERY_PERCENTAGE
        const val KEY_ISLAND_LONG_PRESS_ACTION = IslandSettings.KEY_ISLAND_LONG_PRESS_ACTION
        const val KEY_ISLAND_SLIDE_MODE = IslandSettings.KEY_ISLAND_SLIDE_MODE
        const val KEY_ISLAND_SLIDE_TRACK = IslandSettings.KEY_ISLAND_SLIDE_TRACK
        const val KEY_ISLAND_LIKE_WHILE_PLAYING = IslandSettings.KEY_ISLAND_LIKE_WHILE_PLAYING
        const val KEY_ISLAND_SLIDE_INVERT_DIRECTION = IslandSettings.KEY_ISLAND_SLIDE_INVERT_DIRECTION
        const val KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL = IslandSettings.KEY_ISLAND_BATTERY_PERCENTAGE_CONDITIONAL
        const val KEY_ISLAND_BATTERY_ICON_CONDITIONAL = IslandSettings.KEY_ISLAND_BATTERY_ICON_CONDITIONAL
        const val KEY_ISLAND_BATTERY_ONLY_LOW = IslandSettings.KEY_ISLAND_BATTERY_ONLY_LOW
        const val KEY_ISLAND_BATTERY_PEEK_ALERTS = IslandSettings.KEY_ISLAND_BATTERY_PEEK_ALERTS
        const val KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW = IslandSettings.KEY_ISLAND_DEVICES_BATTERY_ONLY_LOW
        const val KEY_ISLAND_EXPANDED_SCALE = IslandSettings.KEY_ISLAND_EXPANDED_SCALE
        const val KEY_ISLAND_FONT_SCALE = IslandSettings.KEY_ISLAND_FONT_SCALE
        const val KEY_ISLAND_HIDE_IN_OWNER_APP = IslandSettings.KEY_ISLAND_HIDE_IN_OWNER_APP
        const val KEY_ISLAND_HIDE_ON_SHADE = IslandSettings.KEY_ISLAND_HIDE_ON_SHADE
        const val KEY_ISLAND_KEEP_ON_LANDSCAPE = IslandSettings.KEY_ISLAND_KEEP_ON_LANDSCAPE
        const val KEY_ISLAND_KEEP_IN_FULLSCREEN = IslandSettings.KEY_ISLAND_KEEP_IN_FULLSCREEN
        const val KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN = IslandSettings.KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN
        const val KEY_ISLAND_LANDSCAPE_TOP_SPACING = IslandSettings.KEY_ISLAND_LANDSCAPE_TOP_SPACING
        const val KEY_ISLAND_BOND_EDGE = IslandSettings.KEY_ISLAND_BOND_EDGE
        const val KEY_ISLAND_BALANCE_COMPACT = IslandSettings.KEY_ISLAND_BALANCE_COMPACT
        const val KEY_ISLAND_SECONDARY_BUBBLE = IslandSettings.KEY_ISLAND_SECONDARY_BUBBLE
        const val KEY_ISLAND_MAX_ITEMS = IslandSettings.KEY_ISLAND_MAX_ITEMS
        const val KEY_ISLAND_ALWAYS_GESTURES = IslandSettings.KEY_ISLAND_ALWAYS_GESTURES
        const val KEY_ISLAND_DISMISS_ON_OUTSIDE = IslandSettings.KEY_ISLAND_DISMISS_ON_OUTSIDE
        const val KEY_ISLAND_HIDE_LIVE_UPDATES = IslandSettings.KEY_ISLAND_HIDE_LIVE_UPDATES
        const val KEY_ISLAND_HIDE_STATUS_BAR_INSETS = IslandSettings.KEY_ISLAND_HIDE_STATUS_BAR_INSETS
        const val KEY_ISLAND_CAMERA_POSITION = IslandSettings.KEY_ISLAND_CAMERA_POSITION
        const val KEY_ISLAND_ORIENTATION_PROFILES = IslandSettings.KEY_ISLAND_ORIENTATION_PROFILES
        const val KEY_ISLAND_HIDE_PORTRAIT = IslandSettings.KEY_ISLAND_HIDE_PORTRAIT
        const val KEY_ISLAND_HIDE_LANDSCAPE = IslandSettings.KEY_ISLAND_HIDE_LANDSCAPE
        const val KEY_ISLAND_PREVIEW_RING = IslandSettings.KEY_ISLAND_PREVIEW_RING
        const val KEY_ISLAND_PREVIEW_STAGE = IslandSettings.KEY_ISLAND_PREVIEW_STAGE
        const val ISLAND_PREVIEW_STAGE_AUTO = IslandSettings.ISLAND_PREVIEW_STAGE_AUTO
        const val ISLAND_PREVIEW_STAGE_PEEK = IslandSettings.ISLAND_PREVIEW_STAGE_PEEK
        const val ISLAND_PREVIEW_STAGE_EXPANDED = IslandSettings.ISLAND_PREVIEW_STAGE_EXPANDED
        const val KEY_ISLAND_SHOW_CALLS = IslandSettings.KEY_ISLAND_SHOW_CALLS
        const val KEY_ISLAND_CALL_SHOW_MODE = IslandSettings.KEY_ISLAND_CALL_SHOW_MODE
        const val KEY_ISLAND_CALL_STICKY = IslandSettings.KEY_ISLAND_CALL_STICKY
        const val ISLAND_CALL_SHOW_EXPANDED = IslandSettings.ISLAND_CALL_SHOW_EXPANDED
        const val ISLAND_CALL_SHOW_PEEK = IslandSettings.ISLAND_CALL_SHOW_PEEK
        const val ISLAND_CALL_SHOW_COMPACT = IslandSettings.ISLAND_CALL_SHOW_COMPACT
        const val KEY_ISLAND_SHOW_TIMERS = IslandSettings.KEY_ISLAND_SHOW_TIMERS
        const val KEY_ISLAND_PROVIDERS = IslandSettings.KEY_ISLAND_PROVIDERS
        const val KEY_ISLAND_PROVIDERS_HIDDEN = IslandSettings.KEY_ISLAND_PROVIDERS_HIDDEN
        const val KEY_ISLAND_PROVIDERS_LAUNCHER_ONLY = IslandSettings.KEY_ISLAND_PROVIDERS_LAUNCHER_ONLY
        const val KEY_ISLAND_PROVIDERS_HIDE_LOCKED = IslandSettings.KEY_ISLAND_PROVIDERS_HIDE_LOCKED

        const val KEY_ISLAND_CALENDAR_EMOJIS = IslandSettings.KEY_ISLAND_CALENDAR_EMOJIS
        const val KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER = IslandSettings.KEY_ISLAND_TIMERS_SHOW_SCREEN_RECORDER
        const val KEY_ISLAND_TIMERS_FILTER_APPS = IslandSettings.KEY_ISLAND_TIMERS_FILTER_APPS
        const val KEY_ISLAND_TIMERS_SELECTED_APPS = IslandSettings.KEY_ISLAND_TIMERS_SELECTED_APPS
        const val KEY_ISLAND_SHOW_NETWORK = IslandSettings.KEY_ISLAND_SHOW_NETWORK
        const val KEY_ISLAND_SHOW_SIGNAL = IslandSettings.KEY_ISLAND_SHOW_SIGNAL
        const val KEY_ISLAND_NETWORK_ACTIVITY = IslandSettings.KEY_ISLAND_NETWORK_ACTIVITY
        const val KEY_ISLAND_SIGNAL_WIFI = IslandSettings.KEY_ISLAND_SIGNAL_WIFI
        const val KEY_ISLAND_PRIORITY_ORDER = IslandSettings.KEY_ISLAND_PRIORITY_ORDER
        const val KEY_ISLAND_SIGNAL_LOW_ONLY = IslandSettings.KEY_ISLAND_SIGNAL_LOW_ONLY
        const val KEY_ISLAND_SIGNAL_NETWORK_TYPES = IslandSettings.KEY_ISLAND_SIGNAL_NETWORK_TYPES
        const val KEY_ISLAND_SIGNAL_SHOW_MODE = IslandSettings.KEY_ISLAND_SIGNAL_SHOW_MODE
        const val KEY_ISLAND_SHOW_SOUND_MODE = IslandSettings.KEY_ISLAND_SHOW_SOUND_MODE
        const val KEY_ISLAND_SOUND_MODE_KEEP_ICON = IslandSettings.KEY_ISLAND_SOUND_MODE_KEEP_ICON
        const val KEY_ISLAND_SHOW_ALARM = IslandSettings.KEY_ISLAND_SHOW_ALARM
        const val KEY_ISLAND_TIME_BATTERY_LAUNCHER_ONLY = IslandSettings.KEY_ISLAND_TIME_BATTERY_LAUNCHER_ONLY
        const val KEY_ISLAND_CALENDAR_LAUNCHER_ONLY = IslandSettings.KEY_ISLAND_CALENDAR_LAUNCHER_ONLY
        const val KEY_ISLAND_ALARM_LAUNCHER_ONLY = IslandSettings.KEY_ISLAND_ALARM_LAUNCHER_ONLY
        const val KEY_ISLAND_ALARM_WINDOW_HOURS = IslandSettings.KEY_ISLAND_ALARM_WINDOW_HOURS
        const val KEY_ISLAND_ALARM_HIDE_TIME = IslandSettings.KEY_ISLAND_ALARM_HIDE_TIME
        const val KEY_ISLAND_SHOW_CAFFEINATE = IslandSettings.KEY_ISLAND_SHOW_CAFFEINATE
        const val KEY_ISLAND_CAFFEINATE_SHOW_TIME = IslandSettings.KEY_ISLAND_CAFFEINATE_SHOW_TIME
        const val KEY_ISLAND_SHOW_DEVICES = IslandSettings.KEY_ISLAND_SHOW_DEVICES
        const val KEY_ISLAND_SHOW_NAVIGATION = IslandSettings.KEY_ISLAND_SHOW_NAVIGATION
        const val KEY_ISLAND_NAVIGATION_ON_NEW = IslandSettings.KEY_ISLAND_NAVIGATION_ON_NEW
        const val KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND = IslandSettings.KEY_ISLAND_NAVIGATION_PEEK_AFTER_EXPAND
        const val ISLAND_NAVIGATION_ON_NEW_EXPAND = IslandSettings.ISLAND_NAVIGATION_ON_NEW_EXPAND
        const val ISLAND_NAVIGATION_ON_NEW_PEEK = IslandSettings.ISLAND_NAVIGATION_ON_NEW_PEEK
        const val ISLAND_NAVIGATION_ON_NEW_OFF = IslandSettings.ISLAND_NAVIGATION_ON_NEW_OFF
        const val KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE = IslandSettings.KEY_ISLAND_NAVIGATION_EXPAND_DISTANCE
        const val KEY_ISLAND_SHOW_HOTSPOT = IslandSettings.KEY_ISLAND_SHOW_HOTSPOT
        const val KEY_ISLAND_BRIEF_ENABLED = IslandSettings.KEY_ISLAND_BRIEF_ENABLED
        const val KEY_ISLAND_BRIEF_SHOW_ALARM = IslandSettings.KEY_ISLAND_BRIEF_SHOW_ALARM
        const val KEY_ISLAND_BRIEF_TWO_LINE_HEADER = IslandSettings.KEY_ISLAND_BRIEF_TWO_LINE_HEADER
        const val KEY_ISLAND_DEVICES_BATTERY_ORDER = IslandSettings.KEY_ISLAND_DEVICES_BATTERY_ORDER
        const val ISLAND_CAMERA_POSITION_LEFT = IslandSettings.ISLAND_CAMERA_POSITION_LEFT
        const val ISLAND_CAMERA_POSITION_CENTER = IslandSettings.ISLAND_CAMERA_POSITION_CENTER
        const val ISLAND_CAMERA_POSITION_RIGHT = IslandSettings.ISLAND_CAMERA_POSITION_RIGHT
        const val KEY_ISLAND_LINE_STAGE_ENABLED = IslandSettings.KEY_ISLAND_LINE_STAGE_ENABLED
        const val KEY_ISLAND_PEEK_DURATION_MS = IslandSettings.KEY_ISLAND_PEEK_DURATION_MS
        const val KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE = IslandSettings.KEY_ISLAND_MEDIA_PEEK_SONG_CHANGE
        const val KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED = IslandSettings.KEY_ISLAND_MEDIA_KEEP_WHEN_PAUSED
        const val KEY_ISLAND_MEDIA_SHOW_PREVIOUS = IslandSettings.KEY_ISLAND_MEDIA_SHOW_PREVIOUS
        const val KEY_ISLAND_MEDIA_SHOW_LIKE = IslandSettings.KEY_ISLAND_MEDIA_SHOW_LIKE
        const val KEY_ISLAND_NOTIF_CONDENSED_INFO = IslandSettings.KEY_ISLAND_NOTIF_CONDENSED_INFO
        const val KEY_ISLAND_NOTIF_PEEK_STYLE = IslandSettings.KEY_ISLAND_NOTIF_PEEK_STYLE
        const val ISLAND_NOTIF_PEEK_STYLE_FULL = IslandSettings.ISLAND_NOTIF_PEEK_STYLE_FULL
        const val ISLAND_NOTIF_PEEK_STYLE_COMPACT = IslandSettings.ISLAND_NOTIF_PEEK_STYLE_COMPACT
        const val ISLAND_NOTIF_PEEK_STYLE_MINIMAL = IslandSettings.ISLAND_NOTIF_PEEK_STYLE_MINIMAL
        const val KEY_ISLAND_NOTIF_KEEP_PROGRESS = IslandSettings.KEY_ISLAND_NOTIF_KEEP_PROGRESS
        const val KEY_ISLAND_NOTIF_QUEUE = IslandSettings.KEY_ISLAND_NOTIF_QUEUE
        const val KEY_ISLAND_NOTIF_QUEUE_STYLE = IslandSettings.KEY_ISLAND_NOTIF_QUEUE_STYLE
        const val ISLAND_NOTIF_QUEUE_STYLE_STACK = IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_STACK
        const val ISLAND_NOTIF_QUEUE_STYLE_OVERLAP = IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_OVERLAP
        const val KEY_ISLAND_NOTIF_QUEUE_INLINE = IslandSettings.KEY_ISLAND_NOTIF_QUEUE_INLINE
        const val KEY_ISLAND_NOTIF_SKIP_SILENT = IslandSettings.KEY_ISLAND_NOTIF_SKIP_SILENT
        const val KEY_ISLAND_NOTIF_FOLLOW_DND = IslandSettings.KEY_ISLAND_NOTIF_FOLLOW_DND
        const val KEY_ISLAND_NOTIF_FILTER_APPS = IslandSettings.KEY_ISLAND_NOTIF_FILTER_APPS
        const val KEY_ISLAND_NOTIF_FILTER_APPS_LIST = IslandSettings.KEY_ISLAND_NOTIF_FILTER_APPS_LIST
        const val KEY_ISLAND_SHOW_NOTIFICATIONS = IslandSettings.KEY_ISLAND_SHOW_NOTIFICATIONS
        const val KEY_ISLAND_NOTIF_HUB = IslandSettings.KEY_ISLAND_NOTIF_HUB
        const val KEY_ISLAND_NOTIF_HUB_ICONS = IslandSettings.KEY_ISLAND_NOTIF_HUB_ICONS
        const val KEY_ISLAND_NOTIF_HUB_ORDER = IslandSettings.KEY_ISLAND_NOTIF_HUB_ORDER
        const val KEY_ISLAND_NOTIF_HUB_SILENT = IslandSettings.KEY_ISLAND_NOTIF_HUB_SILENT
        const val KEY_ISLAND_NOTIF_HUB_GROUP = IslandSettings.KEY_ISLAND_NOTIF_HUB_GROUP
        const val ISLAND_NOTIF_HUB_ORDER_DYNAMIC = IslandSettings.ISLAND_NOTIF_HUB_ORDER_DYNAMIC
        const val ISLAND_NOTIF_HUB_ORDER_PRIORITY = IslandSettings.ISLAND_NOTIF_HUB_ORDER_PRIORITY
        const val ISLAND_NOTIF_HUB_ORDER_RECENT = IslandSettings.ISLAND_NOTIF_HUB_ORDER_RECENT
        const val KEY_ISLAND_NOTIF_TAP_TO_OPEN = IslandSettings.KEY_ISLAND_NOTIF_TAP_TO_OPEN
        const val KEY_ISLAND_OTP_ENABLED = IslandSettings.KEY_ISLAND_OTP_ENABLED
        const val KEY_ISLAND_OTP_TAP_TO_COPY = IslandSettings.KEY_ISLAND_OTP_TAP_TO_COPY
        const val KEY_ISLAND_OTP_DISMISS_AFTER_COPY = IslandSettings.KEY_ISLAND_OTP_DISMISS_AFTER_COPY
        const val KEY_ISLAND_OTP_BYPASS_FILTER = IslandSettings.KEY_ISLAND_OTP_BYPASS_FILTER
        const val KEY_ISLAND_OTP_TIMEOUT_MS = IslandSettings.KEY_ISLAND_OTP_TIMEOUT_MS
        const val KEY_ISLAND_BORDER_OUTLINE_ENABLED = IslandSettings.KEY_ISLAND_BORDER_OUTLINE_ENABLED
        const val KEY_ISLAND_BORDER_OUTLINE_COLOR = IslandSettings.KEY_ISLAND_BORDER_OUTLINE_COLOR
        const val KEY_ISLAND_BORDER_OUTLINE_DYNAMIC = IslandSettings.KEY_ISLAND_BORDER_OUTLINE_DYNAMIC
        const val KEY_ISLAND_BORDER_OUTLINE_THICKNESS = IslandSettings.KEY_ISLAND_BORDER_OUTLINE_THICKNESS
        const val KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED = IslandSettings.KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED
        const val KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION = IslandSettings.KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION
        const val KEY_ISLAND_PULSE_SHADOW_SIZE = IslandSettings.KEY_ISLAND_PULSE_SHADOW_SIZE
        const val KEY_ISLAND_PULSE_SHADOW_Y_SHIFT = IslandSettings.KEY_ISLAND_PULSE_SHADOW_Y_SHIFT
        const val KEY_ISLAND_PULSE_SHADOW_SPREAD = IslandSettings.KEY_ISLAND_PULSE_SHADOW_SPREAD
        const val KEY_ISLAND_PULSE_SHADOW_DURATION_MS = IslandSettings.KEY_ISLAND_PULSE_SHADOW_DURATION_MS
        const val ISLAND_BORDER_OUTLINE_DEFAULT_COLOR = IslandSettings.ISLAND_BORDER_OUTLINE_DEFAULT_COLOR

        const val KEY_ISLAND_CALENDAR_PRIORITY_MINUTES = IslandSettings.KEY_ISLAND_CALENDAR_PRIORITY_MINUTES

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

    fun isShowLegacyFeatures(): Boolean = getBoolean(KEY_SHOW_LEGACY_FEATURES, true)

    fun setShowLegacyFeatures(enabled: Boolean) = putBoolean(KEY_SHOW_LEGACY_FEATURES, enabled)

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
            val display = DisplaySettings(context)
            if (display.contains(DisplaySettings.KEY_REFRESH_RATE_FIXED) ||
                display.contains(DisplaySettings.KEY_REFRESH_RATE_MIN) ||
                display.contains(
                    DisplaySettings.KEY_REFRESH_RATE_PEAK,
                )
            ) {
                val mode = display.getRefreshRateMode()
                val fixed = display.getFloat(DisplaySettings.KEY_REFRESH_RATE_FIXED, 0f)
                val min = display.getFloat(DisplaySettings.KEY_REFRESH_RATE_MIN, 0f)
                val peak = display.getFloat(DisplaySettings.KEY_REFRESH_RATE_PEAK, 0f)

                if (fixed <= 0f && min <= 0f && peak <= 0f) {
                    RefreshRateUtils.resetRefreshRate(
                        context,
                        display.shouldRestoreInfinityPeakOnRefreshRateReset(),
                    )
                } else if (mode == RefreshRateUtils.MODE_RANGE && min > 0f && peak > 0f) {
                    RefreshRateUtils.applyRangeRefreshRate(
                        context,
                        min,
                        peak,
                    )
                } else if (fixed > 0f || peak > 0f) {
                    RefreshRateUtils.applyFixedRefreshRate(
                        context,
                        if (fixed > 0f) fixed else peak,
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Notification Glance Settings

    fun getScaleAnimationsMode(): String = getString(KEY_SCALE_ANIMATIONS_MODE, "default") ?: "default"

    /**
     * Executes the set scale animations mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setScaleAnimationsMode(mode: String) = putString(KEY_SCALE_ANIMATIONS_MODE, mode)

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
