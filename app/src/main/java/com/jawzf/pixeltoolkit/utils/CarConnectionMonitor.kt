package com.jawzf.pixeltoolkit.utils

import android.content.Context
import androidx.car.app.connection.CarConnection
import java.util.concurrent.CopyOnWriteArraySet

object CarConnectionMonitor {
    @Volatile
    var isProjecting: Boolean = false
        private set

    private val listeners = CopyOnWriteArraySet<(Boolean) -> Unit>()
    private var connection: CarConnection? = null

    fun initialize(context: Context) {
        if (connection != null) return
        try {
            val carConnection = CarConnection(context.applicationContext)
            carConnection.type.observeForever { type ->
                isProjecting = type == CarConnection.CONNECTION_TYPE_PROJECTION
                listeners.forEach { it(isProjecting) }
            }
            connection = carConnection
        } catch (_: Exception) {
            // No car-host provider
        }
    }

    fun addListener(listener: (Boolean) -> Unit) {
        listeners.add(listener)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners.remove(listener)
    }

    /**
     * Checks if the device is currently in Car Mode or projecting Android Auto
     */
    fun isAndroidAutoRunning(context: Context): Boolean {
        if (isProjecting) return true

        // 1. Check UiModeManager
        val uiModeManager =
            context.getSystemService(Context.UI_MODE_SERVICE) as? android.app.UiModeManager
        if (uiModeManager?.currentModeType == android.content.res.Configuration.UI_MODE_TYPE_CAR) return true

        // 2. Check Configuration
        val config = context.resources.configuration
        if ((config.uiMode and android.content.res.Configuration.UI_MODE_TYPE_MASK) ==
            android.content.res.Configuration.UI_MODE_TYPE_CAR
        ) {
            return true
        }

        return hasCarDisplay(context) || hasForegroundCarService(context)
    }

    // 3. Check for Android Auto Projection (Virtual Displays)
    private fun hasCarDisplay(context: Context): Boolean {
        val displayManager =
            context.getSystemService(Context.DISPLAY_SERVICE) as? android.hardware.display.DisplayManager
        return displayManager?.displays?.any { display ->
            val name = display.name ?: ""
            name.contains("Android Auto", ignoreCase = true) ||
                name.contains("Wireless Projector", ignoreCase = true) ||
                name.contains("Car", ignoreCase = true)
        } == true
    }

    // 4. Check for active Car packages
    private fun hasForegroundCarService(context: Context): Boolean {
        try {
            val carPackages =
                listOf(
                    "com.google.android.projection.gearhead",
                    "com.google.android.apps.auto.carservice",
                )
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            val processes = activityManager?.runningAppProcesses
            return processes?.any {
                carPackages.contains(it.processName) &&
                    it.importance <= android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND_SERVICE
            } == true
        } catch (_: Exception) {
        }
        return false
    }
}
