package com.jawzf.pixeltoolkit.core.design.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object ToolkitShapes {
    val group = RoundedCornerShape(16.dp)
    val field = RoundedCornerShape(14.dp)
    val iconTile = RoundedCornerShape(11.dp)
    val pill = CircleShape
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}

val MinTouchTarget = 44.dp

val Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(11.dp),
        medium = RoundedCornerShape(14.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(28.dp),
    )
