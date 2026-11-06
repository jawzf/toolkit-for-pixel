package com.jawzf.pixeltoolkit.feature.apps.consciousgate

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings

/** Compose state and actions for Conscious gate. */
class ConsciousGateState(
    /** Called when the gate turns on or off, so the app-detection service can start or stop. */
    private val onAppDetectionChanged: (Context) -> Unit,
) {
    private lateinit var settings: AppsSettings

    val isConsciousGateEnabled = mutableStateOf(false)
    val consciousGateDelaySeconds = mutableIntStateOf(5)
    val consciousGateReappearMinutes = mutableIntStateOf(0)
    val isConsciousGateFeelEverySecondEnabled = mutableStateOf(false)
    val consciousGateTitle = mutableStateOf("")
    val consciousGateMessage = mutableStateOf("")

    /** Reads the saved values. Call before anything else. */
    fun load(context: Context) {
        settings = AppsSettings(context)
        isConsciousGateEnabled.value =
            settings.getBoolean(AppsSettings.KEY_CONSCIOUS_GATE_ENABLED)
        consciousGateDelaySeconds.intValue = settings.getConsciousGateDelaySeconds()
        consciousGateReappearMinutes.intValue = settings.getConsciousGateReappearMinutes()
        isConsciousGateFeelEverySecondEnabled.value = settings.getConsciousGateFeelEverySecond()
        consciousGateTitle.value = settings.getConsciousGateTitle(context)
        consciousGateMessage.value = settings.getConsciousGateMessage(context)
    }

    /**
     * Executes the set conscious gate enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     * @param context [Context] Target context.
     */
    fun setConsciousGateEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isConsciousGateEnabled.value = enabled
        settings.putBoolean(AppsSettings.KEY_CONSCIOUS_GATE_ENABLED, enabled)
        onAppDetectionChanged(context)
    }

    fun setConsciousGateDelaySeconds(seconds: Int) {
        consciousGateDelaySeconds.intValue = seconds
        settings.setConsciousGateDelaySeconds(seconds)
    }

    fun setConsciousGateReappearMinutes(minutes: Int) {
        consciousGateReappearMinutes.intValue = minutes
        settings.setConsciousGateReappearMinutes(minutes)
    }

    fun setConsciousGateFeelEverySecondEnabled(enabled: Boolean) {
        isConsciousGateFeelEverySecondEnabled.value = enabled
        settings.setConsciousGateFeelEverySecond(enabled)
    }

    fun setConsciousGateTitle(title: String) {
        consciousGateTitle.value = title
        settings.setConsciousGateTitle(title)
    }

    fun setConsciousGateMessage(message: String) {
        consciousGateMessage.value = message
        settings.setConsciousGateMessage(message)
    }

    fun saveConsciousGateSelectedApps(
        context: Context,
        apps: List<AppSelection>,
    ) {
        settings.saveConsciousGateSelectedApps(apps)
    }

    fun loadConsciousGateSelectedApps(context: Context): List<AppSelection> = settings.loadConsciousGateSelectedApps()

    fun updateConsciousGateAppEnabled(
        context: Context,
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateConsciousGateAppSelection(packageName, enabled)
    }
}
