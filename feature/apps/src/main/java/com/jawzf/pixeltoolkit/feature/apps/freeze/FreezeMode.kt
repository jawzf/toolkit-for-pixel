package com.jawzf.pixeltoolkit.feature.apps.freeze

enum class FreezeMode(
    val value: Int,
) {
    FREEZE(0),
    SUSPEND(1),
    ;

    companion object {
        fun fromInt(value: Int) = entries.find { it.value == value } ?: FREEZE
    }
}
