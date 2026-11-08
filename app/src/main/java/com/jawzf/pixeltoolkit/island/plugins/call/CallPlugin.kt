package com.jawzf.pixeltoolkit.island.plugins.call

import android.telecom.TelecomManager
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.platform.device.CallControlUtil
import com.jawzf.pixeltoolkit.core.platform.device.CallPhase
import com.jawzf.pixeltoolkit.core.platform.device.CallSnapshot
import com.jawzf.pixeltoolkit.core.platform.device.CallStateRepository
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.island.model.CompactCell
import com.jawzf.pixeltoolkit.island.model.CompactPlacement
import com.jawzf.pixeltoolkit.island.model.ExpandedContent
import com.jawzf.pixeltoolkit.island.model.IslandItem
import com.jawzf.pixeltoolkit.island.model.IslandPriority
import com.jawzf.pixeltoolkit.island.model.LineContent
import com.jawzf.pixeltoolkit.island.model.PluginRequest
import com.jawzf.pixeltoolkit.island.plugins.BaseIslandPlugin
import com.jawzf.pixeltoolkit.island.plugins.sendPendingIntent
import com.jawzf.pixeltoolkit.island.ui.components.IslandBitmap
import com.jawzf.pixeltoolkit.island.ui.components.IslandIcon
import com.jawzf.pixeltoolkit.island.ui.components.RollingText
import com.jawzf.pixeltoolkit.services.LiveUpdateSnoozer
import com.jawzf.pixeltoolkit.services.NotificationListener
import com.jawzf.pixeltoolkit.utils.call.CallActionRole
import com.jawzf.pixeltoolkit.utils.call.CallNotificationAction
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

class CallPlugin : BaseIslandPlugin() {
    override val id = "call"

    override val settingKeys = setOf(SettingsRepository.KEY_ISLAND_SHOW_CALLS)

    private fun popUp() {
        val c = ctx ?: return
        val sticky = settings.getBoolean(SettingsRepository.KEY_ISLAND_CALL_STICKY, false)
        val mode = settings.getString(SettingsRepository.KEY_ISLAND_CALL_SHOW_MODE, SettingsRepository.ISLAND_CALL_SHOW_PEEK)
        when {
            mode == SettingsRepository.ISLAND_CALL_SHOW_COMPACT -> Unit
            mode == SettingsRepository.ISLAND_CALL_SHOW_PEEK && settings.isIslandLineStageEnabled() ->
                c.request(PluginRequest.Peek(ITEM_KEY, settings.getIslandTimeoutMs(), sticky))
            else -> c.request(PluginRequest.Expand(ITEM_KEY, sticky))
        }
    }

    private var call: CallSnapshot? = null
    private var ticker: Job? = null
    private var observer: Job? = null

    override fun onStart() {
        val c = ctx!!
        observer = c.scope.launch {
            CallStateRepository.state.collect { next ->
                val previous = call
                call = next
                val startedRinging = next?.phase == CallPhase.Ringing && previous?.phase != CallPhase.Ringing
                if (next == null) restoreHeadsUpAfterCall()
                restartTicker()
                render()
                if (startedRinging) {
                    if (enabled(next)) popUp() else releaseHeadsUpForCall()
                }
            }
        }
    }

    private var headsUpReleased = false

    private fun releaseHeadsUpForCall() {
        if (!settings.isIslandSuppressSystemHeadsUpEnabled()) return
        try {
            settings.applyHeadsUpSuppression(false)
            headsUpReleased = true
        } catch (_: Exception) {
        }
    }

    private fun restoreHeadsUpAfterCall() {
        if (!headsUpReleased) return
        headsUpReleased = false
        try {
            if (settings.isIslandSuppressSystemHeadsUpEnabled()) settings.applyHeadsUpSuppression(true)
        } catch (_: Exception) {
        }
    }

    override fun onStop() {
        restoreHeadsUpAfterCall()
        observer?.cancel()
        ticker?.cancel()
        call = null
    }

    override fun refresh() = render()

