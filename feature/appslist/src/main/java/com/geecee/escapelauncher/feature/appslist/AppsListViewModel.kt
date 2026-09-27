package com.geecee.escapelauncher.feature.appslist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.model.InstalledApp
import com.geecee.escapelauncher.core.ui.model.AppAction
import com.geecee.escapelauncher.core.common.fuzzyMatch
import com.geecee.escapelauncher.core.common.getAppShortcuts
import com.geecee.escapelauncher.core.common.isMainUserApp
import com.geecee.escapelauncher.core.common.sortAppsByRelevance
import com.geecee.escapelauncher.core.common.startShortcut
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.geecee.escapelauncher.core.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.Normalizer
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toImmutableList
import com.geecee.escapelauncher.core.model.AppCategory
import com.geecee.escapelauncher.core.common.AppClassifier
import java.util.Calendar

data class AppRow(val apps: ImmutableList<InstalledApp>)

data class AppsListUiState(
    val contextualHeader: String = "",
    val contextualChunks: ImmutableList<AppRow> = emptyList<AppRow>().toImmutableList(),
    val categorizedChunks: ImmutableMap<AppCategory, ImmutableList<AppRow>> = emptyMap<AppCategory, ImmutableList<AppRow>>().toImmutableMap(),
    val recentlyAddedChunks: ImmutableList<AppRow> = emptyList<AppRow>().toImmutableList(),
    val expandedCategories: Set<String> = emptySet(),
    val appIndexMap: ImmutableMap<String, Int> = emptyMap<String, Int>().toImmutableMap()
)

