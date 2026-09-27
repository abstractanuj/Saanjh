package com.geecee.escapelauncher

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.os.Process
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.launchApp
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class MainPagerScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: SettingsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository
) : AndroidViewModel(context as Application) {
    fun setFirstTimeHelp(value: Boolean) {
        viewModelScope.launch {
            repository.setFirstTimeHelp(value)
        }
    }

    val doubleTapToLock = repository.settings.map { it.doubleTapToLock }
    val hapticFeedBackEnabled = repository.settings.map { it.hapticFeedBackEnabled }
    val gestureNavigation = repository.settings.map { it.gestureNavigation }

    var currentSelectedApp = mutableStateOf(InstalledApp("", "", ComponentName("", ""), Process.myUserHandle()))

    var showOpenChallenge = mutableStateOf(false)

    val interactionSource = MutableInteractionSource()

    val appsListScrollState = androidx.compose.foundation.lazy.LazyListState()

    // Triggered when we want to force the drawer to collapse (e.g., app launched, home button)
    private val _drawerCollapseEvent = kotlinx.coroutines.flow.MutableSharedFlow<Unit>()
    val drawerCollapseEvent = _drawerCollapseEvent.asSharedFlow()

    suspend fun goToMainPage() {
        _drawerCollapseEvent.emit(Unit)
    }

    suspend fun animatedGoToMainPage() {
        _drawerCollapseEvent.emit(Unit)
    }

    init {
        // init is now empty or you can place other startup logic here
    }

    fun updateSelectedApp(app: InstalledApp) {
        currentSelectedApp.value = app
    }

    /**
     * Logic to handle what happens after an app is successfully launched.
     * Resets the pager to the main home screen after a short delay.
     */
    private fun onAppLaunched(app: InstalledApp) {
        updateSelectedApp(app)
        viewModelScope.launch {
            // Delay provides a smooth transition feel before resetting the launcher state
            delay(500.milliseconds)
            goToMainPage()
        }
    }

    /**
     * High-level function to open an app, handling challenge checks asynchronously.
     */
    fun openApp(
        app: InstalledApp,
        overrideChallenge: Boolean = false,
        onAppOpened: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val hasChallenge = if (overrideChallenge) false else modifiedAppsRepository.isChallenge(app.packageName)

            withContext(Dispatchers.Main.immediate) {
                if (hasChallenge) {
                    showOpenChallenge.value = true
                    updateSelectedApp(app)
                } else {
                    val success = launchApp(getApplication(), app, onAppOpened)
                    if (success) {
                        onAppLaunched(app)
                        if (overrideChallenge) {
                            showOpenChallenge.value = false
                        }
                    }
                }
            }
        }
    }
}