    // App (notification) calls carry their own controls and need no phone permissions; plain telephony calls do.
    private fun enabled(snap: CallSnapshot? = call): Boolean {
        if (!settings.isIslandShowCallsEnabled()) return false
        val fromNotification = snap?.appName != null || snap?.answerIntent != null || snap?.endIntent != null
        return fromNotification || PermissionUtils.hasCallPermissions(context)
    }

    private fun restartTicker() {
        ticker?.cancel()
        if (call == null) return
        ticker = ctx?.scope?.launch {
            while (isActive) {
                delay(1000L - System.currentTimeMillis() % 1000L)
                CallStateRepository.reconcile(context, activeNotificationKeys())
                if (call?.phase == CallPhase.Active) render()
            }
        }
    }

    private fun activeNotificationKeys(): Set<String>? = try {
        val listener = NotificationListener.instance ?: return null
        val keys = listener.activeNotifications.orEmpty().map { it.key }.toMutableSet()
        LiveUpdateSnoozer.snoozedNotifications().forEach { keys += it.key }
        keys
    } catch (_: Exception) {
        null
    }

    private fun render() {
        val snap = call
        if (ctx == null || snap == null || !enabled()) {
            publish(null)
            return
        }
        val ringing = snap.phase == CallPhase.Ringing
        val caller = snap.name ?: snap.number ?: context.getString(R.string.island_call_incoming)
        val incomingLabel = snap.appName?.let { context.getString(R.string.island_call_incoming_app, it) }
            ?: context.getString(R.string.island_call_incoming)
        val status = if (ringing) incomingLabel else elapsed(snap.startedAt)
        val photo = snap.photo
        val buttons = buttonsFor(snap, ringing)
        publish(
            IslandItem(
                key = ITEM_KEY,
                priority = IslandPriority.CALL,
                placement = CompactPlacement.Dynamic,
                compact = listOf(
                    CompactCell("call.avatar") { IslandBitmap(photo, 22.dp, circle = true, fallbackRes = R.drawable.rounded_call_24) },
                    if (ringing) {
                        CompactCell("call.ringing") { RingingIcon() }
                    } else {
                        CompactCell("call.timer") { RollingText(status) }
                    },
                ),
                line = LineContent(
                    icon = { IslandBitmap(photo, 24.dp, circle = true, fallbackRes = R.drawable.rounded_call_24) },
                    start = caller,
                    end = status,
                    endSlot = ringingSlot(ringing),
                ),
                expanded = ExpandedContent { scope ->
                    CallExpanded(
                        caller = caller,
                        number = snap.number?.takeIf { snap.name != null },
                        photo = photo,
                        ringing = ringing,
                        status = status,
                        buttons = buttons,
                        scope = scope,
                    )
                },
                accent = null,
                onOpen = { guarded { if (!sendPendingIntent(context, snap.contentIntent)) CallControlUtil.showInCallScreen(context) } },
                sourcePackage = snap.packageName,
            ),
        )
    }

    private fun buttonsFor(snap: CallSnapshot, ringing: Boolean): List<CallButtonSpec> {
        val buttons = if (usesOwnControls(snap)) notificationButtons(snap, ringing) else telephonyButtons(snap, ringing)
        if (buttons.isNotEmpty()) return buttons
        return listOf(openButton(snap))
    }

    private fun usesOwnControls(snap: CallSnapshot): Boolean {
        val hasOwn = snap.allActions.isNotEmpty() || snap.answerIntent != null || snap.endIntent != null
        if (!hasOwn) return false
        if (!snap.fromTelephony) return true
        val dialer = guarded<String?>(null) { context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage }
        return snap.packageName != null && snap.packageName != dialer
    }

    private fun telephonyButtons(snap: CallSnapshot, ringing: Boolean): List<CallButtonSpec> {
        val end = CallButtonSpec(
            key = "end",
            icon = R.drawable.rounded_call_end_24,
            label = context.getString(if (ringing) R.string.island_call_decline else R.string.island_call_end),
            style = CallButtonStyle.End,
            onClick = {
                guarded { CallControlUtil.endCall(context) }
                null
            },
        )
        if (!ringing) return listOf(openButton(snap), end)
        val answer = CallButtonSpec(
            key = "answer",
            icon = R.drawable.rounded_call_24,
            label = context.getString(R.string.island_call_answer),
            style = CallButtonStyle.Answer,
            onClick = {
                guarded { CallControlUtil.acceptCall(context) }
                null
            },
        )
        return listOf(end, answer)
    }

