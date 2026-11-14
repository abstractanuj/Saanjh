package com.geecee.escapelauncher.core.model

/**
 * Main data model for all user settings.
 *
 * Senior Dev Note: This class is kept flat for easier DataStore mapping and to avoid
 * breaking existing call sites, but is logically grouped using regions.
 */
data class SettingsData(
    // region Display Settings
    val twelveHourClock: Boolean = false,
    val bigClock: Boolean = false,
    val showDate: Boolean = false,
    val showGlassmorphicReminders: Boolean = true,
    val reminderFrequencyHours: Int = 1,
    val reminderStartTime: String = "08:00",
    val reminderEndTime: String = "22:00",
    val homeAlignment: String = "Center",
    val homeVAlignment: String = "Center",
    val appsAlignment: String = "Center",
    val font: String = "Jost",
    val peoplePageBackgroundUri: String? = null,
    val iconPackPackage: String? = null,
    // endregion

    // region Search Settings
    val showSearchBox: Boolean = true,
    val searchAutoOpen: Boolean = false,
    val bottomSearch: Boolean = true,
    val automaticallyOpenAppsInSearch: Boolean = false,
    val showHiddenAppsInSearch: Boolean = false,
    // endregion

    // region Folder Settings
    val folderGridSize: Int = 3,
    val folderBackgroundOpacity: Float = 0.8f,
    // endregion

    // region Recommendation Settings
    val enableHistoryRecommendations: Boolean = true,
    val enableEconomicsRecommendations: Boolean = true,
    val enablePsychologyRecommendations: Boolean = true,
    val enableSociologyRecommendations: Boolean = true,
    val enableWhatIfRecommendations: Boolean = true,
    val enableForeignMoviesRecommendations: Boolean = true,
    val recommendationFrequencyHours: Int = 24,
    val recommendationTime: String = "10:00",
    // endregion

    // region Theme Settings
    val enableSystemWallpaper: Boolean = false,
    val theme: Int = 11,
    val ltheme: Int = 11,
    val dtheme: Int = 11,
    val syncTheme: Boolean = false,
    // endregion

    // region Behavior Settings
    val hapticFeedBackEnabled: Boolean = true,
    val hidePrivateSpace: Boolean = true,
    val doubleTapToLock: Boolean = false,
    val hideScreenTimePage: Boolean = false,
    val allowAnalytics: Boolean = false,
    val showQuickApps: Boolean = true,
    val maxQuickApps: Int = 4,
    val physicalLauncherMode: Boolean = false,
    val gestureNavigation: Boolean = false,
    val enableNowPlaying: Boolean = false,
    val showLauncherSwitcherNotification: Boolean = false,
    val routineAppsMorning: Set<String> = emptySet(),
    val routineAppsAfternoon: Set<String> = emptySet(),
    val routineAppsEvening: Set<String> = emptySet(),
    val routineAppsNight: Set<String> = emptySet(),
    // endregion

    // region Experimental & Others
    val widgetOffset: Float = 0f,
    val widgetHeight: Float = 125f,
    val widgetWidth: Float = 250f,
    val widgetId: Int = -1,
    val customYoutubeChannels: List<YoutubeChannel> = emptyList(),
    val firstTimeHelp: Boolean = true,
    val firstTime: Boolean = true,
    val isOnDefaultLauncherOnboarding: Boolean = false
    // endregion
) {
    init {
        // Guards to ensure valid state
        require(maxQuickApps in 1..10) { "maxQuickApps must be between 1 and 10 (value: $maxQuickApps)" }
        require(widgetHeight >= 0) { "widgetHeight cannot be negative (value: $widgetHeight)" }
        require(widgetWidth >= 0) { "widgetWidth cannot be negative (value: $widgetWidth)" }
    }
}
