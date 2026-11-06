package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionGsonAdapter
import com.jawzf.pixeltoolkit.core.data.action.ChargingMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionJsonTest {
    @Test
    fun roundTripNoSettingAction() {
        val action = Action.TurnOnFlashlight
        val json = ActionGsonAdapter.toJson(action)
        assertTrue(json.contains(""""type":"TurnOnFlashlight""""))
        val restored = ActionGsonAdapter.fromJson(json)
        assertEquals(action, restored)
    }

    @Test
    fun listFromJsonDropsUnknownType() {
        val json = """[{"type":"UnknownNonExistentAction"},{"type":"TurnOnFlashlight"}]"""
        val list = ActionGsonAdapter.listFromJson(json)
        assertEquals(listOf(Action.TurnOnFlashlight), list)
    }

    @Test
    fun roundTripSetChargingMode() {
        val action = Action.SetChargingMode(ChargingMode.LIMITED)
        val json = ActionGsonAdapter.toJson(action)
        val restored = ActionGsonAdapter.fromJson(json)
        assertEquals(action, restored)
    }
}
