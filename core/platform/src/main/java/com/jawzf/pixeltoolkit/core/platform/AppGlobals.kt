package com.jawzf.pixeltoolkit.core.platform

import android.content.Context

object AppGlobals {
    lateinit var context: Context
        private set

    fun init(appContext: Context) {
        context = appContext
    }
}
