package com.jawzf.pixeltoolkit.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.feature.automation.engine.CombinedActionExecutor
import kotlinx.coroutines.launch

// Exported for launchers, so intents carry only an id and the action is read from app storage
class PinnedActionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)

        val action =
            intent.getStringExtra(EXTRA_SHORTCUT_ID)?.let { SettingsRepository(this).getPinnedAction(it) }
        if (action == null) {
            Toast.makeText(this, R.string.pinned_action_missing_toast, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            CombinedActionExecutor.execute(this@PinnedActionActivity, action)
            finish()
        }
    }

    companion object {
        const val EXTRA_SHORTCUT_ID = "shortcut_id"
    }
}
