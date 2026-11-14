# Implementation Plan - Modern Home Screen Refinement

This plan focuses on refining the home screen layout to fit on a single page and updating the app list tiles to match the home screen card styling (rounded corners, consistent spacing, and monochromatic palette).

## User Review Required

> [!IMPORTANT]
> To fit everything on one page, I'll be reducing some vertical margins and paddings.
> I will also update `MasonryAppItem` to use the same `RoundedCornerShape(28.dp)` and `surfaceContainerLow` color as the home screen cards.

## Proposed Changes

### [Core UI]

#### [ModernHomeComposables.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/ui/src/main/java/com/geecee/escapelauncher/core/ui/composables/ModernHomeComposables.kt)
- Adjust `GreetingHeader` padding (reduce from 24dp to 12dp).
- Adjust `ModernTopBar` padding.
- Reduce spacing between components in `ModernHomeScreenContent` (from 16dp to 8dp-12dp).

#### [MasonryAppItem.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/ui/src/main/java/com/geecee/escapelauncher/core/ui/composables/MasonryAppItem.kt)
- Update `shape` from `RoundedCornerShape(2.dp)` to `RoundedCornerShape(28.dp)`.
- Update `tileColor` to use `MaterialTheme.colorScheme.surfaceContainerLow` by default, matching the cards in the image.
- Remove the "deterministic tints" to keep the monochromatic look as seen in the home screen screenshot.

---

### [Feature HomeScreen]

#### [Home.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/Home.kt)
- Wrap `ModernHomeScreenContent` in a `Column` with `verticalScroll` if necessary, but prioritize fitting it in the viewport by reducing spacers.

## Verification Plan

### Automated Tests
- Run `analyze_file` on modified composables.
- Run `gradle_build` to ensure compilation.

### Manual Verification
- Use `render_compose_preview` to verify the "single page" fit and the new tile styling.
