package com.jawzf.pixeltoolkit.services.widgets

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class StackHostWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        StackHost.startIfNeeded(applicationContext)
        return Result.success()
    }
}
