package com.geecee.escapelauncher

import android.app.Application
import com.geecee.escapelauncher.core.analytics.AnalyticsProxyImpl
import com.geecee.escapelauncher.core.cloudmessaging.MessagingInitializerImpl
import com.geecee.escapelauncher.core.analytics.analyticsProxy
import com.geecee.escapelauncher.core.cloudmessaging.messagingInitializer
import com.geecee.escapelauncher.core.common.AppLogger
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.geecee.escapelauncher.core.data.worker.ContentGenerationWorker
import com.geecee.escapelauncher.core.data.worker.TodaysPicksRefreshWorker
import com.geecee.escapelauncher.core.data.worker.HourlyReminderWorker
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.geecee.escapelauncher.core.ui.recievers.ScreenStateReceiver

import com.geecee.escapelauncher.core.data.worker.RecommendationWorker

@HiltAndroidApp
class EscapeApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private var reminderJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        
        val screenStateReceiver = ScreenStateReceiver(
            onScreenOn = {
                reminderJob?.cancel()
                reminderJob = applicationScope.launch {
                    delay(3 * 60 * 1000L) // 3 minutes
                    
                    val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
                    val lastReminderTime = sharedPrefs.getLong("lastIphoneReminderTime", 0L)
                    val currentTime = System.currentTimeMillis()
                    
                    if (currentTime - lastReminderTime >= 2 * 60 * 60 * 1000L) { // 2 hours
                        sharedPrefs.edit().putLong("lastIphoneReminderTime", currentTime).apply()
                        
                        val intent = Intent(this@EscapeApplication, GlassmorphicReminderActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    }
                }
            },
            onScreenOff = {
                reminderJob?.cancel()
            }
        )
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(screenStateReceiver, filter)

        applicationScope.launch {
            // Initialize logging
            AppLogger.init()

            // Initialize flavor-specific proxies
            analyticsProxy = AnalyticsProxyImpl()
            messagingInitializer = MessagingInitializerImpl()

            setupContentGenerationWork()
            setupTodaysPicksRefreshWork()
            // HourlyReminderWork replaced by ScreenStateReceiver
            setupRecommendationWork()
        }
    }

    private fun setupRecommendationWork() {
        try {
            // Schedule Movie and Music recommendation workers 
            // In a production app, we would use AlarmManager or calculate precise initial delays 
            // to target exactly 6PM / 8AM / 4PM / 10PM. 
            // For now, we queue periodic work that executes daily, leveraging Android's WorkManager constraints.
            
            // Note: Since WorkManager exact timing is not guaranteed due to Doze mode, 
            // we configure it to run every 12 hours as a generalized approach for music 
            // and 24 hours for movies, with the worker internally checking the time 
            // (or we simply let it run when Android determines it's best).
            
            val workRequest = PeriodicWorkRequestBuilder<RecommendationWorker>(
                12, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "RecommendationWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            AppLogger.e("WorkManager: Error setting up RecommendationWork", e)
        }
    }

    private fun setupHourlyReminderWork() {
        try {
            val workRequest = PeriodicWorkRequestBuilder<HourlyReminderWorker>(
                1, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "HourlyReminderWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            AppLogger.e("WorkManager: Error setting up HourlyReminderWork", e)
        }
    }

    private fun setupContentGenerationWork() {
        try {
            val workRequest = PeriodicWorkRequestBuilder<ContentGenerationWorker>(
                1, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ContentGenerationWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            AppLogger.e("WorkManager: Error setting up ContentGenerationWork", e)
        }
    }

    private fun setupTodaysPicksRefreshWork() {
        try {
            val workRequest = PeriodicWorkRequestBuilder<TodaysPicksRefreshWorker>(
                6, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "TodaysPicksRefreshWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            AppLogger.e("WorkManager: Error setting up TodaysPicksRefreshWork", e)
        }
    }
}
