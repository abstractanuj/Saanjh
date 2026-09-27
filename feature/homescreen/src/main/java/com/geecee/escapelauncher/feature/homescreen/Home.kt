package com.geecee.escapelauncher.feature.homescreen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.goToAppInfo
import com.geecee.escapelauncher.core.common.uninstallApp
import com.geecee.escapelauncher.core.model.CuriosityContent
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.composables.*
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.core.data.repository.CuriosityRepository
import com.geecee.escapelauncher.feature.homescreen.todayspicks.TodaysPicksScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onAppOpened: (app: InstalledApp) -> Unit = {},
    onGoHomeRequest: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    homeScreenViewModel: NewHomeScreenViewModel = hiltViewModel(),
    navigateHomeEvent: kotlinx.coroutines.flow.Flow<Unit>? = null,
    homePagerState: androidx.compose.foundation.pager.PagerState = rememberPagerState(initialPage = 3001) { 6000 }
) {
    val context = LocalContext.current
    val hapticFeedbackEnabled by homeScreenViewModel.hapticFeedBackEnabled.collectAsStateWithLifecycle(initialValue = true)
    val favoriteApps by homeScreenViewModel.favoriteApps.collectAsStateWithLifecycle(initialValue = emptyList())
    val showBottomSheet by homeScreenViewModel.showBottomSheet.collectAsStateWithLifecycle(initialValue = false)
    val bottomSheetApp by homeScreenViewModel.bottomSheetApp.collectAsStateWithLifecycle(initialValue = null)
    val bottomSheetActions by homeScreenViewModel.bottomSheetActions.collectAsStateWithLifecycle(initialValue = emptyList())
    val shortcutActions by homeScreenViewModel.shortcutActions.collectAsStateWithLifecycle(initialValue = emptyList())

    val showQuickApps by homeScreenViewModel.showQuickApps.collectAsStateWithLifecycle(initialValue = true)

    val homeVAlignment by homeScreenViewModel.homeVAlignment.collectAsStateWithLifecycle(initialValue = Arrangement.Top)

    val haptics = LocalHapticFeedback.current
    
    val pageCount = 3
    val coroutineScope = rememberCoroutineScope()
    
    // Best Practice: Use derivedStateOf for state that changes frequently but only triggers logic at certain thresholds
    val currentPageModulo by remember {
        derivedStateOf { homePagerState.currentPage % pageCount }
    }

    // Function to go to the absolute default home page (page 1 within the current set)
    suspend fun scrollToDefaultPage() {
        val current = homePagerState.currentPage
        val currentModulo = (current % pageCount).let { if (it < 0) it + pageCount else it }
        val target = (current - currentModulo) + 1
        homePagerState.animateScrollToPage(
            page = target,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
            )
        )
    }


    // Handle UI Events from ViewModel
    LaunchedEffect(Unit) {
        homeScreenViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is HomeUiEvent.NavigateHome -> {
                    coroutineScope.launch {
                        scrollToDefaultPage()
                    }
                }
                is HomeUiEvent.UninstallApp -> uninstallApp(context, event.app)
                is HomeUiEvent.ShowAppInfo -> goToAppInfo(context, event.app)
                is HomeUiEvent.NavigateSettings -> onOpenSettings()
            }
        }
    }

    // Native Predictive Back: If not on home page (1), snap back to it.
    PredictiveBackHandler(enabled = currentPageModulo != 1) { progress ->
        try {
            progress.collect { /* Consume progress for future animation support */ }
            scrollToDefaultPage()
        } catch (_: Exception) {
            // Gesture cancelled by user
        }
    }

    // Haptic feedback on page change
    LaunchedEffect(homePagerState.currentPage) {
        if (hapticFeedbackEnabled) {
            haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.TextHandleMove)
        }
    }

    // Global home navigation trigger
    LaunchedEffect(navigateHomeEvent) {
        navigateHomeEvent?.collect {
            coroutineScope.launch {
                scrollToDefaultPage()
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        MetroPanoramaHeader(
            pagerState = homePagerState,
            pageCount = pageCount,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
        )
    }
}
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
            ) {
                // Fixed: Handle potential negative indices for true infinite scrolling
                val actualIndex = (pageIndex % pageCount).let { if (it < 0) it + pageCount else it }
                when (actualIndex) {
                    0 -> ContactsScreen() // People's Page
                    1 -> {
                        ModernHomeScreenContent(
                            favoriteApps = favoriteApps,
                            showQuickApps = showQuickApps,
                            homeVAlignment = homeVAlignment,
                            onAppClick = { app ->
                                onAppOpened(app)
                                doHapticFeedBack(haptics, hapticFeedbackEnabled)
                            },
                            onAppLongClick = { app ->
                                homeScreenViewModel.setBottomSheetApp(app)
                                homeScreenViewModel.setBottomSheetVisible(visibility = true)
                            },
                            onOpenSettings = onOpenSettings
                        )
                    }
                    2 -> TodaysPicksScreen()
                }
            }
        }

        // Bottom Sheet
        AnimatedVisibility(
            visible = showBottomSheet && (bottomSheetApp != null),
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it }
        ) {
            HomeScreenBottomSheet(
                app = bottomSheetApp!!,
                actions = bottomSheetActions,
                onDismissRequest = { homeScreenViewModel.setBottomSheetVisible(visibility = false) },
                shortcutActions = shortcutActions,
                sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden),
            )
        }

        MetroPanoramaHeader(
            pagerState = homePagerState,
            pageCount = pageCount,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuoteMediaPager(
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    mediaViewModel: MediaViewModel = hiltViewModel()
) {
    val mediaState by mediaViewModel.mediaState.collectAsStateWithLifecycle()
    val shouldShowMedia by mediaViewModel.shouldShowMedia.collectAsStateWithLifecycle()

    // 0: Quote, 1: Media (if active)
    val pageCount = if (shouldShowMedia) 2 else 1
    val pagerState = rememberPagerState { pageCount }

    // If media starts playing and the card should be shown, auto-scroll to media page (1)
    LaunchedEffect(shouldShowMedia, mediaState.isPlaying) {
        if (shouldShowMedia && mediaState.isPlaying) {
            // Wait for pager to realize it has more pages if it just expanded from 1 to 2
            delay(100) 
            if (pagerState.pageCount > 1) {
                pagerState.animateScrollToPage(1)
            }
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier.height(250.dp), // Further increased height to ensure no cutoff
        beyondViewportPageCount = 1,
        pageSpacing = 16.dp,
        verticalAlignment = Alignment.Top,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapPositionalThreshold = 0.15f
        )
    ) { page ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    alpha = (1f - kotlin.math.abs(pageOffset)).coerceIn(0f, 1f)
                },
            contentAlignment = Alignment.CenterStart
        ) {
            when (page) {
                0 -> {
                    Text(
                        text = "Life is\nworth\nliving.",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Normal,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            lineHeight = 64.sp,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    val released = withTimeoutOrNull(7500) {
                                        while (true) {
                                            val event = awaitPointerEvent()
                                            if (event.type == PointerEventType.Move) {
                                                val position = event.changes.first().position
                                                if ((position.x < 0) || (position.x > size.width) || (position.y < 0) || (position.y > size.height)) {
                                                    return@withTimeoutOrNull true
                                                }
                                            }
                                            if (event.type == PointerEventType.Release) {
                                                return@withTimeoutOrNull true
                                            }
                                        }
                                    }
                                    if (released == null) {
                                        onOpenSettings()
                                    }
                                }
                            }
                    )
                }
                1 -> {
                    if (shouldShowMedia) {
                        MediaCard(
                            state = mediaState,
                            modifier = Modifier.padding(horizontal = 0.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHomeScreenContent(
    favoriteApps: List<InstalledApp>,
    showQuickApps: Boolean,
    homeVAlignment: Arrangement.Vertical,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
    onOpenSettings: () -> Unit = {},
) {
    val dateText = remember { SimpleDateFormat("EEE · MMM d", Locale.getDefault()).format(Date()) }
    val greeting = remember { getGreeting() }

    Column(
        modifier = modifier
            .fillMaxSize()
            // Removed statusBarsPadding because it's already padded by the pager's contentPadding
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        ModernTopBar(
            date = dateText,
            greeting = greeting,
            modifier = Modifier.padding(top = 0.dp)
        )

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            QuoteMediaPager(
                onOpenSettings = onOpenSettings,
                modifier = Modifier.weight(1f)
            )

            // Isolated Clock: Only this part recomposes every minute
            ClockPill()
        }

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(
            visible = showQuickApps,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            QuickAppsBar(
                apps = favoriteApps,
                onAppClick = onAppClick,
                onAppLongClick = onAppLongClick,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.weight(1f))

        HomeBottomSection()

        Spacer(Modifier.weight(1f))

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun HomeBottomSection(
    curiosityViewModel: CuriosityViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val curiosityContent by curiosityViewModel.content.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        CuriositySection(
            content = curiosityContent,
            onClick = {
                val currentContent = curiosityContent
                when (currentContent) {
                    is CuriosityContent.Music -> {
                        val query = Uri.encode(currentContent.songName)
                        val url = "https://music.youtube.com/search?q=$query"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            setPackage("app.rvx.android.apps.youtube.music")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            try {
                                context.startActivity(fallbackIntent)
                            } catch (e2: Exception) {
                            }
                        }
                    }
                    is CuriosityContent.Movie -> {
                        val query = Uri.encode(currentContent.movieName)
                        val url = "https://letterboxd.com/search/films/$query/"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                    else -> {}
                }
            }
        )
    }
}

@Composable
fun ClockPill() {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    var currentTime by remember { mutableStateOf(timeFormat.format(Date())) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = timeFormat.format(Date())
            // Synchronize with next minute boundary for a native feel
            val delayMillis = 60000L - (System.currentTimeMillis() % 60000L)
            kotlinx.coroutines.delay(delayMillis)
        }
    }

    val parts = currentTime.split(":")
    val hour = parts.getOrNull(0) ?: "00"
    val minute = parts.getOrNull(1) ?: "00"

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(vertical = 20.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = hour,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = minute,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

private fun getGreeting(): String {
    val base = when (Calendar.getInstance()[Calendar.HOUR_OF_DAY]) {
        in 5..11 -> "It's a lovely morning"
        in 12..16 -> "It's a wonderful afternoon"
        in 17..21 -> "It's a beautiful evening"
        else -> "It's a peaceful night"
    }
    return "$base, Anuj"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MetroPanoramaHeader(
    pagerState: PagerState,
    pageCount: Int,
    modifier: Modifier = Modifier
) {
    val titles = listOf("people", "home", "discover")
    val density = LocalDensity.current
    val screenWidthPx = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }

    val parallaxFactor = 0.5f

    Box(modifier = modifier.fillMaxWidth().height(110.dp).padding(top = 0.dp)) { // Removed top padding, letting statusBarsPadding handle it
        val basePage = pagerState.currentPage

        // Render -1 to 2 to ensure smooth full-bleed edge scrolling as titles move out of view
        for (i in -1..2) {
            val pageIndex = basePage + i
            val actualIndex = (pageIndex % pageCount).let { if (it < 0) it + pageCount else it }
            val title = titles[actualIndex]

            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Light, // Celebrate Typography: Clear, Straightforward
                    fontSize = 72.sp, // Full Bleed Canvas
                    letterSpacing = (-4).sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .graphicsLayer {
                        val offsetFraction = pagerState.currentPageOffsetFraction
                        // hierarchy & motion: fade out titles that aren't the primary focus
                        val distance = abs(i - offsetFraction)
                        alpha = (1f - (distance * 0.7f)).coerceIn(0.15f, 1f)
                        
                        // Parallax sliding
                        translationX = (i - offsetFraction) * screenWidthPx * parallaxFactor
                    }
                    // Remove start padding so the active text aligns flush left (or starts bleeding off screen naturally)
                    .padding(start = 0.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun ModernHomeScreenContentPreview() {
    val apps = listOf(
        InstalledApp("Calculator", "com.calc", android.content.ComponentName("com.calc", "Main"), themeColor = 0xFF00ABA9.toInt()),
        InstalledApp("Calendar", "com.cal", android.content.ComponentName("com.cal", "Main"), themeColor = 0xFF2D89EF.toInt()),
        InstalledApp("Camera", "com.cam", android.content.ComponentName("com.cam", "Main"), themeColor = 0xFF603CBA.toInt())
    )
    MaterialTheme {
        ModernHomeScreenContent(
            favoriteApps = apps,
            showQuickApps = true,
            homeVAlignment = Arrangement.Top,
            onAppClick = {},
            onAppLongClick = {},
        )
    }
}
