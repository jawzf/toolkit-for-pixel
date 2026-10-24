/*
 * Copyright (c) 2026 sameerasw.com
 * License: MIT License
 *
 * Feature Module: Domain Layer Models & Registries
 * File: AppStandbyInfo.kt
 * Description: Domain model and business logic entry for AppStandbyInfo.kt.
 */

package com.jawzf.pixeltoolkit.domain.model

import android.graphics.drawable.Drawable

data class AppStandbyInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?,
    val bucket: Int,
)
