package com.jawzf.pixeltoolkit.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionGsonAdapter
import com.jawzf.pixeltoolkit.core.data.display.TextAnimationsSettings
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.data.power.PowerSettings
import com.jawzf.pixeltoolkit.core.data.security.DeviceLockUtils
import com.jawzf.pixeltoolkit.core.design.haptics.HapticFeedbackType
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs
import com.jawzf.pixeltoolkit.core.platform.shell.RootUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.domain.model.AppIcon
import com.jawzf.pixeltoolkit.domain.model.SystemShortcutsState
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings
import com.jawzf.pixeltoolkit.feature.display.refresh.RefreshRateUtils
import com.jawzf.pixeltoolkit.feature.input.remap.PowerChoice
import com.jawzf.pixeltoolkit.feature.input.remap.RemapSlot
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings
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

        const val KEY_LOCKDOWN_MODE = DeviceLockUtils.KEY_LOCKDOWN_MODE
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
     * Executes the sync system settings with saved operation.
     */
    fun syncSystemSettingsWithSaved() {
        try {
            TextAnimationsSettings(context).applySavedToSystem()
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
