package com.jawzf.pixeltoolkit.feature.island.plugins

import com.jawzf.pixeltoolkit.feature.island.model.IslandItem
import com.jawzf.pixeltoolkit.feature.island.model.IslandPlugin
import com.jawzf.pixeltoolkit.feature.island.model.IslandPluginContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseIslandPlugin : IslandPlugin {
    private val _items = MutableStateFlow<List<IslandItem>>(emptyList())
    override val items: StateFlow<List<IslandItem>> = _items.asStateFlow()

    protected var ctx: IslandPluginContext? = null
        private set

    protected val context get() = ctx!!.service
    protected val settings get() = ctx!!.settings

    final override fun start(context: IslandPluginContext) {
        ctx = context
        onStart()
        refresh()
    }

    final override fun stop() {
        onStop()
        publish(emptyList())
        ctx = null
    }

    protected open fun onStart() {}
    protected open fun onStop() {}

    protected fun publish(items: List<IslandItem>) {
        _items.value = items
    }

    protected fun publish(item: IslandItem?) = publish(listOfNotNull(item))
}
