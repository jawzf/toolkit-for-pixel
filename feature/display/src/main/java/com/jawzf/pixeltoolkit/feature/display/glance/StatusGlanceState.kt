package com.jawzf.pixeltoolkit.feature.display.glance

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.CalendarContract
import android.view.WindowManager
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.glance.StatusGlanceSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CalendarAccount(
    val id: Long,
    val name: String,
    val accountName: String,
    val isSelected: Boolean,
)

class StatusGlanceState(
    private val scope: CoroutineScope,
    private val onFoldableChanged: (Boolean) -> Unit,
) {
    private lateinit var settings: StatusGlanceSettings
    private lateinit var duoSettings: DuoSettings

    val isStatusGlanceEnabled = mutableStateOf(false)
    val isStatusGlanceAutoDetect = mutableStateOf(true)
    val isStatusGlanceOrientationProfiles = mutableStateOf(false)
    val isStatusGlanceHiddenInCurrentOrientation = mutableStateOf(false)
    val statusGlanceOffsetX = mutableFloatStateOf(60f)
    val statusGlanceOffsetY = mutableFloatStateOf(2f)
    val statusGlanceMaxWidth = mutableFloatStateOf(180f)
    val statusGlanceFontSize = mutableFloatStateOf(13f)
    val isStatusGlanceShowFlashlight = mutableStateOf(true)
    val isStatusGlanceShowCalendar = mutableStateOf(true)
    val statusGlanceCalendarTimeframe = mutableStateOf("today")
    val statusGlanceCalendarShowAllDay = mutableStateOf(false)
    val statusGlanceSelectedCalendarIds = mutableStateOf<Set<String>>(emptySet())
    val statusGlanceAvailableCalendars = mutableStateListOf<CalendarAccount>()
    val isStatusGlanceShowMedia = mutableStateOf(true)
    val isStatusGlanceShowTime = mutableStateOf(true)
    val isStatusGlanceBackgroundPill = mutableStateOf(false)
    val isStatusGlanceAlbumArtColors = mutableStateOf(true)
    val isStatusGlanceHideWhenFullscreen = mutableStateOf(true)
    val isStatusGlanceHideInQuickSettings = mutableStateOf(true)
    val isStatusGlanceHideWhenLocked = mutableStateOf(true)
    val isStatusGlanceBoldText = mutableStateOf(false)
    val isStatusGlanceShowBattery = mutableStateOf(false)
    val statusGlanceBatteryDisplayMode = mutableStateOf("icon")
    val isStatusGlanceBatteryShowLow = mutableStateOf(true)
    val isStatusGlanceBatteryShowCharging = mutableStateOf(true)
    val isStatusGlanceBatteryShowFull = mutableStateOf(true)
    val isStatusGlanceBatteryShowOtherwise = mutableStateOf(true)
    val statusGlanceBatteryIconSize = mutableFloatStateOf(16f)
    val statusGlanceTapAction = mutableStateOf<Action?>(null)
    val statusGlanceDoubleTapAction = mutableStateOf<Action?>(null)
    val statusGlanceLongPressAction = mutableStateOf<Action?>(null)

    fun load(context: Context) {
        settings = StatusGlanceSettings(context)
        duoSettings = DuoSettings(context)
        isStatusGlanceEnabled.value = settings.isStatusGlanceEnabled()
        isStatusGlanceAutoDetect.value = settings.isStatusGlanceAutoDetectEnabled()
        statusGlanceOffsetX.floatValue = settings.getStatusGlanceOffsetX()
        statusGlanceOffsetY.floatValue = settings.getStatusGlanceOffsetY()
        statusGlanceMaxWidth.floatValue = settings.getStatusGlanceMaxWidth()
        statusGlanceFontSize.floatValue = settings.getStatusGlanceFontSize()
        isStatusGlanceShowFlashlight.value = settings.isStatusGlanceShowFlashlightEnabled()
        isStatusGlanceShowCalendar.value = settings.isStatusGlanceShowCalendarEnabled()
        statusGlanceCalendarTimeframe.value = settings.getStatusGlanceCalendarTimeframe()
        statusGlanceCalendarShowAllDay.value = settings.isStatusGlanceCalendarShowAllDayEnabled()
        statusGlanceSelectedCalendarIds.value = settings.getStatusGlanceCalendarSelectedCalendars()
        isStatusGlanceShowMedia.value = settings.isStatusGlanceShowMediaEnabled()
        isStatusGlanceShowTime.value = settings.isStatusGlanceShowTimeEnabled()
        isStatusGlanceBackgroundPill.value = settings.isStatusGlanceBackgroundPillEnabled()
        isStatusGlanceAlbumArtColors.value = settings.isStatusGlanceAlbumArtColorsEnabled()
        isStatusGlanceHideWhenFullscreen.value = settings.isStatusGlanceHideWhenFullscreenEnabled()
        isStatusGlanceHideInQuickSettings.value = settings.isStatusGlanceHideInQuickSettingsEnabled()
        isStatusGlanceHideWhenLocked.value = settings.isStatusGlanceHideWhenLockedEnabled()
        isStatusGlanceBoldText.value = settings.isStatusGlanceBoldTextEnabled()
        isStatusGlanceShowBattery.value = settings.isStatusGlanceShowBatteryEnabled()
        statusGlanceBatteryDisplayMode.value = settings.getStatusGlanceBatteryDisplayMode()
        isStatusGlanceBatteryShowLow.value = settings.isStatusGlanceBatteryShowLowEnabled()
        isStatusGlanceBatteryShowCharging.value = settings.isStatusGlanceBatteryShowChargingEnabled()
        isStatusGlanceBatteryShowFull.value = settings.isStatusGlanceBatteryShowFullEnabled()
        isStatusGlanceBatteryShowOtherwise.value = settings.isStatusGlanceBatteryShowOtherwiseEnabled()
        statusGlanceBatteryIconSize.floatValue = settings.getStatusGlanceBatteryIconSize()
        statusGlanceTapAction.value = settings.getStatusGlanceTapAction()
        statusGlanceDoubleTapAction.value = settings.getStatusGlanceDoubleTapAction()
        statusGlanceLongPressAction.value = settings.getStatusGlanceLongPressAction()
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            StatusGlanceSettings.KEY_STATUS_GLANCE_ENABLED ->
                isStatusGlanceEnabled.value = settings.isStatusGlanceEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_USE_AUTO_DETECT ->
                isStatusGlanceAutoDetect.value = settings.isStatusGlanceAutoDetectEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_OFFSET_X ->
                statusGlanceOffsetX.floatValue = settings.getStatusGlanceOffsetX()

            StatusGlanceSettings.KEY_STATUS_GLANCE_OFFSET_Y ->
                statusGlanceOffsetY.floatValue = settings.getStatusGlanceOffsetY()

            StatusGlanceSettings.KEY_STATUS_GLANCE_MAX_WIDTH ->
                statusGlanceMaxWidth.floatValue = settings.getStatusGlanceMaxWidth()

            StatusGlanceSettings.KEY_STATUS_GLANCE_FONT_SIZE ->
                statusGlanceFontSize.floatValue = settings.getStatusGlanceFontSize()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BACKGROUND_PILL ->
                isStatusGlanceBackgroundPill.value = settings.isStatusGlanceBackgroundPillEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_ALBUM_ART_COLORS ->
                isStatusGlanceAlbumArtColors.value = settings.isStatusGlanceAlbumArtColorsEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN ->
                isStatusGlanceHideWhenFullscreen.value = settings.isStatusGlanceHideWhenFullscreenEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS ->
                isStatusGlanceHideInQuickSettings.value = settings.isStatusGlanceHideInQuickSettingsEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED ->
                isStatusGlanceHideWhenLocked.value = settings.isStatusGlanceHideWhenLockedEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BOLD_TEXT ->
                isStatusGlanceBoldText.value = settings.isStatusGlanceBoldTextEnabled()

            else -> onContentPreferenceChanged(key)
        }
    }

    private fun onContentPreferenceChanged(key: String) {
        when (key) {
            StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_FLASHLIGHT ->
                isStatusGlanceShowFlashlight.value = settings.isStatusGlanceShowFlashlightEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_CALENDAR ->
                isStatusGlanceShowCalendar.value = settings.isStatusGlanceShowCalendarEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME ->
                statusGlanceCalendarTimeframe.value = settings.getStatusGlanceCalendarTimeframe()

            StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY ->
                statusGlanceCalendarShowAllDay.value = settings.isStatusGlanceCalendarShowAllDayEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS ->
                statusGlanceSelectedCalendarIds.value = settings.getStatusGlanceCalendarSelectedCalendars()

            StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_MEDIA ->
                isStatusGlanceShowMedia.value = settings.isStatusGlanceShowMediaEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_TIME ->
                isStatusGlanceShowTime.value = settings.isStatusGlanceShowTimeEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_TAP_ACTION ->
                statusGlanceTapAction.value = settings.getStatusGlanceTapAction()

            StatusGlanceSettings.KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION ->
                statusGlanceDoubleTapAction.value = settings.getStatusGlanceDoubleTapAction()

            StatusGlanceSettings.KEY_STATUS_GLANCE_LONG_PRESS_ACTION ->
                statusGlanceLongPressAction.value = settings.getStatusGlanceLongPressAction()

            else -> onBatteryPreferenceChanged(key)
        }
    }

    private fun onBatteryPreferenceChanged(key: String) {
        when (key) {
            StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_BATTERY ->
                isStatusGlanceShowBattery.value = settings.isStatusGlanceShowBatteryEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE ->
                statusGlanceBatteryDisplayMode.value = settings.getStatusGlanceBatteryDisplayMode()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_LOW ->
                isStatusGlanceBatteryShowLow.value = settings.isStatusGlanceBatteryShowLowEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING ->
                isStatusGlanceBatteryShowCharging.value = settings.isStatusGlanceBatteryShowChargingEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_FULL ->
                isStatusGlanceBatteryShowFull.value = settings.isStatusGlanceBatteryShowFullEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE ->
                isStatusGlanceBatteryShowOtherwise.value = settings.isStatusGlanceBatteryShowOtherwiseEnabled()

            StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_ICON_SIZE ->
                statusGlanceBatteryIconSize.floatValue = settings.getStatusGlanceBatteryIconSize()
        }
    }

    fun setStatusGlanceEnabled(enabled: Boolean) {
        isStatusGlanceEnabled.value = enabled
        settings.setStatusGlanceEnabled(enabled)
    }

    fun setStatusGlanceAutoDetect(enabled: Boolean) {
        isStatusGlanceAutoDetect.value = enabled
        settings.setStatusGlanceAutoDetectEnabled(enabled)
    }

    fun refreshStatusGlanceCameraPlacement() {
        onFoldableChanged(duoSettings.isFoldableDevice())
        isStatusGlanceOrientationProfiles.value = settings.isStatusGlanceOrientationProfilesEnabled()
        isStatusGlanceHiddenInCurrentOrientation.value = settings.isStatusGlanceHiddenInCurrentOrientation()
        statusGlanceOffsetX.floatValue = settings.getStatusGlanceOffsetX()
        statusGlanceOffsetY.floatValue = settings.getStatusGlanceOffsetY()
    }

    fun setStatusGlanceOrientationProfiles(enabled: Boolean) {
        settings.setStatusGlanceOrientationProfilesEnabled(enabled)
        refreshStatusGlanceCameraPlacement()
    }

    fun setStatusGlanceHiddenInCurrentOrientation(hidden: Boolean) {
        settings.setStatusGlanceHiddenInCurrentOrientation(hidden)
        isStatusGlanceHiddenInCurrentOrientation.value = hidden
    }

    fun setStatusGlanceOffsetX(value: Float) {
        statusGlanceOffsetX.floatValue = value
        settings.setStatusGlanceOffsetX(value)
    }

    fun setStatusGlanceOffsetY(value: Float) {
        statusGlanceOffsetY.floatValue = value
        settings.setStatusGlanceOffsetY(value)
    }

    fun setStatusGlanceShowFlashlight(enabled: Boolean) {
        isStatusGlanceShowFlashlight.value = enabled
        settings.setStatusGlanceShowFlashlightEnabled(enabled)
    }

    fun setStatusGlanceShowCalendar(enabled: Boolean) {
        isStatusGlanceShowCalendar.value = enabled
        settings.setStatusGlanceShowCalendarEnabled(enabled)
    }

    fun setStatusGlanceCalendarTimeframe(timeframe: String) {
        statusGlanceCalendarTimeframe.value = timeframe
        settings.setStatusGlanceCalendarTimeframe(timeframe)
    }

    fun setStatusGlanceCalendarShowAllDay(enabled: Boolean) {
        statusGlanceCalendarShowAllDay.value = enabled
        settings.setStatusGlanceCalendarShowAllDayEnabled(enabled)
    }

    fun fetchStatusGlanceCalendars(context: Context) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        scope.launch(Dispatchers.IO) {
            val savedSelected = settings.getStatusGlanceCalendarSelectedCalendars()
            withContext(Dispatchers.Main) {
                statusGlanceSelectedCalendarIds.value = savedSelected
            }

            val calendars = mutableListOf<CalendarAccount>()
            val projection =
                arrayOf(
                    CalendarContract.Calendars._ID,
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Calendars.ACCOUNT_NAME,
                    CalendarContract.Calendars.CALENDAR_COLOR,
                )

            context.contentResolver
                .query(
                    CalendarContract.Calendars.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null,
                )?.use { cursor ->
                    val idColumn = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                    val nameColumn = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                    val accountColumn = cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)

                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val name = cursor.getString(nameColumn) ?: "Unnamed Calendar"
                        val account = cursor.getString(accountColumn) ?: "Local"

                        calendars.add(
                            CalendarAccount(
                                id,
                                name,
                                account,
                                savedSelected.isEmpty() || savedSelected.contains(id.toString()),
                            ),
                        )
                    }
                }

            withContext(Dispatchers.Main) {
                statusGlanceAvailableCalendars.clear()
                statusGlanceAvailableCalendars.addAll(calendars)
            }
        }
    }

    fun toggleStatusGlanceCalendarSelection(calendarId: Long) {
        val currentIds = statusGlanceSelectedCalendarIds.value.toMutableSet()
        val idString = calendarId.toString()
        if (currentIds.contains(idString)) {
            currentIds.remove(idString)
        } else {
            currentIds.add(idString)
        }
        statusGlanceSelectedCalendarIds.value = currentIds
        settings.saveStatusGlanceCalendarSelectedCalendars(currentIds)

        val index = statusGlanceAvailableCalendars.indexOfFirst { it.id == calendarId }
        if (index != -1) {
            statusGlanceAvailableCalendars[index] =
                statusGlanceAvailableCalendars[index].copy(isSelected = currentIds.contains(idString))
        }
    }

    fun setStatusGlanceShowMedia(enabled: Boolean) {
        isStatusGlanceShowMedia.value = enabled
        settings.setStatusGlanceShowMediaEnabled(enabled)
    }

    fun setStatusGlanceShowTime(enabled: Boolean) {
        isStatusGlanceShowTime.value = enabled
        settings.setStatusGlanceShowTimeEnabled(enabled)
    }

    fun setStatusGlanceBackgroundPill(enabled: Boolean) {
        isStatusGlanceBackgroundPill.value = enabled
        settings.setStatusGlanceBackgroundPillEnabled(enabled)
    }

    fun setStatusGlanceAlbumArtColors(enabled: Boolean) {
        isStatusGlanceAlbumArtColors.value = enabled
        settings.setStatusGlanceAlbumArtColorsEnabled(enabled)
    }

    fun setStatusGlanceHideWhenFullscreen(enabled: Boolean) {
        isStatusGlanceHideWhenFullscreen.value = enabled
        settings.setStatusGlanceHideWhenFullscreenEnabled(enabled)
    }

    fun setStatusGlanceHideInQuickSettings(enabled: Boolean) {
        isStatusGlanceHideInQuickSettings.value = enabled
        settings.setStatusGlanceHideInQuickSettingsEnabled(enabled)
    }

    fun setStatusGlanceHideWhenLocked(enabled: Boolean) {
        isStatusGlanceHideWhenLocked.value = enabled
        settings.setStatusGlanceHideWhenLockedEnabled(enabled)
    }

    fun setStatusGlanceBoldText(enabled: Boolean) {
        isStatusGlanceBoldText.value = enabled
        settings.setStatusGlanceBoldTextEnabled(enabled)
    }

    fun setStatusGlanceShowBattery(enabled: Boolean) {
        isStatusGlanceShowBattery.value = enabled
        settings.setStatusGlanceShowBatteryEnabled(enabled)
    }

    fun setStatusGlanceBatteryDisplayMode(mode: String) {
        statusGlanceBatteryDisplayMode.value = mode
        settings.setStatusGlanceBatteryDisplayMode(mode)
    }

    fun setStatusGlanceBatteryShowLow(enabled: Boolean) {
        isStatusGlanceBatteryShowLow.value = enabled
        settings.setStatusGlanceBatteryShowLowEnabled(enabled)
    }

    fun setStatusGlanceBatteryShowCharging(enabled: Boolean) {
        isStatusGlanceBatteryShowCharging.value = enabled
        settings.setStatusGlanceBatteryShowChargingEnabled(enabled)
    }

    fun setStatusGlanceBatteryShowFull(enabled: Boolean) {
        isStatusGlanceBatteryShowFull.value = enabled
        settings.setStatusGlanceBatteryShowFullEnabled(enabled)
    }

    fun setStatusGlanceBatteryShowOtherwise(enabled: Boolean) {
        isStatusGlanceBatteryShowOtherwise.value = enabled
        settings.setStatusGlanceBatteryShowOtherwiseEnabled(enabled)
    }

    fun setStatusGlanceBatteryIconSize(size: Float) {
        statusGlanceBatteryIconSize.floatValue = size
        settings.setStatusGlanceBatteryIconSize(size)
    }

    fun setStatusGlanceTapAction(action: Action?) {
        statusGlanceTapAction.value = action
        settings.setStatusGlanceTapAction(action)
    }

    fun setStatusGlanceDoubleTapAction(action: Action?) {
        statusGlanceDoubleTapAction.value = action
        settings.setStatusGlanceDoubleTapAction(action)
    }

    fun setStatusGlanceLongPressAction(action: Action?) {
        statusGlanceLongPressAction.value = action
        settings.setStatusGlanceLongPressAction(action)
    }

    fun setStatusGlanceMaxWidth(value: Float) {
        statusGlanceMaxWidth.floatValue = value
        settings.setStatusGlanceMaxWidth(value)
    }

    fun setStatusGlanceFontSize(value: Float) {
        statusGlanceFontSize.floatValue = value
        settings.setStatusGlanceFontSize(value)
    }

    fun autoDetectStatusGlancePosition(context: Context) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager ?: return
        val dm = context.resources.displayMetrics
        val screenWidth = dm.widthPixels.toFloat()
        val screenHeight = dm.heightPixels.toFloat()
        val density = dm.density

        var detectedXPercent = 60f
        var detectedYPercent = 2f

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            val cutout = wm.defaultDisplay?.cutout
            if (cutout != null && cutout.boundingRects.isNotEmpty()) {
                val targetRect = cutout.boundingRects.firstOrNull()
                if (targetRect != null) {
                    detectedXPercent = ((targetRect.right + 8f * density) / screenWidth) * 100f
                    detectedYPercent = (targetRect.centerY().toFloat() / screenHeight) * 100f
                }
            } else {
                val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
                val statusBarHeight =
                    if (resourceId > 0) {
                        context.resources.getDimensionPixelSize(resourceId).toFloat()
                    } else {
                        24f * density
                    }
                detectedXPercent = 50f
                detectedYPercent = ((statusBarHeight / 2f) / screenHeight) * 100f
            }
        }

        setStatusGlanceOffsetX(detectedXPercent.coerceIn(0f, 100f))
        setStatusGlanceOffsetY(detectedYPercent.coerceIn(0f, 20f))
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                StatusGlanceSettings.KEY_STATUS_GLANCE_ENABLED,
                StatusGlanceSettings.KEY_STATUS_GLANCE_USE_AUTO_DETECT,
                StatusGlanceSettings.KEY_STATUS_GLANCE_OFFSET_X,
                StatusGlanceSettings.KEY_STATUS_GLANCE_OFFSET_Y,
                StatusGlanceSettings.KEY_STATUS_GLANCE_MAX_WIDTH,
                StatusGlanceSettings.KEY_STATUS_GLANCE_FONT_SIZE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_FLASHLIGHT,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_CALENDAR,
                StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_TIMEFRAME,
                StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_SHOW_ALL_DAY,
                StatusGlanceSettings.KEY_STATUS_GLANCE_CALENDAR_SELECTED_CALENDARS,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_MEDIA,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_TIME,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BACKGROUND_PILL,
                StatusGlanceSettings.KEY_STATUS_GLANCE_ALBUM_ART_COLORS,
                StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_WHEN_FULLSCREEN,
                StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_IN_QUICK_SETTINGS,
                StatusGlanceSettings.KEY_STATUS_GLANCE_HIDE_WHEN_LOCKED,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BOLD_TEXT,
                StatusGlanceSettings.KEY_STATUS_GLANCE_SHOW_BATTERY,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_DISPLAY_MODE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_LOW,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_CHARGING,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_FULL,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_SHOW_OTHERWISE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_BATTERY_ICON_SIZE,
                StatusGlanceSettings.KEY_STATUS_GLANCE_TAP_ACTION,
                StatusGlanceSettings.KEY_STATUS_GLANCE_DOUBLE_TAP_ACTION,
                StatusGlanceSettings.KEY_STATUS_GLANCE_LONG_PRESS_ACTION,
            )
    }
}
