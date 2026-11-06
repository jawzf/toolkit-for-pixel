package com.jawzf.pixeltoolkit.feature.display.aod.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.ConfigPickerItem
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.components.SegmentedDropdownMenuItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.feature.display.aod.AodState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AodWallpaperSettingsUI(
    state: AodState,
    isAccessibilityEnabled: Boolean,
    isStoragePermissionGranted: Boolean,
    isNotificationListenerEnabled: Boolean,
    isWriteSecureSettingsEnabled: Boolean,
    permissionItemsFor: (List<String>) -> List<PermissionItem>,
    onRefreshPermissions: () -> Unit,
    mediaAppSelectionSheet: @Composable (onDismiss: () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current

    var showMediaAppSelectionSheet by remember { mutableStateOf(false) }
    var requestingPermissionsFor by remember { mutableStateOf<Pair<Int, List<String>>?>(null) }

    LaunchedEffect(Unit) {
        onRefreshPermissions()
    }

    if (requestingPermissionsFor != null) {
        val (featureTitle, permKeys) = requestingPermissionsFor!!
        val permissionItems = permissionItemsFor(permKeys)
        PermissionsBottomSheet(
            onDismissRequest = {
                requestingPermissionsFor = null
                onRefreshPermissions()
            },
            featureTitle = featureTitle,
            permissions = permissionItems,
        )
    }

    val isWallpaperEnabled = state.isAodWallpaperEnabled.value
    val opacity = state.aodWallpaperOpacity.floatValue
    val blurRadius = state.aodWallpaperBlur.floatValue
    val vignetteIntensity = state.aodWallpaperVignette.floatValue
    val blackThreshold = state.aodWallpaperBlackThreshold.floatValue

    fun requestMainPermissions() {
        val missing = mutableListOf<String>()
        if (!isAccessibilityEnabled) missing.add("ACCESSIBILITY")
        if (!isStoragePermissionGranted) missing.add("STORAGE")
        if (missing.isNotEmpty()) {
            requestingPermissionsFor = Pair(R.string.feat_aod_wallpaper_title, missing)
        }
    }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_wallpaper_24,
                title = stringResource(R.string.feat_aod_wallpaper_title),
                isChecked = isWallpaperEnabled,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked) {
                        if (!isAccessibilityEnabled || !isStoragePermissionGranted) {
                            requestMainPermissions()
                        } else {
                            state.toggleAodWallpaperEnabled(true)
                            state.loadCurrentWallpaperBitmap(context)
                        }
                    } else {
                        state.toggleAodWallpaperEnabled(false)
                    }
                },
                enabled = true,
                onDisabledClick = { requestMainPermissions() },
                modifier = Modifier.highlight(highlightSetting == "aod_wallpaper"),
            )

            AnimatedVisibility(
                visible = isWallpaperEnabled,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_do_not_disturb_on_24,
                    title = stringResource(R.string.feat_aod_wallpaper_disable_on_dnd),
                    isChecked = state.isAodWallpaperDisableOnDnd.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setAodWallpaperDisableOnDnd(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "aod_wallpaper_disable_on_dnd"),
                )
            }

            AnimatedVisibility(
                visible = isWallpaperEnabled,
                enter = expandVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut(animationSpec = tween(durationMillis = 300)),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    ConfigSliderItem(
                        title = stringResource(R.string.feat_aod_wallpaper_opacity),
                        value = (opacity * 100f).coerceIn(10f, 75f),
                        onValueChange = { state.setAodWallpaperOpacity(it / 100f) },
                        valueRange = 10f..75f,
                        increment = 5f,
                        valueFormatter = { "${it.toInt()}%" },
                        iconRes = R.drawable.rounded_visibility_24,
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.feat_aod_wallpaper_blur),
                        value = blurRadius,
                        onValueChange = { state.setAodWallpaperBlur(it) },
                        valueRange = 0f..25f,
                        increment = 1f,
                        valueFormatter = { if (it == 0f) "Off" else "${it.toInt()}" },
                        iconRes = R.drawable.rounded_blur_on_24,
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.feat_aod_wallpaper_vignette),
                        value = vignetteIntensity,
                        onValueChange = { state.setAodWallpaperVignette(it) },
                        valueRange = 0f..100f,
                        increment = 5f,
                        valueFormatter = { if (it == 0f) "Off" else "${it.toInt()}%" },
                        iconRes = R.drawable.rounded_grain_24,
                    )

                    ConfigSliderItem(
                        title = stringResource(R.string.feat_aod_wallpaper_black_threshold),
                        value = blackThreshold,
                        onValueChange = { state.setAodWallpaperBlackThreshold(it) },
                        valueRange = 0f..50f,
                        increment = 1f,
                        valueFormatter = { if (it == 0f) "Off" else "${it.toInt()}" },
                        iconRes = R.drawable.rounded_invert_colors_24,
                    )

                    val timeoutOptions =
                        listOf(
                            "never" to stringResource(R.string.feat_aod_wallpaper_timeout_never),
                            "5s" to stringResource(R.string.feat_aod_wallpaper_timeout_5s),
                            "15s" to stringResource(R.string.feat_aod_wallpaper_timeout_15s),
                            "30s" to stringResource(R.string.feat_aod_wallpaper_timeout_30s),
                            "1m" to stringResource(R.string.feat_aod_wallpaper_timeout_1m),
                            "3m" to stringResource(R.string.feat_aod_wallpaper_timeout_3m),
                            "5m" to stringResource(R.string.feat_aod_wallpaper_timeout_5m),
                            "10m" to stringResource(R.string.feat_aod_wallpaper_timeout_10m),
                            "custom" to stringResource(R.string.feat_aod_wallpaper_timeout_custom),
                        )
                    val currentTimeout = state.aodWallpaperTimeout.value
                    val selectedLabel =
                        timeoutOptions.firstOrNull { it.first == currentTimeout }?.second
                            ?: stringResource(R.string.feat_aod_wallpaper_timeout_3m)
                    ConfigPickerItem(
                        title = stringResource(R.string.feat_aod_wallpaper_timeout),
                        selectedValue = selectedLabel,
                        iconRes = R.drawable.rounded_timer_24,
                    ) {
                        timeoutOptions.forEach { (optionValue, label) ->
                            SegmentedDropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    state.setAodWallpaperTimeout(optionValue)
                                },
                            )
                        }
                    }

                    AnimatedVisibility(
                        visible = currentTimeout == "custom",
                        enter = expandVertically(animationSpec = tween(durationMillis = 300)) + fadeIn(animationSpec = tween(durationMillis = 300)),
                        exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) + fadeOut(animationSpec = tween(durationMillis = 300)),
                    ) {
                        val customTimeout = state.aodWallpaperCustomTimeout.floatValue
                        ConfigSliderItem(
                            title = stringResource(R.string.feat_aod_wallpaper_custom_timeout_slider),
                            value = customTimeout,
                            onValueChange = { state.setAodWallpaperCustomTimeout(it) },
                            valueRange = 1f..180f,
                            increment = 1f,
                            valueFormatter = {
                                val secs = it.toInt()
                                if (secs < 60) "${secs}s" else if (secs % 60 == 0) "${secs / 60}m" else "${secs / 60}m ${secs % 60}s"
                            },
                            iconRes = R.drawable.rounded_timer_24,
                        )
                    }
                }
            }
        }

        val isAlbumArtEnabled = state.isAodWallpaperUseAlbumArt.value
        val isKeepOnMedia = state.isAodWallpaperKeepOnMedia.value
        val isTimeoutNever = state.aodWallpaperTimeout.value == "never" || state.aodWallpaperTimeout.value == "0"

        Text(
            text = stringResource(R.string.feat_aod_wallpaper_media_section_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_music_note_24,
                title = stringResource(R.string.feat_aod_wallpaper_use_album_art),
                isChecked = isAlbumArtEnabled,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked) {
                        if (!isNotificationListenerEnabled) {
                            requestingPermissionsFor =
                                Pair(R.string.feat_aod_wallpaper_use_album_art, listOf("NOTIFICATION_LISTENER"))
                        } else {
                            state.setAodWallpaperUseAlbumArt(true)
                        }
                    } else {
                        state.setAodWallpaperUseAlbumArt(false)
                    }
                },
                enabled = true,
                onDisabledClick = {
                    if (!isNotificationListenerEnabled) {
                        requestingPermissionsFor =
                            Pair(R.string.feat_aod_wallpaper_use_album_art, listOf("NOTIFICATION_LISTENER"))
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "aod_wallpaper_album_art"),
            )

            AnimatedVisibility(
                visible = isAlbumArtEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_timer_24,
                    title = stringResource(R.string.feat_aod_wallpaper_keep_on_media),
                    isChecked = if (isTimeoutNever) true else isKeepOnMedia,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setAodWallpaperKeepOnMedia(checked)
                    },
                    enabled = !isTimeoutNever,
                    modifier = Modifier.highlight(highlightSetting == "aod_wallpaper_keep_on_media"),
                )
            }

            AnimatedVisibility(
                visible = isAlbumArtEnabled,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_apps_24,
                    title = stringResource(R.string.feat_aod_wallpaper_media_apps),
                    showToggle = false,
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        showMediaAppSelectionSheet = true
                    },
                )
            }
        }

        val isExtendedMedia = state.isAodWallpaperExtendedMedia.value

        Text(
            text = stringResource(R.string.feat_aod_wallpaper_extended_section_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        RoundedCardContainer {
            IconToggleItem(
                iconRes = R.drawable.rounded_music_note_24,
                title = stringResource(R.string.feat_aod_wallpaper_extended_media),
                isChecked = isExtendedMedia,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    val missing =
                        buildList {
                            if (!isNotificationListenerEnabled) add("NOTIFICATION_LISTENER")
                            if (!isWriteSecureSettingsEnabled) add("WRITE_SECURE_SETTINGS")
                        }
                    if (checked && missing.isNotEmpty()) {
                        requestingPermissionsFor =
                            Pair(R.string.feat_aod_wallpaper_extended_media, missing)
                    } else {
                        state.setAodWallpaperExtendedMedia(checked)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "aod_wallpaper_extended_media"),
            )

            AnimatedVisibility(
                visible = isExtendedMedia,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_apps_24,
                    title = stringResource(R.string.feat_aod_wallpaper_extended_app_icon),
                    isChecked = state.isAodWallpaperExtendedAppIcon.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        state.setAodWallpaperExtendedAppIcon(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "aod_wallpaper_extended_app_icon"),
                )
            }

            AnimatedVisibility(
                visible = isExtendedMedia,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                ConfigSliderItem(
                    title = stringResource(R.string.status_glance_font_size_title),
                    value = (state.aodWallpaperExtendedTextScale.floatValue * 100f).coerceIn(70f, 200f),
                    onValueChange = { state.setAodWallpaperExtendedTextScale(it / 100f) },
                    valueRange = 70f..200f,
                    increment = 5f,
                    valueFormatter = { "${it.toInt()}%" },
                    iconRes = R.drawable.rounded_format_size_24,
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        if (showMediaAppSelectionSheet) {
            mediaAppSelectionSheet { showMediaAppSelectionSheet = false }
        }
    }
}
