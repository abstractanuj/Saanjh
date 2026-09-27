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
    val homeAlignment: String = "Center",
    val homeVAlignment: String = "Center",
    val appsAlignment: String = "Center",
    val font: String = "Jost",
    val peoplePageBackgroundUri: String? = null,
    // endregion

    // region Search Settings
    val showSearchBox: Boolean = true,
    val searchAutoOpen: Boolean = false,
    val bottomSearch: Boolean = true,
    val automaticallyOpenAppsInSearch: Boolean = false,
    val showHiddenAppsInSearch: Boolean = false,
    // endregion

    // region Theme Settings
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
