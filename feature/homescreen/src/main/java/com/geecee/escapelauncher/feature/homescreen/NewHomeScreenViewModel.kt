package com.geecee.escapelauncher.feature.homescreen

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.model.AppAction
import com.geecee.escapelauncher.core.common.getAppShortcuts
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.common.startShortcut
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.feature.newwidgets.WidgetHostManager
import com.geecee.escapelauncher.core.ui.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map

@HiltViewModel
class NewHomeScreenViewModel @Inject constructor(
    @ApplicationContext context: Context,
    settingsRepository: SettingsRepository,
    appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
    val widgetHostManager: WidgetHostManager,
    appConfiguration: AppConfiguration
) : ViewModel() {
    val isFoss = appConfiguration.isFoss

    // region Navigation & Events
    private val _uiEvent = MutableSharedFlow<HomeUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun navigateToSettings() {
        viewModelScope.launch {
            _uiEvent.emit(HomeUiEvent.NavigateSettings)
        }
    }
    // endregion

    // region Reactive Settings
    val twelveHourClock = settingsRepository.twelveHourClock
    val bigClock = settingsRepository.bigClock
    val showDate = settingsRepository.showDate
    val firstTimeHelp = settingsRepository.firstTimeHelp
    val hapticFeedBackEnabled = settingsRepository.hapticFeedBackEnabled
    val showQuickApps = settingsRepository.showQuickApps
    val maxQuickApps = settingsRepository.maxQuickApps

    val homeAlignment = settingsRepository.homeAlignment.map { alignment ->
        when (alignment) {
            "Left" -> Alignment.Start
            "Center" -> Alignment.CenterHorizontally
            else -> Alignment.End
        }
    }

    val homeVAlignment = settingsRepository.homeVAlignment.map { alignment ->
        when (alignment) {
            "Top" -> Arrangement.Top
            "Center" -> Arrangement.Center
            else -> Arrangement.Bottom
        }
    }

    val widgetOffset = settingsRepository.widgetOffset
    val widgetHeight = settingsRepository.widgetHeight
    val widgetWidth = settingsRepository.widgetWidth
    val widgetId = settingsRepository.widgetId
    // endregion

    // Favorite Apps
    val favoriteApps: StateFlow<List<InstalledApp>> = combine(
        appsRepository.mainUserApps,
        modifiedAppsRepository.getFavouriteAppsInOrderFlow(),
    ) { apps, entities ->
        entities.mapNotNull { entity ->
            apps.find { it.packageName == entity.packageId }
        }
    }.distinctUntilChanged()
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    // All Apps
    val allApps: StateFlow<List<InstalledApp>> = appsRepository.mainUserApps
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    // Bottom Sheet State
    private val _showBottomSheet = MutableStateFlow(value = false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()
    fun setBottomSheetVisible(visibility: Boolean) {
        _showBottomSheet.value = visibility
    }

    private val _bottomSheetApp = MutableStateFlow<InstalledApp?>(null)
    val bottomSheetApp: StateFlow<InstalledApp?> = _bottomSheetApp.asStateFlow()
    fun setBottomSheetApp(app: InstalledApp?) {
        _bottomSheetApp.value = app
    }

    // region App State Monitoring
    val isBottomSheetAppFavourite: StateFlow<Boolean> = combine(
        _bottomSheetApp,
        modifiedAppsRepository.getFavouriteAppsInOrderFlow(),
    ) { app, favourites ->
        app?.let { selectedApp ->
            favourites.any { it.packageId == selectedApp.packageName }
        } ?: false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )

    val doesBottomSheetAppHaveChallenge: StateFlow<Boolean> = combine(
        _bottomSheetApp,
        modifiedAppsRepository.getChallengePackageIdsFlow(),
    ) { app, challenges ->
        app?.let { selectedApp ->
            challenges.any { it == selectedApp.packageName }
        } ?: false
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false,
    )
    // endregion

    // region Action Generators
    val bottomSheetActions: StateFlow<List<AppAction>> = combine(
        _bottomSheetApp,
        isBottomSheetAppFavourite,
        doesBottomSheetAppHaveChallenge,
    ) { app, isFavourite, hasChallenge ->
        if (app == null) return@combine emptyList()
        
        listOf(
            AppAction(
                labelRes = R.string.uninstall,
            ) { clickedApp ->
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.UninstallApp(clickedApp))
                }
            },
            AppAction(
                labelRes = if (isFavourite) R.string.rem_from_fav else R.string.add_to_fav,
                isVisible = { it.isMainUserApp() },
            ) { clickedApp ->
                viewModelScope.launch {
                    if (isFavourite) {
                        modifiedAppsRepository.removeFavourite(clickedApp.packageName)
                    } else {
                        modifiedAppsRepository.addFavourite(clickedApp.packageName)
                        _uiEvent.emit(HomeUiEvent.NavigateHome)
                    }
                    _showBottomSheet.value = false
                }
            },
            AppAction(
                labelRes = R.string.hide,
                isVisible = { it.isMainUserApp() },
            ) { clickedApp ->
                viewModelScope.launch {
                    modifiedAppsRepository.setHidden(clickedApp.packageName, isHidden = true)
                    _showBottomSheet.value = false
                }
            },
            AppAction(
                labelRes = R.string.app_info,
            ) { clickedApp ->
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.ShowAppInfo(clickedApp))
                }
            },
            AppAction(
                labelRes = R.string.add_open_challenge,
                isVisible = { it.isMainUserApp() && !hasChallenge },
            ) { clickedApp ->
                viewModelScope.launch {
                    modifiedAppsRepository.setChallenge(clickedApp.packageName, isChallenge = true)
                    _showBottomSheet.value = false
                }
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    val shortcutActions: StateFlow<List<AppAction>> = _bottomSheetApp.map { app ->
        if ((app == null) || !(app.isMainUserApp())) return@map emptyList()

        getAppShortcuts(context, app.packageName).map { shortcut ->
            AppAction(
                label = shortcut.label,
            ) { clickedApp ->
                startShortcut(context, clickedApp.packageName, shortcut.id)
                _showBottomSheet.value = false
                viewModelScope.launch {
                    _uiEvent.emit(HomeUiEvent.NavigateHome)
                }
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )
    // endregion
}

sealed class HomeUiEvent {
    data object NavigateHome : HomeUiEvent()
    data object NavigateSettings : HomeUiEvent()
    data class UninstallApp(val app: InstalledApp) : HomeUiEvent()
    data class ShowAppInfo(val app: InstalledApp) : HomeUiEvent()
}
