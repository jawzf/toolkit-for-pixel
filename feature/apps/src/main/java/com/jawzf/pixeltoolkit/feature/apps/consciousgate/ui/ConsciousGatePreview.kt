package com.jawzf.pixeltoolkit.feature.apps.consciousgate.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme

@Composable
fun ConsciousGatePreview(
    title: String,
    message: String,
    targetAppLabel: String,
    targetAppPackage: String?,
    delaySeconds: Int,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val loopSeconds = delaySeconds.coerceIn(1, 30)
    val infiniteTransition = rememberInfiniteTransition(label = "ConsciousGatePreviewProgress")
    val progress by
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(durationMillis = loopSeconds * 1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            label = "previewProgress",
        )

    ToolkitTheme {
        ConsciousGatePauseScreen(
            title = title,
            message = message,
            targetAppLabel = targetAppLabel,
            targetAppPackage = targetAppPackage,
            progress = { progress },
            onClose = onExit,
            onContinue = onExit,
            modifier = modifier.fillMaxSize(),
        )
    }
}
