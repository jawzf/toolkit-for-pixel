package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.notifications.NavigationNotificationData
import com.jawzf.pixeltoolkit.core.data.notifications.NotificationHub
import org.junit.Assert.assertEquals
import org.junit.Test

class NotificationHubTest {
    @Test
    fun navigationListenerLifecycle() {
        var callCount = 0
        val listener =
            NotificationHub.NavigationListener {
                callCount++
            }

        NotificationHub.addNavigationListener(listener)
        NotificationHub.addNavigationListener(listener)

        val sampleData =
            NavigationNotificationData(
                key = "test_key",
                packageName = "com.google.android.apps.maps",
                instruction = "Turn right",
                distance = "500 m",
                eta = "10 min",
                maneuver = null,
                contentIntent = null,
                exitIntent = null,
                postTime = 1000L,
            )

        NotificationHub.notifyNavigation(sampleData)
        assertEquals(1, callCount)

        NotificationHub.removeNavigationListener(listener)
        NotificationHub.notifyNavigation(null)
        assertEquals(1, callCount)
    }
}
