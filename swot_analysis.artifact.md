# App Revamp SWOT Analysis & Plan

This document outlines the strategic analysis and high-level plan for implementing the requested UI and functional changes to Escape Launcher.

## SWOT Analysis

### Strengths
* **Modern Tech Stack**: The app relies on Jetpack Compose, Hilt, and MVVM, which makes injecting new UI animations (Motion Design) and background workers straightforward.
* **Existing Infrastructure**: `WorkManager` is already configured in `EscapeApplication.kt`, making the background reminder implementation a natural extension rather than a ground-up build.
* **Centralized Logic**: App categories are cleanly defined in `AppCategory.kt` and `AppClassifier.kt`, meaning category changes are isolated and easy to update.

### Weaknesses
* **Gesture Complexity**: The `MainPagerScreen` already uses a complex `AnchoredDraggableState` with a `NestedScrollConnection`. Adding an alphabet scrubber and expandable folders might introduce gesture conflicts that need careful handling.
* **Static Categorization**: The `AppClassifier.kt` relies on hardcoded package lists. Introducing "Wellbeing" and "Games" will require curating new static lists or relying heavily on the Gemini categorizer.
* **Layout Constraints**: The `ContactsScreen` currently uses a fixed-weight `BentoGroup` engine. Converting this to a scrollable layout requires refactoring how the Bento items calculate their sizes.

### Opportunities
* **Modern OS Feel**: Fully implementing edge-to-edge support with proper window insets will make the launcher feel native and polished on Android 14+.
* **Enhanced UX**: An alphabet scrubber and sorting by recent installation time will drastically reduce friction for users trying to find specific apps quickly.
* **Motion Design**: Applying Compose animations (`AnimatedVisibility`, `animateContentSize`, `spring` specs) will elevate the minimalist design, making the app feel premium and fluid without adding visual clutter.

### Threats (Risks)
* **Background Execution Limits**: Relying on `WorkManager` for hourly reminders when the app is in the background might be affected by Android's Doze mode and manufacturer-specific battery optimizations. The notifications might not be perfectly punctual.
* **Migration Issues**: Users who previously had apps classified under "Tools" or "Productivity" will see their apps moved. Any saved preferences or manual overrides pointing to the old categories might break if not handled safely.
* **Performance**: Adding animations to a large `LazyColumn` (All Apps list) can cause jank if not optimized properly.

---

## High-Level Implementation Plan

1. **Reminders in Background (WorkManager & Notifications)**
   * Create an `HourlyReminderWorker` to run periodically.
   * Implement a Notification Channel and trigger system notifications with the reminder quote.
   * Update `MainPagerScreenViewModel` to toggle this worker instead of just showing an in-app dialog.

2. **Edge-to-Edge Adjustments**
   * Review `MainHomeScreenActivity.kt` and ensure `enableEdgeToEdge()` is used correctly.
   * Apply `Modifier.systemBarsPadding()` or `Modifier.safeDrawingPadding()` at the root of `MainPagerScreen` and `ContactsScreen` to prevent overlap with the status bar and navigation bar.

3. **Category Revamp & "Weekend Leisure" Removal**
   * Modify `AppCategory.kt`: Remove `TOOLS`, `PRODUCTIVITY`, `OTHERS`. Add `GAMES`, `WELLBEING`.
   * Update `AppClassifier.kt` with new package sets.
   * Strip out the "Weekend Leisure" logic and UI from `AppsListViewModel.kt` and `AppsList.kt`.

4. **Apps List Redesign (Folders, Sorting, Scrubber)**
   * **Sorting**: Update `AppsRepositoryImpl` or `AppsListViewModel` to sort the main list of apps by `firstInstallTime` descending.
   * **Folders**: Create a new `AppFolder` composable. Group categorized apps at the top of the `AppsList`. Use `animateContentSize()` for smooth expansion/collapse.
   * **Alphabet Scrubber**: Implement a custom vertical drag gesture on the right edge of the screen that maps the Y-coordinate to the alphabet and scrolls the `LazyColumn` state to the corresponding letter.

5. **Motion Design**
   * Add `animateContentSize()` to folders and bento boxes.
   * Use `AnimatedContent` for transitions between pager states or category selections.
   * Apply subtle spring animations to button presses and list item appearances.

6. **Scrollable Contacts Screen**
   * Refactor `ContactsScreen.kt` to wrap the `BentoGroup` and headers in a `verticalScroll` modifier or a `LazyColumn`.
   * Adjust the `BentoGroup` height calculation to allow for infinite scrolling rather than constraining to screen height.