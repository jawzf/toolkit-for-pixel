package com.jawzf.pixeltoolkit.feature.apps.updates

import AutoUpdaterManager
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutoUpdateManagerHelper(
    private val context: Context,
) {
    private val autoUpdaterManager = AutoUpdaterManager(context)

    suspend fun downloadAndInstallApk(
        apkUrl: String,
        apkName: String,
        onProgressUpdate: (Int) -> Unit,
    ) = withContext(Dispatchers.IO) {
        try {
            AutoBackupManager.backupBeforeUpdate(context)
            autoUpdaterManager.downloadapk(
                context = context,
                apkUrl = apkUrl,
                apkName = apkName,
                onProgressUpdate = onProgressUpdate,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getCurrentAppVersionName(): String = autoUpdaterManager.getCurrentAppVersionName()
}
