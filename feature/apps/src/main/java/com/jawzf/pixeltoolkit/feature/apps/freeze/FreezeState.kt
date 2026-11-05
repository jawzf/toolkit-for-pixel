package com.jawzf.pixeltoolkit.feature.apps.freeze

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.annotation.Keep
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.jawzf.pixeltoolkit.core.data.apps.AppUtil
import com.jawzf.pixeltoolkit.core.data.apps.NotificationApp
import com.jawzf.pixeltoolkit.core.data.prefs.AppSelection
import com.jawzf.pixeltoolkit.feature.apps.AppsSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/** Compose state and actions for App Freezing. */
class FreezeState(
    private val scope: CoroutineScope,
) {
    private lateinit var settings: AppsSettings
    private var appContext: Context? = null

    val isFreezeWhenLockedEnabled = mutableStateOf(false)
    val freezeLockDelayIndex = mutableIntStateOf(1)
    val freezePickedApps = mutableStateOf<List<NotificationApp>>(emptyList())
    val isFreezePickedAppsLoading = mutableStateOf(false)
    val freezeAutoExcludedApps = mutableStateOf<Set<String>>(emptySet())
    val isFreezeDontFreezeActiveAppsEnabled = mutableStateOf(false)
    val freezeMode = mutableIntStateOf(0)
    val isFreezeShowInLauncherEnabled = mutableStateOf(true)
    val freezeTags = mutableStateOf<List<AppTag>>(emptyList())
    val freezeAppTagMap = mutableStateOf<Map<String, List<String>>>(emptyMap())
    val isFreezeTagColorCodedEnabled = mutableStateOf(false)

    /** Reads the saved values and syncs the freeze launcher icon. Call before anything else. */
    fun load(context: Context) {
        appContext = context.applicationContext
        settings = AppsSettings(context)
        freezeMode.intValue = settings.getFreezeMode()
        isFreezeWhenLockedEnabled.value =
            settings.getBoolean(AppsSettings.KEY_FREEZE_WHEN_LOCKED_ENABLED)
        isFreezeDontFreezeActiveAppsEnabled.value =
            settings.getBoolean(AppsSettings.KEY_FREEZE_DONT_FREEZE_ACTIVE_APPS)
        freezeLockDelayIndex.intValue =
            settings.getInt(AppsSettings.KEY_FREEZE_LOCK_DELAY_INDEX, 1)
        freezeAutoExcludedApps.value = settings.getFreezeAutoExcludedApps()
        isFreezeShowInLauncherEnabled.value =
            settings.getBoolean(AppsSettings.KEY_FREEZE_SHOW_IN_LAUNCHER, true)
        freezeTags.value = settings.getFreezeTags()
        freezeAppTagMap.value = settings.getFreezeAppTagMap()
        isFreezeTagColorCodedEnabled.value =
            settings.getBoolean(
                AppsSettings.KEY_FREEZE_TAG_COLOR_CODED_ENABLED,
                false,
            )

        val showLauncher = isFreezeShowInLauncherEnabled.value
        val componentName = ComponentName(context, "com.jawzf.pixeltoolkit.AppFreezingLauncher")
        try {
            val currentState = context.packageManager.getComponentEnabledSetting(componentName)
            val targetState =
                if (showLauncher) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            if (currentState != targetState) {
                context.packageManager.setComponentEnabledSetting(
                    componentName,
                    targetState,
                    PackageManager.DONT_KILL_APP,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Updates the state for a changed preference key; ignores other keys. */
    fun onPreferenceChanged(key: String) {
        when (key) {
            AppsSettings.KEY_FREEZE_WHEN_LOCKED_ENABLED ->
                isFreezeWhenLockedEnabled.value =
                    settings.getBoolean(key)

            AppsSettings.KEY_FREEZE_DONT_FREEZE_ACTIVE_APPS ->
                isFreezeDontFreezeActiveAppsEnabled.value =
                    settings.getBoolean(key)

            AppsSettings.KEY_FREEZE_LOCK_DELAY_INDEX ->
                freezeLockDelayIndex.intValue =
                    settings.getInt(key, 1)

            AppsSettings.KEY_FREEZE_AUTO_EXCLUDED_APPS -> {
                freezeAutoExcludedApps.value =
                    settings.getFreezeAutoExcludedApps()
            }

            AppsSettings.KEY_FREEZE_MODE -> {
                freezeMode.intValue = settings.getFreezeMode()
            }

            AppsSettings.KEY_FREEZE_SHOW_IN_LAUNCHER -> {
                val enabled = settings.getBoolean(key, true)
                isFreezeShowInLauncherEnabled.value = enabled
                appContext?.let { ctx ->
                    val componentName =
                        ComponentName(ctx, "com.jawzf.pixeltoolkit.AppFreezingLauncher")
                    try {
                        ctx.packageManager.setComponentEnabledSetting(
                            componentName,
                            if (enabled) {
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                            } else {
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            },
                            PackageManager.DONT_KILL_APP,
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            else -> {}
        }
    }

    fun setFreezeWhenLockedEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFreezeWhenLockedEnabled.value = enabled
        settings.putBoolean(AppsSettings.KEY_FREEZE_WHEN_LOCKED_ENABLED, enabled)
    }

    fun setFreezeDontFreezeActiveAppsEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFreezeDontFreezeActiveAppsEnabled.value = enabled
        settings.putBoolean(
            AppsSettings.KEY_FREEZE_DONT_FREEZE_ACTIVE_APPS,
            enabled,
        )
    }

    fun setFreezeLockDelayIndex(
        index: Int,
        context: Context,
    ) {
        freezeLockDelayIndex.intValue = index
        settings.putInt(AppsSettings.KEY_FREEZE_LOCK_DELAY_INDEX, index)
    }

    fun setFreezeShowInLauncherEnabled(
        enabled: Boolean,
        context: Context,
    ) {
        isFreezeShowInLauncherEnabled.value = enabled
        settings.putBoolean(AppsSettings.KEY_FREEZE_SHOW_IN_LAUNCHER, enabled)

        val componentName = ComponentName(context, "com.jawzf.pixeltoolkit.AppFreezingLauncher")
        try {
            context.packageManager.setComponentEnabledSetting(
                componentName,
                if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                },
                PackageManager.DONT_KILL_APP,
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveFreezeSelectedApps(
        context: Context,
        apps: List<AppSelection>,
    ) {
        settings.saveFreezeSelectedApps(apps)
        refreshFreezePickedApps(
            context,
            silent = false,
        )
    }

    fun loadFreezeSelectedApps(context: Context): List<AppSelection> = settings.loadFreezeSelectedApps()

    fun updateFreezeAppEnabled(
        context: Context,
        packageName: String,
        enabled: Boolean,
    ) {
        settings.updateFreezeAppSelection(packageName, enabled)
        refreshFreezePickedApps(context, silent = true)
    }

    fun updateFreezeAppAutoFreeze(
        context: Context,
        packageName: String,
        autoFreezeEnabled: Boolean,
    ) {
        val currentSet = freezeAutoExcludedApps.value.toMutableSet()
        if (autoFreezeEnabled) {
            currentSet.remove(packageName)
        } else {
            currentSet.add(packageName)
        }
        freezeAutoExcludedApps.value = currentSet
        settings.saveFreezeAutoExcludedApps(currentSet)
        refreshFreezePickedApps(context, silent = true)
    }

    private fun syncNeverAutoFreezeApps(context: Context) {
        val neverAutoFreezeTagIds =
            freezeTags.value
                .filter { it.neverAutoFreeze }
                .map { it.id }
                .toSet()
        if (neverAutoFreezeTagIds.isEmpty()) return

        val currentExcluded = freezeAutoExcludedApps.value.toMutableSet()
        var changed = false
        freezeAppTagMap.value.forEach { (pkg, tagIds) ->
            if (tagIds.any { neverAutoFreezeTagIds.contains(it) }) {
                if (currentExcluded.add(pkg)) {
                    changed = true
                }
            }
        }
        if (changed) {
            freezeAutoExcludedApps.value = currentExcluded
            settings.saveFreezeAutoExcludedApps(currentExcluded)
            refreshFreezePickedApps(context, silent = true)
        }
    }

    fun addFreezeTag(
        context: Context,
        tag: AppTag,
    ) {
        val updated = freezeTags.value + tag
        freezeTags.value = updated
        settings.saveFreezeTags(updated)
        syncNeverAutoFreezeApps(context)
    }

    fun updateFreezeTag(
        context: Context,
        tag: AppTag,
    ) {
        val updated = freezeTags.value.map { if (it.id == tag.id) tag else it }
        freezeTags.value = updated
        settings.saveFreezeTags(updated)
        syncNeverAutoFreezeApps(context)
    }

    fun deleteFreezeTag(
        context: Context,
        tagId: String,
    ) {
        val updatedTags = freezeTags.value.filter { it.id != tagId }
        freezeTags.value = updatedTags
        settings.saveFreezeTags(updatedTags)

        val updatedMap =
            freezeAppTagMap.value
                .mapValues { (_, tagIds) ->
                    tagIds.filter { it != tagId }
                }.filterValues { it.isNotEmpty() }
        freezeAppTagMap.value = updatedMap
        settings.saveFreezeAppTagMap(updatedMap)
    }

    fun setAppTags(
        context: Context,
        packageName: String,
        tagIds: List<String>,
    ) {
        val currentMap = freezeAppTagMap.value.toMutableMap()
        if (tagIds.isEmpty()) {
            currentMap.remove(packageName)
        } else {
            currentMap[packageName] = tagIds
        }
        freezeAppTagMap.value = currentMap
        settings.saveFreezeAppTagMap(currentMap)
        syncNeverAutoFreezeApps(context)
    }

    fun setFreezeTagColorCoded(enabled: Boolean) {
        isFreezeTagColorCodedEnabled.value = enabled
        settings.putBoolean(
            AppsSettings.KEY_FREEZE_TAG_COLOR_CODED_ENABLED,
            enabled,
        )
    }

    fun refreshFreezePickedApps(
        context: Context,
        silent: Boolean = false,
    ) {
        scope.launch {
            if (!silent) isFreezePickedAppsLoading.value = true
            try {
                val result =
                    withContext(Dispatchers.Default) {
                        val selections = loadFreezeSelectedApps(context).filter { it.isEnabled }
                        if (selections.isEmpty()) return@withContext emptyList()

                        val pickedPkgNames = selections.map { it.packageName }
                        val relevantApps = AppUtil.getAppsByPackageNames(context, pickedPkgNames)

                        val merged = AppUtil.mergeWithSavedApps(relevantApps, selections)
                        val currentExcluded = freezeAutoExcludedApps.value

                        val filteredExcluded =
                            currentExcluded.filter { pickedPkgNames.contains(it) }.toSet()

                        merged
                            .map { it.copy(isEnabled = !filteredExcluded.contains(it.packageName)) }
                            .sortedBy { it.appName.lowercase() }
                    }

                freezePickedApps.value = result

                val currentExcluded = freezeAutoExcludedApps.value
                val selections = loadFreezeSelectedApps(context).filter { it.isEnabled }
                val pickedPkgNames = selections.map { it.packageName }
                val filteredExcluded =
                    currentExcluded.filter { pickedPkgNames.contains(it) }.toSet()
                if (filteredExcluded.size != currentExcluded.size) {
                    freezeAutoExcludedApps.value = filteredExcluded
                    settings.saveFreezeAutoExcludedApps(filteredExcluded)
                }
            } finally {
                if (!silent) isFreezePickedAppsLoading.value = false
            }
        }
    }

    fun freezeAllAuto(context: Context) {
        scope.launch(Dispatchers.IO) {
            FreezeManager.freezeAll(context)
        }
    }

    fun unfreezeAllAuto(context: Context) {
        scope.launch(Dispatchers.IO) {
            FreezeManager.unfreezeAll(context)
        }
    }

    fun freezeAllManual(context: Context) {
        scope.launch(Dispatchers.IO) {
            FreezeManager.freezeAllManual(context)
        }
    }

    fun unfreezeAllManual(context: Context) {
        scope.launch(Dispatchers.IO) {
            FreezeManager.unfreezeAllManual(context)
        }
    }

    fun launchAndUnfreezeApp(
        context: Context,
        packageName: String,
    ) {
        scope.launch(Dispatchers.IO) {
            val isFrozen =
                FreezeManager.isAppFrozen(context, packageName)
            if (isFrozen) {
                FreezeManager.unfreezeApp(context, packageName)
                delay(100)
            }

            val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
            }
        }
    }

    fun freezeAllApps(context: Context) {
        scope.launch(Dispatchers.IO) {
            FreezeManager.freezeAllManual(context)
            refreshFreezePickedApps(context)
        }
    }

    fun unfreezeAllApps(context: Context) {
        scope.launch(Dispatchers.IO) {
            FreezeManager.unfreezeAllManual(context)
            refreshFreezePickedApps(context)
        }
    }

    fun freezeAutomaticApps(context: Context) {
        scope.launch(Dispatchers.IO) {
            FreezeManager.freezeAll(context)
            refreshFreezePickedApps(context)
        }
    }

    fun anyAppsCurrentlyFrozen(context: Context): Boolean {
        val picked = freezePickedApps.value
        return picked.any {
            FreezeManager.isAppFrozen(
                context,
                it.packageName,
            )
        }
    }

    fun setFreezeMode(
        mode: Int,
        context: Context,
    ) {
        freezeMode.intValue = mode
        settings.putInt(AppsSettings.KEY_FREEZE_MODE, mode)
    }

    @Keep
    data class FreezeBackupData(
        @SerializedName("apps") val apps: List<AppSelection>? = null,
        @SerializedName("tags") val tags: List<AppTag>? = null,
        @SerializedName("appTagMap") val appTagMap: Map<String, List<String>>? = null,
    )

    fun exportFreezeApps(outputStream: OutputStream) {
        try {
            val apps = settings.loadFreezeSelectedApps()
            val tags = settings.getFreezeTags()
            val appTagMap = settings.getFreezeAppTagMap()
            val backupData = FreezeBackupData(apps = apps, tags = tags, appTagMap = appTagMap)
            val gson = Gson()
            val json = gson.toJson(backupData)
            outputStream.write(json.toByteArray())
            outputStream.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                outputStream.close()
            } catch (ignored: Exception) {
            }
        }
    }

    private fun parseFreezeBackup(json: String): FreezeBackupData {
        val gson = Gson()
        return if (json.trim().startsWith("[")) {
            val apps = gson.fromJson(json, Array<AppSelection>::class.java).toList()
            FreezeBackupData(apps = apps)
        } else {
            gson.fromJson(json, FreezeBackupData::class.java) ?: FreezeBackupData()
        }
    }

    private fun mergeImportedTags(importedTags: List<AppTag>) {
        if (importedTags.isEmpty()) return
        val currentTags = settings.getFreezeTags().toMutableList()
        importedTags.forEach { importedTag ->
            if (currentTags.none { it.id == importedTag.id }) {
                currentTags.add(importedTag)
            }
        }
        freezeTags.value = currentTags
        settings.saveFreezeTags(currentTags)
    }

    private fun mergeImportedTagMap(
        context: Context,
        importedMap: Map<String, List<String>>,
        installedPkgs: Set<String>,
    ) {
        if (importedMap.isEmpty()) return
        val currentMap = settings.getFreezeAppTagMap().toMutableMap()
        importedMap.forEach { (pkg, tagIds) ->
            if (installedPkgs.contains(pkg)) {
                currentMap[pkg] = tagIds
            }
        }
        freezeAppTagMap.value = currentMap
        settings.saveFreezeAppTagMap(currentMap)
        syncNeverAutoFreezeApps(context)
    }

    fun importFreezeApps(
        context: Context,
        inputStream: InputStream,
        allowSystem: Boolean,
    ): Boolean =
        try {
            val json = inputStream.bufferedReader().use { it.readText() }
            val backupData = parseFreezeBackup(json)
            val importedApps = backupData.apps ?: emptyList()
            val importedTags = backupData.tags ?: emptyList()
            val importedMap = backupData.appTagMap ?: emptyMap()

            val pm = context.packageManager
            val installedApps =
                importedApps.filter { app ->
                    try {
                        pm.getPackageInfo(app.packageName, 0)
                        allowSystem || !AppUtil.hasSystemFlag(context, app.packageName)
                    } catch (e: Exception) {
                        false
                    }
                }

            settings.saveFreezeSelectedApps(installedApps)
            mergeImportedTags(importedTags)
            mergeImportedTagMap(context, importedMap, installedApps.map { it.packageName }.toSet())

            refreshFreezePickedApps(context, silent = true)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                inputStream.close()
            } catch (ignored: Exception) {
            }
        }

    companion object {
        /** Keys MainViewModel forwards to onPreferenceChanged. */
        val PREFERENCE_KEYS =
            setOf(
                AppsSettings.KEY_FREEZE_WHEN_LOCKED_ENABLED,
                AppsSettings.KEY_FREEZE_DONT_FREEZE_ACTIVE_APPS,
                AppsSettings.KEY_FREEZE_LOCK_DELAY_INDEX,
                AppsSettings.KEY_FREEZE_AUTO_EXCLUDED_APPS,
                AppsSettings.KEY_FREEZE_MODE,
                AppsSettings.KEY_FREEZE_SHOW_IN_LAUNCHER,
            )
    }
}
