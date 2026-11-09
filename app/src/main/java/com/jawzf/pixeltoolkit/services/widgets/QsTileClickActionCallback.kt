package com.jawzf.pixeltoolkit.services.widgets

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.jawzf.pixeltoolkit.core.platform.tiles.QsTileCatalog

class QsTileClickActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val serviceClassName = parameters[SERVICE_CLASS_KEY] ?: return
        QsTileCatalog.trigger(context, serviceClassName)
    }

    companion object {
        val SERVICE_CLASS_KEY = ActionParameters.Key<String>("service_class_name")
    }
}
