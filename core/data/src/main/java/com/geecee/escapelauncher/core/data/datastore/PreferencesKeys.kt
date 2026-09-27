package com.geecee.escapelauncher.core.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferencesKeys {
    val ALLOW_ANALYTICS = booleanPreferencesKey(name = "allow_analytics")
    val HAPTIC_FEEDBACK = booleanPreferencesKey(name = "haptic_feedback")

    //Private Space
    val HIDE_PRIVATE_SPACE = booleanPreferencesKey(name = "hidden_private_space")
    //Home Screen
    val TWELVE_HOUR_CLOCK = booleanPreferencesKey(name = "twelve_hour_clock")
    val BIG_CLOCK = booleanPreferencesKey(name = "big_clock")
    val SHOW_DATE = booleanPreferencesKey(name = "show_date")
    val FIRST_TIME_HELP = booleanPreferencesKey(name = "first_time_help")
    val HOME_V_ALIGNMENT = stringPreferencesKey(name = "home_v_alignment")
    val HOME_ALIGNMENT = stringPreferencesKey(name = "home_alignment")

    //Modern Home
    val SHOW_GREETING = booleanPreferencesKey(name = "show_greeting")
    val SHOW_QUICK_APPS = booleanPreferencesKey(name = "show_quick_apps")
    val MAX_QUICK_APPS = intPreferencesKey(name = "max_quick_apps")

    //Apps List
    val APPS_ALIGNMENT = stringPreferencesKey(name = "apps_alignment")

    //Widget
    val WIDGET_OFFSET = floatPreferencesKey(name = "widget_offset")
    val WIDGET_HEIGHT = floatPreferencesKey(name = "widget_height")
    val WIDGET_WIDTH = floatPreferencesKey(name = "widget_width")
    val WIDGET_ID = intPreferencesKey(name = "widget_ID")

    //Theme
    val THEME = intPreferencesKey(name = "theme")
    val DTHEME = intPreferencesKey(name = "dtheme")
    val LTHEME = intPreferencesKey(name = "ltheme")
    val SYNC_THEME = booleanPreferencesKey(name = "sync_theme")
    val FONT = stringPreferencesKey(name = "font")

    //Search
    val SHOW_SEARCH_BOX = booleanPreferencesKey(name = "show_search_box")
    val SEARCH_AUTO_OPEN = booleanPreferencesKey(name = "search_auto_open")
    val BOTTOM_SEARCH = booleanPreferencesKey(name = "bottom_search")
    val AUTOMATICALLY_OPEN_APPS_IN_SEARCH = booleanPreferencesKey(name = "automatically_open_apps_in_search")

    //Misc
    val DOUBLE_TAP_TO_LOCK = booleanPreferencesKey(name = "double_tap_to_lock")
    val SHOW_LAUNCHER_SWITCHER_NOTIFICATION = booleanPreferencesKey(name = "show_launcher_switcher_notification")
    val HIDE_SCREEN_TIME_PAGE = booleanPreferencesKey(name = "hide_screen_time_page")
    val SHOW_HIDDEN_APPS_IN_SEARCH = booleanPreferencesKey(name = "show_hidden_apps_in_search")
    val FIRST_TIME = booleanPreferencesKey(name = "first_time")
    val IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE = booleanPreferencesKey(name = "is_on_default_launcher_onboarding_page")
    val PHYSICAL_LAUNCHER_MODE = booleanPreferencesKey(name = "physical_launcher_mode")
    val GESTURE_NAVIGATION = booleanPreferencesKey(name = "gesture_navigation")
    val ENABLE_NOW_PLAYING = booleanPreferencesKey(name = "enable_now_playing")

    //Weather
    val SHOW_WEATHER = booleanPreferencesKey(name = "show_weather")
    val USE_FAHRENHEIT = booleanPreferencesKey(name = "use_fahrenheit")
    val WEATHER_APP_PACKAGE = stringPreferencesKey(name = "weather_app_package")
    val PEOPLE_PAGE_BACKGROUND_URI = stringPreferencesKey(name = "people_page_background_uri")
    val CUSTOM_YOUTUBE_CHANNELS = stringPreferencesKey(name = "custom_youtube_channels")
}
