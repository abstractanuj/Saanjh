# Escape Launcher Refactor Plan - Phase 3

This phase focuses on correcting the weather and astronomical data flow, refining usage statistics, and adding user settings for advanced features.

## Proposed Changes

### 1. Weather & Astronomical Data System
#### [WeatherImpl.kt (Google)](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/weather/src/google/java/com/geecee/escapelauncher/feature/weather/WeatherImpl.kt)
- Fix JSON parsing to correctly handle the `current` object vs `current_weather` in Open-Meteo.
- Correctly map `wind_speed_10m` to `windSpeed` in `WeatherInfo`.
- Ensure `fetchBrightestObject` uses the Visible Planets API correctly with lat/lon.

#### [WeatherViewModel.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/weather/src/main/java/com/geecee/escapelauncher/feature/weather/WeatherViewModel.kt)
- Support dynamic refresh intervals based on settings.

### 2. Usage Statistics (Screen Time)
#### [UsageStatsHelper.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/common/src/main/java/com/geecee/escapelauncher/core/common/UsageStatsHelper.kt)
- Ensure `getDailyUsageStats` correctly implements the 7 AM epoch reset.
- Ensure `getWeeklyUsageStats` provides data for visual charts.

#### [MindfulStatistics.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/MindfulStatistics.kt)
- Update UI to show a weekly statistic visual (if applicable) while the bottom sentence remains a daily total from 7 AM.

### 3. Settings & Configuration
#### [SettingsData.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/model/src/main/java/com/geecee/escapelauncher/core/model/SettingsData.kt) & [SettingsRepository.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/domain/src/main/java/com/geecee/escapelauncher/core/domain/repository/SettingsRepository.kt)
- Add fields for weather refresh interval, showing sunrise/sunset, brightest object, and wind speed.

#### [NEW] [WeatherSettingsPage.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/settings/src/main/java/com/geecee/escapelauncher/feature/settings/weather/WeatherSettingsPage.kt)
- Create a dedicated settings page for weather and sky observation.

### 4. UI Refinements
#### [Home.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/Home.kt)
- Remove duplicate weather condition text.
- Verify and fix grammatical issues in greetings.
- Condense the "How are you feeling" section (Emotions) into a single-page view without scrolling.

#### [AppsList.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/appslist/src/main/java/com/geecee/escapelauncher/feature/appslist/AppsList.kt)
- Move "Recently Added" apps below the alphabet list.
- Ensure "Recently Added" is collapsed by default.

## Verification Plan
### Automated Tests
- N/A (Manual UI verification preferred for this phase).

### Manual Verification
- Check Home screen for correct weather data (Wind, Sun, Brightest Object).
- Verify Screen Time sentence resets at 7 AM.
- Verify App Drawer layout and "Recently Added" behavior.
- Test new Weather Settings page.
