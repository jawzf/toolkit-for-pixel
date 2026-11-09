package com.jawzf.pixeltoolkit.feature.automation.engine

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.service.notification.Condition
import android.service.notification.ConditionProviderService
import android.util.Log

@Suppress("DEPRECATION")
class ToolkitConditionProvider : ConditionProviderService() {
    companion object {
        private const val TAG = "ToolkitCPS"
        val CONDITION_URI: Uri = Uri.parse("condition://com.jawzf.pixeltoolkit/focus")

        private var instance: ToolkitConditionProvider? = null

        fun setConditionState(
            context: Context,
            isActive: Boolean,
        ) {
            Log.d(TAG, "Requesting condition state: $isActive")
            val provider = instance
            if (provider != null) {
                provider.notifyChange(isActive)
            } else {
                try {
                    requestRebind(ComponentName(context, ToolkitConditionProvider::class.java))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onConnected() {
        Log.d(TAG, "onConnected")
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        instance = null
    }

    @Deprecated("Deprecated in Java")
    override fun onSubscribe(conditionId: Uri?) {
        Log.d(TAG, "onSubscribe: $conditionId")
        notifyChange(false)
    }

    @Deprecated("Deprecated in Java")
    override fun onUnsubscribe(conditionId: Uri?) {
        Log.d(TAG, "onUnsubscribe: $conditionId")
    }

    private fun notifyChange(active: Boolean) {
        Log.d(TAG, "notifyChange: active=$active")
        val state = if (active) Condition.STATE_TRUE else Condition.STATE_FALSE
        val condition = Condition(CONDITION_URI, "Toolkit Focus", state)
        try {
            notifyCondition(condition)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to notify condition", e)
        }
    }
}
