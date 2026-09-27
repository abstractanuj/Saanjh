package com.geecee.escapelauncher.core.data.repository

import android.content.ComponentName
import android.content.Context
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import com.geecee.escapelauncher.core.common.AppLogger
import com.geecee.escapelauncher.core.data.service.EscapeNotificationListenerService
import com.geecee.escapelauncher.core.model.MediaState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val _mediaState = MutableStateFlow(MediaState())
    val mediaState: StateFlow<MediaState> = _mediaState.asStateFlow()

    private val supportedPackages = setOf(
        "app.rvx.android.apps.youtube.music",
        "com.google.android.apps.youtube.music",
        "de.danoeh.antennapod"
    )

    private var activeController: MediaController? = null

    private val callback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updateState()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updateState()
        }
    }

    private val sessionListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        val controller = controllers?.firstOrNull { it.packageName in supportedPackages }

        
        if (controller != activeController) {
            activeController?.unregisterCallback(callback)
            activeController = controller
            activeController?.registerCallback(callback)

            updateState()
        }
    }

    init {
        try {

            mediaSessionManager.addOnActiveSessionsChangedListener(
                sessionListener,
                ComponentName(context, EscapeNotificationListenerService::class.java)
            )
            // Initial check
            val initialControllers = try {
                mediaSessionManager.getActiveSessions(
                    ComponentName(context, EscapeNotificationListenerService::class.java)
                )
            } catch (e: SecurityException) {
                AppLogger.w("MediaManager: Notification access not granted yet.")
                emptyList()
            }

            sessionListener.onActiveSessionsChanged(initialControllers)
        } catch (e: Exception) {
            AppLogger.e("MediaManager: Failed to add session listener", e)
        }
    }

    private fun updateState() {
        val controller = activeController
        if (controller == null) {

            _mediaState.value = _mediaState.value.copy(isPlaying = false)
            return
        }

        val playbackState = controller.playbackState
        val metadata = controller.metadata
        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        
        // If we have a valid controller but lastActiveTime is 0, initialize it.
        // This ensures the card shows up as "Continue where you left off" even if paused.
        val lastActiveTime = if (isPlaying || _mediaState.value.lastActiveTime == 0L) {
            System.currentTimeMillis()
        } else {
            _mediaState.value.lastActiveTime
        }

        val newState = MediaState(
            isPlaying = isPlaying,
            title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE),
            artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST),
            packageName = controller.packageName,
            lastActiveTime = lastActiveTime
        )
        

        _mediaState.value = newState
    }

    fun playPause() {
        activeController?.let {
            if (it.playbackState?.state == PlaybackState.STATE_PLAYING) {
                it.transportControls.pause()
            } else {
                it.transportControls.play()
            }
        }
    }

    fun skipNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }
}
