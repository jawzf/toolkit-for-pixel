package com.jawzf.pixeltoolkit.ui.composables

import android.app.Activity
import android.content.Context
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionItem
import com.jawzf.pixeltoolkit.domain.model.Feature
import com.jawzf.pixeltoolkit.utils.PermissionUIHelper
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

/** Steps of [feature]'s permission sheet that aren't done yet. Empty means it can be turned on. */
fun missingPermissionItems(
    feature: Feature,
    context: Context,
    viewModel: MainViewModel,
    activity: Activity?,
): List<PermissionItem> =
    PermissionUIHelper
        .getPermissionItems(feature.permissionKeys, context, viewModel, activity)
        .filterNot { it.isGranted }
