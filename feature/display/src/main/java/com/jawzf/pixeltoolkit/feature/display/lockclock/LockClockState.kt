package com.jawzf.pixeltoolkit.feature.display.lockclock

import android.content.Context
import android.graphics.Color
import android.provider.Settings
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.jawzf.pixeltoolkit.core.platform.permissions.PermissionUtils
import com.jawzf.pixeltoolkit.core.platform.shell.RootUtils
import com.jawzf.pixeltoolkit.core.platform.shizuku.ShizukuUtils
import com.jawzf.pixeltoolkit.feature.display.DisplaySettings

class LockClockState {
    private lateinit var settings: DisplaySettings

    val lockScreenClockId = mutableStateOf<String?>(null)
    val lockScreenClockWeight = mutableIntStateOf(300)
    val lockScreenClockWidth = mutableIntStateOf(116)
    val lockScreenClockGrade = mutableIntStateOf(0)
    val lockScreenClockRoundness = mutableIntStateOf(100)
    val lockScreenClockColorTone = mutableIntStateOf(75)
    val lockScreenClockSelectedColorId = mutableStateOf("DEFAULT")
    val lockScreenClockSeedColor = mutableIntStateOf(0)

    fun load(context: Context) {
        settings = DisplaySettings(context)
        lockScreenClockId.value = readCurrentLockScreenClockId(context)
        lockScreenClockWeight.intValue = settings.getLockScreenClockWeight()
        lockScreenClockWidth.intValue = settings.getLockScreenClockWidth()
        lockScreenClockGrade.intValue = settings.getLockScreenClockGrade()
        lockScreenClockRoundness.intValue = settings.getLockScreenClockRoundness()
        lockScreenClockColorTone.intValue = settings.getLockScreenClockColorTone()
        lockScreenClockSelectedColorId.value =
            settings.getLockScreenClockSelectedColorId()
        lockScreenClockSeedColor.intValue = settings.getLockScreenClockSeedColor()
    }

    fun setLockScreenClockId(
        clockId: String,
        context: Context,
    ) {
        val timestamp = System.currentTimeMillis()
        val json =
            if (lockScreenClockSelectedColorId.value == "DEFAULT") {
                "{\"clockId\":\"$clockId\"," +
                    "\"metadata\":{\"metadataSelectedColorId\":\"DEFAULT\"," +
                    "\"metadataColorToneProgress\":${lockScreenClockColorTone.intValue}," +
                    "\"appliedTimestamp\":$timestamp}," +
                    "\"axes\":[{\"key\":\"wght\",\"value\":${lockScreenClockWeight.intValue}}," +
                    "{\"key\":\"wdth\",\"value\":${lockScreenClockWidth.intValue}}," +
                    "{\"key\":\"ROND\",\"value\":${lockScreenClockRoundness.intValue}}]}"
            } else {
                "{\"clockId\":\"$clockId\"," +
                    "\"seedColor\":${lockScreenClockSeedColor.intValue}," +
                    "\"metadata\":{\"metadataSelectedColorId\":\"${lockScreenClockSelectedColorId.value}\"," +
                    "\"metadataColorToneProgress\":${lockScreenClockColorTone.intValue}," +
                    "\"appliedTimestamp\":$timestamp}," +
                    "\"axes\":[{\"key\":\"wght\",\"value\":${lockScreenClockWeight.intValue}}," +
                    "{\"key\":\"wdth\",\"value\":${lockScreenClockWidth.intValue}}," +
                    "{\"key\":\"ROND\",\"value\":${lockScreenClockRoundness.intValue}}]}"
            }
        val command = "settings put secure lock_screen_custom_clock_face '$json'"
        var success = false

        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success =
                    Settings.Secure.putString(
                        context.contentResolver,
                        "lock_screen_custom_clock_face",
                        json,
                    )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!success) {
            if (ShizukuUtils.hasPermission()) {
                ShizukuUtils.runCommand(command)
                success = true
            } else if (RootUtils.isRootPermissionGranted()) {
                RootUtils.runCommand(command)
                success = true
            }
        }

