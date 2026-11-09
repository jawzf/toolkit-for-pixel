package com.jawzf.pixeltoolkit.feature.automation.engine.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.GeofencingEvent
import com.jawzf.pixeltoolkit.feature.automation.engine.LocationAutomationWorker

class GeofenceReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val event = GeofencingEvent.fromIntent(intent) ?: return
        if (event.hasError()) {
            Log.e(TAG, "Geofence error ${event.errorCode}")
            return
        }
        val ids = event.triggeringGeofences.orEmpty().map { it.requestId }
        if (ids.isEmpty()) return

        val request =
            OneTimeWorkRequestBuilder<LocationAutomationWorker>()
                .setInputData(
                    workDataOf(
                        LocationAutomationWorker.KEY_AUTOMATION_IDS to ids.toTypedArray(),
                        LocationAutomationWorker.KEY_TRANSITION to event.geofenceTransition,
                    ),
                ).build()
        WorkManager.getInstance(context).enqueue(request)
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
    }
}
