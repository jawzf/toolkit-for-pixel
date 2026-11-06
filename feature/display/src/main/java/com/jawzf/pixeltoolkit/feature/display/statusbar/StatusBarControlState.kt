package com.jawzf.pixeltoolkit.feature.display.statusbar

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.platform.PlatformPrefs

class StatusBarControlState {
    private lateinit var prefsStore: PrefsStore

    val isStatusBarIconControlEnabled = mutableStateOf(false)

    fun load(context: Context) {
        prefsStore = PrefsStore(context)
        isStatusBarIconControlEnabled.value =
            prefsStore.getBoolean(PlatformPrefs.KEY_STATUS_BAR_ICON_CONTROL_ENABLED)
    }

    fun setStatusBarIconControlEnabled(enabled: Boolean) {
        isStatusBarIconControlEnabled.value = enabled
        prefsStore.putBoolean(PlatformPrefs.KEY_STATUS_BAR_ICON_CONTROL_ENABLED, enabled)
    }

    fun onPreferenceChanged(key: String) {
        when (key) {
            PlatformPrefs.KEY_STATUS_BAR_ICON_CONTROL_ENABLED ->
                isStatusBarIconControlEnabled.value =
                    prefsStore.getBoolean(key)
        }
    }

    companion object {
        val PREFERENCE_KEYS = setOf(PlatformPrefs.KEY_STATUS_BAR_ICON_CONTROL_ENABLED)
    }
}
