# Escape Launcher Performance & Fluidity Overhaul

This plan outlines a multi-phase approach to transform the Escape Launcher into a high-performance, low-latency "native-feeling" application.

## User Review Required

> [!IMPORTANT]
> **Database Migration**: Phase I involves a Room database schema change. While `exportSchema = false` is currently set, I will ensure a destructive or auto-migration path is handled to avoid crashes for existing users.
> **Experimental APIs**: Some performance optimizations (like Baseline Profiles) require a separate module and specific build configurations.

## Proposed Changes

### Phase I: Persistent Metadata & Repository Optimization
Objective: Eliminate expensive CPU operations during startup and app reloads.

#### [MODIFY] [ModifiedAppEntity.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/entity/ModifiedAppEntity.kt)
- Add `themeColor: Int?` and `installTime: Long?` to the entity.

#### [MODIFY] [ModifiedAppsDao.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/database/ModifiedAppsDao.kt)
- Add methods to update `themeColor` and `installTime` individually.

#### [MODIFY] [AppsRepository.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/repository/AppsRepository.kt)
- Update `performReload` to first check the database for existing metadata.
- Trigger icon color extraction only if the database record is missing or the app was recently updated.

---

### Phase II: Compose Stability & Recomposition Tuning
Objective: Achieve "zero redundant recompositions" in the most scrolled areas (Apps List, Home Screen).

#### [MODIFY] [InstalledApp.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/model/src/main/java/com/geecee/escapelauncher/core/model/InstalledApp.kt)
- Annotate with `@Immutable` to assist the Compose compiler.

#### [MODIFY] [AppsList.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/appslist/src/main/java/com/geecee/escapelauncher/feature/appslist/AppsList.kt)
- Implement `key` in `LazyColumn` and `items` blocks.
- Refactor `AppItem` to accept primitive types or highly stable objects where possible.

---

### Phase III: Interaction Physics & Haptics
Objective: Fine-tune the "feel" of the launcher to match flagship Android expectations.

#### [MODIFY] [Transitions.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/theme/src/main/java/com/geecee/escapelauncher/core/theme/Transitions.kt)
- Refine `EmphasizedDecelerateEasing` and `SmoothSpring` for more organic movement.

---

### Phase IV: Baseline Profiles (Future Task)
Objective: Pre-compile critical code paths to eliminate JIT jank.
- *Note: This requires creating a new module and will be handled as the final polishing step.*

## Verification Plan

### Automated Tests
- Run `AppsRepositoryTest` (if exists) or create a new one to verify metadata persistence.
- Use `Compose Recomposition Highlighter` during manual testing to verify Phase II.

### Manual Verification
- Monitor Logcat for "Startup: Splash screen timeout" warnings to ensure they decrease.
- Perform rapid swipes between Home and Apps List to verify "Zero Jank" swiping.
