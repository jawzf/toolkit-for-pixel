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
}
