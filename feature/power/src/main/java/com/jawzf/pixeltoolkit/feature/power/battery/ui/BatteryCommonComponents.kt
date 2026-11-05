package com.jawzf.pixeltoolkit.feature.power.battery.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.apps.AppIcons
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.theme.Shapes
import com.jawzf.pixeltoolkit.feature.power.battery.BatteryUsageApp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SectionHeaderTitle(
    title: Any,
    modifier: Modifier = Modifier,
) {
    val displayTitle =
        when (title) {
            is Int -> stringResource(title)
            is String -> title
            else -> title.toString()
        }

    Box(modifier = modifier) {
        Text(
            text = displayTitle,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier =
                Modifier
                    .combinedClickable(
                        onClick = {},
                    ).padding(start = 8.dp),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoDetailRow(
    title: Any,
    value: String,
    iconRes: Int,
    onClick: (() -> Unit)? = null,
) {
    val view = LocalView.current

    val displayTitle =
        when (title) {
            is Int -> stringResource(title)
            is String -> title
            else -> title.toString()
        }

    Box {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceBright,
                        shape = Shapes.extraSmall,
                    ).combinedClickable(
                        onClick = {
                            if (onClick != null) {
                                HapticUtil.performVirtualKeyHaptic(view)
                                onClick()
                            }
                        },
                    ).padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = displayTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    fun parseNum(s: String): Double? {
                        val digits = s.replace("-", "").replace(Regex("[^0-9.]"), "")
                        return digits.toDoubleOrNull()
                    }

                    val oldVal = parseNum(initialState)
                    val newVal = parseNum(targetState)
                    val isIncreasing =
                        if (oldVal != null && newVal != null) newVal > oldVal else true

                    if (isIncreasing) {
                        (slideInVertically { height -> -height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> height } + fadeOut())
                    } else {
                        (slideInVertically { height -> height } + fadeIn())
                            .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                    }
                },
                label = "info_row_value",
            ) { targetVal ->
                Text(
                    text = targetVal,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BatteryLoadingIndicatorCard() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceBright,
                    shape = Shapes.extraSmall,
                ).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinearWavyProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun TopAppsBreakdownHeader(usageApps: List<BatteryUsageApp>) {
    if (usageApps.isEmpty()) return

    val totalAllMah = remember(usageApps) { usageApps.sumOf { it.powerMah }.coerceAtLeast(0.0001) }
    val majorApps =
        remember(usageApps, totalAllMah) {
            usageApps.filter { (it.powerMah / totalAllMah) * 100.0 >= 2.5 }
        }
    val remainingApps =
        remember(usageApps, majorApps) {
            usageApps.filterNot { majorApps.contains(it) }
        }
    val otherMah = remember(remainingApps) { remainingApps.sumOf { it.powerMah } }

    val hasOther = otherMah > 0.0001
    val totalSegments = majorApps.size + (if (hasOther) 1 else 0)
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(88.dp)
                .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            majorApps.forEachIndexed { index, app ->
                val weight = ((app.powerMah / totalAllMah) * 100.0).toFloat().coerceAtLeast(1f)
                var brandColor by remember(app.packageName) {
                    mutableStateOf<Color?>(null)
                }

                androidx.compose.runtime.LaunchedEffect(app.packageName) {
                    if (app.packageName != null) {
                        AppIcons.getAppBrandColor(
                            context,
                            app.packageName,
                        ) { argb ->
                            if (argb != android.graphics.Color.TRANSPARENT && argb != android.graphics.Color.GRAY) {
                                brandColor = Color(argb)
                            }
                        }
                    }
                }

                val barColor = brandColor ?: MaterialTheme.colorScheme.primaryContainer

                val shape =
                    when {
                        totalSegments == 1 -> CircleShape
                        index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes().shape
                        index == totalSegments - 1 -> ButtonGroupDefaults.connectedTrailingButtonShapes().shape
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes().shape
                    }

                Box(
                    modifier =
                        Modifier
                            .weight(weight)
                            .fillMaxHeight()
                            .clip(shape)
                            .background(barColor),
                    contentAlignment = Alignment.Center,
                ) {
                    if (app.icon != null) {
                        val bitmap =
                            remember(app.icon) { app.icon.toBitmap(48, 48).asImageBitmap() }
                        Image(
                            bitmap = bitmap,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = R.drawable.rounded_info_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            if (hasOther) {
                val otherWeight = ((otherMah / totalAllMah) * 100.0).toFloat().coerceAtLeast(1f)
                val otherShape =
                    if (majorApps.isEmpty()) CircleShape else ButtonGroupDefaults.connectedTrailingButtonShapes().shape
                Box(
                    modifier =
                        Modifier
                            .weight(otherWeight)
                            .fillMaxHeight()
                            .clip(otherShape)
                            .background(MaterialTheme.colorScheme.outlineVariant),
                )
            }
        }
    }
}
