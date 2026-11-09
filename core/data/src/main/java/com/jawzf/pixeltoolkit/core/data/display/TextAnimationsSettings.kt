package com.jawzf.pixeltoolkit.core.data.display

import android.content.Context
import android.provider.Settings
import com.google.gson.Gson
import com.jawzf.pixeltoolkit.core.data.prefs.PrefsStore
import com.jawzf.pixeltoolkit.core.platform.shell.RootUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils

class TextAnimationsSettings(
    context: Context,
) : PrefsStore(context) {
    private val gson = Gson()

    /**
     * Executes the get font scale operation.
     * @return The resulting Float data.
     */
    fun getFontScale(): Float =
        try {
            Settings.System.getFloat(
                context.contentResolver,
                Settings.System.FONT_SCALE,
            )
        } catch (e: Exception) {
            1.0f
        }

    /**
     * Executes the set font scale operation.
     *
     * @param scale [Float] Target scale.
     */
    fun setFontScale(scale: Float) {
        putFloat(KEY_FONT_SCALE, scale)
        try {
            Settings.System.putFloat(
                context.contentResolver,
                Settings.System.FONT_SCALE,
                scale,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get font weight operation.
     * @return The resulting Int data.
     */
    fun getFontWeight(): Int =
        try {
            Settings.Secure.getInt(
                context.contentResolver,
                "font_weight_adjustment",
            )
        } catch (e: Exception) {
            0
        }

    /**
     * Executes the set font weight operation.
     *
     * @param weight [Int] Target weight.
     */
    fun setFontWeight(weight: Int) {
        putInt(KEY_FONT_WEIGHT, weight)
        try {
            Settings.Secure.putInt(
                context.contentResolver,
                "font_weight_adjustment",
                weight,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get smallest width operation.
     * @return The resulting Int data.
     */
    fun getSmallestWidth(): Int {
        val forcedDensity =
            try {
                Settings.Secure.getInt(
                    context.contentResolver,
                    "display_density_forced",
                )
            } catch (e: Exception) {
                0
            }
        if (forcedDensity > 0) {
            val metrics = context.resources.displayMetrics
            val widthPx = Math.min(metrics.widthPixels, metrics.heightPixels)
            return (widthPx * 160) / forcedDensity
        }
        return context.resources.configuration.smallestScreenWidthDp
    }

    /**
     * Executes the set smallest width operation.
     *
     * @param widthDp [Int] Target width dp.
     */
    fun setSmallestWidth(widthDp: Int) {
        putInt(KEY_SMALLEST_WIDTH, widthDp)
        val metrics = context.resources.displayMetrics
        val widthPx = Math.min(metrics.widthPixels, metrics.heightPixels)
        val density = (widthPx * 160) / widthDp
        val command = "wm density $density"
        if (ShizukuUtils.isShizukuAvailable() && ShizukuUtils.hasPermission()) {
            ShizukuUtils.runCommand(command)
        } else if (RootUtils.isRootAvailable()) {
            RootUtils.runCommand(command)
        } else {
            try {
                Settings.Secure.putInt(
                    context.contentResolver,
                    "display_density_forced",
                    density,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Executes the reset smallest width operation.
     */
    fun resetSmallestWidth() {
        val command = "wm density reset"
        if (ShizukuUtils.isShizukuAvailable() && ShizukuUtils.hasPermission()) {
            ShizukuUtils.runCommand(command)
        } else if (RootUtils.isRootAvailable()) {
            RootUtils.runCommand(command)
        } else {
            try {
                Settings.Secure.putString(
                    context.contentResolver,
                    "display_density_forced",
                    null,
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        remove(KEY_SMALLEST_WIDTH)
    }

    /**
     * Executes the get animation scale operation.
     *
     * @param key [String] Target key.
     * @return The resulting Float data.
     */
    fun getAnimationScale(key: String): Float =
        try {
            Settings.Global.getFloat(context.contentResolver, key)
        } catch (e: Exception) {
            1.0f
        }

    /**
     * Executes the set animation scale operation.
     *
     * @param key [String] Target key.
     * @param scale [Float] Target scale.
     */
    fun setAnimationScale(
        key: String,
        scale: Float,
    ) {
        when (key) {
            Settings.Global.ANIMATOR_DURATION_SCALE ->
                putFloat(
                    KEY_ANIMATOR_DURATION_SCALE,
                    scale,
                )

            Settings.Global.TRANSITION_ANIMATION_SCALE ->
                putFloat(
                    KEY_TRANSITION_ANIMATION_SCALE,
                    scale,
                )

            Settings.Global.WINDOW_ANIMATION_SCALE ->
                putFloat(
                    KEY_WINDOW_ANIMATION_SCALE,
                    scale,
                )
        }
        try {
            Settings.Global.putFloat(context.contentResolver, key, scale)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getScaleAnimationsMode(): String = getString(KEY_SCALE_ANIMATIONS_MODE, "default") ?: "default"

    /**
     * Executes the set scale animations mode operation.
     *
     * @param mode [String] Target mode.
     */
    fun setScaleAnimationsMode(mode: String) = putString(KEY_SCALE_ANIMATIONS_MODE, mode)

    /**
     * Executes the get scale animations profile operation.
     *
     * @param mode [String] Target mode.
     * @return The resulting ScaleAnimationsProfile data.
     */
    fun getScaleAnimationsProfile(mode: String): ScaleAnimationsProfile {
        val key =
            if (mode == "glove") KEY_SCALE_ANIMATIONS_GLOVE_PROFILE else KEY_SCALE_ANIMATIONS_DEFAULT_PROFILE
        val json = prefs.getString(key, null)
        return if (json != null) {
            try {
                gson.fromJson(json, ScaleAnimationsProfile::class.java)
            } catch (e: Exception) {
                getDefaultScaleAnimationsProfile(mode)
            }
        } else {
            getDefaultScaleAnimationsProfile(mode)
        }
    }

    private fun getDefaultScaleAnimationsProfile(mode: String): ScaleAnimationsProfile =
        if (mode == "glove") {
            ScaleAnimationsProfile(
                fontScale = 1.25f,
                smallestWidth = 385,
                touchSensitivityEnabled = true,
                autoRotateEnabled = true,
                screenTimeout = 60000L,
            )
        } else {
            ScaleAnimationsProfile()
        }

    /**
     * Executes the save scale animations profile operation.
     *
     * @param mode [String] Target mode.
     * @param profile [ScaleAnimationsProfile] Target profile.
     */
    fun saveScaleAnimationsProfile(
        mode: String,
        profile: ScaleAnimationsProfile,
    ) {
        val key =
            if (mode == "glove") KEY_SCALE_ANIMATIONS_GLOVE_PROFILE else KEY_SCALE_ANIMATIONS_DEFAULT_PROFILE
        val json = gson.toJson(profile)
        putString(key, json)
    }

    /**
     * Executes the get touch sensitivity enabled operation.
     * @return The resulting Boolean data.
     */
    fun getTouchSensitivityEnabled(): Boolean =
        try {
            Settings.Secure.getInt(
                context.contentResolver,
                "touch_sensitivity_enabled",
                0,
            ) == 1
        } catch (e: Exception) {
            false
        }

    /**
     * Executes the set touch sensitivity enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setTouchSensitivityEnabled(enabled: Boolean) {
        try {
            Settings.Secure.putInt(
                context.contentResolver,
                "touch_sensitivity_enabled",
                if (enabled) 1 else 0,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get auto rotate enabled operation.
     * @return The resulting Boolean data.
     */
    fun getAutoRotateEnabled(): Boolean =
        try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                0,
            ) == 1
        } catch (e: Exception) {
            false
        }

    /**
     * Executes the set auto rotate enabled operation.
     *
     * @param enabled [Boolean] Target enabled.
     */
    fun setAutoRotateEnabled(enabled: Boolean) {
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.ACCELEROMETER_ROTATION,
                if (enabled) 1 else 0,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Executes the get screen timeout operation.
     * @return The resulting Long data.
     */
    fun getScreenTimeout(): Long =
        try {
            Settings.System.getLong(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                30000L,
            )
        } catch (e: Exception) {
            30000L
        }

    /**
     * Executes the set screen timeout operation.
     *
     * @param timeoutMs [Long] Target timeout ms.
     */
    fun setScreenTimeout(timeoutMs: Long) {
        try {
            Settings.System.putLong(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                timeoutMs,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun currentProfile(): ScaleAnimationsProfile =
        ScaleAnimationsProfile(
            fontScale = getFontScale(),
            fontWeight = getFontWeight(),
            animatorDurationScale = getAnimationScale(Settings.Global.ANIMATOR_DURATION_SCALE),
            transitionAnimationScale = getAnimationScale(Settings.Global.TRANSITION_ANIMATION_SCALE),
            windowAnimationScale = getAnimationScale(Settings.Global.WINDOW_ANIMATION_SCALE),
            smallestWidth = getSmallestWidth(),
            touchSensitivityEnabled = getTouchSensitivityEnabled(),
            autoRotateEnabled = getAutoRotateEnabled(),
            screenTimeout = getScreenTimeout(),
        )

    fun applyProfile(profile: ScaleAnimationsProfile) {
        setFontScale(profile.fontScale)
        setFontWeight(profile.fontWeight)
        setAnimationScale(
            Settings.Global.ANIMATOR_DURATION_SCALE,
            profile.animatorDurationScale,
        )
        setAnimationScale(
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            profile.transitionAnimationScale,
        )
        setAnimationScale(
            Settings.Global.WINDOW_ANIMATION_SCALE,
            profile.windowAnimationScale,
        )
        setSmallestWidth(profile.smallestWidth)
        setTouchSensitivityEnabled(profile.touchSensitivityEnabled)
        setAutoRotateEnabled(profile.autoRotateEnabled)
        setScreenTimeout(profile.screenTimeout)
    }

    /** Re-applies the saved font and animation values to the system settings. */
    fun applySavedToSystem() {
        if (contains(TextAnimationsSettings.KEY_FONT_SCALE)) {
            setFontScale(getFloat(TextAnimationsSettings.KEY_FONT_SCALE, 1.0f))
        }
        if (contains(TextAnimationsSettings.KEY_FONT_WEIGHT)) {
            setFontWeight(getInt(TextAnimationsSettings.KEY_FONT_WEIGHT, 0))
        }
        if (contains(TextAnimationsSettings.KEY_ANIMATOR_DURATION_SCALE)) {
            setAnimationScale(
                Settings.Global.ANIMATOR_DURATION_SCALE,
                getFloat(TextAnimationsSettings.KEY_ANIMATOR_DURATION_SCALE, 1.0f),
            )
        }
        if (contains(TextAnimationsSettings.KEY_TRANSITION_ANIMATION_SCALE)) {
            setAnimationScale(
                Settings.Global.TRANSITION_ANIMATION_SCALE,
                getFloat(TextAnimationsSettings.KEY_TRANSITION_ANIMATION_SCALE, 1.0f),
            )
        }
        if (contains(TextAnimationsSettings.KEY_WINDOW_ANIMATION_SCALE)) {
            setAnimationScale(
                Settings.Global.WINDOW_ANIMATION_SCALE,
                getFloat(TextAnimationsSettings.KEY_WINDOW_ANIMATION_SCALE, 1.0f),
            )
        }
    }

    companion object {
        const val KEY_FONT_SCALE = "font_scale"
        const val KEY_FONT_WEIGHT = "font_weight"
        const val KEY_ANIMATOR_DURATION_SCALE = "animator_duration_scale"
        const val KEY_TRANSITION_ANIMATION_SCALE = "transition_animation_scale"
        const val KEY_WINDOW_ANIMATION_SCALE = "window_animation_scale"
        const val KEY_SMALLEST_WIDTH = "smallest_width"

        const val KEY_SCALE_ANIMATIONS_MODE = "scale_animations_mode"
        const val KEY_SCALE_ANIMATIONS_DEFAULT_PROFILE = "scale_animations_default_profile"
        const val KEY_SCALE_ANIMATIONS_GLOVE_PROFILE = "scale_animations_glove_profile"
    }
}
