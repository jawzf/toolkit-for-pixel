package com.sameerasw.essentials.island.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sameerasw.essentials.island.ui.IslandHaptics

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandSeekBar(
    value: Float,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    wavy: Boolean = true,
    steps: Int = 20,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChange: (Float) -> Unit = {},
    onValueChangeFinished: (Float) -> Unit = {},
    onInteraction: () -> Unit = {},
) {
    val context = LocalContext.current
    var dragValue by remember { mutableStateOf<Float?>(null) }
    var lastStep by remember { mutableIntStateOf(-1) }
    var lastInteraction by remember { mutableStateOf(0L) }
    val dragging = dragValue != null
    Slider(
        value = dragValue ?: value,
        onValueChange = { v ->
            val now = android.os.SystemClock.uptimeMillis()
            if (now - lastInteraction >= 500L) {
                lastInteraction = now
                onInteraction()
            }
            val step = (v * steps).toInt()
            if (dragValue == null) {
                IslandHaptics.touchDown(context)
                lastStep = step
            } else if (step != lastStep) {
                lastStep = step
                IslandHaptics.dragStep(context)
            }
            dragValue = v
            onValueChange(v)
        },
        onValueChangeFinished = {
            onInteraction()
            dragValue?.let {
                IslandHaptics.commit(context)
                onValueChangeFinished(it)
            }
            dragValue = null
        },
        enabled = enabled,
        valueRange = valueRange,
        modifier = modifier.fillMaxWidth(),
        thumb = {
            Box(
                Modifier
                    .size(width = 4.dp, height = if (dragging) 20.dp else 0.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White),
            )
        },
        track = { state ->
            LinearWavyProgressIndicator(
                progress = { state.value },
                modifier = Modifier.fillMaxWidth(),
                color = color,
                trackColor = Color.White.copy(alpha = 0.2f),
                amplitude = { if (wavy && !dragging) 1f else 0f },
            )
        },
    )
}
