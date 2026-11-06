package com.jawzf.pixeltoolkit.feature.display.nightlight

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings

class NightLightState(
    private val onAppDetectionChanged: (Context) -> Unit,
) {
    private lateinit var settings: DisplaySettings

    val isDynamicNightLightEnabled = mutableStateOf(false)

    fun load(context: Context) {
        settings = DisplaySettings(context)
        isDynamicNightLightEnabled.value =
            settings.getBoolean(DisplaySettings.KEY_DYNAMIC_NIGHT_LIGHT_ENABLED)
    }

    fun setDynamicNightLightEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isDynamicNightLightEnabled.value = enabled
        settings.putBoolean(DisplaySettings.KEY_DYNAMIC_NIGHT_LIGHT_ENABLED, enabled)
        onAppDetectionChanged(context)
    }

    fun saveDynamicNightLightSelectedApps(apps: List<AppSelection>) {
        settings.saveDynamicNightLightSelectedApps(apps)
    }

    fun loadDynamicNightLightSelectedApps(): List<AppSelection> = settings.loadDynamicNightLightSelectedApps()

    fun updateDynamicNightLightAppEnabled(
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateDynamicNightLightAppSelection(packageName, enabled)
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            DisplaySettings.KEY_DYNAMIC_NIGHT_LIGHT_ENABLED ->
                isDynamicNightLightEnabled.value =
                    settings.getBoolean(key)
        }
    }

    companion object {
        val PREFERENCE_KEYS =
            setOf(
                DisplaySettings.KEY_DYNAMIC_NIGHT_LIGHT_ENABLED,
            )
    }
}
