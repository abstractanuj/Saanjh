package com.geecee.escapelauncher.feature.appslist

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.geecee.escapelauncher.core.ui.R
import com.geecee.escapelauncher.core.common.goToAppInfo
import com.geecee.escapelauncher.core.common.uninstallApp
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.theme.BackgroundColor
import com.geecee.escapelauncher.core.theme.ContentColor
import com.geecee.escapelauncher.core.ui.composables.*
import com.geecee.escapelauncher.core.ui.utils.doHapticFeedBack
import com.geecee.escapelauncher.core.ui.utils.deriveSoftBackgroundColor
import com.geecee.escapelauncher.core.ui.model.AppAction
import java.util.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContextualAppItem(
    displayName: String,
    themeColor: Int?,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    // Content, not Chrome. Use pure vibrant colors to delight and personalize.
    val tileColor = remember(themeColor, isDark) {
        if (themeColor != null) {
            Color(themeColor).copy(alpha = if (isDark) 0.8f else 1f)
        } else {
            Color(0xFF0078D7) // Metro Blue fallback
        }
    }
    
    // High contrast typography for readability
    val textColor = remember(tileColor) {
        if (ColorUtils.calculateLuminance(tileColor.toArgb()) > 0.4) Color.Black else Color.White
    }

    // Alive in motion: shrink slightly when selected (Metro tap feedback)
    val scale by animateFloatAsState(if (isSelected) 0.95f else 1f, label = "scale")

    Surface(
        modifier = modifier
            .padding(vertical = 4.dp)
            .aspectRatio(1f) // Authentic Digital: True squares feel organized
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(8.dp), // Balancing Metro (sharp) and personal style (rounded)
        color = tileColor,
        border = if (isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface)
                 else null
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    onClick = onAppClick,
                    onLongClick = onAppLongClick
                )
                .padding(12.dp), // Delightful use of whitespace
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = displayName.lowercase(), // Typography is beautiful, straightforward
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp,
                    letterSpacing = (-0.5).sp
                ),
                color = textColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    displayName: String,
    themeColor: Int?,
    isSelected: Boolean,
    index: Int,
    modifier: Modifier = Modifier,
    onAppClick: () -> Unit,
    onAppLongClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val softBgColor = remember(themeColor, isDark) {
        deriveSoftBackgroundColor(themeColor, isDark)
    }

    // Editorial: Varied shapes based on index to feel "hand-crafted"
    val shape = remember(index) {
        when (index % 6) {
            0 -> RoundedCornerShape(topStart = 32.dp, bottomEnd = 32.dp, topEnd = 8.dp, bottomStart = 8.dp)
            1 -> RoundedCornerShape(topEnd = 32.dp, bottomStart = 32.dp, topStart = 8.dp, bottomEnd = 8.dp)
            2 -> RoundedCornerShape(28.dp)
            3 -> RoundedCornerShape(topStart = 40.dp, topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
            4 -> RoundedCornerShape(topStart = 12.dp, topEnd = 40.dp, bottomEnd = 12.dp, bottomStart = 12.dp)
            else -> RoundedCornerShape(16.dp)
        }
    }

    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f, label = "scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.85f, label = "alpha")

    Surface(
        modifier = modifier
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = shape,
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) 
                else softBgColor.copy(alpha = 0.25f),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)) 
                 else null
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(
                    onClick = onAppClick,
                    onLongClick = onAppLongClick
                )
                .padding(vertical = 22.dp, horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                    fontSize = 16.sp,
                    letterSpacing = 0.2.sp
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Main App List composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    isBeingShown: Boolean,
    onAppOpened: (app: InstalledApp) -> Unit = {},
    onGoHomeRequest: () -> Unit = {},
    appsListViewModel: AppsListViewModel = hiltViewModel(),
    scrollState: androidx.compose.foundation.lazy.LazyListState = androidx.compose.foundation.lazy.rememberLazyListState(),
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val hapticFeedbackEnabled by appsListViewModel.hapticFeedBackEnabled.collectAsStateWithLifecycle(initialValue = true)
    val showSearchBox by appsListViewModel.showSearchBox.collectAsStateWithLifecycle(initialValue = true)
    val bottomSearchBox by appsListViewModel.bottomSearch.collectAsStateWithLifecycle(initialValue = false)
    val autoOpenSearch by appsListViewModel.searchAutoOpen.collectAsStateWithLifecycle(initialValue = false)
    val autoOpenAppInSearch by appsListViewModel.automaticallyOpenAppsInSearch.collectAsStateWithLifecycle(
        initialValue = false,
    )
    val uiState by appsListViewModel.uiState.collectAsStateWithLifecycle()

    val searchText by appsListViewModel.searchText.collectAsStateWithLifecycle()
    val showBottomSheet by appsListViewModel.showBottomSheet.collectAsStateWithLifecycle()
    val bottomSheetApp by appsListViewModel.botttomSheetApp.collectAsStateWithLifecycle()
    val showWorkApps by appsListViewModel.showWorkApps.collectAsStateWithLifecycle()

    val bottomSheetActions by appsListViewModel.bottomSheetActions.collectAsStateWithLifecycle()
    val shortcutActions by appsListViewModel.shortcutActions.collectAsStateWithLifecycle()
    val physicalMode by appsListViewModel.physicalLauncherMode.collectAsStateWithLifecycle()
    val selectedIndex by appsListViewModel.selectedIndex.collectAsStateWithLifecycle()
    val scrollState = androidx.compose.foundation.lazy.rememberLazyListState()

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    // Standard app interaction logic
    val handleAppClick: (InstalledApp) -> Unit = { app ->
        onAppOpened(app)
        appsListViewModel.clearSearch()
        keyboardController?.hide()
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    val handleAppLongClick: (InstalledApp) -> Unit = { app ->
        appsListViewModel.setBottomSheetVisible(visibility = true)
        appsListViewModel.setBottomSheetApp(app)
        doHapticFeedBack(haptics, hapticFeedbackEnabled)
    }

    // Handle UI Events from ViewModel
    LaunchedEffect(Unit) {
        appsListViewModel.uiEvent.collectLatest { event ->
            when (event) {
                is AppsListUiEvent.NavigateHome -> onGoHomeRequest()
                is AppsListUiEvent.UninstallApp -> uninstallApp(context, event.app)
                is AppsListUiEvent.ShowAppInfo -> goToAppInfo(context, event.app)
            }
        }
    }

    // This manages tidying everything when the visibility changes
    LaunchedEffect(isBeingShown) {
        if (!isBeingShown) {
            appsListViewModel.clearSearch()
            appsListViewModel.setBottomSheetVisible(visibility = false)
            appsListViewModel.setShowWorkApps(show = false)
            appsListViewModel.collapseAllCategories()
            // Aggressively hide keyboard and clear focus when hidden
            keyboardController?.hide()
            focusManager.clearFocus()
        } else if (autoOpenSearch) {
            appsListViewModel.onSearchExpandedChanged(expanded = true)
        }
    }

    // Header measurement to set correct top padding for LazyColumn
    var headerHeightPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val headerHeightDp = with(density) { headerHeightPx.toDp() }

    AppsListContent(
        modifier = modifier,
        uiState = uiState,
        hapticFeedbackEnabled = hapticFeedbackEnabled,
        showSearchBox = showSearchBox,
        bottomSearchBox = bottomSearchBox,
        autoOpenSearch = autoOpenSearch,
        autoOpenAppInSearch = autoOpenAppInSearch,
        searchText = searchText,
        showBottomSheet = showBottomSheet,
        bottomSheetApp = bottomSheetApp,
        showWorkApps = showWorkApps,
        bottomSheetActions = bottomSheetActions,
        shortcutActions = shortcutActions,
        scrollState = scrollState,
        physicalMode = physicalMode,
        selectedIndex = selectedIndex,
        headerHeightDp = headerHeightDp,
        onHeaderMeasured = { headerHeightPx = it },
        onToggleCategory = { appsListViewModel.toggleCategory(it) },
        onSearchTextChanged = { query ->
            appsListViewModel.onSearchTextChanged(query)
            if (query.isNotEmpty()) {
                doHapticFeedBack(haptics, hapticFeedbackEnabled)
            }
        },
        onAppClick = handleAppClick,
        onAppLongClick = handleAppLongClick,
        onBottomSheetDismiss = { appsListViewModel.setBottomSheetVisible(visibility = false) },
        onShowWorkApps = { appsListViewModel.setShowWorkApps(show = true) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppsListContent(
    modifier: Modifier = Modifier,
    uiState: AppsListUiState,
    hapticFeedbackEnabled: Boolean,
    showSearchBox: Boolean,
    bottomSearchBox: Boolean,
    autoOpenSearch: Boolean,
    autoOpenAppInSearch: Boolean,
    searchText: String,
    showBottomSheet: Boolean,
    bottomSheetApp: InstalledApp?,
    showWorkApps: Boolean,
    bottomSheetActions: List<AppAction>,
    shortcutActions: List<AppAction>,
    scrollState: androidx.compose.foundation.lazy.LazyListState,
    physicalMode: Boolean,
    selectedIndex: Int,
    headerHeightDp: androidx.compose.ui.unit.Dp,
    onHeaderMeasured: (Int) -> Unit,
    onToggleCategory: (String) -> Unit,
    onSearchTextChanged: (String) -> Unit,
    onAppClick: (InstalledApp) -> Unit,
    onAppLongClick: (InstalledApp) -> Unit,
    onBottomSheetDismiss: () -> Unit,
    onShowWorkApps: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val screenWidth = with(density) { windowInfo.containerSize.width.toDp() }
    var isRecentlyAddedExpanded by remember { mutableStateOf(false) }

    // Responsive padding
    val horizontalPadding by remember(screenWidth) {
        derivedStateOf {
            when {
                screenWidth < 400.dp -> 16.dp
                screenWidth < 600.dp -> 32.dp
                else -> 64.dp
            }
        }
    }

    // Auto-open app logic: if only one app remains in search and setting is enabled
    LaunchedEffect(uiState.categorizedChunks, autoOpenAppInSearch, searchText) {
        val allApps = uiState.categorizedChunks.values.flatMap { it }.flatMap { it.apps }
        if ((autoOpenAppInSearch && searchText.isNotEmpty()) && (allApps.size == 1)) {
            onAppClick(allApps.first())
        }
    }

    Box(
        modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .animateContentSize()
            .imePadding(),
    ) {
        // Editorial: Subtle background text or texture could go here
        
        // The Command Palette (Two Column List)
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            contentPadding = PaddingValues(
                top = headerHeightDp + 32.dp, 
                bottom = if (bottomSearchBox) 140.dp else 48.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Editorial: Add a starting spacer to push content down
            item { Spacer(Modifier.height(16.dp)) }

            // Contextual block
            if (searchText.isEmpty() && uiState.contextualChunks.isNotEmpty()) {
                item { Spacer(Modifier.height(16.dp)) } // Delightful use of whitespace
                
                item {
                    Text(
                        text = uiState.contextualHeader.lowercase(), // Typography!
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                }
                
                items(
                    items = uiState.contextualChunks,
                    key = { row -> "contextual-${row.apps.firstOrNull()?.packageName ?: ""}" }
                ) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.apps.forEach { app ->
                            val currentIndex = uiState.appIndexMap[app.packageName] ?: 0
                            val isSelected = physicalMode && selectedIndex == currentIndex
                            
                            ContextualAppItem(
                                displayName = app.displayName,
                                themeColor = app.themeColor,
                                isSelected = isSelected,
                                modifier = Modifier.weight(1f),
                                onAppClick = { onAppClick(app) },
                                onAppLongClick = { onAppLongClick(app) }
                            )
                        }
                        if (row.apps.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                
                item { Spacer(Modifier.height(16.dp)) }
            }

            uiState.categorizedChunks.forEach { (category, rowChunks) ->
                if (rowChunks.isNotEmpty()) {
                    item(key = "header-${category.name}") {
                        CategoryHeader(
                            category = category,
                            isExpanded = uiState.expandedCategories.contains(category.name) || searchText.isNotEmpty(),
                            onToggle = { onToggleCategory(category.name) }
                        )
                    }

                    if (uiState.expandedCategories.contains(category.name) || searchText.isNotEmpty()) {
                        items(
                            items = rowChunks,
                            key = { row -> "row-${category.name}-${row.apps.firstOrNull()?.packageName ?: ""}" }
                        ) { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.apps.forEachIndexed { index, app ->
                                    val currentIndex = uiState.appIndexMap[app.packageName] ?: 0
                                    val isSelected = physicalMode && selectedIndex == currentIndex
                                    val weight = if (index == 0) 1.1f else 0.9f
                                    
                                    AppItem(
                                        displayName = app.displayName,
                                        themeColor = app.themeColor,
                                        isSelected = isSelected,
                                        index = currentIndex,
                                        modifier = Modifier.weight(weight),
                                        onAppClick = { onAppClick(app) },
                                        onAppLongClick = { onAppLongClick(app) }
                                    )
                                }
                                if (row.apps.size == 1) {
                                    Spacer(modifier = Modifier.weight(0.9f))
                                }
                            }
                        }
                    }
                }
            }

            // Recently Installed Apps moved here
            if (searchText.isEmpty() && uiState.recentlyAddedChunks.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isRecentlyAddedExpanded = !isRecentlyAddedExpanded }
                            .padding(top = 24.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "RECENTLY ADDED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Icon(
                            imageVector = if (isRecentlyAddedExpanded) Icons.Default.KeyboardArrowUp 
                                         else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                if (isRecentlyAddedExpanded) {
                    items(
                        items = uiState.recentlyAddedChunks,
                        key = { row -> "recent-${row.apps.firstOrNull()?.packageName ?: ""}" }
                    ) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.apps.forEachIndexed { index, app ->
                                AppItem(
                                    displayName = app.displayName,
                                    themeColor = app.themeColor,
                                    isSelected = false,
                                    index = index,
                                    modifier = Modifier.weight(1f),
                                    onAppClick = { onAppClick(app) },
                                    onAppLongClick = { onAppLongClick(app) }
                                )
                            }
                            if (row.apps.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Static Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BackgroundColor)
                .onGloballyPositioned { onHeaderMeasured(it.size.height) }
                .statusBarsPadding()
                .padding(horizontal = horizontalPadding)
                .padding(top = 24.dp, bottom = 12.dp)
                .zIndex(3f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Choose with\nintention.",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    lineHeight = 42.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "WHY DID YOU PICK UP THE PHONE?",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            
            if (!bottomSearchBox && showSearchBox) {
                Spacer(Modifier.height(16.dp))
                AnimatedPillSearchBar(
                    closedText = "Search for a tool...",
                    searchText = searchText,
                    isExpanded = true,
                    autoFocus = false,
                    showBackground = true,
                    onExpandedChange = { },
                    onSearchTextChanged = onSearchTextChanged,
                    onSearchDone = { _, keboardController ->
                        val allApps = uiState.categorizedChunks.values.flatMap { it }.flatMap { it.apps }
                        if (allApps.isNotEmpty()) {
                            keboardController?.hide()
                            onAppClick(allApps.first())
                        }
                    }
                )
            }
        }

        // Sticky Bottom Search Bar (if enabled)
        if (showSearchBox && bottomSearchBox) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(BackgroundColor.copy(alpha = 0.9f))
                    .navigationBarsPadding()
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = 16.dp)
                    .zIndex(2f),
                contentAlignment = Alignment.Center
            ) {
                AnimatedPillSearchBar(
                    closedText = "Which tool do you need?",
                    searchText = searchText,
                    isExpanded = true,
                    autoFocus = false,
                    showBackground = true,
                    onExpandedChange = { },
                    onSearchTextChanged = onSearchTextChanged,
                    onSearchDone = { _, keboardController ->
                        val allApps = uiState.categorizedChunks.values.flatMap { it }.flatMap { it.apps }
                        if (allApps.isNotEmpty()) {
                            keboardController?.hide()
                            onAppClick(allApps.first())
                        } else {
                            doHapticFeedBack(haptics, hapticFeedbackEnabled)
                        }
                    }
                )
            }
        }
    }

    // Bottom Sheet
    AnimatedVisibility(showBottomSheet && (bottomSheetApp != null)) {
        HomeScreenBottomSheet(
            app = bottomSheetApp!!,
            actions = bottomSheetActions,
            onDismissRequest = onBottomSheetDismiss,
            shortcutActions = shortcutActions
        )
    }
}

@Composable
fun CategoryHeader(
    category: com.geecee.escapelauncher.core.model.AppCategory,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val index = category.ordinal + 1
    val indexText = if (index < 10) "0$index" else "$index"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(top = 24.dp, bottom = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Minimalist index marker
            Text(
                text = indexText,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    letterSpacing = 1.sp
                )
            )

            // Modern, smaller folder name
            Text(
                text = category.displayName.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    fontSize = 14.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview
@Composable
fun AppsListPreview() {
    Box(modifier = Modifier.background(BackgroundColor)) {
        AppsListContent(
            uiState = AppsListUiState(),
            hapticFeedbackEnabled = true,
            showSearchBox = true,
            bottomSearchBox = true,
            autoOpenSearch = false,
            autoOpenAppInSearch = false,
            searchText = "",
            showBottomSheet = false,
            bottomSheetApp = null,
            showWorkApps = false,
            bottomSheetActions = emptyList(),
            shortcutActions = emptyList(),
            onSearchTextChanged = {},
            onAppClick = {},
            onAppLongClick = {},
            onBottomSheetDismiss = {},
            onShowWorkApps = {},
            scrollState = androidx.compose.foundation.lazy.rememberLazyListState(),
            physicalMode = false,
            selectedIndex = -1,
            headerHeightDp = 100.dp,
            onHeaderMeasured = {},
            onToggleCategory = {}
        )
    }
}
