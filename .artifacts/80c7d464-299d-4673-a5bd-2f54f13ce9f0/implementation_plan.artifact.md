# Escape Launcher Features Implementation Plan

This plan outlines the architecture and tasks required to implement the new feature requests. Since this is a massive set of features, they are broken down logically.

## User Review Required

> [!WARNING]
> **Native Widget Host Limits**: Allowing users to add arbitrary native Android widgets on a dedicated page will require careful state management of the `AppWidgetHost`. The app currently has a `feature:newwidgets` module; we will extend it to manage a list of widgets for this new page.
>
> **Notification Listener**: Reading notifications requires the user to explicitly grant the `NotificationListenerService` permission in Android settings. The app will need to prompt the user to enable this if they swipe up to view the notifications widget.

## Open Questions

> [!IMPORTANT]
> 1. **Routine Apps**: Should "Routine Apps" be a separate section on the Home Screen, or should it replace the current "Today's Picks" or "Quick Apps" bar depending on the time of day?
> 2. **Button for Apps List**: Where exactly on the `HomeScreen` would you like the "Apps List" button? (e.g., bottom center, floating action button, replacing an existing element?)
> 3. **iPhone-like Reminders**: When should these reminders trigger? (e.g., randomly, at specific times, when unlocking the phone, or when trying to open a distracting app?)

## Proposed Changes

### Component 1: Swipe Up Gesture & Apps List Button
- **Modify** `MainPagerScreen.kt` to change the `anchoredDraggable` overlay. Instead of revealing the `AppsList`, it will reveal a new `WidgetsScreen`.
- **Modify** `HomeScreen.kt` to include a button that manually triggers the `AppsList` to open (e.g., via a state change or an Intent, depending on how we decide to host it now that it's no longer the swipe-up overlay).

### Component 2: Widgets Page & Notification Widget (METRO Style)
- **Create** a new `feature:notificationwidget` (or integrate into `feature:newwidgets`).
- **Implement** `NotificationListenerService` to intercept incoming notifications.
- **Store/Cache** notifications in memory or Room DB.
- **Create** `NotificationWidget.kt` in pure Metro style (Typography focused, no heavy chrome, monochromatic).
- **Create** `WidgetsScreen.kt` which hosts the `NotificationWidget` at the top, and allows users to add/remove native Android widgets below it using the existing `WidgetHostManager`.

### Component 3: Routine Apps
- **Update** `SettingsRepository` and `SettingsData` to store user preferences for Morning/Afternoon/Evening/Night apps.
- **Create** a `RoutineAppsManager` or `UseCase` that evaluates the current time of day and returns the appropriate list of `InstalledApp`.
- **Modify** `HomeScreen` to display these "Routine Apps" prominently.

### Component 4: iPhone-like Reminders
- **Implement** a background worker or alarm (using `WorkManager` or `AlarmManager`) or hook into device unlock events.
- **Create** a Metro-style Dialog or Overlay (using `AlertDialog` or a custom full-screen Compose overlay) that displays a quote/reminder with an "Okay" button.

### Component 5: Edge-to-Edge System Bars
- **Update** `MainHomeScreenActivity` and Compose Theme to use transparent status and navigation bars.
- Ensure that the `HomeScreen`, `AppsList`, and `WidgetsScreen` all properly handle `WindowInsets` so content does not overlap with the system UI, achieving a true "Full Bleed Canvas".

## Verification Plan

### Automated Tests
- Test time-of-day logic for Routine Apps.
- Test `NotificationListenerService` parsing logic with mock notification payloads.

### Manual Verification
- Deploy to device to verify edge-to-edge transparent system bars.
- Swipe up to ensure the Widgets page appears smoothly.
- Grant Notification Access and receive a test notification (e.g., via WhatsApp or simulated) to verify it appears in Metro style.
- Click the Apps List button to ensure it navigates correctly.
- Add and remove a native Android widget on the new Widgets page.
