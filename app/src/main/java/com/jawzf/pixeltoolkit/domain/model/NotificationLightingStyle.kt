package com.jawzf.pixeltoolkit.domain.model

enum class NotificationLightingStyle {
    STROKE,
    GLOW,
    INDICATOR,
    SWEEP,
    SYSTEM,
    RIPPLE,
    DASH,
    ;

    val usesRestrictedColorModes: Boolean
        get() = this == RIPPLE || this == DASH
}
