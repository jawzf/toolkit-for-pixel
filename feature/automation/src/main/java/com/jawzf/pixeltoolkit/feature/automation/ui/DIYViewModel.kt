package com.jawzf.pixeltoolkit.feature.automation.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jawzf.pixeltoolkit.core.data.action.Action
import com.jawzf.pixeltoolkit.feature.automation.diy.Automation
import com.jawzf.pixeltoolkit.feature.automation.diy.DIYRepository
import com.jawzf.pixeltoolkit.feature.automation.diy.Macro
import com.jawzf.pixeltoolkit.feature.automation.diy.State
import com.jawzf.pixeltoolkit.feature.automation.diy.Trigger
import com.jawzf.pixeltoolkit.services.automation.executors.CombinedActionExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DIYViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val repository = DIYRepository

    init {
        repository.init(application)
    }

    val automations: StateFlow<List<Automation>> =
        repository.automations
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    val macros: StateFlow<List<Macro>> =
        repository.macros
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList(),
            )

    /**
     * Executes the delete automation operation.
     *
     * @param id [String] Target id.
     */
    fun deleteAutomation(id: String) {
        repository.removeAutomation(id)
    }

    /**
     * Executes the delete macro operation.
     *
     * @param id [String] Target id.
     */
    fun deleteMacro(id: String) {
        repository.removeMacro(id)
    }

    /**
     * Executes the toggle automation operation.
     *
     * @param id [String] Target id.
     */
    fun toggleAutomation(id: String) {
        repository.getAutomation(id)?.let { automation ->
            repository.updateAutomation(automation.copy(isEnabled = !automation.isEnabled))
        }
    }

    fun setAutomationsEnabled(ids: Set<String>, enabled: Boolean) {
        ids.forEach { id ->
            repository.getAutomation(id)?.let { automation ->
                if (automation.isEnabled != enabled) {
                    repository.updateAutomation(automation.copy(isEnabled = enabled))
                }
            }
        }
    }

    fun deleteAutomations(ids: Set<String>) {
        ids.forEach { repository.removeAutomation(it) }
    }

    /**
     * Runs the automation's action(s) immediately, without waiting for its trigger/state.
     *
     * @param automation [Automation] Target automation.
     */
    fun testAutomation(automation: Automation) {
        val context = getApplication<Application>().applicationContext
        val actionsToTest =
            if (automation.type == Automation.Type.STATE || automation.type == Automation.Type.APP) {
                automation.entryActionList
            } else {
                automation.actionList
            }
        viewModelScope.launch {
            CombinedActionExecutor.executeAll(context, actionsToTest)
        }
    }

    /**
     * Runs the macro's action(s) immediately.
     *
     * @param macro [Macro] Target macro.
     */
    fun testMacro(macro: Macro) {
        val context = getApplication<Application>().applicationContext
        viewModelScope.launch {
            CombinedActionExecutor.executeAll(context, macro.actionList)
        }
    }
}
