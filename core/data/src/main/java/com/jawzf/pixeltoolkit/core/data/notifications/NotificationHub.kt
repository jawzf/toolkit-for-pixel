package com.jawzf.pixeltoolkit.core.data.notifications

object NotificationHub {
    fun interface NavigationListener {
        fun onNavigationNotificationUpdated(data: NavigationNotificationData?)
    }

    interface AlertListener {
        fun onNotificationAlertPosted(alert: ActiveNotificationAlert)

        fun onNotificationAlertRemoved(key: String)
    }

    interface Source {
        fun latestNavigation(): NavigationNotificationData?

        fun hubAlerts(): List<ActiveNotificationAlert>

        fun dismiss(key: String)

        fun performAction(
            action: NotificationActionItem,
            replyText: String?,
        ): Boolean

        fun activeNotificationKeys(): Set<String>?

        fun activeConsciousGatePackage(): String?
    }

    private val navigationListeners = mutableListOf<NavigationListener>()
    private val alertListeners = mutableListOf<AlertListener>()
    private var source: Source? = null

    fun init(hubSource: Source) {
        source = hubSource
    }

    fun addNavigationListener(listener: NavigationListener) {
        synchronized(navigationListeners) {
            if (!navigationListeners.contains(listener)) navigationListeners.add(listener)
        }
    }

    fun removeNavigationListener(listener: NavigationListener) {
        synchronized(navigationListeners) { navigationListeners.remove(listener) }
    }

    fun notifyNavigation(data: NavigationNotificationData?) {
        val copy = synchronized(navigationListeners) { navigationListeners.toList() }
        copy.forEach { it.onNavigationNotificationUpdated(data) }
    }

    fun addAlertListener(listener: AlertListener) {
        synchronized(alertListeners) {
            if (!alertListeners.contains(listener)) alertListeners.add(listener)
        }
    }

    fun removeAlertListener(listener: AlertListener) {
        synchronized(alertListeners) { alertListeners.remove(listener) }
    }

    fun notifyAlertPosted(alert: ActiveNotificationAlert) {
        val copy = synchronized(alertListeners) { alertListeners.toList() }
        copy.forEach { it.onNotificationAlertPosted(alert) }
    }

    fun notifyAlertRemoved(key: String) {
        val copy = synchronized(alertListeners) { alertListeners.toList() }
        copy.forEach { it.onNotificationAlertRemoved(key) }
    }

    fun latestNavigation(): NavigationNotificationData? = source?.latestNavigation()

    fun hubAlerts(): List<ActiveNotificationAlert> = source?.hubAlerts().orEmpty()

    fun dismiss(key: String) {
        source?.dismiss(key)
    }

    fun performAction(
        action: NotificationActionItem,
        replyText: String?,
    ): Boolean = source?.performAction(action, replyText) == true

    fun activeNotificationKeys(): Set<String>? = source?.activeNotificationKeys()

    fun activeConsciousGatePackage(): String? = source?.activeConsciousGatePackage()
}
