# App Polish & Feature Additions Implementation Plan

This plan outlines the architecture and steps to implement the four major feature areas requested to bring the launcher to a production-ready, polished state.

## User Review Required

> [!IMPORTANT]
> **Advanced Widget Management** requires a custom `AppWidgetHost` implementation. This is notoriously complex in Android because we have to manually handle grid snapping, resizing, and host view recycling. We will scaffold out a robust `AppWidgetHost` implementation, but it will significantly increase the codebase size.
>
> **App Blocking** will be enforced proactively when launching an app from the launcher. If an app is launched externally, we will need to rely on the `EscapeAccessibilityService` to forcefully exit or overlay the blocked app.

## Open Questions

> [!NOTE]
> 1. Do you want the **Onboarding Wizard** to block the user from entering the launcher until ALL permissions are granted, or are some permissions (like Accessibility) optional?
> 2. For the **Recommendation Engine**, should the reworked UI be integrated directly into the `Home.kt` feed, or do you want a dedicated "Mindful Hub" page in the ViewPager?

## Proposed Changes

### 1. Guided Setup & Onboarding Wizard
We need a robust onboarding flow to explain *why* the launcher needs sensitive permissions to function as an anti-addiction tool.
#### [NEW] `feature/onboarding/src/main/java/com/geecee/escapelauncher/feature/onboarding/OnboardingActivity.kt`
- Create a `HorizontalPager` with visually distinct, glassmorphic slides.
- **Slides:**
  1. Welcome & Philosophy
  2. Usage Access (for Screen Time & App Blocking)
  3. Notification Access (for Notification Badges / Hiding distracting pings)
  4. Set as Default Launcher
#### [MODIFY] `app/src/main/java/com/geecee/escapelauncher/EscapeApplication.kt` & `SettingsDataStore.kt`
- Check `isOnboardingComplete` on launch. If false, route the user to `OnboardingActivity` instead of the Main Launcher.

---

### 2. App Blocking / Hard Limits
Allow users to set a strict time limit (e.g., 30 mins) for distracting apps.
#### [NEW] `core/data/src/main/java/com/geecee/escapelauncher/core/data/entity/AppLimitEntity.kt` & `AppLimitDao.kt`
- Create a Room entity to store `packageName` and `dailyLimitMinutes`.
#### [MODIFY] `core/ui/src/main/java/com/geecee/escapelauncher/core/ui/composables/OpenChallenge.kt`
- When an app is clicked, check the current usage vs. the limit.
- If the limit is exceeded, show a **Lockout UI** instead of the Open Challenge, preventing the launch.
#### [MODIFY] `feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/HomeScreenBottomSheet.kt`
- Add a "Set Time Limit" option in the long-press app menu.

---

### 3. Advanced Widget Management
Build a custom AppWidgetHost to properly render, scale, and snap Android widgets to a grid.
#### [NEW] `feature/widgets/src/main/java/com/geecee/escapelauncher/feature/widgets/EscapeAppWidgetHost.kt`
- Extend `android.appwidget.AppWidgetHost` and `AppWidgetHostView` to customize padding and rounded corners natively.
#### [NEW] `feature/widgets/src/main/java/com/geecee/escapelauncher/feature/widgets/WidgetGrid.kt`
- A Compose layout that calculates drag bounds, applies strict grid-snapping (e.g., 4x4 or 5x5 cells), and handles dynamic resizing gestures.

---

### 4. Reworked Recommendations Engine (Movies, Music, Quotes) UI/UX
Transform the generic static lists into a beautiful, engaging "Discovery" feed.
#### [MODIFY] `core/data/src/main/java/com/geecee/escapelauncher/core/data/repository/LocalContent.kt`
- Structure the raw string lists into rich Data Classes (`Quote(text, author, theme)`, `MovieRecommendation(title, director, genre, prompt)`).
#### [MODIFY] `core/ui/src/main/java/com/geecee/escapelauncher/core/ui/composables/ModernHomeComposables.kt`
- Rework the `QuoteMediaPager` into a polished, unified **Mindful Card**.
- **UX Update:** Instead of swiping manually, the card smoothly crossfades between a Quote, a Movie recommendation, and a Music track every few hours, or when tapped.
- Add elegant typography (using the "Celebrate Typography" design pillar) and subtle parallax backgrounds for recommendations.

## Verification Plan
1. **Onboarding:** Wipe app data. Ensure the app forces the onboarding screen and correctly validates each Android permission intent (`ACTION_USAGE_ACCESS_SETTINGS`).
2. **App Blocking:** Set a 1-minute limit on an app. Launch it, wait a minute, close it, and try to launch it again to verify the Lockout UI appears.
3. **Recommendations:** Check `ModernHomeComposables.kt` in the Compose Preview to verify the aesthetic crossfade and updated typography for the new Recommendation Cards.