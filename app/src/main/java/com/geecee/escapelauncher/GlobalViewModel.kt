package com.geecee.escapelauncher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class GlobalViewModel @Inject constructor(
    val modifiedAppsRepository: ModifiedAppsRepository,
    val appsRepository: AppsRepository,
    settingsRepository: SettingsRepository
) : ViewModel() {
    val allowAnalytics = settingsRepository.allowAnalyitics
    val firstTime = settingsRepository.firstTime
    val showLauncherSwitcherNotification = settingsRepository.showLauncherSwitcherNotification

    private val _navigateHomeEvent = MutableSharedFlow<Unit>(
        replay = 0, onBufferOverflow = BufferOverflow.DROP_OLDEST, extraBufferCapacity = 1
    )
    val navigateHomeEvent = _navigateHomeEvent.asSharedFlow()

    fun requestToGoHome() {
        viewModelScope.launch {
            _navigateHomeEvent.emit(Unit)
        }
    }

    // region Readiness State
    /**
     * Single source of truth for app readiness.
     * Combines all essential data loading states.
     */
    val isReady = combine(
        appsRepository.installedApps.map { it.isNotEmpty() },
        modifiedAppsRepository.getFavouriteAppsInOrderFlow().map { true }, // Favorites can be empty, so just knowing the flow emitted is enough
        firstTime.map { true }
    ) { appsReady, favsReady, settingsReady ->
        appsReady && favsReady && settingsReady
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )
    // endregion
}
