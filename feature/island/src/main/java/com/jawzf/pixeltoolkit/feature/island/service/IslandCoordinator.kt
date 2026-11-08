package com.jawzf.pixeltoolkit.feature.island.service

import android.accessibilityservice.AccessibilityService
import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.data.duo.DuoSettings
import com.jawzf.pixeltoolkit.core.data.island.IslandSettings
import com.jawzf.pixeltoolkit.core.data.prefs.StatusBarPrefs
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.shell.ShellUtils
import com.jawzf.pixeltoolkit.core.platform.statusbar.StatusBarManager
import com.jawzf.pixeltoolkit.feature.island.gestures.CompactGestureController
import com.jawzf.pixeltoolkit.feature.island.gestures.CompactGestures
import com.jawzf.pixeltoolkit.feature.island.gestures.HiddenGestureDetector
import com.jawzf.pixeltoolkit.feature.island.model.CompactCell
import com.jawzf.pixeltoolkit.feature.island.model.CompactPlacement
import com.jawzf.pixeltoolkit.feature.island.model.ExpandedContent
import com.jawzf.pixeltoolkit.feature.island.model.IslandItem
import com.jawzf.pixeltoolkit.feature.island.model.IslandPlugin
import com.jawzf.pixeltoolkit.feature.island.model.IslandPluginContext
import com.jawzf.pixeltoolkit.feature.island.model.IslandPriority
import com.jawzf.pixeltoolkit.feature.island.model.IslandPriorityEntries
import com.jawzf.pixeltoolkit.feature.island.model.IslandStage
import com.jawzf.pixeltoolkit.feature.island.model.LineContent
import com.jawzf.pixeltoolkit.feature.island.model.SideBubble
import com.jawzf.pixeltoolkit.feature.island.model.SideBubblePriority
import com.jawzf.pixeltoolkit.feature.island.model.StackIcon
import com.jawzf.pixeltoolkit.feature.island.plugins.alarm.AlarmPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.brief.BriefPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.caffeinate.CaffeinatePlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.calendar.CalendarPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.call.CallPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.consciousgate.ConsciousGatePlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.devices.DevicesPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.flashlight.FlashlightPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.hotspot.HotspotPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.media.MediaPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.navigation.NavigationPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.network.NetworkPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.notifications.NotificationsPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.progress.ProgressPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.signal.SignalPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.soundmode.SoundModePlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.timebattery.TimeBatteryPlugin
import com.jawzf.pixeltoolkit.feature.island.plugins.timer.TimerPlugin
import com.jawzf.pixeltoolkit.feature.island.remote.ProviderPriorities
import com.jawzf.pixeltoolkit.feature.island.remote.ProviderTrustStore
import com.jawzf.pixeltoolkit.feature.island.remote.RemoteProvidersPlugin
import com.jawzf.pixeltoolkit.feature.island.state.CameraAnchor
import com.jawzf.pixeltoolkit.feature.island.state.CameraGeometry
import com.jawzf.pixeltoolkit.feature.island.state.Cancellable
import com.jawzf.pixeltoolkit.feature.island.state.DelayScheduler
import com.jawzf.pixeltoolkit.feature.island.state.IslandController
import com.jawzf.pixeltoolkit.feature.island.state.IslandUiState
import com.jawzf.pixeltoolkit.feature.island.ui.IslandActions
import com.jawzf.pixeltoolkit.feature.island.ui.IslandFontFamily
import com.jawzf.pixeltoolkit.feature.island.ui.IslandLayoutSpec
import com.jawzf.pixeltoolkit.feature.island.ui.IslandMotion
import com.jawzf.pixeltoolkit.feature.island.ui.IslandRoot
import com.jawzf.pixeltoolkit.feature.island.ui.IslandStackBubble
import com.jawzf.pixeltoolkit.feature.island.ui.IslandStackOverlap
import com.jawzf.pixeltoolkit.feature.island.ui.QueueStyle
import com.jawzf.pixeltoolkit.feature.island.ui.components.IslandIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

