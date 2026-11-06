package com.jawzf.pixeltoolkit.feature.input.gesture.ui

import android.content.Intent
import android.net.Uri
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.jawzf.pixeltoolkit.core.data.apps.AppUtil
import com.jawzf.pixeltoolkit.core.data.apps.NotificationApp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.FeatureCard
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.sheets.ToolkitBottomSheet
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.feature.input.InputState
import com.jawzf.pixeltoolkit.feature.input.gesture.BackGestureAppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val BACK_GESTURE_FEATURE_ID = "Back gesture"

private enum class EdgeSide { LEFT, RIGHT }

@Composable
fun BackGestureSettingsUI(
    state: InputState,
    canWriteSecure: Boolean,
    isAppDetectionGranted: Boolean,
    isShizukuAvailable: Boolean,
    isUseUsageAccess: Boolean,
    onRequestShizukuPermission: () -> Unit,
    appPicker: @Composable (onDismiss: () -> Unit, onAppSelected: (NotificationApp) -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var showPermissions by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var editingPackage by remember { mutableStateOf<String?>(null) }
    val preview = rememberEdgePreviewState()
    val offLabel = stringResource(R.string.feat_back_gesture_off)

    LifecycleResumeEffect(Unit) {
        state.refreshBackGestureState()
        onPauseOrDispose {}
    }

    if (showPermissions) {
        BackGesturePermissionsSheet(
            isShizukuAvailable = isShizukuAvailable,
            useUsageAccess = isUseUsageAccess,
            onRequestShizukuPermission = onRequestShizukuPermission,
            isShellGranted = canWriteSecure,
            isAppDetectionGranted = isAppDetectionGranted,
            onDismiss = { showPermissions = false },
        )
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            EdgeWidthSlider(
                left = true,
                value = state.backGestureLeftScale.floatValue,
                onValueChange = {
                    state.backGestureLeftScale.floatValue = it
                    preview.show(EdgeSide.LEFT)
                },
                onValueChangeFinished = {
                    preview.hide()
                    if (canWriteSecure) {
                        state.applyBackGestureScale(left = true)
                    } else {
                        state.refreshBackGestureState()
                        showPermissions = true
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "back_gesture_left_width"),
                description = R.string.feat_back_gesture_default_width_desc,
            )
            EdgeWidthSlider(
                left = false,
                value = state.backGestureRightScale.floatValue,
                onValueChange = {
                    state.backGestureRightScale.floatValue = it
                    preview.show(EdgeSide.RIGHT)
                },
                onValueChangeFinished = {
                    preview.hide()
                    if (canWriteSecure) {
                        state.applyBackGestureScale(left = false)
                    } else {
                        state.refreshBackGestureState()
                        showPermissions = true
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "back_gesture_right_width"),
                description = R.string.feat_back_gesture_default_width_desc,
            )
        }

        if (state.backGestureLeftScale.floatValue == 0f && state.backGestureRightScale.floatValue == 0f) {
            BothEdgesOffNotice(modifier = Modifier.padding(top = 8.dp))
        }

        Text(
            text = stringResource(R.string.back_gesture_apps_section),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
            modifier = Modifier.highlight(highlightSetting == "back_gesture_apps"),
        ) {
            state.backGestureAppConfigs.value.forEach { config ->
                val appIcon =
                    remember(config.packageName) {
                        runCatching {
                            BitmapPainter(
                                AppUtil
                                    .drawableToBitmap(context.packageManager.getApplicationIcon(config.packageName))
                                    .asImageBitmap(),
                            )
                        }.getOrNull()
                    }
                FeatureCard(
                    title = appLabel(config.packageName),
                    descriptionOverride =
                        stringResource(
                            R.string.back_gesture_app_summary,
                            formatEdgeScale(config.leftScale, offLabel),
                            formatEdgeScale(config.rightScale, offLabel),
                        ),
                    isEnabled = true,
                    onToggle = {},
                    onClick = { editingPackage = config.packageName },
                    iconPainter = appIcon,
                    showToggle = false,
                    hasMoreSettings = true,
                    customTrailingContent = {
                        IconButton(
                            onClick = {
                                HapticUtil.performVirtualKeyHaptic(view)
                                state.removeBackGestureAppConfig(config.packageName, context)
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_delete_24),
                                contentDescription = stringResource(R.string.action_remove),
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    },
                )
            }

            FeatureCard(
                title = R.string.back_gesture_add_app,
                description = R.string.back_gesture_add_app_desc,
                iconRes = R.drawable.rounded_add_24,
                isEnabled = true,
                onToggle = {},
                onClick = {
                    if (canWriteSecure && isAppDetectionGranted) showAppPicker = true else showPermissions = true
                },
                showToggle = false,
                hasMoreSettings = false,
            )
        }

        Text(
            text = stringResource(R.string.back_gesture_automation_note),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )
    }

    if (showAppPicker) {
        appPicker({ showAppPicker = false }) { app ->
            if (state.backGestureAppConfigs.value.none { it.packageName == app.packageName }) {
                state.updateBackGestureAppConfig(
                    BackGestureAppConfig(
                        packageName = app.packageName,
                        leftScale = state.backGestureLeftScale.floatValue,
                        rightScale = state.backGestureRightScale.floatValue,
                    ),
                    context,
                )
            }
            editingPackage = app.packageName
        }
    }

    editingPackage?.let { packageName ->
        val config =
            state.backGestureAppConfigs.value.find { it.packageName == packageName }
                ?: BackGestureAppConfig(packageName)
        BackGestureAppSheet(
            title = appLabel(packageName),
            config = config,
            onConfigChange = { state.updateBackGestureAppConfig(it, context) },
            onDismiss = { editingPackage = null },
        )
    }

    EdgeWidthPreview(
        state = preview,
        leftScale = state.backGestureLeftScale.floatValue,
        rightScale = state.backGestureRightScale.floatValue,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackGestureAppSheet(
    title: String,
    config: BackGestureAppConfig,
    onConfigChange: (BackGestureAppConfig) -> Unit,
    onDismiss: () -> Unit,
) {
    var current by remember(config.packageName) { mutableStateOf(config) }
    val preview = rememberEdgePreviewState()

    ToolkitBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            RoundedCardContainer(
                spacing = 2.dp,
                cornerRadius = 24.dp,
            ) {
                EdgeWidthSlider(
                    left = true,
                    value = current.leftScale,
                    onValueChange = {
                        current = current.copy(leftScale = it)
                        preview.show(EdgeSide.LEFT)
                    },
                    onValueChangeFinished = {
                        preview.hide()
                        onConfigChange(current)
                    },
                )
                EdgeWidthSlider(
                    left = false,
                    value = current.rightScale,
                    onValueChange = {
                        current = current.copy(rightScale = it)
                        preview.show(EdgeSide.RIGHT)
                    },
                    onValueChangeFinished = {
                        preview.hide()
                        onConfigChange(current)
                    },
                )
            }
            if (current.leftScale == 0f && current.rightScale == 0f) {
                BothEdgesOffNotice()
            }
            Text(
                text = stringResource(R.string.back_gesture_app_sheet_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        EdgeWidthPreview(
            state = preview,
            leftScale = current.leftScale,
            rightScale = current.rightScale,
        )
    }
}

@Composable
private fun BothEdgesOffNotice(modifier: Modifier = Modifier) {
    RoundedCardContainer(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        cornerRadius = 24.dp,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_info_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = stringResource(R.string.back_gesture_both_off_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Text(
                    text = stringResource(R.string.back_gesture_both_off_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }
    }
}

@Composable
private fun EdgeWidthSlider(
    left: Boolean,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier = Modifier,
    @StringRes description: Int = R.string.feat_back_gesture_width_desc,
) {
    val offLabel = stringResource(R.string.feat_back_gesture_off)
    ConfigSliderItem(
        title = stringResource(if (left) R.string.feat_back_gesture_left_title else R.string.feat_back_gesture_right_title),
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = 0f..1f,
        steps = 9,
        increment = 0.1f,
        iconRes = if (left) R.drawable.rounded_border_left_24 else R.drawable.rounded_border_right_24,
        description = stringResource(description),
        valueFormatter = { formatEdgeScale(it, offLabel) },
        modifier = modifier,
    )
}

private fun formatEdgeScale(
    scale: Float,
    offLabel: String,
): String = if (scale <= 0f) offLabel else "${(scale * 100).roundToInt()}%"

private class EdgePreviewState(
    private val scope: CoroutineScope,
) {
    var side by mutableStateOf<EdgeSide?>(null)
    var visible by mutableStateOf(false)
    private var hideJob: Job? = null

    fun show(edge: EdgeSide) {
        hideJob?.cancel()
        side = edge
        visible = true
    }

    fun hide() {
        hideJob?.cancel()
        hideJob =
            scope.launch {
                delay(700)
                visible = false
            }
    }
}

@Composable
private fun rememberEdgePreviewState(): EdgePreviewState {
    val scope = rememberCoroutineScope()
    return remember { EdgePreviewState(scope) }
}

// Drawn in a non-touchable popup window so it spans the real screen edges and never blocks the slider
@Composable
private fun EdgeWidthPreview(
    state: EdgePreviewState,
    leftScale: Float,
    rightScale: Float,
) {
    val alpha by animateFloatAsState(if (state.visible) 1f else 0f, label = "edgePreviewAlpha")
    val side = state.side ?: return
    if (alpha == 0f) return

    val context = LocalContext.current
    val density = LocalDensity.current
    val baseInset =
        remember {
            // The inset comes from the gestural nav overlay, which only the app's own resources resolve
            val res = context.resources
            val id = res.getIdentifier("config_backGestureInset", "dimen", "android")
            if (id != 0) res.getDimensionPixelSize(id) else 0
        }
    val scale = if (side == EdgeSide.LEFT) leftScale else rightScale
    val width by animateDpAsState(with(density) { (baseInset * scale).toDp() }, label = "edgePreviewWidth")

    Popup(
        popupPositionProvider =
            remember {
                object : PopupPositionProvider {
                    override fun calculatePosition(
                        anchorBounds: IntRect,
                        windowSize: IntSize,
                        layoutDirection: LayoutDirection,
                        popupContentSize: IntSize,
                    ): IntOffset = IntOffset.Zero
                }
            },
        properties =
            PopupProperties(
                flags =
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                excludeFromSystemGesture = false,
            ),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .alpha(alpha),
        ) {
            Box(
                modifier =
                    Modifier
                        .align(if (side == EdgeSide.LEFT) Alignment.CenterStart else Alignment.CenterEnd)
                        .fillMaxHeight()
                        .width(width)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
            )
        }
    }
}

@Composable
private fun BackGesturePermissionsSheet(
    isShizukuAvailable: Boolean,
    useUsageAccess: Boolean,
    onRequestShizukuPermission: () -> Unit,
    isShellGranted: Boolean,
    isAppDetectionGranted: Boolean,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    val shellPermission =
        PermissionItem(
            iconRes = R.drawable.rounded_adb_24,
            title = if (!isShizukuAvailable) R.string.perm_shizuku_title else R.string.perm_shizuku_grant_title,
            description = if (!isShizukuAvailable) R.string.perm_shizuku_desc else R.string.perm_shizuku_grant_desc,
            dependentFeatures = listOf(R.string.feat_back_gesture_title),
            actionLabel =
                when {
                    !isShizukuAvailable -> R.string.perm_shizuku_install_action
                    isShellGranted -> R.string.perm_action_granted
                    else -> R.string.perm_action_grant
                },
            action = {
                if (!isShizukuAvailable) {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rushiranpise/Shizuku-Next"))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                } else {
                    onRequestShizukuPermission()
                }
            },
            isGranted = isShellGranted,
        )

    val appDetectionPermission =
        if (useUsageAccess) {
            PermissionItem(
                iconRes = R.drawable.rounded_data_usage_24,
                title = R.string.perm_usage_stats_title,
                description = R.string.perm_usage_stats_desc_app_lock,
                dependentFeatures = listOf(R.string.back_gesture_apps_section),
                actionLabel = if (isAppDetectionGranted) R.string.perm_action_granted else R.string.perm_action_enable,
                action = { PermissionUtils.openUsageStatsSettings(context) },
                isGranted = isAppDetectionGranted,
            )
        } else {
            PermissionItem(
                iconRes = R.drawable.rounded_accessibility_new_24,
                title = R.string.perm_accessibility_title,
                description = R.string.perm_accessibility_desc_common,
                dependentFeatures = listOf(R.string.back_gesture_apps_section),
                actionLabel = if (isAppDetectionGranted) R.string.label_enabled else R.string.perm_action_enable,
                action = { PermissionUtils.openAccessibilitySettings(context) },
                isGranted = isAppDetectionGranted,
            )
        }

    PermissionsBottomSheet(
        onDismissRequest = onDismiss,
        featureTitle = R.string.feat_back_gesture_title,
        permissions = listOf(shellPermission, appDetectionPermission),
    )
}

@Composable
private fun appLabel(packageName: String): String {
    val context = LocalContext.current
    return remember(packageName) {
        runCatching {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        }.getOrDefault(packageName)
    }
}
