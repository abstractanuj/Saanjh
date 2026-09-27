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
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class EscapeApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        // Initialize logging
        AppLogger.init()

        // Initialize flavor-specific proxies
        analyticsProxy = AnalyticsProxyImpl()
        messagingInitializer = MessagingInitializerImpl()

        setupContentGenerationWork()
        setupTodaysPicksRefreshWork()
    }

    private fun setupContentGenerationWork() {
        val workRequest = PeriodicWorkRequestBuilder<ContentGenerationWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ContentGenerationWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    private fun setupTodaysPicksRefreshWork() {
        val workRequest = PeriodicWorkRequestBuilder<TodaysPicksRefreshWorker>(
            6, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TodaysPicksRefreshWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
