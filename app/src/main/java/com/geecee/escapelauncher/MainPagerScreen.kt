package com.geecee.escapelauncher

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.common.EscapeAccessibilityService
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.ui.composables.OpenChallenge
import com.geecee.escapelauncher.feature.appslist.AppsList
import com.geecee.escapelauncher.feature.homescreen.HomeScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

enum class DrawerValue { Collapsed, Expanded }

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class
)
@Composable
fun MainPagerScreen(
    viewModel: MainPagerScreenViewModel = hiltViewModel(),
    globalViewModel: GlobalViewModel = hiltViewModel(),
    onOpenSettings: () -> Unit
) {
    val doubleTapToLock by viewModel.doubleTapToLock.collectAsStateWithLifecycle(initialValue = false)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val homePagerState = androidx.compose.foundation.pager.rememberPagerState(initialPage = 3001) { 6000 }

    val draggableState = remember {
        @Suppress("DEPRECATION")
        AnchoredDraggableState(
            initialValue = DrawerValue.Collapsed,
            positionalThreshold = { distance: Float -> distance * 0.4f },
            velocityThreshold = { with(density) { 100.dp.toPx() } },
            snapAnimationSpec = spring(
                dampingRatio = 0.8f,
                stiffness = Spring.StiffnessMedium
            ),
            decayAnimationSpec = androidx.compose.animation.splineBasedDecay(density)
        )
    }

    val anchors = remember(screenHeightPx) {
        DraggableAnchors {
            DrawerValue.Expanded at 0f
            DrawerValue.Collapsed at screenHeightPx
        }
    }

    LaunchedEffect(anchors) {
        draggableState.updateAnchors(anchors)
    }

    LaunchedEffect(Unit) {
        viewModel.drawerCollapseEvent.collectLatest {
            draggableState.animateTo(DrawerValue.Collapsed)
        }
    }

    val nestedScrollConnection = remember(draggableState, screenHeightPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                // If dragging UP (negative delta) and drawer is NOT fully expanded (offset > 0),
                // the drawer consumes the drag first to open.
                return if (delta < 0 && draggableState.offset > 0f) {
                    val consumed = draggableState.dispatchRawDelta(delta)
                    Offset(0f, consumed)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // If dragging DOWN (positive delta) and LazyColumn is at top,
                // the drawer consumes the leftover delta to close.
                val delta = available.y
                return if (delta > 0 && draggableState.offset < screenHeightPx) {
                    val dragConsumed = draggableState.dispatchRawDelta(delta)
                    Offset(0f, dragConsumed)
                } else {
                    Offset.Zero
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (draggableState.offset > 0f && draggableState.offset < screenHeightPx) {
                    if (available.y < -300f) {
                        draggableState.animateTo(DrawerValue.Expanded)
                    } else if (available.y > 300f) {
                        draggableState.animateTo(DrawerValue.Collapsed)
                    } else {
                        val target = if (draggableState.offset < screenHeightPx / 2f) DrawerValue.Expanded else DrawerValue.Collapsed
                        draggableState.animateTo(target)
                    }
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    val isDrawerOpen = draggableState.currentValue == DrawerValue.Expanded || draggableState.targetValue == DrawerValue.Expanded

    BackHandler(enabled = isDrawerOpen) {
        coroutineScope.launch {
            draggableState.animateTo(DrawerValue.Collapsed)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = BackgroundColor)
            .anchoredDraggable(
                state = draggableState,
                orientation = Orientation.Vertical
            )
    ) {
        // LAYER 0: Home Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(doubleTapToLock) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && doubleTapToLock) {
                                EscapeAccessibilityService.instance?.lockScreen() ?: run {
                                    Toast.makeText(
                                        context,
                                        resources.getString(R.string.accessibility_not_granted_msg),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    )
                }
        ) {
            HomeScreen(
                onAppOpened = { app -> viewModel.openApp(app) },
                onGoHomeRequest = { globalViewModel.requestToGoHome() },
                onOpenSettings = { onOpenSettings() },
                navigateHomeEvent = globalViewModel.navigateHomeEvent,
                homePagerState = homePagerState
            )
        }

        // LAYER 0.5: Darkening Scrim
        val progress = if (draggableState.anchors.size > 0) {
            val offset = if (draggableState.offset.isNaN()) screenHeightPx else draggableState.offset
            1f - (offset / screenHeightPx).coerceIn(0f, 1f)
        } else {
            0f
        }
        
        if (progress > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = progress * 0.7f))
                    .zIndex(1f)
            )
        }

        // LAYER 1: App Drawer Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
                .offset {
                    val yOffset = if (draggableState.offset.isNaN()) screenHeightPx.roundToInt() else draggableState.offset.roundToInt()
                    IntOffset(0, yOffset)
                }
                .nestedScroll(nestedScrollConnection)
        ) {
            AppsList(
                isBeingShown = progress > 0.1f,
                scrollState = viewModel.appsListScrollState,
                onGoHomeRequest = { globalViewModel.requestToGoHome() },
                onAppOpened = { app -> viewModel.openApp(app) }
            )
        }

        // Open Challenge
        AnimatedVisibility(
            visible = viewModel.showOpenChallenge.value,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.zIndex(3f)
        ) {
            OpenChallenge(
                haptics = LocalHapticFeedback.current,
                enabled = true,
                openApp = {
                    viewModel.openApp(viewModel.currentSelectedApp.value, overrideChallenge = true)
                    coroutineScope.launch {
                        delay(1000.milliseconds)
                        viewModel.showOpenChallenge.value = false
                    }
                },
                goBack = { viewModel.showOpenChallenge.value = false }
            )
        }
    }
}
