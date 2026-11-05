package com.jawzf.pixeltoolkit.core.design.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun EqualizerBars(
    playing: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp,
) {
    val levels = remember { mutableStateListOf(0.16f, 0.16f, 0.16f) }
    LaunchedEffect(playing) {
        if (!playing) {
            for (i in levels.indices) levels[i] = 0.16f
            return@LaunchedEffect
        }
        val random = Random(System.nanoTime())
        while (true) {
            for (i in levels.indices) levels[i] = 0.15f + random.nextFloat() * 0.85f
            delay(180L + random.nextLong(160L))
        }
    }
    val animated =
        levels.mapIndexed { i, level ->
            // Same spring as IslandMotion.float().
            animateFloatAsState(level, spring(dampingRatio = 0.86f, stiffness = 260f), label = "eq$i").value
        }
    Canvas(modifier.sizeIn(maxWidth = size, maxHeight = size).aspectRatio(1f, matchHeightConstraintsFirst = true)) {
        val barWidth = this.size.width / 5f
        animated.forEachIndexed { i, level ->
            val h = this.size.height * level
            drawRoundRect(
                color = color,
                topLeft = Offset(barWidth * (i * 2f), (this.size.height - h) / 2f),
                size = Size(barWidth, h),
                cornerRadius = CornerRadius(barWidth / 2f),
            )
        }
    }
}
