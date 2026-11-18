package com.jawzf.pixeltoolkit.ui.composables

import com.jawzf.pixeltoolkit.domain.model.Feature

fun activeNowFeatures(
    features: List<Feature>,
    pinnedKeys: List<String>,
    isOn: (Feature) -> Boolean,
): List<Feature> {
    val pinned = pinnedKeys.distinct().mapNotNull { key -> features.firstOrNull { it.id == key } }
    val pinnedIds = pinned.map { it.id }.toSet()
    val rest = features.filter { it.showToggle && it.id !in pinnedIds && isOn(it) }
    return pinned + rest
}

/** Features in [active] that are on but whose permission sheet still has a step not done. */
fun featuresNeedingFix(
    active: List<Feature>,
    isOn: (Feature) -> Boolean,
    hasMissingSteps: (Feature) -> Boolean,
): List<Feature> = active.filter { isOn(it) && hasMissingSteps(it) }