    private fun notificationButtons(snap: CallSnapshot, ringing: Boolean): List<CallButtonSpec> {
        val actions = snap.allActions.ifEmpty { legacyActions(snap, ringing) }
        return actions.take(MAX_NOTIFICATION_BUTTONS).map { action ->
            val style = when (action.role) {
                CallActionRole.Answer -> CallButtonStyle.Answer
                CallActionRole.Decline, CallActionRole.HangUp -> CallButtonStyle.End
                else -> CallButtonStyle.Neutral
            }
            val icon = when (action.role) {
                CallActionRole.Answer -> R.drawable.rounded_call_24
                CallActionRole.Decline, CallActionRole.HangUp -> R.drawable.rounded_call_end_24
                CallActionRole.Mute -> R.drawable.rounded_mic_24
                CallActionRole.Speaker -> R.drawable.rounded_volume_up_24
                CallActionRole.Hold -> R.drawable.rounded_pause_24
                CallActionRole.Video -> R.drawable.rounded_videocam_24
                CallActionRole.Other -> null
            }
            val on = action.role == CallActionRole.Mute && action.title.lowercase().contains("unmute")
            ownButton(action, icon, on = on, onIcon = if (action.role == CallActionRole.Mute) R.drawable.rounded_mic_off_24 else null, style = style)
        }
    }

    private fun legacyActions(snap: CallSnapshot, ringing: Boolean): List<CallNotificationAction> = buildList {
        val answer = snap.answerIntent
        val end = snap.endIntent
        if (ringing) {
            if (end != null) add(CallNotificationAction(CallActionRole.Decline, context.getString(R.string.island_call_decline), end))
            if (answer != null) add(CallNotificationAction(CallActionRole.Answer, context.getString(R.string.island_call_answer), answer))
        } else if (end != null) {
            add(CallNotificationAction(CallActionRole.HangUp, context.getString(R.string.island_call_end), end))
        }
    }

    private fun openButton(snap: CallSnapshot) = CallButtonSpec(
        key = "open",
        icon = R.drawable.rounded_open_in_new_24,
        label = context.getString(R.string.island_call_open),
        onClick = {
            guarded { if (!sendPendingIntent(context, snap.contentIntent)) CallControlUtil.showInCallScreen(context) }
            null
        },
    )

    private fun ownButton(
        action: CallNotificationAction,
        icon: Int?,
        on: Boolean,
        onIcon: Int? = null,
        style: CallButtonStyle = CallButtonStyle.Neutral,
    ) = CallButtonSpec(
        key = "own:${action.role}:${action.title}",
        icon = icon,
        onIcon = onIcon,
        label = action.title,
        style = style,
        initialOn = on,
        onClick = {
            guarded { sendPendingIntent(context, action.intent) }
            null
        },
    )

    private fun guarded(block: () -> Unit) {
        try {
            block()
        } catch (_: Exception) {
        }
    }

    private fun <T> guarded(fallback: T, block: () -> T): T = try {
        block()
    } catch (_: Exception) {
        fallback
    }

    private fun elapsed(startedAt: Long): String {
        val total = ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s)
        else String.format(Locale.getDefault(), "%02d:%02d", m, s)
    }

    companion object {
        const val ITEM_KEY = "call"
        private const val MAX_NOTIFICATION_BUTTONS = 3
    }
}

private fun ringingSlot(ringing: Boolean): (@Composable () -> Unit)? = if (ringing) ({ RingingIcon() }) else null

@Composable
private fun RingingIcon() {
    IslandIcon(R.drawable.rounded_call_24, tint = MaterialTheme.colorScheme.primary, size = 18.dp)
}
