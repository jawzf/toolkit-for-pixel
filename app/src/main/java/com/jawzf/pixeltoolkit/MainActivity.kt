package com.jawzf.pixeltoolkit

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.jawzf.pixeltoolkit.core.data.apps.ui.SingleAppSelectionSheet
import com.jawzf.pixeltoolkit.core.design.R
import com.jawzf.pixeltoolkit.core.design.haptics.HapticUtil
import com.jawzf.pixeltoolkit.core.design.modifiers.BlurDirection
import com.jawzf.pixeltoolkit.core.design.modifiers.progressiveBlur
import com.jawzf.pixeltoolkit.core.design.sheets.InstructionsBottomSheet
import com.jawzf.pixeltoolkit.core.design.state.LocalMenuStateManager
import com.jawzf.pixeltoolkit.core.design.state.MenuStateManager
import com.jawzf.pixeltoolkit.core.design.theme.ToolkitTheme
import com.jawzf.pixeltoolkit.domain.registry.initPermissionRegistry
import com.jawzf.pixeltoolkit.feature.apps.updates.AppUpdatesViewModel
import com.jawzf.pixeltoolkit.feature.apps.updates.UpdateInfo
import com.jawzf.pixeltoolkit.feature.apps.updates.github.GitHubAuthViewModel
import com.jawzf.pixeltoolkit.feature.apps.updates.ui.AddRepoBottomSheet
import com.jawzf.pixeltoolkit.feature.apps.updates.ui.GitHubAuthSheet
import com.jawzf.pixeltoolkit.feature.apps.updates.ui.UpdateBottomSheet
import com.jawzf.pixeltoolkit.ui.composables.SetupFeatures
import com.jawzf.pixeltoolkit.ui.composables.WelcomeScreen
import com.jawzf.pixeltoolkit.ui.core.sheets.ShakeForHelp
import com.jawzf.pixeltoolkit.utils.AutoUpdateManagerHelper
import com.jawzf.pixeltoolkit.utils.ShortcutUtil
import com.jawzf.pixeltoolkit.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {
    val viewModel: MainViewModel by viewModels()
    val updatesViewModel: AppUpdatesViewModel by viewModels()
    val gitHubAuthViewModel: GitHubAuthViewModel by viewModels()
    private var isAppReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install and configure the splash screen
        val splashScreen = installSplashScreen()

        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ShortcutUtil
            .updateLauncherDynamicShortcuts(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        // Keep splash screen visible while app is loading
        splashScreen.setKeepOnScreenCondition { !isAppReady }

        // Customize the exit animation - scale up and fade out
        // Safe implementation for OEM devices that may not provide iconView
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            try {
                val splashScreenView = splashScreenViewProvider.view
                val splashIcon =
                    try {
                        splashScreenViewProvider.iconView
                    } catch (e: Exception) {
                        null
                    }

                // Animate the splash screen view fade out
                val fadeOut =
                    ObjectAnimator.ofFloat(splashScreenView, "alpha", 1f, 0f).apply {
                        interpolator = AnticipateInterpolator()
                        duration = 750
                    }
                fadeOut.doOnEnd {
                    splashScreenViewProvider.remove()
                    // Re-apply edge to edge AFTER the splash screen view is removed
                    // to ensure it's not overridden by splash screen cleanup
                    enableEdgeToEdge()
                }

                // Safely animate the icon if it exists
                // Known issue: Some OEM devices (Samsung One UI 8, Xiaomi on Android 16)
                // may not provide iconView, causing NullPointerException
                try {
                    @Suppress("SENSELESS_COMPARISON")
                    if (splashIcon != null) {
                        // Scale down animation
                        val scaleUp =
                            ObjectAnimator.ofFloat(splashIcon, "scaleX", 1f, 0.5f).apply {
                                interpolator = AnticipateInterpolator()
                                duration = 750
                            }

                        val scaleUpY =
                            ObjectAnimator.ofFloat(splashIcon, "scaleY", 1f, 0.5f).apply {
                                interpolator = AnticipateInterpolator()
                                duration = 750
                            }

                        // rotate
                        val rotate360 =
                            ObjectAnimator.ofFloat(splashIcon, "rotation", 0f, -90f).apply {
                                interpolator = AnticipateInterpolator()
                                duration = 750
                            }

                        scaleUp.start()
                        scaleUpY.start()
                        rotate360.start()
                    } else {
                        Log.w("SplashScreen", "iconView is null - OEM device detected")
                    }
                } catch (e: NullPointerException) {
                    // Handle the edge case where iconView becomes null between check and animation
                    Log.w(
                        "SplashScreen",
                        "NullPointerException on iconView animation - likely OEM device",
                        e,
                    )
                }

                // Animate the branding icon if it exists
                val brandingViewId =
                    resources.getIdentifier("splashscreen_branding_view", "id", "android")
                val brandingView =
                    if (brandingViewId != 0) {
                        splashScreenView.findViewById<android.view.View>(brandingViewId)
                    } else {
                        null
                    }

                if (brandingView != null) {
                    ObjectAnimator
                        .ofFloat(
                            brandingView,
                            "translationY",
                            0f,
                            -brandingView.height.toFloat(),
                        ).apply {
                            interpolator = AnticipateInterpolator()
                            duration = 750
                            start()
                        }
                }

                fadeOut.start()
            } catch (e: Exception) {
                // Fallback for any unexpected exceptions during animation
                Log.e("SplashScreen", "Exception during splash screen animation", e)
                try {
                    splashScreenViewProvider.remove()
                } catch (e2: Exception) {
                    Log.e("SplashScreen", "Exception during splash screen removal", e2)
                }
            }
        }

        Log.d("MainActivity", "onCreate with action: ${intent?.action}")

        // Initialize HapticUtil with saved preferences
        HapticUtil.initialize(this)
        // initialize permission registry
        initPermissionRegistry()
        // Initialize viewModel state early for correct initial composition
        viewModel.check(this)
        setContent {
            val isPitchBlackThemeEnabled by viewModel.isPitchBlackThemeEnabled
            val isBlurEnabled by viewModel.isBlurEnabled
            ToolkitTheme(pitchBlackTheme = isPitchBlackThemeEnabled) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalMenuStateManager provides
                        remember {
                            MenuStateManager()
                        },
                ) {
                    val context = LocalContext.current
                    val view = LocalView.current
                    try {
                        context.packageManager.getPackageInfo(context.packageName, 0).versionName
                    } catch (_: Exception) {
                        stringResource(R.string.label_unknown)
                    }

                    var showInstructionsSheet by remember { mutableStateOf(false) }

                    var showGitHubAuthSheet by remember { mutableStateOf(false) }
                    val gitHubToken by viewModel.gitHubToken
                    val gitHubUser by gitHubAuthViewModel.currentUser
                    val isOnboardingCompleted by viewModel.isOnboardingCompleted

                    LaunchedEffect(Unit) {
                        gitHubAuthViewModel.loadCachedUser(context)
                    }

                    LaunchedEffect(gitHubToken) {
                        if (gitHubToken != null && gitHubUser == null) {
                            gitHubAuthViewModel.loadUser(gitHubToken!!, context)
                        }
                    }

                    LaunchedEffect(Unit) {
                        viewModel.check(context)
                        // Request notification permission if not granted (Android 13+)
                        if (!viewModel.isPostNotificationsEnabled.value) {
                            viewModel.requestNotificationPermission(this@MainActivity)
                        }
                        updatesViewModel.loadTrackedRepos(context)
                    }

                    if (showInstructionsSheet) {
                        InstructionsBottomSheet(
                            onDismissRequest = { showInstructionsSheet = false },
                        )
                    }

                    ShakeForHelp(viewModel = viewModel)

                    if (showGitHubAuthSheet) {
                        GitHubAuthSheet(
                            viewModel = gitHubAuthViewModel,
                            onDismissRequest = { showGitHubAuthSheet = false },
                        )
                    }

                    val updateProgress by updatesViewModel.updateProgress

                    var showAddRepoSheet by remember { mutableStateOf(false) }
                    var repoToShowReleaseNotesFullName by remember { mutableStateOf<String?>(null) }
                    val trackedRepos by updatesViewModel.trackedRepos

                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.CreateDocument("application/json"),
                    ) { uri ->
                        uri?.let {
                            contentResolver.openOutputStream(it)?.use { outputStream ->
                                updatesViewModel.exportTrackedRepos(context, outputStream)
                                Toast
                                    .makeText(
                                        context,
                                        getString(R.string.msg_export_success),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        }
                    }

                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocument(),
                    ) { uri ->
                        uri?.let {
                            contentResolver.openInputStream(it)?.use { inputStream ->
                                if (updatesViewModel.importTrackedRepos(context, inputStream)) {
                                    updatesViewModel.loadTrackedRepos(context)
                                    Toast
                                        .makeText(
                                            context,
                                            getString(R.string.msg_import_success),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            getString(R.string.msg_import_failed),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            }
                        }
                    }

                    if (showAddRepoSheet) {
                        AddRepoBottomSheet(
                            viewModel = updatesViewModel,
                            onDismissRequest = {
                                showAddRepoSheet = false
                                updatesViewModel.clearSearch()
                            },
                            onTrackClick = {
                                showAddRepoSheet = false
                                updatesViewModel.clearSearch()
                            },
                            appPicker = { onDismiss, onAppSelected ->
                                SingleAppSelectionSheet(
                                    onDismissRequest = onDismiss,
                                    onAppSelected = onAppSelected,
                                )
                            },
                            onDownloadAndInstall = { apkUrl, apkName, onProgressUpdate ->
                                AutoUpdateManagerHelper(context).downloadAndInstallApk(apkUrl, apkName, onProgressUpdate)
                            },
                        )
                    }

                    if (repoToShowReleaseNotesFullName != null) {
                        val repo =
                            trackedRepos.find { it.fullName == repoToShowReleaseNotesFullName }
                        if (repo != null) {
                            val isNotesLoading = repo.latestReleaseBody.isNullOrBlank()
                            UpdateBottomSheet(
                                updateInfo =
                                    UpdateInfo(
                                        versionName = repo.latestTagName,
                                        releaseNotes = repo.latestReleaseBody ?: "",
                                        downloadUrl = repo.downloadUrl ?: "",
                                        releaseUrl = repo.latestReleaseUrl ?: "",
                                        isUpdateAvailable = repo.isUpdateAvailable,
                                    ),
                                isChecking = isNotesLoading,
                                onDismissRequest = { repoToShowReleaseNotesFullName = null },
                                onDownloadAndInstall = { apkUrl, apkName, onProgressUpdate ->
                                    AutoUpdateManagerHelper(context).downloadAndInstallApk(apkUrl, apkName, onProgressUpdate)
                                },
                            )
                        } else {
                            repoToShowReleaseNotesFullName = null
                        }
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            contentWindowInsets =
                                WindowInsets(
                                    0,
                                    0,
                                    0,
                                    0,
                                ),
                            containerColor = MaterialTheme.colorScheme.surfaceContainer,
                            topBar = {},
                        ) { innerPadding ->
                            val statusBarHeightPx =
                                with(androidx.compose.ui.platform.LocalDensity.current) {
                                    WindowInsets.statusBars
                                        .asPaddingValues()
                                        .calculateTopPadding()
                                        .toPx()
                                }

                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .progressiveBlur(
                                            blurRadius = if (isBlurEnabled) 40f else 0f,
                                            height = statusBarHeightPx * 1.15f,
                                            direction = BlurDirection.TOP,
                                        ),
                            ) {
                                SetupFeatures(
                                    viewModel = viewModel,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding =
                                        PaddingValues(
                                            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                                            bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp,
                                            start = 16.dp,
                                            end = 16.dp,
                                        ),
                                    onHelpClick = { showInstructionsSheet = true },
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = !isOnboardingCompleted,
                            enter = fadeIn() + slideInVertically { it },
                            exit = fadeOut() + slideOutVertically { it },
                        ) {
                            WelcomeScreen(
                                viewModel = viewModel,
                                onBeginClick = {
                                    viewModel.setOnboardingCompleted(true, context)
                                },
                            )
                        }
                    }
                    // Mark app as ready after a short delay to ensure first frame is painted
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(100)
                        isAppReady = true
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.check(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.d("MainActivity", "onNewIntent with action: ${intent.action}")
    }
}
