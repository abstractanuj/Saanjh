package com.geecee.escapelauncher.core.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object PreferencesKeys {
    val ALLOW_ANALYTICS = booleanPreferencesKey(name = "allow_analytics")
    val HAPTIC_FEEDBACK = booleanPreferencesKey(name = "haptic_feedback")

    // Private Space
    val HIDE_PRIVATE_SPACE = booleanPreferencesKey(name = "hidden_private_space")

    // Home Screen & Exact Reminders
    val TWELVE_HOUR_CLOCK = booleanPreferencesKey(name = "twelve_hour_clock")
    val BIG_CLOCK = booleanPreferencesKey(name = "big_clock")
    val SHOW_DATE = booleanPreferencesKey(name = "show_date")
    val SHOW_GLASSMORPHIC_REMINDERS = booleanPreferencesKey(name = "show_glassmorphic_reminders")
    val REMINDER_FREQUENCY_HOURS = intPreferencesKey(name = "reminder_frequency_hours")
    val REMINDER_START_TIME = stringPreferencesKey(name = "reminder_start_time")
    val REMINDER_END_TIME = stringPreferencesKey(name = "reminder_end_time")
    val FIRST_TIME_HELP = booleanPreferencesKey(name = "first_time_help")
    val HOME_V_ALIGNMENT = stringPreferencesKey(name = "home_v_alignment")
    val HOME_ALIGNMENT = stringPreferencesKey(name = "home_alignment")

    // Modern Home
    val SHOW_GREETING = booleanPreferencesKey(name = "show_greeting")
    val SHOW_QUICK_APPS = booleanPreferencesKey(name = "show_quick_apps")
    val MAX_QUICK_APPS = intPreferencesKey(name = "max_quick_apps")

    // Apps List
    val APPS_ALIGNMENT = stringPreferencesKey(name = "apps_alignment")

    // Widget
    val WIDGET_OFFSET = floatPreferencesKey(name = "widget_offset")
    val WIDGET_HEIGHT = floatPreferencesKey(name = "widget_height")
    val WIDGET_WIDTH = floatPreferencesKey(name = "widget_width")
    val WIDGET_ID = intPreferencesKey(name = "widget_ID")

    // Theme
    val THEME = intPreferencesKey(name = "theme")
    val DTHEME = intPreferencesKey(name = "dtheme")
    val LTHEME = intPreferencesKey(name = "ltheme")
    val SYNC_THEME = booleanPreferencesKey(name = "sync_theme")
    val FONT = stringPreferencesKey(name = "font")

    // Search
    val SHOW_SEARCH_BOX = booleanPreferencesKey(name = "show_search_box")
    val SEARCH_AUTO_OPEN = booleanPreferencesKey(name = "search_auto_open")
    val BOTTOM_SEARCH = booleanPreferencesKey(name = "bottom_search")
    val AUTOMATICALLY_OPEN_APPS_IN_SEARCH = booleanPreferencesKey(name = "automatically_open_apps_in_search")

    // Misc
    val DOUBLE_TAP_TO_LOCK = booleanPreferencesKey(name = "double_tap_to_lock")
    val SHOW_LAUNCHER_SWITCHER_NOTIFICATION = booleanPreferencesKey(name = "show_launcher_switcher_notification")
    val HIDE_SCREEN_TIME_PAGE = booleanPreferencesKey(name = "hide_screen_time_page")
    val SHOW_HIDDEN_APPS_IN_SEARCH = booleanPreferencesKey(name = "show_hidden_apps_in_search")
    val FIRST_TIME = booleanPreferencesKey(name = "first_time")
    val IS_ON_DEAFAULT_LAUNCHER_ONBOARDING_PAGE = booleanPreferencesKey(name = "is_on_default_launcher_onboarding_page")
    val PHYSICAL_LAUNCHER_MODE = booleanPreferencesKey(name = "physical_launcher_mode")
    val GESTURE_NAVIGATION = booleanPreferencesKey(name = "gesture_navigation")
    val ENABLE_NOW_PLAYING = booleanPreferencesKey(name = "enable_now_playing")

    val PEOPLE_PAGE_BACKGROUND_URI = stringPreferencesKey(name = "people_page_background_uri")
    val ROUTINE_APPS_MORNING = stringSetPreferencesKey(name = "routine_apps_morning")
    val ROUTINE_APPS_AFTERNOON = stringSetPreferencesKey(name = "routine_apps_afternoon")
    val ROUTINE_APPS_EVENING = stringSetPreferencesKey(name = "routine_apps_evening")
    val ROUTINE_APPS_NIGHT = stringSetPreferencesKey(name = "routine_apps_night")

    // Folders
    val FOLDER_GRID_SIZE = intPreferencesKey(name = "folder_grid_size")
    val FOLDER_BACKGROUND_OPACITY = floatPreferencesKey(name = "folder_background_opacity")

    // Recommendations
    val ENABLE_HISTORY_RECOMMENDATIONS = booleanPreferencesKey(name = "enable_history_recommendations")
    val ENABLE_SYSTEM_WALLPAPER = booleanPreferencesKey(name = "enable_system_wallpaper")
    val ENABLE_ECONOMICS_RECOMMENDATIONS = booleanPreferencesKey(name = "enable_economics_recommendations")
    val ENABLE_PSYCHOLOGY_RECOMMENDATIONS = booleanPreferencesKey(name = "enable_psychology_recommendations")
    val ENABLE_SOCIOLOGY_RECOMMENDATIONS = booleanPreferencesKey(name = "enable_sociology_recommendations")
    val ENABLE_WHAT_IF_RECOMMENDATIONS = booleanPreferencesKey(name = "enable_what_if_recommendations")
    val ENABLE_FOREIGN_MOVIES_RECOMMENDATIONS = booleanPreferencesKey(name = "enable_foreign_movies_recommendations")
    val RECOMMENDATION_FREQUENCY_HOURS = intPreferencesKey(name = "recommendation_frequency_hours")
    val RECOMMENDATION_TIME = stringPreferencesKey(name = "recommendation_time")
    val ICON_PACK_PACKAGE = stringPreferencesKey(name = "icon_pack_package")
}
