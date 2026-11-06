package com.jawzf.pixeltoolkit.feature.widgets.stack

object StackSmartPick {
    fun pick(
        hostedComponents: List<String?>,
        playingPackage: String?,
        calendarPackages: Set<String>,
        charging: Boolean,
    ): Int? {
        fun first(match: (pkg: String, cls: String) -> Boolean): Int? =
            hostedComponents
                .indexOfFirst { it != null && match(it.substringBefore('/'), it.substringAfter('/', "")) }
                .takeIf { it >= 0 }

        return playingPackage?.let { playing -> first { pkg, _ -> pkg == playing } }
            ?: first { pkg, _ -> pkg in calendarPackages }
            ?: if (charging) first { _, cls -> cls.contains("batter", ignoreCase = true) } else null
    }
}
