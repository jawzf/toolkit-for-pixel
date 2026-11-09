package com.jawzf.pixeltoolkit.utils

import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.core.data.action.ActionGsonAdapter
import com.jawzf.pixeltoolkit.core.data.action.ActivityIconSource
import com.jawzf.pixeltoolkit.core.data.apps.ActivityLauncherUtil
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityLauncherTest {
    @Test
    fun openActivityRoundTripKeepsTarget() {
        val action =
            Action.OpenActivity(
                packageName = "com.android.settings",
                className = "com.android.settings.Settings\$DevelopmentSettingsActivity",
                label = "Developer options",
                requiresRoot = true,
                iconSource = ActivityIconSource.CUSTOM,
                customIconPath = "/data/icons/dev.png",
            )
        assertEquals(action, ActionGsonAdapter.fromJson(ActionGsonAdapter.toJson(action)))
    }

    @Test
    fun savedActionsWithoutAKnownIconSourceUseTheActivityIcon() {
        val older = ActionGsonAdapter.fromJson("""{"type":"OpenActivity","packageName":"a","className":"a.B"}""") as Action.OpenActivity
        assertEquals(ActivityIconSource.ACTIVITY, ActivityLauncherUtil.iconSourceOf(older))
        val unknown =
            ActionGsonAdapter.fromJson("""{"type":"OpenActivity","packageName":"a","className":"a.B","iconSource":"EMOJI"}""")
                as Action.OpenActivity
        assertEquals(ActivityIconSource.ACTIVITY, ActivityLauncherUtil.iconSourceOf(unknown))
    }

    @Test
    fun onlyRootOnlyActivitiesRequestRoot() {
        assertEquals(listOf("ROOT"), Action.OpenActivity(requiresRoot = true).permissions)
        assertTrue(Action.OpenActivity(requiresRoot = false).permissions.isEmpty())
    }

    @Test
    fun shortClassNameDropsOwnPackagePrefixOnly() {
        assertEquals(".ui.Main", ActivityLauncherUtil.shortClassName("com.app", "com.app.ui.Main"))
        assertEquals("org.other.Main", ActivityLauncherUtil.shortClassName("com.app", "org.other.Main"))
        assertEquals("com.apple.Main", ActivityLauncherUtil.shortClassName("com.app", "com.apple.Main"))
    }
}
