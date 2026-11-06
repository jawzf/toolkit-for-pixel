package com.jawzf.pixeltoolkit.feature.widgets.stack

import com.jawzf.pixeltoolkit.feature.widgets.stack.WidgetStackConfig

object StackPositions {
    fun restore(
        saved: Map<Int, Int>,
        stacks: List<WidgetStackConfig>,
    ): Map<Int, Int> =
        buildMap {
            for (stack in stacks) {
                if (stack.hostedWidgetIds.isEmpty()) continue
                val pos = saved[stack.stackWidgetId] ?: continue
                put(stack.stackWidgetId, pos.coerceIn(0, stack.hostedWidgetIds.lastIndex))
            }
        }
}
