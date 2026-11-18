package com.jawzf.pixeltoolkit.ui.composables

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.jawzf.pixeltoolkit.FeatureSettingsActivity
import com.jawzf.pixeltoolkit.SettingsActivity
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.cards.FeatureCard
import com.jawzf.pixeltoolkit.core.design.components.ListExpandToggleButton
import com.jawzf.pixeltoolkit.core.design.components.PrimaryButton
import com.jawzf.pixeltoolkit.core.design.components.ToolkitSearchField
import com.jawzf.pixeltoolkit.core.design.containers.RoundedCardContainer
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.scrollMotionBlur
import com.jawzf.pixeltoolkit.core.design.sheets.InstructionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.sheets.PermissionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitShapes
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.domain.model.Feature
import com.jawzf.pixeltoolkit.domain.registry.FeatureRegistry
import com.jawzf.pixeltoolkit.domain.registry.QSTileRegistry
import com.jawzf.pixeltoolkit.feature.apps.freeze.FreezeManager
import com.jawzf.pixeltoolkit.ui.activities.AppFreezingActivity
import com.jawzf.pixeltoolkit.ui.activities.AutomationsActivity
import com.jawzf.pixeltoolkit.ui.activities.PixelSearchbarSettingsActivity
import com.jawzf.pixeltoolkit.ui.activities.WallpaperActivity
import com.jawzf.pixeltoolkit.ui.activities.YourAndroidActivity
import com.jawzf.pixeltoolkit.ui.core.sheets.ReorderFavoritesBottomSheet
import com.jawzf.pixeltoolkit.ui.features.freeze.FrozenAppsSearchResultCard
import com.jawzf.pixeltoolkit.ui.features.permissions.PermissionsSearchResultCard
import com.jawzf.pixeltoolkit.ui.features.tiles.QSTilesSearchResultCard
import com.jawzf.pixeltoolkit.utils.BiometricSecurityHelper
import com.jawzf.pixeltoolkit.utils.PermissionUIHelper
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SetupFeatures(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    searchRequested: Boolean = false,
    onSearchHandled: () -> Unit = {},
    onHelpClick: () -> Unit = {},
) {
    viewModel.remap.isButtonRemapEnabled.value
    viewModel.nightLight.isDynamicNightLightEnabled.value

    viewModel.security.isScreenLockedSecurityEnabled.value
    val pinnedFeatureKeys by viewModel.pinnedFeatureKeys
    val context = LocalContext.current

    var showSheet by remember { mutableStateOf(false) }
    var currentFeature by remember { mutableStateOf<Feature?>(null) }
    var showReorderFavoritesSheet by remember { mutableStateOf(false) }

    // Help Sheet State
    var showHelpSheet by remember { mutableStateOf(false) }
    var selectedHelpFeature by remember {
        mutableStateOf<Feature?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.freeze.refreshFreezePickedApps(context, silent = true)
    }

    // Periodic check for Caffeinate status
    LaunchedEffect(Unit) {
        while (true) {
            viewModel.power.checkCaffeinateActive(context)
            delay(2000)
        }
    }

    val sheetFeature = currentFeature
    if (showSheet && sheetFeature != null) {
        val permissionItems =
            PermissionUIHelper.getPermissionItems(sheetFeature.permissionKeys, context, viewModel, context as? Activity)
        if (permissionItems.isNotEmpty()) {
            PermissionsBottomSheet(
                onDismissRequest = { showSheet = false },
                featureTitle = sheetFeature.title,
                permissions = permissionItems,
                onTurnOn = {
                    showSheet = false
                    if (sheetFeature.showToggle && !sheetFeature.isEnabled(viewModel)) {
                        BiometricSecurityHelper.runWithAuth(
                            activity = context as FragmentActivity,
                            feature = sheetFeature,
                            isToggle = true,
                            action = { sheetFeature.onToggle(viewModel, context, true) },
                        )
                    }
                },
                onHelpClick = {
                    showSheet = false
                    onHelpClick()
                },
            )
        }
    }

    var showInstructionsSheet by rememberSaveable { mutableStateOf(false) }

    if (showInstructionsSheet) {
        InstructionsBottomSheet(
            onDismissRequest = { showInstructionsSheet = false },
        )
    }

    if (showHelpSheet && selectedHelpFeature != null) {
        com.jawzf.pixeltoolkit.ui.core.sheets.FeatureHelpBottomSheet(
            onDismissRequest = {
                showHelpSheet = false
                selectedHelpFeature = null
            },
            feature = selectedHelpFeature!!,
        )
    }

    if (showReorderFavoritesSheet) {
        ReorderFavoritesBottomSheet(
            viewModel = viewModel,
            onDismissRequest = { showReorderFavoritesSheet = false },
        )
    }

    val lazyListState = rememberLazyListState()
    val view = LocalView.current
    val focusRequester = remember { FocusRequester() }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val searchQuery = viewModel.searchQuery.value

    BackHandler(enabled = isFocused || searchQuery.isNotEmpty()) {
        focusManager.clearFocus()
        viewModel.onSearchQueryChanged("", context)
        isFocused = false
    }

    LocalSoftwareKeyboardController.current
    WindowInsets.isImeVisible

    val allFeatures =
        FeatureRegistry.getFilteredFeatures(
            context,
            viewModel.isEnableUnsupportedFeatures.value,
            viewModel.isShowLegacyFeatures.value,
        )

    LaunchedEffect(searchRequested) {
        if (searchRequested) {
            lazyListState.animateScrollToItem(0)
            delay(100)
            focusRequester.requestFocus()
            onSearchHandled()
        }
    }

    val searchResults = viewModel.searchResults.value
    val isSearchingViewModel = viewModel.isSearching.value
    val recentSearches by viewModel.recentSearches
    val matchingTiles =
        remember(searchQuery, viewModel.isEnableUnsupportedFeatures.value, viewModel.isUseUsageAccess.value) {
            QSTileRegistry.searchTiles(
                context = context,
                query = searchQuery,
                includeUnsupported = viewModel.isEnableUnsupportedFeatures.value,
                isUseUsageStats = viewModel.isUseUsageAccess.value,
            )
        }
    val matchingPermissions =
        remember(searchQuery, viewModel) {
            PermissionUIHelper.searchPermissions(
                context = context,
                query = searchQuery,
                viewModel = viewModel,
                activity = context as? Activity,
            )
        }
    val freezePickedApps by viewModel.freeze.freezePickedApps
    val matchingFrozenApps =
        remember(searchQuery, freezePickedApps) {
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                freezePickedApps.filter { app ->
                    (
                        app.appName.contains(searchQuery, ignoreCase = true) ||
                            app.packageName.contains(searchQuery, ignoreCase = true)
                    ) &&
                        FreezeManager.isAppFrozen(context, app.packageName)
                }
            }
        }

    val isMotionBlurEnabled by viewModel.isMotionBlurEnabled

    val openPermissions: (Feature) -> Unit = {
        currentFeature = it
        showSheet = true
    }
    val openHelp: (Feature) -> Unit = {
        selectedHelpFeature = it
        showHelpSheet = true
    }

    LazyColumn(
        state = lazyListState,
        modifier =
            modifier
                .fillMaxSize()
                .scrollMotionBlur(lazyListState, enabled = isMotionBlurEnabled),
        contentPadding =
            PaddingValues(
                top = contentPadding.calculateTopPadding() + 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 64.dp,
            ),
    ) {
        item {
            HomeHeader(onSettingsClick = { context.startActivity(Intent(context, SettingsActivity::class.java)) })
        }

        item {
            // Search Bar
                ToolkitSearchField(
                    query = searchQuery,
                    onQueryChange = {
                        viewModel.onSearchQueryChanged(it, context)
                    },
                    placeholder = stringResource(R.string.search_placeholder),
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged {
                                isFocused = it.isFocused
                            },
                    leadingIcon = {
                        Box(
                            modifier =
                                Modifier
                                    .padding(start = 12.dp, end = 4.dp)
                                    .size(40.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            AnimatedVisibility(
                                visible = isSearchingViewModel,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }

                            AnimatedVisibility(
                                visible = !isSearchingViewModel,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.rounded_search_24),
                                    contentDescription = stringResource(R.string.label_search_content_description),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    },
                    showClear = isFocused || searchQuery.isNotEmpty(),
                    onClear = {
                        if (searchQuery.isNotEmpty()) {
                            viewModel.onSearchQueryChanged("", context)
                        } else {
                            focusManager.clearFocus()
                        }
                    },
                )
            }

            if (isFocused && searchQuery.isEmpty()) {
                item {
                    RecentSearchesSection(
                        recentSearches = recentSearches,
                        allFeatures = allFeatures,
                        pinnedFeatureKeys = pinnedFeatureKeys,
                        context = context,
                        viewModel = viewModel,
                    )
                }
            } else if (searchQuery.isNotEmpty()) {
                if (!isSearchingViewModel && searchResults.isEmpty() && matchingTiles.isEmpty() && matchingPermissions.isEmpty() && matchingFrozenApps.isEmpty()) {
                    item {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "¯\\_(ツ)_/¯",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = stringResource(id = R.string.search_no_results, searchQuery),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                maxLines = 2,
                                modifier =
                                    Modifier
                                        .padding(top = 8.dp)
                                        .padding(horizontal = 32.dp),
                            )
                        }
                    }
                }

                if (!isSearchingViewModel) {
                    item {
                        SearchResultsSection(
                            searchResults = searchResults,
                            allFeatures = allFeatures,
                            pinnedFeatureKeys = pinnedFeatureKeys,
                            context = context,
                            viewModel = viewModel,
                        )
                    }
                }
            } else if (!isFocused) {
                val isOn: (Feature) -> Boolean = { it.isEnabled(viewModel) }
                val active = activeNowFeatures(allFeatures, pinnedFeatureKeys, isOn)
                val needsFix =
                    featuresNeedingFix(active, isOn) {
                        missingPermissionItems(it, context, viewModel, context as? Activity).isNotEmpty()
                    }
                if (needsFix.isNotEmpty()) {
                    item {
                        HomeFixBanner(
                            count = needsFix.size,
                            onFix = {
                                val feature = needsFix.first()
                                BiometricSecurityHelper.runWithAuth(
                                    activity = context as FragmentActivity,
                                    feature = feature,
                                    action = { feature.onClick(context, viewModel) },
                                )
                            },
                        )
                    }
                }
                if (active.isNotEmpty()) {
                    item {
                        HomeSectionLabel(
                            text = stringResource(R.string.home_active_now),
                            action = if (pinnedFeatureKeys.size > 1) stringResource(R.string.home_edit_favorites) else null,
                            onAction = { showReorderFavoritesSheet = true },
                        )
                    }
                    item {
                        RoundedCardContainer(Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                            active.forEach { feature ->
                                HomeFeatureRow(feature, viewModel, feature.id in pinnedFeatureKeys, openPermissions, openHelp)
                            }
                        }
                    }
                }
                item { HomeSectionLabel(text = stringResource(R.string.home_browse)) }
                item { HomeToolsGroup(onOpen = { cls -> context.startActivity(Intent(context, cls)) }) }

                val topLevelFeatures =
                    allFeatures.filter { it.parentFeatureId == null && it.isVisibleInMain }
                val featureMap = topLevelFeatures.associateBy { it.id }

                val sectionFeatureIdsList =
                    listOf(
                        listOf(
                            "Notifications",
                            "Sound",
                            "Display",
                            "Maps power saving mode",
                            "Daily Wallpaper",
                            "Widgets",
                        ),
                        listOf(
                            "Input",
                            "Power and battery",
                            "Quick settings tiles",
                        ),
                        listOf(
                            "Security",
                            "Networks",
                        ),
                        listOf(
                            "Location reached",
                        ),
                    )

                val assignedIds = sectionFeatureIdsList.flatten().toSet()
                val unassignedFeatures = topLevelFeatures.filter { it.id !in assignedIds }

                val sections =
                    sectionFeatureIdsList
                        .map { ids ->
                            ids.mapNotNull { featureMap[it] }
                        }.filter { it.isNotEmpty() } +
                        if (unassignedFeatures.isNotEmpty()) {
                            listOf(
                                unassignedFeatures,
                            )
                        } else {
                            emptyList()
                        }

                sections.forEachIndexed { sectionIndex, sectionFeatures ->
                    item {
                        RoundedCardContainer(
                            modifier =
                                Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp),
                        ) {
                            sectionFeatures.forEach { feature ->
                                HomeFeatureRow(
                                    feature = feature,
                                    viewModel = viewModel,
                                    isPinned = feature.id in pinnedFeatureKeys,
                                    onNeedsPermission = openPermissions,
                                    onHelp = openHelp,
                                )
                            }
                        }
                    }
                }

                item {
                    ListExpandToggleButton(
                        isExpanded = false,
                        onToggle = {
                            showInstructionsSheet = true
                        },
                        title = R.string.label_help_guide,
                        description = R.string.label_help_guide,
                        iconRes = R.drawable.rounded_help_24,
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp, bottom = 12.dp),
                    )
                }
            }
        }
}

