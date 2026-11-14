package com.geecee.escapelauncher.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.geecee.escapelauncher.core.data.datastore.PreferencesKeys
import com.geecee.escapelauncher.core.model.SettingsData
import com.geecee.escapelauncher.core.model.YoutubeChannel
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import com.geecee.escapelauncher.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson,
    @ApplicationScope private val scope: CoroutineScope
) : SettingsRepository {

    private val youtubeChannelListType = object : TypeToken<List<YoutubeChannel>>() {}.type

    override val settings: Flow<SettingsData> = dataStore.data.map { preferences ->
        SettingsData(
            hapticFeedBackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true,
            hidePrivateSpace = preferences[PreferencesKeys.HIDE_PRIVATE_SPACE] ?: true,
            twelveHourClock = preferences[PreferencesKeys.TWELVE_HOUR_CLOCK] ?: false,
            bigClock = preferences[PreferencesKeys.BIG_CLOCK] ?: false,
            showDate = preferences[PreferencesKeys.SHOW_DATE] ?: false,
            showGlassmorphicReminders = preferences[PreferencesKeys.SHOW_GLASSMORPHIC_REMINDERS] ?: true,
            reminderFrequencyHours = preferences[PreferencesKeys.REMINDER_FREQUENCY_HOURS] ?: 1,
            reminderStartTime = preferences[PreferencesKeys.REMINDER_START_TIME] ?: "08:00",
            reminderEndTime = preferences[PreferencesKeys.REMINDER_END_TIME] ?: "22:00",
            homeAlignment = preferences[PreferencesKeys.HOME_ALIGNMENT] ?: "Center",
            homeVAlignment = preferences[PreferencesKeys.HOME_V_ALIGNMENT] ?: "Center",
            appsAlignment = preferences[PreferencesKeys.APPS_ALIGNMENT] ?: "Center",
            doubleTapToLock = preferences[PreferencesKeys.DOUBLE_TAP_TO_LOCK] ?: false,
            showSearchBox = preferences[PreferencesKeys.SHOW_SEARCH_BOX] ?: true,
            searchAutoOpen = preferences[PreferencesKeys.SEARCH_AUTO_OPEN] ?: false,
            bottomSearch = preferences[PreferencesKeys.BOTTOM_SEARCH] ?: true,
            automaticallyOpenAppsInSearch = preferences[PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH] ?: false,
            hideScreenTimePage = preferences[PreferencesKeys.HIDE_SCREEN_TIME_PAGE] ?: false,
            allowAnalytics = preferences[PreferencesKeys.ALLOW_ANALYTICS] ?: false,
            theme = preferences[PreferencesKeys.THEME] ?: 1,
            ltheme = preferences[PreferencesKeys.LTHEME] ?: 1,
            dtheme = preferences[PreferencesKeys.DTHEME] ?: 0,
            syncTheme = preferences[PreferencesKeys.SYNC_THEME] ?: false,
            font = preferences[PreferencesKeys.FONT] ?: "Jost",
            firstTimeHelp = preferences[PreferencesKeys.FIRST_TIME_HELP] ?: true,
            widgetOffset = preferences[PreferencesKeys.WIDGET_OFFSET] ?: 0f,
            widgetHeight = preferences[PreferencesKeys.WIDGET_HEIGHT] ?: 125f,
            widgetWidth = preferences[PreferencesKeys.WIDGET_WIDTH] ?: 250f,
            widgetId = preferences[PreferencesKeys.WIDGET_ID] ?: -1,
            showQuickApps = preferences[PreferencesKeys.SHOW_QUICK_APPS] ?: true,
            maxQuickApps = preferences[PreferencesKeys.MAX_QUICK_APPS] ?: 4,
            showHiddenAppsInSearch = preferences[PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH] ?: false,
            firstTime = preferences[PreferencesKeys.FIRST_TIME] ?: true,
            isOnDefaultLauncherOnboarding = preferences[PreferencesKeys.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE] ?: false,
            physicalLauncherMode = preferences[PreferencesKeys.PHYSICAL_LAUNCHER_MODE] ?: false,
            gestureNavigation = preferences[PreferencesKeys.GESTURE_NAVIGATION] ?: false,
            showLauncherSwitcherNotification = preferences[PreferencesKeys.SHOW_LAUNCHER_SWITCHER_NOTIFICATION] ?: false,
            enableNowPlaying = preferences[PreferencesKeys.ENABLE_NOW_PLAYING] ?: false,
            peoplePageBackgroundUri = preferences[PreferencesKeys.PEOPLE_PAGE_BACKGROUND_URI],
            customYoutubeChannels = try {
                val json = preferences[PreferencesKeys.CUSTOM_YOUTUBE_CHANNELS]
                if (!json.isNullOrEmpty()) {
                    gson.fromJson<List<YoutubeChannel>>(json, youtubeChannelListType)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
            ,
            routineAppsMorning = preferences[PreferencesKeys.ROUTINE_APPS_MORNING] ?: emptySet(),
            routineAppsAfternoon = preferences[PreferencesKeys.ROUTINE_APPS_AFTERNOON] ?: emptySet(),
            routineAppsEvening = preferences[PreferencesKeys.ROUTINE_APPS_EVENING] ?: emptySet(),
            routineAppsNight = preferences[PreferencesKeys.ROUTINE_APPS_NIGHT] ?: emptySet(),
            folderGridSize = preferences[PreferencesKeys.FOLDER_GRID_SIZE] ?: 3,
            folderBackgroundOpacity = preferences[PreferencesKeys.FOLDER_BACKGROUND_OPACITY] ?: 0.8f,
            enableHistoryRecommendations = preferences[PreferencesKeys.ENABLE_HISTORY_RECOMMENDATIONS] ?: true,
            enableEconomicsRecommendations = preferences[PreferencesKeys.ENABLE_ECONOMICS_RECOMMENDATIONS] ?: true,
            enablePsychologyRecommendations = preferences[PreferencesKeys.ENABLE_PSYCHOLOGY_RECOMMENDATIONS] ?: true,
            enableSociologyRecommendations = preferences[PreferencesKeys.ENABLE_SOCIOLOGY_RECOMMENDATIONS] ?: true,
            enableWhatIfRecommendations = preferences[PreferencesKeys.ENABLE_WHAT_IF_RECOMMENDATIONS] ?: true,
            enableForeignMoviesRecommendations = preferences[PreferencesKeys.ENABLE_FOREIGN_MOVIES_RECOMMENDATIONS] ?: true,
            recommendationFrequencyHours = preferences[PreferencesKeys.RECOMMENDATION_FREQUENCY_HOURS] ?: 24,
            recommendationTime = preferences[PreferencesKeys.RECOMMENDATION_TIME] ?: "10:00",
            enableSystemWallpaper = preferences[PreferencesKeys.ENABLE_SYSTEM_WALLPAPER] ?: false,
            iconPackPackage = preferences[PreferencesKeys.ICON_PACK_PACKAGE]
        )
    }.shareIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    override val hapticFeedBackEnabled: Flow<Boolean>
        get() = settings.map { it.hapticFeedBackEnabled }.distinctUntilChanged()

    override suspend fun setHapticFeedback(enabld: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabld }
    }

    override val hidePrivateSpace: Flow<Boolean>
        get() = settings.map { it.hidePrivateSpace }.distinctUntilChanged()

    override suspend fun setHidePrivateSpace(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HIDE_PRIVATE_SPACE] = enabled }
    }

    override val twelveHourClock: Flow<Boolean>
        get() = settings.map { it.twelveHourClock }.distinctUntilChanged()

    override suspend fun setTwelveHourClock(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.TWELVE_HOUR_CLOCK] = enabled }
    }

    override val bigClock: Flow<Boolean>
        get() = settings.map { it.bigClock }.distinctUntilChanged()

    override suspend fun setBigClock(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BIG_CLOCK] = enabled }
    }

    override val showDate: Flow<Boolean>
        get() = settings.map { it.showDate }.distinctUntilChanged()

    override suspend fun setShowDate(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_DATE] = enabled }
    }

    override val firstTimeHelp: Flow<Boolean>
        get() = settings.map { it.firstTimeHelp }.distinctUntilChanged()

    override suspend fun setFirstTimeHelp(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FIRST_TIME_HELP] = enabled }
    }

    override val homeVAlignment: Flow<String>
        get() = settings.map { it.homeVAlignment }.distinctUntilChanged()

    override suspend fun setHomeVAlignment(alignment: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HOME_V_ALIGNMENT] = alignment }
    }

    override val homeAlignment: Flow<String>
        get() = settings.map { it.homeAlignment }.distinctUntilChanged()

    override suspend fun setHomeAlignment(alignment: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HOME_ALIGNMENT] = alignment }
    }

    override val widgetOffset: Flow<Float>
        get() = settings.map { it.widgetOffset }.distinctUntilChanged()

    override suspend fun setWidgetOffset(offset: Float) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_OFFSET] = offset }
    }

    override val widgetHeight: Flow<Float>
        get() = settings.map { it.widgetHeight }.distinctUntilChanged()

    override suspend fun setWidgetHeight(height: Float) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_HEIGHT] = height }
    }

    override val widgetWidth: Flow<Float>
        get() = settings.map { it.widgetWidth }.distinctUntilChanged()

    override suspend fun setWidgetWidth(width: Float) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_WIDTH] = width }
    }

    override val showQuickApps: Flow<Boolean>
        get() = settings.map { it.showQuickApps }.distinctUntilChanged()

    override suspend fun setShowQuickApps(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_QUICK_APPS] = enabled }
    }

    override val maxQuickApps: Flow<Int>
        get() = settings.map { it.maxQuickApps }.distinctUntilChanged()

    override suspend fun setMaxQuickApps(value: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.MAX_QUICK_APPS] = value }
    }
    
    override val iconPackPackage: Flow<String?>
        get() = settings.map { it.iconPackPackage }.distinctUntilChanged()

    override suspend fun setIconPackPackage(pkg: String?) {
        dataStore.edit { preferences -> 
            if (pkg == null) preferences.remove(PreferencesKeys.ICON_PACK_PACKAGE)
            else preferences[PreferencesKeys.ICON_PACK_PACKAGE] = pkg
        }
    }

    override val appsAlignment: Flow<String>
        get() = settings.map { it.appsAlignment }.distinctUntilChanged()

    override suspend fun setAppsAlignment(alignment: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.APPS_ALIGNMENT] = alignment }
    }

    override val theme: Flow<Int>
        get() = settings.map { it.theme }.distinctUntilChanged()

    override suspend fun setTheme(theme: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.THEME] = theme }
    }

    override val ltheme: Flow<Int>
        get() = settings.map { it.ltheme }.distinctUntilChanged()

    override suspend fun setLTheme(theme: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.LTHEME] = theme }
    }

    override val dtheme: Flow<Int>
        get() = settings.map { it.dtheme }.distinctUntilChanged()

    override suspend fun setDTheme(theme: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.DTHEME] = theme }
    }

    override val syncTheme: Flow<Boolean>
        get() = settings.map { it.syncTheme }.distinctUntilChanged()

    override suspend fun setSyncTheme(value: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SYNC_THEME] = value }
    }

    override val font: Flow<String>
        get() = settings.map { it.font }.distinctUntilChanged()

    override suspend fun setFont(value: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FONT] = value }
    }

    override val allowAnalyitics: Flow<Boolean>
        get() = settings.map { it.allowAnalytics }.distinctUntilChanged()

    override suspend fun setAllowAnalytics(value: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.ALLOW_ANALYTICS] = value }
    }

    override val doubleTapToLock: Flow<Boolean>
        get() = settings.map { it.doubleTapToLock }.distinctUntilChanged()

    override suspend fun setDoubleTapToLock(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.DOUBLE_TAP_TO_LOCK] = enabled }
    }
    
    override val showLauncherSwitcherNotification: Flow<Boolean>
        get() = dataStore.data.map { preferences -> preferences[PreferencesKeys.SHOW_LAUNCHER_SWITCHER_NOTIFICATION] ?: false }.distinctUntilChanged()
        
    override suspend fun setShowLauncherSwitcherNotification(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_LAUNCHER_SWITCHER_NOTIFICATION] = enabled }
    }

    override val showSearchBox: Flow<Boolean>
        get() = settings.map { it.showSearchBox }.distinctUntilChanged()

    override suspend fun setShowSearchBox(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_SEARCH_BOX] = enabled }
    }

    override val searchAutoOpen: Flow<Boolean>
        get() = settings.map { it.searchAutoOpen }.distinctUntilChanged()

    override suspend fun setSearchAutoOpen(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SEARCH_AUTO_OPEN] = enabled }
    }

    override val bottomSearch: Flow<Boolean>
        get() = settings.map { it.bottomSearch }.distinctUntilChanged()

    override suspend fun setBottomSearch(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BOTTOM_SEARCH] = enabled }
    }

    override val automaticallyOpenAppsInSearch: Flow<Boolean>
        get() = settings.map { it.automaticallyOpenAppsInSearch }.distinctUntilChanged()

    override suspend fun setAutomaticallyOpenAppsInSearch(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH] = enabled }
    }

    override val hideScreenTimePage: Flow<Boolean>
        get() = settings.map { it.hideScreenTimePage }.distinctUntilChanged()

    override suspend fun setHideScreenTimePage(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HIDE_SCREEN_TIME_PAGE] = enabled }
    }

    override val showHiddenAppsInSearch: Flow<Boolean>
        get() = settings.map { it.showHiddenAppsInSearch }.distinctUntilChanged()

    override suspend fun setShowHiddenAppsInSearch(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH] = enabled }
    }

    override val firstTime: Flow<Boolean>
        get() = settings.map { it.firstTime }.distinctUntilChanged()

    override suspend fun setFirstTime(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.FIRST_TIME] = enabled }
    }

    override val widgetId: Flow<Int>
        get() = settings.map { it.widgetId }.distinctUntilChanged()

    override suspend fun setWidgetId(value: Int) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.WIDGET_ID] = value }
    }

    override val isOnDefaultLauncherOnboarding: Flow<Boolean>
        get() = settings.map { it.isOnDefaultLauncherOnboarding }.distinctUntilChanged()

    override suspend fun setOnDefaultLauncherOnboarding(value: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE] = value }
    }

    override val physicalLauncherMode: Flow<Boolean>
        get() = settings.map { it.physicalLauncherMode }.distinctUntilChanged()

    override suspend fun setPhysicalLauncherMode(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.PHYSICAL_LAUNCHER_MODE] = enabled }
    }

    override val gestureNavigation: Flow<Boolean>
        get() = settings.map { it.gestureNavigation }.distinctUntilChanged()

    override suspend fun setGestureNavigation(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.GESTURE_NAVIGATION] = enabled }
    }

    override val enableNowPlaying: Flow<Boolean>
        get() = settings.map { it.enableNowPlaying }.distinctUntilChanged()

    override suspend fun setEnableNowPlaying(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.ENABLE_NOW_PLAYING] = enabled }
    }

    override val peoplePageBackgroundUri: Flow<String?>
        get() = settings.map { it.peoplePageBackgroundUri }.distinctUntilChanged()

    override suspend fun setPeoplePageBackgroundUri(uri: String?) {
        dataStore.edit { preferences ->
            if (uri == null) {
                preferences.remove(PreferencesKeys.PEOPLE_PAGE_BACKGROUND_URI)
            } else {
                preferences[PreferencesKeys.PEOPLE_PAGE_BACKGROUND_URI] = uri
            }
        }
    }

    override val customYoutubeChannels: Flow<List<YoutubeChannel>>
        get() = settings.map { it.customYoutubeChannels }.distinctUntilChanged()

    override suspend fun setCustomYoutubeChannels(channels: List<YoutubeChannel>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CUSTOM_YOUTUBE_CHANNELS] = gson.toJson(channels)
        }
    }

    override val routineAppsMorning: Flow<Set<String>>
        get() = settings.map { it.routineAppsMorning }.distinctUntilChanged()

    override suspend fun setRoutineAppsMorning(apps: Set<String>) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.ROUTINE_APPS_MORNING] = apps }
    }

    override val routineAppsAfternoon: Flow<Set<String>>
        get() = settings.map { it.routineAppsAfternoon }.distinctUntilChanged()

    override suspend fun setRoutineAppsAfternoon(apps: Set<String>) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.ROUTINE_APPS_AFTERNOON] = apps }
    }

    override val routineAppsEvening: Flow<Set<String>>
        get() = settings.map { it.routineAppsEvening }.distinctUntilChanged()

    override suspend fun setRoutineAppsEvening(apps: Set<String>) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.ROUTINE_APPS_EVENING] = apps }
    }

    override val routineAppsNight: Flow<Set<String>>
        get() = settings.map { it.routineAppsNight }.distinctUntilChanged()

    override suspend fun setRoutineAppsNight(apps: Set<String>) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.ROUTINE_APPS_NIGHT] = apps }
    }

    override suspend fun updateSettings(settings: SettingsData) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HAPTIC_FEEDBACK] = settings.hapticFeedBackEnabled
            preferences[PreferencesKeys.HIDE_PRIVATE_SPACE] = settings.hidePrivateSpace
            preferences[PreferencesKeys.TWELVE_HOUR_CLOCK] = settings.twelveHourClock
            preferences[PreferencesKeys.BIG_CLOCK] = settings.bigClock
            preferences[PreferencesKeys.SHOW_DATE] = settings.showDate
            preferences[PreferencesKeys.SHOW_GLASSMORPHIC_REMINDERS] = settings.showGlassmorphicReminders
            preferences[PreferencesKeys.REMINDER_FREQUENCY_HOURS] = settings.reminderFrequencyHours
            preferences[PreferencesKeys.REMINDER_START_TIME] = settings.reminderStartTime
            preferences[PreferencesKeys.REMINDER_END_TIME] = settings.reminderEndTime
            preferences[PreferencesKeys.HOME_ALIGNMENT] = settings.homeAlignment
            preferences[PreferencesKeys.HOME_V_ALIGNMENT] = settings.homeVAlignment
            preferences[PreferencesKeys.APPS_ALIGNMENT] = settings.appsAlignment
            preferences[PreferencesKeys.DOUBLE_TAP_TO_LOCK] = settings.doubleTapToLock
            preferences[PreferencesKeys.SHOW_SEARCH_BOX] = settings.showSearchBox
            preferences[PreferencesKeys.SEARCH_AUTO_OPEN] = settings.searchAutoOpen
            preferences[PreferencesKeys.BOTTOM_SEARCH] = settings.bottomSearch
            preferences[PreferencesKeys.AUTOMATICALLY_OPEN_APPS_IN_SEARCH] = settings.automaticallyOpenAppsInSearch
            preferences[PreferencesKeys.HIDE_SCREEN_TIME_PAGE] = settings.hideScreenTimePage
            preferences[PreferencesKeys.ALLOW_ANALYTICS] = settings.allowAnalytics
            preferences[PreferencesKeys.THEME] = settings.theme
            preferences[PreferencesKeys.LTHEME] = settings.ltheme
            preferences[PreferencesKeys.DTHEME] = settings.dtheme
            preferences[PreferencesKeys.SYNC_THEME] = settings.syncTheme
            preferences[PreferencesKeys.FONT] = settings.font
            preferences[PreferencesKeys.FIRST_TIME_HELP] = settings.firstTimeHelp
            preferences[PreferencesKeys.WIDGET_OFFSET] = settings.widgetOffset
            preferences[PreferencesKeys.WIDGET_HEIGHT] = settings.widgetHeight
            preferences[PreferencesKeys.WIDGET_WIDTH] = settings.widgetWidth
            preferences[PreferencesKeys.WIDGET_ID] = settings.widgetId
            preferences[PreferencesKeys.SHOW_QUICK_APPS] = settings.showQuickApps
            preferences[PreferencesKeys.MAX_QUICK_APPS] = settings.maxQuickApps
            preferences[PreferencesKeys.SHOW_HIDDEN_APPS_IN_SEARCH] = settings.showHiddenAppsInSearch
            preferences[PreferencesKeys.FIRST_TIME] = settings.firstTime
            preferences[PreferencesKeys.IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE] = settings.isOnDefaultLauncherOnboarding
            preferences[PreferencesKeys.PHYSICAL_LAUNCHER_MODE] = settings.physicalLauncherMode
            preferences[PreferencesKeys.GESTURE_NAVIGATION] = settings.gestureNavigation
            preferences[PreferencesKeys.ENABLE_NOW_PLAYING] = settings.enableNowPlaying
            val backgroundUri = settings.peoplePageBackgroundUri
            if (backgroundUri == null) {
                preferences.remove(PreferencesKeys.PEOPLE_PAGE_BACKGROUND_URI)
            } else {
                preferences[PreferencesKeys.PEOPLE_PAGE_BACKGROUND_URI] = backgroundUri
            }
            preferences[PreferencesKeys.CUSTOM_YOUTUBE_CHANNELS] = gson.toJson(settings.customYoutubeChannels)
            preferences[PreferencesKeys.ROUTINE_APPS_MORNING] = settings.routineAppsMorning
            preferences[PreferencesKeys.ROUTINE_APPS_AFTERNOON] = settings.routineAppsAfternoon
            preferences[PreferencesKeys.ROUTINE_APPS_EVENING] = settings.routineAppsEvening
            preferences[PreferencesKeys.ROUTINE_APPS_NIGHT] = settings.routineAppsNight
            preferences[PreferencesKeys.FOLDER_GRID_SIZE] = settings.folderGridSize
            preferences[PreferencesKeys.FOLDER_BACKGROUND_OPACITY] = settings.folderBackgroundOpacity
            preferences[PreferencesKeys.ENABLE_HISTORY_RECOMMENDATIONS] = settings.enableHistoryRecommendations
            preferences[PreferencesKeys.ENABLE_ECONOMICS_RECOMMENDATIONS] = settings.enableEconomicsRecommendations
            preferences[PreferencesKeys.ENABLE_PSYCHOLOGY_RECOMMENDATIONS] = settings.enablePsychologyRecommendations
            preferences[PreferencesKeys.ENABLE_SOCIOLOGY_RECOMMENDATIONS] = settings.enableSociologyRecommendations
            preferences[PreferencesKeys.ENABLE_WHAT_IF_RECOMMENDATIONS] = settings.enableWhatIfRecommendations
            preferences[PreferencesKeys.ENABLE_FOREIGN_MOVIES_RECOMMENDATIONS] = settings.enableForeignMoviesRecommendations
            preferences[PreferencesKeys.RECOMMENDATION_FREQUENCY_HOURS] = settings.recommendationFrequencyHours
            preferences[PreferencesKeys.RECOMMENDATION_TIME] = settings.recommendationTime
            val pkg = settings.iconPackPackage
            if (pkg == null) preferences.remove(PreferencesKeys.ICON_PACK_PACKAGE) else preferences[PreferencesKeys.ICON_PACK_PACKAGE] = pkg
        }
    }

    suspend fun getSettingsData(): SettingsData {
        return settings.first()
    }
}
