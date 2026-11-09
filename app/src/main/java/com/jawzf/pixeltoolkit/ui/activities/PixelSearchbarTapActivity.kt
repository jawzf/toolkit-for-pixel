package com.jawzf.pixeltoolkit.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.jawzf.pixeltoolkit.feature.automation.diy.Automation
import com.jawzf.pixeltoolkit.feature.automation.diy.DIYRepository
import com.jawzf.pixeltoolkit.services.automation.executors.CombinedActionExecutor
import kotlinx.coroutines.launch

class PixelSearchbarTapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DIYRepository.init(applicationContext)
        val automation =
            DIYRepository.automations.value.find { it.type == Automation.Type.PIXEL_SEARCHBAR }

        if (automation != null && automation.actionList.isNotEmpty() && automation.isEnabled) {
            lifecycleScope.launch {
                CombinedActionExecutor.executeAll(applicationContext, automation.actionList)
                finish()
            }
        } else {
            val resultsIntent =
                Intent(this, PixelSearchResultsActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            startActivity(resultsIntent)
            finish()
        }
    }
}
