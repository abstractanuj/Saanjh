package com.geecee.escapelauncher.core.domain.repository

import com.geecee.escapelauncher.core.model.SettingsData
import com.geecee.escapelauncher.core.model.YoutubeChannel
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<SettingsData>
    val hapticFeedBackEnabled: Flow<Boolean>
    suspend fun setHapticFeedback(enabld: Boolean)
    val hidePrivateSpace: Flow<Boolean>
    suspend fun setHidePrivateSpace(enabled: Boolean)
    val twelveHourClock: Flow<Boolean>
    suspend fun setTwelveHourClock(enabled: Boolean)
    val bigClock: Flow<Boolean>
    suspend fun setBigClock(enabled: Boolean)
    val showDate: Flow<Boolean>
    suspend fun setShowDate(enabled: Boolean)
    val firstTimeHelp: Flow<Boolean>
    suspend fun setFirstTimeHelp(enabled: Boolean)
    val homeVAlignment: Flow<String>
    suspend fun setHomeVAlignment(alignment: String)
    val homeAlignment: Flow<String>
    suspend fun setHomeAlignment(alignment: String)
    val widgetOffset: Flow<Float>
    suspend fun setWidgetOffset(offset: Float)
    val widgetHeight: Flow<Float>
    suspend fun setWidgetHeight(height: Float)
    val widgetWidth: Flow<Float>
    suspend fun setWidgetWidth(width: Float)
    val showQuickApps: Flow<Boolean>
    suspend fun setShowQuickApps(enabled: Boolean)
    val maxQuickApps: Flow<Int>
    suspend fun setMaxQuickApps(value: Int)
    val appsAlignment: Flow<String>
    suspend fun setAppsAlignment(alignment: String)
    val theme: Flow<Int>
    suspend fun setTheme(theme: Int)
    val dtheme: Flow<Int>
    suspend fun setDTheme(theme: Int)
    val ltheme: Flow<Int>
    suspend fun setLTheme(theme: Int)
    val syncTheme: Flow<Boolean>
    suspend fun setSyncTheme(value: Boolean)
    val font: Flow<String>
    suspend fun setFont(value: String)
    val allowAnalyitics: Flow<Boolean>
    suspend fun setAllowAnalytics(value: Boolean)
    val doubleTapToLock: Flow<Boolean>
    suspend fun setDoubleTapToLock(enabled: Boolean)
    val iconPackPackage: Flow<String?>
    suspend fun setIconPackPackage(pkg: String?)
    val showLauncherSwitcherNotification: Flow<Boolean>
    suspend fun setShowLauncherSwitcherNotification(enabled: Boolean)
    val showSearchBox: Flow<Boolean>
    suspend fun setShowSearchBox(enabled: Boolean)
    val searchAutoOpen: Flow<Boolean>
    suspend fun setSearchAutoOpen(enabled: Boolean)
    val bottomSearch: Flow<Boolean>
    suspend fun setBottomSearch(enabled: Boolean)
    val automaticallyOpenAppsInSearch: Flow<Boolean>
    suspend fun setAutomaticallyOpenAppsInSearch(enabled: Boolean)
    val hideScreenTimePage: Flow<Boolean>
    suspend fun setHideScreenTimePage(enabled: Boolean)
    val showHiddenAppsInSearch: Flow<Boolean>
    suspend fun setShowHiddenAppsInSearch(enabled: Boolean)
    val firstTime: Flow<Boolean>
    suspend fun setFirstTime(enabled: Boolean)
    val widgetId: Flow<Int>
    suspend fun setWidgetId(value: Int)
    val isOnDefaultLauncherOnboarding: Flow<Boolean>
    suspend fun setOnDefaultLauncherOnboarding(value: Boolean)

    val physicalLauncherMode: Flow<Boolean>
    suspend fun setPhysicalLauncherMode(enabled: Boolean)

    val gestureNavigation: Flow<Boolean>
    suspend fun setGestureNavigation(enabled: Boolean)
    val enableNowPlaying: Flow<Boolean>
    suspend fun setEnableNowPlaying(enabled: Boolean)
    val peoplePageBackgroundUri: Flow<String?>
    suspend fun setPeoplePageBackgroundUri(uri: String?)
    val customYoutubeChannels: Flow<List<YoutubeChannel>>
    suspend fun setCustomYoutubeChannels(channels: List<YoutubeChannel>)

    val routineAppsMorning: Flow<Set<String>>
    suspend fun setRoutineAppsMorning(apps: Set<String>)

    val routineAppsAfternoon: Flow<Set<String>>
    suspend fun setRoutineAppsAfternoon(apps: Set<String>)

    val routineAppsEvening: Flow<Set<String>>
    suspend fun setRoutineAppsEvening(apps: Set<String>)

    val routineAppsNight: Flow<Set<String>>
    suspend fun setRoutineAppsNight(apps: Set<String>)

    suspend fun updateSettings(settings: SettingsData)
}