@Composable
private fun RecentSearchesSection(
    recentSearches: List<com.jawzf.pixeltoolkit.domain.model.SearchableItem>,
    allFeatures: List<com.jawzf.pixeltoolkit.domain.model.Feature>,
    pinnedFeatureKeys: List<String>,
    context: Context,
    viewModel: MainViewModel,
) {
    if (recentSearches.isEmpty()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            )
            Text(
                text = stringResource(R.string.label_no_recent_searches),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    } else {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.label_recent_searches),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.label_clear_all),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { viewModel.clearRecentSearches() },
            )
        }

        val validSearches =
            remember(recentSearches, allFeatures) {
                recentSearches.filter { item ->
                    item.title.isNotBlank() &&
                        allFeatures.any { it.id == item.featureKey }
                }
            }

        LaunchedEffect(recentSearches.size, validSearches.size) {
            if (recentSearches.isNotEmpty() && validSearches.size != recentSearches.size) {
                // If any corrupted/invalid feature IDs were found in history, clean them up
                viewModel.clearRecentSearches()
            }
        }

        RoundedCardContainer(
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            validSearches.forEach { result ->
                FeatureCard(
                    title = result.title,
                    isEnabled = true,
                    onToggle = {},
                    onClick = {
                        viewModel.addRecentSearch(result)
                        val feature = allFeatures.find { it.id == result.featureKey }
                        if (feature != null) {
                            val targetFeatureKey =
                                if (feature.searchTargetFeatureId != null) {
                                    feature.searchTargetFeatureId
                                } else if (!feature.hasMoreSettings && feature.parentFeatureId != null) {
                                    feature.parentFeatureId
                                } else {
                                    feature.id
                                }
                            val highlightKey =
                                if (!feature.hasMoreSettings && feature.parentFeatureId != null) {
                                    feature.id
                                } else {
                                    result.targetSettingHighlightKey
                                }
                            BiometricSecurityHelper.runWithAuth(
                                activity = context as FragmentActivity,
                                feature = feature,
                                action = {
                                    val intent =
                                        if (targetFeatureKey == "LiveWallpaper") {
                                            Intent(
                                                context,
                                                WallpaperActivity::class.java,
                                            )
                                        } else {
                                            Intent(
                                                context,
                                                FeatureSettingsActivity::class.java,
                                            ).apply {
                                                putExtra("feature", targetFeatureKey)
                                                highlightKey?.let {
                                                    putExtra("highlight_setting", it)
                                                }
                                            }
                                        }
                                    context.startActivity(intent)
                                },
                            )
                        }
                    },
                    iconRes = result.icon ?: R.drawable.rounded_settings_24,
                    showToggle = false,
                    hasMoreSettings = true,
                    description = result.description,
                    isBeta = result.isBeta,
                    isLegacy = result.isLegacy,
                    isUnsupported = result.isUnsupported,
                    isPinned = pinnedFeatureKeys.contains(result.featureKey),
                    onPinToggle = {
                        viewModel.togglePinFeature(result.featureKey)
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchResultsSection(
    searchResults: List<com.jawzf.pixeltoolkit.domain.model.SearchableItem>,
    allFeatures: List<com.jawzf.pixeltoolkit.domain.model.Feature>,
    pinnedFeatureKeys: List<String>,
    context: Context,
    viewModel: MainViewModel,
) {
    val query = viewModel.searchQuery.value
    val matchingTiles =
        remember(query, viewModel.isEnableUnsupportedFeatures.value, viewModel.isUseUsageAccess.value) {
            QSTileRegistry.searchTiles(
                context = context,
                query = query,
                includeUnsupported = viewModel.isEnableUnsupportedFeatures.value,
                isUseUsageStats = viewModel.isUseUsageAccess.value,
            )
        }
    val matchingPermissions =
        remember(query, viewModel) {
            PermissionUIHelper.searchPermissions(
                context = context,
                query = query,
                viewModel = viewModel,
                activity = context as? Activity,
            )
        }
    val freezePickedApps by viewModel.freeze.freezePickedApps
    val matchingFrozenApps =
        remember(query, freezePickedApps) {
            if (query.isBlank()) {
                emptyList()
            } else {
                freezePickedApps.filter { app ->
                    app.appName.contains(query, ignoreCase = true) ||
                        app.packageName.contains(query, ignoreCase = true)
                }
            }
        }

    var isExpanded by rememberSaveable(query) { mutableStateOf(false) }

    val nonQSSearchResults =
        remember(searchResults) {
            searchResults.filter { it.featureKey != "Quick settings tiles" }
        }

    val visibleResults =
        if (!isExpanded && nonQSSearchResults.size > 7) {
            nonQSSearchResults.take(7)
        } else {
            nonQSSearchResults
        }

    if (matchingTiles.isNotEmpty() || nonQSSearchResults.isNotEmpty() || matchingPermissions.isNotEmpty() || matchingFrozenApps.isNotEmpty()) {
        if (nonQSSearchResults.isNotEmpty()) {
            Text(
                text = stringResource(R.string.search_results_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 32.dp, top = 16.dp, bottom = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            RoundedCardContainer(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp),
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .animateContentSize(animationSpec = tween(durationMillis = 300)),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    for (result in visibleResults) {
                        FeatureCard(
                            title = result.title,
                            isEnabled = true,
                            onToggle = {},
                            highlightQuery = query,
                            onClick = {
                                viewModel.addRecentSearch(result)
                                val feature = allFeatures.find { it.id == result.featureKey }
                                if (feature != null) {
                                    val targetFeatureKey =
                                        if (feature.searchTargetFeatureId != null) {
                                            feature.searchTargetFeatureId
                                        } else if (!feature.hasMoreSettings && feature.parentFeatureId != null) {
                                            feature.parentFeatureId
                                        } else {
                                            feature.id
                                        }
                                    val highlightKey =
                                        if (!feature.hasMoreSettings && feature.parentFeatureId != null) {
                                            feature.id
                                        } else {
                                            result.targetSettingHighlightKey
                                        }
                                    BiometricSecurityHelper.runWithAuth(
                                        activity = context as FragmentActivity,
                                        feature = feature,
                                        action = {
                                            val intent =
                                                if (targetFeatureKey == "Pixel Searchbar") {
                                                    Intent(
                                                        context,
                                                        PixelSearchbarSettingsActivity::class.java,
                                                    )
                                                } else if (targetFeatureKey == "LiveWallpaper") {
                                                    Intent(
                                                        context,
                                                        WallpaperActivity::class.java,
                                                    )
                                                } else if (targetFeatureKey == "App updates") {
                                                    Intent(
                                                        context,
                                                        YourAndroidActivity::class.java,
                                                    )
                                                } else {
                                                    Intent(
                                                        context,
                                                        FeatureSettingsActivity::class.java,
                                                    ).apply {
                                                        putExtra("feature", targetFeatureKey)
                                                        highlightKey?.let {
                                                            putExtra("highlight_setting", it)
                                                        }
                                                    }
                                                }
                                            context.startActivity(intent)
                                        },
                                    )
                                } else {
                                    val intent =
                                        if (result.featureKey == "Pixel Searchbar") {
                                            Intent(
                                                context,
                                                PixelSearchbarSettingsActivity::class.java,
                                            )
                                        } else if (result.featureKey == "LiveWallpaper") {
                                            Intent(
                                                context,
                                                WallpaperActivity::class.java,
                                            )
                                        } else if (result.featureKey == "App updates") {
                                            Intent(
                                                context,
                                                YourAndroidActivity::class.java,
                                            )
                                        } else {
                                            Intent(context, FeatureSettingsActivity::class.java).apply {
                                                putExtra("feature", result.featureKey)
                                                result.targetSettingHighlightKey?.let {
                                                    putExtra("highlight_setting", it)
                                                }
                                            }
                                        }
                                    context.startActivity(intent)
                                }
                            },
                            iconRes = result.icon ?: R.drawable.rounded_settings_24,
                            modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp),
                            showToggle = false,
                            hasMoreSettings = true,
                            isBeta = result.isBeta,
                            isLegacy = result.isLegacy,
                            isUnsupported = result.isUnsupported,
                            descriptionOverride =
                                if (result.parentFeature !=
                                    null
                                ) {
                                    "${result.parentFeature} > ${result.description}"
                                } else {
                                    result.description
                                },
                            isPinned = pinnedFeatureKeys.contains(result.featureKey),
                            onPinToggle = {
                                viewModel.togglePinFeature(result.featureKey)
                            },
                        )
                    }
                }
            }

            if (nonQSSearchResults.size > 7) {
                ListExpandToggleButton(
                    isExpanded = isExpanded,
                    onToggle = { isExpanded = !isExpanded },
                    title = R.string.action_show_more,
                    description = R.string.action_show_less,
                    expandedText = stringResource(R.string.action_show_less),
                    collapsedText = stringResource(R.string.action_show_more),
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 4.dp),
                )
            }
        }

        if (matchingFrozenApps.isNotEmpty()) {
            FrozenAppsSearchResultCard(
                apps = matchingFrozenApps,
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                onNavigateToFreezeTab = null,
            )
        }

        if (matchingTiles.isNotEmpty()) {
            QSTilesSearchResultCard(
                tiles = matchingTiles,
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }

        if (matchingPermissions.isNotEmpty()) {
            PermissionsSearchResultCard(
                permissions = matchingPermissions,
                viewModel = viewModel,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun HomeHeader(onSettingsClick: () -> Unit) {
    val view = LocalView.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.tab_toolkit),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ToolkitTheme.colors.text,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onSettingsClick()
            },
            modifier = Modifier.size(44.dp),
            colors =
                IconButtonDefaults.iconButtonColors(
                    containerColor = ToolkitTheme.colors.surface,
                    contentColor = ToolkitTheme.colors.text,
                ),
        ) {
            Icon(
                painter = painterResource(R.drawable.rounded_settings_24),
                contentDescription = stringResource(R.string.content_desc_settings),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun HomeToolsGroup(onOpen: (Class<*>) -> Unit) {
    RoundedCardContainer(
        modifier =
            Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
    ) {
        FeatureCard(
            title = R.string.tab_your_android,
            description = R.string.home_your_android_desc,
            iconRes = R.drawable.rounded_mobile_24,
            showToggle = false,
            hasMoreSettings = true,
            isEnabled = false,
            onToggle = {},
            onClick = { onOpen(YourAndroidActivity::class.java) },
        )
        FeatureCard(
            title = R.string.tab_freeze,
            description = R.string.tab_freeze_subtitle,
            iconRes = R.drawable.rounded_mode_cool_24,
            showToggle = false,
            hasMoreSettings = true,
            isEnabled = false,
            onToggle = {},
            onClick = { onOpen(AppFreezingActivity::class.java) },
        )
        FeatureCard(
            title = R.string.diy_tab_automations,
            description = R.string.home_automations_desc,
            iconRes = R.drawable.rounded_experiment_24,
            showToggle = false,
            hasMoreSettings = true,
            isEnabled = false,
            onToggle = {},
            onClick = { onOpen(AutomationsActivity::class.java) },
        )
    }
}

@Composable
private fun HomeFeatureRow(
    feature: Feature,
    viewModel: MainViewModel,
    isPinned: Boolean,
    onNeedsPermission: (Feature) -> Unit,
    onHelp: (Feature) -> Unit,
) {
    val context = LocalContext.current
    val isOn = feature.isEnabled(viewModel)
    val needsSetup = missingPermissionItems(feature, context, viewModel, context as? Activity).isNotEmpty()
    FeatureCard(
        title = feature.title,
        isEnabled = isOn,
        onToggle = { enabled ->
            if (enabled && needsSetup) {
                onNeedsPermission(feature)
            } else {
                BiometricSecurityHelper.runWithAuth(
                    activity = context as FragmentActivity,
                    feature = feature,
                    isToggle = true,
                    action = {
                        feature.onToggle(viewModel, context, enabled)
                    },
                )
            }
        },
        onClick = {
            BiometricSecurityHelper.runWithAuth(
                activity = context as FragmentActivity,
                feature = feature,
                action = {
                    feature.onClick(context, viewModel)
                },
            )
        },
        iconRes = feature.iconRes,
        modifier = Modifier.padding(horizontal = 0.dp, vertical = 0.dp),
        isToggleEnabled = isOn || feature.isToggleEnabled(viewModel, context),
        showToggle = feature.showToggle,
        hasMoreSettings = feature.hasMoreSettings,
        onDisabledToggleClick = {
            if (feature.id == "Screen locked security") {
                feature.onClick(context, viewModel)
            } else {
                onNeedsPermission(feature)
            }
        },
        description = feature.description,
        isBeta = feature.isBeta,
        isLegacy = feature.isLegacy,
        isUnsupported = !feature.isDeviceSupported(context),
        isPinned = isPinned,
        onPinToggle = {
            viewModel.togglePinFeature(feature.id)
        },
        onHelpClick =
            if (feature.aboutDescription != null) {
                { onHelp(feature) }
            } else {
                null
            },
        hasBadge = false,
    )
}

@Composable
private fun HomeSectionLabel(
    text: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    val view = LocalView.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 4.dp, bottom = 8.dp)
                .heightIn(min = 32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = ToolkitTheme.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (action != null) {
            TextButton(
                onClick = {
                    HapticUtil.performVirtualKeyHaptic(view)
                    onAction()
                },
            ) {
                Text(
                    text = action,
                    color = ToolkitTheme.colors.accent,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun HomeFixBanner(
    count: Int,
    onFix: () -> Unit,
) {
    val view = LocalView.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .clip(ToolkitShapes.field)
                .background(ToolkitTheme.colors.warningContainer)
                .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.rounded_info_24),
            contentDescription = null,
            tint = ToolkitTheme.colors.onWarningContainer,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = pluralStringResource(R.plurals.home_fix_banner, count, count),
            style = MaterialTheme.typography.bodyMedium,
            color = ToolkitTheme.colors.onWarningContainer,
            modifier = Modifier.weight(1f),
        )
        PrimaryButton(
            onClick = {
                HapticUtil.performVirtualKeyHaptic(view)
                onFix()
            },
            modifier = Modifier.height(36.dp),
        ) {
            Text(stringResource(R.string.home_fix_action))
        }
    }
}
