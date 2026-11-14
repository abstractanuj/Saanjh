# Fix stuck scroll and improve adaptivity on Contacts Screen

The user reports that scrolling "gets stuck" on the contacts page and requests making the app "adaptive to all screens... sizes.. dpi..". This plan addresses both by improving the grid layout and ensuring proper nested scroll coordination with the main pager.

## User Review Required

> [!IMPORTANT]
> The number of columns in the contacts grid will now change based on screen width (e.g., 3 columns on tablets, 2 on phones). This is to fulfill the "adaptive" requirement.

## Proposed Changes

### Feature: Home Screen

#### [MODIFY] [ContactsScreen.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/ContactsScreen.kt)
- Add `onScrollStateChanged` callback to disable/enable parent pager scrolling.
- Implement `LaunchedEffect` to notify when scrolling is in progress.
- Replace `StaggeredGridCells.Fixed(2)` with a dynamic calculation using `LocalConfiguration` to support different screen sizes.
- Optimize `allTiles` logic for better performance.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/Anuj/Downloads/Escape-Launcher-refactor/Escape-Launcher-refactor/feature/homescreen/src/main/java/com/geecee/escapelauncher/feature/homescreen/Home.kt)
- Pass the `onScrollStateChanged` callback to `ContactsScreen`.

## Verification Plan

### Automated Tests
- Run existing unit tests for `ContactsViewModel`.

### Manual Verification
- Deploy to an emulator or device.
- Verify that vertical scrolling in the contacts screen is smooth and does not get "stuck".
- Test on different screen sizes (phone vs tablet) or by resizing the emulator to verify the adaptive column count.
- Verify that diagonal swiping on the contacts screen doesn't accidentally trigger a page switch in the `HorizontalPager`.
