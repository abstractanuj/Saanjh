package com.geecee.escapelauncher.feature.widgets

import android.content.Context
import android.content.pm.LauncherApps
import android.graphics.drawable.Drawable
import android.util.LruCache
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.common.fuzzyMatch
import com.geecee.escapelauncher.core.data.repository.AppsRepository
import com.geecee.escapelauncher.core.data.repository.ContactsRepository
import com.geecee.escapelauncher.core.model.ContactData
import com.geecee.escapelauncher.core.model.InstalledApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

sealed interface KhojSideEffect {
    data class LaunchApp(val app: InstalledApp) : KhojSideEffect
    data class DialNumber(val phoneNumber: String) : KhojSideEffect
    data class LaunchWhatsApp(val phoneNumber: String) : KhojSideEffect
    data class ShowError(val messageId: Int) : KhojSideEffect
}

@HiltViewModel
class KhojSearchViewModel @Inject constructor(
    private val appsRepository: AppsRepository,
    private val contactsRepository: ContactsRepository
) : ViewModel() {

    private val iconCache = LruCache<String, Drawable>(50)

    val iconPackManager = appsRepository.iconPackManager

    private val _sideEffects = Channel<KhojSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    val iconPackReady = appsRepository.iconPackManager.iconPackReady.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    @OptIn(FlowPreview::class)
    val filteredApps: StateFlow<List<InstalledApp>> = combine(
        appsRepository.installedApps,
        _searchQuery.debounce(250.milliseconds)
    ) { apps, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            apps.filter { app ->
                fuzzyMatch(app.displayName, query)
            }.sortedBy { it.displayName }.take(12)
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(FlowPreview::class)
    val filteredContacts: StateFlow<List<ContactData>> = _searchQuery
        .debounce(250.milliseconds)
        .mapLatest { query ->
            if (query.isBlank()) emptyList() else contactsRepository.searchContacts(query).take(10)
        }
        .flowOn(Dispatchers.IO)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onAppClicked(app: InstalledApp) {
        viewModelScope.launch {
            _sideEffects.send(KhojSideEffect.LaunchApp(app))
        }
    }

    fun onContactCallClicked(contact: ContactData) {
        viewModelScope.launch {
            _sideEffects.send(KhojSideEffect.DialNumber(contact.phoneNumber))
        }
    }

    fun onContactWhatsAppClicked(contact: ContactData) {
        viewModelScope.launch {
            _sideEffects.send(KhojSideEffect.LaunchWhatsApp(contact.phoneNumber))
        }
    }

    fun onError(messageId: Int) {
        viewModelScope.launch {
            _sideEffects.send(KhojSideEffect.ShowError(messageId))
        }
    }
}
