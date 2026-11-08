package com.jawzf.pixeltoolkit.ui.features.display

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.FeatureSettingsActivity
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.FeatureCard
import com.jawzf.pixeltoolkit.core.design.cards.IconToggleItem
import com.jawzf.pixeltoolkit.core.design.components.ConfigSliderItem
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.highlight
import com.jawzf.pixeltoolkit.core.design.pickers.ColorSwatchPicker
import com.jawzf.pixeltoolkit.core.design.pickers.SegmentedPicker
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.domain.diy.displayName
import com.jawzf.pixeltoolkit.feature.apps.consciousgate.ui.CONSCIOUS_GATE_FEATURE_ID
import com.jawzf.pixeltoolkit.feature.display.duo.ui.BatteryColorOptions
import com.jawzf.pixeltoolkit.feature.island.model.IslandPriorityEntries
import com.jawzf.pixeltoolkit.feature.island.remote.ProviderDiscovery
import com.jawzf.pixeltoolkit.feature.island.remote.ProviderPriorities
import com.jawzf.pixeltoolkit.feature.island.remote.ProviderTrustStore
import com.jawzf.pixeltoolkit.feature.island.settings.IslandPriorityActions
import com.jawzf.pixeltoolkit.feature.island.settings.IslandProvidersSection
import com.jawzf.pixeltoolkit.feature.island.settings.ProviderAppIcon
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandAlarmOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandAppOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandBatteryOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandBorderOutlineOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandBriefOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandCaffeinateOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandCallOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandDevicesBatteryBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandNavigationOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandPulseShadowOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandSignalOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandSoundModeOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandTimeOptionsBottomSheet
import com.jawzf.pixeltoolkit.feature.island.settings.sheets.IslandTimerOptionsBottomSheet
import com.jawzf.pixeltoolkit.ui.core.sheets.AppSelectionSheet
import com.jawzf.pixeltoolkit.ui.features.display.actions.GestureActionPickerSheet
import com.jawzf.pixeltoolkit.ui.features.display.actions.HorizontalSlideModeSheet
import com.jawzf.pixeltoolkit.ui.features.display.actions.horizontalSlideDescription
import com.jawzf.pixeltoolkit.ui.features.display.sheets.StatusGlanceCalendarOptionsBottomSheet
import com.jawzf.pixeltoolkit.utils.PermissionUIHelper
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel
import sh.calvin.reorderable.ReorderableColumn

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun IslandSettingsUI(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    highlightSetting: String? = null,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var requestingPermissionsFor by remember { mutableStateOf<Pair<Int, List<String>>?>(null) }

    val prioritySettings = remember { SettingsRepository(context) }
    var reorderMode by remember { mutableStateOf(false) }
    var providerVersion by remember { mutableIntStateOf(0) }
    var providerOptionsFor by remember { mutableStateOf<Pair<String, String>?>(null) }
    val providerEntries = remember(providerVersion) { ProviderPriorities.entries(context, prioritySettings) }
    var savedRaw by remember { mutableStateOf(prioritySettings.getIslandPriorityOrder().orEmpty()) }
    val savedOrder = remember(savedRaw, providerEntries) { IslandPriorityEntries.normalize(savedRaw, providerEntries) }
    var workingOrder by remember { mutableStateOf(savedOrder) }
    var showMediaAppSelectionSheet by remember { mutableStateOf(false) }
    var showCalendarOptionsSheet by remember { mutableStateOf(false) }
    var showSoundModeOptionsSheet by remember { mutableStateOf(false) }
    var showDevicesBatterySheet by remember { mutableStateOf(false) }
    var showTimeOptionsSheet by remember { mutableStateOf(false) }
    var showBatteryOptionsSheet by remember { mutableStateOf(false) }
    var showTimerOptionsSheet by remember { mutableStateOf(false) }
    var showCaffeinateOptionsSheet by remember { mutableStateOf(false) }
    var showCallOptionsSheet by remember { mutableStateOf(false) }
    var showSignalOptionsSheet by remember { mutableStateOf(false) }
    var showAlarmOptionsSheet by remember { mutableStateOf(false) }
    var showNavigationOptionsSheet by remember { mutableStateOf(false) }
    var showBriefOptionsSheet by remember { mutableStateOf(highlightSetting == "island_brief_show_alarm") }
    var pickingGesture by remember { mutableStateOf<String?>(null) }
    var showSlideModeSheet by remember { mutableStateOf(false) }

    if (requestingPermissionsFor != null) {
        val (titleRes, permKeys) = requestingPermissionsFor!!
        val permissionItems = PermissionUIHelper.getPermissionItems(permKeys, context, viewModel)
        PermissionsBottomSheet(
            onDismissRequest = {
                requestingPermissionsFor = null
                viewModel.check(context)
            },
            featureTitle = stringResource(titleRes),
            permissions = permissionItems,
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_notifications_unread_24,
                title = stringResource(R.string.island_enable_title),
                isChecked = viewModel.island.isIslandEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    if (checked) {
                        val missingPermissions = mutableListOf<String>()
                        if (!viewModel.isAccessibilityEnabled.value) {
                            missingPermissions.add("ACCESSIBILITY")
                        }
                        if (!viewModel.isNotificationListenerEnabled.value) {
                            missingPermissions.add("NOTIFICATION_LISTENER")
                        }

                        if (missingPermissions.isNotEmpty()) {
                            requestingPermissionsFor = Pair(R.string.island_title, missingPermissions)
                        } else {
                            viewModel.island.setIslandEnabled(true)
                        }
                    } else {
                        viewModel.island.setIslandEnabled(false)
                    }
                },
                modifier = Modifier.highlight(highlightSetting == "island_enabled"),
            )
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            ISLAND_SUB_PAGES.forEach { (featureId, titleRes, iconRes) ->
                FeatureCard(
                    title = titleRes,
                    iconRes = iconRes,
                    isEnabled = true,
                    showToggle = false,
                    onToggle = {},
                    onClick = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        context.startActivity(
                            Intent(context, FeatureSettingsActivity::class.java).apply {
                                putExtra("feature", featureId)
                            },
                        )
                    },
                )
            }
        }

        Text(
            text = stringResource(R.string.duo_section_what_to_show),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceBright, MaterialTheme.shapes.extraSmall)
                    .padding(top = 12.dp)
                    .highlight(highlightSetting == "island_max_items"),
            ) {
                Text(
                    text = stringResource(R.string.island_max_items_title),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                SegmentedPicker(
                    items = listOf(1, 2, 3, 4),
                    selectedItem = viewModel.island.islandMaxItems.value,
                    onItemSelected = {
                        HapticUtil.performVirtualKeyHaptic(view)
                        viewModel.island.setIslandMaxItems(it)
                    },
                    labelProvider = { it.toString() },
                    title = R.string.island_max_items_title,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            val renderEntry: @Composable (String, (@Composable () -> Unit)?) -> Unit = { entryId, handle ->
                when (entryId) {
                    "calls" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_call_24,
                        title = stringResource(R.string.island_show_calls_title),
                        isChecked = viewModel.island.isIslandShowCalls.value && PermissionUtils.hasCallPermissions(context),
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            if (checked && !PermissionUtils.hasCallPermissions(context)) {
                                requestingPermissionsFor = Pair(R.string.island_title, listOf("READ_PHONE_STATE", "ANSWER_PHONE_CALLS", "READ_CONTACTS"))
                            } else {
                                viewModel.island.setIslandShowCalls(checked)
                            }
                        },
                        onSettingsClick = { showCallOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_calls"),
                    )
                    "notifications" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_notifications_unread_24,
                        title = stringResource(R.string.island_section_notifications),
                        isChecked = viewModel.island.isIslandShowNotifications.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowNotifications(checked)
                        },
                        onSettingsClick = {
                            HapticUtil.performVirtualKeyHaptic(view)
                            context.startActivity(
                                Intent(context, FeatureSettingsActivity::class.java).apply {
                                    putExtra("feature", ISLAND_NOTIFICATIONS_FEATURE_ID)
                                },
                            )
                        },
                        modifier = Modifier.highlight(highlightSetting == "island_show_notifications"),
                    )
                    "time" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_schedule_24,
                        title = stringResource(R.string.island_show_time_title),
                        isChecked = viewModel.island.isIslandShowTimeBattery.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowTimeBattery(checked)
                        },
                        onSettingsClick = { showTimeOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_time_battery"),
                    )
                    "battery" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_battery_android_0_24,
                        title = stringResource(R.string.island_show_battery_title),
                        isChecked = viewModel.island.isIslandShowBattery.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowBattery(checked)
                        },
                        onSettingsClick = { showBatteryOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_battery"),
                    )
                    "flashlight" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_flashlight_on_24,
                        title = stringResource(R.string.feat_flashlight_title),
                        isChecked = viewModel.island.isIslandShowFlashlight.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowFlashlight(checked)
                        },
                        modifier = Modifier.highlight(highlightSetting == "island_show_flashlight"),
                    )
                    "timer" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_timer_24,
                        title = stringResource(R.string.island_show_timers_title),
                        isChecked = viewModel.island.isIslandShowTimers.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowTimers(checked)
                        },
                        onSettingsClick = { showTimerOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_timers"),
                    )
                    "media" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_motion_play_24,
                        title = stringResource(R.string.duo_show_media_title),
                        isChecked = viewModel.island.isIslandShowMedia.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowMedia(checked)
                        },
                        onSettingsClick = { showMediaAppSelectionSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_media"),
                    )
                    "conscious_gate" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_pause_24,
                        title = stringResource(R.string.feat_conscious_gate_title),
                        isChecked = viewModel.island.isIslandShowConsciousGate.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowConsciousGate(checked)
                        },
                        onSettingsClick = {
                            val intent = Intent(context, FeatureSettingsActivity::class.java).apply {
                                putExtra("feature", CONSCIOUS_GATE_FEATURE_ID)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.highlight(highlightSetting == "island_show_conscious_gate"),
                    )
                    "caffeinate" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_coffee_24,
                        title = stringResource(R.string.feat_caffeinate_title),
                        isChecked = viewModel.island.isIslandShowCaffeinate.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowCaffeinate(checked)
                        },
                        onSettingsClick = { showCaffeinateOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_caffeinate"),
                    )
                    "calendar" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_calendar_today_24,
                        title = stringResource(R.string.status_glance_show_calendar_title),
                        isChecked = viewModel.island.isIslandShowCalendar.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            if (checked && !viewModel.isCalendarPermissionGranted.value) {
                                requestingPermissionsFor = Pair(R.string.island_title, listOf("READ_CALENDAR"))
                            } else {
                                viewModel.island.setIslandShowCalendar(checked)
                            }
                        },
                        onSettingsClick = { showCalendarOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_calendar"),
                    )
                    "sound_mode" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_volume_up_24,
                        title = stringResource(R.string.island_show_sound_mode_title),
                        isChecked = viewModel.island.isIslandShowSoundMode.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowSoundMode(checked)
                        },
                        onSettingsClick = { showSoundModeOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_sound_mode"),
                    )
                    "network" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_android_wifi_3_bar_24,
                        title = stringResource(R.string.island_show_network_title),
                        isChecked = viewModel.island.isIslandShowNetwork.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowNetwork(checked)
                        },
                        onSettingsClick = { showSignalOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_network"),
                    )
                    "alarm" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_alarm_24,
                        title = stringResource(R.string.island_show_alarm_title),
                        isChecked = viewModel.island.isIslandShowAlarm.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowAlarm(checked)
                        },
                        onSettingsClick = { showAlarmOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_alarm"),
                    )
                    "navigation" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.round_navigation_24,
                        title = stringResource(R.string.island_show_navigation_title),
                        isChecked = viewModel.island.isIslandShowNavigation.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowNavigation(checked)
                        },
                        onSettingsClick = { showNavigationOptionsSheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_navigation"),
                    )
                    "hotspot" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_wifi_tethering_24,
                        title = stringResource(R.string.island_show_hotspot_title),
                        isChecked = viewModel.island.isIslandShowHotspot.value,
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            viewModel.island.setIslandShowHotspot(checked)
                            if (checked) {
                                val missing = mutableListOf<String>()
                                if (!PermissionUtils.hasUsageStatsPermission(context)) missing.add("USAGE_STATS")
                                if (!ShellUtils.isAvailable(context) || !ShellUtils.hasPermission(context)) missing.add("SHIZUKU")
                                if (missing.isNotEmpty()) requestingPermissionsFor = Pair(R.string.island_title, missing)
                            }
                        },
                        modifier = Modifier.highlight(highlightSetting == "island_show_hotspot"),
                    )
                    "devices" -> IconToggleItem(
                        dragHandle = handle,
                        iconRes = R.drawable.rounded_bluetooth_24,
                        title = stringResource(R.string.island_show_devices_title),
                        isChecked = viewModel.island.isIslandShowDevices.value && PermissionUtils.hasBluetoothPermission(context),
                        onCheckedChange = { checked ->
                            HapticUtil.performVirtualKeyHaptic(view)
                            if (checked && !PermissionUtils.hasBluetoothPermission(context)) {
                                requestingPermissionsFor = Pair(R.string.island_title, listOf("BLUETOOTH_CONNECT", "BLUETOOTH_SCAN"))
                            } else {
                                viewModel.island.setIslandShowDevices(checked)
                            }
                        },
                        onSettingsClick = { showDevicesBatterySheet = true },
                        modifier = Modifier.highlight(highlightSetting == "island_show_devices"),
                    )
                    else -> if (IslandPriorityEntries.isProvider(entryId)) {
                        val packageName = IslandPriorityEntries.providerPackage(entryId)
                        val provider = remember(packageName, providerVersion) { ProviderDiscovery.find(context, packageName) }
                        if (provider != null) {
                            IconToggleItem(
                                dragHandle = handle,
                                leadingIcon = ProviderAppIcon(provider.packageName),
                                title = provider.label,
                                isChecked = !ProviderTrustStore(prioritySettings).isHiddenFromIsland(packageName),
                                onCheckedChange = { checked ->
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    ProviderTrustStore(prioritySettings).setHiddenFromIsland(packageName, !checked)
                                    providerVersion++
                                },
                                onSettingsClick = { providerOptionsFor = provider.packageName to provider.label },
                            )
                        }
                    }
                }
            }
            renderEntry("calls", null)
            renderEntry("time", null)
            ReorderableColumn(
                list = if (reorderMode) workingOrder else savedOrder,
                onSettle = { from, to -> workingOrder = workingOrder.toMutableList().apply { add(to, removeAt(from)) } },
                verticalArrangement = Arrangement.spacedBy(2.dp),
                onMove = { HapticUtil.performVirtualKeyHaptic(view) },
            ) { _, entryId, _ ->
                key(entryId) {
                    ReorderableItem {
                        renderEntry(
                            entryId,
                            if (reorderMode) {
                                {
                                    IconButton(
                                        modifier = Modifier.draggableHandle(),
                                        onClick = {},
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.rounded_drag_handle_24),
                                            contentDescription = stringResource(R.string.content_desc_drag_reorder),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(24.dp),
                                        )
                                    }
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        }

        IslandPriorityActions(
            editing = reorderMode,
            onStart = {
                workingOrder = savedOrder
                reorderMode = true
            },
            onSave = {
                prioritySettings.setIslandPriorityOrder(workingOrder.takeIf { it != IslandPriorityEntries.defaultOrder(providerEntries) })
                savedRaw = workingOrder
                reorderMode = false
            },
            onReset = { workingOrder = IslandPriorityEntries.defaultOrder(providerEntries) },
        )

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_calendar_today_24,
                title = stringResource(R.string.island_brief_title),
                description = stringResource(R.string.island_brief_desc),
                isChecked = viewModel.island.isIslandBriefEnabled.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.island.setIslandBriefEnabled(checked)
                },
                onSettingsClick = { showBriefOptionsSheet = true },
                modifier = Modifier.highlight(highlightSetting == "island_brief_enabled"),
            )
        }

        IslandProvidersSection(onChanged = { providerVersion++ })

        Text(
            text = stringResource(R.string.duo_section_actions),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp),
        )

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_front_hand_24,
                title = stringResource(R.string.duo_action_long_press_title),
                description =
                    if (viewModel.island.isIslandBriefEnabled.value) {
                        stringResource(R.string.island_brief_title)
                    } else {
                        viewModel.island.islandLongPressAction.value
                            ?.displayName(context) ?: stringResource(R.string.duo_action_none)
                    },
                showToggle = false,
                enabled = !viewModel.island.isIslandBriefEnabled.value,
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    pickingGesture = "long_press"
                },
                modifier = Modifier.highlight(highlightSetting == "island_long_press_action"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_favorite_24,
                title = stringResource(R.string.island_like_while_playing_title),
                isChecked = viewModel.island.isIslandLikeWhilePlaying.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.island.setIslandLikeWhilePlayingEnabled(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_like_while_playing"),
            )
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_compare_arrows_24,
                title = stringResource(R.string.duo_action_horizontal_slide_title),
                description = horizontalSlideDescription(viewModel.island.islandSlideMode.value, viewModel.island.isIslandSlideTrack.value),
                showToggle = false,
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    showSlideModeSheet = true
                },
                modifier = Modifier.highlight(highlightSetting == "island_slide_mode"),
            )

            IconToggleItem(
                iconRes = R.drawable.rounded_skip_next_24,
                title = stringResource(R.string.duo_action_horizontal_slide_track),
                description = stringResource(R.string.duo_action_horizontal_slide_track_desc),
                isChecked = viewModel.island.isIslandSlideTrack.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.island.setIslandSlideTrackEnabled(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_slide_track"),
            )

            AnimatedVisibility(
                visible = viewModel.island.isIslandSlideTrack.value || viewModel.island.islandSlideMode.value == "sound_mode",
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_compare_arrows_24,
                    title = stringResource(R.string.duo_slide_mirror_direction_title),
                    isChecked = viewModel.island.isIslandSlideInvertDirection.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        viewModel.island.setIslandSlideInvertDirection(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "island_slide_invert_direction"),
                )
            }
        }

        RoundedCardContainer(
            spacing = 2.dp,
            cornerRadius = 24.dp,
        ) {
            IconToggleItem(
                iconRes = R.drawable.rounded_touch_app_24,
                title = stringResource(R.string.island_always_gestures_title),
                description = stringResource(R.string.island_always_gestures_desc),
                isChecked = viewModel.island.isIslandAlwaysGestures.value,
                onCheckedChange = { checked ->
                    HapticUtil.performVirtualKeyHaptic(view)
                    viewModel.island.setIslandAlwaysGestures(checked)
                },
                modifier = Modifier.highlight(highlightSetting == "island_always_gestures"),
            )
        }

        AnimatedVisibility(
            visible = !viewModel.island.isIslandShowTimeBattery.value,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Text(
                text = stringResource(R.string.island_gesture_visuals_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        AnimatedVisibility(
            visible = viewModel.duo.isDuoEnabled.value && viewModel.island.isIslandEnabled.value,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            RoundedCardContainer(
                spacing = 2.dp,
                cornerRadius = 24.dp,
            ) {
                IconToggleItem(
                    iconRes = R.drawable.rounded_link_24,
                    title = stringResource(R.string.island_combine_with_duo),
                    isChecked = viewModel.duo.isDuoIslandCombined.value,
                    onCheckedChange = { checked ->
                        HapticUtil.performVirtualKeyHaptic(view)
                        viewModel.duo.setDuoIslandCombined(checked)
                    },
                    modifier = Modifier.highlight(highlightSetting == "duo_island_combined"),
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    if (pickingGesture != null) {
        GestureActionPickerSheet(
            viewModel = viewModel,
            currentAction = viewModel.island.islandLongPressAction.value,
            onActionSelected = viewModel.island::setIslandLongPressAction,
            onPickerClosed = { pickingGesture = null },
        )
    }

    if (showSlideModeSheet) {
        HorizontalSlideModeSheet(
            mode = viewModel.island.islandSlideMode.value,
            onModeSelected = viewModel.island::setIslandSlideMode,
            onDismissRequest = { showSlideModeSheet = false },
        )
    }

    if (showMediaAppSelectionSheet) {
        AppSelectionSheet(
            title = stringResource(R.string.island_media_config_title),
            onDismissRequest = { showMediaAppSelectionSheet = false },
            onLoadApps = { viewModel.island.loadIslandMediaApps(it) },
            onSaveApps = { ctx, apps -> viewModel.island.saveIslandMediaApps(ctx, apps) },
            onAppToggle = { ctx, pkg, enabled -> viewModel.island.updateIslandMediaAppEnabled(ctx, pkg, enabled) },
            context = context,
            headerContent = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    RoundedCardContainer(spacing = 2.dp, cornerRadius = 24.dp) {
                        if (viewModel.island.isIslandLineStageEnabled.value) {
                            IconToggleItem(
                                iconRes = R.drawable.rounded_music_note_24,
                                title = stringResource(R.string.island_media_peek_song_change_title),
                                isChecked = viewModel.island.isIslandMediaPeekSongChange.value,
                                onCheckedChange = { checked ->
                                    HapticUtil.performVirtualKeyHaptic(view)
                                    viewModel.island.setIslandMediaPeekSongChange(checked)
                                },
                                modifier = Modifier.highlight(highlightSetting == "island_media_peek_song_change"),
                            )
                        }
                        IconToggleItem(
                            iconRes = R.drawable.rounded_pause_24,
                            title = stringResource(R.string.island_media_keep_when_paused_title),
                            isChecked = viewModel.island.isIslandMediaKeepWhenPaused.value,
                            onCheckedChange = { checked ->
                                HapticUtil.performVirtualKeyHaptic(view)
                                viewModel.island.setIslandMediaKeepWhenPaused(checked)
                            },
                            modifier = Modifier.highlight(highlightSetting == "island_media_keep_when_paused"),
                        )
                        IconToggleItem(
                            iconRes = R.drawable.rounded_skip_previous_24,
                            title = stringResource(R.string.island_media_show_previous_title),
                            isChecked = viewModel.island.isIslandMediaShowPrevious.value,
                            onCheckedChange = { checked ->
                                HapticUtil.performVirtualKeyHaptic(view)
                                viewModel.island.setIslandMediaShowPrevious(checked)
                            },
                            modifier = Modifier.highlight(highlightSetting == "island_media_show_previous"),
                        )
                        IconToggleItem(
                            iconRes = R.drawable.rounded_favorite_24,
                            title = stringResource(R.string.island_media_show_like_title),
                            isChecked = viewModel.island.isIslandMediaShowLike.value,
                            onCheckedChange = { checked ->
                                HapticUtil.performVirtualKeyHaptic(view)
                                viewModel.island.setIslandMediaShowLike(checked)
                            },
                            modifier = Modifier.highlight(highlightSetting == "island_media_show_like"),
                        )
                    }
                    Text(
                        text = stringResource(R.string.duo_media_skip_apps_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            },
        )
    }

    if (showBriefOptionsSheet) {
        IslandBriefOptionsBottomSheet(
            state = viewModel.island,
            onDismissRequest = { showBriefOptionsSheet = false },
            highlightSetting = highlightSetting,
        )
    }

    if (showSoundModeOptionsSheet) {
        IslandSoundModeOptionsBottomSheet(
            state = viewModel.island,
            onDismissRequest = { showSoundModeOptionsSheet = false },
        )
    }

    if (showNavigationOptionsSheet) {
        IslandNavigationOptionsBottomSheet(
            state = viewModel.island,
            onDismissRequest = { showNavigationOptionsSheet = false },
        )
    }

    if (showAlarmOptionsSheet) {
        IslandAlarmOptionsBottomSheet(
            state = viewModel.island,
            onDismissRequest = { showAlarmOptionsSheet = false },
        )
    }

    if (showSignalOptionsSheet) {
        IslandSignalOptionsBottomSheet(onDismissRequest = { showSignalOptionsSheet = false })
    }

    providerOptionsFor?.let { (pkg, label) ->
        IslandAppOptionsBottomSheet(packageName = pkg, label = label, onDismissRequest = { providerOptionsFor = null })
    }

    if (showCallOptionsSheet) {
        IslandCallOptionsBottomSheet(onDismissRequest = { showCallOptionsSheet = false })
    }

    if (showTimerOptionsSheet) {
        IslandTimerOptionsBottomSheet(
            state = viewModel.island,
            appSelectionSheet = { onDismiss ->
                AppSelectionSheet(
                    onDismissRequest = onDismiss,
                    onLoadApps = { viewModel.island.loadIslandTimersSelectedApps(it) },
                    onSaveApps = { ctx, apps -> viewModel.island.saveIslandTimersSelectedApps(ctx, apps) },
                    onAppToggle = { ctx, pkg, enabled -> viewModel.island.updateIslandTimersAppEnabled(ctx, pkg, enabled) },
                    context = context,
                )
            },
            onDismissRequest = { showTimerOptionsSheet = false },
        )
    }

    if (showCaffeinateOptionsSheet) {
        IslandCaffeinateOptionsBottomSheet(onDismissRequest = { showCaffeinateOptionsSheet = false })
    }

    if (showTimeOptionsSheet) {
        IslandTimeOptionsBottomSheet(
            onDismissRequest = { showTimeOptionsSheet = false },
        )
    }

    if (showBatteryOptionsSheet) {
        IslandBatteryOptionsBottomSheet(
            state = viewModel.island,
            batteryColorOptions = {
                BatteryColorOptions(
                    state = viewModel.duo,
                    isIslandBatteryIdleColorEnabled = viewModel.island.isIslandBatteryIdleColorEnabled.value,
                    islandBatteryIdleColor = viewModel.island.islandBatteryIdleColor.value,
                    onIslandBatteryIdleColorEnabledChange = { viewModel.island.setIslandBatteryIdleColorEnabled(it) },
                    onIslandBatteryIdleColorChange = { viewModel.island.setIslandBatteryIdleColor(it) },
                    showIdleColor = true,
                    showLevels = true,
                )
            },
            onDismissRequest = { showBatteryOptionsSheet = false },
        )
    }

    if (showDevicesBatterySheet) {
        IslandDevicesBatteryBottomSheet(
            state = viewModel.island,
            batteryColorOptions = {
                BatteryColorOptions(
                    state = viewModel.duo,
                    isIslandBatteryIdleColorEnabled = viewModel.island.isIslandBatteryIdleColorEnabled.value,
                    islandBatteryIdleColor = viewModel.island.islandBatteryIdleColor.value,
                    onIslandBatteryIdleColorEnabledChange = { viewModel.island.setIslandBatteryIdleColorEnabled(it) },
                    onIslandBatteryIdleColorChange = { viewModel.island.setIslandBatteryIdleColor(it) },
                    showIdleColor = true,
                    showLevels = true,
                )
            },
            onDismissRequest = { showDevicesBatterySheet = false },
        )
    }

    if (showCalendarOptionsSheet) {
        StatusGlanceCalendarOptionsBottomSheet(
            viewModel = viewModel,
            onDismissRequest = { showCalendarOptionsSheet = false },
            allowIconEdit = true,
        )
    }
}