class IslandCoordinator(
    private val service: AccessibilityService,
) : SharedPreferences.OnSharedPreferenceChangeListener {
    private val settings by lazy { IslandSettings(service) }
    private val statusBarPrefs by lazy { StatusBarPrefs(service) }
    private val mainHandler = Handler(Looper.getMainLooper())
    private val wm by lazy { service.getSystemService(Context.WINDOW_SERVICE) as WindowManager }
    private val keyguardManager by lazy { service.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager }
    private val windowHost by lazy { IslandWindowHost(service, wm) }

    private val controller = IslandController(
        scheduler = DelayScheduler { delayMs, action ->
            val runnable = Runnable(action)
            mainHandler.postDelayed(runnable, delayMs)
            Cancellable { mainHandler.removeCallbacks(runnable) }
        },
        anchorProvider = { geometry?.anchor ?: CameraAnchor.Center },
    )

    private val latestItems = HashMap<String, List<IslandItem>>()
    private var userPriorities: Map<String, Int> = emptyMap()

    private fun publishItems(pluginId: String, items: List<IslandItem>) {
        latestItems[pluginId] = items
        items.forEach { it.userPriority = userPriorities[it.priorityGroup ?: pluginId] }
        controller.setItems(pluginId, items)
    }

    private fun resolvePriorities(): Map<String, Int> =
        IslandPriorityEntries.resolve(settings.getIslandPriorityOrder(), ProviderPriorities.entries(service, settings))

    private fun reloadPriorities() {
        userPriorities = resolvePriorities()
        latestItems.toMap().forEach { (id, items) -> publishItems(id, items) }
    }

    private val plugins: List<IslandPlugin> = listOf(
        CallPlugin(),
        TimeBatteryPlugin(),
        NotificationsPlugin(),
        ProgressPlugin(),
        MediaPlugin(),
        CalendarPlugin(),
        ConsciousGatePlugin(),
        FlashlightPlugin(),
        TimerPlugin(),
        SoundModePlugin(),
        AlarmPlugin(),
        RemoteProvidersPlugin(),
        CaffeinatePlugin(),
        NavigationPlugin(),
        NetworkPlugin(),
        SignalPlugin(),
        HotspotPlugin(),
        DevicesPlugin(),
        BriefPlugin(),
    )

    private var scope: CoroutineScope? = null
    private var geometry: CameraGeometry? = null
    private val spec = MutableStateFlow(IslandLayoutSpec())
    private val showRing = MutableStateFlow(false)

    private var isScreenOff = false
    private var isLandscape = false
    private var colorRevision by mutableIntStateOf(0)
    private var isFullscreenApp = false
    private var isShadeExpanded = false
    private var running = false
    private var foregroundPackage: String? = null
    private var textInputActive = false

    private val isWindowSuppressed get() = (isFullscreenApp && !settings.isIslandKeepInFullscreenEnabled()) ||
        if (DuoSettings(service).isFoldableDevice()) {
            settings.isIslandHiddenInCurrentOrientation()
        } else {
            isLandscape && !settings.isIslandKeepOnLandscapeEnabled()
        }
    private val isContentSuppressed: Boolean
        get() = isWindowSuppressed ||
            when (settings.getIslandShowWhen()) {
                IslandSettings.ISLAND_SHOW_WHEN_ALWAYS -> false
                IslandSettings.ISLAND_SHOW_WHEN_SCREEN_ON -> isScreenOff
                else -> isScreenOff || keyguardManager?.isKeyguardLocked == true
            } ||
            (settings.isIslandHideOnShadeEnabled() && isShadeExpanded)

    private val compactGestures = CompactGestureController(
        context = service,
        settings = settings,
        scope = { scope },
        openBrief = { openBrief() },
    )

    private val hiddenGestures = HiddenGestureDetector(
        context = service,
        gestures = { compactGestures },
        onTap = { if (settings.isIslandBriefEnabled()) openBrief() },
        onFeedback = { controller.setFeedbackActive(it) },
    )

    private fun syncGestureHitbox() {
        windowHost.setAlwaysGestures(running && settings.isIslandAlwaysGesturesEnabled() && !isContentSuppressed)
    }

    private val actions = object : IslandActions {
        override val compactGestures: CompactGestures get() = this@IslandCoordinator.compactGestures
        override fun onTap(itemKey: String?) = controller.onTap(itemKey)
        override fun onLongPress(itemKey: String?) = controller.onLongPress(itemKey)
        override fun onCollapse() = controller.collapse()
        override fun onDismiss(): Boolean = controller.dismissFocused()
        override fun onInteraction() {
            controller.onUserInteraction()
            val focused = controller.state.value.focusedKey
            plugins.forEach { it.onUserInteraction(focused) }
        }

        override fun onTextInputChanged(active: Boolean) {
            textInputActive = active
            windowHost.setTextInput(active)
        }

        override fun onAdvance(): Boolean = controller.advanceFocused()

        override fun onOpenFocused() {
            controller.state.value.focused?.onOpen?.invoke()
            controller.collapse()
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> isScreenOff = true
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> isScreenOff = false
                else -> return
            }
            if (!running) return
            applySuppression()
            plugins.forEach { it.onScreenStateChanged() }
        }
    }

    init {
        settings.registerOnSharedPreferenceChangeListener(this)
        isLandscape = service.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        service.registerReceiver(
            screenReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            },
        )
        controller.fallbackTapKey = { BriefPlugin.ITEM_KEY.takeIf { settings.isIslandBriefEnabled() } }
        controller.onStageChanged = { stage ->
            if (stage != IslandStage.Expanded) windowHost.setTextInput(false)
            windowHost.onStageChanged(stage)
            syncStatusBar()
            reportVisibility(stage != IslandStage.Hidden)
        }
        settings.setIslandPreviewRingEnabled(false)
        settings.setIslandPreviewStage(IslandSettings.ISLAND_PREVIEW_STAGE_AUTO)
        updateState()
    }

    // Fires with true while the island has anything on screen
    var onVisibilityChanged: ((Boolean) -> Unit)? = null
    private var lastVisible = false

    private fun reportVisibility(visible: Boolean) {
        if (visible == lastVisible) return
        lastVisible = visible
        onVisibilityChanged?.invoke(visible)
    }

    fun openBrief() {
        mainHandler.post { controller.expand(BriefPlugin.ITEM_KEY) }
    }

    fun updateState() {
        mainHandler.post {
            val shouldRun = settings.isIslandEnabled() && !isWindowSuppressed
            if (shouldRun) start() else stop(animate = true)
            if (running) {
                applyConfig()
                applySuppression()
                plugins.forEach { it.refresh() }
            }
        }
    }

    // Anything the user touches outside the island collapses the expanded card, when enabled.
    private fun onOutsideTouch() {
        if (!running || !settings.isIslandDismissOnOutsideEnabled()) return
        if (textInputActive) return
        if (controller.state.value.stage != IslandStage.Expanded) return
        mainHandler.post { controller.collapse() }
    }

    private var pendingPackage: String? = null
    private val applyForegroundPackage = Runnable {
        val packageName = pendingPackage ?: return@Runnable
        if (isTransientPackage(packageName) || foregroundPackage == packageName) return@Runnable
        foregroundPackage = packageName
        applyOwnerAppHiding()
        onLauncher = packageName in launcherPackages
        applyLauncherOnly()
    }

    fun onForegroundPackage(packageName: String) {
        if (pendingPackage == packageName) return
        pendingPackage = packageName
        mainHandler.removeCallbacks(applyForegroundPackage)
        mainHandler.postDelayed(applyForegroundPackage, SETTLE_MS)
    }

    private var onLauncher = true

    private val launcherPackages: Set<String> by lazy {
        try {
            service.packageManager
                .queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0)
                .map { it.activityInfo.packageName }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }

    private fun isTransientPackage(packageName: String): Boolean =
        packageName == "android" ||
            packageName == "com.android.systemui" ||
            packageName == service.packageName ||
            packageName == Settings.Secure.getString(service.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)?.substringBefore('/')

    private fun applyLauncherOnly() {
        val sources = LAUNCHER_ONLY_KEYS.filterValues { settings.getBoolean(it, false) }.keys
        controller.setLauncherState(onLauncher, sources, ProviderTrustStore(settings).launcherOnly())
    }

    private fun applyOwnerAppHiding() {
        controller.setHiddenPackage(foregroundPackage.takeIf { settings.isIslandHideInOwnerAppEnabled() })
    }

    fun updateConsciousGateState() {
        mainHandler.post { plugins.filterIsInstance<ConsciousGatePlugin>().forEach { it.refresh() } }
    }

    private var pendingFullscreen = false
    private val applyFullscreen = Runnable {
        if (isFullscreenApp == pendingFullscreen) return@Runnable
        isFullscreenApp = pendingFullscreen
        updateState()
    }

    fun setFullscreen(fullscreen: Boolean) {
        mainHandler.post {
            if (pendingFullscreen == fullscreen) return@post
            pendingFullscreen = fullscreen
            mainHandler.removeCallbacks(applyFullscreen)
            if (isFullscreenApp != fullscreen) mainHandler.postDelayed(applyFullscreen, SETTLE_MS)
        }
    }

    private var pendingShadeExpanded = false
    private val applyShadeExpanded = Runnable {
        if (isShadeExpanded == pendingShadeExpanded) return@Runnable
        isShadeExpanded = pendingShadeExpanded
        if (running) applySuppression()
    }

    fun setShadeExpanded(expanded: Boolean) {
        mainHandler.post {
            if (pendingShadeExpanded == expanded) return@post
            pendingShadeExpanded = expanded
            mainHandler.removeCallbacks(applyShadeExpanded)
            if (isShadeExpanded != expanded) mainHandler.postDelayed(applyShadeExpanded, SETTLE_MS)
        }
    }

    fun onConfigurationChanged() {
        colorRevision++
        val landscape = service.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (landscape != isLandscape) {
            isLandscape = landscape
            updateState()
        } else if (running) {
            applyConfig()
        }
    }

    fun restart() {
        mainHandler.post {
            stop()
            updateState()
        }
    }

    fun onDestroy() {
        mainHandler.removeCallbacks(applyForegroundPackage)
        mainHandler.removeCallbacks(applyFullscreen)
        mainHandler.removeCallbacks(applyShadeExpanded)
        settings.unregisterOnSharedPreferenceChangeListener(this)
        try {
            service.unregisterReceiver(screenReceiver)
        } catch (_: Exception) {
        }
        stop()
    }

    private fun start() {
        pendingStop?.let {
            mainHandler.removeCallbacks(it)
            pendingStop = null
            hiding = false
            applySuppression()
        }
        if (running) return
        running = true
        revealing = true
        controller.setSuppressed(true)
        applyLauncherOnly()
        val newScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        scope = newScope
        applyConfig()
        val geo = geometry ?: return
        windowHost.onOutsideTouch = ::onOutsideTouch
        windowHost.hiddenTouch = hiddenGestures::onTouch
        val attached = windowHost.attach(geo) {
            val state by controller.state.collectAsState()
            val layoutSpec by spec.collectAsState()
            val revision = colorRevision
            val colors =
                remember(revision) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) dynamicDarkColorScheme(service) else darkColorScheme()
                }
            MaterialTheme(colorScheme = colors, typography = IslandTypography) {
                val base = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(base.density, base.fontScale * layoutSpec.fontScale),
                ) {
                    val ring by showRing.collectAsState()
                    IslandRoot(
                        state, layoutSpec, actions, windowHost::onTargetBoundsChanged,
                        registerCollapseAnimator = { controller.collapseAnimator = it },
                        showCameraRing = ring,
                    )
                }
            }
        }
        // Fails until the accessibility service is connected; onServiceConnected calls updateState() again.
        if (!attached) {
            running = false
            revealing = false
            newScope.cancel()
            scope = null
            return
        }
        mainHandler.postDelayed(endReveal, REVEAL_DELAY_MS)
        val context = IslandPluginContext(
            service = service,
            settings = settings,
            scope = newScope,
            mainHandler = mainHandler,
            request = { request -> mainHandler.post { controller.handle(request) } },
            isContentSuppressed = { isContentSuppressed },
            currentStage = { controller.state.value.stage },
            focusedKey = { controller.state.value.focusedKey },
        )
        latestItems.clear()
        userPriorities = resolvePriorities()
        plugins.forEach { plugin ->
            plugin.start(context)
            newScope.launch { plugin.items.collect { publishItems(plugin.id, it) } }
        }
        newScope.launch {
            controller.state
                .map { it.stage to complicationCount(it) }
                .distinctUntilChanged()
                .collect { syncStatusBar() }
        }
        newScope.launch {
            controller.state
                .map { it.stage to it.focusedKey }
                .distinctUntilChanged()
                .collect { (stage, key) -> plugins.forEach { it.onFocusChanged(stage, key) } }
        }
        newScope.launch { IslandPreviewRequests.queue.collect { showQueuePreview() } }
        if (settings.isIslandSuppressSystemHeadsUpEnabled()) settings.applyHeadsUpSuppression(true)
        applyPreviewStage()
    }

    private val endQueuePreview = Runnable {
        controller.holdFocus = settings.getIslandPreviewStage() != IslandSettings.ISLAND_PREVIEW_STAGE_AUTO
        controller.setItems(QUEUE_PREVIEW_SOURCE, emptyList())
    }

    private fun showQueuePreview() {
        mainHandler.post {
            if (!running) return@post
            mainHandler.removeCallbacks(endQueuePreview)
            val minimal = settings.isIslandLineStageEnabled() &&
                settings.getIslandNotifPeekStyle() == IslandSettings.ISLAND_NOTIF_PEEK_STYLE_MINIMAL
            val layout = when {
                !minimal -> QueuePreviewLayout.Peek
                settings.isIslandNotifQueueInlineEnabled() -> QueuePreviewLayout.MinimalInline
                else -> QueuePreviewLayout.MinimalStack
            }
            controller.holdFocus = layout == QueuePreviewLayout.Peek
            controller.setItems(
                QUEUE_PREVIEW_SOURCE,
                listOf(
                    queuePreviewItem(
                        service.getString(R.string.island_preview_sample_title),
                        service.getString(R.string.island_preview_sample_text),
                        layout,
                        overlap = settings.getIslandNotifQueueStyle() == IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_OVERLAP,
                    ),
                ),
            )
            val state = controller.state.value
            if (layout == QueuePreviewLayout.Peek) {
                if (state.focusedKey != QUEUE_PREVIEW_KEY || state.stage != IslandStage.Line) {
                    controller.collapseImmediately()
                    controller.peek(QUEUE_PREVIEW_KEY, 0L, force = true)
                }
            } else if (state.focusedKey == QUEUE_PREVIEW_KEY) {
                controller.collapseImmediately()
            }
            mainHandler.postDelayed(endQueuePreview, QUEUE_PREVIEW_HOLD_MS)
        }
    }

    private fun applyPreviewRing() {
        showRing.value = settings.isIslandPreviewRingEnabled()
    }

    private fun applyPreviewStage() {
        mainHandler.post {
            if (!running) return@post
            val stage = settings.getIslandPreviewStage()
            val locked = stage != IslandSettings.ISLAND_PREVIEW_STAGE_AUTO
            controller.holdFocus = locked
            controller.setItems(PREVIEW_SOURCE, if (locked) listOf(previewItem()) else emptyList())
            controller.collapseImmediately()
            when (stage) {
                IslandSettings.ISLAND_PREVIEW_STAGE_PEEK -> controller.peek(PREVIEW_KEY, 0L, force = true)
                IslandSettings.ISLAND_PREVIEW_STAGE_EXPANDED -> {
                    val hasBrief = controller.state.value.items[BriefPlugin.ITEM_KEY]?.expanded != null
                    controller.expand(if (hasBrief) BriefPlugin.ITEM_KEY else PREVIEW_KEY)
                }
            }
        }
    }

    private fun previewItem() = IslandItem(
        key = PREVIEW_KEY,
        priority = IslandPriority.NOTIFICATION,
        placement = CompactPlacement.Dynamic,
        compact = listOf(CompactCell("preview.icon") { IslandIcon(R.drawable.rounded_notifications_unread_24, size = 18.dp) }),
        line = LineContent(
            icon = { IslandIcon(R.drawable.rounded_notifications_unread_24, size = 24.dp) },
            start = service.getString(R.string.island_preview_sample_title),
            end = service.getString(R.string.island_preview_sample_text),
        ),
        expanded = ExpandedContent { scope ->
            Column(
                Modifier
                    .padding(scope.spec.expandedOutset)
                    .padding(horizontal = 20.dp)
                    .padding(top = scope.spec.expandedTopPadding + scope.spec.compactHeight, bottom = scope.spec.expandedBottomPadding),
            ) {
                Text(stringResource(R.string.island_preview_sample_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.island_preview_sample_text), style = MaterialTheme.typography.bodyMedium)
            }
        },
        compactVisible = false,
    )

    private enum class QueuePreviewLayout { Peek, MinimalInline, MinimalStack }

    private fun queuePreviewItem(title: String, text: String, layout: QueuePreviewLayout, overlap: Boolean): IslandItem {
        fun icon(key: String, current: Boolean = false) = StackIcon(key, current = current) { size ->
            IslandIcon(R.drawable.rounded_notifications_unread_24, size = size * 0.7f)
        }
        val waiting = listOf(icon("$QUEUE_PREVIEW_KEY.1"), icon("$QUEUE_PREVIEW_KEY.2"))
        val iconCell = CompactCell("preview.queue.icon") { IslandIcon(R.drawable.rounded_notifications_unread_24, size = 18.dp) }
        val queueCell = CompactCell("preview.queue.stack") {
            if (overlap) IslandStackOverlap(waiting, 22.dp) else IslandStackBubble(waiting, 26.dp)
        }
        return IslandItem(
            key = QUEUE_PREVIEW_KEY,
            priority = IslandPriority.NOTIFICATION,
            placement = CompactPlacement.Dynamic,
            compact = if (layout == QueuePreviewLayout.MinimalInline) listOf(queueCell, iconCell) else listOf(iconCell),
            line = LineContent(
                icon = { IslandIcon(R.drawable.rounded_notifications_unread_24, size = 24.dp) },
                start = title,
                end = text,
            ),
            compactVisible = layout != QueuePreviewLayout.Peek,
            sideBubble = if (layout == QueuePreviewLayout.MinimalStack) {
                SideBubble(priority = SideBubblePriority.NOTIFICATION, onOpen = {}, icons = waiting)
            } else {
                null
            },
            stack = listOf(icon(QUEUE_PREVIEW_KEY, current = true)) + waiting,
            queued = waiting,
        )
    }

    private var revealing = false
    private var hiding = false
    private var pendingStop: Runnable? = null
    private val endReveal = Runnable {
        revealing = false
        applySuppression()
    }

    private fun stop(animate: Boolean = false) {
        if (!running) return
        if (animate) {
            if (pendingStop != null) return
            hiding = true
            applySuppression()
            val task = Runnable {
                pendingStop = null
                hiding = false
                stop()
            }
            pendingStop = task
            mainHandler.postDelayed(task, IslandMotion.HIDE_BUBBLE_MS + IslandMotion.SQUASH_MS + HIDE_SETTLE_MS)
            return
        }
        mainHandler.removeCallbacks(endReveal)
        revealing = false
        running = false
        controller.holdFocus = false
        controller.setItems(PREVIEW_SOURCE, emptyList())
        mainHandler.removeCallbacks(endQueuePreview)
        controller.setItems(QUEUE_PREVIEW_SOURCE, emptyList())
        plugins.forEach {
            it.stop()
            controller.setItems(it.id, emptyList())
        }
        scope?.cancel()
        scope = null
        windowHost.detach()
        reportVisibility(false)
        IslandStatusBarHider.restore(service)
        if (settings.isIslandSuppressSystemHeadsUpEnabled()) settings.applyHeadsUpSuppression(false)
    }

    private fun applyConfig() {
        val geo = CameraGeometryResolver.resolve(service, wm, settings)
        geometry = geo
        val density = service.resources.displayMetrics.density
        val screenWidthDp = geo.screenWidth / density
        val slotDp = geo.cameraSlotWidth / density
        val available = when (geo.anchor) {
            CameraAnchor.Center -> screenWidthDp - 16f
            CameraAnchor.Start -> screenWidthDp - (geo.centerX / density - slotDp / 2f) - 8f
            CameraAnchor.End -> geo.centerX / density + slotDp / 2f - 8f
        }.coerceAtLeast(slotDp * 3f)
        val flatExpanded = !geo.hasCamera && settings.isIslandBondEdgeEnabled()
        val scale = if (flatExpanded) 1f else settings.getIslandExpandedScale().coerceIn(1f, 1.3f)
        val lineWidth = minOf(settings.getIslandMaxWidth(), available)
        val expandedWidth = minOf(settings.getIslandExpandedWidth(), available / scale)
        val iconCustomMode = settings.getString(IslandSettings.KEY_ISLAND_ICON_COLOR_MODE, null) == IslandSettings.ISLAND_ICON_COLOR_CUSTOM
        val iconMaterialYou = settings.getBoolean(IslandSettings.KEY_ISLAND_ICON_MATERIAL_YOU, true)
        spec.value = IslandLayoutSpec(
            cameraDiameter = (geo.diameter / density).dp,
            cameraGap = (geo.gap / density).dp,
            verticalGap = (geo.verticalGap / density).dp,
            surfaceTop = (geo.surfaceTop / density).dp,
            lineWidth = lineWidth.dp,
            expandedWidth = expandedWidth.dp,
            expandedCorner = settings.getIslandExpandedRoundness().dp,
            expandedPadding = settings.getIslandExpandedPadding().dp,
            expandedTopPadding = settings.getIslandExpandedTopPadding().dp,
            expandedBottomPadding = settings.getIslandExpandedBottomPadding().dp,
            expandedScale = scale,
            fontScale = settings.getIslandFontScale().coerceIn(0.8f, 1.3f),
            expandedOutset = (expandedWidth * (scale - 1f) / 2f).dp,
            cameraAnchor = geo.anchor,
            cameraPresence = if (geo.hasCamera) 1f else 0f,
            landscape = isLandscape,
            bondEdge = !geo.hasCamera && settings.isIslandBondEdgeEnabled(),
            balanceCompact = settings.isIslandBalanceCompactEnabled(),
            maxExpandedHeight = if (isLandscape) ((geo.screenHeight - geo.surfaceTop) / density - 12f).dp else Dp.Unspecified,
            outlineColor = if (settings.isIslandBorderOutlineEnabled()) {
                runCatching { Color(AndroidColor.parseColor(settings.getIslandBorderOutlineColor())) }
                    .getOrElse { Color(AndroidColor.parseColor(IslandSettings.ISLAND_BORDER_OUTLINE_DEFAULT_COLOR)) }
            } else {
                null
            },
            outlineDynamic = settings.isIslandBorderOutlineDynamicEnabled(),
            outlineThickness = settings.getIslandBorderOutlineThickness().coerceIn(1f, 5f).dp,
            outlineHiddenWhenExpanded = settings.isIslandBorderOutlineHiddenWhenExpanded(),
            pulseShadow = settings.isIslandPulseShadowEnabled(),
            pulseSize = settings.getIslandPulseShadowSize().coerceIn(0.2f, 1f),
            pulseYShift = settings.getIslandPulseShadowYShift().coerceIn(0f, 1f),
            pulseSpread = settings.getIslandPulseShadowSpread().coerceIn(1f, 4f),
            pulseDurationMs = settings.getIslandPulseShadowDurationMs().coerceIn(300f, 4000f).toInt(),
            queueStyle = when (settings.getIslandNotifQueueStyle()) {
                IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_OVERLAP -> QueueStyle.Overlap
                else -> QueueStyle.Stack
            },
            queueInline = settings.isIslandNotifQueueInlineEnabled(),
            iconColorCustom = if (iconCustomMode && !iconMaterialYou) {
                runCatching {
                    Color(AndroidColor.parseColor(settings.getString(IslandSettings.KEY_ISLAND_ICON_COLOR_CUSTOM, null) ?: "#FFFFFF"))
                }.getOrDefault(Color.White)
            } else {
                null
            },
            iconDynamic = !iconCustomMode,
            themedText = settings.getBoolean(IslandSettings.KEY_ISLAND_THEMED_TEXT, false),
            iconMaterialYou = iconMaterialYou,
        )
        windowHost.maxWidthPx = (maxOf(lineWidth, expandedWidth * scale) * density).toInt()
        windowHost.updateGeometry(geo)
        controller.lineStageEnabled = settings.isIslandLineStageEnabled()
        controller.maxCells = settings.getIslandMaxItems() * 2
        controller.balanceCompact = settings.isIslandBalanceCompactEnabled()
        controller.secondaryBubbleEnabled = settings.isIslandSecondaryBubbleEnabled()
        controller.secondaryBubbleOverlap = settings.getIslandNotifQueueStyle() == IslandSettings.ISLAND_NOTIF_QUEUE_STYLE_OVERLAP
        controller.relayout()
        controller.expandedTimeoutMs = settings.getIslandExpandedTimeoutMs()
        applyPreviewRing()
    }

    private var lastContentSuppressed = false

    private fun applySuppression() {
        val contentSuppressed = isContentSuppressed
        if (contentSuppressed != lastContentSuppressed) {
            lastContentSuppressed = contentSuppressed
            plugins.forEach { it.onSuppressionChanged(contentSuppressed) }
        }
        controller.setSuppressed(contentSuppressed || revealing || hiding)
        windowHost.setInteractionsDisabled(
            isFullscreenApp && settings.isIslandKeepInFullscreenEnabled() && settings.isIslandDisableInteractionsInFullscreenEnabled(),
        )
        syncGestureHitbox()
    }

    private fun complicationCount(state: IslandUiState): Int =
        state.arrangement.visibleItems.count { state.items[it]?.placement == CompactPlacement.Dynamic } +
            if (state.sideBubble != null) 1 else 0

    private fun syncStatusBar() {
        val dynamic = settings.getBoolean(IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR, false)
        val compact = settings.getBoolean(IslandSettings.KEY_ISLAND_COMPACT_HIDE_STATUS_BAR, false)
        val selective = settings.getBoolean(IslandSettings.KEY_ISLAND_HIDE_ICONS_ENABLED, false)
        if ((!dynamic && !compact && !selective) || !ShellUtils.hasPermission(service)) {
            IslandStatusBarHider.restore(service)
            return
        }
        val state = controller.state.value
        val peekOrExpanded = dynamic && (state.stage == IslandStage.Line || state.stage == IslandStage.Expanded)
        val crowdedCompact = compact && state.stage == IslandStage.Compact && complicationCount(state) >= 2
        val flags = mutableSetOf<String>()
        if (peekOrExpanded || crowdedCompact) {
            flags += StatusBarManager.FLAG_SYSTEM_ICONS
            flags += StatusBarManager.FLAG_NOTIFICATION_ICONS
            flags += StatusBarManager.FLAG_CLOCK
        }
        if (selective && state.stage != IslandStage.Hidden) {
            statusBarPrefs.getIslandHideIcons().forEach {
                when (it) {
                    StatusBarPrefs.ISLAND_HIDE_ICON_SYSTEM -> flags += StatusBarManager.FLAG_SYSTEM_ICONS
                    StatusBarPrefs.ISLAND_HIDE_ICON_NOTIFICATIONS -> flags += StatusBarManager.FLAG_NOTIFICATION_ICONS
                    StatusBarPrefs.ISLAND_HIDE_ICON_CLOCK -> flags += StatusBarManager.FLAG_CLOCK
                }
            }
        }
        IslandStatusBarHider.applyFlags(service, flags)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key ?: return
        val baseKey = key.substringBefore('@')
        when (baseKey) {
            IslandSettings.KEY_ISLAND_ENABLED, IslandSettings.KEY_ISLAND_KEEP_ON_LANDSCAPE,
            IslandSettings.KEY_ISLAND_KEEP_IN_FULLSCREEN, IslandSettings.KEY_ISLAND_DISABLE_INTERACTIONS_IN_FULLSCREEN,
            -> updateState()
            IslandSettings.KEY_ISLAND_HIDE_PORTRAIT,
            IslandSettings.KEY_ISLAND_HIDE_LANDSCAPE,
            -> updateState()
            IslandSettings.KEY_ISLAND_ALWAYS_GESTURES -> syncGestureHitbox()
            IslandSettings.KEY_ISLAND_PRIORITY_ORDER, IslandSettings.KEY_ISLAND_PROVIDERS -> if (running) reloadPriorities()
            IslandSettings.KEY_ISLAND_DYNAMIC_HIDE_STATUS_BAR, IslandSettings.KEY_ISLAND_COMPACT_HIDE_STATUS_BAR,
            IslandSettings.KEY_ISLAND_HIDE_ICONS_ENABLED, StatusBarPrefs.KEY_ISLAND_HIDE_ICONS,
            -> syncStatusBar()
            IslandSettings.KEY_ISLAND_HIDE_WHEN_SCREEN_OFF, IslandSettings.KEY_ISLAND_SHOW_WHEN -> applySuppression()
            IslandSettings.KEY_ISLAND_HIDE_IN_OWNER_APP -> applyOwnerAppHiding()
            in LAUNCHER_ONLY_KEYS.values, IslandSettings.KEY_ISLAND_PROVIDERS_LAUNCHER_ONLY -> applyLauncherOnly()
            IslandSettings.KEY_ISLAND_HIDE_ON_SHADE -> applySuppression()
            IslandSettings.KEY_ISLAND_PREVIEW_RING -> if (running) applyPreviewRing()
            IslandSettings.KEY_ISLAND_PREVIEW_STAGE -> applyPreviewStage()
            IslandSettings.KEY_ISLAND_SUPPRESS_SYSTEM_HEADS_UP ->
                if (running) settings.applyHeadsUpSuppression(settings.isIslandSuppressSystemHeadsUpEnabled())
            in CONFIG_KEYS -> if (running) applyConfig()
        }
        if (running) plugins.filter { key in it.settingKeys }.forEach { it.refresh() }
    }

    private companion object {
        const val REVEAL_DELAY_MS = 160L
        const val HIDE_SETTLE_MS = 700L
        const val SETTLE_MS = 300L
        const val PREVIEW_SOURCE = "preview"
        const val PREVIEW_KEY = "preview.sample"
        const val QUEUE_PREVIEW_SOURCE = "preview.queue"
        const val QUEUE_PREVIEW_KEY = "preview.queue.sample"
        const val QUEUE_PREVIEW_HOLD_MS = 5000L
        val LAUNCHER_ONLY_KEYS = mapOf(
            "time_battery" to IslandSettings.KEY_ISLAND_TIME_BATTERY_LAUNCHER_ONLY,
            "calendar" to IslandSettings.KEY_ISLAND_CALENDAR_LAUNCHER_ONLY,
            "alarm" to IslandSettings.KEY_ISLAND_ALARM_LAUNCHER_ONLY,
        )
        val CONFIG_KEYS = setOf(
            IslandSettings.KEY_ISLAND_NOTIF_QUEUE_STYLE,
            IslandSettings.KEY_ISLAND_NOTIF_QUEUE_INLINE,
            IslandSettings.KEY_ISLAND_USE_AUTO_DETECT,
            IslandSettings.KEY_ISLAND_CAMERA_OFFSET_X,
            IslandSettings.KEY_ISLAND_CAMERA_OFFSET_Y,
            IslandSettings.KEY_ISLAND_CAMERA_SIZE,
            IslandSettings.KEY_ISLAND_MAX_WIDTH,
            IslandSettings.KEY_ISLAND_CUTOUT_GAP,
            IslandSettings.KEY_ISLAND_EXPANDED_WIDTH,
            IslandSettings.KEY_ISLAND_ICON_COLOR_MODE,
            IslandSettings.KEY_ISLAND_ICON_COLOR_CUSTOM,
            IslandSettings.KEY_ISLAND_ICON_MATERIAL_YOU,
            IslandSettings.KEY_ISLAND_THEMED_TEXT,
            IslandSettings.KEY_ISLAND_BORDER_OUTLINE_ENABLED,
            IslandSettings.KEY_ISLAND_BORDER_OUTLINE_COLOR,
            IslandSettings.KEY_ISLAND_BORDER_OUTLINE_DYNAMIC,
            IslandSettings.KEY_ISLAND_BORDER_OUTLINE_THICKNESS,
            IslandSettings.KEY_ISLAND_BORDER_OUTLINE_HIDE_EXPANDED,
            IslandSettings.KEY_ISLAND_PULSE_SHADOW_ON_NOTIFICATION,
            IslandSettings.KEY_ISLAND_PULSE_SHADOW_SIZE,
            IslandSettings.KEY_ISLAND_PULSE_SHADOW_Y_SHIFT,
            IslandSettings.KEY_ISLAND_PULSE_SHADOW_SPREAD,
            IslandSettings.KEY_ISLAND_PULSE_SHADOW_DURATION_MS,
            IslandSettings.KEY_ISLAND_EXPANDED_ROUNDNESS,
            IslandSettings.KEY_ISLAND_EXPANDED_PADDING,
            IslandSettings.KEY_ISLAND_EXPANDED_TOP_PADDING,
            IslandSettings.KEY_ISLAND_EXPANDED_BOTTOM_PADDING,
            IslandSettings.KEY_ISLAND_EXPANDED_TIMEOUT_MS,
            IslandSettings.KEY_ISLAND_LINE_STAGE_ENABLED,
            IslandSettings.KEY_ISLAND_EXPANDED_SCALE,
            IslandSettings.KEY_ISLAND_FONT_SCALE,
            IslandSettings.KEY_ISLAND_CAMERA_POSITION,
            IslandSettings.KEY_ISLAND_ORIENTATION_PROFILES,
            IslandSettings.KEY_ISLAND_HIDE_PORTRAIT,
            IslandSettings.KEY_ISLAND_HIDE_LANDSCAPE,
            IslandSettings.KEY_ISLAND_LANDSCAPE_TOP_SPACING,
            IslandSettings.KEY_ISLAND_BOND_EDGE,
            IslandSettings.KEY_ISLAND_BALANCE_COMPACT,
            IslandSettings.KEY_ISLAND_SECONDARY_BUBBLE,
            IslandSettings.KEY_ISLAND_MAX_ITEMS,
        )
    }
}

private val IslandTypography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(fontFamily = IslandFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = IslandFontFamily),
        bodySmall = bodySmall.copy(fontFamily = IslandFontFamily),
        labelLarge = labelLarge.copy(fontFamily = IslandFontFamily),
        labelMedium = labelMedium.copy(fontFamily = IslandFontFamily),
        labelSmall = labelSmall.copy(fontFamily = IslandFontFamily),
        titleMedium = titleMedium.copy(fontFamily = IslandFontFamily),
        titleSmall = titleSmall.copy(fontFamily = IslandFontFamily),
    )
}
