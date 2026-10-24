package com.jawzf.pixeltoolkit.external

import android.content.Context
import android.database.Cursor
import android.os.Bundle

interface ExternalHandler {
    val path: String

    fun onQuery(
        context: Context,
        remainingPath: String,
        extras: Bundle?,
    ): Cursor?

    fun onUpdate(
        context: Context,
        remainingPath: String,
        value: String?,
        extras: Bundle?,
    ): Boolean

    fun onAction(
        context: Context,
        remainingPath: String,
        action: String?,
        extras: Bundle?,
    ): Bundle?
}
