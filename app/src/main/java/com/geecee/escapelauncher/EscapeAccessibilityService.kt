package com.geecee.escapelauncher

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("AccessibilityPolicy")
@AndroidEntryPoint
class EscapeAccessibilityService : AccessibilityService() {
    companion object {
        var instance: EscapeAccessibilityService? = null
        private var lastReminderTime = 0L
    }

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private var previousVolume: Int? = null
    private val noisyApps = setOf(
        "com.instagram.lite",
        "com.instagram.android",
        "com.google.android.youtube",
        "app.revanced.android.youtube",
        "com.brave.browser",
        "com.android.chrome",
        "org.mozilla.firefox"
    )

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        instance = this
        startReminderLoop()
    }

    private fun startReminderLoop() {
        serviceScope.launch {
            while (true) {
                delay(TimeUnit.MINUTES.toMillis(15))
                val now = System.currentTimeMillis()
                if (now - lastReminderTime > TimeUnit.HOURS.toMillis(2)) {
                    val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
                    if (powerManager.isInteractive) {
                        triggerReminder()
                        lastReminderTime = now
                    }
                }
            }
        }
    }

    private fun triggerReminder() {
        val intent = Intent().apply {
            setClassName(this@EscapeAccessibilityService, "com.geecee.escapelauncher.GlassmorphicReminderActivity")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to launch reminder")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return

            // Ignore system UI and our own app
            if (packageName == "com.android.systemui" || packageName == applicationContext.packageName) return

            // Noisy App Muting Logic
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            if (noisyApps.contains(packageName)) {
                if (previousVolume == null) {
                    previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0)
                    Timber.tag("Accessibility").d("Muted $packageName (was $previousVolume)")
                }
            } else {
                previousVolume?.let { vol ->
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, 0)
                    Timber.tag("Accessibility").d("Restored volume to $vol (left noisy app)")
                    previousVolume = null
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onInterrupt() {
    }

    fun lockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN)
        }
    }

    fun performBack() {
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performRecents() {
        performGlobalAction(GLOBAL_ACTION_RECENTS)
    }
}
