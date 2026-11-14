# Implementation Plan: Curiosity Questions Section

Add a "Curiosity Questions" section to the main home screen, positioned below the favorite apps list and significantly above the navigation bar. This section will display child-like curiosity questions to inspire the user.

## User Review Required

> [!NOTE]
> I will implement a "Curiosity" section that displays a randomly selected question from a predefined list each time the home screen is viewed. This keeps the experience fresh.

> [!IMPORTANT]
> The section will be placed at the bottom of the home screen content, with a 64dp spacer below it to ensure it sits "significantly above" the system navigation bar, as requested.

## Proposed Changes

### [Component: UI]

#### [NEW] [CuriositySection.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/ui/src/main/java/com/geecee/escapelauncher/core/ui/composables/CuriositySection.kt)
- Create a new Composable `CuriositySection` that displays a label "CURIOSITY" and a random question.
- Include a list of questions related to space, nature, animals, and existence.

#### [MODIFY] [Home.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/Home.kt)
- Update `ModernHomeScreenContent` to include the `CuriositySection`.
- Position it at the bottom of the `Column`, below the `Spacer(Modifier.weight(1f))`.
- Add a `Spacer(Modifier.height(64.dp))` at the very bottom to maintain distance from the navigation bar.

## Verification Plan

### Automated Tests
- I'll run `gradle_build` to ensure the project still compiles after the changes.

### Manual Verification
- Render the `ModernHomeScreenContentPreview` to verify the visual placement of the new section.
- I will ask the user to verify the placement on a real device or emulator if possible, or use `take_screenshot` if I can deploy.