        if (success) {
            lockScreenClockId.value = clockId
        }
    }

    fun resetLockScreenClock(context: Context): Boolean {
        val key = "lock_screen_custom_clock_face"
        var success = false

        if (PermissionUtils.canWriteSecureSettings(context)) {
            try {
                success = Settings.Secure.putString(context.contentResolver, key, null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (!success) {
            val command = "settings delete secure $key"
            if (ShizukuUtils.hasPermission()) {
                ShizukuUtils.runCommand(command)
                success = true
            } else if (RootUtils.isRootPermissionGranted()) {
                RootUtils.runCommand(command)
                success = true
            }
        }

        if (success) {
            lockScreenClockId.value = null
        }
        return success
    }

    fun setLockScreenClockWeight(
        value: Int,
        context: Context,
    ) {
        lockScreenClockWeight.intValue = value
        settings.setLockScreenClockWeight(value)
        lockScreenClockId.value?.let { setLockScreenClockId(it, context) }
    }

    fun setLockScreenClockWidth(
        value: Int,
        context: Context,
    ) {
        lockScreenClockWidth.intValue = value
        settings.setLockScreenClockWidth(value)
        lockScreenClockId.value?.let { setLockScreenClockId(it, context) }
    }

    fun setLockScreenClockGrade(
        value: Int,
        context: Context,
    ) {
        lockScreenClockGrade.intValue = value
        settings.setLockScreenClockGrade(value)
        lockScreenClockId.value?.let { setLockScreenClockId(it, context) }
    }

    fun setLockScreenClockRoundness(
        value: Int,
        context: Context,
    ) {
        lockScreenClockRoundness.intValue = value
        settings.setLockScreenClockRoundness(value)
        lockScreenClockId.value?.let { setLockScreenClockId(it, context) }
    }

    fun setLockScreenClockColorTone(
        value: Int,
        context: Context,
    ) {
        lockScreenClockColorTone.intValue = value
        settings.setLockScreenClockColorTone(value)

        // Update effective seed color based on new tone
        if (lockScreenClockSelectedColorId.value != "DEFAULT") {
            val effectiveSeed =
                calculateEffectiveSeedColor(lockScreenClockSelectedColorId.value, value)
            lockScreenClockSeedColor.intValue = effectiveSeed
            settings.setLockScreenClockSeedColor(effectiveSeed)
        }

        lockScreenClockId.value?.let { setLockScreenClockId(it, context) }
    }

    fun setLockScreenClockColor(
        id: String,
        seed: Int,
        context: Context,
    ) {
        lockScreenClockSelectedColorId.value = id
        val effectiveSeed =
            if (id == "DEFAULT") {
                0
            } else {
                calculateEffectiveSeedColor(
                    id,
                    lockScreenClockColorTone.intValue,
                )
            }
        lockScreenClockSeedColor.intValue = effectiveSeed
        settings.setLockScreenClockSelectedColorId(id)
        settings.setLockScreenClockSeedColor(effectiveSeed)
        lockScreenClockId.value?.let { setLockScreenClockId(it, context) }
    }

    private fun calculateEffectiveSeedColor(
        colorId: String,
        tone: Int,
    ): Int {
        val baseColor =
            when (colorId) {
                "RED" -> Color.parseColor("#E57373")
                "GREEN" -> Color.parseColor("#81C784")
                "BLUE" -> Color.parseColor("#64B5F6")
                "YELLOW" -> Color.parseColor("#FFF176")
                "ORANGE" -> Color.parseColor("#FFB74D")
                "PURPLE" -> Color.parseColor("#BA68C8")
                "PINK" -> Color.parseColor("#F06292")
                "TEAL" -> Color.parseColor("#4DB6AC")
                else -> return 0
            }
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        // Calibrated HSV mapping to match user examples:
        // Tone 0  -> Saturation ~0.8, Value ~0.35 (Dark, saturated)
        // Tone 100 -> Saturation ~0.2, Value ~1.0  (Light, pastel)
        hsv[1] = (0.8f - (tone / 100f) * 0.6f).coerceIn(0f, 1f)
        hsv[2] = (0.35f + (tone / 100f) * 0.65f).coerceIn(0f, 1f)

        return Color.HSVToColor(hsv)
    }

    private fun readCurrentLockScreenClockId(context: Context): String? {
        return try {
            val raw =
                Settings.Secure.getString(
                    context.contentResolver,
                    "lock_screen_custom_clock_face",
                ) ?: return null
            // Extract clockId from JSON string like {"clockId":"DIGITAL_CLOCK_WEATHER"}
            val match = Regex(""""clockId":\s*"([^"]+)"""").find(raw)
            match?.groupValues?.getOrNull(1)
        } catch (e: Exception) {
            null
        }
    }
}
