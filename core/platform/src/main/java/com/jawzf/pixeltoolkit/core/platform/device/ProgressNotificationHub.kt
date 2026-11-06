package com.jawzf.pixeltoolkit.core.platform.device

/** Tells other modules about progress notifications. `NotificationListener` in `:app` feeds it. */
object ProgressNotificationHub {
    fun interface Listener {
        fun onProgressNotificationUpdated(data: ProgressNotificationData?)
    }

    private val listeners = mutableListOf<Listener>()
    private var latest: () -> ProgressNotificationData? = { null }

    /** Set once in ToolkitApp.onCreate. */
    fun init(getLatest: () -> ProgressNotificationData?) {
        latest = getLatest
    }

    fun getLatest(): ProgressNotificationData? = latest()

    fun add(listener: Listener) {
        synchronized(listeners) { if (!listeners.contains(listener)) listeners.add(listener) }
    }

    fun remove(listener: Listener) {
        synchronized(listeners) { listeners.remove(listener) }
    }

    fun notifyListeners(data: ProgressNotificationData?) {
        val copy = synchronized(listeners) { listeners.toList() }
        copy.forEach { it.onProgressNotificationUpdated(data) }
    }
}
