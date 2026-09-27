package com.geecee.escapelauncher.feature.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.CuriosityRepository
import com.geecee.escapelauncher.core.model.CuriosityContent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CuriosityViewModel @Inject constructor(
    private val repository: CuriosityRepository
) : ViewModel() {

    private val _content = MutableStateFlow<CuriosityContent>(CuriosityContent.None)
    val content: StateFlow<CuriosityContent> = _content

    init {
        refreshContent()
        startHourlyTimer()
    }

    private fun refreshContent() {
        viewModelScope.launch {
            _content.value = repository.getCurrentContent()
        }
    }

    private fun startHourlyTimer() {
        viewModelScope.launch {
            while (true) {
                // Wait until the start of the next hour
                val now = System.currentTimeMillis()
                val nextHour = (now / 3600000 + 1) * 3600000
                delay(nextHour - now + 1000) // +1s buffer
                refreshContent()
            }
        }
    }
}
