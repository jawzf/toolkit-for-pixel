package com.jawzf.pixeltoolkit.feature.notifications.hilight

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.action.HilightEffect
import com.jawzf.pixeltoolkit.core.data.action.HilightProgressColorMode
import com.jawzf.pixeltoolkit.feature.notifications.NotificationSettings

class HilightState {
    private lateinit var settings: NotificationSettings

    val isHilightNotificationsEnabled = mutableStateOf(false)
    val isHilightOnlyWhenScreenOff = mutableStateOf(true)
    val isHilightSkipDnd = mutableStateOf(true)
    val hilightCooldownSeconds = mutableIntStateOf(60)
    val isHilightProgressEnabled = mutableStateOf(false)
    val hilightProgressColorMode = mutableStateOf(HilightProgressColorMode.VIBGYOR)
    val hilightProgressColor = mutableIntStateOf(HilightEffect.DEFAULT_COLOR)
    val hilightProgressDurationMs = mutableLongStateOf(HilightEffect.DEFAULT_DURATION_MS)
    val hilightAppEffects = mutableStateMapOf<String, HilightEffect>()

    fun load(context: Context) {
        settings = NotificationSettings(context)
        isHilightNotificationsEnabled.value = settings.isHilightNotificationsEnabled()
        isHilightOnlyWhenScreenOff.value = settings.isHilightOnlyWhenScreenOff()
        isHilightSkipDnd.value = settings.isHilightSkipDnd()
        hilightCooldownSeconds.intValue = settings.getHilightCooldownSeconds()
        isHilightProgressEnabled.value = settings.isHilightProgressEnabled()
        hilightProgressColorMode.value = settings.getHilightProgressColorMode()
        hilightProgressColor.intValue = settings.getHilightProgressColor()
        hilightProgressDurationMs.longValue = settings.getHilightProgressDurationMs()
        hilightAppEffects.clear()
        hilightAppEffects.putAll(settings.getHilightAppEffects())
    }

    fun setHilightNotificationsEnabled(enabled: Boolean) {
        isHilightNotificationsEnabled.value = enabled
        settings.setHilightNotificationsEnabled(enabled)
    }

    fun setHilightSkipDnd(enabled: Boolean) {
        isHilightSkipDnd.value = enabled
        settings.setHilightSkipDnd(enabled)
    }

    fun setHilightOnlyWhenScreenOff(enabled: Boolean) {
        isHilightOnlyWhenScreenOff.value = enabled
        settings.setHilightOnlyWhenScreenOff(enabled)
    }

    fun setHilightCooldownSeconds(seconds: Int) {
        hilightCooldownSeconds.intValue = seconds
        settings.setHilightCooldownSeconds(seconds)
    }

    fun setHilightProgressEnabled(enabled: Boolean) {
        isHilightProgressEnabled.value = enabled
        settings.setHilightProgressEnabled(enabled)
    }

    fun setHilightProgressColorMode(mode: HilightProgressColorMode) {
        hilightProgressColorMode.value = mode
        settings.setHilightProgressColorMode(mode)
    }

    fun setHilightProgressColor(color: Int) {
        hilightProgressColor.intValue = color
        settings.setHilightProgressColor(color)
    }

    fun setHilightProgressDurationMs(durationMs: Long) {
        hilightProgressDurationMs.longValue = durationMs
        settings.setHilightProgressDurationMs(durationMs)
    }

    fun setHilightAppEffect(
        packageName: String,
        effect: HilightEffect,
    ) {
        hilightAppEffects[packageName] = effect
        settings.setHilightAppEffect(packageName, effect)
    }

    fun removeHilightApp(packageName: String) {
        hilightAppEffects.remove(packageName)
        settings.setHilightAppEffect(packageName, null)
    }
}
