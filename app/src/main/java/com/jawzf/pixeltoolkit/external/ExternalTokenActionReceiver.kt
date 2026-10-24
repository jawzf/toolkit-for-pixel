package com.jawzf.pixeltoolkit.external

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import java.security.MessageDigest

class ExternalTokenActionReceiver : BroadcastReceiver() {
    companion object {
        const val EXTRA_TOKEN = "token"
    }

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        val expected = SettingsRepository(context).getExternalControlToken()
        val provided = intent.getStringExtra(EXTRA_TOKEN)
        if (expected == null || provided == null ||
            !MessageDigest.isEqual(expected.toByteArray(), provided.toByteArray())
        ) {
            Log.w("ExternalTokenReceiver", "Rejected external control request with missing or wrong token")
            return
        }
        handleExternalControlIntent(context, intent)
    }
}
