package com.geecee.escapelauncher

import android.Manifest
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.geecee.escapelauncher.core.analytics.analyticsProxy
import com.geecee.escapelauncher.core.cloudmessaging.messagingInitializer
import com.geecee.escapelauncher.core.common.AppLogger
import com.geecee.escapelauncher.core.common.configureNavBar
import com.geecee.escapelauncher.core.common.configureStatusBar
import com.geecee.escapelauncher.core.common.goToAppInfo
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.EscapeTheme
import com.geecee.escapelauncher.core.theme.enterTransition
import com.geecee.escapelauncher.core.theme.exitTransition
import com.geecee.escapelauncher.core.theme.popEnterTransition
import com.geecee.escapelauncher.core.theme.popExitTransition
import com.geecee.escapelauncher.core.ui.recievers.ScreenOffReceiver
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import com.geecee.escapelauncher.feature.onboarding.Onboarding
import com.geecee.escapelauncher.feature.settings.Settings
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

/**
 * Type-safe navigation keys for the top-level app destinations.
 */
sealed interface AppNavKey : NavKey {
    @Serializable
    data object Home : AppNavKey

    @Serializable
    data object Settings : AppNavKey

    @Serializable
    data object Onboarding : AppNavKey
}

@AndroidEntryPoint
class MainHomeScreenActivity : ComponentActivity() {
    private val globalViewModel: GlobalViewModel by viewModels()
    private val mainPagerViewModel: MainPagerScreenViewModel by viewModels()

    @Inject
    lateinit var widgetHostManager: WidgetHostManager
    private lateinit var screenOffReceiver: ScreenOffReceiver
    
    private var startDestination by mutableStateOf<AppNavKey?>(null)

    override fun onStart() {
        super.onStart()


        // Start listening for widgets
        try {
            widgetHostManager.startListening()
        } catch (e: Exception) {
            AppLogger.e("Widgets: Error starting AppWidgetHost in onStart", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Setup Splashscreen
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Startup Guard
        lifecycleScope.launch {
            delay(5000.milliseconds)
            if (startDestination == null) {
                AppLogger.w("StartupGuard: UI not ready after 5 seconds! Forced destination determination.")
                // Attempt a last-ditch effort if somehow the flow didn't emit
                val isFirstTime = globalViewModel.firstTime.first()
                startDestination = if (isFirstTime) AppNavKey.Onboarding else AppNavKey.Home
            }
        }

        // Setup analytics
        lifecycleScope.launch {

        }

        // Sort out the window
        enableEdgeToEdge()
        window.configureStatusBar(hide = false)
        window.configureNavBar(hide = false)

        // Determine start destination before hiding splash screen
        lifecycleScope.launch {
            val isFirstTime = globalViewModel.firstTime.first()
            startDestination = if (isFirstTime) AppNavKey.Onboarding else AppNavKey.Home
        }

        // Keep splash screen visible until we know where to go and essential data is loaded
        val startTime = System.currentTimeMillis()
        splashScreen.setKeepOnScreenCondition {
            val elapsed = System.currentTimeMillis() - startTime
            val isTimedOut = elapsed > 3000
            
            if (isTimedOut) {
                AppLogger.w("Startup: Splash screen timeout reached. Forcing UI render.", tag = "Startup")
                return@setKeepOnScreenCondition false
            }

            val isReady = globalViewModel.isReady.value
            val isDestSet = startDestination != null
            
            !(isReady && isDestSet)
        }

        // Register screen off receiver
        screenOffReceiver = ScreenOffReceiver {
            // Screen turned off
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)

        // Manage Launcher Switcher Notification
        lifecycleScope.launch {
            globalViewModel.showLauncherSwitcherNotification.collect { showNotification ->
                if (showNotification) {
                    LauncherNotificationManager.showLauncherSwitcherNotification(this@MainHomeScreenActivity)
                } else {
                    LauncherNotificationManager.hideLauncherSwitcherNotification(this@MainHomeScreenActivity)
                }
            }
        }

        // Subscribe to notifications via flavour-specific initializer
        messagingInitializer.initialize(this)

        // Set up the application content
        setContent {
            val destination = startDestination
            if (destination != null) {
                EscapeTheme {
                    SetupNavDisplay(destination)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Responsible for taking you to the favourites page when you press the home button
        if (intent.action == Intent.ACTION_MAIN && intent.hasCategory(Intent.CATEGORY_HOME)) {
            // Only navigate home if we've finished onboarding
            lifecycleScope.launch {
                val isFirstTime = globalViewModel.firstTime.first()
                if (!isFirstTime) {
                    globalViewModel.requestToGoHome()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        return super.onKeyUp(keyCode, event)
    }

    override fun onStop() {
        super.onStop()


        // Stop listening for widgets
        try {
            widgetHostManager.stopListening()
        } catch (e: Exception) {
            AppLogger.e("Widgets: Error stopping AppWidgetHost in onStop", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop the screen off receivers
        if (::screenOffReceiver.isInitialized) {
            unregisterReceiver(screenOffReceiver)
        }
    }


    /**
     * Sets up main navigation display for the app
     *
     * @param startDestination Where to start
     */
    @Composable
    private fun SetupNavDisplay(startDestination: AppNavKey) {
        val backStack = rememberNavBackStack(startDestination)

        // Responsible for going home when navigateHomeEvent happens
        LaunchedEffect(globalViewModel.navigateHomeEvent) {
            globalViewModel.navigateHomeEvent.collect {
                // Clear the back stack and set Home as the only entry
                if (backStack.lastOrNull() !is AppNavKey.Home) {
                    backStack.removeAll { it !is AppNavKey.Home }
                    if (backStack.isEmpty()) {
                        backStack.add(AppNavKey.Home)
                    }
                }
                launch {
                    mainPagerViewModel.animatedGoToMainPage() // This already goes to index 0/1 in some logic, let's verify
                }
                launch {
                    delay(300.milliseconds)
                    mainPagerViewModel.appsListScrollState.scrollToItem(0)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = BackgroundColor)
                .animateContentSize()
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = { 
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider {
                    entry<AppNavKey.Home> {
                        MainPagerScreen(
                            viewModel = mainPagerViewModel,
                            onOpenSettings = { backStack.add(AppNavKey.Settings) },
                        )
                    }
                    entry<AppNavKey.Settings> {
                        Settings(
                            goBack = {
                                backStack.removeLastOrNull()
                            },
                            activity = this@MainHomeScreenActivity,
                        )
                    }
                    entry<AppNavKey.Onboarding> {
                        Onboarding(
                            onFinished = {
                                backStack.clear()
                                backStack.add(AppNavKey.Home)
                            }
                        )
                    }
                },
                transitionSpec = {
                    enterTransition() togetherWith exitTransition()
                },
                popTransitionSpec = {
                    popEnterTransition() togetherWith popExitTransition()
                },
                predictivePopTransitionSpec = {
                    popEnterTransition() togetherWith popExitTransition()
                }
            )
        }
    }
}