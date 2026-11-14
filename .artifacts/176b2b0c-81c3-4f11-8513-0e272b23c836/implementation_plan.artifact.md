# Fix Today's Picks: YouTube Feeds and Entry Management

This plan addresses the missing YouTube entries and the issue where items are not being deleted or hidden from the "Today's Picks" screen.

## User Review Required

> [!IMPORTANT]
> The plan includes adding a "Dismiss" feature to individual picks. This will involve UI changes (adding a dismiss button or swipe-to-dismiss) and database schema updates.

## Proposed Changes

### [Component] core:data

#### [MODIFY] [TodaysPicksDao](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/database/TodaysPicksDao.kt)
- Add `@Query("DELETE FROM todays_picks_videos WHERE fetchedAt < :cutoffTime")` to clean up old videos.
- Add methods to mark articles and videos as "dismissed" (requires schema update in entities).
- Update select queries to filter out `isDismissed = 1`.

#### [MODIFY] [TodaysPickArticleEntity](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/entity/TodaysPickArticleEntity.kt)
- Add `val isDismissed: Boolean = false`.

#### [MODIFY] [TodaysPickVideoEntity](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/entity/TodaysPickVideoEntity.kt)
- Add `val isDismissed: Boolean = false`.

#### [MODIFY] [TodaysPicksRepositoryImpl](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/core/data/src/main/java/com/geecee/escapelauncher/core/data/repository/TodaysPicksRepositoryImpl.kt)
- Update `youtubeSources` with correct, verified Channel IDs.
- Call `deleteOldArticles` and `deleteOldVideos` during `refreshTodaysPicks`.
- Implement `dismissArticle(id: String)` and `dismissVideo(id: String)`.

### [Component] feature:homescreen

#### [MODIFY] [TodaysPicksViewModel](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/TodaysPicksViewModel.kt)
- Add `dismissArticle(id: String)` and `dismissVideo(id: String)` methods.

#### [MODIFY] [TodaysPicksScreen](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/todayspicks/TodaysPicksScreen.kt)
- Pass dismiss callbacks to `VideoCard` and `ArticleCard`.

#### [MODIFY] [ArticleCard](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/todayspicks/ArticleCard.kt) & [VideoCard](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/todayspicks/VideoCard.kt)
- Add a dismiss button (e.g., an 'X' in the corner) or implement swipe-to-dismiss.

## Verification Plan

### Automated Tests
- Run `TodaysPicksViewModelTest` after updates.
- Create a new integration test for the Repository to verify cleanup logic.

### Manual Verification
- Deploy to device/emulator.
- Check logs for "YouTube SUCCESS" and "Saved ... to DB".
- Verify that clicking "Dismiss" removes the item from the UI and it doesn't reappear on reload.
- Check that old items (older than 6 hours) are removed from the database during refresh.
