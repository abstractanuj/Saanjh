package com.geecee.escapelauncher.feature.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.MediaManager
import com.geecee.escapelauncher.core.model.MediaState
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
    private val mediaManager: MediaManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val mediaState = mediaManager.mediaState

    private val _shouldShowMedia = MutableStateFlow(false)
    val shouldShowMedia: StateFlow<Boolean> = _shouldShowMedia.asStateFlow()

    init {
        monitorMediaSession()
    }

    private fun monitorMediaSession() {
        viewModelScope.launch {
            combine(mediaState, settingsRepository.enableNowPlaying) { state, enabled ->
                if (!enabled) return@combine false
                
                if (state.isPlaying) return@combine true
                
                if (state.packageName != null && state.lastActiveTime > 0) {
                    val now = System.currentTimeMillis()
                    val tenMinutesMillis = 10 * 60 * 1000L
                    val timeSinceActive = now - state.lastActiveTime
                    
                    if (timeSinceActive < tenMinutesMillis) {
                        // Start a timer to hide it after the remaining time
                        launch {
                            delay(tenMinutesMillis - timeSinceActive + 1000) // Buffer 1s
                            val currentState = mediaState.value
                            if (!currentState.isPlaying && (System.currentTimeMillis() - currentState.lastActiveTime) >= tenMinutesMillis) {
                                _shouldShowMedia.value = false
                            }
                        }
                        return@combine true
                    }
                }
                false
            }.collect { shouldShow ->
                _shouldShowMedia.value = shouldShow
            }
        }
    }

    fun playPause() = mediaManager.playPause()
    fun skipNext() = mediaManager.skipNext()
    fun skipPrevious() = mediaManager.skipPrevious()
}
