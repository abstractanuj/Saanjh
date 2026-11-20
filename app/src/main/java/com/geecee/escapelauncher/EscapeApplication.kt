package com.geecee.escapelauncher

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.geecee.escapelauncher.core.common.AppLogger
import com.geecee.escapelauncher.core.data.repository.PeopleRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@HiltAndroidApp
class EscapeApplication : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var nudgeLoopJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        AppLogger.init()
        startAwarenessNudgeObserver()
    }

    private fun startAwarenessNudgeObserver() {
        startNudgeLoop()

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(
            object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        Intent.ACTION_SCREEN_ON -> startNudgeLoop()
                        Intent.ACTION_SCREEN_OFF -> {
                            nudgeLoopJob?.cancel()
                            nudgeLoopJob = null
                        }
                    }
                }
            },
            filter
        )
    }

    private fun startNudgeLoop() {
        if (nudgeLoopJob?.isActive == true) return
        nudgeLoopJob = appScope.launch {
            while (isActive) {
                // Show periodically every 20 minutes while the screen remains active
                delay(20 * 60 * 1000L)
                val prefs = getSharedPreferences(PeopleRepository.PREFS_NAME, Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("awareness_nudge_enabled", true)
                if (enabled) {
                    try {
                        startActivity(
                            Intent(this@EscapeApplication, AwarenessNudgeActivity::class.java).apply {
                                addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                        Intent.FLAG_ACTIVITY_NO_ANIMATION or
                                        Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                                )
                            }
                        )
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }
}
