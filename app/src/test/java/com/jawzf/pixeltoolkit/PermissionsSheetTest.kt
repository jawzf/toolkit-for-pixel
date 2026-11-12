package com.jawzf.pixeltoolkit

import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.core.design.sheets.canTurnOn
import com.jawzf.pixeltoolkit.core.design.sheets.stepActionLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionsSheetTest {
    @Test
    fun canTurnOnReturnsExpected() {
        assertTrue(canTurnOn(emptyList()))

        val grantedItem = PermissionItem(iconRes = 0, title = "t", description = "d", isGranted = true)
        val missingItem = PermissionItem(iconRes = 0, title = "t", description = "d", isGranted = false)

        assertTrue(canTurnOn(listOf(grantedItem)))
        assertTrue(canTurnOn(listOf(grantedItem, grantedItem)))
        assertFalse(canTurnOn(listOf(missingItem)))
        assertFalse(canTurnOn(listOf(grantedItem, missingItem)))
        assertFalse(canTurnOn(listOf(missingItem, grantedItem)))
    }

    @Test
    fun stepActionLabelReturnsExpected() {
        assertEquals(R.string.perm_action_grant_short, stepActionLabel(null))
        assertEquals(R.string.perm_action_grant_short, stepActionLabel(R.string.perm_action_grant))
        assertEquals(R.string.perm_action_grant_short, stepActionLabel(R.string.perm_action_enable))
        assertEquals(R.string.perm_action_copy_adb, stepActionLabel(R.string.perm_action_copy_adb))
        assertEquals("Custom", stepActionLabel("Custom"))
    }
}
