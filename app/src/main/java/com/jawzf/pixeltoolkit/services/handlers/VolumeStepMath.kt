package com.jawzf.pixeltoolkit.services.handlers

internal object VolumeStepMath {
    const val MIN_REPEAT_INTERVAL_MS = 150L
    const val REPEAT_MS_PER_STEP = 100L

    fun target(
        current: Int,
        min: Int,
        max: Int,
        steps: Int,
        raise: Boolean,
    ): Int = (current + if (raise) steps else -steps).coerceIn(min, max)

    fun repeatIntervalMs(steps: Int): Long = maxOf(MIN_REPEAT_INTERVAL_MS, steps * REPEAT_MS_PER_STEP)
}
