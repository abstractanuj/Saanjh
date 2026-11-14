# Overhaul: Paper/E-ink Design, App Friction, and Performance Optimization

This plan details a massive refactoring of Escape Launcher. It aims to completely overhaul the visual design system to a minimalist, Japanese-inspired "Paper/e-ink" aesthetic (incorporating Ma, Kanso, Shibui, and glassmorphism). It also introduces "App Opening Friction" for specific apps to promote digital well-being, adds support for Work Profile apps, strictly enforces Android architectural and performance best practices (Baseline profiles, lazy init, Compose stability), and removes the previous Cognitive Development Plan features while retaining music/movie suggestions.

## User Review Required

> [!IMPORTANT]
> **Design System Shift:** The move from a "Metro" style to "Paper/e-ink" requires completely replacing the current color palettes, typography, and spacing tokens in `core/theme`.
> **Feature Deprecation:** All widgets, traces, and question-prompts associated with the "Cognitive Development Plan" will be permanently removed.
> **Friction Mechanics:** The new friction mechanisms (Intent prompt, 5-10s delay, visual decay, budget display) will significantly alter how the launcher behaves when opening distracting apps.

## Open Questions

> [!WARNING]
> 1. **Friction Settings:** How should the user configure which apps receive friction? Should this be in the main settings screen, or accessible via a long-press context menu on the app icon?
> 2. **Glassmorphism:** "Paper/e-ink" implies solid, matte, low-contrast surfaces, whereas glassmorphism implies translucency and blur. How would you like these combined? (e.g., solid matte backgrounds with subtle translucent overlays/dialogs?)
> 3. **Work Profiles:** The project already has a `feature/workapps/` module. Should we integrate work apps into the main drawer with a briefcase badge, or keep them in a separate tab?

## Proposed Changes

### Build & Performance Optimizations
Enforcing performance best practices, modernizing the build system, and improving cold-start times.

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/gradle/libs.versions.toml)
- Add dependencies for `androidx.benchmark.macro` and `androidx.profileinstaller` for Baseline Profiles.
- Add `androidx.core:core-splashscreen`.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/app/build.gradle.kts)
- Enable R8 minify (`isMinifyEnabled = true`) and `isShrinkResources = true` for the release build type.
- Add baseline profile dependencies.

#### [MODIFY] [gradle.properties](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/gradle.properties)
- Ensure `org.gradle.configuration-cache=true` and KSP optimizations are enabled.

### App Lifecycle & Startup
Optimizing the `Application` class and startup experience.

#### [MODIFY] [EscapeApplication.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/app/src/main/java/com/geecee/escapelauncher/EscapeApplication.kt) (or equivalent Application class)
- Remove any blocking initialization in `onCreate()`.
- Shift heavy initializations (like database, analytics) to lazy delegates or background coroutines using `App Startup` or `WorkManager`.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/app/src/main/java/com/geecee/escapelauncher/MainActivity.kt)
- Implement `installSplashScreen()` from the new Splash Screen API.
- Review `singleTask` launch mode and back/home button intent handling to ensure instant resume.

### Design System: Paper / E-ink (Japanese Principles)
Redefining the design tokens for Ma (space), Kanso (simplicity), Wabi-sabi, and Shibui.

#### [MODIFY] [Color.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/theme/src/main/java/com/geecee/escapelauncher/core/theme/Color.kt)
- Define low-saturation, calm color palettes (creams, warm grays, muted ink blacks).
- Add support for visual decay (grayscale matrices for app icons).

#### [MODIFY] [Typography.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/theme/src/main/java/com/geecee/escapelauncher/core/theme/Typography.kt)
- Adjust the type scale to emphasize typographic hierarchy with generous line height (Ma/negative space).
- Ensure high contrast for accessibility while maintaining a warm, understated look.

#### [MODIFY] [Theme.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/theme/src/main/java/com/geecee/escapelauncher/core/theme/Theme.kt)
- Implement unified design tokens for spacing (4/8dp baseline grid), corner radii, and elevation (using subtle glassmorphism or flat paper layers instead of harsh drop shadows).

### App Opening Friction & Work Profiles
Implementing the core well-being features and integrating work apps.

#### [MODIFY] [feature/appslist/...]
- **App Drawer:** Integrate Work Profile apps (using `LauncherApps.getProfiles()`) and display them with standard work badges.
- **Positional Friction:** Implement logic to bury "friction" apps at the bottom of the drawer.
- **Visual Decay:** Apply a ColorMatrix to desaturate icons of frequently used apps.

#### [MODIFY] [feature/homescreen/...]
- **Intent Interceptor:** When launching a "friction" app, intercept the click.
- Implement the "Intent Prompt" ("Why are you opening this?").
- Implement the 5-10 second breathing countdown delay.
- Implement the Budget display showing minutes used today.

### Cognitive Plan Removal
Cleaning up old features.

#### [DELETE] [feature/newwidgets/...] (If exclusively for cognitive widgets)
- Delete UI components and data models related to the cognitive questions/widgets.
- Retain models and UI for Music/Movie suggestions.

## Verification Plan

### Automated Tests
- Create macrobenchmarks to verify startup time improvements and scroll jank metrics.
- Ensure unit tests for the Intent Interceptor logic correctly trigger friction based on app configuration.

### Manual Verification
- **Startup:** Launch the app from a cold state; verify the splash screen appears and transitions smoothly without ANRs.
- **Work Profile:** Ensure apps from the work profile appear in the drawer and launch correctly.
- **Friction Flow:** Mark an app as "distracting", attempt to launch it, and verify the intent prompt, 10s delay, and budget display appear before the app actually opens.
- **Design Review:** Verify the UI reflects the low-saturation, high-spacing "Paper" aesthetic.