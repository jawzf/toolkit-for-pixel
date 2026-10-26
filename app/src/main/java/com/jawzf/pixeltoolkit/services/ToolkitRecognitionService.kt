package com.jawzf.pixeltoolkit.services

import android.content.Intent
import android.speech.RecognitionService

class ToolkitRecognitionService : RecognitionService() {
    override fun onStartListening(
        recognizerIntent: Intent?,
        listener: Callback?,
    ) {
    }

    override fun onStopListening(listener: Callback?) {
    }

    override fun onCancel(listener: Callback?) {
    }
}