@HiltViewModel
class AppsListViewModel @Inject constructor(
    @ApplicationContext context: Context,
    settingsRepository: SettingsRepository,
    appsRepository: AppsRepository,
    private val modifiedAppsRepository: ModifiedAppsRepository,
) : ViewModel() {
    // UI Events
    private val _uiEvent = MutableSharedFlow<AppsListUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val settings = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // region Settings
    val showSearchBox = settingsRepository.settings.map { it.showSearchBox }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val searchAutoOpen = settingsRepository.settings.map { it.searchAutoOpen }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val bottomSearch = settingsRepository.settings.map { it.bottomSearch }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val automaticallyOpenAppsInSearch = settingsRepository.settings.map { it.automaticallyOpenAppsInSearch }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val hiddenAppsInSearch = settingsRepository.settings.map { it.showHiddenAppsInSearch }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    
    val hapticFeedBackEnabled = settingsRepository.settings.map { it.hapticFeedBackEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    
    val physicalLauncherMode = settingsRepository.settings.map { it.physicalLauncherMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    // endregion

    // region Selection for Physical Launcher Mode
    private val _selectedIndex = MutableStateFlow(-1)
    val selectedIndex: StateFlow<Int> = _selectedIndex.asStateFlow()

    fun selectNextApp(itemCount: Int) {
        if (itemCount == 0) return
        _selectedIndex.value = (_selectedIndex.value + 1) % itemCount
    }

    fun selectPreviousApp(itemCount: Int) {
        if (itemCount == 0) return
        _selectedIndex.value = if (_selectedIndex.value <= 0) itemCount - 1 else _selectedIndex.value - 1
    }

    fun resetSelection() {
        _selectedIndex.value = -1
    }
    // endregion

    // region Search & Categorization
    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText.asStateFlow()
    private val _searchExpanded = MutableStateFlow(false)
    val searchExpanded: StateFlow<Boolean> = _searchExpanded.asStateFlow()

    fun onSearchTextChanged(query: String) {
        _searchText.value = query
    }
    
    fun onSearchExpandedChanged(expanded: Boolean) {
        _searchExpanded.value = expanded
        if (!_searchExpanded.value) {
            _searchText.value = ""
        }
    }
    
    fun clearSearch() {
        _searchText.value = ""
    }

    // Moved uiState lower so it can reference the initialized StateFlows

    private val searchTrieFlow: StateFlow<AppSearchTrie> = appsRepository.mainUserApps
        .map { apps ->
            withContext(Dispatchers.Default) {
                val trie = AppSearchTrie()
                apps.forEach { app ->
                    val normalized = Normalizer.normalize(app.displayName, Normalizer.Form.NFD)
                        .replace(Regex("\\p{M}+"), "")
                        .lowercase()
                    trie.insert(app, normalized)
                }
                trie
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSearchTrie())

    private val manualCategories: StateFlow<Map<String, String>> = modifiedAppsRepository.getCategoriesMapFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val recentlyInstalledApps: StateFlow<ImmutableList<InstalledApp>> = appsRepository.mainUserApps
        .map { apps ->
            // Past 4 days logic
            val fourDaysAgo = System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000L)
            apps.filter { it.installTime >= fourDaysAgo }
                .sortedByDescending { it.installTime }
                .toImmutableList()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList<InstalledApp>().toImmutableList())

    private val _expandedCategories = MutableStateFlow<Set<String>>(emptySet())
    val expandedCategories: StateFlow<Set<String>> = _expandedCategories.asStateFlow()

    fun toggleCategory(category: String) {
        _expandedCategories.value = if (_expandedCategories.value.contains(category)) {
            _expandedCategories.value - category
        } else {
            _expandedCategories.value + category
        }
    }

    fun collapseAllCategories() {
        _expandedCategories.value = emptySet()
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val categorizedApps: StateFlow<ImmutableMap<AppCategory, ImmutableList<InstalledApp>>> = combine(
        searchTrieFlow,
        appsRepository.mainUserApps,
        modifiedAppsRepository.getHiddenPackageIdsFlow(),
        _searchText,
        hiddenAppsInSearch,
        manualCategories
    ) { args: Array<Any> ->
        val searchTrie = args[0] as AppSearchTrie
        @Suppress("UNCHECKED_CAST") val allApps = args[1] as List<InstalledApp>
        @Suppress("UNCHECKED_CAST") val hiddenIds = args[2] as List<String>
        val rawQuery = args[3] as String
        val showHidden = args[4] as Boolean
        @Suppress("UNCHECKED_CAST") val manualOverrides = args[5] as Map<String, String>

        val query = rawQuery.trim()
        val hiddenSet = hiddenIds.toSet()

        val filtered = if (query.isBlank()) {
            allApps
                .filter { app ->
                    val isHidden = hiddenSet.contains(app.packageName)
                    (!isHidden || showHidden)
                }
        } else {
            val normalizedQuery = Normalizer.normalize(query, Normalizer.Form.NFD)
                .replace(Regex("\\p{M}+"), "")
                .lowercase()

            val matchedApps = searchTrie.search(normalizedQuery)
            matchedApps.filter { app ->
                val isHidden = hiddenSet.contains(app.packageName)
                (!isHidden || showHidden)
            }.sortedBy { it.displayName.lowercase() }
        }
        
        filtered.groupBy { 
            AppClassifier.classify(it.packageName, manualOverrides)
        }.mapValues { it.value.toImmutableList() }
         .toSortedMap()
         .toImmutableMap()
    }.onEach { map ->
        if (searchText.value.isNotEmpty()) {
            _expandedCategories.value = map.keys.map { cat -> cat.name }.toSet()
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap<AppCategory, ImmutableList<InstalledApp>>().toImmutableMap()
    )

    val uiState: StateFlow<AppsListUiState> = combine(
        categorizedApps,
        recentlyInstalledApps,
        expandedCategories,
        _searchText
    ) { categories, recent, expanded, query ->
        // Time logic for Contextual Apps
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        
        var contextHeader = ""
        val contextualApps = mutableListOf<InstalledApp>()
        
        if (isWeekend) {
            contextHeader = "WEEKEND LEISURE"
            val validCategories = setOf(AppCategory.ENTERTAINMENT, AppCategory.SOCIAL, AppCategory.TRAVEL)
            categories.forEach { (cat, apps) ->
                if (cat in validCategories) contextualApps.addAll(apps)
            }
        } else if (hour in 5..11) {
            contextHeader = "MORNING ROUTINE"
            val validCategories = setOf(AppCategory.PRODUCTIVITY, AppCategory.FINANCE, AppCategory.TOOLS)
            categories.forEach { (cat, apps) ->
                if (cat in validCategories) contextualApps.addAll(apps)
            }
        } else if (hour in 18..23 || hour < 5) {
            contextHeader = "EVENING UNWIND"
            val validCategories = setOf(AppCategory.ENTERTAINMENT, AppCategory.SOCIAL)
            categories.forEach { (cat, apps) ->
                if (cat in validCategories) contextualApps.addAll(apps)
            }
        } else {
            contextHeader = "AFTERNOON HUSTLE"
            val validCategories = setOf(AppCategory.PRODUCTIVITY, AppCategory.TOOLS)
            categories.forEach { (cat, apps) ->
                if (cat in validCategories) contextualApps.addAll(apps)
            }
        }
        
        val topContextual = contextualApps.shuffled().take(4)
        val contextualChunksList = topContextual.chunked(2).map { AppRow(it.toImmutableList()) }.toImmutableList()

        // Create chunks
        val categorizedChunksMap = categories.mapValues { entry ->
            entry.value.chunked(2).map { AppRow(it.toImmutableList()) }.toImmutableList()
        }.toImmutableMap()
        
        val recentChunksList = recent.chunked(2).map { AppRow(it.toImmutableList()) }.toImmutableList()

        // Pre-calculate indexes for physical mode
        var index = 0
        val map = mutableMapOf<String, Int>()
        
        // Add contextual apps to index map if search is empty
        if (query.isEmpty()) {
            topContextual.forEach { app ->
                map[app.packageName] = index++
            }
        }
        
        categories.forEach { (category, apps) ->
            if (expanded.contains(category.name) || query.isNotEmpty()) {
                apps.forEach { app ->
                    map[app.packageName] = index++
                }
            } else {
                index += apps.size
            }
        }

        AppsListUiState(
            contextualHeader = contextHeader,
            contextualChunks = contextualChunksList,
            categorizedChunks = categorizedChunksMap,
            recentlyAddedChunks = recentChunksList,
            expandedCategories = expanded,
            appIndexMap = map.toImmutableMap()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppsListUiState())
    // endregion
    
    // region Bottom Sheet State & Actions
    private val _showBottomSheet = MutableStateFlow(false)
    val showBottomSheet: StateFlow<Boolean> = _showBottomSheet.asStateFlow()
    fun setBottomSheetVisible(visibility: Boolean) {
        _showBottomSheet.value = visibility
    }

    private val _bottomSheetApp = MutableStateFlow<InstalledApp?>(null)
    val botttomSheetApp: StateFlow<InstalledApp?> = _bottomSheetApp.asStateFlow()
    fun setBottomSheetApp(app: InstalledApp?) {
        _bottomSheetApp.value = app
    }

    val isBottomSheetAppFavourite: StateFlow<Boolean> = combine(
        _bottomSheetApp,
        modifiedAppsRepository.getFavouriteAppsInOrderFlow()
    ) { app, favourites ->
        app?.let { selectedApp ->
            favourites.any { it.packageId == selectedApp.packageName }
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val doesBottomSheetAppHaveChallenge: StateFlow<Boolean> = combine(
        _bottomSheetApp,
        modifiedAppsRepository.getChallengePackageIdsFlow()
    ) { app, challenges ->
        app?.let { selectedApp ->
            challenges.any { it == selectedApp.packageName }
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val bottomSheetActions: StateFlow<List<AppAction>> = combine(
        _bottomSheetApp,
        isBottomSheetAppFavourite,
        doesBottomSheetAppHaveChallenge
    ) { app, isFavourite, hasChallenge ->
        if (app == null) return@combine emptyList()
        
        val actions = mutableListOf<AppAction>()
        
        actions.add(AppAction(labelRes = R.string.uninstall) { clickedApp ->
            viewModelScope.launch { _uiEvent.emit(AppsListUiEvent.UninstallApp(clickedApp)) }
        })

        actions.add(AppAction(
            labelRes = if (isFavourite) R.string.rem_from_fav else R.string.add_to_fav,
            isVisible = { it.isMainUserApp() }
        ) { clickedApp ->
            viewModelScope.launch {
                if (isFavourite) modifiedAppsRepository.removeFavourite(clickedApp.packageName)
                else {
                    modifiedAppsRepository.addFavourite(clickedApp.packageName)
                    _uiEvent.emit(AppsListUiEvent.NavigateHome)
                }
                _showBottomSheet.value = false
            }
        })

        actions.add(AppAction(labelRes = R.string.hide, isVisible = { it.isMainUserApp() }) { clickedApp ->
            viewModelScope.launch {
                modifiedAppsRepository.setHidden(clickedApp.packageName, true)
                _showBottomSheet.value = false
            }
        })

        actions.add(AppAction(labelRes = R.string.app_info) { clickedApp ->
            viewModelScope.launch { _uiEvent.emit(AppsListUiEvent.ShowAppInfo(clickedApp)) }
        })

        actions.add(AppAction(
            labelRes = R.string.add_open_challenge,
            isVisible = { it.isMainUserApp() && !hasChallenge }
        ) { clickedApp ->
            viewModelScope.launch {
                modifiedAppsRepository.setChallenge(clickedApp.packageName, true)
                _showBottomSheet.value = false
            }
        })

        actions
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shortcutActions: StateFlow<List<AppAction>> = _bottomSheetApp.map { app ->
        if (app == null || !app.isMainUserApp()) return@map emptyList()
        getAppShortcuts(context, app.packageName).map { shortcut ->
            AppAction(label = shortcut.label) { clickedApp ->
                startShortcut(context, clickedApp.packageName, shortcut.id)
                _showBottomSheet.value = false
                viewModelScope.launch { _uiEvent.emit(AppsListUiEvent.NavigateHome) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // endregion

    // region Work Apps
    private val _showWorkApps = MutableStateFlow(false)
    val showWorkApps: StateFlow<Boolean> = _showWorkApps.asStateFlow()
    fun setShowWorkApps(show: Boolean) {
        _showWorkApps.value = show
    }
    // endregion
}

sealed class AppsListUiEvent {
    data object NavigateHome : AppsListUiEvent()
    data class UninstallApp(val app: InstalledApp) : AppsListUiEvent()
    data class ShowAppInfo(val app: InstalledApp) : AppsListUiEvent()
}
