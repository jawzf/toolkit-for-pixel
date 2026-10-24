package com.jawzf.pixeltoolkit.services

import android.media.AudioManager
import com.jawzf.pixeltoolkit.domain.diy.Action
import com.jawzf.pixeltoolkit.services.automation.modules.SoundModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SoundModuleTest {
    @Test
    fun mapsNormalRingerModeToSound() {
        assertEquals(Action.SoundModeType.SOUND, SoundModule.soundModeOf(AudioManager.RINGER_MODE_NORMAL))
    }

    @Test
    fun mapsVibrateRingerModeToVibrate() {
        assertEquals(Action.SoundModeType.VIBRATE, SoundModule.soundModeOf(AudioManager.RINGER_MODE_VIBRATE))
    }

    @Test
    fun mapsSilentRingerModeToSilent() {
        assertEquals(Action.SoundModeType.SILENT, SoundModule.soundModeOf(AudioManager.RINGER_MODE_SILENT))
    }

    @Test
    fun returnsNullForUnknownRingerMode() {
        assertNull(SoundModule.soundModeOf(-1))
        assertNull(SoundModule.soundModeOf(999))
    }
}
