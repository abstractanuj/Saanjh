package com.geecee.escapelauncher.core.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geecee.escapelauncher.core.domain.repository.TodaysPicksRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Worker to refresh "Today's Picks" data periodically.
 */
@HiltWorker
class TodaysPicksRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: TodaysPicksRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {

        return try {
            val result = repository.refreshTodaysPicks()
            if (result.isSuccess) {

                Result.success()
            } else {
                Timber.w("Failed to refresh Today's Picks: %s", result.exceptionOrNull()?.message)
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in TodaysPicksRefreshWorker")
            Result.retry()
        }
    }
}
