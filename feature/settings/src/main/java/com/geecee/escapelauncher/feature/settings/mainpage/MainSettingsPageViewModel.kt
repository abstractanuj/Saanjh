package com.geecee.escapelauncher.feature.settings.mainpage

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.AppConfiguration
import com.geecee.escapelauncher.core.data.network.RssParser
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.BackupRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.domain.repository.TodaysPicksRepository
import com.geecee.escapelauncher.core.model.YoutubeChannel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

data class MainSettingsUiState(
    val hapticFeedBackEnabled: Boolean = true,
    val twelveHourClock: Boolean = false,
    val bigClock: Boolean = false,
    val showDate: Boolean = false,
    val homeAlignment: Int = 1,
    val homeVAlignment: Int = 1,
    val appsAlignment: Int = 1,
    val doubleTapToLock: Boolean = false,
    val showSearchBox: Boolean = true,
    val searchAutoOpen: Boolean = false,
    val bottomSearch: Boolean = true,
    val automaticallyOpenAppsInSearch: Boolean = false,
    val hideScreenTimePage: Boolean = false,
    val allowAnalytics: Boolean = false,
    val showQuickApps: Boolean = true,
    val maxQuickApps: Int = 4,
    val physicalLauncherMode: Boolean = false,
    val gestureNavigation: Boolean = false,
    val showLauncherSwitcherNotification: Boolean = false,
    val enableNowPlaying: Boolean = false,
    val customYoutubeChannels: List<YoutubeChannel> = emptyList()
)

