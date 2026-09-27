package com.geecee.escapelauncher.feature.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.domain.usecase.FetchTodaysPicksUseCase
import com.geecee.escapelauncher.core.model.TodaysPicks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the "Today's Picks" feature.
 * Manages the state of articles and videos, and handles interactions like summary toggles.
 */
@HiltViewModel
class TodaysPicksViewModel @Inject constructor(
    private val fetchTodaysPicksUseCase: FetchTodaysPicksUseCase
) : ViewModel() {

    private val _todaysPicks = MutableStateFlow<TodaysPicks?>(null)
    val todaysPicks: StateFlow<TodaysPicks?> = _todaysPicks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> = _error.asStateFlow()

    init {
        loadTodaysPicks()
        refreshIfNeeded()
    }

    private fun refreshIfNeeded() {
        viewModelScope.launch {

            fetchTodaysPicksUseCase.refreshIfNeeded()
        }
    }

    /**
     * Collects the "Today's Picks" data stream.
     * Note: Refreshing is handled periodically by WorkManager.
     */
    fun loadTodaysPicks() {
        viewModelScope.launch {
            _isLoading.value = true
            fetchTodaysPicksUseCase()
                .catch { e ->
                    _error.value = e as? Exception ?: Exception(e.message)
                    _isLoading.value = false
                }
                .collect { picks ->
                    _todaysPicks.value = picks
                    _error.value = null
                    _isLoading.value = false
                }
        }
    }

    /**
     * Toggles the expansion state of an article summary in the local UI state.
     */
    fun toggleArticleSummary(articleId: String) {
        _todaysPicks.value = _todaysPicks.value?.let { current ->
            current.copy(
                articles = current.articles.map { article ->
                    if (article.id == articleId) {
                        article.copy(isSummaryExpanded = !article.isSummaryExpanded)
                    } else {
                        article
                    }
                }
            )
        }
    }
}
