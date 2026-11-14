# Implementation Plan - Unresponsiveness and Hang Fixes

The user reports that the app becomes unresponsive (stuck on screen, no clicks/swipes) after long usage, long screen off, or overnight. It eventually starts working after a few minutes. This suggests potential main thread blockages or resource saturation (CPU/IO) during app resumption or process restart.

## Proposed Changes

### Core Data & Infrastructure

#### [MODIFY] [AppsRepository.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/repository/AppsRepository.kt)
- Add a `Mutex` to `performReload` to ensure only one reload operation runs at a time.
- Use `withContext(Dispatchers.IO)` more effectively and ensure that if a reload is already in progress, new requests are queued or the current one is cancelled if appropriate. (Actually, a Mutex is better for "run one at a time").
- Add timing logs to `performReload` to help diagnose long-running reloads.

#### [MODIFY] [IconColorCache.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/repository/IconColorCache.kt)
- Use `ConcurrentHashMap` instead of `mutableMapOf` for thread safety.
- **Optimization**: Currently, colors are re-extracted on every process start. I will modify it to accept a "pre-calculated" color if available, and eventually look into persisting these colors.
- For now, I'll focus on making it thread-safe and ensuring it doesn't block.

#### [MODIFY] [GeminiCategorizer.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/repository/GeminiCategorizer.kt)
- Wrap the synchronous `httpClient.newCall(request).execute()` call in `withContext(Dispatchers.IO)` to ensure it never blocks the caller's thread (especially if called from `Main`).

### Feature: Home Screen

#### [MODIFY] [MainHomeScreenActivity.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/app/src/main/java/com/geecee/escapelauncher/MainHomeScreenActivity.kt)
- Ensure all lifecycle-aware collections and initializations are done off the main thread where possible.
- Add lifecycle logging to track "warm start" performance.

### Fail-safe & Guard Rails

- Implement a "Startup Guard" that logs if initialization takes too long.
- Add more robust error handling in `performReload` to catch potential system service hangs.

## Verification Plan

### Automated Tests
- N/A (Mostly lifecycle and concurrency fixes).

### Manual Verification
- Deploy the app and monitor logs during startup and resumption.
- Simulate a large number of installed apps to see if `performReload` causes any noticeable hitch.
- Test "warm start" (resuming after backgrounding) and "cold start" (process restart) to ensure smoothness.