@HiltViewModel
class MainSettingsPageViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    appsRepository: AppsRepository,
    private val repository: SettingsRepository,
    private val todaysPicksRepository: TodaysPicksRepository,
    private val backupRepository: BackupRepository,
    private val rssParser: RssParser,
    val appConfiguration: AppConfiguration
) : ViewModel() {

    private val _isAddingChannel = MutableStateFlow(false)
    val isAddingChannel: StateFlow<Boolean> = _isAddingChannel.asStateFlow()

    val uiState: StateFlow<MainSettingsUiState> = repository.settings.map { settings ->
        MainSettingsUiState(
            hapticFeedBackEnabled = settings.hapticFeedBackEnabled,
            twelveHourClock = settings.twelveHourClock,
            bigClock = settings.bigClock,
            showDate = settings.showDate,
            homeAlignment = when (settings.homeAlignment) {
                "Left" -> 0
                "Center" -> 1
                else -> 2
            },
            homeVAlignment = when (settings.homeVAlignment) {
                "Top" -> 0
                "Center" -> 1
                else -> 2
            },
            appsAlignment = when (settings.appsAlignment) {
                "Left" -> 0
                "Center" -> 1
                else -> 2
            },
            doubleTapToLock = settings.doubleTapToLock,
            showSearchBox = settings.showSearchBox,
            searchAutoOpen = settings.searchAutoOpen,
            bottomSearch = settings.bottomSearch,
            showLauncherSwitcherNotification = settings.showLauncherSwitcherNotification,
            automaticallyOpenAppsInSearch = settings.automaticallyOpenAppsInSearch,
            hideScreenTimePage = settings.hideScreenTimePage,
            allowAnalytics = settings.allowAnalytics,
            showQuickApps = settings.showQuickApps,
            maxQuickApps = settings.maxQuickApps,
            physicalLauncherMode = settings.physicalLauncherMode,
            gestureNavigation = settings.gestureNavigation,
            enableNowPlaying = settings.enableNowPlaying,
            customYoutubeChannels = settings.customYoutubeChannels
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainSettingsUiState()
    )

    fun setHapticFeedback(value: Boolean) {
        viewModelScope.launch {
            repository.setHapticFeedback(value)
        }
    }

    fun setTwelveHourClock(value: Boolean) {
        viewModelScope.launch {
            repository.setTwelveHourClock(value)
        }
    }

    fun setBigClock(value: Boolean) {
        viewModelScope.launch {
            repository.setBigClock(value)
        }
    }

    fun setShowDate(value: Boolean) {
        viewModelScope.launch {
            repository.setShowDate(value)
        }
    }

    fun setHomeAlignment(index: Int) {
        val value = when (index) {
            0 -> "Left"
            1 -> "Center"
            else -> "Right"
        }
        viewModelScope.launch {
            repository.setHomeAlignment(value)
        }
    }

    fun setHomeVAlignment(index: Int) {
        val value = when (index) {
            0 -> "Top"
            1 -> "Center"
            else -> "Bottom"
        }
        viewModelScope.launch {
            repository.setHomeVAlignment(value)
        }
    }

    fun setAppsAlignment(index: Int) {
        val value = when (index) {
            0 -> "Left"
            1 -> "Center"
            else -> "Right"
        }
        viewModelScope.launch {
            repository.setAppsAlignment(value)
        }
    }

    fun setDoubleTapToLock(value: Boolean) {
        viewModelScope.launch {
            repository.setDoubleTapToLock(value)
        }
    }

    fun setShowSearchBox(value: Boolean) {
        viewModelScope.launch {
            repository.setShowSearchBox(value)
        }
    }

    fun setSearchAutoOpen(value: Boolean) {
        viewModelScope.launch {
            repository.setSearchAutoOpen(value)
        }
    }

    fun setBottomSearch(value: Boolean) {
        viewModelScope.launch {
            repository.setBottomSearch(value)
        }
    }

    fun setAutomaticallyOpenAppsInSearch(value: Boolean) {
        viewModelScope.launch {
            repository.setAutomaticallyOpenAppsInSearch(value)
        }
    }

    fun setHideScreenTimePage(value: Boolean) {
        viewModelScope.launch {
            repository.setHideScreenTimePage(value)
        }
    }

    fun setAllowAnalytics(value: Boolean) {
        viewModelScope.launch {
            repository.setAllowAnalytics(value)
        }
    }

    fun setShowQuickApps(value: Boolean) {
        viewModelScope.launch {
            repository.setShowQuickApps(value)
        }
    }

    fun setMaxQuickApps(value: Int) {
        viewModelScope.launch {
            repository.setMaxQuickApps(value)
        }
    }

    fun setPhysicalLauncherMode(value: Boolean) {
        viewModelScope.launch {
            repository.setPhysicalLauncherMode(value)
        }
    }

    fun setGestureNavigation(enabled: Boolean) {
        viewModelScope.launch {
            repository.setGestureNavigation(enabled)
        }
    }

    fun setEnableNowPlaying(enabled: Boolean) {
        viewModelScope.launch {
            repository.setEnableNowPlaying(enabled)
        }
    }
    
    fun setShowLauncherSwitcherNotification(enabled: Boolean) {
        viewModelScope.launch {
            repository.setShowLauncherSwitcherNotification(enabled)
        }
    }

    fun setPeoplePageBackgroundUri(uri: String?) {
        viewModelScope.launch {
            repository.setPeoplePageBackgroundUri(uri)
        }
    }

    fun addCustomYoutubeChannel(id: String) {
        viewModelScope.launch {
            _isAddingChannel.value = true
            try {
                val current = uiState.value.customYoutubeChannels.toMutableList()
                if (current.none { it.id == id }) {
                    // Fetch the real name from the RSS feed
                    val url = "https://www.youtube.com/feeds/videos.xml?channel_id=$id"
                    val result = rssParser.fetchAndParseYoutubeFeed(url, "")
                    val channelName = result.getOrNull()?.firstOrNull()?.channel ?: "Channel $id"
                    
                    current.add(YoutubeChannel(id, channelName))
                    repository.setCustomYoutubeChannels(current)
                    // Immediate refresh to show new content
                    todaysPicksRepository.refreshTodaysPicks()
                }
            } finally {
                _isAddingChannel.value = false
            }
        }
    }

    fun removeCustomYoutubeChannel(channelId: String) {
        viewModelScope.launch {
            val current = uiState.value.customYoutubeChannels.toMutableList()
            if (current.removeIf { it.id == channelId }) {
                repository.setCustomYoutubeChannels(current)
                // Immediate refresh to update list
                todaysPicksRepository.refreshTodaysPicks()
            }
        }
    }

    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = backupRepository.exportData()
            onResult(json)
        }
    }

    fun importBackup(json: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            backupRepository.importData(json)
            onComplete()
        }
    }

    val installedApps = appsRepository.mainUserApps
}
