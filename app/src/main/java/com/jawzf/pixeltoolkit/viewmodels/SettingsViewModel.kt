package com.jawzf.pixeltoolkit.viewmodels

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.jawzf.pixeltoolkit.data.repository.SettingsRepository
import com.jawzf.pixeltoolkit.domain.DIYTabs
import com.jawzf.pixeltoolkit.domain.HapticFeedbackType

class SettingsViewModel : ViewModel() {
    val pinnedFeatureKeys = mutableStateOf<List<String>>(emptyList())
    val pinnedQsTileKeys = mutableStateOf<List<String>>(emptyList())
    val hapticFeedbackType = mutableStateOf(HapticFeedbackType.SUBTLE)
    val defaultTab = mutableStateOf(DIYTabs.TOOLKIT)

    /**
     * Loads saved pinned features and user settings from repository.
     *
     * @param context [Context] Application context for shared preferences access.
     */
    fun loadSettings(context: Context) {
        val repo = SettingsRepository(context)
        pinnedFeatureKeys.value = repo.getPinnedFeatures()
        pinnedQsTileKeys.value = repo.getPinnedQsTiles()
    }

    /**
     * Toggles a pinned feature key.
     *
     * @param context [Context] Application context for persistence.
     * @param key [String] Unique key of the feature card.
     */
    fun togglePinFeature(
        context: Context,
        key: String,
    ) {
        val repo = SettingsRepository(context)
        val current = pinnedFeatureKeys.value.toMutableList()
        if (current.contains(key)) {
            current.remove(key)
        } else {
            current.add(key)
        }
        repo.savePinnedFeatures(current)
        pinnedFeatureKeys.value = current
    }
}
