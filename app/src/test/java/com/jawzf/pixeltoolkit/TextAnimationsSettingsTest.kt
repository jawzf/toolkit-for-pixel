package com.jawzf.pixeltoolkit

import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.data.display.ScaleAnimationsProfile
import com.jawzf.pixeltoolkit.core.data.display.TextAnimationsSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class TextAnimationsSettingsTest {
    @Test
    fun keysAreUnchanged() {
        assertEquals("font_scale", TextAnimationsSettings.KEY_FONT_SCALE)
        assertEquals("font_weight", TextAnimationsSettings.KEY_FONT_WEIGHT)
        assertEquals("animator_duration_scale", TextAnimationsSettings.KEY_ANIMATOR_DURATION_SCALE)
        assertEquals("transition_animation_scale", TextAnimationsSettings.KEY_TRANSITION_ANIMATION_SCALE)
        assertEquals("window_animation_scale", TextAnimationsSettings.KEY_WINDOW_ANIMATION_SCALE)
        assertEquals("smallest_width", TextAnimationsSettings.KEY_SMALLEST_WIDTH)
        assertEquals("scale_animations_mode", TextAnimationsSettings.KEY_SCALE_ANIMATIONS_MODE)
        assertEquals("scale_animations_default_profile", TextAnimationsSettings.KEY_SCALE_ANIMATIONS_DEFAULT_PROFILE)
        assertEquals("scale_animations_glove_profile", TextAnimationsSettings.KEY_SCALE_ANIMATIONS_GLOVE_PROFILE)
    }

    @Test
    fun scaleAnimationsProfileSerializationRoundTrip() {
        val gson = Gson()
        val profile =
            ScaleAnimationsProfile(
                fontScale = 1.25f,
                fontWeight = 100,
                animatorDurationScale = 0.5f,
                transitionAnimationScale = 0.5f,
                windowAnimationScale = 0.5f,
                smallestWidth = 411,
                touchSensitivityEnabled = true,
                autoRotateEnabled = true,
                screenTimeout = 60000L,
            )
        val json = gson.toJson(profile)
        val deserialized = gson.fromJson(json, ScaleAnimationsProfile::class.java)
        assertEquals(profile, deserialized)
    }
}
