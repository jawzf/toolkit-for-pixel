package com.jawzf.pixeltoolkit.feature.input.remap

import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore

object RemapSettings {
    const val KEY_BUTTON_REMAP_ENABLED = "button_remap_enabled"
    const val KEY_BUTTON_REMAP_USE_SHIZUKU = "button_remap_use_shizuku"
    const val KEY_BUTTON_REMAP_VOL_UP_ACTION_OFF = "button_remap_vol_up_action_off"
    const val KEY_BUTTON_REMAP_VOL_UP_ACTION = "button_remap_vol_up_action" // Legacy
    const val KEY_BUTTON_REMAP_VOL_DOWN_ACTION_OFF = "button_remap_vol_down_action_off"
    const val KEY_BUTTON_REMAP_VOL_DOWN_ACTION = "button_remap_vol_down_action" // Legacy
    const val KEY_BUTTON_REMAP_VOL_UP_ACTION_ON = "button_remap_vol_up_action_on"
    const val KEY_BUTTON_REMAP_VOL_DOWN_ACTION_ON = "button_remap_vol_down_action_on"
    const val KEY_BUTTON_REMAP_POWER_HOLD_ACTION_OFF = "button_remap_power_hold_action_off"
    const val KEY_BUTTON_REMAP_POWER_HOLD_ACTION_ON = "button_remap_power_hold_action_on"
    const val KEY_BUTTON_REMAP_POWER_HOLD_CHOICE_OFF = "button_remap_power_hold_choice_off"
    const val KEY_BUTTON_REMAP_POWER_HOLD_CHOICE_ON = "button_remap_power_hold_choice_on"
    const val KEY_BUTTON_REMAP_POWER_OVERRIDDEN = "button_remap_power_overridden"
    const val KEY_BUTTON_REMAP_POWER_CAMERA_FLIP = "button_remap_power_camera_flip"
    const val KEY_BUTTON_REMAP_HAPTIC_TYPE = "button_remap_haptic_type"
    const val KEY_BUTTON_REMAP_MIGRATION_DONE = "button_remap_action_migration_done"
    const val KEY_BUTTON_REMAP_PAUSE_ON_VOLUME_DIALOG = "button_remap_pause_on_volume_dialog"
}

fun PrefsStore.getPowerChoice(slot: RemapSlot): PowerChoice {
    val defaultChoice = slot.defaultChoice ?: PowerChoice.ASSISTANT
    val key = slot.choicePrefKey ?: return defaultChoice
    val value = getString(key, null) ?: return defaultChoice
    return try {
        PowerChoice.valueOf(value)
    } catch (_: IllegalArgumentException) {
        defaultChoice
    }
}

fun PrefsStore.setPowerChoice(
    slot: RemapSlot,
    choice: PowerChoice,
) {
    val key = slot.choicePrefKey ?: return
    putString(key, choice.name)
}
