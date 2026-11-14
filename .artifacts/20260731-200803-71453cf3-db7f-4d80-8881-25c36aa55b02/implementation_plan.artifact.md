# Move Search Bar to Bottom in App List

Move the search bar in the app list to the bottom of the screen by default, just above the Android navigation bar, and add a setting to toggle this behavior.

## Proposed Changes

### Core Model & Data

#### [SettingsData.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/model/src/main/java/com/geecee/escapelauncher/core/model/SettingsData.kt)
- Change default value of `bottomSearch` from `false` to `true`.

#### [SettingsRepositoryImpl.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/repository/SettingsRepositoryImpl.kt)
- Change default value of `bottomSearch` in the `settings` flow mapping from `false` to `true`.

---

### Settings UI

#### [MainSettingsPageViewModel.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/settings/src/main/java/com/geecee/escapelauncher/feature/settings/mainpage/MainSettingsPageViewModel.kt)
- Change initial value of `bottomSearch` in `MainSettingsUiState` from `false` to `true`.

#### [MainSettingsPage.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/settings/src/main/java/com/geecee/escapelauncher/feature/settings/mainpage/MainSettingsPage.kt)
- Add an "Apps List" group in settings.
- Add a `SettingsSwitch` for "Search at bottom" (using `R.string.search_at_bottom`).

---

### App List UI

#### [AppsList.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/appslist/src/main/java/com/geecee/escapelauncher/feature/appslist/AppsList.kt)
- In `AppsListContent`, improve the bottom search bar rendering:
    - Add a semi-transparent background to the container `Box` when at the bottom to ensure readability over scrolling content.
    - Always show the background of `AnimatedPillSearchBar` (set `showBackground = true`).
    - Adjust grid `contentPadding` to reduce top padding and ensure enough bottom padding when search is at the bottom.

---

## Verification Plan

### Automated Tests
- Run `AppsListPreview` using `render_compose_preview` to verify the visual layout with the search bar at the bottom.

### Manual Verification
- Deploy the app to a device/emulator.
- Open the Apps List and verify the search bar is at the bottom by default.
- Scroll through the apps and verify that the search bar is readable and items scroll under it correctly.
- Go to Settings and toggle the "Search at bottom" setting.
- Verify that the search bar moves to the top when the setting is disabled.
